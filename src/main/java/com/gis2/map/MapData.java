package com.gis2.map;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.referencing.CRS;
import org.jaitools.numeric.RangeExtendedComparator.Result;

//import com.gis.engine.datamanager.umdfile.UMDBlock;
//import com.gis.engine.datamanager.umdfile.UMDReader;
//import com.gis.engine.util.FileUt;
import com.gis2.map.style.BasicStyleExtend;
import com.gis2.map.style.QueryStyle;
import com.gis2.map.style.StyleFactory;
import com.data.file.FileLayer;
import com.data.util.MapLog;
import com.gis.map.Layer;
import com.gis.map.MData;
import com.gis2.map.MapContext;
import com.gis.projection.ScreenCoordUtil;
import com.gis.protocol.LayerInfo;
import com.gis.protocol.LevelConfig;
import com.gis.protocol.LevelConfigs;
import com.gis.protocol.ScaleInfo;
import com.gis.protocol.ScaleInfos;
import com.gis.protocol.freegis3.Styles;
import com.gis.protocol.freegis3.TextStyle;
import com.gis2.storage.StorageMng;
import com.gis2.storage.TileServiceMng;
import com.util2.thread.ThreadQueue;
import com.util2.thread.ThreadQueue.Listener;
import com.gis2.storage.ServiceConfig;
//import com.gis.xml.raster.PointStyleS;
//import com.gis.xml.raster.QueryS;
//import org.freegis.GisPlatform;
//import org.freegis.LayerInfo;
//import org.freegis.LevelConfig;
//import org.freegis.LevelConfigs;
//import org.freegis.MapInfo;
//import org.freegis.PointStyle;
//import org.freegis.PolygonStyle;
//import org.freegis.PolylineStyle;
//import org.freegis.Query;
//import org.freegis.ScaleInfo;
//import org.freegis.ScaleInfos;
//import org.freegis.Styles;
import org.locationtech.jts.geom.Envelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public class MapData implements Listener, MData {

	static public String trans = "";

	String name;

	public HashMap<String, Style> styles = null;

	HashMap<Integer, ScaleInfo> scaleInfo = new HashMap();

	public HashMap<Integer, LevelConfig> levelInfos = new HashMap();

	public HashMap<Integer, MapContext> mapContexts = new HashMap<Integer, MapContext>();

	StorageMng stm = null;

	Object lock = new Object();

	boolean largeFontMode = false;
	
	//private boolean reference = false;
	
	private boolean isProcess = false;
	
	boolean processReference = false;
	
	String tileServiceName = "";
	
	static HashMap<Integer, GenericObjectPool<BufferedImage>> imagePools = new HashMap<Integer, GenericObjectPool<BufferedImage>>();
	
	static Object lockObject = new Object();
	
	public String layerPath = "";
	
	public String rasterPath = "";
	

	public boolean isProcessReference() {
		return processReference;
	}
	
	public boolean isProcess() {
		return this.isProcess;
	}
	
	public void setProcess(boolean value) {
		this.isProcess = value;
	}

	public void setProcessReference(boolean isProcessReference) {
		this.processReference = isProcessReference;
	}

	public MapData(String name) {
		this.name = name;
		this.stm = null;

	}
	
	
	/*
	public void setReference(boolean reference) {
		this.reference = reference;
	}
	
	public boolean getReference() {
		return this.reference;
	}
	*/
	
	public void setTileServiceName(String tileServiceName) {
		this.tileServiceName = tileServiceName;
	}
	
	public String getTileServiceName() {
		return this.tileServiceName;
	}
	
	Styles style;

	public MapData(String name, ScaleInfos scaleInfos, LevelConfigs lcs, Styles _styles) {
		//style = _styles;
		
		this.name = name;
		this.stm = null;
		
		if(com.gis2.storage.TileServiceMng.hdMode) {
			List<ScaleInfo> siss = scaleInfos.getScaleInfo();
			for(ScaleInfo si : siss) {
				si.setPixelPerMeter(si.getPixelPerMeter()/2.0);
			}
		}

		this.setScaleInfos(scaleInfos);
		this.setLevelInfos(lcs);
		this.styles = new HashMap();

		// boolean largeFontMode = false;

		float fontSize = Integer.MIN_VALUE;
		List<com.gis.protocol.freegis3.PointStyle> pss = _styles.getPointStyle();
		for (com.gis.protocol.freegis3.PointStyle ps : pss) {
			TextStyle ts = ps.getTextStyle();
			if (ts != null) {
				float width = ts.getFontSize();

				if (ts.getWidthRatio() != null) {
					width += ts.getWidthRatio() * ts.getFontSize();
				}
				if (width > 40) {
					largeFontMode = true;
				}
			}
		}
		
		
		StyleFactory.loadStyles(_styles, this.styles);
		
		
		List<ScaleInfo> scales = scaleInfos.getScaleInfo();
		for (ScaleInfo si : scales) {
			MapContext mct = this.createMapContext(si.getId());

			if (mct != null && largeFontMode) {
				mct.largeFontMode = largeFontMode;
			}
			/*
			if (mct != null) {
				mct.setStyle(styles);
			}
			*/
		}
	}
	

	public String getName() {
		return this.name;
	}
	
	public HashMap<String, Style> getStyle(){
		return this.styles;
	}

	public MapData cloneMapData(LevelConfigs lcs) {
		MapData clone = new MapData(this.name);
		clone.styles = this.styles;
		clone.scaleInfo = this.scaleInfo;
		clone.stm = this.stm;
		clone.setLevelInfos(lcs);
		int k=0;
		clone.largeFontMode = this.largeFontMode;
		Set set = this.scaleInfo.keySet();
		Iterator it = set.iterator();
		while (it.hasNext()) {
			Integer levelId = (Integer) it.next();
			MapContext mct = clone.createMapContext(levelId);

			if (mct != null) {
				//mct.setStyle(styles);
				mct.largeFontMode = clone.largeFontMode;
			}
		}
		return clone;
	}

	public HashMap<String, Vector<SimpleFeature>> getObjects(int level, int x, int y) throws Exception {

		HashMap<String, Vector<SimpleFeature>> results = new HashMap();

		LevelConfig lc = this.levelInfos.get(level);

		for (LayerInfo li : lc.getLayerInfo()) {
			String name = li.getLayerName();
			
			FileLayer fl = this.stm.getFileLayer(name, false);
			
			SimpleFeatureCollection sfc = fl.getFeatures(x - 10, y - 10, x + 10, y + 10);
			
			SimpleFeatureIterator sfi = sfc.features();
			while(sfi.hasNext()) {
				SimpleFeature obj = sfi.next();
				
				Vector<SimpleFeature> objs = null;

				if (!results.containsKey(li.getLayerName())) {
					results.put(li.getLayerName(), new Vector<SimpleFeature>());
				}

				objs = results.get(li.getLayerName());

				if (obj != null) {
					objs.add((SimpleFeature) obj);
				}

			}
			
		}

		return results;
	}

	public int getLikeLevel(double scale) {

		double tempScale = Double.MAX_VALUE;
		int selectLevel = -1;

		Set<Integer> set = this.scaleInfo.keySet();
		Iterator<Integer> it = set.iterator();
		while (it.hasNext()) {
			Integer key = it.next();

			ScaleInfo si = this.scaleInfo.get(key);

			Double ptm = si.getPixelPerMeter();
			double tptm = Math.abs(ptm - scale);

			if (tptm < tempScale) {
				selectLevel = key;
				tempScale = tptm;
			}
		}
		return selectLevel;
	}
	
	/**
	 * 레벨 아이디를 입력해서 가장 가까운 레벨의 아이디를 리턴
	 * @param level
	 * @return
	 */
	public int getLikeLevel(int level) {

		double tempScale = Double.MAX_VALUE;
		int selectLevel = -1;

		Set<Integer> set = this.scaleInfo.keySet();
		Iterator<Integer> it = set.iterator();
		while (it.hasNext()) {
			Integer key = it.next();
			
			int temp = Math.abs(level - key);
			
			if (temp < tempScale) {
				selectLevel = key;
				tempScale = temp;
			}
		}
		return selectLevel;
	}

	
	public boolean isExistLevel(int levelId) {
		LevelConfig lcf = this.levelInfos.get(levelId);
		if(lcf != null) {
			return true;
		}
		return false;
	}

	public StorageMng getStorageMng() {
		return this.stm;
	}

	/*
	 * public void loadData(String xml){
	 * 
	 * GisPlatform gpf = null;
	 * 
	 * if(xml == null){ gpf = ServiceConfig.getConfig(null, "org.freegis2"); } else{
	 * gpf = ServiceConfig.getConfig(xml, "org.freegis2"); } if(this.stm == null){
	 * this.stm = new StorageMng(gpf); } MapInfo mapInfo = null; List<MapInfo>
	 * mapInfos = gpf.getMapInfo();
	 * 
	 * for(MapInfo info : mapInfos){ if(info.getName().equals(this.name)){ mapInfo =
	 * info; } }
	 * 
	 * this.setScaleInfos(mapInfo.getScaleInfos());
	 * this.loadLevelInfos(mapInfo.getLevelConfigs(), true);
	 * 
	 * }
	 */

//	public void setStyles(HashMap<String, BasicStyleExtend> styles){
//		this.styles = styles;
//	}

//	public HashMap<String, BasicStyleExtend> getStyles(){
//		return this.styles;
//	}

	/**
	 * 메모리 로딩 여부
	 * 
	 * @param memory
	 */

	/*
	 * public void loadData(boolean memory){
	 * 
	 * GisPlatform gpf =ServiceConfig.getConfig(null, "org.freegis2"); if(this.stm
	 * == null){ this.stm = new StorageMng(gpf); } MapInfo mapInfo = null;
	 * List<MapInfo> mapInfos = gpf.getMapInfo(); for(MapInfo info : mapInfos){
	 * if(info.getName().equals(this.name)){ mapInfo = info; } }
	 * 
	 * 
	 * this.setScaleInfos(mapInfo.getScaleInfos());
	 * this.loadLevelInfos(mapInfo.getLevelConfigs(), memory, null);
	 * 
	 * }
	 */

//	void loadStyles(Styles stls){
//		List<PointStyle> pss = stls.getPointStyle();
//		for(PointStyle ps : pss){
//			if(!this.styles.containsKey(ps.getName())){
//				BasicStyleExtend bse = StyleFactory.createPointStyle(ps, this.stm);
//				this.styles.put(bse.getName(), bse);
//			}
//		}
//		
//		for(PointStyle ps : pss){
//			List<Query> querys = ps.getQuery();
//			if(querys != null && querys.size() > 0){
//				BasicStyleExtend bse = this.styles.get(ps.getName());
//				Vector<QueryStyle> bseVector = new Vector();
//				for(Query query : querys){
//					QueryStyle qs = new QueryStyle();
//					qs.name = query.getName();
//					qs.value = query.getValue();
//					BasicStyleExtend subBse = this.styles.get(query.getStyleName());
//					if (subBse == null && query.getPriority() == -1) {
//						System.out.println("PointStyle name=" + ps.getName() + ", subInfo name = " + query.getName() + ", value = " + query.getValue() + ", style = "
//								+ query.getStyleName());
//						subBse = bse;
//					} else {
//						com.gis.map.style.PointStyle baseClone = (com.gis.map.style.PointStyle) bse.clone();
//	
//						if (query.getPriority() == -1) {
//							baseClone.setCompare(subBse);
//							subBse = baseClone;
//						} else if (query.getStyleName() != null && query.getStyleName().trim().length() > 0) {
//							baseClone.setCompare(subBse);
//							baseClone.priority = query.getPriority();
//							subBse = baseClone;
//						} else {
//							baseClone.priority = query.getPriority();
//							subBse = baseClone;
//						}
//					}
//					qs.style = subBse;
//					bseVector.add(qs);
//				}
//				bse.setQuerys(bseVector);
//			}
//			
//		}
//		
//		
//		List<PolygonStyle> pls = stls.getPolygonStyle();
//		for(PolygonStyle pl : pls){
//			if(!this.styles.containsKey(pl.getName())){
//				BasicStyleExtend bse = StyleFactory.createPolygonStyle(pl, this.stm);
//				this.styles.put(bse.getName(), bse);
//			}
//		}
//		
//		List<PolylineStyle> pgs = stls.getPolylineStyle();
//		for(PolylineStyle pg : pgs){
//			if(!this.styles.containsKey(pg.getName())){
//				BasicStyleExtend bse = StyleFactory.createPolylineStyle(pg, this.stm);
//				
//				this.styles.put(bse.getName(), bse);
//			}
//		}
//	}

	public void setStyles(HashMap<String, Style> nStyles) {
		this.styles = nStyles;
	}

	/*
	 * public void loadStyles(Styles stls, HashMap<String, BasicStyleExtend>
	 * nStyles){ List<PointStyle> pss = stls.getPointStyle(); for(PointStyle ps :
	 * pss){ if(!nStyles.containsKey(ps.getName())){ BasicStyleExtend bse =
	 * StyleFactory.createPointStyle(ps); nStyles.put(bse.getName(), bse); } }
	 * 
	 * for(PointStyle ps : pss){ List<Query> querys = ps.getQuery(); if(querys !=
	 * null && querys.size() > 0){ BasicStyleExtend bse = nStyles.get(ps.getName());
	 * Vector<QueryStyle> bseVector = new Vector(); for(Query query : querys){
	 * QueryStyle qs = new QueryStyle(); qs.name = query.getName(); qs.value =
	 * query.getValue(); BasicStyleExtend subBse =
	 * nStyles.get(query.getStyleName()); if (subBse == null && query.getPriority()
	 * == -1) { System.out.println("PointStyle name=" + ps.getName() +
	 * ", subInfo name = " + query.getName() + ", value = " + query.getValue() +
	 * ", style = " + query.getStyleName()); subBse = bse; } else {
	 * 
	 * } qs.style = subBse; bseVector.add(qs); } bse.setQuerys(bseVector); }
	 * 
	 * }
	 * 
	 * 
	 * List<PolygonStyle> pls = stls.getPolygonStyle(); for(PolygonStyle pl : pls){
	 * if(!nStyles.containsKey(pl.getName())){ BasicStyleExtend bse =
	 * StyleFactory.createPolygonStyle(pl); nStyles.put(bse.getName(), bse); } }
	 * 
	 * List<PolylineStyle> pgs = stls.getPolylineStyle(); for(PolylineStyle pg :
	 * pgs){ if(!nStyles.containsKey(pg.getName())){ BasicStyleExtend bse =
	 * StyleFactory.createPolylineStyle(pg);
	 * 
	 * nStyles.put(bse.getName(), bse); } } }
	 */
	public void setScaleInfos(ScaleInfos scaleInfo) {
		this.scaleInfo.clear();
		List<ScaleInfo> sis = scaleInfo.getScaleInfo();
		for (ScaleInfo si : sis) {
			this.scaleInfo.put(si.getId(), si);
		}
	}

	public void setLevelInfos(LevelConfigs levelInfos_) {

		// ExecutorService executorService = Executors.newFixedThreadPool(1);
		this.levelInfos.clear();
		
		if(levelInfos_ == null || levelInfos_.getLevelConfig() == null ||levelInfos_.getLevelConfig().size() == 0) {
			return;
		}
		
		List<LevelConfig> lls = levelInfos_.getLevelConfig();
		for (LevelConfig ll : lls) {
			
			
			List<LayerInfo> lis = ll.getLayerInfo();
			
			for(LayerInfo li : lis) {
				String layerName = li.getLayerName();
				
				if(!layerName.startsWith("tileMapService")) {
					int idx = layerName.lastIndexOf(":");
					if(idx != -1) {
						String start = layerName.substring(0, idx);
						String end = layerName.substring(idx, layerName.length());
						li.setLayerName(start+end.toLowerCase());
					}
					else {
						li.setLayerName(layerName.toLowerCase());
					}
				}
			}
			
			this.levelInfos.put(ll.getLevelId(), ll);
			//List<LayerInfo> lis = ll.getLayerInfo();
		}

	}
	
	public void setReferenceLayerName(LevelConfigs levelInfos) {
		List<LevelConfig> lls = levelInfos.getLevelConfig();
		for (LevelConfig ll : lls) {
			List<LayerInfo> lis = ll.getLayerInfo();
			for(LayerInfo li : lis) {
				String layerName = li.getLayerName();
				if(!layerName.startsWith("tileMapService")) {
					int idx = layerName.lastIndexOf(":");
					String start = layerName.substring(0, idx);
					String end = layerName.substring(idx, layerName.length());
					li.setLayerName(start+end.toLowerCase());

				}
			}
			this.levelInfos.put(ll.getLevelId(), ll);
			
			MapContext map = this.mapContexts.get(ll.getLevelId());
			
			for (Layer layer: map.getLayers()) {
				if (layer instanceof VectorLayer) {
					VectorLayer vl = (VectorLayer) layer;

				
					String onlylayerName = vl.getName().substring(vl.getName().lastIndexOf(":")+1, vl.getName().length());
					
					for(LayerInfo li : lis) {
						String[] names = li.getLayerName().split(":");
						String layerN = names[names.length-1];
						//if(li.getLayerName().lastIndexOf(onlylayerName) > -1) {
						if(layerN.equals(onlylayerName)) {	
							vl.setName(li.getLayerName());
							
							break;
						}
						
					}
				}
			}
			
		}
		
	}

	public void loadLayers() throws Exception {

		HashMap<String, String> loadLayers = new HashMap();
		HashMap<String, String> rasterLayers = new HashMap();

		Set<Integer> set = this.mapContexts.keySet();
		Iterator<Integer> it = set.iterator();
		while (it.hasNext()) {
			Integer key = it.next();
			MapContext map = this.mapContexts.get(key);
			Vector<Layer> layers = map.getLayers();
			for (Layer layer : layers) {
				
				if (layer instanceof VectorLayer) {
					loadLayers.put(layer.getName(), layer.getName());
				}
				if (layer instanceof RasterLayer) {
					rasterLayers.put(layer.getName(), layer.getName());
				}
			}
		}

		ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);

		int layerCnt = loadLayers.size() + rasterLayers.size();
		int nowCnt = 1;
		
		Vector<Future> fts = new Vector();
		Set set1 = loadLayers.keySet();
		Iterator it1 = set1.iterator();
		while (it1.hasNext()) {
			String layerName = (String) it1.next();
			//layerName = layerName.split(":")[layerName.split(":").length-1];
			
			Future ft = executorService.submit(() -> {
				try {
					
					
					FileLayer reader = null;
					//UMDReader reader = null;
					if(reader == null) {
						
						String ln = layerName.split(":")[layerName.split(":").length-1];
						
						reader = StorageMng.getFileLayer(layerPath+ln, false);
						
						if (reader == null ) {
							MapLog.getSCLog().debug("로딩 실패: " + layerName);
							//MapLog.getSCLog.debug("로딩 실패: " + layerName);
							//Monitor.append("로딩 실패: " + layerName);
						}
					}
				} catch (Exception ex) {
					throw new Exception(ex);
				}
				return 1;
			});
			fts.add(ft);
			nowCnt++;
		}
		Set set2 = rasterLayers.keySet();
		Iterator it2 = set2.iterator();
		while (it2.hasNext()) {
			String layerName = (String) it2.next();
			
			GridCoverage2D gc = null;
			gc = StorageMng.getGridCoverage2D(layerName);
			
			/*
			Future ft = executorService.submit(() -> {
				try {
					System.out.println("영상 로딩 시작: " + layerName);
					GridCoverage2D gc = null;
					gc = StorageMng.getGridCoverage2D(layerName);
					System.out.println("영상 로딩 완료: " + layerName);
				} catch (Exception e) {
					throw new Exception(e);
				}
				return 1;
			});
			fts.add(ft);
			
			
			nowCnt++;
			*/
		}

		for (Future ft : fts) {
			try {
				ft.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}

		executorService.shutdown();
		
		set = this.mapContexts.keySet();
		it = set.iterator();
		while (it.hasNext()) {
			Integer key = it.next();
			MapContext map = this.mapContexts.get(key);
			Vector<Layer> layers = map.getLayers();
			for (Layer ml : layers) {

				if (ml instanceof VectorLayer) {
					VectorLayer vl = (VectorLayer) ml;

					boolean load = false;

					String layerName = vl.getName();
					
					if(vl.getFileReader() == null) {
						
						
						String ln = layerName.split(":")[layerName.split(":").length-1];
						
						FileLayer reader = null;
						reader = StorageMng.getFileLayer(layerPath+ln, false);

						vl.setFileReader(reader); 
						
					}

				}
				else if (ml instanceof RasterLayer) {
					RasterLayer rl = (RasterLayer) ml;
					String layerName = rl.getName();
					
					rl.setGridCoverage2D(stm.getGridCoverage2D(rl.getName()));
				}
			}
		}
	}
	
	/*
	public MapContext createMapContext2(int level) {

		if (this.mapContexts.containsKey(level)) {
			MapContext context = this.mapContexts.get(level);
			if (context != null) {
				return context;
			}
		}

		this.mapContexts.remove(level);

		LevelConfig _lmi = this.levelInfos.get(level);
		if (_lmi == null) {
			return null;
		}
		MapContext map = new MapContext(this.name, this.stm);
		int cnt = 0;
		List<LayerInfo> layerInfos = _lmi.getLayerInfo();
		for (LayerInfo layerInfo : layerInfos) {
			VectorLayer layer = new VectorLayer();
			layer.setName(layerInfo.getLayerName());

			layer.setUMDReader(this.stm.getUMD1(layerInfo.getLayerName(), false));

			if (layer.getUMDReader() == null) {
				System.out.println("not exist umd name = " + layerInfo.getLayerName());
				continue;
			}

			if (layerInfo.getDrawOrder() == null) {
				// layer.drawPriority = 0;
				layer.setDrawPriority(0);
			} else {
				// layer.drawPriority = layerInfo.getDrawOrder();
				layer.setDrawPriority(layerInfo.getDrawOrder());
			}

			layer.setStyleName(layerInfo.getSelectStyleName());
			// layer.style = this.styles.get(layerInfo.getSelectStyleName());
			map.addLayer(cnt++, layer);
		}
		map.sort();

		map.setStyle(this.styles);

		map.setServiceName(this.trans);

		this.mapContexts.put(level, map);

		return map;
	}
	*/
	/*
	public boolean isContainLayerName(String layerName) {
		Set set = this.levelInfos.keySet();
		Iterator it = set.iterator();
		while (it.hasNext()) {
			Object key = it.next();
			LevelConfig lc = this.levelInfos.get(key);
			List<LayerInfo> lis = lc.getLayerInfo();
			for (LayerInfo li : lis) {
				String[] splits = li.getLayerName().split(":");
				String lyName = splits[splits.length - 1];
				if (lyName.toLowerCase().equals(layerName)) {
					return true;
				}
			}
		}
		return false;
	}
	*/
	
	

	public MapContext createMapContext(int level) {

		if (this.mapContexts.containsKey(level)) {
			MapContext context = this.mapContexts.get(level);
			if (context != null) {
				return context;
			}
		}

		this.mapContexts.remove(level);

		LevelConfig _lmi = this.levelInfos.get(level);
		if (_lmi == null) {
			return null;
		}
		MapContext map = new MapContext(this.name);
		
		map.setLevel(level);

		int cnt = 0;
		List<LayerInfo> layerInfos = _lmi.getLayerInfo();
		for (LayerInfo layerInfo : layerInfos) {
			String layerName = layerInfo.getLayerName();
			

			Layer layer = null;

			if (layerName.startsWith("tileMapService")) {
				layer = new TileLayer();
				String[] splits = layerName.split(":");
				layer.setName(splits[1]);
			} else if (layerName.startsWith("raster")) {
				int idx = layerName.indexOf(":");
				String layerNameTemp =layerName.substring(idx+1, layerName.length());	//확장자 없음
				
				RasterLayer rl = new RasterLayer();
				rl.setName(layerNameTemp);
				layer = rl;
			} else {
				String layerNameTemp = layerInfo.getLayerName();
				if (layerName.startsWith("vector")) {
					int idx = layerName.indexOf(":");
					layerNameTemp = layerName.substring(idx+1, layerName.length());
				}
				
				VectorLayer vl = new VectorLayer();
				vl.setName(layerNameTemp);
				vl.setStyleName(layerInfo.getSelectStyleName());
				
				layer = vl;
				
			}

			if (layerInfo.getDrawOrder() == null) {
				// layer.drawPriority = 0;
				layer.setDrawPriority(0);
			} else {
				// layer.drawPriority = layerInfo.getDrawOrder();
				layer.setDrawPriority(layerInfo.getDrawOrder());
			}

			// layer.style = this.styles.get(layerInfo.getSelectStyleName());
			map.addLayer(cnt++, layer);
		}
		map.sort();

		map.setStyle(this.styles);

		map.setServiceName(this.trans);

		this.mapContexts.put(level, map);

		return map;
	}
	
	
	/**
	 * 입력된 레이어 이름과 같은 이름을 갖는 하나의 Layer 객체를 리턴
	 * @param layerName 주제도의 경우 주제도이름:레이어이름, 경량화의 경우 주제도이름:레이어이름:process:타일서비스이름:레이어이름
	 * @return
	 */
	
	public Layer getLayer(String layerName) {
		Set set = mapContexts.keySet();
		Iterator it = set.iterator();
		while(it.hasNext()) {
			MapContext mc = mapContexts.get(it.next());
			for(Layer ly : mc.getLayers()) {
				if(ly instanceof VectorLayer) {
					VectorLayer vl = (VectorLayer)ly;
					
					if(vl.getName().equals(layerName)) {
						return vl;
					}
				} else if (ly instanceof RasterLayer) {
					RasterLayer rl = (RasterLayer) ly;
					if (rl.getName().equals(layerName)) {
						return rl;
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * 맵에 존재하는 전체 레이어 이름을 리턴한다.
	 * @param layerName
	 * @return
	 */
	public Vector<String> getLayerNames() {
		
		HashMap<String, String> hnames = new HashMap();
		Set set = mapContexts.keySet();
		Iterator it = set.iterator();
		while(it.hasNext()) {
			MapContext mc = mapContexts.get(it.next());
			for(Layer ly : mc.getLayers()) {
				hnames.put(ly.getName(), ly.getName());
			}
		}
		
		Vector<String> names = new Vector();
		names.addAll(hnames.values());
		return names;
	}
	
	
	/**
	 * 입력된 레이어 이름을 갖는 Layer 객체 리스트를 리턴
	 * @param _layerName 주제도의 경우 주제도이름:레이어이름, 경량화의 경우 주제도이름:레이어이름:process:타일서비스이름:레이어이름
	 * @param onlyLayerName 레이어 이름으로만 비교해서 리스트를 리턴
	 * @return
	 */
	public Vector<Layer> getLayers(String _layerName, boolean onlyLayerName) {
		String layerName = "";
		
		if(onlyLayerName) {
			layerName = _layerName.substring(_layerName.lastIndexOf(":")+1, _layerName.length());
		}
		else {
			layerName = _layerName;
		}
		
		Vector<Layer> lyList = new Vector();
		
		Set<Integer> set = this.mapContexts.keySet();
		Iterator<Integer> it = set.iterator();
		while (it.hasNext()) {
			Integer key = it.next();
			MapContext map = this.mapContexts.get(key);
			Vector<Layer> layers = map.getLayers();
			
			for (Layer layer : layers) {
				if(layer.getName().lastIndexOf(layerName) > -1) {
					lyList.add(layer);
				}
			}
		}
		return lyList;
	}
	
	/**
	 * 입력된 레이어 이름을 갖는 Layer 객체 리스트를 리턴
	 * @param _layerName 주제도의 경우 주제도이름:레이어이름, 경량화의 경우 주제도이름:레이어이름:process:타일서비스이름:레이어이름
	 * @param onlyLayerName 레이어 이름으로만 비교해서 리스트를 리턴
	 * @return
	 */
	public Vector<Layer> getLayers(int level, String _layerName, boolean onlyLayerName) {
		String layerName = "";
		
		if(onlyLayerName) {
			layerName = _layerName.substring(_layerName.lastIndexOf(":")+1, _layerName.length());
		}
		else {
			layerName = _layerName;
		}
		
		Vector<Layer> lyList = new Vector();
		
		MapContext map = this.mapContexts.get(level);
		Vector<Layer> layers = map.getLayers();
		for (Layer layer : layers) {
			if(layer.getName().lastIndexOf(layerName) > -1) {
				lyList.add(layer);
			}
		}
		
		
//		Set<Integer> set = this.mapContexts.keySet();
//		Iterator<Integer> it = set.iterator();
//		while (it.hasNext()) {
//			Integer key = it.next();
//			MapContext map = this.mapContexts.get(key);
//			Vector<Layer> layers = map.getLayers();
//			for (Layer layer : layers) {
//				if(layer.getName().lastIndexOf(layerName) > -1) {
//					lyList.add(layer);
//				}
//			}
//		}
		
		
		return lyList;
	}

	
	
	/**
	 * 전체 레이어 리스트를 반환한다.
	 * @param reference true 참조된 레이어까지 포함, false 참조된 리스트 제외
	 * @return
	 */
	
	/*
	public Vector<Layer> getLayers(boolean reference){
		Vector<Layer> list = new Vector();
		
		
		Set<Integer> set = this.mapContexts.keySet();
		Iterator<Integer> it = set.iterator();
		while (it.hasNext()) {
			Integer key = it.next();
			MapContext map = this.mapContexts.get(key);
			Vector<Layer> layers = map.getLayers();
			
			for (Layer layer : layers) {
				
				if(reference) {
					list.add(layer);
				}
				else {
					String layerName = layer.getName();
					String onlyLayerName = layer.getOnlyName();
					if(this.isProcess) {
						if(layer instanceof VectorLayer) {
							String tileServiceLayerName = this.tileServiceName+":"+onlyLayerName;
							if(layerName.endsWith(tileServiceLayerName)) {
								list.add(layer);
							}
						}
						else if(layer instanceof RasterLayer) {
							if(layer.isProcess()) {
								String tileServiceLayerName = this.tileServiceName+":"+onlyLayerName;
								String ln = layer.getServiceName()+":"+onlyLayerName;
								
								if(tileServiceLayerName.equals(ln)) {
									list.add(layer);
								}
							}
							else {
								String mapLayerName = this.name+":"+onlyLayerName;
								String ln = layer.getMapName()+":"+onlyLayerName;
								if(mapLayerName.equals(ln)) {
									list.add(layer);
								}
							}
						}
					}
					else {
						String mapInfoLayerName = this.name+":"+onlyLayerName;
						if(layerName.startsWith(mapInfoLayerName)) {
							list.add(layer);
						}
					}
				}
			}
		}
		return list;
	}
	
	/**
	 * 레벨에 존재하는 레이어 리스트를 반환한다.
	 * @param levelId
	 * @return
	 */
	public Vector<Layer> getLayers(int levelId){
		MapContext mc = this.mapContexts.get(levelId);
		return mc.layerList;
	}
	

	/*
	public void loadLevelInfos1(LevelConfigs _levelInfos, boolean memory) {

		// ExecutorService executorService = Executors.newFixedThreadPool(1);
		this.levelInfos.clear();

		HashMap<String, String> loadLayers = new HashMap();

		List<LevelConfig> lls = _levelInfos.getLevelConfig();
		for (LevelConfig ll : lls) {
			this.levelInfos.put(ll.getLevelId(), ll);
			List<LayerInfo> lis = ll.getLayerInfo();
			for (LayerInfo li : lis) {
				loadLayers.put(li.getLayerName(), li.getLayerName());
			}
		}

		ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);

		Vector<Future> fts = new Vector();
		Set set = loadLayers.keySet();
		Iterator it = set.iterator();
		while (it.hasNext()) {
			String layerName = (String) it.next();
			Future ft = executorService.submit(() -> {
				try {

					stm.getUMD1(layerName, memory);
				} catch (Exception ex) {
					throw new Exception(ex);
				}
				return 1;
			});
			fts.add(ft);
		}

		for (Future ft : fts) {
			try {
				ft.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}

		executorService.shutdown();

	}
	*/

	@Override
	public void allRunningThreadsFinished() {
		synchronized (this.lock) {
			this.lock.notify();
		}
	}

//	public void drawMap(int level, Envelope mbr, BufferedImage image){
//		long st = System.currentTimeMillis();
//		ScreenCoordUtil scu = new ScreenCoordUtil();
//		Dimension dimension = new Dimension(image.getWidth(), image.getHeight());
//		scu.setData(dimension, mbr);
//		MapContext mapContext = this.createMap(level);
//		mapContext.loadMemLayers(mbr, scu);
//		mapContext.setStyle(this.styles);
//		Graphics2D _g = image.createGraphics();
//		mapContext.drawMap(_g, mbr, scu );
//		System.out.println("drawMap time=" + (System.currentTimeMillis() - st));
//	}

	Object locks = new Object();

	public void drawMap(Envelope mbr, BufferedImage img, String epsg, boolean isDebug) {

		double pixelPerMeter = mbr.getWidth() / img.getWidth();

		int selectLevel = this.getLikeLevel(pixelPerMeter);

		this.drawMap(selectLevel, mbr, img, epsg, isDebug);
	}

	public void drawMap(int level, Envelope mbr, BufferedImage img, String epsg, boolean isDebug) {

		double pixelPerMeter = mbr.getWidth() / img.getWidth();

		int selectLevel = this.getLikeLevel(pixelPerMeter);

		MapContext context = this.mapContexts.get(selectLevel);
		
		context.isProcess = this.isProcess;

		ScreenCoordUtil scu = new ScreenCoordUtil();
		Dimension dimension = new Dimension(img.getWidth(), img.getHeight());
		scu.setData(dimension, mbr);
		CoordinateReferenceSystem viewCRS = null;
		try {
			viewCRS = CRS.decode(epsg);
		} catch (NoSuchAuthorityCodeException e) {
			e.printStackTrace();
		} catch (FactoryException e) {
			e.printStackTrace();
		};
		
		
		Vector<Layer> layers = context.loadMemLayers(mbr, scu, viewCRS, epsg, null);
		
		//context.drawMap(img.createGraphics(), mbr, scu, layers, isDebug, viewCRS);
		context.drawMap(img.createGraphics(), mbr, scu, layers, isDebug);

	}
	
	public void drawLayer(int level, Envelope mbr, BufferedImage img, String epsg, boolean isDebug, Layer layer) {

		double pixelPerMeter = mbr.getWidth() / img.getWidth();

		int selectLevel = this.getLikeLevel(pixelPerMeter);

		MapContext context = this.mapContexts.get(selectLevel);
		
		context.isProcess = this.isProcess;

		ScreenCoordUtil scu = new ScreenCoordUtil();
		Dimension dimension = new Dimension(img.getWidth(), img.getHeight());
		scu.setData(dimension, mbr);
		CoordinateReferenceSystem viewCRS = null;
		try {
			viewCRS = CRS.decode(epsg);
		} catch (NoSuchAuthorityCodeException e) {
			e.printStackTrace();
		} catch (FactoryException e) {
			e.printStackTrace();
		};
		
		
		VectorLayer vlayer = (VectorLayer)layer;
		
		if(vlayer.getStyle() == null) {
			com.gis2.map.style.PointStyle pts = null;
			com.gis2.map.style.BasicStyleExtend pgs = null;
			
			Vector<Layer> layers = context.getLayers();
			for(Layer ly : layers) {
				VectorLayer vly = (VectorLayer)ly;
				if(vly.getStyle() instanceof com.gis2.map.style.PointStyle) {
					pts = (com.gis2.map.style.PointStyle) vly.getStyle();
				}
				else {
					pgs = (BasicStyleExtend) vly.getStyle();
				}
			}
			if(pgs != null) {
				vlayer.addStyle(pgs);
			}
			if(pts != null) {
				vlayer.addStyle(pts);
			}
			
		}
		
		//Vector<Layer> layers = context.loadMemLayers(mbr, scu, viewCRS, epsg, layer);
		
		Vector<Layer> layers = new Vector();
		layers.add(layer);
		
		//context.drawMap(img.createGraphics(), mbr, scu, layers, isDebug, viewCRS);
		context.drawMap(img.createGraphics(), mbr, scu, layers, isDebug);

	}
	
	public void drawLayers(int level, Envelope mbr, BufferedImage img, String epsg, boolean isDebug, Vector<Layer> layers) {

		double pixelPerMeter = mbr.getWidth() / img.getWidth();

		int selectLevel = this.getLikeLevel(pixelPerMeter);

		MapContext context = this.mapContexts.get(selectLevel);
		
		context.isProcess = this.isProcess;

		ScreenCoordUtil scu = new ScreenCoordUtil();
		Dimension dimension = new Dimension(img.getWidth(), img.getHeight());
		scu.setData(dimension, mbr);
		CoordinateReferenceSystem viewCRS = null;
		try {
			viewCRS = CRS.decode(epsg);
		} catch (NoSuchAuthorityCodeException e) {
			e.printStackTrace();
		} catch (FactoryException e) {
			e.printStackTrace();
		};
		
		Vector<Layer> layers_ = context.loadMemLayers(mbr, scu, viewCRS, epsg, layers);
		
		//context.drawMap(img.createGraphics(), mbr, scu, layers_, isDebug, viewCRS);
		context.drawMap(img.createGraphics(), mbr, scu, layers_, isDebug);

	}
	

	
	public static void main(String[] args) {
		MapData map = new MapData("mgis_map");
		// map.loadData(null);
	}

	public MapContext getMapContext(int level) {
		return this.mapContexts.get(level);
	}

	
	@Override
	public int getLayerSize(int level) {
		
		MapContext mm = getMapContext(level);

		if (mm != null) {
			return mm.getLayers().size();
		}
		
		return 0;

	}

}
