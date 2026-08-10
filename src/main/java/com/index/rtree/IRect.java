package com.index.rtree;

import com.index.rtree.IShape;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: </p>
 *
 * <p>Company: </p>
 *
 * @author 
 * @version 1.0
 */

public class IRect implements IShape, Cloneable {
	
  public int x1 = Integer.MAX_VALUE;
  public int y1 = Integer.MAX_VALUE;
  public int x2 = Integer.MIN_VALUE;
  public int y2 = Integer.MIN_VALUE;

  public IRect() {
  }

  public IRect(int x1, int y1, int x2, int y2) {
	  
	  this.x1 = x1;
	  this.x2 = x2;
	  this.y1 = y1;
	  this.y2 = y2;
	  
//    if (x2 - x1 < 0) {
//      this.x1 = x2;
//      this.x2 = x1;
//    }
//    else {
//      this.x1 = x1;
//      this.x2 = x2;
//    }
//
//    if (y2 - y1 < 0) {
//      this.y1 = y2;
//      this.y2 = y1;
//    }
//    else {
//      this.y1 = y1;
//      this.y2 = y2;
//    }
  }
  
  public IRect(double x1, double y1, double x2, double y2) {
	  this.x1 = (int)x1;
	  this.x2 = (int)x2;
	  this.y1 = (int)y1;
	  this.y2 = (int)y2;
  }  

  public IRect(IPoint p1, IPoint p2) {
    this(p1.x, p1.y, p2.x, p2.y);
  }

  public IRect(IRect r) {
    this(r.x1, r.y1, r.x2, r.y2);
  }

  public String toString() {
    return (x1 + "," + y1 + "," + x2 + "," + y2);
  }

  public boolean equals(Object o) {
    if (o instanceof IRect) {
      IRect r = (IRect) o;
      return (r.x1 == this.x1 &&
              r.y1 == this.y1 &&
              r.x2 == this.x2 &&
              r.y2 == this.y2);
    }
    return super.equals(o);
  }

  public Object clone() {
    return new IRect(this);
  }

  public boolean intersects(IShape s) {
    if (s instanceof IRect) {
      return intersects( (IRect) s);
    }
    else if (s instanceof IPoint) {
      return intersects( (IPoint) s);
    }
    else {
      throw new IllegalStateException("intersects: IShape의 인스턴스를 알 수 없습니다!");
    }
  }
  
  public boolean intersects(IPoint s){
	  if( (s.x > this.x1 && s.x < this.x2) 
			  && (s.y > this.y1 && s.y < this.y2)){
		  return true;
	  }
	  return false;
  }
  
  
  public boolean intersects(int x, int y){
	  if( (x > this.x1 && x < this.x2) 
			  && (y > this.y1 && y < this.y2)){
		  return true;
	  }
	  return false;
  }
  

  public boolean contains(IShape s) {
    if (s instanceof IRect) {
      return contains( (IRect) s);
    }
    else if (s instanceof IPoint) {
      return contains( (IPoint) s);
    }
    else {
      throw new IllegalStateException("contains: IShape의 인스턴스를 알 수 없습니다!");
    }
  }

  public boolean touches(IShape s) {
    if (s instanceof IRect) {
      return touches( (IRect) s);
    }
    else if (s instanceof IPoint) {
      return touches( (IPoint) s);
    }
    else {
      throw new IllegalStateException("touches: IShape의 인스턴스를 알 수 없습니다!");
    }
  }

  public IPoint getCenter() {
    return new IPoint( (int) ( (x1 + x2) / 2.0), (int) ( (y1 + y2) / 2.0));
  }

  public IRect getMBR() {
    return new IRect(this);
  }

  public long getArea() {
    return ( (long) x2 - x1) * (y2 - y1);
  }

  public double getMinimumDistance(IShape s) {
    if (s instanceof IRect) {
      return getMinimumDistance( (IRect) s);
    }
    else if (s instanceof IPoint) {
      return getMinimumDistance( (IPoint) s);
    }
    else {
      throw new IllegalStateException("getMinimumDistance: IShape의 인스턴스를 알 수 없습니다!");
    }
  }

  public boolean intersects(IRect r) {
    if (x1 > r.x2 || x2 < r.x1)return false;
    else if (y1 > r.y2 || y2 < r.y1)return false;
    else return true;
  }

  public boolean contains(IRect r) {
    if (x1 > r.x1 || x2 < r.x2)return false;
    else if (y1 > r.y1 || y2 < r.y2)return false;
    else return true;
  }

  public boolean touches(IRect r) {
    if (x1 == r.x1 || x2 == r.x2)return true;
    else if (y1 == r.y1 || y2 == r.y2)return true;
    else return false;
  }

  public double getMinimumDistance(IRect r) {
    double ret = 0.0;
    double dx = 0.0;

    if (r.x2 < this.x1) {
      dx = Math.abs(r.x2 - this.x1);
    }
    else if (this.x2 > r.x1) {
      dx = Math.abs(r.x1 - this.x2);
    }

    ret += dx * dx;
    dx = 0.0;

    if (r.y2 < this.y1) {
      dx = Math.abs(r.y2 - this.y1);
    }
    else if (this.y2 > r.y1) {
      dx = Math.abs(r.y1 - this.y2);
    }

    ret += dx * dx;

    return Math.sqrt(ret);
  }

  public boolean contains(IPoint p) {
    if (x1 > p.x || x2 < p.x)return false;
    else if (y1 > p.y || y2 < p.y)return false;
    else return true;
  }

  public boolean touches(IPoint p) {
    if (x1 == p.x || x2 == p.x)return true;
    else if (y1 == p.y || y2 == p.y)return true;
    else return false;
  }

  public double getMinimumDistance(IPoint p) {
    double ret = 0.0;
    double dx = 0.0;

    if (p.x < x1) {
      dx = Math.abs(x1 - p.x);
    }
    else if (p.x > x2) {
      dx = Math.abs(p.x - x2);
    }
    ret += dx * dx;
    dx = 0.0;

    if (p.y < y1) {
      dx = Math.abs(y1 - p.y);
    }
    else if (p.y > y2) {
      dx = Math.abs(p.y - y2);
    }
    ret += dx * dx;

    return Math.sqrt(ret);
  }
  
  public double getMinimumDistance(int x, int y) {
	    double ret = 0.0;
	    double dx = 0.0;

	    if (x < x1) {
	      dx = Math.abs(x1 - x);
	    }
	    else if (x > x2) {
	      dx = Math.abs(x - x2);
	    }
	    ret += dx * dx;
	    dx = 0.0;

	    if (y < y1) {
	      dx = Math.abs(y1 - y);
	    }
	    else if (y > y2) {
	      dx = Math.abs(y - y2);
	    }
	    ret += dx * dx;

	    return Math.sqrt(ret);
	  }
  

  public int getIntersectingArea(IRect r) {
    if (x1 > r.x2 || x2 < r.x1)return 0;
    else if (y1 > r.y2 || y2 < r.y1)return 0;

    int ret = 1;
    int n1, n2;

    n1 = Math.max(x1, r.x1);
    n2 = Math.min(x2, r.x2);
    ret *= n2 - n1;

    n1 = Math.max(y1, r.y1);
    n2 = Math.min(y2, r.y2);
    ret *= n2 - n1;

    return ret;
  }

  // R-star에서 사용
  public int getMargin() {
    return ( (x2 - x1) + (y2 - y1)) * 2;
  }

  public IRect combinedRegion(IRect r) {
    int cx1 = Math.min(x1, r.x1);
    int cy1 = Math.min(y1, r.y1);
    int cx2 = Math.max(x2, r.x2);
    int cy2 = Math.max(y2, r.y2);

    return new IRect(cx1, cy1, cx2, cy2);
  }

  public static void combinedRegion(IRect dr, IRect sr) {
    int cx1 = Math.min(dr.x1, sr.x1);
    int cy1 = Math.min(dr.y1, sr.y1);
    int cx2 = Math.max(dr.x2, sr.x2);
    int cy2 = Math.max(dr.y2, sr.y2);

    dr.x1 = cx1;
    dr.y1 = cy1;
    dr.x2 = cx2;
    dr.y2 = cy2;
  }
}
