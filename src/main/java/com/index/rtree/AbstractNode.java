package com.index.rtree;

import java.util.*;

import com.index.rtree.PageFaultError;

public abstract class AbstractNode implements Node {

  protected transient RTree tree;
  protected  int level;
  protected  int pageID;
  protected  int parentPageID;
  protected int childCount;
  public int[] childPageID;
  public IRect[] childMBR;

  public AbstractNode(RTree tree, int parentPageID, int pageID, int level) {
    this.parentPageID = parentPageID;
    this.tree = tree;
    this.pageID = pageID;
    this.level = level;
    this.childMBR = new IRect[tree.getNodeCapacity() + 1];
    this.childPageID = new int[tree.getNodeCapacity() + 1];
    this.childCount = 0;
  }
  public int getChildCount(){
    return this.childCount;
  }

  public void setChildCount(int childCount){
    this.childCount = childCount;
  }

  public int getLevel() {
    return level;
  }

  public boolean isRoot() {
    return (parentPageID == RTree.NIL);
  }

  public boolean isIndex() {
    return (level != 0);
  }

  public boolean isLeaf() {
    return (level == 0);
  }

  public int getPageID(){
    return this.pageID;
  }

  public void setPageID(int pageID){
    this.pageID = pageID;
  }

  public int getParentPageID(){
    return this.parentPageID;
  }

  public void setParentPageID(int parentPageID){
    this.parentPageID = parentPageID;
  }

  public IRect getNodeMBR() {
    if (childCount > 0) {

      int cx1 = Integer.MAX_VALUE;
      int cy1 = Integer.MAX_VALUE;
      int cx2 = Integer.MIN_VALUE;
      int cy2 = Integer.MIN_VALUE;

      for (int i = 0; i < childCount; i++) {
        cx1 = Math.min(cx1, childMBR[i].x1);
        cy1 = Math.min(cy1, childMBR[i].y1);
        cx2 = Math.max(cx2, childMBR[i].x2);
        cy2 = Math.max(cy2, childMBR[i].y2);
      }
      return new IRect(cx1, cy1, cx2, cy2);
    }
    else {
      return new IRect();
    }
  }

  public AbstractNode getParent() {
    if (isRoot()) {
      return null;
    }
    else {
      return tree.file.readNode(parentPageID);
    }
  }

  public  abstract LeafNode chooseLeaf(IRect MBR);

  public  abstract LeafNode findLeaf(IRect MBR, int page);

  public  void addData(IRect rect, int page) {
    if (childCount == tree.getNodeCapacity()) {
      throw new IllegalStateException("노드가 꽉 차있습니다.");
    }

    this.childMBR[childCount] = rect;
    this.childPageID[childCount] = page;
    childCount++;
  }

  public  void deleteData(int childIndex) {
    if (childIndex < 0 || childIndex >= childCount) {
      throw new IllegalStateException("삭제하려는 자식 노드 인덱스가 잘못되었습니다.");
    }

    System.arraycopy(childMBR, childIndex + 1, childMBR, childIndex, childCount - childIndex - 1);
    System.arraycopy(childPageID, childIndex + 1, childPageID, childIndex, childCount - childIndex - 1);
    childCount--;
  }

  public  int[][] quadraticSplit(IRect rect, int page) {
    if (rect == null) {
      throw new IllegalArgumentException("MBR이 Null입니다.");
    }

    childMBR[childCount] = rect;
    this.childPageID[childCount] = page;
    int total = childCount + 1;

    int[] mask = new int[total];
    for (int i = 0; i < total; i++) {
      mask[i] = 1;
    }

    int min = Math.round(tree.getNodeCapacity() * tree.getFillFactor());
    if (min < 2) min = 2;

    int rem = total;

    int c = total - min;

    int[] g1 = new int[c];
    int[] g2 = new int[c];

    int i1 = 0, i2 = 0;

    int[] seed = pickSeeds();
    g1[i1++] = seed[0];
    g2[i2++] = seed[1];
    rem -= 2;
    mask[g1[0]] = -1;
    mask[g2[0]] = -1;

    while (rem > 0) {
      if (i2 == c) {
        for (int i = 0; i < total; i++) {
          if (mask[i] != -1) {
            g1[i1++] = i;
            mask[i] = -1;
            rem--;
          }
        }
      }
      else if (i1 == c) {
        for (int i = 0; i < total; i++) {
          if (mask[i] != -1) {
            g2[i2++] = i;
            mask[i] = -1;
            rem--;
          }
        }
      }
      else {
        IRect mbr1 = (IRect) childMBR[g1[0]].clone();
        for (int i = 1; i < i1; i++) {
          IRect.combinedRegion(mbr1, childMBR[g1[i]]);
        }
        IRect mbr2 = (IRect) childMBR[g2[0]].clone();
        for (int i = 1; i < i2; i++) {
          IRect.combinedRegion(mbr2, childMBR[g2[i]]);
        }

        long dif = Long.MIN_VALUE;
        long d1 = 0, d2 = 0;
        int sel = -1;
        for (int i = 0; i < total; i++) {
          if (mask[i] != -1) {
            IRect a = mbr1.combinedRegion(childMBR[i]);
            d1 = a.getArea() - mbr1.getArea();
            IRect b = mbr2.combinedRegion(childMBR[i]);
            d2 = b.getArea() - mbr2.getArea();
            if (Math.abs(d1 - d2) > dif) {
              dif = Math.abs(d1 - d2);
              sel = i;
            }
          }
        }

        if (d1 < d2) {
          g1[i1++] = sel;
        }
        else if (d2 < d1) {
          g2[i2++] = sel;
        }
        else if (mbr1.getArea() < mbr2.getArea()) {
          g1[i1++] = sel;
        }
        else if (mbr2.getArea() < mbr1.getArea()) {
          g2[i2++] = sel;
        }
        else if (i1 < i2) {
          g1[i1++] = sel;
        }
        else if (i2 < i1) {
          g2[i2++] = sel;
        }
        else {
          g1[i1++] = sel;
        }
        mask[sel] = -1;
        rem--;
      }
    }

    int[][] ret = new int[2][];
    ret[0] = new int[i1];
    ret[1] = new int[i2];

    for (int i = 0; i < i1; i++) {
      ret[0][i] = g1[i];
    }
    for (int i = 0; i < i2; i++) {
      ret[1][i] = g2[i];
    }

    return ret;
  }

  public  int[] pickSeeds() {
    long inefficiency = Long.MIN_VALUE;
    int i1 = 0, i2 = 0;

    for (int i = 0; i < childCount; i++) {
      for (int j = i + 1; j <= childCount; j++) {
        IRect r = childMBR[i].combinedRegion(childMBR[j]);

        long d = r.getArea() - childMBR[i].getArea() - childMBR[j].getArea();

        if (d > inefficiency) {
          inefficiency = d;
          i1 = i;
          i2 = j;
        }
      }
    }

    return new int[] {i1, i2};
  }

  public  void condenseTree(Vector q) {
/*    if (isRoot()) {

      if (!isLeaf() && childCount == 1) {
        AbstractNode n = tree.file.readNode(childPageID[0]);
        tree.file.deletePage(n.pageID);
        n.pageID = 0;
        n.parentPageID = RTree.NIL;
        tree.file.writeNode(n);
        if (!n.isLeaf()) {
          for (int i = 0; i < n.childCount; i++) {
            AbstractNode m = ( (IndexNode) n).getChild(i);
            m.parentPageID = 0;
            tree.file.writeNode(m);
          }
        }
      }
    }
    else {
      AbstractNode p = getParent();
      int e;

      for (e = 0; e < p.childCount; e++) {
        if (pageID == p.childPageID[e]) {
          break;
        }
      }

      int min = Math.round(tree.getNodeCapacity() * tree.getFillFactor());
      if (childCount < min) {

        p.deleteData(e);

        q.addElement(this);
      }
      else {
        p.childMBR[e] = getNodeMBR();
      }

      tree.file.writeNode(p);
      p.condenseTree(q);
    }
*/  }
}
