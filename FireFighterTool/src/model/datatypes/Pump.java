package model.datatypes;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.MapObjectImpl;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;

public class Pump extends MapObjectImpl implements MapMarker{
	
	private Coordinate coords;
	private Image image;
	
	private double elevation;
	private double position;
	
	private int hosePosition;
	
	public Pump(Coordinate coords) {
		super("Pump");
		this.coords = coords;
		try {
			this.image = ImageIO.read(new File("lib/pump.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setHosePosition(int pos) {
		hosePosition = pos;
	}
	
	public int getHosePosition() {
		return hosePosition;
	}
	
	public double getElevation() {
		return elevation;
	}
	
	public void setElevation(double ele) {
		elevation = ele;
	}
	
	public void setPosition(double pos) {
		position = pos;
	}
	
	public double getPosition() {
		return position;
	}
	
	public Coordinate getCoordinates() {
		return coords;
	}
	
	public void setCoordinates(Coordinate c) {
		coords = c;
	}

	@Override
	public void setLat(double arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLon(double arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Coordinate getCoordinate() {
		// TODO Auto-generated method stub
		return coords;
	}

	@Override
	public double getLat() {
		// TODO Auto-generated method stub
		return coords.getLat();
	}

	@Override
	public double getLon() {
		// TODO Auto-generated method stub
		return coords.getLon();
	}

	@Override
	public STYLE getMarkerStyle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getRadius() {
		// TODO Auto-generated method stub
		return 5;
	}

	public void paint(Graphics g, Point position, int radio) {
		int width = 36;
		int height = 27;
        g.drawImage(this.image, position.x-width/2, position.y-height/2, width, height, null);
        String str = hosePosition+"";
        Color textColor = Color.BLACK;
        Color bgColor = Color.white;
        int x = 30;
        int y = 15;
        g.setColor(bgColor);
        g.fillRect(position.x-width/2+5, position.y-height/2-14,
                   x,
                   y);

        g.setColor(textColor);

        Font f = new Font("Dialog", Font.BOLD, 12);
        g.setFont(f);
        g.drawString(str, position.x-width/2+20, position.y-height/2);
	}

	
	
}
