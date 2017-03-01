package model.datatypes;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;

public class HosePoint implements ICoordinate{

	private Coordinate coords;
	private double elevation;
	private double pressure;
	private long id;
	
	private double position;
	
	public HosePoint(Coordinate c, double elevation, double pressure) {
		this.coords = c;
		this.elevation = elevation;
		this.pressure = pressure;
	}
	
	public void setPosition(double pos) {
		position = pos;
	}
	
	public double getPosition() {
		return position;
	}
	
	public Long getID() {
		return id;
	}
	
	public void setID(Long id){
		this.id = id;
	}
	
	public Coordinate getCoordintes() {
		return coords;
	}
	
	public double getElevation() {
		return elevation;
	}
	
	public double getPressure() {
		return pressure;
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
	public void setLat(double lat) {
		coords.setLat(lat);
	}

	@Override
	public void setLon(double lon) {
		coords.setLon(lon);
	}
}
