package com.util2.thread;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.data.util.MapLog;

//import org.apache.log4j.Logger;

//import org.slf4j.Logger;

//import com.data.util.MapLog;
//import org.apache.log4j.Logger;
//
//import com.gis.engine.admin.MuscatSystem;
//import com.gis.engine.util.MapLog;
import com.util2.thread.NotCreateException;
import com.util2.thread.ThreadObject;
import org.slf4j.Logger;

public abstract class PoolManager {
	
	public static int MUSCAT_THREAD_MIN = 100;

	public static int MUSCAT_THREAD_MAX = 100;

	public static int MUSCAT_THREAD_WATINGTIME = 200;

	static Logger logger = MapLog.getSCLog();

	public int initSize = 2;                 // 초기 Pool Size
	public int maxSize = 2;                 // 최대 Pool Size
//	public int currentSize;                   // 현재 Pool Size
//	public int usedCount = 0;                 // 현재 사용중인 갯수
	public int waitingTime = 2000000;            // Pool을 다 썼을 경우 대기 시간
	public Integer totalCount = 0;						// 현재까지 사용한 Object수
	public List<ThreadObject> readyPool;        // 사용 가능한 ThreadObject 관리
	public List<ThreadObject> usedPool;			// 사용하고 있는 ThreadObject 관리
//	public List<ThreadObject> pool;             // 전체 ThreadObject 관리
;
	
	public int getCurrentSize() {
		return usedPool.size() + readyPool.size();
	}
	
	public int getUsedCount() {
		return usedPool.size();
	}
	
	private int getReadyCount() {
		return readyPool.size();
	}
	
	protected PoolManager() throws NotCreateException {

		readyPool = new Vector<ThreadObject>();
		usedPool = new Vector<ThreadObject>();
//		readyPool = new ArrayList<ThreadObject>();
//		usedPool = new ArrayList<ThreadObject>();
//		pool = new ArrayList<ThreadObject>();
		

//		readProperties();
//		initPool();

//		logger.debug("현재 전체 " + currentSize + "개 Pooling 되어졌으며 " + usedCount + "개 사용중입니다.");
//		logger.debug("현재 전체 " + currentSize + "개 Pooling 되어졌으며 " + usedCount + "개 사용중입니다.");
//		logger.debug("현재 전체 " + getCurrentSize() + "개 Pooling 되어졌으며 " + getUsedCount() + "개 사용중입니다.");

	} // end private PoolManager()

	//private void readProperties(String propertiesfilename) {
	private void readProperties() {

		// Properties 파일을 얻어온다.
		//InputStream is = getClass().getResourceAsStream(propertiesfilename);
		try {
			
			initSize = MUSCAT_THREAD_MIN; //tmpInitSize;
			maxSize = MUSCAT_THREAD_MAX; //tmpMaxSize;
			waitingTime = MUSCAT_THREAD_WATINGTIME; //tmpWaitingTime;

			logger.debug(" init_size = " + initSize + ", max_size = " + maxSize + ", waiting_time = " + waitingTime);

		} catch (Exception e) {

			logger.error("",e);
			/*
			logger.debug("properties file을 찾을수 없습니다.");
			logger.debug("'pool.properties'파일을 CLASSPATH안에 만들어 주세요.");
			logger.debug("Default 설정값으로 초기화 : init_size = " + initSize + ", max_size = " + maxSize + ", waiting_time = " + waitingTime);
			*/

		} // end try

	} // end private void readProperties()

	/**
	 * 각종 설정값을 초기화 한다.
	 *
	 * @throws NotCreateException
	 */

	public void initPool() throws NotCreateException {

		// 초기값 만큼 풀을 생성한다.
		ThreadObject obj = null;
//		currentSize = 0;
//		usedCount = 0;

		for (int i = 0; i < initSize; i++) {

			try {

				createThreadObject();				
//				obj.create();
//				readyPool.add( obj );
//				pool.add( obj );

			} catch (NotCreateException e) {} // end try

		} // end for

//		if (currentSize == 0) {
		if (getCurrentSize() == 0) {
		// 하나도 생성하지 못했으면 예외를 던진다.
			throw new NotCreateException("ThreadObject 객체를 하나도 생성하지 못했습니다.");

		} // end if (currentSize == 0)

	} // end private void initPool() throws NotCreateException

//	/**
//	 * Thread 객체를 생성한다.
//	 * @return 생성된 Thread 객체
//	 * @throws NotCreateException
//	 */
//	protected abstract ThreadObject createThreadObject() throws NotCreateException;

	private void destoryPool(List<ThreadObject> pool) {
		Iterator<ThreadObject> iter = pool.iterator();
		while(iter.hasNext()) {
			iter.next().destroy();
		}
	}
	/**
	 * 전체 ThreadObject을 닫는다.
	 */
	private void destroyAll() {

		logger.debug("전체 ThreadObject을 닫습니다.");
		
		destoryPool(readyPool);
		destoryPool(usedPool);

//		ThreadObject obj;
//		int length = pool.size();
//
//		for (int i = 0; i < length; i++) {
//
//			obj = (ThreadObject) pool.get(i);
//			obj.destroy();
//
//		} // end for

	} // end private destroyAll()

	/**
	 * ThreadObject을 새로 연결한다.
	 *
	 * @throws NotCreateException
	 */

	public synchronized void reset() throws NotCreateException {

		logger.debug("total ThreadObject reset");

		// 전체 ThreadObject을 닫는다.
		destroyAll();

		// 기본 풀들을 초기화 한다.
		readyPool.clear();
		usedPool.clear();
		
//		pool.clear();

		// 새롭게 풀들을 설정한다.
		initPool();

	} // end public void reset() throws NotCreateException


	private synchronized void readyToUsed(ThreadObject obj) {
		readyPool.remove(obj);
		usedPool.add(obj);		
	}
	
	private synchronized void usedToReady(ThreadObject obj) {
		usedPool.remove(obj);
		readyPool.add(obj);		
	}
	
	/**
	 * Pool에 있는 ThreadObject 하나를 얻어 온다.
	 * (만일 waitingTime값 만큼 기다린 다음 반납 된것이 없으면 null을 리턴)
	 *
	 * @return Pool에 있는 ThreadObject
	 * @throws NotCreateException
	 */
	private ThreadObject getObject() throws NotCreateException {

		ThreadObject obj = null;

		if (readyPool.size() > 0) {
		// readyPool에 남는 것이 있다면 할당한다.
//			obj = (ThreadObject) readyPool.get(0);
//			readyPool.remove(0);
//			obj = readyPool.get(0);
			obj = readyPool.remove(0);
			

		} // end if (readyPool.size() > 0)

//		else if (maxSize == 0 || currentSize < maxSize) {
		else if (maxSize == 0 || getCurrentSize() < maxSize) {
		// 남는것이 없고 현재 최대 개수만큼 생성되지 않았다면 새로 생성한다.
			obj = createThreadObject();

		} // end else if (maxSize == 0 || currentSize < maxSize)

		if (obj != null) {
//			synchronized (totalCount){
			totalCount++;
//			}
					
			readyToUsed(obj);								
//			usedCount++;

		} // end if (obj != null)

		return obj;

	} // end private ThreadObject getObject() throws NotCreateException

	/**
	 *
	 * @return
	 * @throws NotCreateException
	 */

	public synchronized ThreadObject getThreadObject() throws NotCreateException {

		ThreadObject obj = null;
		long startTime = System.currentTimeMillis();
		long tmpTime;
		long time = waitingTime;

		while ((obj = getObject()) == null) {

			try {
			// 기다리는 시간동안 wait() 한다.
			// 하지만 기다리는 시간내에 반납되면 notify 하면 깨어난다.
			// 따라서 기다리는 시간 이내에 깨어 날 수도 있다.
				wait(time);
				
			} catch (InterruptedException e) {}

			tmpTime = System.currentTimeMillis();

			if ((tmpTime - startTime) >= waitingTime) {
			// 기다리는 시간이 넘었다면 null을 리턴
				return null;

			} // end if ((tmpTime - startTime) >= waitingTime)

			time = tmpTime - startTime;

		} // end while ((obj = getObject()) == null)

		//logger.debug("now total size = " + currentSize + ", Pooling = " + usedCount);

		return obj;

	} // end public synchronized ThreadObject getThreadObject()

	/**
	 * ThreadObject를 Pool에 반납한다.
	 *
	 * @param obj 반납한 객체
	 */

	public synchronized void release(ThreadObject obj) {
		
		usedPool.remove(obj);
		readyPool.add(obj);
//		System.out.println(this.toString());
//		usedCount--;

		//notify();
		notifyAll();

		//logger.debug("release success : now use cnt = " + usedCount);

	} // end public synchronized void release(ThreadObject obj)
	
	public int clearTotalCount() {
		int rs = 0;
		
		synchronized (totalCount){
			 rs = totalCount;
			 totalCount = 0;
		}
		
		return rs;
	}
	
	/**
	 * 쓰레드 상태를 알려준다.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("conn: "); sb.append(clearTotalCount());		
		sb.append(" (threads:  UsedCount ="); sb.append(getUsedCount()); 
		sb.append("/ReadyCount="); sb.append(getReadyCount());		
		sb.append("/CurrentSize="); sb.append(getCurrentSize());		
		sb.append("/initSize="); sb.append(initSize);
		sb.append("/maxSize="); sb.append(maxSize); sb.append(")");
		
		return sb.toString();
	}
	
	
	 /**
     * 새로운 ThreadObject 객체를 얻어온다.
     *
     * @return 생성된 ThreadObject 객체
     * @throws NotCreateException
     */
//	abstract synchronized ThreadObject createThreadObject() throws NotCreateException;
    protected synchronized ThreadObject createThreadObject() throws NotCreateException {

    	return null;

    } // end private ThreadObject createThreadObject() throws NotCreateException

} // end class PoolManager
