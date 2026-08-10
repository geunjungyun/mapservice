package com.data.util;

import java.io.IOException;

//import org.slf4j.Logger;
//import org.slf4j.event.Level;

//import org.apache.log4j.ConsoleAppender;
//import org.apache.log4j.Layout;
//import org.apache.log4j.Level;
//import org.apache.log4j.LogManager;
//import org.apache.log4j.Logger;
//import org.apache.log4j.PatternLayout;
//import org.apache.log4j.RollingFileAppender;
//import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gis2.storage.ServiceConfig;
import com.gis2.storage.TileDB;
import com.gis2.storage.Version;

public class MapLog {

	public static Logger SERVICELOG;
	
	public static Logger FILELOG;
	
	static Object lock = new Object();
	
	static public Logger searchLogger;
	
	static public Logger searchUserLogger;
	
	static public Logger analysisLogger;
	
	static public Logger reqLogger;
	
	static public Logger loadLogger;	
	
	static public Logger geotoolsLogger;
	
	public static Logger getLoadLog() {
		synchronized(lock) {
			if (loadLogger == null) {
				loadLogger = LoggerFactory.getLogger("load_log");
			}
		}
		return loadLogger;
	}

	public static Logger getSearchLog() {
		synchronized(lock) {
			if (searchLogger == null) {
				searchLogger = LoggerFactory.getLogger("search_log");
			}
		}
		return searchLogger;
	}
	
	public static Logger getSearchUserLog() {
		synchronized(lock) {
			if (searchUserLogger == null) {
				searchUserLogger = LoggerFactory.getLogger("search_user_log");
			}
		}
		return searchUserLogger;
	}

	
	public static Logger getReqLog() {
		synchronized(lock) {
			if (reqLogger == null) {
				reqLogger = LoggerFactory.getLogger("req_log");
			}
		}
		return reqLogger;
	}
	
	public static Logger getAnalysisLog() {
		synchronized(lock) {
			if (analysisLogger == null) {
				analysisLogger = LoggerFactory.getLogger("analysis_log");
			}
		}
		return analysisLogger;
	}
	
	
	public static Logger getGeotoolsLog() {
		synchronized(lock) {
			if (geotoolsLogger == null) {
				geotoolsLogger = LoggerFactory.getLogger("org.geotools");
			}
		}
		return geotoolsLogger;
	}
	
	/**
	 * FileLayer 관련 로그
	 * @return
	 */
	public static Logger getFileLog() {
		synchronized(lock) {
			if (FILELOG == null) {
				FILELOG = LoggerFactory.getLogger("file_log");
			}
		}
		return FILELOG;
	}
	
	/**
	 * 서비스 관련 로그
	 * @return
	 */
	public static Logger getSCLog() {
		if (SERVICELOG == null) {
			SERVICELOG = LoggerFactory.getLogger("service_log");
		}

		return SERVICELOG;
	}
	
	/**
	 * 서비스 디버그 정보 저장
	 * @param log
	 */
	public static void debug(String log) {
		getSCLog().debug(log);
	}
	

	/**
	private static Logger getLogger(String filename, String loggername, Level level,
			String maxFileSize, int maxFileIndex) {

		return getLogger(filename, loggername, level, maxFileSize, maxFileIndex,
		"[%d{yy-MM-dd HH:mm:ss}] %r @ [%-5p] %C{5}.%M().LINE:%L - %m%n", MapLog.consolelog);

	}
	**/
	
	/**
	public static Logger getLogger(String filename, String loggername, Level level,
			String maxFileSize, int maxFileIndex, String pattern,
			boolean additivity) {

		Logger logger = null;

		try {
			Layout layout = new PatternLayout(pattern);
			logger = LogManager.getLogger(loggername);
			
			switch(MGR_APPENDER_TYPE){
				case 1 :
						//String datePattern = "'.'yyyy-MM-dd-HH-mm";
						String datePattern = "'.'yyyy-MM-dd";
						CustomDailyRollingFileAppender dailyAppender = new CustomDailyRollingFileAppender(layout, filename, datePattern);			
						dailyAppender.setMaxBackupIndex(maxFileIndex);
						dailyAppender.setThreshold(level);
						logger.addAppender(dailyAppender);
						
						break;
				default : 	
						RollingFileAppender appender = new RollingFileAppender(layout, filename, true);
						
						appender.setMaxFileSize(maxFileSize);
						appender.setMaxBackupIndex(maxFileIndex);
						appender.setThreshold(level);
						logger.addAppender(appender);
			}
			
			logger.setAdditivity(additivity);
			if(additivity) {
				ConsoleAppender consoleAppender = new ConsoleAppender(layout);
				logger.addAppender(consoleAppender);				
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return logger;
	}
	*/
	
//	public static String getInfo()
//	{
//		StringBuffer ret = new StringBuffer();
//		
//		ret.append("Logger").append('\n');
//
//		ret.append("ACCESSLOG: ").append(getASLog()).append('\n');
//		ret.append("SERVICELOG: ").append(getSCLog()).append('\n');
//		ret.append("SYSTEMLOG: ").append(getSTLog()).append('\n');
//		
//		return ret.toString();
//	}	
	

	
	public static void printDebugLog(String message) {		
		MapLog.getSCLog().debug(message);
	}

	public static Logger getDebugLog(){
		return MapLog.getSCLog();
	}
	
	public static Logger getErrlog(){
		return MapLog.getSCLog();
	}
	
	public static Logger getErrorLog(){
		return MapLog.getSCLog();
	}

	
	public static void main(String[] args){

	}
	
}
