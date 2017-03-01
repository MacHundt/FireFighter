package controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import model.database.DBConnection;
import view.Osmmap;


public class Starter {

	/**
	 * Main method for testing.
	 * @param args
	 * @throws IOException 
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws IOException, SQLException {
		
	
		DBConnection.startDB();
		
//		Connection c = DBConnection.getConnection(Type.BIGGIS.name(), false);

//		Osmmap osmMap = new Osmmap(Type.LOCAL.name());
//		osmMap.setVisible(true);
		
		Osmmap osmMap = new Osmmap(Type.BIGGIS.name());
		osmMap.setVisible(true);
		
	}
	
	public enum Type {
		LOCAL, BIGGIS
	}

}
