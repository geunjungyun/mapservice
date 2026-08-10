package com.util2.thread;

import java.util.HashMap;

import org.locationtech.jts.geom.Envelope;

public class RemoteTileCreateException extends Exception {
	
	public int level;
	public int x;
	public int y;
	public Envelope tileEnv;
	
	public RemoteTileCreateException(String message, Throwable cause) {
		super(message, cause);
	}
	
//	HashMap<String, byte[]> tiles = this.getTileImages(runLevel, (int) runX,
//			(int) runY, DrawCanvasRatio, tileEnv, null, null);
}
