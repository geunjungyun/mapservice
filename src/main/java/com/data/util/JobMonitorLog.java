package com.data.util;

import java.io.File;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Vector;

//import org.slf4j.Logger;

//import org.apache.log4j.Level;
//import org.apache.log4j.Logger;
import org.slf4j.Logger;


/**
 * 배치 로직에 진행 현황을 로그로 저장한다.
 *
 */
public class JobMonitorLog {
	
	Logger log = null;
	
	long startTime = System.currentTimeMillis();

	long st = System.currentTimeMillis();
	
	double totalCnt = 0;
	
	float percent = -1;
	
	double nowCnt =-1;
	
	Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	String s = null;
	
	File dstFile = null;
	
	int unit = 0;
	
	double startfreeSpace = 0f;
	
	double totalSaveSize = 0f;
	
	
	/**
	 * 
	 * @param pfile 데이타가 저장될 경로
	 * @param _log 로그
	 * @param _unit 
	 */
	public JobMonitorLog(File pfile, Logger _log, int _unit) {
		this.setLog(_log);
		this.setWriteUnit(_unit);
		if(pfile == null) {
			this.log.error("pfile is null");
		}
		this.setDstFile(pfile);
		
	}
	
//	jobLog.setDstFile(new File(this.outPath));
//	jobLog.setLog(log);
//	jobLog.setWriteUnit(1);

	
	
	/**
	 * 전체 개수
	 * @param cnt
	 */
	public void setTotalCnt(double cnt){
		this.totalCnt = cnt;
	}
	
	/**
	 * 시작 시간
	 * @param time
	 */
	public void setStartTime(long time){
		this.startTime = time;
		this.st = time;
		s = formatter.format(startTime);
	}
	
	/**
	 * 파일 시스템 저장소
	 * @param file
	 */
	public void setDstFile(File file){
		this.dstFile = file;
		this.startfreeSpace = dstFile.getFreeSpace()/Math.pow(1024, 3);
	}
	
	public void setLog(Logger log){
		this.log = log;
	}
	
	/**
	 * 로그를 저정하는 단위 
	 * 0 int 형 단위 ex) 0 1 2 3
	 * -1 소수점 첫번째 ex) 0.1 0.2 0.3
	 * -2 소수점 두번째 ex) 0.01 0.02 0.03
	 * 1 정수 두자리수 단위 ex) 10 20 30 
	 */
	public void setWriteUnit(int cnt){
		this.unit = cnt;
	}
	
	
	public float getUnit(double percentTmp){
		
		float value = -1;
		if(this.unit == 0){
			value = (new Float(String.format("%.0f", percentTmp))).floatValue();
		}
		else if(this.unit == -1){
			value = (new Float(String.format("%.1f", percentTmp))).floatValue();
		}
		else if(this.unit == -2){
			value = (new Float(String.format("%.2f", percentTmp))).floatValue();
		}
		else if(this.unit == 1){
			value = (((int)(percentTmp/10))*10);
		}
		
		return value;
	}
	
	
	/**
	 * 
	 * @param cnt 진행 개수
	 * @param size 진행 사이즈(byte 단위)
	 */
	public boolean setNowJob(double cnt, double _size){
		this.nowCnt = cnt;
		
		double percentTmp = (nowCnt/totalCnt)*100.0;
		
		float compareValue = this.getUnit(percentTmp);
		
		if(percent != compareValue){
			
			percent = compareValue;
			long et = System.currentTimeMillis();
			
			//정확한 진행율
			long percentTime = (et - st);
			
			long jobTime = et - startTime;
			
			long restTime = (long) ((jobTime * 100.0)/(percentTmp) - jobTime);
			
			if(percent == 0){
				restTime = 0;
			}
			
			int jobHour = (int) (jobTime/1000.0/3600.0);
			int jobMin  = (int) ( ((jobTime/1000.0/3600.0) - jobHour)*60);
			
			int restHour = (int) (restTime/1000.0/3600.0);
			int restMin = (int) ( ((restTime/1000.0/3600.0) - restHour)*60);
			
			long totalTime = jobTime + restTime;
			
			int totalHour = (int) (totalTime/1000.0/3600.0);
			int totalMin = (int) ( ((totalTime/1000.0/3600.0) - totalHour)*60);
			
			
			String endTime = formatter.format(startTime+restTime+jobTime);
			
			double restfreeSpace = dstFile.getFreeSpace()/Math.pow(1024, 3);
			
			double size = _size;
			
			if(size == -1) {
				size = this.startfreeSpace  - restfreeSpace;
			}
			
			//double saveSize = size/Math.pow(1024, 3);

			double saveSize = this.getFileSize(dstFile)/Math.pow(1024, 3);
			
			double expectTotalSize = (saveSize*100.0)/percentTmp;
			
			
			
//			Vector results = itpm.getLastSaveTile();
//			lastSaveLevel = ((Integer)results.get(0)).intValue();
//			lastXIdx = ((Integer)results.get(1)).intValue();
//			lastYIdx = ((Integer)results.get(2)).intValue();
//			lastSaveEnv = (Envelope)results.get(3);
			
			StringBuffer sb = new StringBuffer();
		
			
			sb.append("\n "+this.dstFile.getAbsolutePath()+"############################################### \n");
			sb.append("시작 시간      : " + s +",    진행율 : "+ percent+"%, 소요 시간 = "+ jobHour+" h "+ jobMin + " m \n");
			sb.append(
					"저장 사이즈 : "+Math.round(saveSize*1000)/1000.0+" GB"+
					", 전체 예상 사이즈 : "+Math.round(expectTotalSize*1000)/1000.0 +" GB" + 
					", 남은 예상 사이즈 : " +Math.round((expectTotalSize-saveSize)*1000)/1000.0 +" GB" +
					", 디스크 남은 사이즈 : " + Math.round(restfreeSpace*1000)/1000.0+" GB \n");
			
			sb.append("종료 예상 시간 : "+ endTime+", 남은 진행율 : "+String.format("%.1f", 100.0-percent)+
					"%, 남은 예상 시간 = " + restHour +" h " +restMin+" m"+
					", 전체 소요 예상 시간 = " + (totalHour)+" h "+totalMin+" m \n");
			sb.append("###################################################################################### \n");
			
			log.info(sb.toString());
			st = et;
			return true;
		}
		return false;
	}
	
	
	public void addSize(long value) {
		this.totalSaveSize+=value;
	}
	
	/**
	 * 
	 * @param cnt 진행 개수
	 * @param size 진행 사이즈(byte 단위)
	 * @throws Exception 
	 */
	public String getNowJob(double cnt) {
		this.nowCnt = cnt;
		
		double percentTmp = (nowCnt/totalCnt)*100.0;
		
		float compareValue = (int)percentTmp;//this.getUnit(percentTmp);
		
		if(percent != compareValue){
			
			percent = compareValue;
			
			long et = System.currentTimeMillis();
			
			//정확한 진행율
			long percentTime = (et - st);
			
			long jobTime = et - startTime;
			
			long restTime = (long) ((jobTime * 100.0)/(percentTmp) - jobTime);
			
			if(percent == 0){
				restTime = 0;
			}
			
			int jobHour = (int) (jobTime/1000.0/3600.0);
			int jobMin  = (int) ( ((jobTime/1000.0/3600.0) - jobHour)*60);
			
			int restHour = (int) (restTime/1000.0/3600.0);
			int restMin = (int) ( ((restTime/1000.0/3600.0) - restHour)*60);
			
			long totalTime = jobTime + restTime;
			
			int totalHour = (int) (totalTime/1000.0/3600.0);
			int totalMin = (int) ( ((totalTime/1000.0/3600.0) - totalHour)*60);
			
			
			String endTime = formatter.format(startTime+restTime+jobTime);
			
			
			//double restfreeSpace = dstFile.getFreeSpace()/Math.pow(1024, 3);
			
			
//			double size = -1;
//			
//			if(size == -1) {
//				size = this.startfreeSpace  - restfreeSpace;
//			}
			
			//double saveSize = size/Math.pow(1024, 3);
			
			if(this.dstFile == null) {
				this.log.error("this.dst File is null");
			}
			
			//double saveSize = this.getFileSize(this.dstFile)/Math.pow(1024, 3);
			
			double saveSize = this.totalSaveSize/Math.pow(1024, 3);
			
			double expectTotalSize = (saveSize*100.0)/percentTmp;
			
			
			
//			Vector results = itpm.getLastSaveTile();
//			lastSaveLevel = ((Integer)results.get(0)).intValue();
//			lastXIdx = ((Integer)results.get(1)).intValue();
//			lastYIdx = ((Integer)results.get(2)).intValue();
//			lastSaveEnv = (Envelope)results.get(3);
			
			StringBuffer sb = new StringBuffer();
		
			
			sb.append("###### "+this.dstFile.getAbsolutePath()+"####### \n");
			sb.append("시작 시간      : " + s +",    진행율 : "+ percent+"%, 소요 시간 = "+ jobHour+" h "+ jobMin + " m \n");
			sb.append(
					"저장 사이즈 : "+Math.round(saveSize*1000)/1000.0+" GB"+
					", 전체 예상 사이즈 : "+Math.round(expectTotalSize*1000)/1000.0 +" GB \n");
			
			sb.append("종료 예상 시간 : "+ endTime+", 남은 진행율 : "+String.format("%.1f", 100.0-percent)+
					"%, 남은 예상 시간 = " + restHour +" h " +restMin+" m"+
					", 전체 소요 예상 시간 = " + (totalHour)+" h "+totalMin+" m \n");
			sb.append("###################################################################################### \n");
			
			//log.info(sb.toString());
			st = et;
			return sb.toString();
		}
		return null;
	}
	
	
	
	Long getFileSize(File f){
		Long l = new Long(0);
		File[] list = f.listFiles();
		for (int i = 0; i < list.length; i++) {
			if (list[i].isDirectory())
				l = new Long(l.longValue() + getFileSize(list[i]).longValue());
			else {
				//System.out.println(list[i].getAbsolutePath() + " : " + list[i].length());
				l = new Long(l.longValue() + list[i].length());
			}
		}
		return l;
	}

	
	public static void main(String[] args) {
		
//		Logger mlog = MapLog.getLogger("f:/test.log", "log", Level.INFO, "1MB", 5, "[%d{yy-MM-dd HH:mm:ss}] %m%n", true);
//		JobMonitorLog jml = new JobMonitorLog(new File("F:/ArcGISServer"), mlog, 1);
//		jml.setTotalCnt(100);
//		jml.setStartTime(System.currentTimeMillis());
//		
//		for(int i=1; i<=100; i++) {
//			jml.setNowJob(i, -1);
//		}
	}

}
