package controller;

import java.io.IOException;
import java.sql.SQLException;

import model.database.DBConnection;
import view.Osmmap_webservice;


public class Starter {

	/**
	 * Main method for testing.
	 * @param args
	 * @throws IOException 
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws IOException, SQLException {
		
		
		
		
//		String[] path = {"s11, s12, s13", "s21, s22, s23", "s31, s32"};
//		
//		// goal: s11 - s12, s13 - s21, s22 - s23
//		String concat = "";
//		for (int i = 0; i< path.length-1; i++) {
//			concat += path[i]+", ";
//		}
//		concat += path[path.length-1];
//		
//		String[] p = concat.split(",");
//		String first = p[0];
//		
//		for (int i = 1; i< p.length; i++) {
//			String second = p[i].trim();
//			System.out.println(first +" - "+second);
//			first = second;
//		}
		
	
		DBConnection.startDB();
		
//		Connection c = DBConnection.getConnection(Type.BIGGIS.name(), false);

//		Osmmap osmMap = new Osmmap(Type.LOCAL.name());
//		osmMap.setVisible(true);
		
//		Osmmap osmMap = new Osmmap(Type.BIGGIS.name());
		
//		WEB SERVICE for routing and Vertex finding! and BIGGIS for the height
		// give the address .. default is localhost
		Osmmap_webservice osmMap = new Osmmap_webservice(Type.BIGGIS.name());
		osmMap.setVisible(true);
		
	}
	
	public enum Type {
		LOCAL, BIGGIS
	}

}
