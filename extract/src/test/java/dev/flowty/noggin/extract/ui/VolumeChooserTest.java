package dev.flowty.noggin.extract.ui;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import dev.flowty.noggin.data.Volume;
import dev.flowty.noggin.extract.model.Directory;

@EnabledIfSystemProperty(named = "dicomdir", matches = ".+")
@SuppressWarnings("static-method")
public class VolumeChooserTest {

	private static final Path DICOMDIR = Paths.get( System.getProperty( "dicomdir" ) );

	/**
	 * Exercises reading a file
	 */
	@Test
	void read() {
		Directory dir = new Directory( DICOMDIR );

		Volume vol = VolumeChooser.extractData( dir );

		System.out.println( vol );
	}
}
