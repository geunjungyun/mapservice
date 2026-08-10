package com.gis2.storage;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.io.FileUtils;

import org.slf4j.Logger;
//import com.gis.storage.TileDB;

//import org.slf4j.LoggerFactory;

//import org.apache.log4j.BasicConfigurator;
//import org.apache.log4j.Logger;
//import org.apache.log4j.PatternLayout;

import com.data.file.FileLayer;
//import com.data.util.CustomDailyRollingFileAppender;

import com.data.util.MapLog;
import com.gis.map.MData;
import com.gis.map.MapContext;
import com.gis.map.MapData;
import com.gis.map.TileMapFactory2;
import com.gis.map.style.BasicStyleExtend;
import com.gis.map.style.StyleFactory;
import com.gis.protocol.LevelConfigs;
import com.gis.protocol.MapInfo;
import com.gis.protocol.ScaleInfo;
import com.gis.protocol.ScaleInfos;
import com.gis.protocol.Styles;
import com.gis2.servlet.MapService;
import com.util.io.FileUt;


public class TileServiceMng implements ServletContextListener {
	
	public static String rasterPath = null;

	public static String nowPath = null;
	
	public static boolean hdMode = false;
	
	public static int colorCnt = 100;
	
	/*
	public static String savePath = null;
	
	public static String convPath = null;
	
	public static boolean isSaveServer = false;
	
	public static String saveServer = null;
	
	public static String saveResultUrl = null;
	*/
	
	
	
	//public static String previousPath = null;
	
	static ServletContext sc = null;
	//public static com.gis.protocol.freegis3.GisPlatform gpf = null;

	public static String defaultPath = "";

//	static Hashtable<String, MapData> mapDatas = new Hashtable();
//
//	static Hashtable<String, TileMapFactory2> tmss = new Hashtable();
	
	public static Hashtable<String, TileMapFactory2> emaps = new Hashtable();
	
	public static Hashtable<String, Version> versions = new Hashtable();
	
	static public boolean DEBUG = true;
	
	static public Logger searchLogger;
	
	static public Logger searchUserLogger;
	
	static public Logger analysisLogger;
	
	static public Logger reqLogger;
	
	static public Logger loadLogger;
	
	
	static public boolean isSearchLog = false;
	static public boolean isSearchUserLog = false;
	static public boolean isAnalysisLog = false;
	static public boolean isReqLog = false;
	static public boolean isLoadLog = false;
	
	
	static public State timg = new State();
	static public State tiles = new State();
	static public State search = new State();
	static public State code = new State();
	static public State analysis = new State();
	
	static public byte[] errorImageByte = null;
	
	//static public Vector<String> servers = new Vector();
	
	static public WMTSMNg wmts = new WMTSMNg();
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		this.release();
	}
	
	public static void release() {
		System.out.println("TileServiceMng  contextDestroy start");
		StorageMng.removeFileLayer();
		System.out.println("TileServiceMng  contextDestroy end");
	}
	
	public static void readEmap(String path) {
		File parent = new File(path);
		File[] files = parent.listFiles();
		for(File file : files) {
			if(file.isDirectory()) {
				if(!file.getName().equals("WEB-INF")) {
					
					TileMapFactory2 mng = null;
					if(file.getName().toLowerCase().indexOf("hd") > -1) {
						mng = TileMapFactory2.initEMapHD();
					}
					else {
						mng = TileMapFactory2.initEMap();
					}
					
					
//					if(file.getName().toLowerCase().indexOf("hd") > -1) {
//						mng.filePath = "F:\\tiles\\hd";	
//					}
//					else {
//						mng.filePath = "E:\\app\\jetty-distribution-9.4.35.v20201120\\webapps\\root\\sd";
//					}
					
					mng.filePath = file.getAbsolutePath();
					mng.setName(file.getName());
					mng.realTileMode = false;
					emaps.put(file.getName(), mng);
				}
			}
		}
	}

//	public static Hashtable<String, MapData> getMapDatas() {
//		return mapDatas;
//	}
//
//	public static Hashtable<String, TileMapFactory2> getTileMapFactorys() {
//		return tmss;
//	}

	
	public static Hashtable<String, TileMapFactory2> getTileMapFactorys() {
		return emaps;
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		//Properties enginProperty = new Properties();
		sc = arg0.getServletContext();
		this.init();
	}
	
	public void init() {
		Properties enginProperty = new Properties();
		System.out.println("TileServiceMng contextInitialized Loading");
		try {
			// Get Engine config path
			//String getContextPath = sc.getContextPath();

			String meta_info_path = null;
			
			if(sc != null) {
				meta_info_path = sc.getRealPath("WEB-INF");
			}
			
			String cur_dir = System.getProperty("user.dir");
			
			MapLog.getSCLog().debug("Properties read start");
			
			//MapLog.getSCLog().debug("jetty.home " + System.getProperty("jetty.home"));
			
			//System.out.println("jetty.home " + System.getProperty("jetty.home"));
			
			String WMTSCapabilitiesPath = cur_dir + File.separator + "map_service" + File.separator + "WMTSCapabilities.xml";
			MapLog.getSCLog().debug("WMTSCapabilities.xml path=" + WMTSCapabilitiesPath);
			TileServiceMng.wmts.readXml(WMTSCapabilitiesPath);

			String service_configPath = cur_dir + File.separator + "map_service" + File.separator + "service_config.properties";
			
			File path = new File(service_configPath);

			//System.out.println("Properties read start");

			if (path.exists()) {
				FileInputStream fiss = new FileInputStream(path);
				enginProperty.load(fiss);
				meta_info_path = cur_dir + File.separator + "map_service";
				MapLog.getSCLog().debug("service_configPath path=" + service_configPath);
				// PropertyMng.getInstance(path.getAbsolutePath());
			} else {
				FileInputStream fiss = new FileInputStream(
						meta_info_path + File.separator + "service_config.properties");
				enginProperty.load(fiss);

				// PropertyMng.getInstance(meta_info_path+File.separator +
				// "service_config.properties");
			}
			MapLog.getSCLog().debug("Properties read end");

			nowPath = enginProperty.getProperty("now", "");
			//previousPath = enginProperty.getProperty("previous", "");
			MapLog.getSCLog().debug("now="+nowPath);
			
			
			
			/*
			String isSaveServerS = enginProperty.getProperty("isSaveServer", "");
			
			if(isSaveServerS != null && isSaveServerS.equals("true")) {
				isSaveServer = true;
			}
			else {
				isSaveServer = false;
			}
			MapLog.getSCLog().debug("isSaveServer="+isSaveServer);
			
			String saveServerS = enginProperty.getProperty("saveServer", "");
			
			saveServer = saveServerS;
			
			MapLog.getSCLog().debug("saveServer="+saveServer);
			
			savePath = enginProperty.getProperty("savePath", "");

			MapLog.getSCLog().debug("savePath="+savePath);
			
			convPath = enginProperty.getProperty("convPath", "");

			MapLog.getSCLog().debug("convPath="+convPath);
			
			saveResultUrl = enginProperty.getProperty("saveResultUrl", "");
			
			MapLog.getSCLog().debug("saveResultUrl="+saveResultUrl);
			*/
			
			
			
			rasterPath = enginProperty.getProperty("rasterPath", "");
			MapLog.getSCLog().debug("rasterPath="+rasterPath);

			String dllPath = enginProperty.getProperty("dll", "");
			MapLog.getSCLog().debug("dll="+dllPath);
			String emapPath = enginProperty.getProperty("emapPath", "");
			MapLog.getSCLog().debug("emapPath="+emapPath);
			if(emapPath != null && emapPath.trim().length() > 0) {
				System.out.println("emapPath = " + emapPath);
				readEmap(emapPath);
			}

			
			String hdMode = enginProperty.getProperty("hdMode", "");
			MapLog.getSCLog().debug("hdMode="+hdMode);
			
			if(hdMode != null && hdMode.trim().equals("true")) {
				TileServiceMng.hdMode = true;
			}
			
			
			String colorCnt = enginProperty.getProperty("colorCnt", "");
			MapLog.getSCLog().debug("colorCnt="+colorCnt);
			
			if(colorCnt != null && colorCnt.trim().length() > 0) {
				TileServiceMng.colorCnt = Integer.parseInt(colorCnt);
			}
		
			
			String fontPath = enginProperty.getProperty("fontPath");
			MapLog.getSCLog().debug("fontPath="+fontPath);
			if(fontPath != null && fontPath.length() > 0) {
				StorageMng.fontPath = fontPath;
			}
			
			String logPath = enginProperty.getProperty("logPath", "");
			MapLog.getSCLog().debug("logPath="+logPath);
			
			
			String isSaveServerS = enginProperty.getProperty("isSaveServer", "");
			if(isSaveServerS != null && isSaveServerS.trim().equals("false")) {
				String errorImage = enginProperty.getProperty("errorImage", "");
				MapLog.getSCLog().debug("errorImage="+errorImage);
				File errorFile = new File(errorImage);
				TileServiceMng.errorImageByte = this.readWithStream(errorImage);
			}

			this.initDll(dllPath);
			
			
			String[] paths = nowPath.split("\\|");
			
			for(String pt : paths) {
				this.initVersions(pt);
			}
			
			/*
			String serverS = enginProperty.getProperty("servers", "");
			
			String[] serverSS = serverS.split("\\|");
			
			for(String server:serverSS) {
				servers.add(server);
			}
			*/
//			String[] prePaths = previousPath.split("&");
//			for(int i=0; i<prePaths.length; i++) {
//				this.initVersion(prePaths[i]);
//			}
			
			if(logPath != null && logPath.length() > 0) {
				
				String value = enginProperty.getProperty("isSearchLog", "");
				if(value != null && value.trim().toLowerCase().equals("true")) {
					isSearchLog = true;
				}
				
				value = enginProperty.getProperty("isSearchUserLog", "");
				if(value != null && value.trim().toLowerCase().equals("true")) {
					isSearchUserLog = true;
				}

				
				value = enginProperty.getProperty("isAnalysisLog", "");
				if(value != null && value.trim().toLowerCase().equals("true")) {
					isAnalysisLog = true;
				}
				
				value = enginProperty.getProperty("isReqLog", "");
				if(value != null && value.trim().toLowerCase().equals("true")) {
					isReqLog = true;
				}
				
				value = enginProperty.getProperty("isLoadLog", "");
				if(value != null && value.trim().toLowerCase().equals("true")) {
					isLoadLog = true;
				}
				this.initLog(logPath);
			}
			
			//System.out.println(previousPath);
			
			if(isLoadLog) {
				
				int intervalMin = 5;
				
				String value = enginProperty.getProperty("loadLogInterval", "");
				if(value != null ) {
					intervalMin = Integer.parseInt(value);
				}
				
				this.initTimer(intervalMin);
			}
			
			

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public byte[] readWithStream(String filePath) {
        File file = new File(filePath);
        
        // try-with-resources 구문 (Java 7 이상 지원)
        // 블록을 빠져나가면 fis와 bos가 자동으로 close 됩니다.
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            
            byte[] buffer = new byte[1024];
            int len;
            
            // 버퍼 단위로 읽어서 ByteArrayOutputStream에 씀
            while ((len = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            
            return bos.toByteArray();
            
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
	
	
	public void initLog(String path) {
		if(isSearchLog) {
			initSearchLog(path);
		}
		if(isAnalysisLog) {
			initAnalysisLog(path);
		}
		if(isReqLog) {
			initReqLog(path);
		}
		
		if(isLoadLog) {
			initLoadLog(path);
		}
		
		if(isSearchUserLog) {
			initSearchUserLog(path);
		}

	}
	
	public void initLoadLog(String path) {
		this.loadLogger = MapLog.getLoadLog();
	}
	
	
	public void initSearchLog(String path) {
		this.searchLogger = MapLog.getSearchLog();
	}
	
	public void initSearchUserLog(String path) {
		this.searchUserLogger = MapLog.getSearchUserLog();
	}
	
	public void initReqLog(String path) {
		this.reqLogger = MapLog.getReqLog();
	}

	
	public void initAnalysisLog(String path) {
		this.analysisLogger = MapLog.getAnalysisLog();
	}

	
	static public void reInit() {
		versions.clear();
		
		
		String[] paths = nowPath.split("\\|");
		
		for(String pt : paths) {
			initVersions(pt);
		}
		//initVersions(nowPath);
		//initVersion(previousPath);
		
		System.out.println("reInit End");
	}
	
	static public void reloadTile() {
		System.out.println("reloadTile()");
		Set set = versions.keySet();
		Iterator it = set.iterator();
		while(it.hasNext()) {
			Object key  = it.next();
			Version ver = versions.get(key);
			Hashtable<String, TileMapFactory2> tmss = ver.getTMSS();
			Set set1 = tmss.keySet();
			Iterator it1 = set1.iterator();
			while(it1.hasNext()) {
				Object key1 = it1.next();
				TileMapFactory2 tmm = tmss.get(key1);
				
				TileDB tdb = tmm.getTileDB();
				
				if(tdb == null) {
					tmm.setDBPath(tmm.dbPath, true, false);
					tdb = tmm.getTileDB();
				}
				
				if(tdb == null) {
					continue;
				}
				
				File newFile = tdb.getNewVersion();
				if(newFile != null) {
					
					System.out.println("new tile db = " + newFile.getAbsolutePath());
					long st = System.currentTimeMillis();
					
					tdb.close();
					File dbFile = new File(tdb.path);
					
					File back = new File(tdb.path+File.separator+"bak");
					File backTile = new File(tdb.path+File.separator+"bak"+File.separator+dbFile.getName());
					
					//System.out.println("backup tile db = " + backTile.getAbsolutePath());
					
					try {
						FileUtils.forceMkdir(back);
						FileUtils.forceMkdir(backTile);
						File[] bins = dbFile.listFiles();
						for(File bin : bins) {
							if(!bin.isDirectory()) {
								FileUtils.moveFile(bin, new File(tdb.path+File.separator+"bak"
								+File.separator+dbFile.getName()+File.separator+bin.getName()));
							}
						}
						
						File[] newFiles = newFile.listFiles();
						
						for(File bin : newFiles) {
							if(!bin.isDirectory()) {
								FileUtils.moveFile(bin, new File(tdb.path+File.separator+bin.getName()));
							}
						}
						
						tmm.setDBPath(tmm.dbPath, true, false);
						//tdb.init();
						
						File newD = newFile.getParentFile();
						
						FileUtils.deleteDirectory(newD);
						
						long et = System.currentTimeMillis();
						
						System.out.println("change complete : tiledb = " + tmm.dbPath + ", backup tiledb = "+backTile.getAbsoluteFile()+", time=" + (et - st)/1000);
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			}
		}
		
		
		Set set1 = StorageMng.fls.keySet();
		
		Iterator it1 = set1.iterator();
		
		while(it1.hasNext()) {
			Object key = it1.next();
			FileLayer fl = StorageMng.fls.get(key);
			
			File pathFile = fl.getPath();
			
			File[] subFiles = pathFile.listFiles();
			
			for(File subFile : subFiles) {
				if(subFile.isDirectory() && subFile.getName().equals("new")) {
					
					File[] layers = subFile.listFiles();
					
					for(File layer : layers) {
						if(layer.isDirectory() && layer.getName().equals(pathFile.getName())) {
							FileLayer newfl = null;
							try {
								newfl = new FileLayer(layer.getAbsolutePath(), null);
								
								if(newfl != null) {
									long st = System.currentTimeMillis();
									//System.out.println("new layer = " + layer.getAbsolutePath());
									newfl.close();
									
									File bakFile = new File(pathFile.getAbsoluteFile()+File.separator+"bak"+File.separator+pathFile.getName());
									//System.out.println("backup layer = " + bakFile.getAbsolutePath());
									fl.close();
									
									FileUtils.moveDirectory(new File(pathFile.getAbsoluteFile()+File.separator+"geoindex"), 
											new File(bakFile.getAbsoluteFile()+File.separator+"geoindex"));
									
									FileUtils.moveDirectory(new File(pathFile.getAbsoluteFile()+File.separator+"data"), 
											new File(bakFile.getAbsoluteFile()+File.separator+"data"));
									
									FileUtils.moveFile(new File(pathFile.getAbsoluteFile()+File.separator+"layerInfo.xml"), 
											new File(bakFile.getAbsoluteFile()+File.separator+"layerInfo.xml"));

									FileUtils.moveDirectory(new File(layer.getAbsoluteFile()+File.separator+"geoindex"), 
											new File(pathFile.getAbsoluteFile()+File.separator+"geoindex"));
									
									FileUtils.moveDirectory(new File(layer.getAbsoluteFile()+File.separator+"data"), 
											new File(pathFile.getAbsoluteFile()+File.separator+"data"));
									
									FileUtils.moveFile(new File(layer.getAbsoluteFile()+File.separator+"layerInfo.xml"), 
											new File(pathFile.getAbsoluteFile()+File.separator+"layerInfo.xml"));
									
									//fl.reOpenClose();
									fl.read(fl.getPath().getAbsolutePath());
									
									FileUtils.deleteDirectory(subFile);
									long et = System.currentTimeMillis();
									System.out.println("change complete : layer = " + fl.getPath().getAbsolutePath() 
											+", backup layer = " + bakFile.getAbsolutePath()+", time=" + (et - st)/1000);
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} 
							

						}
					}
					
				}
			}
		}
	}
	
	
	public void initDll(String path) {
	       String libraryPath = "";
	       String libraryName = "";
	       
			String osName = System.getProperty("os.name");
			
			if(osName.toLowerCase().indexOf("win") > -1) {
				libraryName = "imagequant.dll";
				libraryPath = path+FileUt.SEPERATOR+"win64"+FileUt.SEPERATOR + libraryName;
			}
			else {
				libraryName = "libimagequant.so";
				libraryPath = path+FileUt.SEPERATOR+"linux64"+FileUt.SEPERATOR + libraryName;
			}
			File libraryFile = new File(libraryPath);
			
			if(libraryFile.exists()) {
				try {
					//System.loadLibrary("imagequant");
					System.load(libraryFile.getAbsolutePath());
				}
				catch(UnsatisfiedLinkError  e) {
					e.printStackTrace();
				}
				MapLog.getSCLog().debug("load library imagequant , "+libraryFile);
			}
			else {
				MapLog.getSCLog().debug("not load library imagequant , "+libraryFile);
			}
	}
	
	static public void initVersion(File file) {
		
		MapLog.getSCLog().debug("Thematic Map = "+file.getAbsolutePath());
		
		Version ver = new Version();
		if(!file.isDirectory()) {
			return;
		}
		
		boolean newMode = false;
		
		//String versionName = file.getName();
		
		ver.name = file.getName();
		
		
		Styles styles = null;
		
		com.gis.protocol.freegis3.Styles newStyle = null;
		
		String layerPath = "";
		
		File[] subFile  = file.listFiles();
		
		for(File style : subFile) {
			if(style.getName().equals("tiles")) {
				try {
					
					File styleFile = new File(style.getAbsolutePath()+FileUt.SEPERATOR+"emp_style.xml");
					
					if(styleFile.exists()) {
						newMode = true;
					}
					if(newMode) {
						newStyle = ServiceConfig.readNewStyles(style.getAbsolutePath()+FileUt.SEPERATOR+"emp_style.xml");
					}
					else {
						styles = ServiceConfig.readStyles(style.getAbsolutePath()+FileUt.SEPERATOR+"style.xml");
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(style.getName().equals("layers")) {
				layerPath = style.getAbsolutePath();
				ver.layerPath = layerPath;
				if(newMode) {
					layerPath = style.getAbsolutePath()+"_";
					ver.layerPath = layerPath;
				}
			}
		}
		
		for(File style : subFile) {
			if(style.isDirectory()) {
				File[] tiles = style.listFiles();
				for(File tile : tiles) {
					String tilePath = tile.getAbsolutePath();
					
					
					File mapInfoF = new File(tilePath+FileUt.SEPERATOR+"mapInfo.xml");
					
					if(newMode) {
						mapInfoF = new File(tilePath+FileUt.SEPERATOR+"mapInfo_.xml");
					}
					
					
					if(mapInfoF.exists()) {
						try {
							MapInfo mapInfo = ServiceConfig.readMapInfo(mapInfoF.getAbsolutePath());
							//StorageMng.symbolPath =  _path+FileUt.SEPERATOR+ver.name + FileUt.SEPERATOR+"tiles"+FileUt.SEPERATOR+"symbols";
							StorageMng.symbolPath =  file.getAbsolutePath() + FileUt.SEPERATOR+"tiles"+FileUt.SEPERATOR+"symbols";
							
							ScaleInfos sis = mapInfo.getScaleInfos();
							
							//MData md = null;
							
							//MapData map = new MapData(mapInfo.getName(), sis, mapInfo.getLevelConfigs(), styles);
							MData map = null;
							
							if(newMode) {
								
								newStyle.setSymbolPath(file.getAbsolutePath() + FileUt.SEPERATOR+"tiles"+FileUt.SEPERATOR+"symbols");
								
								map = new com.gis2.map.MapData(mapInfo.getName(), sis, mapInfo.getLevelConfigs(), newStyle);
								
								com.gis2.map.MapData mapData = (com.gis2.map.MapData)map;
								mapData.layerPath = layerPath+FileUt.SEPERATOR;
								mapData.layerPath = layerPath+"_"+FileUt.SEPERATOR;
								
								mapData.rasterPath = rasterPath+FileUt.SEPERATOR;

								mapData.loadLayers();
								
							}
							else {
								
								styles.setSymbolPath(file.getAbsolutePath() + FileUt.SEPERATOR+"tiles"+FileUt.SEPERATOR+"symbols");
								map = new MapData(mapInfo.getName(), sis, mapInfo.getLevelConfigs(), styles);
								
								MapData mapData = (MapData)map;
								
								if(mapInfo.getMbrExtend() == null) {
									mapData.setMbrExtend(200);
								}
								else {
									mapData.setMbrExtend(mapInfo.getMbrExtend());
								}
								
								mapData.layerPath = layerPath+FileUt.SEPERATOR;
								mapData.rasterPath = rasterPath+FileUt.SEPERATOR;
								mapData.readFileLayers();
								ver.addMapData(mapInfo.getName(), mapData);
							}
							
							
							String dbPath = tilePath;//+FileUt.SEPERATOR+mapInfo.getName();
							
							
							TileMapFactory2 tmf = new TileMapFactory2(mapInfo.getName(), sis, dbPath);
							
							tmf.setMapData(map);
							
							ver.addTileMapFactory(mapInfo.getName(), tmf);
							
							
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
		
		Set set = ver.jiguNames.keySet();
		Iterator it = set.iterator();
		while(it.hasNext()) {
			String layerName = (String)it.next();
			FileLayer fl = StorageMng.getFileLayer(ver.layerPath + FileUt.SEPERATOR + layerName, false);
			
			
			if(fl.getSimpleFeatureType().indexOf("WTNNC_SN".toLowerCase()) > -1) {
				ver.pnuAnalysisLayers.add(layerName);
				MapLog.getSCLog().debug("pnuAnalysisLayer add tileSet="+ver.name+", layer="+layerName);
				
			}
		}
		
		if(ver.layerPath != null && ver.layerPath.trim().length() > 0) {
			readLayers(ver);
		}
		
		TileServiceMng.versions.put(ver.name, ver);

	}
	
	
	static public void initVersions(String _path) {
		
		File path = new File(_path);
		
		if(!path.exists()) {
			MapLog.getSCLog().debug("not exist version = " + _path);
			return;
		}
		
		File[] files = path.listFiles();
		
		for(File file : files) {
			Version ver = new Version();
			if(!file.isDirectory()) {
				continue;
			}
			initVersion(file);
		}
		
	}
	
	
	public static void readLayers(Version ver) {
		ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);

		File layerPath = new File(ver.layerPath);
		
		
		File[] files = layerPath.listFiles();
		Vector<Future> fts = new Vector();
		for(File file : files) {
			if(file.isDirectory()) {
				
				Future ft = executorService.submit(() -> {
					try {
						FileLayer reader = null;

						if(reader == null) {
							//System.out.println(file.getAbsolutePath());
							reader = StorageMng.getFileLayer(file.getAbsolutePath(), false);
							ver.layers.put(file.getName(), reader);
						}
						
					} catch (Exception ex) {
						throw new Exception(ex);
					}
					return 1;
				});
				fts.add(ft);

				
			}
		}
		

		for (Future ft : fts) {
			try {
				ft.get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		executorService.shutdown();

	}
	
	
	
	public void initTimer(int min ) {
		
		float interval = 60*min;
		
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            int cnt = 0;
            @Override
            public void run() {
            	DecimalFormat form = new DecimalFormat("#.####");

            	loadLogger.debug("---------------------------------------------------------------");
            	synchronized (TileServiceMng.timg) {
            		
            		float avTime = 0;
            		float avSize = 0;
            		
            		float maxTime = 0;
            		float maxSize = 0;
            		
            		int tps = 0;
            		if(timg.cnt > 0) {
						
            			avTime = (timg.time/timg.cnt)/1000.0f;
						avSize = (timg.size/timg.cnt)/1024.0f;
						tps = (int) (timg.cnt/interval);
						
						maxTime = timg.maxTime/1000.0f;
						maxSize = timg.maxSize/1024.0f;
            		}
					timg.time = 0;
					timg.size = 0;
					timg.cnt = 0;
					timg.maxSize = Long.MIN_VALUE;
					timg.maxTime = Long.MIN_VALUE;
					loadLogger.debug("timg, avTime = "+form.format(avTime)+" sec, maxTime = " + form.format(maxTime) +" sec, "
							+ "avSize = "+form.format(avSize)+" kbyte, maxSize = " + form.format(maxSize)+" kbyte, "
							+ "tps = " + tps +", total = " + timg.totalCnt);
            	}
				synchronized (TileServiceMng.tiles) {
            		float avTime = 0;
            		float avSize = 0;
            		int tps = 0;
            		float maxTime = 0;
            		float maxSize = 0;
            		if(tiles.cnt > 0) {
						avTime = (tiles.time/tiles.cnt)/1000.0f;
						avSize = (tiles.size/tiles.cnt)/1024.0f;
						tps = (int) (tiles.cnt/interval);
						maxTime = tiles.maxTime/1000.0f;
						maxSize = tiles.maxSize/1024.0f;
            		}
					tiles.time = 0;
					tiles.size = 0;
					tiles.cnt = 0;
					tiles.maxSize = Long.MIN_VALUE;
					tiles.maxTime = Long.MIN_VALUE;

					loadLogger.debug("tiles, avTime = "+form.format(avTime)+" sec, maxTime = " + form.format(maxTime) +" sec, "
							+ "avSize = "+form.format(avSize)+" kbyte, maxSize = " + form.format(maxSize)+" kbyte, "
							+ "tps = " + tps +", total = " + tiles.totalCnt);
				}
				synchronized (TileServiceMng.search) {
            		float avTime = 0;
            		float avSize = 0;
            		int tps = 0;
            		float maxTime = 0;
            		float maxSize = 0;
					if(search.cnt > 0) {
						avTime = ((search.time/search.cnt))/1000.0f;
						avSize = ((search.size/search.cnt))/1024.0f;
						tps = (int) (search.cnt/interval);
						maxTime = search.maxTime/1000.0f;
						maxSize = search.maxSize/1024.0f;
					}
					search.time = 0;
					search.size = 0;
					search.cnt = 0;
					search.maxSize = Long.MIN_VALUE;
					search.maxTime = Long.MIN_VALUE;					
					loadLogger.debug("search, avTime = "+form.format(avTime)+" sec, maxTime = " + form.format(maxTime) +" sec, "
							+ "avSize = "+form.format(avSize)+" kbyte, maxSize = " + form.format(maxSize)+" kbyte, "
							+ "tps = " + tps +", total = " + search.totalCnt);
				}
				synchronized (TileServiceMng.code) {
            		float avTime = 0;
            		float avSize = 0;
            		int tps = 0;
            		float maxTime = 0;
            		float maxSize = 0;            		
            		if(code.cnt > 0) {
						avTime = ((code.time/code.cnt))/1000.0f;
						avSize = ((code.size/code.cnt))/1024.0f;
						tps = (int) (code.cnt/interval);
						maxTime = code.maxTime/1000.0f;
						maxSize = code.maxSize/1024.0f;
            		}
					code.time = 0;
					code.size = 0;
					code.cnt = 0;	
					code.maxSize = Long.MIN_VALUE;
					code.maxTime = Long.MIN_VALUE;					
					loadLogger.debug("code, avTime = "+form.format(avTime)+" sec, maxTime = " + form.format(maxTime) +" sec, "
							+ "avSize = "+form.format(avSize)+" kbyte, maxSize = " + form.format(maxSize)+" kbyte, "
							+ "tps = " + tps +", total = " + code.totalCnt);				}
				synchronized (TileServiceMng.analysis) {
            		float avTime = 0;
            		float avSize = 0;
            		int tps = 0;
            		float maxTime = 0;
            		float maxSize = 0;                		
            		if(analysis.cnt > 0) {
						avTime = ((analysis.time/analysis.cnt))/1000.0f;
						avSize = ((analysis.size/analysis.cnt))/1024.0f;
						tps = (int) (analysis.cnt/interval);
						maxTime = analysis.maxTime/1000.0f;
						maxSize = analysis.maxSize/1024.0f;						
            		}
					analysis.time = 0;
					analysis.size = 0;
					analysis.cnt = 0;
					analysis.maxSize = Long.MIN_VALUE;
					analysis.maxTime = Long.MIN_VALUE;						
					loadLogger.debug("analysis, avTime = "+form.format(avTime)+" sec, maxTime = " + form.format(maxTime) +" sec, "
							+ "avSize = "+form.format(avSize)+" kbyte, maxSize = " + form.format(maxSize)+" kbyte, "
							+ "tps = " + tps +", total = " + analysis.totalCnt);
				}
            	
            }
        };
        timer.schedule(timerTask,(int)(1000*interval),(int)(1000*interval));
	}

	public static Vector<String> getLocalServerIps() {
		try {
			Vector<String> addressesString = new Vector();
			Vector<InetAddress> addresses = new Vector();
			
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()
							&& inetAddress.isSiteLocalAddress()) {
						addresses.add(inetAddress);
						//return inetAddress.getHostAddress().toString();
					}
				}
			}
			
			for (InetAddress address: addresses) {
				try {
					addressesString.add(address.getHostAddress());
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
			
			if (addressesString.size() < 1) {
				return null;
			}
			
			return addressesString;
			
		} catch (SocketException ex) {
		}
		return null;
	}
	
	
	
	public static String getLocalServerIp() {
		
		String localIp = "";
		
		Vector<String> ips = TileServiceMng.getLocalServerIps();
		
		for(String ip: ips) {
			
			for(String server : AuthorityMng.conServers) {
				String[] ss = server.split(":");
	            if(ss[0].equals(ip)) {
	            	localIp = ip;
	            	break;
	            }
			}
			if(localIp.length() > 0) {
				break;
			}
		}

		return localIp;
	}
	

	
	
}
