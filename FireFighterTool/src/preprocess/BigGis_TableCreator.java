package preprocess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import controller.Starter;
import model.database.DBConnection;

public class BigGis_TableCreator {

	
	
	public static void main(String[] args) {
		
		// create linestring for de_ways
//		create_linestring_for_de_ways();
		
		// TEST
//		String lat = "477077727";
//		String lon = "88423962";
//
//		System.out.println(convertToCoordinate(lat) + "\n"
//				+ convertToCoordinate(lon));
		
		
//		createTestWayPoints();
		
		createNewIntID();
		
		
		ArrayList<Thread> threads = new ArrayList<>();
		
		Thread elev = new Thread(new Runnable() {
			@Override
			public void run() {
				getElevationforWayPoints();
			}
		});
		elev.setName("Elevation_Thread");
//		threads.add(elev);
		
		Thread nearestPoint = new Thread(new Runnable() {
			@Override
			public void run() {
				// create nearest node_point from hydrant
				create_nearest_not_point_from_hy();
			}
		});
		nearestPoint.setName("NearestPoint_Thread");
//		threads.add(nearestPoint);
		
		
		// Start Threads
		for (Thread t : threads) {
			t.start();
		}

		// Wait for end
		try {
			for (Thread t : threads)
				t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Finished!");
		
	}
	
	
	
	
	
	private static void createNewIntID() {
		
		
		Connection con = DBConnection.getConnection(Starter.Type.BIGGIS.name(), true);
		PreparedStatement st;
		// Alter Table
		try {
			Statement update_statement = con.createStatement();
			st = con.prepareStatement("Select * from de_hydrants;");
			
			ResultSet rs = st.executeQuery();
			int i = 0;
			while(rs.next() ) {
				
				i++;
				long id = rs.getLong(1);
				update_statement.execute("Update de_hydrants set int_id = "+i+" where id ="+id);
			}
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}





	public static void createTestWayPoints()  {
		Connection insert_con = DBConnection.getConnection(Starter.Type.BIGGIS.name(), true);
		
		Connection con = DBConnection.getConnection(Starter.Type.BIGGIS.name(), true);
		PreparedStatement st;
		// Create table
		try {
			st = con.prepareStatement("CREATE TABLE IF NOT EXISTS de_way_points_test ("
					+ " way_id bigint,"
					+ "point_id bigint,"
					+ "seq_id bigint,"
					+ "way geometry(Point,4326)"
					+ ");"
					);
			
			st.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		try {
//			PreparedStatement insert_statement = insert_con.prepareStatement("Insert into de_way_points_test (way_id, point_id, seq_id, way) Values (?, ?, ?, ?) ");
			Statement insert_statement = insert_con.createStatement();
			
			con.setAutoCommit(false);
			st = con.prepareStatement("Select * from de_way_nodes_test;");
			
			st.setFetchSize(2000); 
			
			ResultSet rs = st.executeQuery();
			long way_id = 0;
			long node_id = 0;
			long seq_id = 0;
			String point = "";
			int i = 0;
			
			int already_inserted = 0;
			
			System.out.println("START - calc height from: "+already_inserted);
			while(rs.next() ) {
				
				i++;
				
				if (i <= already_inserted )
					continue;
				
				way_id = rs.getLong(1);
				node_id = rs.getLong(2);
				seq_id = rs.getLong(3);
//				INSERT INTO app(p_id, the_geom)
//				VALUES(2, ST_GeomFromText('POINT(-71.060316 48.432044)', 4326));
//					If you have columns with numeric longitude/latitude, you can directly make a POINT geometry:
//				ST_SetSRID(ST_MakePoint(long, lat), 4326);
				point = getPointFromNodeId(insert_con, node_id);
				
				// Create Row
//				insert_statement.setLong(1, way_id);
//				insert_statement.setLong(2, node_id);
//				insert_statement.setLong(3, seq_id);
//				insert_statement.setObject(4, point);
				
				String sql = "Insert into de_way_points_test (way_id, point_id, seq_id, way) Values ("+way_id+", "+node_id+", "+seq_id+", "+point+");";
				
				// must be:
//				Insert into de_way_points_test (way_id, point_id, seq_id, way) Values (179314927, 1896568523, 1, ST_GeomFromText('POINT(8.8423962 47.7077727)', 4326)) 
				
				insert_statement.execute(sql);
//				insert_statement.execute();
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private static String getPointFromNodeId(Connection insert_con, long node_id) {
//		"POINT(8.7494238 53.517195)"
//				ST_SetSRID(ST_MakePoint("+convertToCoordinate(n.lon)+"", 47.76035)
		String point = "";
		
		String getNode = "Select n.lat, n.lon from de_nodes as n where n.id = "+node_id;

		PreparedStatement sta;
		try {
			sta = insert_con.prepareStatement(getNode);
			ResultSet node = sta.executeQuery();
			
			String lat = "";
			String lon = "";
			while(node.next() ) {
				lat = node.getInt(1)+"";
				lon = node.getInt(2)+"";
				
				// must be:
//				Insert into de_way_points_test (way_id, point_id, seq_id, way) Values (179314927, 1896568523, 1, ST_GeomFromText('POINT(8.8423962 47.7077727)', 4326)) 
				
				if (!lat.isEmpty() && !lon.isEmpty()) {
					point = "ST_GeomFromText(\'POINT("+convertToCoordinate(lon)+" "+convertToCoordinate(lat)+")\', 4326)";
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return point;
	}





	private static double convertToCoordinate(String s) {
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
	
	
	private static void getElevationforWayPoints() {
		
		Connection insert_con = DBConnection.getConnection(Starter.Type.BIGGIS.name(), true);
		PreparedStatement st;
		try {
			st = insert_con.prepareStatement("CREATE TABLE IF NOT EXISTS de_way_points_elevation_test ("
				+ "point_id bigint, "
				+ "height int, "
				+ " PRIMARY KEY(point_id))");
		
			st.execute();
		
		
//			st = insert_con.prepareStatement("CREATE TABLE IF NOT EXISTS de_way_points_elevation ("
//					+ "point_id bigint, "
//					+ "height int, "
//					+ " PRIMARY KEY(point_id))");
//			
//			st.execute();
			
			PreparedStatement insert_statement = insert_con.prepareStatement("Insert into de_way_points_elevation_test (point_id, height) Values (?, ?) ");
//			PreparedStatement insert_statement = insert_con.prepareStatement("Insert into de_way_points_elevation (point_id, height) Values (?, ?) ");
//			PreparedStatement insert_statement = con.prepareStatement("INSERT INTO de_hydrantsToWayPoints  (hydrant_id, way_node_id, distance)
			
			Connection con = DBConnection.getConnection(Starter.Type.BIGGIS.name(), true);
			con.setAutoCommit(false);
//			st = con.prepareStatement("SELECT p.point_id, st_astext(p.way) from de_way_points as p ");
			
			// TEST dataset
			st = con.prepareStatement("Select p.point_id, st_astext(p.way) From de_way_points_test  as p ");
			
			// get 20 km buffer
//			st = con.prepareStatement("Select p.point_id, st_astext(p.way) "
//					+ "From (Select * from de_way_points ) as p, "
//					+ "(SELECT ST_Buffer(ST_SetSRID(ST_MakePoint(8.97, 47.76035), 4326)::geography, 20000)::geometry as geom) as buffer "
//					+ "Where ST_Contains(buffer.geom, p.way);");
			
			st.setFetchSize(2000); 
			
			ResultSet rs = st.executeQuery();
			long point_id = 0;
			String point = "";
			int commit_at = 1000;
			int commit_counter = 0;
			int i = 0;
			
			while(rs.next() ) {
				
				i++;
				point_id = rs.getLong(1);
				
				if (Exists(con,"de_way_points_elevation_test", "point_id", point_id))
					continue;
				
				point = rs.getString(2);
				int height = getheightFromGeom(insert_con, point);
				
				// Create Row
				insert_statement.setLong(1, point_id);
				insert_statement.setInt(2, height);
				
				if (commit_counter++ >= commit_at) {
					commit_counter = 0;
					System.out.println(">>> "+ i);
				}
				
				insert_statement.execute();
				
			}
			
		} catch (SQLException e) {
			System.out.println(">>>> SQL Exception calculate height for way points, Start again :)");
			try {
				 TimeUnit.SECONDS.sleep(4);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			getElevationforWayPoints();
			e.printStackTrace();
		}
		
	}
	
	
	private static int getheightFromGeom(Connection con, String point) throws SQLException {
		
		String getelev = "SELECT st_value(elevation_srtm90_v4.rast, st_geomfromtext('"+point+"', 4326)) AS height "
				+ "FROM elevation_srtm90_v4 "
				+ "WHERE st_intersects(elevation_srtm90_v4.rast, st_geomfromtext('"+point+"', 4326), 4326);";

		PreparedStatement sta = con.prepareStatement(getelev);
		ResultSet rs_height = sta.executeQuery();
		
		int height = 0;
		while(rs_height.next()) {
			height = rs_height.getInt(1);
		}
		
		return height;
		
	}



	private static void create_nearest_not_point_from_hy() {
		PreparedStatement st;
		try {
			Connection con = DBConnection.getConnection(Starter.Type.BIGGIS.name(), true);
			
//			st = con.prepareStatement("CREATE TABLE IF NOT EXISTS de_hydrantsToWayPoints_test ("
//					+ "hydrant_id bigint, "
//					+ "way_id bigint, "
//					+ "osm_id bigint, "
//					+ "seq_id int, "
//					+ "distance double precision, "
//					+ " PRIMARY KEY(hydrant_id, osm_id))");
//			st.execute();
			
			st = con.prepareStatement("CREATE TABLE IF NOT EXISTS de_hydrantsToWayPoints ("
					+ "hydrant_id bigint, "
					+ "way_id bigint, "
					+ "osm_id bigint, "
					+ "seq_id int, "
					+ "distance double precision, "
					+ " PRIMARY KEY(hydrant_id, osm_id))");
			st.execute();

//			PreparedStatement insert_statement = con.prepareStatement("Insert into de_hydrantsToWayPoints_test (hydrant_id, way_id, osm_id, seq_id, distance) Values (?, ?, ?, ?, ?) ");
			PreparedStatement insert_statement = con.prepareStatement("Insert into de_hydrantsToWayPoints (hydrant_id, way_id, osm_id, seq_id, distance) Values (?, ?, ?, ?, ?) ");
//			PreparedStatement insert_statement = con.prepareStatement("INSERT INTO de_hydrantsToWayPoints  (hydrant_id, way_node_id, distance)
			
//			st = con.prepareStatement("SELECT h.id, h.geom from de_hydrants as h ");
			
//			st = con.prepareStatement("Select h.id,  st_astext(h.geom), h.height "
//					+ "From (Select * from de_hydrants ) as h, "
//					+ "(SELECT ST_Buffer(ST_SetSRID(ST_MakePoint(8.97, 47.76035), 4326)::geography, 20000)::geometry as geom) as buffer "
//					+ "Where ST_Contains(buffer.geom, h.geom);");
			
//			st = con.prepareStatement("Select h.id,  st_astext(h.geom), h.height "
//					+ "From (Select * from de_hydrants_test ) as h ");
			
			st = con.prepareStatement("Select h.id,  st_astext(h.geom), h.height "
					+ "From (Select * from de_hydrants ) as h ");
			
			ResultSet rs = st.executeQuery();
			long hydrant_id = 0;
			int commit_at = 50;
			int commit_counter = 0;
			int i = 0;
			
			int already_inserted = 22366;
			Statement insertcount = con.createStatement();
			ResultSet count = insertcount.executeQuery("select count(hydrant_id) from de_hydrantstowaypoints");
			while(count.next()) { 
				already_inserted = count.getInt(1);
			}
			
			
			here:
			while(rs.next()) {
				i++;
				
				while (i < already_inserted)
					continue here;
				
				hydrant_id = rs.getLong(1);
				if (Exists(con,"de_hydrantstowaypoints", "hydrant_id", hydrant_id))
					continue;
				
//				st = con.prepareStatement("select exists (select true from de_hydrantsToWayPoints where hydrant_id = "+hydrant_id+");");
//				ResultSet exists = st.executeQuery();
//				exists.next();
//				if (exists.getBoolean(1))
//					continue;
					
				ResultSet rs_nearest = getNearestNode(con, hydrant_id);
				while(rs_nearest.next()) {
					long way_id = rs_nearest.getLong(2);
					long way_point_id = rs_nearest.getLong(3);
					int seq_id = rs_nearest.getInt(4);
					double distance = rs_nearest.getDouble(5);
					String hyd = rs_nearest.getString(6);
					String point = rs_nearest.getString(7);
					
					// Create Row

					insert_statement.setLong(1, hydrant_id);
					insert_statement.setLong(2, way_id);
					insert_statement.setLong(3, way_point_id);
					insert_statement.setLong(4, seq_id);
					insert_statement.setDouble(5, distance);		// in projected units
					
					insert_statement.execute();
					
					if (commit_counter++ >= commit_at) {
						commit_counter = 0;
						System.out.println(">>> "+ i);
					}
				}
			}
			con.close();
		} catch (SQLException e) {
			System.out.println(">>>> SQL Exception in nearest point to hydrant, Start again :)");
			try {
				 TimeUnit.SECONDS.sleep(8);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			create_nearest_not_point_from_hy();
			e.printStackTrace();
		}
		
	}
	

	
	private static boolean Exists(Connection con, String table, String column, long value) {
		
		PreparedStatement stm;
		boolean exists = false;
		try {
			stm = con.prepareStatement("select exists(select 1 from "+table+" where "+column+" = "+value+")");
			ResultSet rs = stm.executeQuery();
			while(rs.next()) { 
				exists = rs.getBoolean(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return exists;
		
	}



	private static ResultSet getNearestNode(Connection con, long hydrant_id) throws SQLException {
		
		
		// distance in meter
//		String nearest_query = "SELECT h.id as hydrant_id, p.way_id, p.point_id as osm_id, p.seq_id, ST_Distance(h.geom::geography, p.way::geography) as distance"
//				+ " FROM de_way_points as p, de_hydrants as h " + "where h.id = " + hydrant_id + ""
//				+ " ORDER BY p.way <-> st_setsrid(h.geom,4326) LIMIT 1";
		
//		String nearest_query = "SELECT h.id, cjl.way_id, cjl.osm_id, cjl.seq_id, cjl.distance "
//							+ "FROM de_hydrants as h "
//							+ "CROSS JOIN LATERAL ( "
//							+ "SELECT p.way_id, p.point_id as osm_id, p.seq_id, "
//							+ "ST_Distance(h.geom::geography, p.way::geography) as distance "
//							+ "FROM de_way_points as p "
//							+ "ORDER BY p.way <-> h.geom "
//							+ "LIMIT 1 "
//							+ ") cjl "
//							+ "WHERE h.id = " +hydrant_id+";";
//		
		
		String nearest_query = "SELECT h.id, cjl.way_id, cjl.osm_id, cjl.seq_id, cjl.distance, st_astext(h.geom), st_astext(cjl.way)"
				+ "FROM de_hydrants as h "
				+ "CROSS JOIN LATERAL ( "
				+ "SELECT p.way_id, p.point_id as osm_id, p.seq_id, "
				+ "ST_Distance(h.geom::geography, p.way::geography) as distance, p.way "
				+ "FROM de_way_points as p "
				+ "ORDER BY p.way <-> h.geom "
				+ "LIMIT 1 "
				+ ") cjl "
				+ "WHERE h.id = " +hydrant_id+";";
		
//		 distance in projected units
//		String nearest_query = "SELECT h.id as hydrant_id, p.way_id, p.point_id as osm_id, p.seq_id, ST_Distance(h.geom, p.way) as distance"
//				+ " FROM de_way_points as p, de_hydrants as h " + "where h.id = " + hydrant_id + ""
//				+ " ORDER BY p.way <-> st_setsrid(h.geom,4326) LIMIT 1";
		
//		String nearest_query = "SELECT h.id as hydrant_id, p.way_id, p.point_id as osm_id, p.seq_id, ST_Distance(h.geom, p.way) as distance"
//				+ " FROM de_way_points_test as p, de_hydrants_test as h " + "where h.id = " + hydrant_id + ""
//				+ " ORDER BY p.way <-> st_setsrid(h.geom,4326) LIMIT 1";
		


		PreparedStatement sta = con.prepareStatement(nearest_query);
		ResultSet rs_nearest = sta.executeQuery();
		return rs_nearest;
		
	}

}
