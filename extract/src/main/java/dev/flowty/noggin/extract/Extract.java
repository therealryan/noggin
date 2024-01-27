package dev.flowty.noggin.extract;

import java.nio.file.Path;

/**
 * Extracts data form a dicomdir file
 *
 */
public class Extract {

	/**
	 * Dumps the file's data to stdout
	 * 
	 * @param dicomdir the file
	 */
	public static void read(Path dicomdir) {
		Directory dir = new Directory(dicomdir);
		dir.roots().forEach(root -> root.print("", "IMAGE"::equals));
	}

}
