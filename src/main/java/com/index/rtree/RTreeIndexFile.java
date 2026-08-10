package com.index.rtree;

import java.io.*;
import java.util.*;

//import org.apache.log4j.Logger;

import com.data.util.MapLog;
import com.index.rtree.IndexFile;




public class RTreeIndexFile extends IndexFile {

  //static Logger logger = MapLog.getSCLog();

  private RandomAccessFile file;
  private Stack emptyPages = new Stack();
  //private int objSize = 0;
  
  // ByteBuffer2지원
//  public RTreeIndexFile(ByteBuffer2 file, boolean createNewFile) {
//	  if(file instanceof RAFByteBuffer2) {
//		  this.file = (RandomAccessFile) file.getObject();
//		  init(createNewFile);
//	  }
//	  else
//		  throw new IllegalArgumentException("isn't RAFByteBuffer2");
//  }
  
  private void init(boolean createNewFile) {
	    try {
	   
	      if (createNewFile == false) {
	        file.seek(0);
	        byte[] header = new byte[headerSize];
	        if (headerSize == file.read(header)) {
	          DataInputStream ds = new DataInputStream(new ByteArrayInputStream(header));
	          fillFactor = ds.readFloat();
	          nodeCapacity = ds.readInt();
	          pageSize = ds.readInt();

	          int i = 0;
	          try {

	        	//비어있는 데이타를 체크한다
            while (true) {
              if (EMPTY_PAGE == file.readInt()) {
                emptyPages.push(new Integer(i));
//	                  emptyPages.push(new String("" + i));
              }
              
              int nodeKind = file.readInt();
              int childCnt = file.readInt();
              if(nodeKind == 0){
            	  this.objSize+=childCnt;
              }
              
              i++;
              file.seek(headerSize + i * pageSize);
            }
            
          }
          catch (IOException e) {
        	  e.printStackTrace();
          }
          //System.out.println("leaf Size="+objSize);
        }
      }
    }
    catch (Exception e) {
      //logger.error(e);
    	e.printStackTrace();
    }
  }

  public RTreeIndexFile(String fileName, boolean createNewFile) {
	  try {
		//System.out.println("RTreeIndexFile=" + fileName);
		file = new RandomAccessFile(fileName, "rw");
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	  init(createNewFile);
    
  }

  public  void initialize(RTree tree, float fillFactor, int capacity) {
    super.initialize(tree, fillFactor, capacity);
    emptyPages.clear();

    try {
      file.setLength(0);
      file.seek(0);
      file.writeFloat(fillFactor);
      file.writeInt(nodeCapacity);
      file.writeInt(pageSize);
    }
    catch (IOException e) {
      //logger.error(e);
    	e.printStackTrace();
    }
  }

  public  void finalize() throws Throwable {
    try {
      file.close();
    }
    catch (Exception e) {
      //logger.error(e);
    	e.printStackTrace();
    }

    super.finalize();
  }

  public  AbstractNode readNode(int page) throws PageFaultError {
    if (page < 0) {
      throw new IllegalArgumentException("페이지는 음수가 될 수 없습니다.");
    }

    try {
      file.seek(headerSize + page * pageSize);

      byte[] b = new byte[pageSize];
      int l = file.read(b);
      if ( -1 == l) {
        throw new PageFaultError(page + " 페이지를 읽을 수 없습니다. (EOF)");
      }

      DataInputStream ds = new DataInputStream(new ByteArrayInputStream(b));

      int parent = ds.readInt();
      if (parent == EMPTY_PAGE) {
        throw new PageFaultError(page + " 페이지를 읽을 수 없습니다. (EMPTY)");
      }

      int level = ds.readInt();
      int childCnt = ds.readInt();

      AbstractNode n;
      if (level != 0) {
        n = new IndexNode(tree, parent, page, level);
      }
      else {
        n = new LeafNode(tree, parent, page);
      }

      n.setChildCount(childCnt);

      int x1, y1, x2, y2;

      for (int i = 0; i < childCnt; i++) {
        x1 = ds.readInt();
        y1 = ds.readInt();
        x2 = ds.readInt();
        y2 = ds.readInt();
        n.childMBR[i] = new IRect(x1, y1, x2, y2);

        n.childPageID[i] = ds.readInt();
      }
      return n;
    }
    catch (IOException e) {
    	e.printStackTrace();
      //logger.error(e);
      return null;
    }
  }

  public  int writeNode(AbstractNode n) throws PageFaultError {
    if (n == null) {
      throw new IllegalArgumentException("노드가 널입니다.");
    }

    try {
      int page;
      if (n.getPageID() < 0) {
        if (emptyPages.empty()) {
          page = (int) ( (file.length() - headerSize) / pageSize);
        }
        else {
          page = ( (Integer) emptyPages.pop()).intValue();
        }
        n.setPageID(page);
      }
      else {
        page = n.getPageID();
      }

      ByteArrayOutputStream bs = new ByteArrayOutputStream(pageSize);
      DataOutputStream ds = new DataOutputStream(bs);
      ds.writeInt(n.getParentPageID());
      ds.writeInt(n.getLevel());
      ds.writeInt(n.getChildCount());

      for (int i = 0; i < tree.getNodeCapacity(); i++) {
        if (n.childMBR[i] == null) {
          ds.writeInt( -1);
          ds.writeInt( -1);
          ds.writeInt( -1);
          ds.writeInt( -1);
        }
        else {
          ds.writeInt(n.childMBR[i].x1);
          ds.writeInt(n.childMBR[i].y1);
          ds.writeInt(n.childMBR[i].x2);
          ds.writeInt(n.childMBR[i].y2);
        }
        ds.writeInt(n.childPageID[i]);
      }
      ds.flush();
      bs.flush();

      file.seek(headerSize + page * pageSize);
      file.write(bs.toByteArray());

      return page;
    }
    catch (IOException e) {
      //logger.error(e);
    	e.printStackTrace();
      return -1;
    }
  }

  public  AbstractNode deletePage(int page) throws PageFaultError {
    try {
      if (page < 0 || page > (file.length() - headerSize) / pageSize) {
        return null;
      }
      else {
        AbstractNode n = readNode(page);
        file.seek(headerSize + page * pageSize);
        file.writeInt(EMPTY_PAGE);
        emptyPages.push(new Integer(page));
//        emptyPages.push(new String("" + page));
        return n;
      }
    }
    catch (IOException e) {
      //logger.error(e);
    	e.printStackTrace();
      return null;
    }
  }
  
  public SortedLinkedHashSet<Integer> searchList(int cnt, int pageNo){
	  
	  SortedLinkedHashSet<Integer> data = new SortedLinkedHashSet<Integer>();
	  
		int startIdx = cnt*(pageNo-1);
		
		int endIdx = 0;
		if(pageNo > (int) this.objSize/cnt){
			endIdx = this.objSize% cnt+startIdx;
		}
		else{
			endIdx = (startIdx+cnt);
		}
		
		int reCnt = (endIdx -startIdx);
		
		try{
			int pos = 0;
			int pageIdx = 0;
			while(true){
				file.seek(headerSize + pageIdx * pageSize);
		        int parent = file.readInt();    
	            int nodeKind = file.readInt();
	            int childCnt = file.readInt();
	            if(EMPTY_PAGE != parent && nodeKind == 0){
	            	
	            	int tempPos = pos;
	            	pos += childCnt;
//	            	System.out.println("now="+tempPos+", add="+childCnt+",objsize="+data.size());
	            	if( data.size() < reCnt && pos > startIdx){
	            		int st = 0;
	            		if(data.size() == 0){
	            			st = startIdx-tempPos;
	            		}
	            		int et = childCnt;
	            		
	            		if( endIdx >= tempPos && endIdx <= pos){
	            			et =  childCnt - (pos - endIdx);
	            		}
//	            		System.out.println("st="+st+",et="+et);
	            		//int spos = pos - startIdx;
	            		//노드의 mbr을 건너뛴다.
	            		int ps = 5*4;
	            		//자식 노드 중에 시작 노드로 건너뛴다.
	            		file.seek(file.getFilePointer()+ps*(st));
	            		
	            		for(int i = st; i < et; i++){
	            			//file.seek(file.getFilePointer()+ps*(i)+4*4);
	            			
		            		//file.seek(file.getFilePointer()+ps*(st+i)+4*4);
	            			int minx = file.readInt();
	            			int miny = file.readInt();
	            			int maxx = file.readInt();
	            			int maxy = file.readInt();
	            			
		            		int pageNum = file.readInt();
		            		data.add(pageNum);
//		            		System.out.println("minx="+minx+",miny="+miny+",maxx="+maxx+",maxy="+maxy+",pagNum="+pageNum);
	            		}
	            		//mbr 값은 건너뛴다.
	            	}
	            	if(data.size() == (reCnt)){
	            		break;
	            	}
	            }
	            pageIdx++;
			}
		}
		catch(Exception e){
			
		}
		
//		for(int i=startIdx ; i< endIdx ; i++){
//			sdls.add(rsls.get(i));
//		}
	  
	  return data;
  }
  
  public static void main(String[] args){
//	  RTreeIndexFile rif = new RTreeIndexFile("E:/dawul/service/mgis/umd/poi.urx", false);
//	  
//	  SortedLinkedHashSet<Integer> result = rif.searchList(100, 4);
//	  System.out.println("result size="+result.size());
  }
  
  
}
