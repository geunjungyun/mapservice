package com.index.rtree;

import java.util.Vector;





public abstract class IndexFile {

  /**
   * 연결된 R-tree
   */ 
  public  RTree tree = null;

  // 노드 최소 채움 비율, 0 ~ 0.5
  public  float fillFactor = -1;

  // 노드 최대 채움 갯수
  public  int nodeCapacity = -1;

  // 헤더 크기는 다음과 같은 공식을 통해 구할 수 있다.
  // headerSize = fillFactor + nodeCapacity + pageSize
  public  int headerSize = 12;
  
  public int objSize = 0;

  // 페이지 크기는 다음의 공식을 사용하여 구할 수 있다.
  // [nodeCapacity * {sizeof(Int2DRect) + sizeof(Branch)}] + parent + level + childCount
  // = [nodeCapacity * {(4 * sizeof(int) + sizeof(int)}] + sizeof(int) + sizeof(int) + sizeof(int)
  // = (nodeCapacity * 20) + 12
  public  int pageSize = -1;

  public static final int EMPTY_PAGE = -2;

//
// 추상 메소드 정의
//
  // 페이지에 저장된 노드를 리턴
  public  abstract com.index.rtree.AbstractNode readNode(int page) throws PageFaultError;

  // 노드를 가용한 페이지에 저장, 페이지 번호를 리턴
  public  abstract int writeNode(com.index.rtree.AbstractNode o) throws PageFaultError;

  // 페이지에 저장된 노드를 삭제
  public  abstract com.index.rtree.AbstractNode deletePage(int page) throws PageFaultError;

  // 페이지 파일 초기화, R-Tree에서 호출
  public  void initialize(com.index.rtree.RTree tree, float fillFactor, int capacity) {
    this.fillFactor = fillFactor;
    this.nodeCapacity = capacity;
    this.tree = tree;

    // {nodeCapacity * [(4 * sizeof(int)) + sizeof(int)]} + sizeof(int) + sizeof(int) + sizeof(int)
    this.pageSize = capacity * (16 + 4) + 12;
  }
  
  public abstract SortedLinkedHashSet<Integer> searchList(int cnt, int pageNo); 

  public  void finalize() throws Throwable {
    super.finalize();
  }
}
