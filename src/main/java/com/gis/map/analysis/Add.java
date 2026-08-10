package com.gis.map.analysis;
import java.util.Vector;

import org.locationtech.jts.geom.Coordinate;

public class Add{
    	
    	public Add(){
    		
    	}
    	
    	Vector<Vector<Coordinate>> list = new Vector();
    	
    	Vector<Coordinate> now = null;
    	
    	public void add(Coordinate cd1, Coordinate cd2){
    		if(now == null) {
    			now = new Vector();
    			now.add(cd1);
    			now.add(cd2);
    		}
    		else {
    			Coordinate pre = now.get(now.size()-1);
    			if(pre.equals(cd1)) {
    				now.add(cd2);
    			}
    			else {
    				list.add(now);
    				now = new Vector();
    				now.add(cd1);
    				now.add(cd2);
    			}
    		}
    	}
    	
    	public Vector<Coordinate[]> getList(){
    		
    		if(now == null) {
    			return null;
    		}
    		
    		Vector<Coordinate[]> cds = new Vector();
    		
    		for(Vector<Coordinate> vd : list) {
    			Coordinate[] li = new Coordinate[vd.size()];
    			for(int i=0; i<vd.size(); i++) {
    				li[i] = vd.get(i);
    			}
    			cds.add(li);
    		}
    		
			Coordinate[] li = new Coordinate[now.size()];
			for(int i=0; i<now.size(); i++) {
				li[i] = now.get(i);
			}
			cds.add(li);
    		
    		
    		return cds;
    	}
    }
