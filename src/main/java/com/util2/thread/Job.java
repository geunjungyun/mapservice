package com.util2.thread;

import org.locationtech.jts.geom.Envelope;

import com.gis.map.TileMapFactory2;

public class Job {
	private String tmsName;
	private int runLevel;
	private int runX;
	private int runY;
	private Envelope tileEnv;
	
	private int DrawCanvasRatio;
	private TileMapFactory2 tmf;
	
	private boolean isProcess;
	
	private Envelope[] saveMbrs;
	private boolean debug = false;
	
	public int mbrIdx = -1;
	
	public int dataSize = 0;
	
	
	public boolean getDebug() {
		return debug;
	}
	
	public void setDebug(boolean mode) {
		this.debug = mode;
	}
	
	
	public int getRunLevel() {
		return runLevel;
	}
	public void setRunLevel(int runLevel) {
		this.runLevel = runLevel;
	}
	public int getRunX() {
		return runX;
	}
	public void setRunX(int runX) {
		this.runX = runX;
	}
	public int getRunY() {
		return runY;
	}
	public void setRunY(int runY) {
		this.runY = runY;
	}
	public Envelope getTileEnv() {
		return tileEnv;
	}
	public void setTileEnv(Envelope tileEnv) {
		this.tileEnv = tileEnv;
	}
	public TileMapFactory2 getTmf() {
		return tmf;
	}
	public void setTmf(TileMapFactory2 tmf) {
		this.tmf = tmf;
	}
	public int getDrawCanvasRatio() {
		return DrawCanvasRatio;
	}
	public void setDrawCanvasRatio(int drawCanvasRatio) {
		DrawCanvasRatio = drawCanvasRatio;
	}
	public String getTmsName() {
		return tmsName;
	}
	public void setTmsName(String tmsName) {
		this.tmsName = tmsName;
	}
	public boolean isProcess() {
		return isProcess;
	}
	public void setProcess(boolean isProcess) {
		this.isProcess = isProcess;
	}
	public Envelope[] getSaveMbrs() {
		return saveMbrs;
	}
	public void setSaveMbrs(Envelope[] saveMbrs) {
		this.saveMbrs = saveMbrs;
	}
}
