package com.gis.map.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class Scale {
	
	protected int level = -1;
	protected double pixelPerMeter = -1.0;
	
	protected int maxLevel = Integer.MIN_VALUE;
	protected int minLevel  = Integer.MAX_VALUE;
	
	public HashMap scales = new HashMap();
	
	
	public Scale(){
		
	}
	
	public int getLikeLevel(double scale){
		
		double tempScale = Double.MAX_VALUE;
		int selectLevel = -1;
		
		Set<Integer> set = this.scales.keySet();
		Iterator<Integer> it = set.iterator();
		while(it.hasNext()){
			Integer key = it.next();
			Double ptm = (Double)this.scales.get(key);
			double tptm = Math.abs(ptm-scale);
			
			if( tptm < tempScale){
				selectLevel = key;
				tempScale = tptm;
			}
		}
		return selectLevel;
	}
	
	public int getMinLevel(){
		return this.minLevel;
	}
	
	public int getMaxLevel(){
		return this.maxLevel;
	}
	
	public int getLevel(){
		return this.level;
	}
	
	public double getScaleRatio(int level){
		if(this.scales.containsKey(level)) {
			return (Double)this.scales.get(level);	
		}
		else {
			return -1;
		}
	}
	
	protected void zoomIn(){
		level++;
		pixelPerMeter = (Double)this.scales.get(level);
	}
	
	protected void zoomOut(){
		level--;
		pixelPerMeter =(Double)this.scales.get(level);
	}
	
	public void addScale(int level, double scale){
		if(this.maxLevel < level){
			this.maxLevel = level;
		}
		if(this.minLevel > level){
			this.minLevel = level;
		}
		this.scales.put(level, scale);
		this.level = this.minLevel;
		this.pixelPerMeter = (Double)this.scales.get(this.minLevel); 
	}
	
	public void setScales(double ratio, int cnt){
		for(int i=0; i<cnt; i++){
			this.addScale(i, ratio/(Math.pow(2, i)));
		}
	}
	
	public void setScale(int level){
		this.level = level;
		this.pixelPerMeter = (Double)this.scales.get(this.level); 
	}
	
	protected boolean isMaxLevel(){
		if(this.level == this.maxLevel){
			return true;
		}
		return false;
	}
	
	protected boolean isMinLevel(){
		if(this.level == this.minLevel){
			return true;
		}
		return false;
	}
	
	protected void adjustRatio(double ratio){
		HashMap newScales = new HashMap();
		
		Set set = this.scales.keySet();
		Iterator it = set.iterator();
		while(it.hasNext()){
			Object key = it.next();
			Double ptm = (Double)this.scales.get(key);
			newScales.put(key, ptm.doubleValue()/ratio);
		}
		this.scales = newScales;
		this.pixelPerMeter /=ratio;
	}
	
	public Scale clone(){
		Scale copyScale = new Scale();
		copyScale.level = this.level;
		copyScale.maxLevel = this.maxLevel;
		copyScale.minLevel = this.minLevel;
		copyScale.pixelPerMeter = this.pixelPerMeter;
		
		Set set = this.scales.keySet();
		Iterator it = set.iterator();
		while(it.hasNext()){
			Object key = it.next();
			Double ptm = (Double)this.scales.get(key);
			copyScale.scales.put(key, ptm);
		}
		return copyScale;
	}
}
