package com.index.rtree;

import java.nio.ByteBuffer;
import java.util.*;

import com.data.file.FileLayer;



/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright:
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author
 * @version 1.0
 */
public class RTree {

	//static Logger logger = MapLog.getSCLog();
	// 노드 채움 상수
	public static final float FILL_FACTOR = 0.4f;
	// 자식 노드 갯수
	public static final int CAPACITY = 10;

	// 인덱스 저장소
	public IndexFile file = null;

	// 루트 노드의 부모 노드 상수
	public static final int NIL = -1;

	// 검색 모드
	public static final int SEARCH_INTERSECTION = 0;
	public static final int SEARCH_CONTAINMENT = 1;

	// 새 메모리 페이지 파일로 R-Tree을 생성한다.
	public RTree(float fillFactor, int capacity) throws Exception {
		this(fillFactor, capacity, new MemoryRTreeIndexFile());
	}

	// 새 페이지 파일로 R-Tree를 생성한다.
	public RTree(float fillFactor, int capacity, IndexFile file)
			throws Exception {
		if (fillFactor < 0 || fillFactor > 0.5) {
			throw new IllegalArgumentException(
					"Fill factor는 0에서 0.5 사이 값이여야 합니다..");
		}

		if (capacity <= 1) {
			throw new IllegalArgumentException("Capacity는 1보다 커야 합니다.");
		}

		if (file.tree != null) {
			throw new IllegalArgumentException("PageFile이 새 파일이 아닙니다.");
		}

		file.initialize(this, fillFactor, capacity);
		this.file = file;

		// 비어있는 루트 노드를 만들어 저장한다.
		LeafNode root = new LeafNode(this, NIL, 0);
		file.writeNode(root);
	}

	// 존재하는 R-Tree 인덱스 파일로 R-Tree를 구성한다.
	// CachedPersistentPageFile은 인덱스를 메모리에 관리한다.
	// PersistentPageFile은 인덱스를 디스크에 관리한다.
	public RTree(IndexFile file) throws Exception {
		if (file.tree != null) {
			throw new IllegalArgumentException(
					"PageFile이 이미 다른 인스턴스에 의해 사용중입니다.");
		}

		file.tree = this;
		this.file = file;

		// 초기화
		if (file instanceof CachedRTreeIndexFile) {
			((CachedRTreeIndexFile) file).diskToMemory();
		}
	}

	public int getNodeCapacity() {
		return file.nodeCapacity;
	}

	public float getFillFactor() {
		return file.fillFactor;
	}

	public int getPageSize() {
		return file.pageSize;
	}

	public int getTreeLevel() {
		return file.readNode(0).getLevel();
	}

	// 페이지의 데이터와 MBR을 R-Tree에 삽입한다.
	public int insertEntity(IRect rect, int page) throws Exception {
		if (rect == null) {
			throw new IllegalArgumentException("입력된 MBR이 Null 입니다.");
		}

		AbstractNode root = file.readNode(0);
		LeafNode l = root.chooseLeaf(rect);
		return l.insert(rect, page);
	}

	// rect를 포함하는 리프 노드를 R-Tree에서 삭제한다.
	public int deleteEntity(IRect rect, int page) throws Exception {
		if (rect == null) {
			throw new IllegalArgumentException("입력된 MBR이 Null 입니다.");
		}

		AbstractNode root = file.readNode(0);
		LeafNode l = root.findLeaf(rect, page);
		if (l != null) {
			return l.delete(rect, page);
		}
		return NIL;
	}

	// 레벨 순회
	public Vector traverseByLevel(AbstractNode root) throws Exception {
		if (root == null) {
			throw new IllegalArgumentException("노드가 널입니다.");
		}

		Vector ret = new Vector();
		Vector v = traversePostOrder(root);

		for (int i = 0; i <= getTreeLevel(); i++) {
			Vector a = new Vector();
			for (int j = 0; j < v.size(); j++) {
				Node n = (Node) v.elementAt(j);
				if (n.getLevel() == i) {
					a.addElement(n);
				}
			}
			for (int j = 0; j < a.size(); j++) {
				ret.addElement(a.elementAt(j));
			}
		}

		return ret;
	}

	// 레벨 순회
	public Enumeration traverseByLevel() throws Exception {
		class ByLevelEnum implements Enumeration {
			private boolean hasNext = true;
			private Vector nodes;
			private int index = 0;

			public ByLevelEnum() throws Exception {
				AbstractNode root = file.readNode(0);
				nodes = traverseByLevel(root);
			}

			public boolean hasMoreElements() {
				return hasNext;
			}

			public Object nextElement() {
				if (!hasNext) {
					throw new NoSuchElementException("traverseByLevel");
				}

				Object n = nodes.elementAt(index);
				index++;
				if (index == nodes.size()) {
					hasNext = false;
				}
				return n;
			}
		}
		;

		return new ByLevelEnum();
	}

	// 후위 순회
	public Vector traversePostOrder(AbstractNode root) throws Exception {
		if (root == null) {
			throw new IllegalArgumentException("노드가 널입니다.");
		}

		Vector v = new Vector();
		v.addElement(root);

		if (root.isLeaf()) {
		} else {
			for (int i = 0; i < root.childCount; i++) {
				Vector a = traversePostOrder(((IndexNode) root).getChild(i));
				for (int j = 0; j < a.size(); j++) {
					v.addElement(a.elementAt(j));
				}
			}
		}
		return v;
	}

	// 후위 순회
	public Enumeration traversePostOrder() throws Exception {
		class PostOrderEnum implements Enumeration {
			private boolean hasNext = true;
			private Vector nodes;
			private int index = 0;

			public PostOrderEnum() throws Exception {
				AbstractNode root = file.readNode(0);
				nodes = traversePostOrder(root);
			}

			public boolean hasMoreElements() {
				return hasNext;
			}

			public Object nextElement() {
				if (!hasNext) {
					throw new NoSuchElementException("traversePostOrder");
				}

				Object n = nodes.elementAt(index);
				index++;
				if (index == nodes.size()) {
					hasNext = false;
				}
				return n;
			}
		}
		;

		return new PostOrderEnum();
	}

	// 전위 순회
	public Vector traversePreOrder(AbstractNode root) throws Exception {
		if (root == null) {
			throw new IllegalArgumentException("노드가 널입니다.");
		}

		Vector v = new Vector();

		if (root.isLeaf()) {
			v.addElement(root);
		} else {
			for (int i = 0; i < root.childCount; i++) {
				Vector a = traversePreOrder(((IndexNode) root).getChild(i));
				for (int j = 0; j < a.size(); j++) {
					v.addElement(a.elementAt(j));
				}
			}
			v.addElement(root);
		}
		return v;
	}

	// 전위 순회
	public Enumeration traversePreOrder() throws Exception {
		class PreOrderEnum implements Enumeration {
			private boolean hasNext = true;
			private Vector nodes;
			private int index = 0;

			public PreOrderEnum() throws Exception {
				AbstractNode root = file.readNode(0);
				nodes = traversePreOrder(root);
			}

			public boolean hasMoreElements() {
				return hasNext;
			}

			public Object nextElement() {
				if (!hasNext) {
					throw new NoSuchElementException("traversePreOrder");
				}

				Object n = nodes.elementAt(index);
				index++;
				if (index == nodes.size()) {
					hasNext = false;
				}
				return n;
			}
		}
		;

		return new PreOrderEnum();
	}

	// R-Tree를 System.out로 출력
	public Vector<IRect> dumpString() {
		Vector<IRect> rects = new Vector();
		dumpString("", this.file.readNode(0), rects);
		return rects;
	}

	public void dumpString(String prevStr, AbstractNode root, Vector<IRect> rects) {
		if (root == null) {
			//FreeLayer.LOG.debug("루트가 널입니다.");
			return;
		}
		/*
		FreeLayer.LOG.debug(prevStr + "< Page: " + root.pageID + ", Level: "
				+ root.level + ", UsedSpace: " + root.childCount + ", Parent: "
				+ root.parentPageID + ", IsRoot: " + root.isRoot()
				+ ", IsIndex: " + root.isIndex() + ", IsLeaf: " + root.isLeaf()
				+ " >");
		FreeLayer.LOG.debug(prevStr + "< MBR: " + root.getNodeMBR() + " >");
		*/
		for (int i = 0; i < root.childCount; i++) {
			if (root.isLeaf()) {
				/*
				FreeLayer.LOG.debug(prevStr + " (" + (i + 1) + ") "
						+ root.childMBR[i].toString() + " --> "
						+ " child page: " + root.childPageID[i]);
						*/
				rects.add(root.childMBR[i]);
			} else {
//				FreeLayer.LOG.debug(prevStr + " (" + (i + 1) + ") "
//						+ root.childMBR[i].toString() + " --> "
//						+ " index page: " + root.childPageID[i]);
			}
		}
//		System.out.println();

		if (!root.isLeaf()) {
			for (int i = 0; i < root.childCount; i++) {
				AbstractNode n = file.readNode(root.childPageID[i]);
				dumpString(prevStr, n, rects);
			}
		}
	}

	// MBR이 사각형에 교차하는 데이터, 또는 MBR이 사각형에 포함되는 데이터 반환
	public Vector searchRect(IRect h, int searchMethod) throws Exception {
		if (searchMethod == RTree.SEARCH_INTERSECTION) {
			return intersectionRectPortion(h, new IRect[0], this.file.readNode(0));
		} else if (searchMethod == RTree.SEARCH_CONTAINMENT) {
			return containmentRectPortion(h, new IRect[0], this.file.readNode(0));
		} else {
			throw new IllegalArgumentException("검색 조건을 알 수 없습니다.");
		}
	}
	
	// MBR이 사각형에 교차하는 데이터, 또는 MBR이 사각형에 포함되는 데이터 반환, 중심점을 기준으로 가장 가까운 것 부터 정렬함.
	public Vector searchRect2(IRect h, int searchMethod, IPoint pt) throws Exception {
		if (searchMethod == RTree.SEARCH_INTERSECTION) {
			Vector<ResultNode> rns = intersectionRectPortion2(h, new IRect[0], this.file.readNode(0), pt);
			Collections.sort(rns, Collections.reverseOrder());
			Vector rs = new Vector();
			for(ResultNode rn : rns){
				rs.add(rn.pageId);
			}
			return rs;
			//return intersectionRectPortion2(h, new IRect[0], this.file.readNode(0));
			
		} else if (searchMethod == RTree.SEARCH_CONTAINMENT) {
			return containmentRectPortion(h, new IRect[0], this.file.readNode(0));
		} else {
			throw new IllegalArgumentException("검색 조건을 알 수 없습니다.");
		}
	}
	
	public class ResultNode implements Comparable{
		int pageId = 0;
		double distance = Double.MAX_VALUE;
		
		
		//@Override
		public int compareTo(Object o) {
			// TODO Auto-generated method stub
			
			if(o != null){
				ResultNode rn = (ResultNode)o;
				if(rn.distance > this.distance){
					return 1;
				}
				else{
					return -1;
				}
			}
			
			return 0;
		}
		
		
	}

	// IShape 을 사용하여 검색, 현재는 포인트 데이터를 대상으로 원 영역 검색 기능 때문에 추가 함
	public Vector searchIShape(IShape h, int searchMethod)
			throws Exception {
		if (searchMethod == RTree.SEARCH_INTERSECTION) {
			return intersectionIShape(h, new IRect[0], this.file.readNode(0));
		}
		// else if (searchMethod == RTree.SEARCH_CONTAINMENT) {
		// return containmentRectPortion(h, new IRect[0],
		// this.file.readNode(0));
		// }
		else {
			throw new IllegalArgumentException("검색 조건을 알 수 없습니다.");
		}
	}
	
	// IShape 을 사용하여 검색, 현재는 포인트 데이터를 대상으로 원 영역 검색 기능 때문에 추가 함
	public Vector searchIShape(IShape h, int searchMethod, IPoint pt)
			throws Exception {
		if (searchMethod == RTree.SEARCH_INTERSECTION) {
			return intersectionIShape(h, new IRect[0], this.file.readNode(0));
		}
		// else if (searchMethod == RTree.SEARCH_CONTAINMENT) {
		// return containmentRectPortion(h, new IRect[0],
		// this.file.readNode(0));
		// }
		else {
			throw new IllegalArgumentException("검색 조건을 알 수 없습니다.");
		}
	}


	// MBR이 incRect에 교차하면서 excRect에 교차하지 않는 데이터 반환
	public int searchRectPortion(IRect incRect, IRect[] excRect,
			int searchMethod, byte[] data, ByteBuffer buffer) throws Exception {
		if (searchMethod == RTree.SEARCH_INTERSECTION) {
			return intersectionRectPortion(incRect, excRect,
					this.file.readNode(0), data, buffer);
		}
		/*
		 * else if (searchMethod == RTree.SEARCH_CONTAINMENT) { return
		 * containmentRectPortion(incRect, excRect, this.file.readNode(0)); }
		 */
		else {
			throw new IllegalArgumentException("검색 조건을 알 수 없습니다.");
		}
	}

	public Vector searchRectPortion(IRect incRect, IRect[] excRect,
			int searchMethod) throws Exception {
		if (searchMethod == RTree.SEARCH_INTERSECTION) {
			return intersectionRectPortion(incRect, excRect,
					this.file.readNode(0));
		}

		else if (searchMethod == RTree.SEARCH_CONTAINMENT) {
			return containmentRectPortion(incRect, excRect,
					this.file.readNode(0));
		}

		else {
			throw new IllegalArgumentException("검색 조건을 알 수 없습니다.");
		}
	}

	private Vector intersectionIShape(IShape incRect,
			IShape[] excRect, AbstractNode root) throws Exception {
		if (incRect == null || excRect == null || root == null) {
			String objName = "";
			if (incRect == null) {
				objName = "incRect";
			} else if (excRect == null) {
				objName = "excRect";
			} else {
				objName = "root";
			}
			throw new IllegalArgumentException("함수 인수가 Null입니다.");
		}

		Vector v = new Vector();

		boolean isExcluded = false;

		if (incRect.intersects(root.getNodeMBR())) {
			// 모든 Child Node에 대해
			for (int i = 0; i < root.childCount; i++) {
				// incRect와 교차하고
				if (incRect.intersects(root.childMBR[i])) {
					// excRect와 교차하지 않는 Child Node를
					isExcluded = false;
					if (root.isLeaf()) {
						for (int j = 0; j < excRect.length; j++) {
							if (excRect[j].intersects(root.childMBR[i])) {
								isExcluded = true;
								break;
							}
						}
					} else {
						for (int j = 0; j < excRect.length; j++) {
							if (excRect[j].contains(root.childMBR[i])) {
								isExcluded = true;
								break;
							}
						}
					}

					if (!isExcluded) {
						// Leaf Node라면
						// Vector에 추가한다.
						if (root.isLeaf()) {
							v.add(new Integer(root.childPageID[i]));
							// v.addElement(new String("" +
							// root.childPageID[i]));
						}
						// Index Node라면
						// Recursion으로 교차여부를 검사한다.
						else {
							Vector a = intersectionIShape(incRect, excRect,((IndexNode) root).getChild(i));
							Iterator<Integer> it = a.iterator();
							while (it.hasNext()) {
								v.add(it.next());
							}
						}
					}
				}
			}
		}
		return v;
	}
	
	private Vector<ResultNode> intersectionIShape(IShape incRect,
			IShape[] excRect, AbstractNode root, IPoint pt) throws Exception {
		if (incRect == null || excRect == null || root == null) {
			String objName = "";
			if (incRect == null) {
				objName = "incRect";
			} else if (excRect == null) {
				objName = "excRect";
			} else {
				objName = "root";
			}
			throw new IllegalArgumentException("함수 인수가 Null입니다.");
		}
		
		double x = incRect.getCenter().x;
		double y = incRect.getCenter().y;
		
		if(pt != null){
			x = pt.x;
			y = pt.y;
		}
		
		//SortedLinkedHashSet<Integer> v = new SortedLinkedHashSet<Integer>();
		Vector<ResultNode> v = new Vector();

		boolean isExcluded = false;

		if (incRect.intersects(root.getNodeMBR())) {
			// 모든 Child Node에 대해
			for (int i = 0; i < root.childCount; i++) {
				// incRect와 교차하고
				if (incRect.intersects(root.childMBR[i])) {
					// excRect와 교차하지 않는 Child Node를
					isExcluded = false;
					if (root.isLeaf()) {
						for (int j = 0; j < excRect.length; j++) {
							if (excRect[j].intersects(root.childMBR[i])) {
								isExcluded = true;
								break;
							}
						}
					} else {
						for (int j = 0; j < excRect.length; j++) {
							if (excRect[j].contains(root.childMBR[i])) {
								isExcluded = true;
								break;
							}
						}
					}

					if (!isExcluded) {
						// Leaf Node라면
						// Vector에 추가한다.
						if (root.isLeaf()) {
							ResultNode rn = new ResultNode();
							rn.pageId = root.childPageID[i];
							
							
							double xx = (root.childMBR[i].x1+root.childMBR[i].x2)/2;
							double yy = (root.childMBR[i].y1+root.childMBR[i].y2)/2;
							


							rn.distance = Math.sqrt((xx-x) * (xx-x) + (yy-y) * (yy-y));
							v.add(rn);
							
							//v.add(new Integer(root.childPageID[i]));
							// v.addElement(new String("" +
							// root.childPageID[i]));
						}
						// Index Node라면
						// Recursion으로 교차여부를 검사한다.
						else {
							Vector<ResultNode> a = intersectionIShape(incRect, excRect, ((IndexNode) root).getChild(i), pt);
							for (int j = 0; j < a.size(); j++) {
								//v.addElement(a.elementAt(j));
								v.add(a.elementAt(j));
							}
//							SortedLinkedHashSet<Integer> a = intersectionIShape(incRect, excRect,((IndexNode) root).getChild(i));
//							Iterator<Integer> it = a.iterator();
//							while (it.hasNext()) {
//								v.add(it.next());
//							}
						}
					}
				}
			}
		}
		return v;
	}


	private Vector intersectionRectPortion(IRect incRect, IRect[] excRect, AbstractNode root) throws Exception {
		if (incRect == null || excRect == null || root == null) {
			String objName = "";
			if (incRect == null) {
				objName = "incRect";
			} else if (excRect == null) {
				objName = "excRect";
			} else {
				objName = "root";
			}
			throw new IllegalArgumentException("함수 인수가 Null입니다.");
		}

		Vector v = new Vector();

		boolean isExcluded = false;

		if (incRect.intersects(root.getNodeMBR())) {
			// 모든 Child Node에 대해
			for (int i = 0; i < root.childCount; i++) {
				// incRect와 교차하고
				if (incRect.intersects(root.childMBR[i])) {
					// excRect와 교차하지 않는 Child Node를
					isExcluded = false;
					if (root.isLeaf()) {
						for (int j = 0; j < excRect.length; j++) {
							if (excRect[j].intersects(root.childMBR[i])) {
								isExcluded = true;
								break;
							}
						}
					} else {
						for (int j = 0; j < excRect.length; j++) {
							if (excRect[j].contains(root.childMBR[i])) {
								isExcluded = true;
								break;
							}
						}
					}

					if (!isExcluded) {
						// Leaf Node라면
						// Vector에 추가한다.
						if (root.isLeaf()) {
							
							v.addElement(new Integer(root.childPageID[i]));
							
							// v.addElement(new String("" +
							// root.childPageID[i]));
						}
						// Index Node라면
						// Recursion으로 교차여부를 검사한다.
						else {
							Vector a = intersectionRectPortion(incRect, excRect, ((IndexNode) root).getChild(i));
							for (int j = 0; j < a.size(); j++) {
								v.addElement(a.elementAt(j));
							}
						}
					}
				}
			}
		}
		return v;
	}
	
	private Vector<ResultNode> intersectionRectPortion2(IRect incRect, IRect[] excRect, AbstractNode root, IPoint pt) throws Exception {
		if (incRect == null || excRect == null || root == null) {
			String objName = "";
			if (incRect == null) {
				objName = "incRect";
			} else if (excRect == null) {
				objName = "excRect";
			} else {
				objName = "root";
			}
			throw new IllegalArgumentException("함수 인수가 Null입니다.");
		}

		double x = (incRect.x1+incRect.x2)/2;
		double y = (incRect.y1+incRect.y2)/2;

		if(pt != null){
			x = pt.x;
			y = pt.y;
		}
		
		Vector<ResultNode> v = new Vector();

		boolean isExcluded = false;

		if (incRect.intersects(root.getNodeMBR())) {
			// 모든 Child Node에 대해
			for (int i = 0; i < root.childCount; i++) {
				// incRect와 교차하고
				if (incRect.intersects(root.childMBR[i])) {
					// excRect와 교차하지 않는 Child Node를
					isExcluded = false;
					if (root.isLeaf()) {
						for (int j = 0; j < excRect.length; j++) {
							if (excRect[j].intersects(root.childMBR[i])) {
								isExcluded = true;
								break;
							}
						}
					} else {
						for (int j = 0; j < excRect.length; j++) {
							if (excRect[j].contains(root.childMBR[i])) {
								isExcluded = true;
								break;
							}
						}
					}

					if (!isExcluded) {
						// Leaf Node라면
						// Vector에 추가한다.
						if (root.isLeaf()) {
							
							ResultNode rn = new ResultNode();
							rn.pageId = root.childPageID[i];
							
							
							double xx = (root.childMBR[i].x1+root.childMBR[i].x2)/2;
							double yy = (root.childMBR[i].y1+root.childMBR[i].y2)/2;
							


							rn.distance = Math.sqrt((xx-x) * (xx-x) + (yy-y) * (yy-y));
							v.add(rn);
							
							// v.addElement(new String("" +
							// root.childPageID[i]));
						}
						// Index Node라면
						// Recursion으로 교차여부를 검사한다.
						else {
							Vector<ResultNode> a = intersectionRectPortion2(incRect, excRect, ((IndexNode) root).getChild(i), pt);
							for (int j = 0; j < a.size(); j++) {
								//v.addElement(a.elementAt(j));
								v.add(a.elementAt(j));
							}
						}
					}
				}
			}
		}
		
		
		return v;
	}

	public int searchRect(IRect h, int searchMethod, byte[] data,
			ByteBuffer buffer) throws Exception {
		if (searchMethod == RTree.SEARCH_INTERSECTION) {
			return intersectionRectPortion(h, new IRect[0],
					this.file.readNode(0), data, buffer);
		} else {
			throw new IllegalArgumentException("검색 조건을 알 수 없습니다.");
		}
	}

	private int intersectionRectPortion(IRect incRect, IRect[] excRect,
			AbstractNode root, byte[] data, ByteBuffer buffer) throws Exception {
		if (incRect == null || excRect == null || root == null) {
			throw new IllegalArgumentException("함수 인수가 Null입니다.");
		}
		// logger.debug("intersectionRectPortion=");
		// Thread.sleep(10);
		int resultCnt = 0;
		boolean isExcluded = false;
		if (incRect.intersects(root.getNodeMBR())) {
			// 모든 Child Node에 대해
			for (int i = 0; i < root.childCount; i++) {
				// incRect와 교차하고
				if (incRect.intersects(root.childMBR[i])) {
					// excRect와 교차하지 않는 Child Node를
					isExcluded = false;
					if (root.isLeaf()) {
						for (int j = 0; j < excRect.length; j++) {
							if (excRect[j].intersects(root.childMBR[i])) {
								isExcluded = true;
								break;
							}
						}
					} else {
						for (int j = 0; j < excRect.length; j++) {
							if (excRect[j].contains(root.childMBR[i])) {
								isExcluded = true;
								break;
							}
						}
					}

					if (!isExcluded) {
						// Leaf Node라면
						// Vector에 추가한다.
						if (root.isLeaf()) {
							resultCnt++;
							int offset = root.childPageID[i];
							int shp_size = byte2int_new(data, offset);
							int att_size = byte2int_new(data, offset + 2);
							// int objid = byte2int(data, offset + 4);
							// byte subid = data[offset + 4 + size - 1];
							// logger.debug("objid : " + objid + " : " + subid);
							// logger.debug("sss: " + size);
							buffer.putInt(shp_size + att_size);
							buffer.put(data, offset + 4, shp_size + att_size);
							// logger.debug("page id="+offset);
						}
						// Index Node라면
						// Recursion으로 교차여부를 검사한다.
						else {
							int size = intersectionRectPortion(incRect,
									excRect, ((IndexNode) root).getChild(i),
									data, buffer);
							resultCnt += size;
						}
					}
				}
			}
		}
		return resultCnt;
	}

	public int byte2int(byte[] b, int pos) {
		return (((b[pos] & 0xFF) << 24) + ((b[pos + 1] & 0xFF) << 16)
				+ ((b[pos + 2] & 0xFF) << 8) + (b[pos + 3] & 0xFF));
	}

	public int byte2int_new(byte[] b, int pos) {
		return (((b[pos] & 0xFF) << 8) + ((b[pos + 1] & 0xFF)));
	}

	private Vector containmentRectPortion(IRect incRect, IRect[] excRect,
			AbstractNode root) throws Exception {
		if (incRect == null || root == null) {
			throw new IllegalArgumentException("함수 인수가 Null입니다.");
		}

		Vector v = new Vector();
		boolean isExcluded = false;

		if (incRect.contains(root.getNodeMBR())) {
			for (int i = 0; i < root.childCount; i++) {
				if (incRect.contains(root.childMBR[i])) {
					isExcluded = false;
					for (int j = 0; j < excRect.length; j++) {
						if (excRect[j].contains(root.childMBR[i])) {
							isExcluded = true;
							break;
						}
					}
					if (!isExcluded) {
						if (root.isLeaf()) {
							v.addElement(new Integer(root.childPageID[i]));
							// v.addElement(new String("" +
							// root.childPageID[i]));
						} else {
							Vector a = containmentRectPortion(incRect, excRect,
									((IndexNode) root).getChild(i));
							for (int j = 0; j < a.size(); j++) {
								v.addElement(a.elementAt(j));
							}
						}
					}
				}
			}
		}
		return v;
	}
	
	

	public SortedLinkedHashSet<Integer> searchList(int cnt, int pageNo) {
		return this.file.searchList(cnt, pageNo);
	}

	public int objCnt() {
		return this.file.objSize;
	}

}
