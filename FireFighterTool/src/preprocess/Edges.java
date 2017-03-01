package preprocess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import controller.Starter;
import model.database.DBConnection;


public class Edges {
	private Connection conn;
	
	// TEST Dataset
//	String table = "de_edges_test";
	String table = "de_edges";
	
	public Edges() {
		DBConnection.startDB();
	    conn = DBConnection.getConnection(Starter.Type.BIGGIS.name(), true);
	}
	
	
//	public void edges_test() throws Exception {
//		
//		Connection insert_con = DBConnection.getConnection(Starter.Type.BIGGIS.name(), true);
//		try {
//			PreparedStatement stm = conn.prepareStatement("SELECT id, nodes "
//					+ "FROM de_ways_test "
//					+ ";");
//			
//			ResultSet rs = stm.executeQuery();
//			
//			String allNodes = "";
//			
//			long way = 0;
//			
//			long count = 0;
//			
//			while (rs.next()) {
//				ArrayList<Long> nodeIDs = new ArrayList<Long>();
//				way = rs.getLong(1);
//				allNodes = rs.getString(2);
//				allNodes = allNodes.replace("{", "");
//				allNodes = allNodes.replace("}", "");
////				System.out.println(count);
//				String[] nodesSplit = allNodes.split(",");
//				for (String node : nodesSplit) {
//					nodeIDs.add(Long.parseLong(node));
//				}
//				
//				long from = 0;
//				long to = 0;
//				
//				for (int i = 0; i < nodeIDs.size() -1; i++) {
//					if (i == (nodeIDs.size() -1)) {
//						break;
//					}
//					from = nodeIDs.get(i);
//					to = nodeIDs.get(i+1);
//					
//					PreparedStatement st2 = insert_con.prepareStatement("INSERT into "+table+"("
//							+ "source, target, way_id) "
//							+ "VALUES ("+from+", "+to+","+way+");");
//					st2.execute();
//					st2.close();
//				}
//				count++;
//			}
//			rs.close();
//			stm.close();
//			
//			
//			
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	
	public void edges() throws Exception {
		try {
//			PreparedStatement stm = conn.prepareStatement("SELECT id, nodes "
//					+ "FROM ways "
//					+ ";");
			
			conn.setAutoCommit(false);
			PreparedStatement stm = conn.prepareStatement("SELECT id, nodes "
					+ "FROM de_ways "
					+ ";");
			stm.setFetchSize(2000); 
			Connection insert_con = DBConnection.getConnection(Starter.Type.BIGGIS.name(), true);
			
			ResultSet rs = stm.executeQuery();
			String allNodes = "";
			
			long way = 0;
			
			long count = 0;
			
			while (rs.next()) {
				ArrayList<Long> nodeIDs = new ArrayList<Long>();
				way = rs.getLong(1);
				allNodes = rs.getString(2);
				allNodes = allNodes.replace("{", "");
				allNodes = allNodes.replace("}", "");
				String[] nodesSplit = allNodes.split(",");
				for (String node : nodesSplit) {
					nodeIDs.add(Long.parseLong(node));
				}
				
				long from = 0;
				long to = 0;
				
				for (int i = 0; i < nodeIDs.size() -1; i++) {
					if (i == (nodeIDs.size() -1)) {
						break;
					}
					from = nodeIDs.get(i);
					to = nodeIDs.get(i+1);
					
//					PreparedStatement st2 = conn.prepareStatement("INSERT into edges("
//							+ "\"from\", \"to\", way) "
//							+ "VALUES ("+from+", "+to+","+way+");");
					
					if (Exists(conn, table, "source = "+from+" and target ="+to ))
						continue;
					
					PreparedStatement st2 = insert_con.prepareStatement("INSERT into "+table+"("
							+ "source, target, way_id) "
							+ "VALUES ("+from+", "+to+","+way+");");
					st2.execute();
					st2.close();
				}
				count++;
			}
			rs.close();
			stm.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
  private boolean Exists(Connection con, String table, String whereCondition) {
		
		PreparedStatement stm;
		boolean exists = false;
		try {
			stm = con.prepareStatement("select exists(select 1 from "+table+" where "+whereCondition+")");
			ResultSet rs = stm.executeQuery();
			while(rs.next()) { 
				exists = rs.getBoolean(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return exists;
		
	}
	
	
	public void distance() {
		System.out.println("Calculate Distance");
		PreparedStatement stm;
		try {
//			stm = conn.prepareStatement("SELECT \"from\", \"to\" "
//					+ "FROM edges "
//					+ "WHERE distance is null"
//					+ ";");
			
			Connection conn = DBConnection.getConnection(Starter.Type.BIGGIS.name(), true);
			stm = conn.prepareStatement("SELECT source, target "
					+ "FROM "+table+" "
					+ "WHERE distance is null"
					+ ";");
			
			ResultSet rs = stm.executeQuery();
			
			int counter = 0;
			while(rs.next()) {
//				System.out.println(counter);
//				PreparedStatement st2 = conn.prepareStatement("SELECT ST_Distance_Sphere(a.geom,b.geom) "
//						+ "FROM nodes a, nodes b "
//						+ "where a.id = "+rs.getLong(1)+" and b.id = "+rs.getLong(2)+";");
				
				
				
//				SELECT ST_Distance_Sphere(st_geomfromtext('POINT(8.7494238 53.517195)', 4326),st_geomfromtext('POINT(8.7494256 53.5675642)', 4326))
				
				String pointA = getPointFromNodeId(conn, rs.getLong(1));
				String pointB = getPointFromNodeId(conn, rs.getLong(2));
				
				PreparedStatement st2 = conn.prepareStatement("SELECT ST_Distance_Sphere("
						+ "st_geomfromtext('"+pointA+"', 4326), "
						+ "st_geomfromtext('"+pointB+"', 4326)"
						+ ")");
				
				ResultSet rs2 = st2.executeQuery();
				
				while(rs2.next()) {
//					PreparedStatement st3 = conn.prepareStatement("UPDATE edges "
//							+ "SET distance = "+rs2.getDouble(1)
//							+ " WHERE \"from\" = "+rs.getLong(1)+" and \"to\" = "+rs.getLong(2)+";");
					
					PreparedStatement st3 = conn.prepareStatement("UPDATE "+table+" "
							+ "SET distance = "+rs2.getDouble(1)
							+ " WHERE source = "+rs.getLong(1)+" and target = "+rs.getLong(2)+";");
					
					st3.execute();
					st3.close();
				}
				st2.close();
				rs2.close();
				counter++;
			}
			
			rs.close();
			stm.close();
		} catch (SQLException e) {
			System.out.println(">>>> SQL Exception in Distance, Start again :)");
			try {
				TimeUnit.SECONDS.sleep(4);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			distance();
			e.printStackTrace();
		}
		System.out.println("Calculate Distance ... Finished");
		
	}
	
	public void elevationDiff() {
		System.out.println("Calculate the Elevation difference");
		PreparedStatement stm;
		try {
//			stm = conn.prepareStatement("SELECT \"from\", \"to\" "
//					+ "FROM edges "
//					+ "WHERE ele_diff is null;");
			
			
			Connection conn = DBConnection.getConnection(Starter.Type.BIGGIS.name(), true);
			stm = conn.prepareStatement("SELECT source, target "
					+ "FROM "+table+" "
					+ "WHERE ele_diff is null;");
			
			ResultSet rs = stm.executeQuery();
			
			int counter = 0;
			while(rs.next()) {
//				System.out.println(counter);
//				PreparedStatement st2 = conn.prepareStatement("SELECT (b.height - a.height) "
//						+ "FROM elevation a, elevation b "
//						+ "where a.node_id = "+rs.getLong(1)+" and b.node_id = "+rs.getLong(2)+";");
				
				String pointA = getPointFromNodeId(conn, rs.getLong(1));
				String pointB = getPointFromNodeId(conn, rs.getLong(2));
				
				double heightA = getheightFromGeom(conn, pointA);
				double heightB = getheightFromGeom(conn, pointB);
				
				PreparedStatement st3 = conn.prepareStatement("UPDATE "+table+" "
						+ "SET ele_diff = "+(heightB-heightA)
						+ " WHERE source = "+rs.getLong(1)+" and target = "+rs.getLong(2)+";");
				
				st3.execute();
				st3.close();
				
				
//				PreparedStatement st2 = conn.prepareStatement("SELECT (b.height - a.height) "
//						+ "FROM elevation a, elevation b "
//						+ "where a.node_id = "+rs.getLong(1)+" and b.node_id = "+rs.getLong(2)+";");
//				
//				ResultSet rs2 = st2.executeQuery();
//				while(rs2.next()) {
//					PreparedStatement st3 = conn.prepareStatement("UPDATE edges "
//							+ "SET ele_diff = "+rs2.getDouble(1)
//							+ " WHERE \"from\" = "+rs.getLong(1)+" and \"to\" = "+rs.getLong(2)+";");
//					
//					st3.execute();
//					st3.close();
//				}
//				st2.close();
//				rs2.close();
				counter++;
			}
			
			rs.close();
			stm.close();
		} catch (SQLException e) {
			System.out.println(">>>> SQL Exception in Elev_Diff, Start again :)");
			try {
				 TimeUnit.SECONDS.sleep(4);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			elevationDiff();
			e.printStackTrace();
		}
		System.out.println("Calculate the Elevation difference ... Finished");
	}
	
	
	private String getPointFromNodeId(Connection con, long node_id) {
//		"POINT(8.7494238 53.517195)"
//		ST_SetSRID(ST_MakePoint("+convertToCoordinate(n.lon)+"", lat => 47.76035)
		String point = "";
		
		String getNode = "Select n.lat, n.lon from de_nodes as n where n.id = "+node_id;

		PreparedStatement sta;
		try {
			sta = con.prepareStatement(getNode);
			ResultSet node = sta.executeQuery();
			
			String lat = "";
			String lon = "";
			while(node.next() ) {
				lat = node.getInt(1)+"";
				lon = node.getInt(2)+"";
				
				if (!lat.isEmpty() && !lon.isEmpty()) {
					point = "POINT("+convertToCoordinate(lon)+" "+convertToCoordinate(lat)+")";
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return point;
	}



	private double convertToCoordinate(String s) {
		if (s.length() == 11) {
			s = s.substring(0, 4)+"."+ s.substring(4);
		}
		else if (s.length() == 10) {
			s = s.substring(0, 3)+"."+ s.substring(3);
		}
		else if (s.length() == 9) {
			s = s.substring(0, 2)+"."+ s.substring(2);
		}
		else if (s.length() == 8) {
			s = s.substring(0, 1)+"."+ s.substring(1);
		}
		return Double.parseDouble(s);
	}
	
	
	private double getheightFromGeom(Connection con, String point) throws SQLException {
		
		String getelev = "SELECT st_value(elevation_srtm90_v4.rast, st_geomfromtext('"+point+"', 4326)) AS height "
				+ "FROM elevation_srtm90_v4 "
				+ "WHERE st_intersects(elevation_srtm90_v4.rast, st_geomfromtext('"+point+"', 4326), 4326);";

		PreparedStatement sta = con.prepareStatement(getelev);
		ResultSet rs_height = sta.executeQuery();
		
		double height = 0;
		while(rs_height.next()) {
			height = rs_height.getDouble(1);
		}
		
		return height;
		
	}
	
	public void reversing() {
		
		System.out.println("Reverse all Edges (two way)");
		
		PreparedStatement stm;
		try {
			
			// remove duplicates
			stm = conn.prepareStatement("DELETE FROM "+table+" as ed "
					+ "using "
					+ "( "
					+ "SELECT way_id, source, target, t.rnum "
					+ "FROM "
					+ "(SELECT way_id, source, target, "
					+ "ROW_NUMBER() OVER (partition BY way_id, source, target ORDER BY way_id) AS rnum "
					+ "FROM "+table+") t "
					+ "WHERE t.rnum > 1 ) as del "
					+ "WHERE ed.way_id = del.way_id and ed.source = del.source and ed.target = del.target;");
			
			stm.execute();
			
//			stm = conn.prepareStatement("SELECT \"from\", \"to\", distance, ele_diff, way "
//					+ "FROM edges "
//					+ ";");
//			System.out.println(stm.toString());
			
			Connection conn_collect = DBConnection.getConnection(Starter.Type.BIGGIS.name(), true);
//			conn_collect.setAutoCommit(false);
			PreparedStatement stm_collect = conn_collect.prepareStatement("SELECT source, target, distance, ele_diff, way_id "
					+ "FROM "+table+" "
					+ ";");
//			stm_collect.setFetchSize(2000); 
			ResultSet rs = stm_collect.executeQuery();
			
			PreparedStatement insert_statement = conn.prepareStatement("INSERT into "+table+" (source, target, distance, ele_diff, way_id) "
					+ "VALUES ( ?, ?, ?, ?, ?);");
			
			int counter = 0;
			while(rs.next()) {
//				System.out.println(counter);
//				PreparedStatement st2 = conn.prepareStatement("INSERT into edges "
//						+ "VALUES ( "
//						+ rs.getLong(2)+","+rs.getLong(1)+","+rs.getDouble(3)+","+(-1)*rs.getDouble(4)+","+rs.getLong(5)+");");
				
//				PreparedStatement st2 = conn.prepareStatement("INSERT into de_edges_test (source, target, distance, ele_diff, way_id) "
//						+ "VALUES ( "
//						+ rs.getLong(2)+","+rs.getLong(1)+","+rs.getDouble(3)+","+(-1)*rs.getDouble(4)+","+rs.getLong(5)+");");
				
				insert_statement.setLong(1, rs.getLong(2));
				insert_statement.setLong(2, rs.getLong(1));
				insert_statement.setDouble(3, rs.getDouble(3));
				insert_statement.setDouble(4, rs.getDouble(4));
				insert_statement.setLong(5, rs.getLong(5));
				

				insert_statement.execute();
				counter++;
			}
			insert_statement.close();
			rs.close();
			stm_collect.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Reverse all Edges (two way) ... Finished");
	}
	
	public void createID() {
		System.out.println("Create ID");
		PreparedStatement stm;
		try {

			// remove duplicates
			stm = conn.prepareStatement("DELETE FROM "+table+" as ed " + "using " + "( "
					+ "SELECT way_id, source, target, t.rnum " + "FROM " + "(SELECT way_id, source, target, "
					+ "ROW_NUMBER() OVER (partition BY way_id, source, target ORDER BY way_id) AS rnum "
					+ "FROM "+table+") t " + "WHERE t.rnum > 1 ) as del "
					+ "WHERE ed.way_id = del.way_id and ed.source = del.source and ed.target = del.target;");

			stm.execute();
			
			stm = conn.prepareStatement("ALTER TABLE "+table+" Drop column if exists new_id;");
			stm.execute();
			
			stm = conn.prepareStatement("ALTER TABLE "+table+" add column new_id bigserial;");
			stm.execute();
			
			stm = conn.prepareStatement("Update "+table+" set id=new_id;");
			stm.execute();
			
			stm = conn.prepareStatement("ALTER TABLE "+table+" Drop column new_id;");
			stm.execute();

			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		System.out.println("Create ID ... Finished");
	}
	
	
	// LOCAL
	public void changeID() {
		PreparedStatement stm;
		try {
//			stm = conn.prepareStatement("SELECT id "
//					+ "FROM nodes "
//					+ ";");
			
			stm = conn.prepareStatement("SELECT id "
					+ "FROM de_nodes "
					+ ";");
			
			ResultSet rs = stm.executeQuery();
			
			int counter = 1;
			while(rs.next()) {
				System.out.println(counter);
//				PreparedStatement st2 = conn.prepareStatement("UPDATE nodes "
//						+ "SET id = "+counter
//						+ " where id = "+rs.getLong(1));
				
				PreparedStatement st2 = conn.prepareStatement("UPDATE nodes "
						+ "SET id = "+counter
						+ " where id = "+rs.getLong(1));
				st2.execute();
				st2.close();
				st2 = conn.prepareStatement("UPDATE way_nodes "
						+ "SET node_id = "+counter
						+ " where node_id = "+rs.getLong(1));
				st2.execute();
				st2.close();
				
				st2 = conn.prepareStatement("UPDATE elevation "
						+ "SET node_id = "+counter
						+ " where node_id = "+rs.getLong(1));
				st2.execute();
				st2.close();
				
				st2 = conn.prepareStatement("UPDATE edges "
						+ "SET \"from\" = "+counter
						+ " where \"from\" = "+rs.getLong(1));
				st2.execute();
				st2.close();
				
				st2 = conn.prepareStatement("UPDATE edges "
						+ "SET \"to\" = "+counter
						+ " where \"to\" = "+rs.getLong(1));
				st2.execute();
				st2.close();
				
				st2 = conn.prepareStatement("SELECT nodes "
						+ "FROM ways "
						+ "where nodes @> ARRAY["+rs.getLong(1)+"]::bigint[];");
				ResultSet rs2 = st2.executeQuery();
				while(rs2.next()) {
					PreparedStatement st3 = conn.prepareStatement("UPDATE ways "
							+ "SET nodes = '"+ rs2.getString(1).replaceAll(rs.getLong(1)+"", counter+"")+"'::bigint[] "
							+ "where nodes @> ARRAY["+rs.getLong(1)+"]::bigint[]");
					st3.execute();
					st3.close();
				}
				st2.execute();
				st2.close();
				
				counter++;
			}
			
			rs.close();
			stm.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void test() {
		PreparedStatement stm;
		try {
			stm = conn.prepareStatement("SELECT nodes "
					+ "FROM ways "
					+ "where nodes @> ARRAY[28305439]::bigint[] and nodes @> ARRAY[36948875]::bigint[];");
			
			ResultSet rs = stm.executeQuery();
			
			int counter = 1;
			while(rs.next()) {
				System.out.println(rs.getString(1));
				counter++;
			}
			
			rs.close();
			stm.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	// LOCAL !!
	public void hydrantPoints() {
		PreparedStatement stm;
		try {
			stm = conn.prepareStatement("SELECT id "
					+ "FROM hydrants;");
			
			ResultSet rs = stm.executeQuery();
			
			int counter = 1;
			while(rs.next()) {
				PreparedStatement st2 = conn.prepareStatement("SELECT hydrant, waypoint, distance "
						+ "FROM \"nearestWayPointOfHydrant\" "
						+ " where hydrant = "+rs.getLong(1)+" "
						+ "  order by distance limit 1;");
				ResultSet rs2 = st2.executeQuery();
				while(rs2.next()) {
					PreparedStatement st3 = conn.prepareStatement("INSERT INTO \"hydrantsToWayPoints\"("
							+ "hydrant_node, way_node, distance) "
							+ "VALUES ("+rs2.getLong(1)+", "+rs2.getLong(2)+", "+rs2.getDouble(3)+");");
					st3.execute();
					st3.close();
				}
				counter++;
			}
			
			rs.close();
			stm.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public static void main(String[] args) {
		Edges edges = new Edges();
		try {
//			edges.edges_test();
			edges.edges();
			
			ArrayList<Thread> threads = new ArrayList<>();
			Thread dis = new Thread(new Runnable() {
				@Override
				public void run() {
					edges.distance();
				}
			});
			dis.setName("Distance_Thread");
			threads.add(dis);
			
			Thread ele = new Thread(new Runnable() {
				@Override
				public void run() {
					edges.elevationDiff();
				}
			});
			ele.setName("Elevation_Thread");
			threads.add(ele);
			
			
			// Start Threads
			for (Thread t : threads) {
				t.start();
			}

			// Wait for end
			try {
				for (Thread t : threads)
					t.join();
			} catch (InterruptedException e) {
				System.out.println(">>> Interrupted: connect again and restart :)");
				e.printStackTrace();
			}
			

			edges.reversing();
			edges.createID();
//			edges.changeID();
//			edges.test();
//			edges.hydrantPoints();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
