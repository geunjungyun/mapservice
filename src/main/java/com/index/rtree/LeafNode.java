package com.index.rtree;

import java.util.*;

import com.data.file.FileLayer;



public class LeafNode extends AbstractNode {


  public LeafNode(RTree tree, int parent, int pageNumber) {
    super(tree, parent, pageNumber, 0);
  }

  public  LeafNode(RTree tree, int parent) {
    super(tree, parent, -1, 0);
  }

  public  LeafNode chooseLeaf(IRect h) {
    return this;
  }

  public  LeafNode findLeaf(IRect h, int page) {
    for (int i = 0; i < childCount; i++) {
      if (childPageID[i] == page && childMBR[i].equals(h)) {
        return this;
      }
    }

    return null;
  }

  // 새 노드를 추가한다.
  // 만약 모든 엔트리 공간이 사용되었다면 새로운 리프 노드 2개로 분할한다.
  public  int insert(IRect rect, int page) {
    if (childCount < tree.getNodeCapacity()) {
      childMBR[childCount] = rect;
      childPageID[childCount] = page;
      childCount++;
      tree.file.writeNode(this);
      IndexNode p = (IndexNode) getParent();
      if (p != null) {
        p.adjustTree(this, null);
      }
      return pageID;
    }
    else {
      LeafNode[] a = splitLeaf(rect, page);
      LeafNode l = a[0];
      LeafNode ll = a[1];

      if (isRoot()) {
        // 루트 노드가 가득 찼으므로 루트 노드를 분할한다.
        // 새로운 인덱스 노드가 루트 노드가 된다.
        l.parentPageID = 0;
        l.pageID = -1;
        ll.parentPageID = 0;
        ll.pageID = -1;
        tree.file.writeNode(l);
        tree.file.writeNode(ll);
        // 페이지는 0, 레벨은 1인 새로운 루트 노드를 만든다.
        IndexNode r = new IndexNode(tree, RTree.NIL, 0, 1);
        r.addData(l.getNodeMBR(), l.pageID);
        r.addData(ll.getNodeMBR(), ll.pageID);
        tree.file.writeNode(r);
      }
      else {
        // 왼쪽 노드는 이전 페이지를 사용, 오른쪽 노드는 새로운 페이지를 사용
        l.pageID = pageID;
        ll.pageID = -1;
        tree.file.writeNode(l);
        tree.file.writeNode(ll);
        IndexNode p = (IndexNode) getParent();
        p.adjustTree(l, ll);
      }

      // 어느 리프에 삽입되었는지 찾아서 페이지를 반환한다.
      for (int i = 0; i < l.childCount; i++) {
        if (l.childPageID[i] == page) {
          return l.pageID;
        }
      }

      for (int i = 0; i < ll.childCount; i++) {
        if (ll.childPageID[i] == page) {
          return ll.pageID;
        }
      }

      return -1;
    }
  }

  public  int delete(IRect h, int page) {
    for (int i = 0; i < childCount; i++) {
      if (childMBR[i].equals(h) && childPageID[i] == page) {
        int pointer = childPageID[i];
        // 데이터 제거 후 노드 저장
        deleteData(i);
        tree.file.writeNode(this);
        Vector q = new Vector();
        condenseTree(q);

        // 제거된 노드를 재삽입
        try {
          for (int l = 0; l < q.size(); l++) {
            AbstractNode n = (AbstractNode) q.elementAt(l);
              tree.file.deletePage(n.pageID);
            if (n.isLeaf()) {
              for (int j = 0; j < n.childCount; j++) {
                tree.insertEntity(n.childMBR[j], n.childPageID[j]);
              }
            }
            else {
              Vector v = tree.traversePostOrder(n);
              //!!!!!
//              tree.file.deletePage(n.pageID);
              for (int j = 0; j < v.size(); j++) {
                AbstractNode m = (AbstractNode) v.elementAt(j);
                // !!!!!!
                tree.file.deletePage(m.pageID);
                if (m.isLeaf()) {
                  for (int k = 0; k < m.childCount; k++) {
                    tree.insertEntity(m.childMBR[k], m.childPageID[k]);
                  }
                }
//                tree.file.deletePage(m.pageID);
              }
            }
//            tree.file.deletePage(n.pageID);
          }
        }
        catch (Exception e) {
//          FileLayer.LOG.warn("노드 삽입에 실패했습니다.");
//          FileLayer.LOG.error(e);
        	e.printStackTrace();
          System.exit(1);
        }

        return pointer;
      }
    }
    return RTree.NIL;
  }

  private LeafNode[] splitLeaf(IRect h, int page) {
    int[][] group = null;

    group = quadraticSplit(h, page);

    LeafNode l = new LeafNode(tree, parentPageID);
    LeafNode ll = new LeafNode(tree, parentPageID);

    int[] g1 = group[0];
    int[] g2 = group[1];

    for (int i = 0; i < g1.length; i++) {
      l.addData(childMBR[g1[i]], childPageID[g1[i]]);
    }

    for (int i = 0; i < g2.length; i++) {
      ll.addData(childMBR[g2[i]], childPageID[g2[i]]);
    }

    return new LeafNode[] {
        l, ll};
  }

  public int getDataPointer(int i) {
    if (i < 0 || i >= childCount) {
      throw new IndexOutOfBoundsException("" + i);
    }

    return childPageID[i];
  }
}
