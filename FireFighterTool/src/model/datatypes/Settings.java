package model.datatypes;

public class Settings {
	
	private static Settings instance;
	
	private static int numberOfLines;
	private static int flowRate;
	private static String tilesPath;

	private Settings() {
		Settings.numberOfLines = 1;
		Settings.flowRate = 800;
		
		// Path to offline tiles.
		//Settings.tilesPath = "N:/Tiles/";
	}
	
	public static Settings getInstance() {
		if (instance == null) {
			instance = new Settings();
		}
		return instance;
	}
	
	public void setNumberOflInes(int n) {
		numberOfLines = n;
	}
	
	public void setFlowrate(int f) {
		flowRate = f;
	}
	
	public void setTilesPath(String path) {
		tilesPath = path;
	}
	
	public int getNumberOfLines() {
		return numberOfLines;
	}
	
	public int getFlowRate() {
		return flowRate;
	}
	
	public String getTilesPath() {
		return tilesPath;
	}
}
