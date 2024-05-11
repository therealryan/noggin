package dev.flowty.noggin.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Metadata {

	public final int width;
	public final int height;
	public final int depth;

	public Metadata( int width, int height, int depth ) {
		this.width = width;
		this.height = height;
		this.depth = depth;
	}

	private static final Pattern SIZES = Pattern.compile( "sizes: (\\d+) (\\d+) (\\d+)" );

	public static Metadata extractFrom( Path nrrd ) {
		try( BufferedReader br = new BufferedReader(
				new InputStreamReader( Files.newInputStream( nrrd ) ) ) ) {
			String line;
			while( (line = br.readLine()) != null ) {
				Matcher m = SIZES.matcher( line );
				if( m.find() ) {
					return new Metadata(
							Integer.parseInt( m.group( 1 ) ),
							Integer.parseInt( m.group( 2 ) ),
							Integer.parseInt( m.group( 3 ) ) );
				}
			}
		}
		catch( IOException e ) {
			throw new UncheckedIOException( e );
		}
		return null;
	}
}
