package model.datatypes;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;

public class WaySegment extends MapPolygonImpl{
	
	private WayNode start;
	private WayNode end;
	
	private Image image;

	private boolean visible = true;
	
	public WaySegment(WayNode start, WayNode end) {
		super(Arrays.asList(start, end, end));
		this.start = start;
		this.end = end;
	}
	
	public WayNode getStart() {
		return start;
	}
	
	public WayNode getEnd() {
		return end;
	}
	
	public void setVisibility(boolean vis) {
		visible = vis;
	}
	
	public String toString() {
		return start.toString()+" --> "+end.toString();
	}
	
	@Override
	public void paint(Graphics g, List<Point> points) {
		if (!visible) {
			return;
		}
		try {
			this.image = ImageIO.read(new File("lib/blocked.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Point anfang = points.get(0);
		Point ende = points.get(1);
		int x = (anfang.x + ende.x)/2;
		int y = (anfang.y + ende.y)/2;
		int width = 20;
		int height = 20;
        g.drawImage(this.image, x-width/2, y-height/2, width, height, null);
    }

}
