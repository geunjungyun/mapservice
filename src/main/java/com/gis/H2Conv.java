package com.gis;

import java.io.File;
import java.util.Iterator;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.rtree.MVRTreeMap;
import org.h2.mvstore.rtree.SpatialKey;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.opengis.feature.simple.SimpleFeature;

import com.data.file.FileLayer;

public class H2Conv {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		WKBReader wkbReader = new WKBReader();
		WKBWriter wkbWriter = new WKBWriter();
		
		String path = args[0];
		
		//MVStore s = MVStore.open(null);
		
		MVStore db = new MVStore.Builder().fileName(args[1]).autoCompactFillRate(0).//->데이터정합성을 위한 필수 설정
	    open();
		
		
		
		MVRTreeMap<String> r = db.openMap("rtree", new MVRTreeMap.Builder<String>());
		
		MVMap<String, Object> b = db.openMap("btree");
		
		File pathF = new File(path);
		
		File[] listF = pathF.listFiles();
		
		int idx = 0;
		long st = System.currentTimeMillis();
		for(File file : listF) {
			if(file.getName().startsWith("jijuk")) {
				try {
					System.out.println(file.getAbsolutePath());
					FileLayer fl = new FileLayer(file.getAbsolutePath(), null);
					
					Iterator  it  = fl.iterator();
					while(it.hasNext()) {
						SimpleFeature sf = (SimpleFeature) it.next();
						
						String pnu = (String) sf.getAttribute("pnu");
						
						Geometry geo = (Geometry) sf.getDefaultGeometry();
						
						Envelope env = geo.getEnvelopeInternal();
						
						//byte[] data = wkbWriter.write(geo);
						
						r.add(new SpatialKey(idx++, (float)env.getMinX(), (float)env.getMaxX(), 
								(float)env.getMinY(), (float)env.getMaxY()), idx+"");
						b.put(pnu, idx+"");
						if(idx % 100000 == 0) {
							System.out.println("idx="+idx+", time = " + (System.currentTimeMillis() - st)/1000/60);
						}
					}
					fl.close();
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		db.commit();
		db.close();
		
	}

}
