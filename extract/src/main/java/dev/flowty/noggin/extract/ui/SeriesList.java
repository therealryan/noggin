package dev.flowty.noggin.extract.ui;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import dev.flowty.noggin.data.Volume;
import dev.flowty.noggin.extract.model.DirectoryRecord;
import dev.flowty.noggin.extract.model.DirectoryRecord.Type;
import dev.flowty.noggin.render.Render;

/**
 * Displays the images in the series currently chosen in the {@link DicomTree}
 */
public class SeriesList {

	private final JList<DirectoryRecord> list = new JList<>();
	private final JSlider slider = new JSlider( JSlider.VERTICAL );
	private final JComponent widget;
	private final JButton export = new JButton( "Export" );

	/***/
	public SeriesList( Preview preview ) {
		JScrollPane ls = new JScrollPane( list,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );

		slider.setInverted( true );
		slider.setEnabled( false );
		list.setEnabled( false );

		export.setEnabled( false );

		rangeSelection( selected -> {
			export.setEnabled( true );
		} );

		export.addActionListener( e -> {

			JFileChooser jfc = new JFileChooser();
			jfc.setCurrentDirectory( new File( "." ) );
			if( jfc.showDialog( export, "Export" ) == JFileChooser.APPROVE_OPTION ) {
				List<DirectoryRecord> selected = selected();
				Rectangle selection = preview.selection();
				Volume volume = new Volume( selection.width, selection.height, selected.size() );
				for( DirectoryRecord dr : selected ) {
					BufferedImage image = dr.getImage();
					byte[] data = ((DataBufferByte) image.getData( selection ).getDataBuffer()).getData();
					volume.with( data );
				}

				Path path = jfc.getSelectedFile().toPath();
				volume.writeNRRD( path );
				Render.serve( path );
			}

		} );

		widget = new JPanel( new BorderLayout() );
		widget.add( slider, BorderLayout.WEST );
		widget.add( ls, BorderLayout.CENTER );
		widget.add( export, BorderLayout.SOUTH );

		list.addListSelectionListener( e -> {
			SwingUtilities.getRootPane( list ).invalidate();

			slider.setEnabled( list.getSelectedIndices().length > 1 );
			slider.setMaximum( list.getSelectedIndices().length - 1 );
			slider.setValue( Math.min( slider.getValue(), slider.getMaximum() ) );
		} );
	}

	/**
	 * Called when new series is clicked in the tree
	 * 
	 * @param series The new series to display
	 */
	void set( DirectoryRecord series ) {
		if( series.getType() == Type.SERIES ) {
			DefaultListModel<DirectoryRecord> m = new DefaultListModel<>();
			m.addAll( series.children() );
			list.setModel( m );
			list.setSelectedIndex( 0 );
			list.setEnabled( true );
		}
		else {
			list.setModel( new DefaultListModel<>() );
			list.setEnabled( false );
		}

		slider.setMinimum( 0 );
		slider.setMaximum( 0 );
		slider.setValue( 0 );
		slider.setEnabled( false );
	}

	/**
	 * @return The widget to put in the gui
	 */
	public JComponent widget() {
		return widget;
	}

	/**
	 * Adds a listener that will be notifed when the list selection changes
	 * 
	 * @param listener The object to appraise of selection changes
	 */
	public void selection( Consumer<DirectoryRecord> listener ) {
		list.addListSelectionListener( e -> Optional
				.ofNullable( list.getSelectedValuesList() )
				.filter( l -> l.size() == 1 )
				.map( l -> l.get( 0 ) )
				.ifPresent( listener ) );

		slider.addChangeListener( e -> {
			List<DirectoryRecord> values = list.getSelectedValuesList();
			if( !values.isEmpty() ) {
				int idx = slider.getValue();
				idx = Math.max( 0, Math.min( idx, values.size() - 1 ) );
				listener.accept( values.get( idx ) );
			}
		} );
	}

	/**
	 * Adds a listener that will be notified when the list selection range changes
	 * 
	 * @param listener The object to appraise of selection changes
	 */
	public void rangeSelection( Consumer<List<DirectoryRecord>> listener ) {
		list.addListSelectionListener( e -> listener.accept( list.getSelectedValuesList() ) );
	}

	/**
	 * @return The list of currently-selected records
	 */
	public List<DirectoryRecord> selected() {
		return list.getSelectedValuesList();
	}
}
