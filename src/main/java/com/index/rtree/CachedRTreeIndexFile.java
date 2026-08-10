package com.index.rtree;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;


public class CachedRTreeIndexFile extends IndexFile {
	  // 인덱스 파일 명칭
	  private String fileName = null;

	  // 인덱스를 메모리로 올리기 위한 Hashtable
	  private Hashtable memFile = null;

	  // 페이지 갯수
	  private int pageCount = -1;

	  public static final int EMPTY_PAGE = -2;

	  public static final float SAFETY_FACTOR = 1.1f;
	  public static final float LOAD_FACTOR = 0.75f;

	  public CachedRTreeIndexFile(String fileName) {
	    this.fileName = fileName;
	  }

	  // R-Tree와 연동된 다음, 파일의 내용을 메모리로 불러온다.
	  public  void diskToMemory() {
	    try {
	      // RTree 인덱스 파일
	      RandomAccessFile file = new RandomAccessFile(fileName, "r");
	      file.seek(0);
	      // 파일 헤더를 읽어온다.
	      byte[] header = new byte[headerSize];
	      if (headerSize == file.read(header)) {
	        DataInputStream ds = new DataInputStream(new ByteArrayInputStream(header));
	        fillFactor = ds.readFloat();
	        nodeCapacity = ds.readInt();
	        pageSize = ds.readInt();
	      }

	      // nodeLength를 통해 Hashtable 크기를 결정한다.
	      // Hashtable은 크기가 initialCapacity * loadFactor 가 되면 재해쉬가 일어나므로,,
	      // 재해쉬를 막기 위해 initialCapacity와 loadFactor를 정하여 생성한다.
	      int fileSize = (int) file.length();
	      pageCount = (fileSize - headerSize) / pageSize;
	      int tableCapacity = (int) ( (pageCount / LOAD_FACTOR) * SAFETY_FACTOR);
	      if (tableCapacity < 11) {
	        tableCapacity = 11;
	      }

	      // Hashtable 생성
	      memFile = new Hashtable(tableCapacity, LOAD_FACTOR);

	      // Node를 메모리로 로드한다.
	      int i = 0;
	      try {
	        // 최초 위치로 이동
	        file.seek(headerSize + i * pageSize);
	        // 페이지 단위 버퍼
	        byte[] b = new byte[pageSize];
	        int l = -1;

	        while (i < pageCount) {
	          l = file.read(b);

	          if ( -1 == l) {
	            throw new IOException("인덱스 파일이 잘못되었습니다.");
	          }

	          DataInputStream ds = new DataInputStream(new ByteArrayInputStream(b));

	          // 노드를 읽어온다.
	          int parent = ds.readInt();

	          if (parent != EMPTY_PAGE) {
	            int level = ds.readInt();
	            int childCnt = ds.readInt();

	            AbstractNode n;
	            if (level != 0) {
	              n = new IndexNode(tree, parent, i, level);
	            }
	            else {
	              n = new LeafNode(tree, parent, i);
	            }

	            n.setChildCount(childCnt);

	            int x1, y1, x2, y2;

	            for (int j = 0; j < childCnt; j++) {
	              x1 = ds.readInt();
	              y1 = ds.readInt();
	              x2 = ds.readInt();
	              y2 = ds.readInt();
	              n.childMBR[j] = new IRect(x1, y1, x2, y2);

	              n.childPageID[j] = ds.readInt();
	            }

	            // 노드를 Hashtable에 추가한다.
	            memFile.put(new Integer(i), n);
//	            memFile.put(new String("" + i), n);
	          }
	          i++;
	          file.seek(headerSize + i * pageSize);
	        }
	      }
	      catch (IOException e) {
	        e.printStackTrace();
	        System.exit( -1);
	      }
	      finally {
	        file.close();
	      }
	    }
	    catch (Exception e) {
	      e.printStackTrace();
	    }
	  }

	  public  void initialize(RTree tree, float fillFactor, int capacity) {
	    // 호출되지 않는다.
	    throw new IllegalStateException("Initialize 함수를 호출할 수 없습니다.");
	  }

	  public  AbstractNode readNode(int page) throws PageFaultError {
	    if (page < 0) {
	      throw new IllegalArgumentException("페이지는 음수가 될 수 없습니다.");
	    }

	    AbstractNode ret = (AbstractNode) memFile.get(new Integer(page));
//	    AbstractNode ret = (AbstractNode) memFile.get(new String("" + page));

	    if (ret == null) {
	      throw new PageFaultError("잘못된 페이지 요청입니다.");
	    }

	    return ret;
	  }

	  public  int writeNode(AbstractNode n) throws PageFaultError {
	    // 호출되지 않는다.
	    throw new IllegalStateException("writeNode 함수를 호출할 수 없습니다.");
	  }

	  public  AbstractNode deletePage(int page) throws PageFaultError {
	    // 호출되지 않는다.
	    throw new IllegalStateException("deletePage 함수를 호출할 수 없습니다.");
	  }

	@Override
	public SortedLinkedHashSet<Integer> searchList(int cnt, int pageNo) {
		// TODO Auto-generated method stub
		return null;
	}
	}
