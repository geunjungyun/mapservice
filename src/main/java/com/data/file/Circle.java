package com.data.file;

import org.locationtech.jts.geom.Coordinate;

public class Circle {
	
	public double radius;
	public double x;
	public double y;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String[] coordsS = "1,2,3,4,5,6,7".split(",");
		
		if(coordsS.length >= 4 && coordsS.length%2 == 0) {
			
			Coordinate[] coords = new Coordinate[coordsS.length/2];
			
			for(int i=0; i<coords.length; i++) {
				
				if(coords[i/2] == null) {
					coords[i/2] = new Coordinate();	
				}
				
				if(i%2 == 0) {
					coords[i/2].x = Double.parseDouble(coordsS[i]);
				}
				else {
					coords[i/2].y = Double.parseDouble(coordsS[i]);
				}
				
			}
			
		}

	}

}
