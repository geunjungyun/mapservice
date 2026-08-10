package com.gis2.storage;

public class State {
	public long totalCnt = 0;
	long size = 0;
	long time = 0;
	public long cnt = 0;
	public int tps = 0;
	
	public long maxSize = Long.MIN_VALUE;
	public long maxTime = Long.MIN_VALUE;
	
	public void addSize(long size) {
		this.size += size;
		if(this.maxSize < size) {
			this.maxSize = size;
		}
	}
	
	public void addTime(long time) {
		this.time += time;
		if(this.maxTime < time) {
			this.maxTime = time;
		}
	}
}
