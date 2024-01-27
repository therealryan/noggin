package dev.flowty.noggin.extract;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/**
 * Exercises {@link Extract}
 */
@EnabledIfSystemProperty(named = "dicomdir", matches = ".+")
@SuppressWarnings("static-method")
class ExtractTest {

	private static final Path DICOMDIR = Paths.get(System.getProperty("dicomdir"));

	/**
	 * Exercises reading a file
	 * 
	 */
	@Test
	void read() {
		Extract.read(DICOMDIR);
	}
}
