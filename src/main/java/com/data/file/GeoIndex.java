package com.data.file;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.rtree.MVRTreeMap;
import org.h2.mvstore.rtree.SpatialKey;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

/**
 * 지적은 rtree 만 생성
 * 
 * 구역은 코드 단위로 rtree 생성, 전체도 생성
 * 
 * @author Administrator
 *
 */
public class GeoIndex {

	// private IndexFile rtreePageFile = null;
	private MVStore db = null;

	//this.rtree = db.openMap("rtree", new MVRTreeMap.Builder<String>());
	// 인덱스 저장시 <String>의 값도 저장할수 있다.
	MVRTreeMap<String> rtree = null;
	//
	MVMap<String, Long> btree = null;

	/*
	 * 검색 로직에서 동기화 문제 발생
	 */
	
	//boolean bIndex = false;
	boolean codeIndex = false;

	private String path = null;
	
	
	HashMap<String, MVRTreeMap<String>> subRtrees = new HashMap();
	
	Object obj = new Object();

	/**
	 * 
	 * @param _path     저장 패스
	 * @param newcreate 생성 : true, 읽기 : false
	 * @throws Exception
	 */
	public void createIndex(String _path, boolean newcreate, boolean memoryMode) throws Exception {

		File file = new File(_path + FileName.DS + FileName.indexGeoPath);
		if (!file.exists()) {
			file.mkdirs();
		}

		this.path = file.getAbsolutePath() + FileName.DS + FileName.rtreeindexFileName;

		if (newcreate) {
			File indexFile = new File(this.path);
			if (indexFile.exists()) {
				indexFile.delete();
			}
			this.db = new MVStore.Builder().fileName(this.path).autoCompactFillRate(0).open();
		}
		else {
			//this.db = new MVStore.Builder().fileName(this.path).autoCompactFillRate(0).open();
			this.db = new MVStore.Builder().fileName(this.path).readOnly().autoCompactFillRate(0).open();
		}
		//this.db = new MVStore.Builder().fileName(this.path).autoCompactFillRate(0).open();
		//this.db = new MVStore.Builder().fileName(this.path).readOnly().autoCompactFillRate(0).open();
		
		this.rtree = db.openMap("rtree", new MVRTreeMap.Builder<String>());

	}

	public void write(Geometry geo, double page, String code, boolean isCodeIndex) throws Exception {
		Envelope env = geo.getEnvelopeInternal();
		synchronized (this.obj) {
			
			this.rtree.add(new SpatialKey((long) page, (float) env.getMinX(), (float) env.getMaxX(),
					(float) env.getMinY(), (float) env.getMaxY()), code);
			
			if(isCodeIndex) {
				
				MVRTreeMap<String> subRtree = this.getSubRtree(code);
				subRtree.add(new SpatialKey((long) page, (float) env.getMinX(), (float) env.getMaxX(),
					(float) env.getMinY(), (float) env.getMaxY()), code);
			}
			/*
			else {
				if(this.btree == null) {
					this.btree = db.openMap("btree");
				}
				
				this.btree.put(code, (long)page);
			}
			*/
		}
	}
	
	public long read1(String pnu) {
		if(this.btree == null) {
			this.btree = db.openMap("btree");
		}
		
		long value = this.btree.get(pnu);
		return value;
	}
	
	
	public MVRTreeMap<String> getSubRtree(String code){
		
		MVRTreeMap<String> value = null;
		
		if(!this.subRtrees.containsKey(code)) {
			MVRTreeMap<String> subRtree = db.openMap(code, new MVRTreeMap.Builder<String>());
			this.subRtrees.put(code, subRtree);
		}
		
		value = this.subRtrees.get(code);
		
		return value;
		
//		MVRTreeMap<String> subRtree = db.openMap(code, new MVRTreeMap.Builder<String>());
//		result = subRtree.remove(new SpatialKey(pageId));
	}
	

	public boolean remove(Geometry geo, long pageId, String code) throws Exception {

		Envelope env = geo.getEnvelopeInternal();

		String result = this.rtree.remove(new SpatialKey(pageId));
		
		if(this.codeIndex) {
			
			MVRTreeMap<String> subRtree = this.getSubRtree(code);
			result = subRtree.remove(new SpatialKey(pageId));
			
		}
		

		if (result == null) {
			FileLayer.LOG.error("공간 인덱스가 존재 하지 않습니다. pageid=" + pageId);
			return false;
		}
		return true;
	}

	public Vector read(double minx, double miny, double maxx, double maxy) throws Exception {

		Vector result = new Vector();

		Iterator<SpatialKey> it = rtree.findIntersectingKeys(new SpatialKey(0, (float) minx, (float) maxx, (float) miny, (float) maxy));
		
		
	
		for (SpatialKey k; it.hasNext();) {

			if (result == null) {
				result = new Vector();
			}

			k = it.next();
			result.add(k.getId());
		}
		
		return result;
	}
	
	
	/**
	 * 공간 인덱스 파일 생성시 저장된 코드 값을 제공한다. 
	 * 조건 검색으로 공간데이타를 읽는 것이 아니고 저장된 코드 값을 리턴한다. 
	 * @param minx
	 * @param miny
	 * @param maxx
	 * @param maxy
	 * @return
	 * @throws Exception
	 */
	public Vector readCode(double minx, double miny, double maxx, double maxy) throws Exception {

		Vector result = new Vector();

		Iterator<SpatialKey> it = rtree.findIntersectingKeys(new SpatialKey(0, (float) minx, (float) maxx, (float) miny, (float) maxy));
		
		for (SpatialKey k; it.hasNext();) {

			if (result == null) {
				result = new Vector();
			}

			k = it.next();
			
			String code = rtree.get(k);
			
			result.add(code);
		}
		
		return result;
	}
	
	public Vector read(double minx, double miny, double maxx, double maxy, String code) throws Exception {

		Vector result = null;

		Iterator<SpatialKey> it = null;
		
		MVRTreeMap<String> subRtree = this.getSubRtree(code);
		it = subRtree.findIntersectingKeys(new SpatialKey(0, (float) minx, (float) maxx, (float) miny, (float) maxy));
		
		for (SpatialKey k; it.hasNext();) {

			if (result == null) {
				result = new Vector();
			}

			k = it.next();
			result.add(k.getId());
		}
		
		return result;
	}
	
	public void close() throws Throwable {
		
		this.db.commit();
		this.db.close();
	}

}
