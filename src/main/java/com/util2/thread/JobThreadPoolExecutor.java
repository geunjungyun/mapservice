package com.util2.thread;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.locationtech.jts.geom.Envelope;

import com.data.util.MapLog;
import com.gis.map.ILayer;
import com.gis.map.TileMapFactory2;
import com.gis.map.VectorLayer;
import com.util.io.FileUt;

public class JobThreadPoolExecutor extends ThreadPoolExecutor implements Comparable{
	
	long jobTime = -1;
	
	int jobCnt = 0;
	
	//public Object lock = new Object();
	
	Object lock = null;
	
	Job lastJob = null;

	public JobThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, Object _lock) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
		lock = _lock;
		// TODO Auto-generated constructor stub
	}
	
	
	protected void afterExecute(Runnable r, Throwable t) {
		super.afterExecute(r, t);
		if (t == null && r instanceof Future<?>) {
			try {
				Job result = (Job)((Future<?>) r).get();
				
				lastJob = result;
				
				jobCnt ++;
			} catch (CancellationException ce) {
				t = ce;
			} catch (ExecutionException ee) {
				t = ee.getCause();
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt(); // ignore/reset
			}
		}
		if (t != null)
			t.printStackTrace();
		synchronized (lock) {
			lock.notifyAll();
		}
	}

	@Override
	public int compareTo(Object o) {
		JobThreadPoolExecutor ttpe = (JobThreadPoolExecutor) o;
		if (ttpe.getMaximumPoolSize() > this.getMaximumPoolSize()) {
			return -1;
		} else {
			return 1;
		}
	}
	
	public void run(Job jr) {
		
		String tmsName = jr.getTmsName();
		int runLevel = jr.getRunLevel(); 
		int runX = jr.getRunX(); 
		int runY = jr.getRunY(); 
		Envelope tileEnv = jr.getTileEnv(); 
		int DrawCanvasRatio = jr.getDrawCanvasRatio(); 
		TileMapFactory2 tmf = jr.getTmf(); 
		boolean isProcess = jr.isProcess();
		boolean isDebug = jr.getDebug();
		

			// Future ft = executorService.submit(() -> {
			Callable task1 = () -> {
				//long dataSize = 0;
				
				long st = System.currentTimeMillis();
				try {
					HashMap<String, byte[]> tiles = tmf.getTileImages(runLevel, (int) runX,	(int) runY, DrawCanvasRatio, tileEnv, null, null, isDebug);
					
				} catch (Exception e) {
					e.printStackTrace();
					RemoteTileCreateException rce = new RemoteTileCreateException(e.toString(),
							e.getCause());
					rce.level = runLevel;
					rce.x = (int) runX;
					rce.y = (int) runY;
					rce.tileEnv = tileEnv;
					throw rce;
				}
				
				return jr;
			};

			Future ft1 = this.submit(task1);
			

	}
	

}
