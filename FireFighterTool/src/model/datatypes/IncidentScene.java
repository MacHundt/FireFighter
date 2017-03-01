package model.datatypes;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Stroke;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.Layer;
import org.openstreetmap.gui.jmapviewer.MapObjectImpl;
import org.openstreetmap.gui.jmapviewer.Style;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;

public class IncidentScene extends MapObjectImpl implements MapMarker{
	
	private ICoordinate coords;
	private double lat;
	private double lon;
	
	private Image image;
	
	private boolean visible = true;
	
	public IncidentScene(ICoordinate iCoordinate) {
		super("Incident");
		coords = iCoordinate;
		lat = iCoordinate.getLat();
		lon = iCoordinate.getLon();
		try {
			image = ImageIO.read(new File("lib/truck.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public IncidentScene(double lat, double lon) {
		super("Incident");
		this.lat = lat;
		this.lon = lon;
		coords = new Coordinate(lat, lon);
		try {
			image = ImageIO.read(new File("lib/truck.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public Color getBackColor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Color getColor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Font getFont() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Layer getLayer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stroke getStroke() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Style getStyle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Style getStyleAssigned() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isVisible() {
		// TODO Auto-generated method stub
		return visible;
	}

	@Override
	public void setLayer(Layer arg0) {
		// TODO Auto-generated method stub

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
		return new Coordinate(lat, lon);
	}

	@Override
	public double getLat() {
		// TODO Auto-generated method stub
		return lat;
	}

	@Override
	public double getLon() {
		// TODO Auto-generated method stub
		return lon;
	}

	@Override
	public STYLE getMarkerStyle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getRadius() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void paint(Graphics g, Point position, int arg2) {
		int width = 40;
		int height = 30;
        g.drawImage(this.image, position.x-width/2, position.y-height/2, width, height, null);
	}

}
