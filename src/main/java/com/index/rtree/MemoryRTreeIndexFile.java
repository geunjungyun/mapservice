package com.index.rtree;

import java.util.*;


public class MemoryRTreeIndexFile extends IndexFile {
  private Hashtable memFile = new Hashtable();

  public  void initialize(RTree tree, float fillFactor, int capacity) {
    super.initialize(tree, fillFactor, capacity);
    memFile.clear();
  }

  public  AbstractNode readNode(int page) throws PageFaultError {
    if (page < 0) {
      throw new IllegalArgumentException("페이지는 음수가 될 수 없습니다.");
    }

    AbstractNode ret = (AbstractNode) memFile.get(new Integer(page));
//    AbstractNode ret = (AbstractNode) memFile.get(new String("" + page));

    if (ret == null) {
      throw new PageFaultError("잘못된 페이지 요청입니다..");
    }

    return ret;
  }

  public  int writeNode(AbstractNode n) throws PageFaultError {
    if (n == null) {
      throw new IllegalArgumentException("노드가 널입니다.");
    }

    int i = 0;
    if (n.getPageID() < 0) {
      while (true) {
        if (!memFile.containsKey(new Integer(i))) {
//          if (!memFile.containsKey(new String("" + i))) {
          break;
        }
        i++;
      }
      n.setPageID(i);
    }
    else {
      i = n.getPageID();
    }

    memFile.put(new Integer(i), n);
//    memFile.put(new String("" + i), n);

    return i;
  }

  public  AbstractNode deletePage(int page) throws PageFaultError {
    return (AbstractNode) memFile.remove(new Integer(page));
//      return (AbstractNode) memFile.remove(new String("" + page));
  }

@Override
public SortedLinkedHashSet<Integer> searchList(int cnt, int pageNo) {
	// TODO Auto-generated method stub
	return null;
}
}
