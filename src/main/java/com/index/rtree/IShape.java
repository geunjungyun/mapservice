package com.index.rtree;


public interface IShape {
  public boolean intersects(IShape s);

  public boolean contains(IShape s);

  public boolean touches(IShape s);

  public IPoint getCenter();

  public IRect getMBR();

  public long getArea();

  public double getMinimumDistance(IShape s);
}
