package dev.flowty.noggin.extract.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.nio.file.Path;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.flowty.noggin.extract.model.DirectoryRecord;
import dev.flowty.noggin.extract.model.DirectoryRecord.Type;

/**
 * Displays the currently-selected image from {@link SeriesList}
 */
class Preview {
	private static final Logger LOG = LoggerFactory.getLogger( Preview.class );

	private final JComponent widget;

	private final ImageReader imageReader = ImageIO.getImageReadersByFormatName( "DICOM" ).next();

	private BufferedImage image;

	public Preview() {
		widget = new JPanel() {
			@Override
			public void paint( Graphics g ) {

				g.setColor( Color.LIGHT_GRAY );
				g.fillRect( 0, 0, widget.getWidth(), widget.getHeight() );

				if( image != null ) {
					float hr = (float) widget.getWidth() / image.getWidth();
					float vr = (float) widget.getHeight() / image.getHeight();
					float r = Math.min( hr, vr );
					int ho = (int) (widget.getWidth() - image.getWidth() * r) / 2;
					int vo = (int) (widget.getHeight() - image.getHeight() * r) / 2;
					g.drawImage( image,
							ho, vo,
							(int) (image.getWidth() * r), (int) (image.getHeight() * r),
							null );
				}
				else {
					g.setColor( Color.black );
					g.drawString( "No associated image data!", 20, 20 );
				}
			}
		};
		widget.setMinimumSize( new Dimension( 300, 300 ) );
		widget.setSize( new Dimension( 300, 300 ) );
		widget.setPreferredSize( new Dimension( 300, 300 ) );
	}

	public JComponent widget() {
		return widget;
	}

	void set( DirectoryRecord dr ) {
		Path file = dr.referencedFile();
		if( dr.getType() == Type.SERIES ) {
			image = dr.children().get( 0 ).getImage();
		}
		else {
			image = dr.getImage();
		}
		widget.repaint();
	}
}
