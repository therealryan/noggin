package dev.flowty.noggin.extract;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@EnabledIfSystemProperty(named = "dicomdir", matches = ".+")
class ExtractTest {

	private static final Path DICOMDIR = Paths.get(System.getProperty("dicomdir"));

	@Test
	void read() throws Exception {
		Extract.read(DICOMDIR);
	}
}
