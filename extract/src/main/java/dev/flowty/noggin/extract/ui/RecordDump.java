package dev.flowty.noggin.extract.ui;

import java.awt.Font;
import java.util.stream.Stream;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import dev.flowty.noggin.extract.model.DirectoryRecord;

class RecordDump {

	private static final JTextArea text = new JTextArea( 10, 10 );
	private static final JScrollPane widget = new JScrollPane( text,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );

	RecordDump() {
		text.setEditable( false );
		text.setSize( 100, 100 );
		text.setFont( new Font( Font.MONOSPACED, 0, 12 ) );
	}

	public JComponent widget() {
		return widget;
	}

	public void set( DirectoryRecord dirRec ) {
		if( dirRec != null ) {
			text.setText( dirRec.dump() );
			SwingUtilities.getRootPane( widget ).invalidate();
		}
		else {
			text.setText( "" );
		}

		int c = Stream.of( text.getText().split( "\n" ) )
				.mapToInt( String::length ).max()
				.orElse( 10 );
		c = Math.min( c, 10 );
		c = Math.max( c, 80 );
		text.setColumns( c );
	}
}
