package com.index.rtree;



public class IndexNode extends AbstractNode {

  public IndexNode(RTree tree, int parent, int pageNumber, int level) {
    super(tree, parent, pageNumber, level);
  }

  public  LeafNode chooseLeaf(IRect h) {
    int i = findLeastEnlargement(h);
    return ( (AbstractNode) getChild(i)).chooseLeaf(h);
  }

  public  LeafNode findLeaf(IRect h, int page) {
    for (int i = 0; i < childCount; i++) {
      if (childMBR[i].contains(h)) {
        LeafNode l = ( (AbstractNode) getChild(i)).findLeaf(h, page);
        if (l != null) {
          return l;
        }
      }
    }
    return null;
  }

  // 최소 확장 인덱스를 찾는다.
  private int findLeastEnlargement(IRect h) {
    long area = Long.MAX_VALUE;
    int sel = -1;

    for (int i = 0; i < childCount; i++) {
      long enl = childMBR[i].combinedRegion(h).getArea() - childMBR[i].getArea();
      if (enl < area) {
        area = enl;
        sel = i;
      }
      else if (enl == area) {
        sel = (childMBR[sel].getArea() <= childMBR[i].getArea()) ? sel : i;
      }
    }
    return sel;
  }

  // 새 노드를 추가한다.
  // 만약 모든 엔트리 공간이 사용되었다면 현재 노드를 분할한다.
  // 분할이 일어난 경우, true를 리턴
  // 분할이 일어나지 않은 경우, false를 리턴
  public  boolean insert(AbstractNode node) {
    if (childCount < tree.getNodeCapacity()) {
//      tuples[childCount] = null;
      childMBR[childCount] = node.getNodeMBR();
      childPageID[childCount] = node.pageID;
      childCount++;
      node.parentPageID = pageID;
      tree.file.writeNode(node);
      tree.file.writeNode(this);
      IndexNode p = (IndexNode) getParent();
      if (p != null) {
        p.adjustTree(this, null);
      }
      return false;
    }
    else {
      IndexNode[] a = splitIndex(node);
      IndexNode n = a[0];
      IndexNode nn = a[1];

      if (isRoot()) {
        n.parentPageID = 0;
        n.pageID = -1;
        nn.parentPageID = 0;
        nn.pageID = -1;
        int p = tree.file.writeNode(n);
        for (int i = 0; i < n.childCount; i++) {
          AbstractNode ch = (AbstractNode) n.getChild(i);
          ch.parentPageID = p;
          tree.file.writeNode(ch);
        }
        p = tree.file.writeNode(nn);
        for (int i = 0; i < nn.childCount; i++) {
          AbstractNode ch = (AbstractNode) nn.getChild(i);
          ch.parentPageID = p;
          tree.file.writeNode(ch);
        }
        IndexNode r = new IndexNode(tree, RTree.NIL, 0, level + 1);
        r.addData(n.getNodeMBR(), n.pageID);
        r.addData(nn.getNodeMBR(), nn.pageID);
        tree.file.writeNode(r);
      }
      else {
        n.pageID = pageID;
        n.parentPageID = parentPageID;
        nn.pageID = -1;
        nn.parentPageID = parentPageID;
        tree.file.writeNode(n);
        int j = tree.file.writeNode(nn);
        for (int i = 0; i < nn.childCount; i++) {
          AbstractNode ch = (AbstractNode) nn.getChild(i);
          ch.parentPageID = j;
          tree.file.writeNode(ch);
        }
        IndexNode p = (IndexNode) getParent();
        p.adjustTree(n, nn);
      }

      return true;
    }
  }

  // insert에서 분할이 일어난 경우, 트리를 조절한다.
  public  void adjustTree(AbstractNode n1, AbstractNode n2) {
    for (int i = 0; i < childCount; i++) {
      if (childPageID[i] == n1.pageID) {
        childMBR[i] = n1.getNodeMBR();
        tree.file.writeNode(this);
        break;
      }
    }

    if (n2 != null) {
      // 분할이 일어난 경우는 새 노드를 삽입
      insert(n2);
    }
    else if (!isRoot()) {
      // 분할이 아닌 경우는 Root까지 recursion으로 트리 MBR 조절
      IndexNode p = (IndexNode) getParent();
      p.adjustTree(this, null);
    }
  }

  // 인덱스 노드를 분할
  private IndexNode[] splitIndex(AbstractNode n) {
    int[][] group = null;

    group = quadraticSplit(n.getNodeMBR(), n.pageID);

    IndexNode i1 = new IndexNode(tree, parentPageID, pageID, level);
    IndexNode i2 = new IndexNode(tree, parentPageID, -1, level);

    int[] g1 = group[0];
    int[] g2 = group[1];

    for (int i = 0; i < g1.length; i++) {
      i1.addData(childMBR[g1[i]], childPageID[g1[i]]);
    }

    for (int i = 0; i < g2.length; i++) {
      i2.addData(childMBR[g2[i]], childPageID[g2[i]]);
    }

    return new IndexNode[] {i1, i2};
  }

  // 자식 노드를 반환
  public AbstractNode getChild(int i) {
    if (i < 0 || i >= childCount) {
      throw new IndexOutOfBoundsException("" + i);
    }
    return tree.file.readNode(childPageID[i]);
  }
}
