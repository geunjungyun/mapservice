package com.util2.thread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.gis.map.TileMapFactory2;

public class CustomThreadPoolExecutor extends ThreadPoolExecutor {
    public CustomThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);  // 기본 afterExecute 호출 (상속받은 기본 구현 사용)
        
		super.afterExecute(r, t);
		if (t == null && r instanceof Future<?>) {
			try {
				Boolean result = (Boolean)((Future<?>) r).get();
				//System.out.println("동기화 서버 전달 상태 =" + result);
				
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

        // 추가적인 후처리 작업을 여기에 작성
        // 예: 실행된 작업의 로그를 기록하거나 특정 상태를 갱신하는 작업
    }
}
