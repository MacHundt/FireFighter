package model.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import controller.Starter;

public abstract class DBConnection {
	
	// Standard -- LOCAL
	static String driver = "org.postgresql.Driver";
	static String host = "localhost";
	static String port = "5432";
	static String database = "osm";
	static String user = "postgres";
	static String password = "postgres";
	
	static Connection conn = null;
	
	private static String db_type = "";
	
	public static void  startDB() {
		loadDriver();
//		openConnection();
	}
	
	/**
	 * Close connection.
	 */
	private static void closeConnection() {
		try {
			conn.close();
			conn = null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	 
	 /**
	  * Returns the URL.
	  * @return String the URL
	  */
	private static String getURL() {
		return ("jdbc:postgresql://"+host+":"+port+"/"+database);
	}
	
	/**
	 * Loads the JDBC driver.
	 */
	private static void loadDriver() {
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Opens the connection.
	 */
	private static void openConnection() {
		try {
			conn = DriverManager.getConnection(getURL(), user, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns the connection to the db. If there is no connection a new one is established.
	 * @return
	 */
	public static Connection getConnection(String type, boolean newCon){
		if (!db_type.equals(type)) {
			db_type = type;
			conn = null;
		}
		if (type.equals(Starter.Type.LOCAL.name())) {
			driver = "org.postgresql.Driver";
			host = "localhost";
			port = "5432";
			database = "osm";
			user = "postgres";
			password = "postgres";
		} else if (type.equals(Starter.Type.BIGGIS.name())) {
			driver = "org.postgresql.Driver";
			host = "merkur237.inf.uni-konstanz.de";
			port = "5432";
			database = "biggis";
//			user = "hundt";
//			password = "e4GkAKiEaxK8ZrfpgZqr";
			user = "postgres";
			password = "2016.biggis.uni-konstanz";
		}
		
		
		
		if (newCon) {
			try {
				Connection c = DriverManager.getConnection(getURL(), user, password);
				conn = c;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(conn == null){
			startDB();
		} 
		
		
		return conn;
	}
	
	

}
