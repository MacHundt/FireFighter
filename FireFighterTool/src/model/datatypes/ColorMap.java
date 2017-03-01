package model.datatypes;

import java.awt.Color;

public class ColorMap {
	
	private double minRise;
	private double maxRise;
	
	public ColorMap(double min, double max) {
		minRise = min;
		maxRise = max;
	}
	
	public Color getColor(double rise) {
		int bucket = 0;
		if (rise < 0) {
			double difference = Math.abs(minRise);
			double bucketSize = difference/4;
			bucket = (int) Math.floor((Math.abs(minRise) + rise)/bucketSize);
		}
		else {
			double difference = maxRise;
			double bucketSize = difference/5;
			bucket = (int) Math.floor(rise/bucketSize)+4; 
		}
		
		switch (bucket) {
		case 0:
			return new Color(31, 98, 183);
		case 1:
			return new Color(44, 137, 196);
		case 2:
			return new Color(60, 172, 211);
		case 3:
			return new Color(104, 199, 224);
		case 4:
			return new Color(250, 250, 89); // ab hier positiv
		case 5:
			return new Color(251, 214, 116);
		case 6:
			return new Color(253, 174, 97);
		case 7:
			return new Color(244, 109, 67);
		case 8:
			return new Color(215, 48, 39);
		default:
			return new Color(255, 0, 0);
		}
	}

}
