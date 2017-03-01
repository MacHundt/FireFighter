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
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;

public class Hydrant extends MapObjectImpl implements MapMarker {
	
	private double lat;
    private double lon;
    
    private double elevation;
    
    private String type;
    private String position;
    private String diameter;
    
    private boolean visible = true;
    
    private boolean blackwhite = true;
    
    private Image image;
    
    public Hydrant(Coordinate c, double ele) {
        this(c.getLat(), c.getLon(), ele);
    }
    
    public Hydrant(double lat, double lon, double ele) {
    	super("Hydrant");
        this.lat = lat;
    	this.lon = lon;
    	this.elevation = ele;
    }
    
    public void setBlackWhite(boolean b) {
    	blackwhite = b;
    }
    
    public void setVisible(boolean vis) {
    	visible = vis;
    }
    
    public double getElevation() {
		return elevation;
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
		try {

			int width = 18;
			int height = 30;
			if (blackwhite) {
				this.image = ImageIO.read(new File("lib/hydrant_small_blackwhite.png"));	
			}
			else {
				this.image = ImageIO.read(new File("lib/hydrant_small.png"));
			}

	        g.drawImage(this.image, position.x-width/2, position.y-height/2, width, height, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setType(String substring) {
		type = substring;
	}
	
	public String getType() {
		return type;
	}

	public void setDiameter(String substring) {
		diameter = substring;
	}
	
	public String getDiameter() {
		return diameter;
	}
	
	public String getPosition() {
		return position;
	}

	public void setPosition(String substring) {
		position = substring;
	}

}
