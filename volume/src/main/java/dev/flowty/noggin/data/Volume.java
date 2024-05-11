package dev.flowty.noggin.data;

import static java.nio.charset.StandardCharsets.US_ASCII;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;

/**
 * Encapsulates volume data - a 3d array of values
 */
public class Volume {

	private final Metadata metadata;

	private int writeIndex = 0;
	private final byte[] data;

	public Volume( int width, int height, int depth ) {
		metadata = new Metadata( width, height, depth );

		data = new byte[width * height * depth];
	}

	public Volume with( byte[] slice ) {
		System.arraycopy( slice, 0, data, writeIndex, slice.length );
		writeIndex += slice.length;
		return this;
	}

	public void writeNRRD( Path destination ) {
		try( OutputStream out = Files.newOutputStream( destination ) ) {

			out.write( String.format( """
					NRRD0004
					# http://teem.sourceforge.net/nrrd/format.html
					type: uint8
					dimension: 3
					sizes: %s %s %s
					encoding: gzip
					endian: big
					space directions: (1,0,0) (0,1,0) (0,0,1)
					space origin: (0, 0, 0)

					""",
					metadata.width, metadata.height, metadata.depth )
					.getBytes( US_ASCII ) );

			try( GZIPOutputStream gz = new GZIPOutputStream( out ) ) {
				gz.write( data );
			}
		}
		catch( IOException ioe ) {
			throw new UncheckedIOException( "failed to write volume", ioe );
		}
	}
}
