package view;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;

public class DragImage extends JComponent implements MouseMotionListener {
	
	 /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	static int imageWidth = 60, imageHeight = 60;
	 int imageX, imageY;

	 Image image;
	 
	 public DragImage(Image i) {
		 image = i;
		 addMouseMotionListener(this);
	 }

	@Override
	public void mouseDragged(MouseEvent e) {
		imageX = e.getX();
		imageY = e.getY();
		repaint();
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		
	}
	
	 public void paint(Graphics g) {
		    Graphics2D g2 = (Graphics2D) g;

		    g2.drawImage(image, imageX, imageY, this);
		  }

}
