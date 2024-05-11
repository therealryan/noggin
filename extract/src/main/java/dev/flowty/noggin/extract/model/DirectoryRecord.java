package dev.flowty.noggin.extract.model;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.media.DicomDirReader;

/**
 * A single record in a <code>DICOMDIR</code> file
 */
public class DirectoryRecord {
	private static final ImageReader IMAGE_READER = ImageIO.getImageReadersByFormatName( "DICOM" )
			.next();

	public enum Type {
		METADATA,
		PATIENT,
		STUDY,
		SERIES,
		SR_DOCUMENT,
		IMAGE,
		PRESENTATION,
		;

		public static Type get( String name ) {
			if( name == null ) {
				return METADATA;
			}
			return valueOf( name.replace( ' ', '_' ) );
		}
	}

	/**
	 * The file reader
	 */
	protected final DicomDirReader reader;
	private final Attributes data;
	private final Type type;

	private List<DirectoryRecord> children = null;

	/**
	 * @param reader The file reader
	 * @param data   The record data
	 */
	DirectoryRecord( DicomDirReader reader, Attributes data ) {
		this.reader = reader;
		this.data = data;
		type = Type.get( data.getString( Tag.DirectoryRecordType ) );
	}

	/**
	 * @return The child of this record
	 */
	public List<DirectoryRecord> children() {
		if( children == null ) {
			children = new ArrayList<>();
			try {
				Attributes child = reader.readLowerDirectoryRecord( data );
				while( child != null ) {
					children.add( new DirectoryRecord( reader, child ) );
					child = reader.readNextDirectoryRecord( child );
				}
			}
			catch( IOException ioe ) {
				throw new UncheckedIOException( "Failed to read child of " + data, ioe );
			}
		}

		return children;
	}

	public Path referencedFile() {
		return Optional.of( data )
				.map( dr -> dr.getStrings( Tag.ReferencedFileID ) )
				.map( s -> {
					Path p = reader.getFile().toPath().getParent();
					for( String e : s ) {
						p = p.resolve( e );
					}
					return p;
				} )
				.orElse( null );
	}

	public String summary() {
		String desc = switch( type ) {
			case PATIENT -> data.getString( Tag.PatientName );
			case STUDY -> data.getString( Tag.StudyDescription );
			case SERIES -> data.getString( Tag.SeriesDescription );
			case SR_DOCUMENT -> String.join( "/", data.getStrings( Tag.ReferencedFileID ) );
			case IMAGE -> String.join( "/", data.getStrings( Tag.ReferencedFileID ) );
			default -> "???";
		};

		return type + " " + desc;
	}

	public Type getType() {
		return type;
	}

	public String dump() {
		return data.toString();
	}

	@Override
	public String toString() {
		return summary();
	}

	/**
	 * Extracts image data, if any is referenced, from the dicom record
	 * 
	 * @return image data, or <code>null</code> if this record does not reference an
	 *         image
	 */
	public BufferedImage getImage() {
		Path file = referencedFile();
		if( file != null ) {
			try( DicomInputStream dis = new DicomInputStream( file.toFile() ) ) {
				synchronized( IMAGE_READER ) {
					IMAGE_READER.setInput( dis );
					return IMAGE_READER.read( 0 );
				}
			}
			catch( Exception e ) {
				throw new IllegalStateException( "Failed to read image data", e );
			}
		}
		return null;
	}

}
