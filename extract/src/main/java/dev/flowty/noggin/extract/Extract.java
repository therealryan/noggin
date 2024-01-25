package dev.flowty.noggin.extract;

import java.nio.file.Path;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.media.DicomDirReader;

public class Extract {

	public static void read( Path dicomdir ) throws Exception {

		DicomDirReader ddr = new DicomDirReader( dicomdir.toFile() );
		Attributes attr = ddr.getFileMetaInformation();

		System.out.println( "Attributes are: " + attr );
	}

}
