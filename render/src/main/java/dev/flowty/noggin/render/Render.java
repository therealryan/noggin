package dev.flowty.noggin.render;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.sun.net.httpserver.HttpServer;

import dev.flowty.noggin.data.Metadata;
import dev.flowty.noggin.util.Resource;

/**
 * Creates javascript applications to render volume data. This is all lifted
 * directly from https://threejs.org/examples/webgl2_materials_texture3d.html
 */
public class Render {

	private static final Resource INDEX = new Resource().of( Render.class ).named( "index.html" );
	static final Resource MANIFEST = new Resource().of( Render.class ).named( "manifest.txt" );

	private static HttpServer server;

	private static final Map<String, String> SUFFIX_CONTENT_TYPES = new TreeMap<>();
	static {
		SUFFIX_CONTENT_TYPES.put( ".html", "text/html; charset=utf-8" );
		SUFFIX_CONTENT_TYPES.put( ".js", "text/javascript" );
		SUFFIX_CONTENT_TYPES.put( ".png", "image/x-png" );
	}

	/**
	 * Serves a renderer for a volume file
	 * 
	 * @param nrrd The path to an NRRD file
	 * @return The url to view the renderer
	 * @throws IOException
	 */
	public static String serve( Path nrrd ) {
		try {
			if( server == null ) {
				server = HttpServer.create( new InetSocketAddress( 0 ), 0 );

				// create routes for the static content
				MANIFEST.asLines()
						.map( r -> "res/" + r )
						.forEach( res -> server.createContext( "/" + res, exch -> {
							byte[] content = new Resource().of( Render.class ).named( res ).asBytes();
							Optional.ofNullable( SUFFIX_CONTENT_TYPES
									.get( res.substring( res.lastIndexOf( '.' ) ) ) )
									.ifPresent( ct -> exch.getResponseHeaders()
											.add( "Content-Type", ct ) );
							exch.sendResponseHeaders( 200, content.length );
							try( OutputStream os = exch.getResponseBody() ) {
								os.write( content );
							}
						} ) );
				server.setExecutor( null ); // creates a default executor
				server.start();
			}

			String name = nrrd.getFileName().toString();
			name = name.substring( 0, name.lastIndexOf( '.' ) );
			Path html = nrrd.resolveSibling( name + ".html" );
			Metadata meta = Metadata.extractFrom( nrrd );
			Files.write( html, index( name, meta ).getBytes( UTF_8 ) );

			Stream.of( nrrd, html )
					.forEach( path -> server.createContext( "/" + path.getFileName().toString(), exch -> {
						byte[] content = Files.readAllBytes( path );
						exch.sendResponseHeaders( 200, content.length );
						try( OutputStream os = exch.getResponseBody() ) {
							os.write( content );
						}
					} ) );

			String url = "http://localhost:" + server.getAddress().getPort() + "/" + name + ".html";
			Desktop dsk = Desktop.getDesktop();
			if( dsk != null && dsk.isSupported( Action.BROWSE ) ) {
				try {
					dsk.browse( new URI( url ) );
				}
				catch( URISyntaxException e ) {
					throw new IllegalStateException( "bad url " + url, e );
				}
			}
			else {
				new ProcessBuilder( "xdg-open", url ).start();
			}

			return url;
		}
		catch( IOException e ) {
			e.printStackTrace();
			return null;
		}
	}

	private static String index( String name, Metadata meta ) {
		String template = INDEX.asString();
		return template
				.replace( "${title}", name )
				.replace( "${half_width}", String.valueOf( meta.width / 2 ) )
				.replace( "${half_height}", String.valueOf( meta.height / 2 ) )
				.replace( "${half_depth}", String.valueOf( meta.depth / 2 ) )
				.replace( "${twice_max}", String.valueOf(
						IntStream.of( meta.width, meta.height, meta.depth )
								.max().orElse( 1 ) * 2 ) );
	}
}
