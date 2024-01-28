package dev.flowty.noggin.extract.ui;

import java.util.List;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import dev.flowty.noggin.extract.model.Directory;
import dev.flowty.noggin.extract.model.DirectoryRecord;

class DicomDirTreeModel implements TreeModel {

	private final Directory directory;

	DicomDirTreeModel( Directory directory ) {
		this.directory = directory;
	}

	private static List<DirectoryRecord> children( Object parent ) {
		return ((DirectoryRecord) parent).children();
	}

	@Override
	public Object getRoot() {
		return directory;
	}

	@Override
	public Object getChild( Object parent, int index ) {
		return children( parent ).get( index );
	}

	@Override
	public int getChildCount( Object parent ) {
		return children( parent ).size();
	}

	@Override
	public boolean isLeaf( Object node ) {
		return children( node ).isEmpty();
	}

	@Override
	public void valueForPathChanged( TreePath path, Object newValue ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getIndexOfChild( Object parent, Object child ) {
		return children( parent ).indexOf( child );
	}

	@Override
	public void addTreeModelListener( TreeModelListener l ) {

	}

	@Override
	public void removeTreeModelListener( TreeModelListener l ) {

	}

}
