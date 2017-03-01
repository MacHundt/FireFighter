package model.datatypes;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;

public class WayNode implements ICoordinate{
	
	private Coordinate coords;
	private long id;
	private double elevation;
	
	public WayNode(long id, Coordinate c, double elevation) {
		this.id = id;
		this.coords = c;
		this.elevation = elevation;
	}
	
	public long getID() {
		return id;
	}
	
	public Coordinate getCoordinates() {
		return coords;
	}
	
	public double getElevation() {
		return elevation;
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
	public void setLat(double arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLon(double arg0) {
		// TODO Auto-generated method stub
		
	}

}
