package model.datatypes;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.util.Arrays;
import java.util.List;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;

public class Hose extends MapPolygonImpl{
	
	private HosePoint start;
	private HosePoint end;
	private double length;
	private double elevationDiff;
	private double rise;
	
	
	private HoseLine parentLine;
	
	private boolean visible = true;
	
	public Hose(HosePoint start, HosePoint end) {
		super(null, null, Arrays.asList(start, end, end));
		this.start = start;
		this.end = end;
		this.length = coordinateDistanceInMeters(start.getCoordintes(), end.getCoordintes());
		this.elevationDiff = end.getElevation() - start.getElevation();
		this.rise = (elevationDiff/length)*100;
	}
	
	public void setVisibility(boolean vis) {
		visible = vis;
	}

	public HosePoint getStart() {
		return start;
	}
	
	public HosePoint getEnd() {
		return end;
	}
	
	public double getElevationDiff() {
		return elevationDiff;
	}
	
	public double getRise() {
		return rise;
	}
	
	public void setHoseLine(HoseLine line) {
		parentLine = line;
	}
	
	public float coordinateDistanceInMeters(Coordinate c1, Coordinate c2) {
	    double earthRadius = 6371000; //meters
	    double dLat = Math.toRadians(c2.getLat()-c1.getLat());
	    double dLng = Math.toRadians(c2.getLon()-c1.getLon());
	    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
	               Math.cos(Math.toRadians(c1.getLat())) * Math.cos(Math.toRadians(c2.getLat())) *
	               Math.sin(dLng/2) * Math.sin(dLng/2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	    float dist = (float) (earthRadius * c);

	    return dist;
}
	
	@Override
	public void paint(Graphics g, List<Point> points) {
		if (!visible) {
			return;
		}
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setColor(parentLine.getColorMap().getColor(rise));
        g2d.setStroke(new BasicStroke(5));
        Path2D path = buildPath(points);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.draw(path);
        g2d.dispose();
    }

    private Path2D buildPath(List<Point> points) {
        Path2D path = new Path2D.Double();
        if (points != null && points.size() > 0) {
            Point firstPoint = points.get(0);
            path.moveTo(firstPoint.getX(), firstPoint.getY());
            for (Point p : points) {
                path.lineTo(p.getX(), p.getY()); 
            }
        } 
        return path;
    }

	public double getLength() {
		// TODO Auto-generated method stub
		return length;
	}
}
