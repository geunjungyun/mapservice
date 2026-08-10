package com.util2.thread;

import java.util.List;


import com.util2.thread.CarMultiPoolManager;
import com.util2.thread.CarMultiThread;
import com.util2.thread.NotCreateException;
import com.util2.thread.PoolManager;
import com.util2.thread.ThreadObject;

public class CarMultiPoolManager extends PoolManager {
	
	/** Singleton 기법을 위해 */
	private static CarMultiPoolManager aServerThreadPoolManager;

	protected CarMultiPoolManager() throws NotCreateException {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public void initMng(int size, int waitTime){
		this.waitingTime = waitTime;
		this.initSize = size;
		this.maxSize = size;
		try {
			this.initPool();
		} catch (NotCreateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static CarMultiPoolManager getInstance() throws NotCreateException {
		if (aServerThreadPoolManager == null) {
			synchronized (CarMultiPoolManager.class) {
				if (aServerThreadPoolManager == null)
					aServerThreadPoolManager = new CarMultiPoolManager(); 
			}
		}
		
		return aServerThreadPoolManager;
	}
	
	public synchronized ThreadObject getThreadObject() throws NotCreateException {
		//System.out.println("getThreadObject="+this.toString());
		return super.getThreadObject();
	} // end public synchronized ThreadObject getThreadObject()

	
	
	public synchronized void release(ThreadObject obj) {
		
		super.release(obj);
		//System.out.println("release="+this.toString());
	} // end public synchronized void release(ThreadObject obj)
	
    protected synchronized ThreadObject createThreadObject() throws NotCreateException {

    	CarMultiThread obj = null;

        obj = new CarMultiThread();
        
        //obj.create();
        obj.create();
        
        readyPool.add(obj);

        return obj;

    } 

}
