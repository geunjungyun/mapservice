package com.index.rtree;



public interface Node {
  public int getLevel();

  public IRect getNodeMBR();

  public boolean isLeaf();

  public boolean isRoot();

  public boolean isIndex();

  public AbstractNode getParent();
}
