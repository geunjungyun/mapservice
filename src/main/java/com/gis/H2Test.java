package com.gis;

import java.util.Iterator;

import org.h2.mvstore.MVStore;
import org.h2.mvstore.rtree.MVRTreeMap;
import org.h2.mvstore.rtree.SpatialKey;

public class H2Test {
	public static void main(String[] args) {
		H2Test test = new H2Test();

		test.test1();
		System.out.println();
		test.test2();
	}
	
	
	public void test1() {
		MVStore db = new MVStore.Builder().fileName("c://test").autoCompactFillRate(0).//->데이터정합성을 위한 필수 설정
			    open();
		MVRTreeMap<String> r = db.openMap("rtree", new MVRTreeMap.Builder<String>());
		
		//long idx = 0;
		r.add(new SpatialKey(1, (float)0, (float)10,(float)0, (float)10), "aaa");
		r.add(new SpatialKey(2, (float)10, (float)20,(float)0, (float)10), "bbb");
		r.add(new SpatialKey(3, (float)20, (float)30,(float)0, (float)10), "ccc");
		r.add(new SpatialKey(4, (float)40, (float)50,(float)0, (float)10), "ddd");
		
		Iterator<SpatialKey> it = r.findIntersectingKeys(new SpatialKey(0, 5, 5, 5, 5));
		for (SpatialKey k; it.hasNext();) {
			k = it.next();
			System.out.println("search id="+k.getId() + ": value=" + r.get(k));
		}
		
		it = r.findIntersectingKeys(new SpatialKey(0, 15, 15, 5, 5));
		for (SpatialKey k; it.hasNext();) {
			k = it.next();
			System.out.println("search id="+k.getId() + ": value=" + r.get(k));
		}
		
		String result = r.remove(new SpatialKey(2));
		
		System.out.println("remove value="+result);
		
		it = r.findIntersectingKeys(new SpatialKey(0, 15, 15, 5, 5));
		for (SpatialKey k; it.hasNext();) {
			k = it.next();
			System.out.println("search id="+k.getId() + ": value=" + r.get(k));
		}
	}
	
	public void test2() {
		MVStore db = new MVStore.Builder().fileName("c://test2").autoCompactFillRate(0).//->데이터정합성을 위한 필수 설정
			    open();
		MVRTreeMap<String> r = db.openMap("rtree", new MVRTreeMap.Builder<String>());
		
		MVRTreeMap<String> r1 = db.openMap("rtree1", new MVRTreeMap.Builder<String>());
		
		//long idx = 0;
		r.add(new SpatialKey(1, (float)0, (float)10,(float)0, (float)10), "aaa");
		r.add(new SpatialKey(2, (float)10, (float)20,(float)0, (float)10), "bbb");
		r.add(new SpatialKey(3, (float)20, (float)30,(float)0, (float)10), "ccc");
		r.add(new SpatialKey(4, (float)40, (float)50,(float)0, (float)10), "ddd");
		
		r1.add(new SpatialKey(1, (float)0, (float)10,(float)0, (float)10), "aaa");
		r1.add(new SpatialKey(2, (float)10, (float)20,(float)0, (float)10), "bbb");
		r1.add(new SpatialKey(3, (float)20, (float)30,(float)0, (float)10), "ccc");
		r1.add(new SpatialKey(4, (float)40, (float)50,(float)0, (float)10), "ddd");

		
		Iterator<SpatialKey> it = r.findIntersectingKeys(new SpatialKey(0, 5, 5, 5, 5));
		for (SpatialKey k; it.hasNext();) {
			k = it.next();
			System.out.println("search id="+k.getId() + ": value=" + r.get(k));
		}
		
		it = r.findIntersectingKeys(new SpatialKey(0, 15, 15, 5, 5));
		for (SpatialKey k; it.hasNext();) {
			k = it.next();
			System.out.println("search id="+k.getId() + ": value=" + r.get(k));
		}
		
		String result = r.remove(new SpatialKey(2));
		
		System.out.println("remove value="+result);
		
		it = r.findIntersectingKeys(new SpatialKey(0, 15, 15, 5, 5));
		for (SpatialKey k; it.hasNext();) {
			k = it.next();
			System.out.println("search id="+k.getId() + ": value=" + r.get(k));
		}
		
		
		it = r1.findIntersectingKeys(new SpatialKey(0, 5, 5, 5, 5));
		for (SpatialKey k; it.hasNext();) {
			k = it.next();
			System.out.println("search id="+k.getId() + ": value=" + r1.get(k));
		}
		
		it = r1.findIntersectingKeys(new SpatialKey(0, 15, 15, 5, 5));
		for (SpatialKey k; it.hasNext();) {
			k = it.next();
			System.out.println("search id="+k.getId() + ": value=" + r1.get(k));
		}
		
		result = r1.remove(new SpatialKey(2));
		
		System.out.println("remove value="+result);
		
		it = r1.findIntersectingKeys(new SpatialKey(0, 15, 15, 5, 5));
		for (SpatialKey k; it.hasNext();) {
			k = it.next();
			System.out.println("search id="+k.getId() + ": value=" + r1.get(k));
		}

	}
}
