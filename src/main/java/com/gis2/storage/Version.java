package com.gis2.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import com.data.file.FileLayer;
import com.gis.map.MapData;
import com.gis.map.TileMapFactory2;
import com.gis.protocol.LayerInfo;
import com.gis.protocol.LevelConfig;

public class Version {
	
	public String name;
	
	private Hashtable<String, MapData> mapDatas = new Hashtable();

	private Hashtable<String, TileMapFactory2> tmss = new Hashtable();
	
	public String layerPath = "";
	
	//지적레이어 이름
	public String jijukName = "";
	
	//지역지구 레이어 이름, 레이어 이름의 시작이 f, g 가 아닌 레이어
	public HashMap<String, String> jiguNames = new HashMap();
	
	
	//jiguNames의 중에 컬럼이 WTNNC_SN 이 존재하는 레이어 이름  
	public Vector<String> pnuAnalysisLayers = new Vector();
	
	public HashMap<String, FileLayer> layers = new HashMap();
	
	public void release() {

		mapDatas.clear();
		tmss.clear();

	}
	
	public void addMapData(String name, MapData data) {
		this.mapDatas.put(name, data);
		
		Set set = data.levelInfos.keySet();
		Iterator it = set.iterator();
		while(it.hasNext()) {
			LevelConfig lc = data.levelInfos.get(it.next());
			List<LayerInfo> lis = lc.getLayerInfo();
			for(LayerInfo li : lis) {
				String[] styles = li.getSelectStyleName().split("\\|");
				if(styles[1].toLowerCase().equals("tile") || styles[1].toLowerCase().equals("raster")) {
					continue;
				}
				
				if(li.getLayerName().toLowerCase().indexOf("fa") > -1) {
					this.jijukName = li.getLayerName().toLowerCase();
				}
				//System.out.println("layer name = " + li.getLayerName());
				if( !li.getLayerName().toLowerCase().startsWith("f") 
						&& !li.getLayerName().toLowerCase().startsWith("g")) {
					this.jiguNames.put(li.getLayerName().toLowerCase(), li.getLayerName().toLowerCase());	
					//System.out.println("layer name = " + li.getLayerName());
				}
				if(li.getLayerName().indexOf("grid:") > -1) {
					int sidx = li.getLayerName().lastIndexOf(":");
					int eidx = li.getLayerName().lastIndexOf("_");
					
					if(sidx > -1 && eidx > -1) {
						String layerName = li.getLayerName().substring(sidx+1, eidx);
						
						if( !layerName.toLowerCase().startsWith("f") 
								&& !layerName.toLowerCase().startsWith("g")) {
							this.jiguNames.put(layerName.toLowerCase(), layerName.toLowerCase());	
							//System.out.println("layer name = " + li.getLayerName());
						}
					}
					
				}
			}
		}
	}
	
	public List<String> getMapDataNames(){
		List<String> keyList = new ArrayList<String>(mapDatas.keySet());
		return keyList;
	}
	
	public MapData getMapData(String name) {
		return this.mapDatas.get(name);
	}
	
	public void addTileMapFactory(String name, TileMapFactory2 tmf) {
		this.tmss.put(name, tmf);
	}
	
	public TileMapFactory2 getTileMapFactory(String name) {
		return this.tmss.get(name);
	}
	
	public Hashtable<String, TileMapFactory2> getTMSS(){
		return this.tmss;
	}
}
