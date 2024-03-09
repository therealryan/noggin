package dev.flowty.noggin.extract.ui;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.flowty.noggin.data.Volume;
import dev.flowty.noggin.extract.model.Directory;

/**
 * Displays a UI from which volume data can be selected from a dicom file
 * structure
 */
public class VolumeChooser {

	private static final Logger LOG = LoggerFactory.getLogger( VolumeChooser.class );

	/**
	 * Displays a dialog allows the user to select volume data from a dicom file
	 * structure
	 *
	 * @param directory The dicomdir file
	 * @return The chosen data, or <code>null</code> if the selection is cancelled
	 */
	public static Volume extractData( Directory directory ) {
		VolumeChooser vc = new VolumeChooser( directory );
		JFrame frame = new JFrame( "Choose volume data" );
		frame.getContentPane().add( vc.main );
		frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );

		frame.pack();
		frame.setVisible( true );

		AtomicBoolean complete = new AtomicBoolean( false );

		vc.okButton.addActionListener( e -> {
			frame.setVisible( false );
			frame.dispose();
		} );

		frame.addWindowListener( new WindowAdapter() {
			@Override
			public void windowClosed( WindowEvent e ) {
				frame.dispose();
				synchronized( complete ) {
					complete.set( true );
					complete.notify();
				}
			}
		} );

		while( !complete.get() ) {
			try {
				synchronized( complete ) {
					complete.wait();
				}
			}
			catch( InterruptedException e ) {
				LOG.warn( "unexpected", e );
				Thread.currentThread().interrupt();
			}
		}

		return vc.result;
	}

	private final Directory directory;
	private final JPanel main = new JPanel();
	private final DicomTree tree;
	private final SeriesList series;
	private final RecordDump dump;
	private final Preview preview;
	private final JButton okButton = new JButton( "OK" );

	private Volume result;

	private VolumeChooser( Directory directory ) {
		this.directory = directory;

		tree = new DicomTree( directory );
		series = new SeriesList();
		dump = new RecordDump();
		preview = new Preview();

		tree.selection( dr -> {
			series.set( dr );
			dump.set( dr );
			preview.set( dr );
		} );
		series.selection( dr -> {
			dump.set( dr );
			preview.set( dr );
		} );

		JSplitPane east = new JSplitPane( JSplitPane.VERTICAL_SPLIT,
				tree.widget(), dump.widget() );

		JSplitPane split = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT,
				east, preview.widget() );

		main.setLayout( new BorderLayout() );
		main.add( new JLabel( directory.file().toString(), SwingConstants.CENTER ),
				BorderLayout.NORTH );
		main.add( split, BorderLayout.CENTER );
		main.add( series.widget(), BorderLayout.EAST );
		main.add( okButton, BorderLayout.SOUTH );
	}
}
