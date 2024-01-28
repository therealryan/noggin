package dev.flowty.noggin.extract.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.nio.file.Path;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.dcm4che3.io.DicomInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.flowty.noggin.extract.model.DirectoryRecord;

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
	}

	public JComponent widget() {
		return widget;
	}

	void set( DirectoryRecord dr ) {
		image = null;
		Path file = dr.referencedFile();

		if( file != null ) {
			try( DicomInputStream dis = new DicomInputStream( file.toFile() ) ) {
				imageReader.setInput( dis );
				image = imageReader.read( 0 );
			}
			catch( Exception e ) {
				LOG.error( "Failed to read " + file, e );
			}
		}

		widget.repaint();
	}
}