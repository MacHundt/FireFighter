package model.datatypes;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;

public class HoseLine extends MapPolygonImpl implements Comparable<HoseLine>{
	
	private List<Hose> hoses;
	private List<Pump> pumps;
	private int way;
	
	private double length;
	
	private double minRise;
	private double maxRise;
	
	private ColorMap colormap;
	
	private boolean visible = true;
	
	public HoseLine(List<Hose> hoses, List<Pump> pumps) {
		this.hoses = hoses;
		this.pumps = pumps;
		calcLengthAndRise();
		this.colormap = new ColorMap(minRise, maxRise);
	}
	
	private void calcLengthAndRise() {
		length = 0.0;
		minRise = 0.0;
		maxRise = 0.0;
		
		if (hoses.size() < 1) return;
		for (Hose h : hoses) {
			length += h.getLength();
			minRise = Math.min(minRise, h.getRise());
			maxRise = Math.max(maxRise, h.getRise());
			
			h.setHoseLine(this);
		}
	}
	
	public void setVisibility(boolean vis) {
		visible = vis;
	}
	
	public double getLength() {
		return length;
	}
	
	public ColorMap getColorMap() {
		return colormap;
	}

	public List<Hose> getHoses() {
		return hoses;
	}
	
	public void setHosePoints(List<Hose> hosepoints) {
		hoses = hosepoints;
	}
	
	public List<Pump> getPumps() {
		return pumps;
	}
	
	public void setPumps(List<Pump> pumps) {
		this.pumps = pumps;
	}

	@Override
	public int compareTo(HoseLine o) {
		if(pumps.size() > o.getPumps().size()) {
			return 1;
		}
		else if (pumps.size() == o.getPumps().size()) {
			if (hoses.size() > o.getHoses().size()) {
				return 1;
			}
			else return -1;
		}
		return -1;
	}

	
	@Override
	public void paint(Graphics g, List<Point> points) {
		System.out.println("jkhk");
		if (!visible) {
			System.out.println("asdassad");
			return;
		}
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setColor(getColor());
        g2d.setStroke(getStroke());
        Path2D path = buildPath(points);
        g2d.draw(path);
        g2d.dispose();
    }

    private Path2D buildPath(List<Point> points) {
        Path2D path = new Path2D.Double();
        if (points != null && points.size() > 0) {
            Point firstPoint = points.get(0);
            path.moveTo(firstPoint.getX(), firstPoint.getY());
            points = sort(points,firstPoint);
            for (Point p : points) {
                path.lineTo(p.getX(), p.getY()); 
            }
        } 
        return path;
    }

	private List<Point> sort(List<Point> points, Point first) {
		
		List<Point> list = new ArrayList<Point>();
		
		list.add(first);
		int toremove = 0;
		for (int i = 0; i < points.size();i++) {
			if (dist(first, points.get(i)) == 0) {
				toremove= i;
			}
		}
		
		points.remove(toremove);
		
		int size = points.size();
		
		for (int i = 0; i< size;i++) {
			int nearest = 0;
			for(int j = 0; j < points.size();j++) {
				if (dist(points.get(j), list.get(list.size()-1)) < dist(points.get(nearest), list.get(list.size()-1))) {
					nearest = j;
				}
			}
			list.add(points.remove(nearest));
		}
		
		// TODO Auto-generated method stub
		return list;
	}

	private double dist(Point point, Point first) {
		double sq = Math.pow(first.getX()-point.getX(), 2) + Math.pow(first.getY()-point.getY(), 2);
		return Math.sqrt(sq);
	}

	public void setWay(int i) {
		way = i;
	}

	public int getWay() {
		// TODO Auto-generated method stub
		return way;
	}
	
	public String toString() {
		return "Pumps: "+pumps.size()+"; Length: "+(int)length+"m; Hoses: "+(int)Math.ceil(length/20);
		
	}
}
