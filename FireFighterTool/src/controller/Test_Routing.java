package controller;

import java.io.File;
import java.util.Properties;

import de.cm.osm2po.routing.DefaultRouter;
import de.cm.osm2po.routing.Graph;
import de.cm.osm2po.routing.RoutingResultSegment;

public class Test_Routing {

	public static void main(String[] args) {
		
		System.out.println("TEST Routing");
		
		File graphFile = new File("/Users/michaelhundt/Documents/Meine/Studium/MASTER/HIWI/Daten/osm2po-5.1.0/de/de_2po.gph");
		Graph graph = new Graph(graphFile);
		DefaultRouter router = new DefaultRouter();

		// Somewhere in Hamburg
		int sourceId = graph.findClosestVertexId(48.50f, 8.57f);
		int targetId = graph.findClosestVertexId(48.63f, 8.60f);

		// additional params for DefaultRouter
		Properties params = new Properties();
		params.setProperty("findShortestPath", "true");
		params.setProperty("ignoreRestrictions", "false");
		params.setProperty("ignoreOneWays", "false");
		params.setProperty("heuristicFactor", "1.0"); // 0.0 Dijkstra, 1.0 good A*

		int[] path = router.findPath(
		    graph, sourceId, targetId, Float.MAX_VALUE, params);

		if (path != null) { // Found!
		    for (int i = 0; i < path.length; i++) {
		        RoutingResultSegment rrs = graph.lookupSegment(path[i]);
		        int segId = rrs.getId();
		        int from = rrs.getSourceId();
		        int to = rrs.getTargetId();
		        String segName = rrs.getName().toString();
		        System.out.println(from + "-" + to + "  " + segId + "/" + path[i] + " " + segName);
		    }
		}

		graph.close();

	}

}
