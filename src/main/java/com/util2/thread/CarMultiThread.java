package com.util2.thread;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Vector;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.simple.SimpleFeature;

import com.data.file.FileLayer;

import com.util2.thread.CarMultiPoolManager;
import com.util2.thread.NotCreateException;
import com.util2.thread.ThreadObject;

public class CarMultiThread extends ThreadObject implements Runnable {
	
	private static final boolean DEBUG = false;



	private String trId = "";
	private Thread runner; // 사용자 접속을 처리하는 Thread
	private boolean isRun; // 현재 Thread의 running 여부
	private Object lockObject; // 동기화 처리를 위해
	
	
	int callCnt = 0;
	double jobTime = 0;
	int callMaxCnt = 1000;

	
//	com.gis.protocol.freegis3.CarRouteReq crr = null;
//	HashMap<CarRouteReq, CarRoute> ccrs = null;
	
	FileLayer jijukLayer = null;
	long idx = 0;
	Geometry subG = null;
	Vector<String> addPnus = null;
	String code = "";
	
	Object lock = null;
	
	public CarMultiThread() {
		lockObject = new Object();

		//log = new StringBuffer();
	}
	
	/**
	 * 객체를 초기화 한다.
	 */
	public void init(FileLayer _jijuk, long _idx, Geometry _jigu, Vector<String> _addPnus, String _code) {
		this.jijukLayer = _jijuk;
		this.idx = _idx;
		this.subG = _jigu;
		this.addPnus = _addPnus;
		this.code = _code;
		
	}
	
	public Object lockObj = new Object();
	
	public void run() {

		while (isRun) {
			synchronized (lockObject) {
				
				try {
					
					//Long idx = (Long) idxs.get(i);
					/*
					SimpleFeature jijukSf = jijukLayer.readFeature(idx);
					Geometry jijukGeo = (Geometry) jijukSf.getDefaultGeometry();

					String jpnu = (String) jijukSf.getAttribute("pnu");
					if (jpnu == null || !jpnu.startsWith(code)) {
						//continue;
					}
					else if(subG.intersects(jijukGeo)) {
						this.addPnus.add(jpnu);
					}
					*/
					
					SimpleFeature jijukSf = jijukLayer.readFeature(idx);
					Geometry jijukGeo = (Geometry) jijukSf.getDefaultGeometry();

					String jpnu = (String) jijukSf.getAttribute("pnu");
					if (jpnu == null || !jpnu.startsWith(code)) {
						//continue;
					}
					else if(subG.contains(jijukGeo)) {
						this.addPnus.add(jpnu);
					}
					else if(subG.intersects(jijukGeo)) {
						Geometry intertemp = null;
						try {
							intertemp = jijukGeo.intersection(subG);
						} catch (Exception e) {
							try {
								jijukGeo = jijukGeo.buffer(0.0000001);
								Geometry intersTp = subG.buffer(0.0000001);
								intertemp = jijukGeo.intersection(intersTp);
							} catch (Exception e1) {

							}
						}
						if (intertemp != null) {
							Geometry temp = this.getPolygon2(intertemp);
							if (temp != null && temp.isEmpty() == false) {
								this.addPnus.add(jpnu);
							}
						}
					}
					
					
					disconnect();
					
					// 자신의 자원을 양보하여 동시 사용을 높인다.
					// 실제로 IO 부분에서 synchonize를 처리해 주지만 확인 사살 차원이다.
					Thread.yield();

				} catch (Exception e) {
					e.printStackTrace();
					//MapLog.printErrorLog(e);
					disconnect();
					Thread.yield();
				} // end try

			} // end synchronized(lockObject)

		} // end while(true)

	} // end public void run()

	
	/**
	 * Muscat 에러를 처리한다.
	 */
//	private void printError(MuscatException e, Runner oldRunner) {
//		if((header != null) && (header.res_type != null)) {
//			Except.create(oldRunner, e);
//		}		
//	}
	
	/**
	 * 접속 종료
	 */
	private void disconnect() {
		try {


		} catch (Exception e) {

		} finally {

			try {
				// 순서에 주의
				// 반납후 wait 하여 멈추게 한다.
				CarMultiPoolManager.getInstance().release(this);
				waitThread();
				//stop(); //Waiting testcode
			} catch (Exception e) {
			}

		} // end try

	} // end private void disconnect()

	/**
	 * Thread를 초기화 한다.
	 * 
	 * @throws NotCreateException
	 */
	public void start() {

		if (runner == null) {
			// 새로 Thread를 생성하여 실행한다.
			runner = new Thread(this, "Server Receiver");
			isRun = true;
			runner.start();
		} // end if (runner == null)
		else {
			stop();
			start();
		} // end else

	} // end public void start()

	/**
	 * Thread를 종료한다.
	 */
	public void stop() {
		if (runner != null) {
			isRun = false;
			runner.interrupt();

			// Thread가 종료할때까지 대기한다.

			try {
				runner.join(1000);
			} catch (Exception e) {
			}
			// 명시적으로 가비지 컬렉팅을 위해 마킹한다.
			runner = null;

		} // end if (runner != null)

	} // end public void stop()

	public void create() throws com.util2.thread.NotCreateException{
		waitThread();
	} // end public void create() throws

	/**
	 * Thread의 상태를 멈춘다.
	 */
	public void waitThread() {

		//synchronized (lockObject) {
			try {
				lockObject.wait();
			} catch (Exception e) {
				//MapLog.printErrorLog(e);
			}
		//}
	}

	/**
	 * Thread의 상태를 활성화 한다.
	 */
	public void notifyThread() {
		
		synchronized (lockObject) {
			// lock을 푼다.
			lockObject.notify();

		}

		if (runner == null) {
			// Thread가 null이 라면 시작한다.
			start();
		}

	}

	/**
	 * 최종화 처리를 한다.
	 */
	public void destroy() {
		disconnect();
		stop();
	}
	
	public Geometry getPolygon2(Geometry geo) {

		// double lenArea = 2.2;

		if (geo instanceof Polygon || geo instanceof MultiPolygon) {
			return geo;
		} else if (geo instanceof GeometryCollection) {
			Vector<Polygon> newps = new Vector();
			GeometryCollection geoc = (GeometryCollection) geo;
			int cnt = geoc.getNumGeometries();
			for (int i = 0; i < cnt; i++) {
				Geometry subGeo = geoc.getGeometryN(i);
				if (subGeo instanceof Polygon) {
					newps.add((Polygon) subGeo);
				} else if (subGeo instanceof MultiPolygon) {
					int mcnt = subGeo.getNumGeometries();
					for (int k = 0; k < mcnt; k++) {
						Geometry subPg = subGeo.getGeometryN(k);
						newps.add((Polygon) subPg);
					}
				}
			}

			Polygon[] pls = new Polygon[newps.size()];
			for (int m = 0; m < newps.size(); m++) {
				pls[m] = newps.get(m);
			}
			return gft.createMultiPolygon(pls);

		}
		return null;
	}

	GeometryFactory gft = new GeometryFactory();
}
