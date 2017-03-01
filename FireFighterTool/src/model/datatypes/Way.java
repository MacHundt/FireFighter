package model.datatypes;

import java.util.ArrayList;
import java.util.List;

public class Way {
	
	private List<WaySegment> waySegments;
	
	public Way() {
		waySegments = new ArrayList<WaySegment>();
	}
	
	public Way(List<WaySegment> nodes) {
		this.waySegments = nodes;
	}
	
	public List<WaySegment> getWaySegments() {
		return waySegments;
	}
	
	public void setWaySegments(List<WaySegment> ways) {
		waySegments = ways;
	}
	
	public void addWaySegments(WaySegment node) {
		waySegments.add(node);
	}

}
