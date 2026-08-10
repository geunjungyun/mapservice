package com.index.rtree;

import com.index.rtree.IShape;


public class IPoint implements IShape, Cloneable {
  public int x = -1;
  public int y = -1;

  public IPoint() {
  }

  public IPoint(IPoint p) {
    this(p.x, p.y);
  }

  public IPoint(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public String toString() {
    return (x + "," + y);
  }

  public boolean equals(Object o) {
    if (o instanceof IPoint) {
      IPoint pt = (IPoint) o;
      return (this.x == pt.x) && (this.y == pt.y);
    }
    return super.equals(o);
  }

  public Object clone() {
    return new IPoint(this);
  }

  public boolean intersects(IShape s) {
    if (s instanceof IRect) {
      return ( (IRect) s).contains(this);
    }
    return false;
  }

  public boolean contains(IShape s) {
    return false;
  }

  public boolean touches(IShape s) {
    if (s instanceof IPoint && this.equals(s)) {
      return true;
    }
    else if (s instanceof IRect) {
      return ( (IRect) s).touches(this);
    }
    else {
      return false;
    }
  }

  public IPoint getCenter() {
//    return this;
    return (IPoint)this.clone();
  }

  public IRect getMBR() {
    return new IRect(this, this);
  }

  public long getArea() {
    return 0l;
  }

  public double getMinimumDistance(IShape s) {
    if (s instanceof IRect) {
      return ( (IRect) s).getMinimumDistance(this);
    }
    else if (s instanceof IPoint) {
      return getMinimumDistance( (IPoint) s);
    }
    else {
      throw new IllegalStateException("getMinimumDistance: IShape의 인스턴스를 알 수 없습니다!");
    }
  }

  public double getMinimumDistance(IPoint p) {
    return getMinimumDistance(this.x, this.y, p.x, p.y);
  }

  public static double getMinimumDistance(double X1, double Y1, double X2, double Y2) {
    X1 -= X2;
    Y1 -= Y2;
    return Math.sqrt(X1 * X1 + Y1 * Y1);
  }
}
