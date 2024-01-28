package dev.flowty.noggin.extract.model;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.media.DicomDirReader;

/**
 * A single record in a <code>DICOMDIR</code> file
 */
public class DirectoryRecord {

	/**
	 * The file reader
	 */
	protected final DicomDirReader reader;
	private final Attributes data;

	private List<DirectoryRecord> children = null;

	/**
	 * @param reader The file reader
	 * @param data   The record data
	 */
	DirectoryRecord( DicomDirReader reader, Attributes data ) {
		this.reader = reader;
		this.data = data;
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
		String type = data.getString( Tag.DirectoryRecordType );
		if( type == null ) {
			return "Metadata";
		}
		String desc = switch( type ) {
			case "PATIENT" -> data.getString( Tag.PatientName );
			case "STUDY" -> data.getString( Tag.StudyDescription );
			case "SERIES" -> data.getString( Tag.SeriesDescription );
			case "SR DOCUMENT" -> String.join( "/", data.getStrings( Tag.ReferencedFileID ) );
			case "IMAGE" -> String.join( "/", data.getStrings( Tag.ReferencedFileID ) );
			default -> "???";
		};

		return type + " " + desc;
	}

	public String dump() {
		return data.toString();
	}

	@Override
	public String toString() {
		return summary();
	}
}
