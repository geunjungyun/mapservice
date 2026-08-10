package com.util2.thread;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.locationtech.jts.geom.Envelope;

import com.data.util.MapLog;
import com.gis.map.ILayer;
import com.gis.map.TileMapFactory2;
import com.gis.map.VectorLayer;
import com.gis2.storage.TileDB;
import com.util.io.FileUt;

public class TileThreadPoolExecutor extends ThreadPoolExecutor implements Comparable {

	public String ip = "";

	public int port = -1;

	long jobTime = -1;

	int jobCnt = 0;

	// public Object lock = new Object();

	Object lock = null;

	JobResource lastJob = null;
	
	Vector<JobResource> list = new Vector();

	public TileThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, Object _lock) {

		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);

		lock = _lock;
		// TODO Auto-generated constructor stub
	}

	public void saveLastJob(String file) {

		File ing = new File(file);

		synchronized(this.list) {
			if(this.list.size() == 0) {
				return;
			}
			lastJob = this.list.get(0);
		}
		
		lastJob.getTmf().shutdown = true;
		String data = (int) lastJob.getRunLevel() + "," + (int) lastJob.getRunX() + "," + (int) lastJob.getRunY() + ","
				+ lastJob.mbrIdx + "," + lastJob.getTileEnv().getMinX() + "," + lastJob.getTileEnv().getMinY();
		
//		Instant instant = Instant.ofEpochMilli(lastJob.time);
//		LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        String formatted = dateTime.format(formatter);
        
        Date date = new Date(lastJob.time);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        System.out.println("년월일시분초: " + sdf.format(date));
        
		try {
			MapLog.getFileLog().debug("마지막 저장 타일 정보 =" + data+", time="+sdf.format(date)+",대기 및 진행 개수=" + this.list.size());
			FileUtils.writeStringToFile(ing, data, "utf-8", false);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void afterExecute(Runnable r, Throwable t) {
		super.afterExecute(r, t);
		if (t == null && r instanceof Future<?>) {
			try {
				JobResource result = (JobResource) ((Future<?>) r).get();

//				jobTime += (Long)result;

				TileMapFactory2.jobLog.addSize(result.dataSize);
				

//				lastJob = result;
				
				this.remove(result);
				
//				String data = (int) result.getRunLevel() + "," + (int) result.getRunX() + ","
//						+ (int) result.getRunY() + "," + result.mbrIdx + "," + result.getTileEnv().getMinX() + ","
//						+ result.getTileEnv().getMinY();
//				System.out.println("afterExecute=" + data);
				

				jobCnt++;
			} catch (CancellationException ce) {
				t = ce;
			} catch (ExecutionException ee) {
				t = ee.getCause();
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt(); // ignore/reset
			}
		}
		if (t != null) {
			if(t instanceof RemoteTileCreateException) {
				RemoteTileCreateException rtce = (RemoteTileCreateException)t;
				JobResource jr = new JobResource();
				jr.setRunX(rtce.x);
				jr.setRunY(rtce.y);
				jr.setRunLevel(rtce.level);
				this.remove(jr);
				System.out.println("rtce=" + rtce.toString());
			}
			else if(r instanceof Future<?>) {
				try {
					JobResource result = (JobResource) ((Future<?>) r).get();
					this.remove(result);
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
			
			t.printStackTrace();
		}
		synchronized (lock) {
			lock.notifyAll();
		}
	}

	@Override
	public int compareTo(Object o) {
		TileThreadPoolExecutor ttpe = (TileThreadPoolExecutor) o;
		if (ttpe.getMaximumPoolSize() > this.getMaximumPoolSize()) {
			return -1;
		} else {
			return 1;
		}
	}
	
	public synchronized void remove(JobResource jr_) {
		
		int selectIdx = -1;
		for(int i=0; i<this.list.size(); i++) {
			JobResource jr = this.list.get(i);
			if(jr.getRunX() == jr_.getRunX() && jr.getRunY() == jr_.getRunY() && jr.getRunLevel() == jr_.getRunLevel()) {
				selectIdx = i;
			}
		}
		
		this.list.remove(selectIdx);
	}
	
	

	public void run(JobResource jr) {
		
		jr.time = System.currentTimeMillis();
		
		synchronized(this.list) {
			list.add(jr);
		}

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
			// long dataSize = 0;

			long st = System.currentTimeMillis();
			try {
				HashMap<String, byte[]> tiles = tmf.getTileImages(runLevel, (int) runX, (int) runY, DrawCanvasRatio,
						tileEnv, null, null, isDebug);

				Set set = tiles.keySet();
				Iterator it = set.iterator();
				while (it.hasNext()) {
					String tilePath = (String) it.next();

					Envelope tenv = tmf.getMbr(tilePath);
					if (this.isSaveMbrInterect(jr.getSaveMbrs(), tenv)) {

						byte[] data = tiles.get(tilePath);

//						if (data.length == 169) {
//							continue;
//						}

						jr.dataSize += data.length;
						tmf.putTileImage(tilePath, data);
					}
				}

				//System.out.println("getTileImages time = " + (System.currentTimeMillis() - st) / 1000.0);

			} catch (Exception e) {
				e.printStackTrace();
				RemoteTileCreateException rce = new RemoteTileCreateException(e.toString(), e.getCause());
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

	public boolean isSaveMbrInterect(Envelope[] mbrs, Envelope tileMbr) {
		for (Envelope saveEnv : mbrs) {
			if (saveEnv.intersects(tileMbr)) {
				return true;
			}
		}
		return false;
	}

}
