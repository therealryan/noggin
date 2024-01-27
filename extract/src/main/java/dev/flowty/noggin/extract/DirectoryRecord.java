package dev.flowty.noggin.extract;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.media.DicomDirReader;

/**
 * A single record in a <code>DICOMDIR</code> file
 */
public class DirectoryRecord {

	private final DicomDirReader reader;
	private final Attributes data;

	private List<DirectoryRecord> children = null;

	/**
	 * @param reader The file reader
	 * @param data   The record data
	 */
	DirectoryRecord(DicomDirReader reader, Attributes data) {
		this.reader = reader;
		this.data = data;
	}

	/**
	 * @return The child of this record
	 */
	public List<DirectoryRecord> children() {
		if (children == null) {
			children = new ArrayList<>();
			try {
				Attributes child = reader.readLowerDirectoryRecord(data);
				while (child != null) {
					children.add(new DirectoryRecord(reader, child));
					child = reader.readNextDirectoryRecord(child);
				}
			} catch (IOException ioe) {
				throw new UncheckedIOException("Failed to read child of " + data, ioe);
			}
		}

		return children;
	}

	/**
	 * Dumps the data structure from here on down
	 * 
	 * @param indent How far to indent the printed data
	 * @param skip   returns true on the types of records to not print
	 */
	public void print(String indent, Predicate<String> skip) {
		if (skip.test(data.getString(Tag.DirectoryRecordType))) {
			// shh
		} else {
			System.out.println(indent + toString());
		}
		children().forEach(c -> c.print(indent + "  ", skip));
	}

	@Override
	public String toString() {
		String type = data.getString(Tag.DirectoryRecordType);
		String desc = switch (type) {
		case "PATIENT" -> data.getString(Tag.PatientName);
		case "STUDY" -> data.getString(Tag.StudyDescription);
		case "SERIES" -> data.getString(Tag.SeriesDescription);
		default -> "unknown!";
		};
		return type + " " + desc;
	}
}
