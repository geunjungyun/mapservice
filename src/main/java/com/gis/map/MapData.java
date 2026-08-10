package com.gis.map;

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

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.referencing.CRS;
import org.jaitools.numeric.RangeExtendedComparator.Result;

import com.data.file.FileLayer;
import com.gis.map.style.BasicStyleExtend;
import com.gis.map.style.QueryStyle;
import com.gis.map.style.StyleFactory;
import com.gis.projection.ScreenCoordUtil;
import com.gis.protocol.LayerInfo;
import com.gis.protocol.LevelConfig;
import com.gis.protocol.LevelConfigs;
import com.gis.protocol.MapInfo;
import com.gis.protocol.PointStyle;
import com.gis.protocol.PolygonStyle;
import com.gis.protocol.PolylineStyle;
import com.gis.protocol.Query;
import com.gis.protocol.ScaleInfo;
import com.gis.protocol.ScaleInfos;
import com.gis.protocol.Styles;
import com.gis.protocol.TextStyle;
import com.gis2.storage.StorageMng;
import com.gis2.storage.TileServiceMng;

//import com.gis2.storage.TileServiceMng;
//import com.util2.thread.ThreadQueue;
//import com.util2.thread.ThreadQueue.Listener;
//import com.gis2.storage.ServiceConfig;
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

public class MapData implements MData {

	static public String trans = "";

	String name;

	public HashMap<String, Style> styles = null;

	HashMap<Integer, ScaleInfo> scaleInfo = new HashMap();

	public HashMap<Integer, LevelConfig> levelInfos = new HashMap();

	public HashMap<Integer, MapContext> mapContexts = new HashMap<Integer, MapContext>();

	StorageMng stm = null;

	Object lock = new Object();

	boolean largeFontMode = false;
	
	private boolean reference = false;
	
	public String layerPath = "";
	
	public String rasterPath = "";

	private int levelId;

	
	int mbrExtend = 200;
	
	public MapData(String name) {
		this.name = name;
		this.stm = null;

	}
	
	public void setMbrExtend(int value) {
		this.mbrExtend = value;
	}
	
	public int getMbrExtend() {
		return this.mbrExtend;
	}
	
	public void setReference(boolean reference) {
		this.reference = reference;
	}
	
	public boolean getReference() {
		return this.reference;
	}

	public MapData(String name, ScaleInfos scaleInfos, LevelConfigs lcs, Styles _styles) {
		this.name = name;
		this.stm = null;
		
		if(TileServiceMng.hdMode) {
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
		List<com.gis.protocol.PointStyle> pss = _styles.getPointStyle();
		for (com.gis.protocol.PointStyle ps : pss) {
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
			MapContext mct = this.createMapContext1(si.getId());

			if (mct != null && largeFontMode) {
				mct.largeFontMode = largeFontMode;
			}

			if (mct != null) {
				mct.setStyle(styles);
			}
		}
	}

	public String getName() {
		return this.name;
	}

	public MapData cloneMapData(LevelConfigs lcs) {
		MapData clone = new MapData(this.name);
		clone.styles = this.styles;
		clone.scaleInfo = this.scaleInfo;
		clone.stm = this.stm;
		clone.setLevelInfos(lcs);
		clone.largeFontMode = this.largeFontMode;
		Set set = this.scaleInfo.keySet();
		Iterator it = set.iterator();
		while (it.hasNext()) {
			Integer levelId = (Integer) it.next();
			MapContext mct = clone.createMapContext1(levelId);

			if (mct != null) {
				mct.setStyle(styles);
				mct.largeFontMode = clone.largeFontMode;
			}
		}
		return clone;
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

	public StorageMng getStorageMng() {
		return this.stm;
	}


	public void setStyles(HashMap<String, Style> nStyles) {
		this.styles = nStyles;
	}

	public void setScaleInfos(ScaleInfos scaleInfo) {
		this.scaleInfo.clear();
		List<ScaleInfo> sis = scaleInfo.getScaleInfo();
		for (ScaleInfo si : sis) {
			this.scaleInfo.put(si.getId(), si);
		}
	}

	public void setLevelInfos(LevelConfigs levelInfos) {

		// ExecutorService executorService = Executors.newFixedThreadPool(1);
		this.levelInfos.clear();

		List<LevelConfig> lls = levelInfos.getLevelConfig();
		for (LevelConfig ll : lls) {
			
			
			List<LayerInfo> lis = ll.getLayerInfo();
			
			for(LayerInfo li : lis) {
				String layerName = li.getLayerName();
				if(!layerName.startsWith("tileMapService")) {
					li.setLayerName(layerName);
				}
			}
			
			this.levelInfos.put(ll.getLevelId(), ll);
			//List<LayerInfo> lis = ll.getLayerInfo();
		}

	}


	public void readFileLayers() throws Exception {

		HashMap<String, String> loadLayers = new HashMap();
		
		HashMap<String, String> loadRasters = new HashMap();

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
				else if(layer instanceof RasterLayer) {
					loadRasters.put(layer.getName(), layer.getName());
				}
				
			}
			
			
		}

		ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);

		Vector<Future> fts = new Vector();
		
		Set set1 = loadLayers.keySet();
		Iterator it1 = set1.iterator();
		while (it1.hasNext()) {
			String layerName = (String) it1.next();
			Future ft = executorService.submit(() -> {
				try {
					FileLayer reader = null;

					if(reader == null) {
						//System.out.println(layerPath+layerName);
						reader = StorageMng.getFileLayer(layerPath+layerName, false);
					}
					
				} catch (Exception ex) {
					throw new Exception(ex);
				}
				return 1;
			});
			fts.add(ft);
		}
		
		
		/*
		Set set2 = loadRasters.keySet();
		Iterator it2 = set2.iterator();
		while (it2.hasNext()) {
			String layerName = (String) it2.next();
			Future ft = executorService.submit(() -> {
				try {
					GridCoverage2D gc = null;

					if(gc == null) {
						System.out.println("readFileLayers : " +layerPath+layerName);
						gc = StorageMng.getGridCoverage2D(rasterPath+layerName);
					}
					
				} catch (Exception ex) {
					throw new Exception(ex);
				}
				return 1;
			});
			fts.add(ft);
		}
		*/

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
						
						vl.setFileReader(stm.getFileLayer(layerPath+vl.getName(), false)); 
					}

				}
				else if(ml instanceof RasterLayer) {
					RasterLayer rl = (RasterLayer) ml;
					if(rl.getGridCoverage2D() == null) {
						//rl.gc = stm.getGridCoverage2D(rasterPath+rl.getName());
						rl.setGridCoverage2D(stm.getGridCoverage2D(rasterPath+rl.getName()));
					}
				}
			}
		}
	}
	
	public MapContext getMapContext(int level) {
		return this.mapContexts.get(level);
	}

	/*
	public MapContext createMapContext1111(int level) {

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

			layer.setFileReader(this.stm.getFileLayer(layerInfo.getLayerName(), false));

			if (layer.getFileReader() == null) {
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

	public MapContext createMapContext1(int level) {
		
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
		MapContext map = new MapContext(this.name, this.stm, this);
		map.levelId = level;

		int cnt = 0;
		List<LayerInfo> layerInfos = _lmi.getLayerInfo();
		for (LayerInfo layerInfo : layerInfos) {
			String layerName = layerInfo.getLayerName();
			
			
			Layer layer = null;
			
			String type = layerInfo.getSelectStyleName().split("\\|")[1];
			
			if(type.equals("TILE")) {
				TileLayer tl = new TileLayer();
				tl.setName(layerInfo.getLayerName());
				layer = tl;
			}
			else if(type.equals("RASTER")) {
				RasterLayer rasterLayer = new RasterLayer();
				rasterLayer.setName(layerInfo.getLayerName());
				layer = rasterLayer;
			}
			else {
				VectorLayer vl = new VectorLayer();
				vl.setName(layerInfo.getLayerName());
				vl.setStyleName(layerInfo.getSelectStyleName());
				layer = vl;
			}
			
				layer.setDrawPriority(layerInfo.getDrawOrder());
			
			map.addLayer(cnt++, layer);
		}
		map.sort();
		
		if(level == 15) {
			map.sort1();
		}

		map.setStyle(this.styles);

		map.setServiceName(this.trans);

		this.mapContexts.put(level, map);

		return map;
	}
	
	public Layer getLayer(String layerName) {
		Set set = mapContexts.keySet();
		Iterator it = set.iterator();
		while(it.hasNext()) {
			MapContext mc = mapContexts.get(it.next());
			for(Layer ly : mc.getLayers()) {
				if(ly instanceof VectorLayer) {
					VectorLayer vl = (VectorLayer)ly;
					//System.out.println(vl.getName());
					if(vl.getName().equals(layerName) && vl.getFileReader() != null) {
						return vl;
					}
				}
			}
		}
		return null;
	}

	public void loadLevelInfos(LevelConfigs _levelInfos, boolean memory) {

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

					stm.getFileLayer(layerName, memory);
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		executorService.shutdown();

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
	
	
	

//	public void getMap(Envelope mbr, BufferedImage img, String epsg) {
//
//		double pixelPerMeter = mbr.getWidth() / img.getWidth();
//
//		int selectLevel = this.getLikeLevel(pixelPerMeter);
//
//		this.getMap(selectLevel, mbr, img, epsg);
//	}

	public void getMap(int level, Envelope mbr, BufferedImage img) {
		
		MapContext context = this.mapContexts.get(level);

		ScreenCoordUtil scu = new ScreenCoordUtil();
		Dimension dimension = new Dimension(img.getWidth(), img.getHeight());
		scu.setData(dimension, mbr);

		Vector<Layer> layers = context.loadMemLayers(mbr, scu);
		context.drawMap(img.createGraphics(), mbr, scu, layers, false);

	}
	
	public void updateLayer(String _layerName, FileLayer read) {
		Set set = this.mapContexts.keySet();
		Iterator it = set.iterator();
		while(it.hasNext()) {
			MapContext mc = this.mapContexts.get(it.next());
			
			for(int i=0; i<mc.layerList.size();i++) {
				Layer ly = mc.layerList.get(i);
				if(ly instanceof VectorLayer) {
					
					String _onlyLayerName = _layerName.substring(_layerName.lastIndexOf(":")+1, _layerName.length());
					String onlyLayerName = ly.getName().substring(ly.getName().lastIndexOf(":")+1, ly.getName().length());
					
					if(onlyLayerName.equals(_onlyLayerName)) {
						VectorLayer vly = (VectorLayer)ly;
						//vly.umdR = read;
						vly.setFileReader(read);
					}
				}
			}
		}
	}
	
	

	public static void main(String[] args) {
		MapData map = new MapData("mgis_map");
		// map.loadData(null);
	}

	@Override
	public int getLayerSize(int level) {
		// TODO Auto-generated method stub
		MapContext mm = getMapContext(level);

		if (mm != null) {
			return mm.getLayers().size();
		}
		return 0;
	}

}
