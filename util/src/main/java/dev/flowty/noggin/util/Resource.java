package dev.flowty.noggin.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Classpath resource loading
 */
public class Resource {

	private String name;
	private Function<String, InputStream> loader = ClassLoader
			.getSystemClassLoader()::getResourceAsStream;

	/**
	 * Sets the class that owns the resource. The name is then assumed to be
	 * relative to that class package
	 * 
	 * @param owner The class that owns the resource
	 * @return <code>this</code>
	 */
	public Resource of( Class<?> owner ) {
		if( owner != null ) {
			loader = n -> owner.getResourceAsStream( n );
		}
		else {
			loader = ClassLoader.getSystemClassLoader()::getResourceAsStream;
		}
		return this;
	}

	/**
	 * Sets the name of the resource
	 * 
	 * @param n the new name
	 * @return <code>this</code>
	 */
	public Resource named( String n ) {
		name = n;
		return this;
	}

	/**
	 * @return The resource data, as a stream
	 */
	public InputStream asStream() {
		return loader.apply( name );
	}

	/**
	 * @return The resource bytes
	 */
	public byte[] asBytes() {
		try( ByteArrayOutputStream baos = new ByteArrayOutputStream();
				InputStream is = asStream() ) {
			byte[] buff = new byte[8192];
			int read;
			while( (read = is.read( buff )) != -1 ) {
				baos.write( buff, 0, read );
			}
			return baos.toByteArray();
		}
		catch( IOException ioe ) {
			throw new UncheckedIOException( "Failed to access resource " + name, ioe );
		}
	}

	/**
	 * @return The resource bytes, as a UTF-8 string
	 */
	public String asString() {
		return new String( asBytes(), StandardCharsets.UTF_8 );
	}

	/**
	 * @return The non-empty lines of the resource
	 */
	public Stream<String> asLines() {
		return Stream.of( asString().split( "\n" ) )
				.filter( line -> !line.isEmpty() );
	}
}
