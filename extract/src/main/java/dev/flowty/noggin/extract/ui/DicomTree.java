package dev.flowty.noggin.extract.ui;

import java.util.function.Consumer;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import dev.flowty.noggin.extract.model.Directory;
import dev.flowty.noggin.extract.model.DirectoryRecord;

/**
 * Displays the dicomDirect tree structure, down to the level of series
 */
public class DicomTree {

	private final JTree tree;
	private final JComponent widget;

	public DicomTree( Directory directory ) {
		DicomDirTreeModel model = new DicomDirTreeModel( directory );
		tree = new JTree( model );
		widget = new JScrollPane( tree,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );

		tree.addTreeSelectionListener( e -> {
			SwingUtilities.getRootPane( tree ).invalidate();
		} );
	}

	public JComponent widget() {
		return widget;
	}

	public void selection( Consumer<DirectoryRecord> listener ) {
		tree.addTreeSelectionListener( e -> {
			listener.accept( (DirectoryRecord) e.getNewLeadSelectionPath().getLastPathComponent() );
		} );
	}
}
