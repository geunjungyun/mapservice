package com.gis2.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import com.data.util.MapLog;
import com.gis.map.TileMapFactory2;
import com.util2.thread.JobResource;

public class JobMng implements ServletContextListener {

	// ConcurrentMap<Object, JobResource> threadStatus = new ConcurrentHashMap<>();

	Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	static ServletContext sc = null;

	Vector<JobResource> jrs = new Vector();

	Object lock = new Object();

	ThreadPoolExecutor executor = null;

	int threadCnt = 10;

	public static String savePath = null;

	public static String convPath = null;

	public static boolean isSaveServer = false;

	public static String saveServer = null;

	public static String saveResultUrl = null;

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		JobMng jm = new JobMng();
		jm.liveCheck();

		try {
			Thread.sleep(1000 * 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		// TODO Auto-generated method stub

		ServletContext context = sce.getServletContext();
		this.sc = context;

		context.setAttribute("JobMng", this);

		this.initThread();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// TODO Auto-generated method stub

	}

	public void initThread() {

		Properties enginProperty = new Properties();
		System.out.println("AuthorityMng contextInitialized Loading");
		try {
			// Get Engine config path
			String getContextPath = sc.getContextPath();

			String meta_info_path = sc.getRealPath("WEB-INF");
			String cur_dir = System.getProperty("user.dir");

			System.out.println("jetty.home " + System.getProperty("jetty.home"));

			File path = new File(
					cur_dir + File.separator + "map_service" + File.separator + "service_config.properties");

			System.out.println("Properties read start");

			if (path.exists()) {
				FileInputStream fiss = new FileInputStream(path);
				enginProperty.load(fiss);
				meta_info_path = cur_dir + File.separator + "map_service";

				// PropertyMng.getInstance(path.getAbsolutePath());
			} else {
				FileInputStream fiss = new FileInputStream(
						meta_info_path + File.separator + "service_config.properties");
				enginProperty.load(fiss);

				// PropertyMng.getInstance(meta_info_path+File.separator +
				// "service_config.properties");
			}

			String isSaveServerS = enginProperty.getProperty("isSaveServer", "");

			if (isSaveServerS != null && isSaveServerS.equals("true")) {
				isSaveServer = true;
			} else {
				isSaveServer = false;
			}
			
			MapLog.getSCLog().debug("isSaveServer=" + isSaveServer);
			


			String saveServerS = enginProperty.getProperty("saveServer", "");

			saveServer = saveServerS;

			MapLog.getSCLog().debug("saveServer=" + saveServer);
			

			savePath = enginProperty.getProperty("savePath", "").trim();

			MapLog.getSCLog().debug("savePath=" + savePath);

			convPath = enginProperty.getProperty("convPath", "").trim();

			MapLog.getSCLog().debug("convPath=" + convPath);

			saveResultUrl = enginProperty.getProperty("saveResultUrl", "");

			MapLog.getSCLog().debug("saveResultUrl=" + saveResultUrl);

			String cnt = enginProperty.getProperty("saveServerThreadCnt", "");

			if (cnt != null) {
				this.threadCnt = Integer.parseInt(cnt.trim());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		executor = new ThreadPoolExecutor(threadCnt, threadCnt, 0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<>()) {
			@Override
			protected void beforeExecute(Thread t, Runnable r) {
				super.beforeExecute(t, r);

				String jobId = "";
				if (t == null && r instanceof Future<?>) {
					try {
						Job job = (Job) ((Future<?>) r).get();
						jobId = job.id;
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				// System.out.println("Before execute: " + jobId);
			}

			@Override
			protected void afterExecute(Runnable r, Throwable t) {
				super.afterExecute(r, t);
				String jobId = "";
				if (t == null && r instanceof Future<?>) {
					try {
						Job job = (Job) ((Future<?>) r).get();

						boolean ok = sendResult(job);

						if (ok) {
							removeJob(job.id);
						}

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

				System.out.println("After execute: " + jobId);
			}
		};
		
		if(this.isSaveServer) {
			this.liveCheck();
		}
	}

	public boolean sendResult(Job job) {
		boolean ok = true;
		try {
			URL url = new URL(JobMng.saveResultUrl + "?jobId=" + job.getId() + "&jobStatus=" + job.current + "&error="
					+ job.error);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(3000);
			connection.setReadTimeout(3000);

			// InputStream 읽기
			try (InputStream inputStream = connection.getInputStream()) {
				// IOUtils로 문자열 변환
				String json = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
				System.out.println("작업 서버 결과 전달 : " + json);
			}

		} catch (Exception e) {
			MapLog.getSCLog().debug("sendResult=" + saveResultUrl + " error=" + e.toString());
			// e.printStackTrace();
			ok = false;
		}

		return ok;
	}

	public void run(Job job) {

		Callable task = () -> {

			job.save();

			return job;
		};

		Future ft = executor.submit(task);

		JobResource jr = new JobResource(ft, job);

		synchronized (lock) {
			this.jrs.add(jr);
		}

	}

	public boolean removeJob(String jobId) {
		JobResource jr = null;

		synchronized (lock) {

			for (int i = 0; i < this.jrs.size(); i++) {
				JobResource jrc = this.jrs.get(i);
				if (jrc.job.id.equals(jobId)) {
					jr = jrc;
				}
			}

			if (jr == null) {
				return false;
			}

			this.jrs.remove(jr);
		}

		return true;
	}

	public Job getJobStatus(String id) {

		JobResource jr = null;

		Job job = null;
		synchronized (lock) {

			int ingStart = -1;

			int nowIdx = -1;
			System.out.println("-------------------------------------------------------------");
			for (int i = 0; i < this.jrs.size(); i++) {
				JobResource jrc = this.jrs.get(i);

				String regTime = formatter.format(jrc.job.regTime);
				String startTime = formatter.format(jrc.job.startTime);

				long jobTime = jrc.job.endTime - jrc.job.startTime;

				int second = (int) (jobTime / 1000.0);

				if (jrc.job.current != Job.END) {
					second = 0;
				}

				System.out.println("id=" + jrc.job.id + ", cur=" + jrc.job.printCur() + ",st=" + regTime + ",rt="
						+ startTime + ",jt=" + second + " sec");

				if ((jrc.job.current == Job.STANDBY || jrc.job.current == Job.ING) && ingStart == -1) {
					ingStart = i;
				}

				if (jrc.job.id.equals(id)) {
					nowIdx = i;
					jr = jrc;
				}
			}

			if (jr != null) {
				job = jr.job;
				if (nowIdx != -1) {
					job.queueCnt = nowIdx - ingStart;
				} else {
					job.queueCnt = 0;
				}
			}

		}
		return job;

	}

	/**
	 * 
	 * @param id
	 * @return 입력되는 id 의 Job 이 없을 경우 false
	 */
	public boolean stop(String id) {
		JobResource jr = null;

		synchronized (lock) {
			for (JobResource jre : this.jrs) {
				if (jre.job.id.equals(id)) {
					jr = jre;
				}
			}
		}

		if (jr == null) {
			return false;
		}

		Future<?> future2 = jr.future;

		boolean removed = executor.getQueue().remove(future2);

		if (removed) {
			System.out.println("Queue 삭제");
		} else {
			jr.job.runStop = true;
			// future2.cancel(true);
			System.out.println("실행중 쓰레드 중지");
		}
		return true;
	}

	class JobResource {
		Future<?> future;
		Job job;

		public JobResource(Future<?> future, Job job) {
			this.future = future;
			this.job = job;
		}
	}

	public void liveCheck() {

		// System.out.println(formatter.format(System.currentTimeMillis()));
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

		scheduler.scheduleAtFixedRate(() -> {

			synchronized (this.lock) {

				Vector<Integer> clearList = new Vector();

				for (int i = 0; i < this.jrs.size(); i++) {
					JobResource jb = this.jrs.get(i);

					long time = System.currentTimeMillis() - jb.job.endTime;

					long hour5 = 1000 * 3600 * 5;
					//long hour5 = 1000 * 120;

					if (jb.job.current == Job.END && hour5 < time ) {
						clearList.add(i);
					}
				}

				for (int i = (clearList.size() - 1); i > -1; i--) {
					
					int deleteIdx = clearList.get(i);
					MapLog.getSCLog().debug("delete job = " + this.jrs.get(deleteIdx).job.getId());
					
					this.jrs.remove(deleteIdx);
				}

				//System.out.println("job resource delete cnt = " + clearList.size());
			}

		}, 20, 20, TimeUnit.SECONDS); // 처음 실행 지연 시간 0초, 주기 20초
	}

}
