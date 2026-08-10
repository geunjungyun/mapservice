package com.gis;

import java.util.Iterator;
import java.util.Random;

import org.h2.mvstore.MVStore;
import org.h2.mvstore.OffHeapStore;
import org.h2.mvstore.rtree.MVRTreeMap;
import org.h2.mvstore.rtree.SpatialKey;

public class H2Read {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
// x=953875.0, y=1951913.0,
		//OffHeapStore offHeap = new OffHeapStore();
		MVStore db = new MVStore.Builder().fileName(args[0]).autoCompactFillRate(0).// ->데이터정합성을 위한 필수 설정
				open();

		MVRTreeMap<byte[]> r = db.openMap("rtree", new MVRTreeMap.Builder<byte[]>());
		
		Random rd = new Random();
		
		float x = 953875.0f;
		float y = 1951913.0f;
		
		rd.nextInt(20000);
		for(int i=0; i < 10000 ;i++) {
			
			float xx = x + rd.nextInt(20000);
			float yy = y + rd.nextInt(20000);
			long st = System.currentTimeMillis();
			Iterator<SpatialKey> it = r.findIntersectingKeys(new SpatialKey(0, xx, xx, yy, yy));
			for (SpatialKey k; it.hasNext();) {
				k = it.next();
				System.out.println(k + ": " + r.get(k));
			}
			long et = System.currentTimeMillis();
			
			System.out.println("x="+xx+", y="+yy+", time="+(et-st)/1000.0);
		}
		db.commit();
		db.close();
	}

}
