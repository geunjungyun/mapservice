package com.gis.map.render;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import com.index.rtree.IRect;
import com.index.rtree.RTree;

public class OverlapMng {
	
	
	//public final static int defaultExtensionArea = 10;
	//public final static int defaultExtensionArea = 0;
	
	public HashMap<String, Overlap> ols = new HashMap();
	
	public OverlapMng(){
		Overlap ol = new Overlap("main");
		this.ols.put(ol.id, ol);
	}
	
	public Overlap getOverlay(String id){
		Overlap ol = null;
		if(!this.ols.containsKey(id)){
			return null;
		}
		else{
			ol = this.ols.get(id);
		}
		return ol;
	}
	
	public void createOverlay(String id){
		Overlap ol = new Overlap(id);
		this.ols.put(id, ol);
	}
	
	public boolean chechOverlap(String id, Rectangle2D srcRT, int extensionArea) {
		Overlap ol = null;
		if(!this.ols.containsKey(id)){
			return false;
		}
		else{
			ol = this.ols.get(id);
		}
		
		return ol.chechOverlap(srcRT, extensionArea);
	}
	
	public boolean add(String id, Rectangle2D srcRT, int extensionArea){
		Overlap ol = null;
		if(!this.ols.containsKey(id)){
			return false;
		}
		else{
			ol = this.ols.get(id);
		}
		
		ol.add(srcRT, extensionArea);
		return true;
	}

	
	public class Overlap{
		
		int tempPageNo = 1;
		
		public Overlap(String _id){
			id = _id;
			//this.extensionArea = _extensionArea;
			try {
				this.overlaps = new RTree(RTree.FILL_FACTOR, RTree.CAPACITY);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public String id;
		
		//Vector<Rectangle> overlaps = new Vector();
		
		public RTree overlaps = null;
		
		
		//int extensionArea = defaultExtensionArea;
		
		public boolean chechOverlap(Rectangle2D srcRT, int extensionArea) {
			
			
			if(srcRT.getMinX() == srcRT.getMaxX() && srcRT.getMinY() == srcRT.getMaxY()) {
				return false;
			}
			
			IRect rect = new IRect();
			
			
			
//			rect.x1 = srcRT.x - extensionArea;
//			rect.y1 = srcRT.y - extensionArea;
//			rect.x2 = srcRT.x + srcRT.width + extensionArea;
//			rect.y2 = srcRT.y + srcRT.height + extensionArea;
			/*
			rect.x1 = (int)(srcRT.getMinX() - extensionArea);
			rect.y1 = (int)(srcRT.getMinY() - extensionArea);
			rect.x2 = (int)(srcRT.getMaxX() + extensionArea);
			rect.y2 = (int)(srcRT.getMaxY() + extensionArea);
			*/
			//2025.10.18 개선 코드
			rect.x1 = (int)Math.floor(srcRT.getMinX() - extensionArea);
			rect.y1 = (int)Math.floor(srcRT.getMinY() - extensionArea);
			rect.x2 = (int)Math.ceil(srcRT.getMaxX() + extensionArea);
			rect.y2 = (int)Math.ceil(srcRT.getMaxY() + extensionArea);			
			
			Vector result = null;
			try {
				result = this.overlaps.searchRect(rect, RTree.SEARCH_INTERSECTION);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(result != null && result.size() > 0){
				return true;
			}
			return false;
//			for (Rectangle dstRT : overlaps) {
//				if (dstRT.intersects(srcRT)) {
//					return true;
//				}
//			}
//
//			return false;
		}
		
		public void clear(){
			this.overlaps = null;
			try {
				this.overlaps = new RTree(RTree.FILL_FACTOR, RTree.CAPACITY);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public void add(Rectangle2D srcRT, int extensionArea){
			IRect rect = new IRect();
//			rect.x1 = srcRT.x - extensionArea;
//			rect.x2 = srcRT.x + srcRT.width + extensionArea;
//			
//			rect.y1 = srcRT.y - extensionArea;
//			rect.y2 = srcRT.y + srcRT.height + extensionArea;
			
			if(srcRT.getMinX() == srcRT.getMaxX() && srcRT.getMinY() == srcRT.getMaxY()) {
				return;
			}
			
			rect.x1 = (int)(srcRT.getMinX() - extensionArea);
			rect.y1 = (int)(srcRT.getMinY() - extensionArea);
			rect.x2 = (int)(srcRT.getMaxX() + extensionArea);
			rect.y2 = (int)(srcRT.getMaxY() + extensionArea);
			if(rect.x1 == -20 && rect.y1 == -20) {
				System.out.println();
			}
			try {
				this.overlaps.insertEntity(rect, tempPageNo++);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
//		public Vector<Rectangle> getExistRect(){
//			return overlaps;
//		}
	}
	
	public void clear(){
		Set set = this.ols.keySet();
		Iterator it = set.iterator();
		while(it.hasNext()){
			Object key = it.next();
			Overlap ol = this.ols.get(key);
			ol.clear();
		}
		this.ols.clear();
		Overlap ol = new Overlap("main");
		ols.put(ol.id, ol);
	}
}
