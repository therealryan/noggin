package dev.flowty.noggin.extract.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
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
	private AffineTransform invertedDisplay;

	private double selectionMinX = 0;
	private double selectionMinY = 0;
	private double selectionMaxX = 1;
	private double selectionMaxY = 1;

	private RectangleEditPoint dragging = null;

	public Preview() {
		widget = new JPanel() {
			@Override
			public void paint( Graphics gl ) {
				Graphics2D g = (Graphics2D) gl;
				g.setColor( Color.LIGHT_GRAY );
				g.fillRect( 0, 0, widget.getWidth(), widget.getHeight() );
				invertedDisplay = null;

				if( image != null ) {
					float hr = (float) widget.getWidth() / image.getWidth();
					float vr = (float) widget.getHeight() / image.getHeight();
					float r = Math.min( hr, vr );
					int ho = (int) (widget.getWidth() - image.getWidth() * r) / 2;
					int vo = (int) (widget.getHeight() - image.getHeight() * r) / 2;

					AffineTransform before = g.getTransform();

					g.translate( ho, vo );
					g.scale( r, r );

					g.drawImage( image, 0, 0, image.getWidth(), image.getHeight(), null );

					g.setColor( Color.green );
					Rectangle sr = selection();
					g.drawRect( sr.x, sr.y, sr.width, sr.height );

					try {
						invertedDisplay = g.getTransform().createInverse();
					}
					catch( NoninvertibleTransformException e ) {
						throw new IllegalStateException( e );
					}

					g.setTransform( before );
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

		widget.addMouseListener( new MouseAdapter() {
			@Override
			public void mousePressed( MouseEvent e ) {
				if( invertedDisplay != null ) {
					Point2D.Double ep = new Point2D.Double( e.getX(), e.getY() );
					invertedDisplay.transform( ep, ep );
					dragging = RectangleEditPoint.closest( selection(), ep.getX(), ep.getY() );
				}
			}

			@Override
			public void mouseReleased( MouseEvent e ) {
				dragging = null;
			}
		} );
		widget.addMouseMotionListener( new MouseAdapter() {
			@Override
			public void mouseMoved( MouseEvent e ) {
				if( invertedDisplay != null ) {
					Point2D.Double ep = new Point2D.Double( e.getX(), e.getY() );
					invertedDisplay.transform( ep, ep );
					Rectangle sr = selection();
					if( sr != null ) {
						RectangleEditPoint rep = RectangleEditPoint.closest( selection(), ep.getX(),
								ep.getY() );
						widget.setCursor( rep.cursor );
					}
				}
			}

			@Override
			public void mouseDragged( MouseEvent e ) {
				if( invertedDisplay != null ) {
					Point2D.Double ep = new Point2D.Double( e.getX(), e.getY() );
					invertedDisplay.transform( ep, ep );
					Rectangle sr = selection();
					Point2D.Double min = new Point2D.Double( sr.x, sr.y );
					Point2D.Double max = new Point2D.Double( sr.x + sr.width, sr.y + sr.height );
					dragging.update( min, max, ep.getX(), ep.getY() );
					setSelection( min, max );
				}
			}
		} );
	}

	public Rectangle selection() {
		if( image != null ) {
			return new Rectangle(
					(int) Math.round( selectionMinX * image.getWidth() ),
					(int) Math.round( selectionMinY * image.getHeight() ),
					(int) Math.round( (selectionMaxX - selectionMinX) * image.getWidth() ),
					(int) Math.round( (selectionMaxY - selectionMinY) * image.getHeight() ) );
		}
		return null;
	}

	private void setSelection( Point2D.Double min, Point2D.Double max ) {
		if( image != null ) {
			selectionMinX = min.x / image.getWidth();
			selectionMinY = min.y / image.getHeight();
			selectionMaxX = max.x / image.getWidth();
			selectionMaxY = max.y / image.getHeight();
		}

		double n = Math.min( selectionMinX, selectionMaxX );
		double x = Math.max( selectionMinX, selectionMaxX );
		selectionMinX = n;
		selectionMaxX = x;

		n = Math.min( selectionMinY, selectionMaxY );
		x = Math.max( selectionMinY, selectionMaxY );
		selectionMinY = n;
		selectionMaxY = x;

		selectionMinX = Math.max( 0, selectionMinX );
		selectionMinY = Math.max( 0, selectionMinY );
		selectionMaxX = Math.min( 1, selectionMaxX );
		selectionMaxY = Math.min( 1, selectionMaxY );

		widget.repaint();
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

	private enum RectangleEditPoint {
		NORTH_WEST(Cursor.NW_RESIZE_CURSOR, 0, 0) {
			@Override
			public void update( Point2D.Double min, Point2D.Double max, double px, double py ) {
				min.x = px;
				min.y = py;
			}
		},
		NORTH(Cursor.N_RESIZE_CURSOR, 0.5, 0) {
			@Override
			public void update( Point2D.Double min, Point2D.Double max, double px, double py ) {
				min.y = py;
			}
		},
		NORTH_EAST(Cursor.NE_RESIZE_CURSOR, 1, 0) {
			@Override
			public void update( Point2D.Double min, Point2D.Double max, double px, double py ) {
				max.x = px;
				min.y = py;
			}
		},
		EAST(Cursor.E_RESIZE_CURSOR, 1, 0.5) {
			@Override
			public void update( Point2D.Double min, Point2D.Double max, double px, double py ) {
				max.x = px;
			}
		},
		SOUTH_EAST(Cursor.SE_RESIZE_CURSOR, 1, 1) {
			@Override
			public void update( Point2D.Double min, Point2D.Double max, double px, double py ) {
				max.x = px;
				max.y = py;
			}
		},
		SOUTH(Cursor.S_RESIZE_CURSOR, 0.5, 1) {
			@Override
			public void update( Point2D.Double min, Point2D.Double max, double px, double py ) {
				max.y = py;
			}
		},
		SOUTH_WEST(Cursor.SW_RESIZE_CURSOR, 0, 1) {
			@Override
			public void update( Point2D.Double min, Point2D.Double max, double px, double py ) {
				min.x = px;
				max.y = py;
			}
		},
		WEST(Cursor.W_RESIZE_CURSOR, 0, 0.5) {
			@Override
			public void update( Point2D.Double min, Point2D.Double max, double px, double py ) {
				min.x = px;
			}
		};

		public final Cursor cursor;
		private final double x, y;

		RectangleEditPoint( int cursor, double x, double y ) {
			this.cursor = Cursor.getPredefinedCursor( cursor );
			this.x = x;
			this.y = y;
		}

		private int manhattanDistance( Rectangle rectangle, double px, double py ) {
			double epx = rectangle.x + x * rectangle.width;
			double epy = rectangle.y + y * rectangle.height;
			return (int) (Math.abs( epx - px ) + Math.abs( epy - py ));
		}

		public abstract void update( Point2D.Double min, Point2D.Double max, double px, double py );

		static RectangleEditPoint closest( Rectangle rectangle, double px, double py ) {
			RectangleEditPoint closest = null;
			int minMd = Integer.MAX_VALUE;

			for( RectangleEditPoint rep : RectangleEditPoint.values() ) {
				int md = rep.manhattanDistance( rectangle, px, py );
				if( md < minMd ) {
					closest = rep;
					minMd = md;
				}
			}
			return closest;
		}
	}
}
