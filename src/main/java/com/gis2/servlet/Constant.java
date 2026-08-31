package com.gis2.servlet;

import java.util.HashMap;
import java.util.Vector;

import com.gis2.storage.TileServiceMng;

public class Constant {
	
	public static String tile = "tile";
	public static String tiles = "tiles";
	public static String analysis = "analysis";
	public static String pnuAnalysis = "pnu_analysis";
	public static String pnuGeo = "pnu_geo";
	public static String search = "search";
	public static String code = "code";
	public static String roadIntersection = "road_intersection";
	public static String layersSave = "layers_save";
	public static String slope = "slope";
	public static String layerList = "layerlist";
	
	
	public static String users = "users";
	public static String user = "user";
	
	public static String TILE = "TILE";
	public static String ANALYSIS = "ANALYSIS";
	
	
	public static Vector<String> openApiNames = new Vector();
	
	public static Vector<String> openApiTile = new Vector();
	
	public static Vector<String> openApiAnalysis = new Vector();
	
	
	public static String update = "update";
	public static String updatetile = "updatetile";
	
	
	public static String tileSet = "tileSet";
	
	public static String layer = "layer";
	
	public static String req = "req";
	
	public static String key = "key";
	
	public static String path = "path";
	
	public static String functionName = "functionName";
	
	public static String tileName = "tileName";
	
	public static String layerName = "layerName";
	
	public static String count = "count";
	
	public static int authOK = 0;
	
	public static int authNotKey = 1001;
	
	public static int authNotDomain = 1002;
	
	public static int authNotFunc = 1003;
	
	public static int authNotCnt = 1004;
	
	public static String authOKDesc = "정상";
	
	public static String authNotKeyDesc = "권한 없는 인증키 : ";
	
	public static String authNotDomainDesc = "권한 없는 도메인 : ";
	
	public static String authNotFuncDesc = "권한 없는 기능 : ";
	
	public static String authNotCntDesc = "일 요청 건수 제한 : ";
	
	
	
//	else if(req.equals("update")) {
//		TileServiceMng.reInit();
//	}
//	else if(req.equals("updatetile")) {
//		TileServiceMng.reloadTile();
//	}	
	
	public static Vector<String> getOpenApiNames(){
		synchronized(openApiNames) {
			if(openApiNames.size() == 0) {
				openApiNames.add(Constant.tile);
				openApiNames.add(Constant.tiles);
				openApiNames.add(Constant.analysis);
				openApiNames.add(Constant.pnuAnalysis);
				openApiNames.add(Constant.search);
				openApiNames.add(Constant.code);
				openApiNames.add(Constant.roadIntersection);
				openApiNames.add(Constant.slope);
				openApiNames.add(Constant.layersSave);
			}
		}
		return openApiNames;
	}
	
	
	public static Vector<String> getOpenApiTile(){
		synchronized(openApiTile) {
			if(openApiTile.size() == 0) {
				openApiTile.add(Constant.tile);
				openApiTile.add(Constant.tiles);
			}
		}
		return openApiTile;
	}

	public static Vector<String> getOpenApiAnalysis(){
		synchronized(openApiAnalysis) {
			if(openApiAnalysis.size() == 0) {
				openApiAnalysis.add(Constant.analysis);
				openApiAnalysis.add(Constant.pnuAnalysis);
				openApiAnalysis.add(Constant.search);
				openApiAnalysis.add(Constant.code);
				openApiAnalysis.add(Constant.roadIntersection);
				openApiAnalysis.add(Constant.slope);
				openApiAnalysis.add(Constant.layersSave);
			}
		}
		return openApiAnalysis;
	}
	
	
	public static HashMap<Integer, String> resultDesList = new HashMap();
	
	public static String getResultDes(int code) {
		
		synchronized(resultDesList) {
			if(resultDesList.size() == 0) {
				
				resultDesList.put(authOK, authOKDesc);
				resultDesList.put(authNotKey, authNotKeyDesc);
				resultDesList.put(authNotDomain, authNotDomainDesc);
				resultDesList.put(authNotFunc, authNotFuncDesc);
				resultDesList.put(authNotCnt, authNotCntDesc);
			}
		}
		return resultDesList.get(code);
	}
}

