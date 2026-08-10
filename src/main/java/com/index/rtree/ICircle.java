package com.index.rtree;


public class ICircle implements IShape, Cloneable {
	
	int x = 0;
	int y = 0;
	
	int radius = 0;
	
	public ICircle(int x, int y, int radius){
		this.x = x;
		this.y = y;
		this.radius = radius;
	}
	
	public int getX(){
		return this.x;
	}
	
	public int getY(){
		return this.y;
	}
	
	public int getRadius(){
		return this.radius;
	}
	
	public boolean contains(IShape s) {
		// TODO Auto-generated method stub
		return false;
	}

	public long getArea() {
		// TODO Auto-generated method stub
		return 0;
	}

	public IPoint getCenter() {
		// TODO Auto-generated method stub
		return null;
	}

	public IRect getMBR() {
		// TODO Auto-generated method stub
		return null;
	}

	public double getMinimumDistance(IShape s) {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean intersects(IShape s) {
		IRect rec = (IRect)s;
		// TODO Auto-generated method stub
		//IPoint center = s.getCenter();
		if(rec.intersects(this.x, this.y)){
			return true;
		}
		
//		double distance = Math.sqrt( Math.pow(center.x - this.x, 2) + 
//				Math.pow(center.y - this.y, 2));
		double distance = rec.getMinimumDistance(this.x, this.y);
		
		if(distance <= this.radius){
			return true;
		}
		
		return false;
	}

	public boolean touches(IShape s) {
		// TODO Auto-generated method stub
		return false;
	}

}
