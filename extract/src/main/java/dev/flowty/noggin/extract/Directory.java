package dev.flowty.noggin.extract;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.media.DicomDirReader;

/**
 * The data from a <code>DICOMDIR</code> file
 */
public class Directory {

	private final DicomDirReader reader;
	private List<DirectoryRecord> roots = null;

	/**
	 * @param file The file to read
	 */
	public Directory(Path file) {
		try {
			reader = new DicomDirReader(file.toFile());
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to read " + file, e);
		}
	}

	/**
	 * @return The root records of the file
	 */
	public List<DirectoryRecord> roots() {
		if (roots == null) {
			roots = new ArrayList<>();

			try {
				Attributes r = reader.readFirstRootDirectoryRecord();
				while (r != null) {
					roots.add(new DirectoryRecord(reader, r));
					r = reader.readNextDirectoryRecord(r);
				}
			} catch (IOException ioe) {
				throw new UncheckedIOException("Failed to read roots", ioe);
			}
		}

		return roots;
	}
}
