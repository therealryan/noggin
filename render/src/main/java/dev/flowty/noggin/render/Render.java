package dev.flowty.noggin.render;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Stream;

import com.sun.net.httpserver.HttpServer;

import dev.flowty.noggin.util.Resource;

/**
 * Creates javascript applications to render volume data. This is all lifted
 * directly from https://threejs.org/examples/webgl2_materials_texture3d.html
 */
public class Render {

	static final Resource MANIFEST = new Resource().of( Render.class ).named( "manifest.txt" );

	private static final Map<String, String> SUFFIX_CONTENT_TYPES = new TreeMap<>();
	static {
		SUFFIX_CONTENT_TYPES.put( ".html", "text/html; charset=utf-8" );
		SUFFIX_CONTENT_TYPES.put( ".js", "text/javascript" );
		SUFFIX_CONTENT_TYPES.put( ".png", "image/x-png" );
	}

	public static void serve() throws IOException {
		HttpServer server = HttpServer.create( new InetSocketAddress( 34887 ), 0 );

		Stream.concat(
				MANIFEST.asLines().map( r -> "res/" + r ),
				Stream.of( "index.html", "stent.nrrd" ) )
				.peek( System.out::println )
				.forEach( res -> server.createContext( "/" + res, exch -> {
					byte[] content = new Resource().of( Render.class ).named( res ).asBytes();
					Optional.ofNullable( SUFFIX_CONTENT_TYPES.get( res.substring( res.lastIndexOf( '.' ) ) ) )
							.ifPresent( ct -> exch.getResponseHeaders()
									.add( "Content-Type", ct ) );
					exch.sendResponseHeaders( 200, content.length );
					try( OutputStream os = exch.getResponseBody() ) {
						os.write( content );
					}
				} ) );
		server.setExecutor( null ); // creates a default executor
		server.start();

		System.out.println( server.getAddress() );

		System.in.read();
	}

}
