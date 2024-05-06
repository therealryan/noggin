package dev.flowty.noggin.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

/**
 * Exercises {@link Render}
 */
class RenderTest {

	/**
	 * Checks that the manifest is accurate
	 * 
	 * @throws IOException on failure
	 */
	@Test
	void manifest() throws IOException {
		String expected = Render.MANIFEST.asString().trim();

		Path res = Paths.get( "src", "main", "resources", "dev", "flowty", "noggin", "render", "res" );
		String actual = Files.walk( res )
				.filter( Files::isRegularFile )
				.map( file -> res.relativize( file ) )
				.map( Path::toString )
				.sorted()
				.collect( Collectors.joining( "\n" ) );

		assertEquals( expected, actual );
	}

	/**
	 * @throws IOException on failure
	 */
	@Test
	void serve() throws IOException {
		Render.serve();
	}
}
