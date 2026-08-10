package com.gis2.servlet;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;

import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTS;
import org.geotools.ows.ServiceException;

import org.h2.value.Value;
import org.json.JSONArray;
import org.json.JSONObject;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.simple.SimpleFeature;

import com.data.file.Circle;
import com.data.file.FileLayer;
import com.data.file.MetaInfo;
import com.data.file.ShapeFile;
import com.gis.map.Layer;
import com.gis.map.MapContext;
import com.gis.map.MapData;
import com.gis.map.TileMapFactory2;
import com.gis.map.VectorLayer;
import com.gis.map.analysis.SegmentIntersact;
import com.gis.protocol.LayerInfo;
import com.gis.protocol.LevelConfig;
import com.gis2.storage.AuthorityMng;
import com.gis2.storage.Job;
import com.gis2.storage.JobMng;
import com.gis2.storage.StorageMng;
import com.gis2.storage.TileDB;
import com.gis2.storage.TileServiceMng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapplan.Analysis;
import com.mapplan.CodeArea;
import com.mapplan.Codes;
import com.mapplan.Header;
//import com.mapplan.Http;
//import com.mapplan.Http.Header;
import com.mapplan.LayerArea;
import com.mapplan.Protocol;
import com.mapplan.Res;
import com.mapplan.Tile;
import com.mapplan.Tiles;
import com.mapplan.Tileset;
import com.mapplan.Version;
import com.util.io.FileUt;

//http://192.168.100.17:8081/MapAppServer/TileCreate?jobname=maketile_chinag
public class MapService extends HttpServlet {

	GeometryFactory gf = new GeometryFactory();

//	Vector<Double> widths = new Vector();
//	Vector<Double> height = new Vector();

	Vector<Double> vscale = new Vector();

	double width = 1914.0;
	double height = 939.0;

	Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setPrettyPrinting()
			.registerTypeAdapter(XMLGregorianCalendar.class, new MetaInfo.Deserializer())
			.registerTypeAdapter(XMLGregorianCalendar.class, new MetaInfo.Serializer()).create();

	JobMng jobMng = null;
	
	Object lock = new Object();
	/**
	 * Constructor of the object. init() 함수로 빼서 초기화가 될수 있도록 개선 필요
	 */

	byte[] tempImg = null;

	public MapService() {
		super();
		try {
			BufferedImage bufferedImage = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ImageIO.write(bufferedImage, "png", bos);
			bos.flush();
			tempImg = bos.toByteArray();

			vscale.add(2048.0);
			vscale.add(1024.0);
			vscale.add(512.0);
			vscale.add(256.0);
			vscale.add(128.0);
			vscale.add(64.0);
			vscale.add(32.0);
			vscale.add(16.0);
			vscale.add(8.0);
			vscale.add(4.0);
			vscale.add(2.0);
			vscale.add(1.0);
			vscale.add(0.5);
			vscale.add(0.25);
			vscale.add(0.125);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getLevel(double minx, double miny, double maxx, double maxy) {
		int level = -1;

		double rwidth = maxx - minx;
		double rheight = maxy - miny;

		for (int i = 0; i < this.vscale.size(); i++) {

			double lwidth = this.vscale.get(i) * this.width;
			double lheight = this.vscale.get(i) * this.height;

			if (rwidth < lwidth && rheight < lheight) {
				level = (i + 1);
			}
		}

		return level;
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		long st = System.currentTimeMillis();

	}

	//http://192.168.60.213:8070/ver1/mapservice?key=221df9812idf12109812dfa
	// SERVICE=WMS&REQUEST=GetCapabilities
	// layer=지적&mbr=192,121,121,121&code=C00231&tiles=tile1,tile2&levelid=10
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		//System.out.println("doGet = " + request.getRequestURI() +"?" +request.getQueryString());

		synchronized(this.lock) {
			if(this.jobMng == null) {
				ServletContext scontext = getServletContext();
				jobMng = (JobMng) scontext.getAttribute("JobMng");
				
			}
		}
		
		boolean debug = false;
		if (debug) {
			System.out.println("url1 = " + request.getRequestURI());
			System.out.println("url2 = " + request.getQueryString());

			Enumeration<String> headerNames = request.getHeaderNames();
			while (headerNames.hasMoreElements()) {
				String name = headerNames.nextElement();
				System.out.println("header name = " + name + ", value=" + request.getHeader(name));
			}
		}

		String refer = request.getHeader("referer");
		
		if(refer == null) {
			refer = request.getHeader("Referer");
		}

		String domain = null;
		if (refer != null) {
			refer = new String(refer.getBytes("8859_1"), "UTF-8");
			URI url1;
			try {
				url1 = new URI(refer);
				domain = url1.getHost();
				// System.out.println("referer host = " + domain);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		else {
			domain = this.getClientIP(request);
		}

		String req = request.getParameter(Constant.req);

		String keyS = request.getParameter(Constant.key);

		String layerS = null;

		// String tileSetS = null;

		String tileNameS = null;
		
		//SERVICE 요청 시 req는 null 이다.
		if (req!= null && req.equals(Constant.tile)) {

			tileNameS = request.getParameter(Constant.path);

			String[] urltilePath = tileNameS.split("/");

			String tileSetS = urltilePath[0];
			String tileName = urltilePath[1];

			//tileNameS = tileSetS + "|" + tileName;
			tileNameS = tileSetS;
		}
		else if (
				req!= null &&
				(
				req.equals(Constant.search) || req.equals(Constant.code) || req.equals(Constant.analysis) 
				|| req.equals(Constant.pnuGeo) || req.equals(Constant.roadIntersection) || req.equals(Constant.layersSave)
				|| req.equals(Constant.slope)
				)
				
				) {

			String tileSetS = request.getParameter(Constant.tileSet);
			
			if(req.equals(Constant.slope)) {
				tileSetS = request.getParameter("slope_tileSet");
			}

			//layerS = tileSetS + "|" + request.getParameter(Constant.layer);
			layerS = tileSetS;
		}

		String servcie = request.getParameter("SERVICE");

		if (servcie != null) {

			// String json = FileUtils.readFileToString(new
			// File("D:\\workspace\\MapService\\map_service\\WMTSCapabilities.xml"),
			// "utf-8");

			Vector<String> authTileNames = AuthorityMng.getUserTileNames(keyS);

			Vector<String> tileSetNames = new Vector();

			Enumeration<com.gis2.storage.Version> versions = TileServiceMng.versions.elements();
			while (versions.hasMoreElements()) {
				com.gis2.storage.Version ver = versions.nextElement();
				
				Enumeration<TileMapFactory2> tmfs = ver.getTMSS().elements();
				while (tmfs.hasMoreElements()) {
					// tileNames.add(tmfs.nextElement().getName());
					
					String tileName = tmfs.nextElement().getName();

					boolean ok = false;
					
					if(AuthorityMng.isAuthority) {
						for (String authTileName : authTileNames) {
	
							if ((ver.name + "|" + tileName).startsWith(authTileName)) {
								ok = true;
							}
						}
					}
					else {
						ok = true;
					}
					
					if (ok) {
						tileSetNames.add(ver.name + "/" + tileName);
					}
				}
			}

			String json = TileServiceMng.wmts.getWMTSCapabilities(tileSetNames, keyS);

			response.setContentType("application/xml; charset=utf-8");
			byte[] bodyByte = json.getBytes("utf-8");
			BufferedOutputStream boss = new BufferedOutputStream(response.getOutputStream());
			boss.write(bodyByte);
			boss.flush();
			boss.close();
			return;
		}

		response.setHeader("Access-Control-Allow-Origin", "*");

		long st = System.currentTimeMillis();

		// String timg = request.getParameter("tile");

		String json = "";

		boolean searchLogMode = false;

		// boolean searchUserLogMode = false;

		boolean analysisLogMode = false;

		String pnuslog = "";

		int selectLevel = -1;

		Protocol pt = new Protocol();

		Header header = new Header();
		header.setResult("0000");
		header.setResultDesc("정상");
		header.setReq(req);

		pt.setHeader(header);

		boolean authResult = true;

		if (AuthorityMng.isAuthority && !req.equals(Constant.tiles)) {
			authResult = AuthorityMng.isOK(keyS, req, tileNameS, layerS, domain, header);
		}

		if (authResult == false) {
			
			if(req.equals(Constant.tile)) {
				response.setContentType("image/png");
				response.getOutputStream().write(TileServiceMng.errorImageByte);
				response.setStatus(HttpServletResponse.SC_OK);
			}
			else {
				json = gson.toJson(pt);
			}
			
		} else if (req.equals(Constant.tile)) {
			this.doTile(request, response);
		} else if (req.equals(Constant.tiles)) {

			Tiles tilesReq = new Tiles();
			Enumeration<com.gis2.storage.Version> versions = TileServiceMng.versions.elements();
			while (versions.hasMoreElements()) {
				com.gis2.storage.Version ver = versions.nextElement();
				List<Tileset> vsList = tilesReq.getTileset();

				Tileset vs = new Tileset();
				vsList.add(vs);
				vs.setName(ver.name);
				List<String> tileNames = vs.getTile();

				Vector<String> temp = new Vector();

				Enumeration<TileMapFactory2> tmfs = ver.getTMSS().elements();
				while (tmfs.hasMoreElements()) {
					// tileNames.add(tmfs.nextElement().getName());
					temp.add(tmfs.nextElement().getName());
				}

				Collections.sort(temp);
				for (String tt : temp) {
					tileNames.add(tt);
				}

				List<String> layers = vs.getLayer();

				Set set = ver.layers.keySet();
				Iterator it = set.iterator();
				while (it.hasNext()) {
					Object layerName = it.next();
					layers.add((String) layerName);
				}
			}

			pt.setBody(tilesReq);

//			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setPrettyPrinting()
//					.registerTypeAdapter(XMLGregorianCalendar.class, new MetaInfo.Deserializer())
//					.registerTypeAdapter(XMLGregorianCalendar.class, new MetaInfo.Serializer()).create();

			// json = gson.toJson(tilesReq);
			json = gson.toJson(pt);

		} else if (req.equals(Constant.search)) {
			// layer=지적&mbr=192,121,121,121&code=C00231&tiles=tile1,tile2&levelid=10
			String version = request.getParameter("tileSet");
			String layer = request.getParameter("layer").toLowerCase();

//			String user = request.getParameter("user");
//			if(user != null) {
//				searchUserLogMode = true;
//			}

			if (TileServiceMng.isSearchLog && layer.equals("fa")) {
				searchLogMode = true;
			}

			double minx = -1;
			double miny = -1;
			double maxx = -1;
			double maxy = -1;

			// int selectLevel = -1;
			String mbr = request.getParameter("mbr");
			boolean isMbr = false;
			if (mbr != null) {

				String[] coords = mbr.split(",");
				minx = Double.parseDouble(coords[0]);
				miny = Double.parseDouble(coords[1]);
				maxx = Double.parseDouble(coords[2]);
				maxy = Double.parseDouble(coords[3]);
				selectLevel = this.getLevel(minx, miny, maxx, maxy);
				isMbr = true;
			}

			String circle = request.getParameter("circle");
			boolean isCircle = false;

			Circle cc = new Circle();

			if (circle != null) {

				String[] coords = circle.split(",");

				if (coords.length == 3) {
					cc.radius = Double.parseDouble(coords[0]);
					cc.x = Double.parseDouble(coords[1]);
					cc.y = Double.parseDouble(coords[2]);

					minx = cc.x - cc.radius;
					miny = cc.y - cc.radius;
					maxx = cc.x + cc.radius;
					maxy = cc.y + cc.radius;
					selectLevel = this.getLevel(minx, miny, maxx, maxy);
					isCircle = true;
				}
			}

			String polygon = request.getParameter("polygon");
			boolean isPolygon = false;

			Polygon pg = null;

			if (polygon != null) {

				String[] coordsS = polygon.split(",");

				if (coordsS.length >= 4 && coordsS.length % 2 == 0) {

					Coordinate[] coords = new Coordinate[coordsS.length / 2];

					for (int i = 0; i < coordsS.length; i++) {

						if (coords[i / 2] == null) {
							coords[i / 2] = new Coordinate();
						}

						if (i % 2 == 0) {
							coords[i / 2].x = Double.parseDouble(coordsS[i]);
						} else {
							coords[i / 2].y = Double.parseDouble(coordsS[i]);
						}

					}

					LinearRing ring = this.gf.createLinearRing(coords);
					pg = this.gf.createPolygon(ring, null);

					Envelope env = pg.getEnvelopeInternal();

					minx = env.getMinX();
					miny = env.getMinY();
					maxx = env.getMaxX();
					maxy = env.getMaxY();

					selectLevel = this.getLevel(minx, miny, maxx, maxy);

					isPolygon = true;
				}
			}

			String pk = request.getParameter("pk");

			String code = request.getParameter("code");

			String geoType = request.getParameter("geoType");

			if (geoType == null) {
				geoType = "default";
			}

			com.gis2.storage.Version ver = TileServiceMng.versions.get(version);

			ListFeatureCollection rsearch = null;

			FileLayer fl = StorageMng.getFileLayer(ver.layerPath + FileUt.SEPERATOR + layer, false);

			boolean search = true;
			if (fl == null || ((isMbr == true || isPolygon == true || isCircle == true) && selectLevel < 6)) {
				search = false;
			}

			if (search) {
				try {

					if ((isMbr || isCircle || isPolygon) && code == null) {
						rsearch = (ListFeatureCollection) fl.getFeatures(minx, miny, maxx, maxy);
					} else if ((isMbr || isCircle || isPolygon) && code != null) {
						rsearch = (ListFeatureCollection) fl.getFeatures(minx, miny, maxx, maxy, code);
					} else if (pk != null) {
						rsearch = new ListFeatureCollection(fl.getSimpleFeatureType());
						SimpleFeature sf = (SimpleFeature) fl.getFeature(pk);
						rsearch.add(sf);
					}

//					if (mbr != null && minx == maxx && miny == maxy) {
//						rsearch = (ListFeatureCollection) fl.getFeatures(minx, miny, maxx, maxy);
//					} else if(mbr != null && code != null){
//						rsearch = (ListFeatureCollection) fl.getFeatures(minx, miny, maxx, maxy, code);
//					} else if(mbr == null && code != null){
//						rsearch = new ListFeatureCollection(fl.getSimpleFeatureType());
//						SimpleFeature sf  = (SimpleFeature) fl.getFeature(code);
//						rsearch.add(sf);
//					}
				} catch (Exception e) {
					System.out.println(
							"error=" + e.toString() + ", " + request.getRequestURL() + "?" + request.getQueryString());
				}
			}

			ListFeatureCollection lf = null;

			if (rsearch != null) {
				lf = new ListFeatureCollection(fl.getSimpleFeatureType());

				Iterator it = rsearch.iterator();
				while (it.hasNext()) {
					SimpleFeature sf = (SimpleFeature) it.next();

					Geometry geo = (Geometry) sf.getDefaultGeometry();

					if (isCircle) {
						Geometry reqGeo = gf.createPoint(new Coordinate(cc.x, cc.y));
						if (geo.distance(reqGeo) > cc.radius) {
							continue;
						}
					} else if (isMbr || isPolygon) {
						Geometry reqGeo = null;
						if (minx == maxx && miny == maxy) {
							reqGeo = gf.createPoint(new Coordinate(minx, miny));
						} else if (isMbr) {
							Coordinate[] shell = new Coordinate[5];
							shell[0] = new Coordinate(minx, miny);
							shell[1] = new Coordinate(maxx, miny);
							shell[2] = new Coordinate(maxx, maxy);
							shell[3] = new Coordinate(minx, maxy);
							shell[4] = new Coordinate(minx, miny);
							reqGeo = this.gf.createPolygon(shell);
						} else if (isPolygon) {
							reqGeo = pg;
						}

						if (!geo.intersects(reqGeo)) {
							continue;
						}
					}

					if (searchLogMode) {
						if (pnuslog.length() == 0) {
							pnuslog += sf.getAttribute("pnu");
						} else {
							pnuslog += ("," + sf.getAttribute("pnu"));
						}
					}

					if (geoType.equals("center")) {
						sf.setDefaultGeometry(geo.getCentroid());
					} else if (geoType.equals("mbr")) {
						Envelope env = geo.getEnvelopeInternal();
						sf.setDefaultGeometry(JTS.toGeometry(env));
					} else if (geoType.equals("none")) {
						sf.setDefaultGeometry(null);
					}

					lf.add(sf);

					if (searchLogMode) {
						if (pnuslog.length() == 0) {
							pnuslog += sf.getAttribute("pnu");
						} else {
							pnuslog += ("," + sf.getAttribute("pnu"));
						}
					}
				}
			}

			json = this.getGeoJson1(lf, pt);

		} else if (req.equals(Constant.code)) {

			Codes codeRes = new Codes();

			long st1 = System.currentTimeMillis();

			String version = request.getParameter("tileSet");
			String mbr = request.getParameter("mbr");
			String[] coords = mbr.split(",");
			String tiles = request.getParameter("tiles");
			String levelId = request.getParameter("levelId");

			double levelTemp = Double.parseDouble(levelId);

			int levelInt = (int) Math.round(levelTemp);

			String accuracy = request.getParameter("accuracy");

			double minx = Double.parseDouble(coords[0]);
			double miny = Double.parseDouble(coords[1]);
			double maxx = Double.parseDouble(coords[2]);
			double maxy = Double.parseDouble(coords[3]);

			com.gis2.storage.Version ver = TileServiceMng.versions.get(version);

			HashMap<String, HashMap<String, String>> allCodes = new HashMap();

			String[] tilesA = tiles.split(",");

			for (int i = 0; i < tilesA.length; i++) {

				MapData data = ver.getMapData(tilesA[i]);

				MapContext context = data.mapContexts.get(levelInt);

				Vector<Layer> layers = context.getLayers();

				HashMap<String, FileLayer> selectLayers = new HashMap();

				for (Layer layer : layers) {
					if (layer instanceof VectorLayer) {
						VectorLayer vl = (VectorLayer) layer;
						if (!vl.getName().toLowerCase().startsWith("f")
								&& !vl.getName().toLowerCase().startsWith("g")) {
							selectLayers.put(vl.getName(), vl.getFileReader());
						}
					}
				}

				Polygon mbrP = null;
				if (accuracy.equals("true")) {
					Coordinate[] shell = new Coordinate[5];
					shell[0] = new Coordinate(minx, miny);
					shell[1] = new Coordinate(maxx, miny);
					shell[2] = new Coordinate(maxx, maxy);
					shell[3] = new Coordinate(minx, maxy);
					shell[4] = new Coordinate(minx, miny);
					try {
						mbrP = this.gf.createPolygon(shell);
					} catch (Exception e) {
						System.out.println("error : mbr = " + mbr);
						e.printStackTrace();
						break;
					}
				}

				Set set = selectLayers.keySet();
				Iterator it = set.iterator();
				while (it.hasNext()) {
					Object key = it.next();
					FileLayer fl = selectLayers.get(key);

					int selectFieldIdx = -1;

					if (fl.getSimpleFeatureType().indexOf("mnum") > -1) {
						selectFieldIdx = fl.getSimpleFeatureType().indexOf("mnum");
					}

					if (selectFieldIdx == -1 && fl.getSimpleFeatureType().indexOf("atrb_se") > -1) {
						selectFieldIdx = fl.getSimpleFeatureType().indexOf("atrb_se");
					}

					try {

						if (accuracy.equals("true")) {
							SimpleFeatureCollection sfc = fl.getFeatures(minx, miny, maxx, maxy);
							SimpleFeatureIterator sfit = sfc.features();
							while (sfit.hasNext()) {
								SimpleFeature sf = sfit.next();
								Geometry geo = (Geometry) sf.getDefaultGeometry();

								if (geo.intersects(mbrP)) {
									String code = (String) sf.getAttribute(selectFieldIdx);
									// allCodes.put((String) code, (String) code);
									this.setCodes(allCodes, (String) key, code);
								}
							}

						} else {
							Vector codes = fl.getCodes(minx, miny, maxx, maxy);
							for (Object code : codes) {
								// allCodes.put((String) code, (String) code);
								this.setCodes(allCodes, (String) key, (String) code);
							}
						}

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			// System.out.println(version+","+mbr+","+tiles+","+levelId+","+accuracy);

			Vector<String> codeList = new Vector();

			Set set1 = allCodes.keySet();
			Iterator it1 = set1.iterator();
			while (it1.hasNext()) {

				List<com.mapplan.Layer> lys = codeRes.getLayer();

				String key = (String) it1.next();

				com.mapplan.Layer ly = new com.mapplan.Layer();
				ly.setName(key);
				lys.add(ly);

				HashMap<String, String> codes = allCodes.get(key);

				Set set2 = codes.keySet();
				Iterator it2 = set2.iterator();
				while (it2.hasNext()) {
					String code = (String) it2.next();
					ly.getCode().add(code);
				}
			}

			pt.setBody(codeRes);

			json = gson.toJson(pt);

			// json = gson.toJson(codeRes);

			long et1 = System.currentTimeMillis();
			// System.out.println("code, time=" + (et1-st1)/1000.0 + json);
			// } else if (req.equals("analysis")) {
		} else if (req.equals(Constant.analysis)) {

			Analysis an = new Analysis();

			// <레이어 이름,<코드,면적>>
			// HashMap<String, HashMap<String, Double>> codes = new HashMap();
			
			HashMap<String, HashMap<String, Geometry>> codes = new HashMap();

			String version = request.getParameter("tileSet");
			String pnus = request.getParameter("pnus");
			com.gis2.storage.Version ver = TileServiceMng.versions.get(version);

			String versionPath = "";
			SimpleFeature sf = null;
			if (ver.jijukName.length() > 0) {
				FileLayer fl = StorageMng.getFileLayer(ver.layerPath + FileUt.SEPERATOR + ver.jijukName, false);
				try {
					sf = fl.getFeature(pnus);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (sf != null) {
				Geometry jijukGeo = (Geometry) sf.getDefaultGeometry();

				// System.out.println("jijuk area = " + jijukGeo.getArea());
				Envelope jijukEnv = jijukGeo.getEnvelopeInternal();
				Set set = ver.jiguNames.keySet();
				Iterator it = set.iterator();
				while (it.hasNext()) {
					String layerName = (String) it.next();

					FileLayer fl = StorageMng.getFileLayer(ver.layerPath + FileUt.SEPERATOR + layerName, false);

					try {
						SimpleFeatureCollection sfc = fl.getFeatures(jijukEnv.getMinX(), jijukEnv.getMinY(),
								jijukEnv.getMaxX(), jijukEnv.getMaxY());

						int selectFieldIdx = -1;

						if (fl.getSimpleFeatureType().indexOf("mnum") > -1) {
							selectFieldIdx = fl.getSimpleFeatureType().indexOf("mnum");
						}

						if (selectFieldIdx == -1 && fl.getSimpleFeatureType().indexOf("atrb_se") > -1) {
							selectFieldIdx = fl.getSimpleFeatureType().indexOf("atrb_se");
						}
						
						if(selectFieldIdx == -1) {
							System.out.println("layerName =" + layerName+", not exist mnum or atrb_se");
						}
						
						SimpleFeatureIterator sfi = sfc.features();
						while (sfi.hasNext()) {
							SimpleFeature feature = sfi.next();
							Geometry geo = (Geometry) feature.getDefaultGeometry();
							if (geo.intersects(jijukGeo)) {

								Geometry intersection = geo.intersection(jijukGeo);
								
								intersection = extractPolygons(intersection, this.gf);

								if (intersection == null || intersection.getArea() == 0) {
									continue;
								}
								if(selectFieldIdx > -1) {
									String code = (String) feature.getAttribute(selectFieldIdx);
									this.setArea2(codes, layerName, code, intersection);
								}
							}
						}

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			Set set1 = codes.keySet();
			Iterator it1 = set1.iterator();
			while (it1.hasNext()) {
				String layerName = (String) it1.next();

				List<LayerArea> ly = an.getLayer();

				LayerArea la = new LayerArea();
				ly.add(la);

				la.setName(layerName);

				List<CodeArea> cas = la.getCodes();

				HashMap<String, Geometry> code = codes.get(layerName);

				Set set2 = code.keySet();
				Iterator it2 = set2.iterator();
				while (it2.hasNext()) {
					String cd = (String) it2.next();

					// double area = code.get(cd);
					Geometry area = code.get(cd);

					CodeArea ca = new CodeArea();
					ca.setCode(cd);
					ca.setArea(area.getArea());
					cas.add(ca);
				}

			}

			pt.setBody(an);

			json = gson.toJson(pt);

			if (TileServiceMng.isAnalysisLog) {
				analysisLogMode = true;
				pnuslog = pnus;
			}

		}
		else if(req.equals(Constant.pnuGeo)) {
//			Analysis an = new Analysis();

			// <레이어 이름,<코드,면적>>
			// HashMap<String, HashMap<String, Double>> codes = new HashMap();
			
			HashMap<String, HashMap<String, Geometry>> codes = new HashMap();

			String version = request.getParameter("tileSet");
			String pnus = request.getParameter("pnus");
			com.gis2.storage.Version ver = TileServiceMng.versions.get(version);

			String versionPath = "";
			SimpleFeature sf = null;
			if (ver.jijukName.length() > 0) {
				FileLayer fl = StorageMng.getFileLayer(ver.layerPath + FileUt.SEPERATOR + ver.jijukName, false);
				try {
					sf = fl.getFeature(pnus);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (sf != null) {
				Geometry jijukGeo = (Geometry) sf.getDefaultGeometry();

				// System.out.println("jijuk area = " + jijukGeo.getArea());
				Envelope jijukEnv = jijukGeo.getEnvelopeInternal();
				Set set = ver.jiguNames.keySet();
				Iterator it = set.iterator();
				while (it.hasNext()) {
					String layerName = (String) it.next();

					FileLayer fl = StorageMng.getFileLayer(ver.layerPath + FileUt.SEPERATOR + layerName, false);

					try {
						SimpleFeatureCollection sfc = fl.getFeatures(jijukEnv.getMinX(), jijukEnv.getMinY(),
								jijukEnv.getMaxX(), jijukEnv.getMaxY());

						int selectFieldIdx = -1;

						if (fl.getSimpleFeatureType().indexOf("mnum") > -1) {
							selectFieldIdx = fl.getSimpleFeatureType().indexOf("mnum");
						}

						if (selectFieldIdx == -1 && fl.getSimpleFeatureType().indexOf("atrb_se") > -1) {
							selectFieldIdx = fl.getSimpleFeatureType().indexOf("atrb_se");
						}
						
						if(selectFieldIdx == -1) {
							System.out.println("layerName =" + layerName+", not exist mnum or atrb_se");
						}

						SimpleFeatureIterator sfi = sfc.features();
						while (sfi.hasNext()) {
							SimpleFeature feature = sfi.next();
							Geometry geo = (Geometry) feature.getDefaultGeometry();
							if (geo.intersects(jijukGeo)) {

								Geometry intersection = geo.intersection(jijukGeo);
								
								intersection = extractPolygons(intersection, this.gf);

								if (intersection == null || intersection.getArea() == 0) {
									continue;
								}
								
								if(selectFieldIdx > -1) {
									String code = (String) feature.getAttribute(selectFieldIdx);
									this.setArea2(codes, layerName, code, intersection);
								}

							}
						}

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			JSONObject protocolj = new JSONObject();
			
			JSONObject headerj = new JSONObject();

			headerj.put("result", pt.getHeader().getResult());
			headerj.put("resultDesc", pt.getHeader().getResultDesc());
			headerj.put("req", pt.getHeader().getReq());
			
			protocolj.put("header", headerj);

			JSONObject bodyj = new JSONObject();
			
			
			
			JSONArray layers = new JSONArray();
			bodyj.put("layer", layers);

			
			Set set1 = codes.keySet();
			Iterator it1 = set1.iterator();
			while (it1.hasNext()) {
				String layerName = (String) it1.next();
				
				
				JSONObject layerObj = new JSONObject();
				
				layers.put(layerObj);
				
				layerObj.put("name", layerName);
				
				JSONArray codesOjb = new JSONArray();
				
				layerObj.put("codes", codesOjb);
				
				HashMap<String, Geometry> code = codes.get(layerName);

				Set set2 = code.keySet();
				Iterator it2 = set2.iterator();
				while (it2.hasNext()) {
					
					String cd = (String) it2.next();
					
					Geometry area = code.get(cd);
					
					GeometryJSON gj = new GeometryJSON(5);
					StringWriter bw = new StringWriter();
					gj.write(area, bw);
					
					JSONObject geoObj = new JSONObject(bw.toString());
					
					JSONObject codeObj = new JSONObject();
					
					codeObj.put("code", cd);
					
					codeObj.put("geometry", geoObj);
					
					codesOjb.put(codeObj);
					
				}

			}


			protocolj.put("body", bodyj);
			
			json = protocolj.toString();
			
			/*
			Set set1 = codes.keySet();
			Iterator it1 = set1.iterator();
			while (it1.hasNext()) {
				String layerName = (String) it1.next();

				List<LayerArea> ly = an.getLayer();

				LayerArea la = new LayerArea();
				ly.add(la);

				la.setName(layerName);

				List<CodeArea> cas = la.getCodes();

				HashMap<String, Geometry> code = codes.get(layerName);

				Set set2 = code.keySet();
				Iterator it2 = set2.iterator();
				while (it2.hasNext()) {
					String cd = (String) it2.next();

					// double area = code.get(cd);
					Geometry area = code.get(cd);

					CodeArea ca = new CodeArea();
					ca.setCode(cd);
					ca.setArea(area.getArea());
					cas.add(ca);
				}

			}
			
			pt.setBody(an);

			json = gson.toJson(pt);
			*/
			if (TileServiceMng.isAnalysisLog) {
				analysisLogMode = true;
				pnuslog = pnus;
			}
			
		}
		
		// else if(req.equals("pnu_analysis")) {
		else if (req.equals(Constant.pnuAnalysis)) {

			JSONObject values = new JSONObject();

			JSONArray valueArray = new JSONArray();

			values.put("jigu_info", valueArray);

//			Vector<String> jiguNames = new Vector();
//			jiguNames.add("ca");
//			jiguNames.add("da");
//			jiguNames.add("db");
//			jiguNames.add("dc");
//			jiguNames.add("ea");

			String version = request.getParameter("tileSet");
			String pnus = request.getParameter("pnu");
			com.gis2.storage.Version ver = TileServiceMng.versions.get(version);

			String versionPath = "";
			SimpleFeature sf = null;
			if (ver.jijukName.length() > 0) {
				FileLayer fl = StorageMng.getFileLayer(ver.layerPath + FileUt.SEPERATOR + ver.jijukName, false);
				try {
					sf = fl.getFeature(pnus);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (sf != null) {
				Geometry jijukGeo = (Geometry) sf.getDefaultGeometry();

				Envelope jijukEnv = jijukGeo.getEnvelopeInternal();

				for (String layerName : ver.pnuAnalysisLayers) {
					FileLayer fl = StorageMng.getFileLayer(ver.layerPath + FileUt.SEPERATOR + layerName, false);
					if (fl != null) {
						SimpleFeatureCollection sfc = null;
						try {
							sfc = fl.getFeatures(jijukEnv.getMinX(), jijukEnv.getMinY(), jijukEnv.getMaxX(),
									jijukEnv.getMaxY());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (sfc != null) {
							SimpleFeatureIterator sfi = sfc.features();
							while (sfi.hasNext()) {
								SimpleFeature feature = sfi.next();
								Geometry geo = (Geometry) feature.getDefaultGeometry();
								if (geo.intersects(jijukGeo)) {

									int selectFieldIdx = -1;

									if (fl.getSimpleFeatureType().indexOf("mnum") > -1) {
										selectFieldIdx = fl.getSimpleFeatureType().indexOf("mnum");
									}

									if (selectFieldIdx == -1 && fl.getSimpleFeatureType().indexOf("atrb_se") > -1) {
										selectFieldIdx = fl.getSimpleFeatureType().indexOf("atrb_se");
									}

									String id = (String) feature.getAttribute("present_sn");
									String jiguCode = null;

									if (selectFieldIdx > -1) {
										jiguCode = (String) feature.getAttribute(selectFieldIdx);
									}

									String wtnnc_sn = (String) feature.getAttribute("wtnnc_sn");

									String fd_code = (String) feature.getAttribute("fd_code");
									JSONObject jiguJson = new JSONObject();
									jiguJson.put("present_sn", id == null ? "" : id);
									jiguJson.put("code", jiguCode == null ? "" : jiguCode);
									jiguJson.put("wtnnc_sn", wtnnc_sn == null ? "" : wtnnc_sn);
									jiguJson.put("fd_code", fd_code == null ? "" : fd_code);
									valueArray.put(jiguJson);
								}
							}
						}
					}
				}
			}

			JSONObject protocolj = new JSONObject();

			JSONObject headerj = new JSONObject();

			headerj.put("result", pt.getHeader().getResult());
			headerj.put("resultDesc", pt.getHeader().getResultDesc());
			headerj.put("req", pt.getHeader().getReq());

			JSONObject bodyj = new JSONObject();

			bodyj.put("jigu_info", valueArray);

			protocolj.put("header", headerj);
			protocolj.put("body", bodyj);

			// json = values.toString();
			json = protocolj.toString();
		}
		// else if(req.equals("update")) {

		else if (req.equals(Constant.roadIntersection)) {

			String version = request.getParameter("tileSet");
			String pnu_code = request.getParameter("pnu");
			com.gis2.storage.Version ver = TileServiceMng.versions.get(version);
			
			String roadInfos = request.getParameter("roadInfos");
			
			String snapS = request.getParameter("snap");
			
			double snap = 1;
			
			if(snapS != null) {
				snap = Double.parseDouble(snapS);
			}
			
			//System.out.println("roadInfo=" + roadInfos);
			
			Vector<ListFeatureCollection> lfcs = getRoadsIntersaction(version, pnu_code, roadInfos , snap);
			
			//public static Vector<ListFeatureCollection> getRoadsIntersaction(String tileSet, String pnu, String roadInfo_){

			json = this.getGeoJson2(lfcs, pt);

		} else if (req.equals(Constant.update)) {
			TileServiceMng.reInit();
			return;
		}
		// else if(req.equals("updatetile")) {
		else if (req.equals(Constant.updatetile)) {
			TileServiceMng.reloadTile();
			return;
		}
		else if(req.equals(Constant.layersSave)) {
			if(JobMng.isSaveServer) {
				JSONObject protocolj = new JSONObject();
	
				JSONObject headerj = new JSONObject();
	
				headerj.put("result", pt.getHeader().getResult());
				headerj.put("resultDesc", pt.getHeader().getResultDesc());
				headerj.put("req", pt.getHeader().getReq());
	
				JSONObject bodyj = new JSONObject();
				
				String jobProcess = request.getParameter("jobProcess");
				
				String tileSet = request.getParameter(Constant.tileSet);
				String tile = request.getParameter("tile");
				String levelId = request.getParameter("levelId");
				String mbr = request.getParameter("mbr");
				String option = request.getParameter("option");
				String layers = request.getParameter("layers");
				String dxf = request.getParameter("dxf");
				
				String jobId = request.getParameter("jobId");
				
				if(jobProcess != null && jobProcess.equals("start")) {
					
					Job job = new Job(tileSet, tile, levelId, mbr, option, layers, dxf);
					
					if(job.validate()) {
						this.jobMng.run(job);
						Job ingJob = this.jobMng.getJobStatus(job.id);
						bodyj.put("jobId", job.getId());
						bodyj.put("jobStatus", job.current);
						bodyj.put("queueCnt", ingJob.queueCnt);
						bodyj.put("error", ingJob.error);
					}
					else {
						bodyj.put("jobId", "");
						bodyj.put("jobStatus", job.current);
						bodyj.put("queueCnt", job.queueCnt);
						bodyj.put("error", job.error);
					}
					
				}
				else if(jobProcess != null && jobProcess.equals("status")) {
					
					Job job = this.jobMng.getJobStatus(jobId);
					
					if(job == null) {
						bodyj.put("jobId", "");
						bodyj.put("jobStatus", Job.ERROR);
						bodyj.put("queueCnt", "");
						bodyj.put("error", Job.ERROR_JOBID_NOT_EXIST);
					}
					else {
						bodyj.put("jobId", job.id);
						bodyj.put("jobStatus", job.current);
						bodyj.put("error", job.error);
						if(job.current == Job.END || job.current == Job.ERROR) {
							bodyj.put("queueCnt", 0);
							this.jobMng.removeJob(jobId);
						}
						else {
							bodyj.put("queueCnt", job.queueCnt);
						}
					}
					
				}
				else if(jobProcess != null && jobProcess.equals("stop")) {
					
					Job job = this.jobMng.getJobStatus(jobId);
					
					if(job == null) {
						bodyj.put("jobId", "");
						bodyj.put("jobStatus", Job.ERROR);
						bodyj.put("queueCnt", "");
						bodyj.put("error", Job.ERROR_JOBID_NOT_EXIST);
					}
					else {
						
						this.jobMng.stop(jobId);
						
						bodyj.put("jobId", job.id);
						bodyj.put("jobStatus", job.current);
						bodyj.put("queueCnt", job.queueCnt);
						bodyj.put("error", job.error);
						this.jobMng.removeJob(jobId);
					}
				}
				
				protocolj.put("header", headerj);
				protocolj.put("body", bodyj);
	
				// json = values.toString();
				json = protocolj.toString();
			}
			else {
				
		        try {
		        	
		            URL url = new URL("http://"+JobMng.saveServer + request.getRequestURI() +"?"+ request.getQueryString());
		            
		            //URL url = new URL("http://"+JobMng.saveServer + request.getRequestURI() +"?ack=0");

		            // HttpURLConnection 열기
		            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		            //connection.setRequestProperty("referer", refer);

		            // 타임아웃 설정
		            connection.setConnectTimeout(3000);
		            connection.setReadTimeout(3000);

		            // InputStream 읽기
		            
		            InputStream inputStream = connection.getInputStream();
	                json = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
	                System.out.println("Received to saveServer : " + json);

	                
//		            try (InputStream inputStream = connection.getInputStream()) {
//		                // IOUtils로 문자열 변환
//		                json = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
//		                System.out.println("Received to saveServer : " + json);
//		            }
		            
		        } catch (Exception e) {
		        	e.printStackTrace();
		        	
					JSONObject protocolj = new JSONObject();
					
					JSONObject headerj = new JSONObject();
		
					headerj.put("result", pt.getHeader().getResult());
					headerj.put("resultDesc", pt.getHeader().getResultDesc());
					headerj.put("req", pt.getHeader().getReq());
		
					JSONObject bodyj = new JSONObject();
					
					bodyj.put("jobId", "");
					bodyj.put("jobStatus", Job.ERROR);
					bodyj.put("queueCnt", "");
					bodyj.put("error", Job.ERROR_SERVER_ERROR);

					protocolj.put("header", headerj);
					protocolj.put("body", bodyj);
		
					// json = values.toString();
					json = protocolj.toString();
		            
		        }
			}
		}
		else if(req.equals(Constant.slope)) {
			
			String pnuTileSet = request.getParameter("pnu_tileSet");
			String pnuLayer = request.getParameter("pnu_layer");
			String pnu = request.getParameter("pnu");
			String slopeTileSet = request.getParameter("slope_tileSet");
			String slopeLayer = request.getParameter("slope_layer");
			
			com.gis2.storage.Version ver = TileServiceMng.versions.get(pnuTileSet);
			
			FileLayer pnuFileLayer =  ver.layers.get(pnuLayer);
			
			com.gis2.storage.Version ver1 = TileServiceMng.versions.get(slopeTileSet);
			
			FileLayer slopeFileLayer =  ver1.layers.get(slopeLayer);
			
			
			SimpleFeature sf = null;
			try {
				sf = pnuFileLayer.getFeature(pnu);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Geometry geo = (Geometry) sf.getDefaultGeometry();
			
			Envelope env = geo.getEnvelopeInternal();
			
			ListFeatureCollection slopelfc = new ListFeatureCollection(slopeFileLayer.getSimpleFeatureType());
			
			try {
				Vector pageIds = slopeFileLayer.getFeatureIdxs(env.getMinX(), env.getMinY(), env.getMaxX(), env.getMaxY());
				
				for (int i = 0; i < pageIds.size(); i++) {
					
					long pageId = (long) pageIds.get(i);
					SimpleFeature slopesf = slopeFileLayer.readFeature(pageId);
					Geometry slopeGeo = (Geometry) slopesf.getDefaultGeometry();
					
					if (geo.contains(slopeGeo)) {
						slopelfc.add(slopesf);
					} else if (geo.intersects(slopeGeo)) {
						Geometry clipGeo = geo.intersection(slopeGeo);
						slopesf.setDefaultGeometry(clipGeo);
						slopelfc.add(slopesf);
					}
					
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			
			json = this.getGeoJson1(slopelfc, pt);
			
			long ed = System.currentTimeMillis();
			
			
			System.out.println("time = " +(ed -st)/1000.0+", size="+json.length()/1024/1024 +" m"+", area = " + geo.getArea());
		}
		
		
		if (req.equals(Constant.tile) && pt.getHeader().getResult().equals("0000")) {

		} else {
			String callBack = request.getParameter("callback");
			if (callBack == null || callBack.indexOf("http") > -1) {
				response.setContentType("application/json; charset=utf-8");
				byte[] bodyByte = json.getBytes("utf-8");
				BufferedOutputStream boss = new BufferedOutputStream(response.getOutputStream());
				boss.write(bodyByte);
				boss.flush();

				boss.close();
			} else {
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), "utf-8"));
				bw.write(callBack + "(" + json + ")");
				bw.flush();
				bw.close();
			}
		}

		final String tileNameSTemp = tileNameS;
		final String layerSTemp = layerS;

		final boolean searchLogModeTemp = searchLogMode;

		Callable task1 = () -> {
			Boolean ok = false;
			if (AuthorityMng.isAuthority && pt.getHeader().getResult().equals("0000") && !req.equals(Constant.tiles)) {

				int userCnt = AuthorityMng.addCnt(keyS, req, tileNameSTemp, layerSTemp, -1);
				
				System.out.println("addCnt="+",key="+keyS+",req="+req+",cnt="+userCnt);

				if (AuthorityMng.conServers.size() > 0) {

					String uri = AuthorityMng.uri;

					for (String server : AuthorityMng.conServers) {

						if (server.length() == 0) {
							continue;
						}

						if (!AuthorityMng.liveServers.get(server)) {
							continue;
						}
						
						String urlS = "http://" + server + uri + "?req=user&" + Constant.key + "=" + keyS + "&"
								+ Constant.functionName + "=" + req;

						if (tileNameSTemp != null) {
							urlS += ("&" + Constant.tileName + "=" + tileNameSTemp);
						}
						if (layerSTemp != null) {
							urlS += ("&" + Constant.layerName + "=" + layerSTemp);
						}

						urlS += ("&" + Constant.count + "=" + userCnt);

						System.out.println(urlS);

						URL url = new URL(urlS);

						String value = IOUtils.toString(url, "utf-8");
						
						ok = true;
					}
				}
			}

			return ok;
		};
		
		Future ft1 = AuthorityMng.tpool.submit(task1);

		if (searchLogMode) {
			TileServiceMng.searchLogger.debug(this.getClientIP(request) + "|" + pnuslog);
		}

		if (analysisLogMode) {
			TileServiceMng.analysisLogger.debug(this.getClientIP(request) + "|" + pnuslog);
		}

		if (TileServiceMng.isLoadLog) {
			long et = System.currentTimeMillis();
			if (req.equals("tiles")) {
				synchronized (TileServiceMng.tiles) {
					TileServiceMng.tiles.totalCnt++;
					TileServiceMng.tiles.addSize(json.length());
					TileServiceMng.tiles.addTime(et - st);
					TileServiceMng.tiles.cnt++;
				}
			} else if (req.equals("search")) {
				synchronized (TileServiceMng.search) {
					TileServiceMng.search.totalCnt++;
					TileServiceMng.search.addSize(json.length());
					TileServiceMng.search.addTime(et - st);
					TileServiceMng.search.cnt++;
				}
			} else if (req.equals("code")) {
				synchronized (TileServiceMng.code) {
					TileServiceMng.code.totalCnt++;
					TileServiceMng.code.addSize(json.length());
					TileServiceMng.code.addTime(et - st);
					TileServiceMng.code.cnt++;
				}
			} else if (req.equals("analysis")) {
				synchronized (TileServiceMng.analysis) {
					TileServiceMng.analysis.totalCnt++;
					TileServiceMng.analysis.addSize(json.length());
					TileServiceMng.analysis.addTime(et - st);
					TileServiceMng.analysis.cnt++;
				}
			}

			if (TileServiceMng.isReqLog) {
				if (!req.equals("timg")) {
					String jsonLen = String.format("%.2f", (json.length() / 1024.0));
					String time = String.format("%.2f", (et - st) / 1000.0);
					String log = this.getClientIP(request) + "|" + time + " sec|" + jsonLen + " kbyte|" + "|level="
							+ selectLevel + "," + request.getRequestURL() + "?" + request.getQueryString();
					TileServiceMng.reqLogger.debug(log);
				}
			}

		}

	}

	public static String getGeoJson(ListFeatureCollection features) {
		String geojson = "";

		GeometryJSON gj = new GeometryJSON(5);
		
		FeatureJSON fj = new FeatureJSON(gj);
		// fj.setEncodeNullValues(encodeNullValues);
		// String data = "";
		fj.setEncodeFeatureBounds(false);
		fj.setEncodeFeatureCollectionBounds(false);
		fj.setEncodeFeatureCollectionCRS(false);
		fj.setEncodeFeatureCRS(false);
		fj.setEncodeNullValues(true);
		JSONArray featureArray = new JSONArray();
		if (features != null && features.size() > 0) {

			Iterator<SimpleFeature> it = features.iterator();
			while (it.hasNext()) {
				StringWriter bw = new StringWriter();
				SimpleFeature sf = it.next();

				try {
					fj.writeFeature(sf, bw);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				featureArray.put(new JSONObject(bw.toString()));
			}
		}
		JSONObject featureCollectionj = new JSONObject();
		featureCollectionj.put("type", "FeatureCollection");
		featureCollectionj.put("features", featureArray);

		return featureCollectionj.toString();
	}
	
	public static JSONObject getGeoJsonObject(ListFeatureCollection features) {
		String geojson = "";

		GeometryJSON gj = new GeometryJSON(5);
		
		
		
		FeatureJSON fj = new FeatureJSON(gj);
		// fj.setEncodeNullValues(encodeNullValues);
		// String data = "";
		fj.setEncodeFeatureBounds(false);
		fj.setEncodeFeatureCollectionBounds(false);
		fj.setEncodeFeatureCollectionCRS(false);
		fj.setEncodeFeatureCRS(false);
		fj.setEncodeNullValues(true);
		JSONArray featureArray = new JSONArray();
		if (features != null && features.size() > 0) {

			Iterator<SimpleFeature> it = features.iterator();
			while (it.hasNext()) {
				StringWriter bw = new StringWriter();
				SimpleFeature sf = it.next();

				try {
					fj.writeFeature(sf, bw);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				featureArray.put(new JSONObject(bw.toString()));
			}
		}
		JSONObject featureCollectionj = new JSONObject();
		featureCollectionj.put("type", "FeatureCollection");
		featureCollectionj.put("features", featureArray);

		return featureCollectionj;
	}

	
	
	public static String getGeoJson(Vector<ListFeatureCollection> lfcs) {
		String geojson = "";

		GeometryJSON gj = new GeometryJSON(5);
		FeatureJSON fj = new FeatureJSON(gj);
		// fj.setEncodeNullValues(encodeNullValues);
		// String data = "";
		fj.setEncodeFeatureBounds(false);
		fj.setEncodeFeatureCollectionBounds(false);
		fj.setEncodeFeatureCollectionCRS(false);
		fj.setEncodeFeatureCRS(false);
		fj.setEncodeNullValues(true);
		JSONArray featureArray = new JSONArray();
		
		
		for(ListFeatureCollection features : lfcs) {
			if (features != null && features.size() > 0) {
	
				Iterator<SimpleFeature> it = features.iterator();
				while (it.hasNext()) {
					StringWriter bw = new StringWriter();
					SimpleFeature sf = it.next();
	
					try {
						fj.writeFeature(sf, bw);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	
					featureArray.put(new JSONObject(bw.toString()));
				}
			}
		}
		
		JSONObject featureCollectionj = new JSONObject();
		featureCollectionj.put("type", "FeatureCollection");
		featureCollectionj.put("features", featureArray);

		return featureCollectionj.toString();
	}
	

	public static String getGeoJson1(ListFeatureCollection features, Protocol pt) {
		String geojson = "";

		GeometryJSON gj = new GeometryJSON(2);
		FeatureJSON fj = new FeatureJSON(gj);
		// fj.setEncodeNullValues(encodeNullValues);
		// String data = "";
		fj.setEncodeFeatureBounds(false);
		fj.setEncodeFeatureCollectionBounds(false);
		fj.setEncodeFeatureCollectionCRS(false);
		fj.setEncodeFeatureCRS(false);
		fj.setEncodeNullValues(true);
		JSONArray featureArray = new JSONArray();
		if (features != null && features.size() > 0) {

			Iterator<SimpleFeature> it = features.iterator();
			while (it.hasNext()) {
				StringWriter bw = new StringWriter();
				SimpleFeature sf = it.next();

				try {
					fj.writeFeature(sf, bw);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				featureArray.put(new JSONObject(bw.toString()));
			}
		}

		JSONObject protocolj = new JSONObject();

		JSONObject headerj = new JSONObject();

		headerj.put("result", pt.getHeader().getResult());
		headerj.put("resultDesc", pt.getHeader().getResultDesc());
		headerj.put("req", pt.getHeader().getReq());

		JSONObject bodyj = new JSONObject();

		JSONObject featureCollectionj = new JSONObject();
		featureCollectionj.put("type", "FeatureCollection");
		featureCollectionj.put("features", featureArray);

		bodyj.put("geojson", featureCollectionj);

		protocolj.put("header", headerj);
		protocolj.put("body", bodyj);

		return protocolj.toString();
	}
	
	public static String getGeoJson2(Vector<ListFeatureCollection> lfcs, Protocol pt) {
		String geojson = "";

		GeometryJSON gj = new GeometryJSON(2);
		FeatureJSON fj = new FeatureJSON(gj);
		// fj.setEncodeNullValues(encodeNullValues);
		// String data = "";
		fj.setEncodeFeatureBounds(false);
		fj.setEncodeFeatureCollectionBounds(false);
		fj.setEncodeFeatureCollectionCRS(false);
		fj.setEncodeFeatureCRS(false);
		fj.setEncodeNullValues(true);
		JSONArray featureArray = new JSONArray();
		
		/*
		for(Vector<SimpleFeature> features : lfcs) {
			
			for(SimpleFeature sf : features) {
				StringWriter bw = new StringWriter();

				try {
					fj.writeFeature(sf, bw);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				featureArray.put(new JSONObject(bw.toString()));

			}
			
		}
		*/
		
		for(ListFeatureCollection features : lfcs) {
			if (features != null && features.size() > 0) {
	
				Iterator<SimpleFeature> it = features.iterator();
				while (it.hasNext()) {
					StringWriter bw = new StringWriter();
					SimpleFeature sf = it.next();
	
					try {
						fj.writeFeature(sf, bw);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	
					featureArray.put(new JSONObject(bw.toString()));
				}
			}
		}
		
		JSONObject protocolj = new JSONObject();

		JSONObject headerj = new JSONObject();

		headerj.put("result", pt.getHeader().getResult());
		headerj.put("resultDesc", pt.getHeader().getResultDesc());
		headerj.put("req", pt.getHeader().getReq());

		JSONObject bodyj = new JSONObject();

		JSONObject featureCollectionj = new JSONObject();
		featureCollectionj.put("type", "FeatureCollection");
		featureCollectionj.put("features", featureArray);

		bodyj.put("geojson", featureCollectionj);

		protocolj.put("header", headerj);
		protocolj.put("body", bodyj);

		return protocolj.toString();
	}
	

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	public void doTile(HttpServletRequest request, HttpServletResponse response) {

		long st = System.currentTimeMillis();

		String timg = request.getParameter("path");

		// String create = request.getParameter("tiles");

		String debug = request.getParameter("preview");

		String[] urltilePath = timg.split("/");

		// level,xidx,yidx,ratio,mbr
		int sidx = timg.indexOf("/");

		String versionS = urltilePath[0];
		String tileNameS = urltilePath[1];
		String levelS = urltilePath[2];

//		String xS = urltilePath[3];
//		String yS = urltilePath[4];
		String imgFile = urltilePath[3] + "/" + urltilePath[4];

		String tileServiceName = tileNameS;
		// String tilePath = levelS + "/" + xS + "/" + yS + "/" + imgFile;
		// String tilePath = (Integer.parseInt(levelS)-5) + "/" + imgFile;
		String tilePath = (Integer.parseInt(levelS)) + "/" + imgFile;
		// TileMapFactory2 tmf =
		// TileServiceMng.versions.get(versionS).tmss.get(tileServiceName);
		TileMapFactory2 tmf = TileServiceMng.versions.get(versionS).getTileMapFactory(tileServiceName);
		
		if(tmf == null) {
			System.out.println("doTile not exist tileServiceName=" + tileServiceName+", subTile="+versionS);
			return;
		}

		long imgSize = 0;

		try {

			byte[] imgByte = null;

			if (debug == null) {
				imgByte = tmf.getTileByte(tilePath);

				if (imgByte == null) {
					// System.out.println("img is null, " + timg);
				}
			} else {
				int level = 1;

				if (request.getParameter("level") != null) {
					level = Integer.parseInt(request.getParameter("level"));
				} else {
					level = tmf.getLevel(tilePath);
				}

				if (tmf.scales.getScaleRatio(level) < 0) {
					return;
				}

				int xidx = 1;
				int xidxt = 1;
				if (request.getParameter("xidx") != null) {
					xidx = Integer.parseInt(request.getParameter("xidx"));
				} else {
					xidx = tmf.getXIdx(tilePath);
				}

				int yidx = 1;
				int yidxt = 1;
				if (request.getParameter("yidx") != null) {
					yidx = Integer.parseInt(request.getParameter("yidx"));
				} else {
					yidx = tmf.getYIdx(tilePath);
				}

				int ratio = 1;
				if (request.getParameter("ratio") != null) {
					ratio = Integer.parseInt(request.getParameter("ratio"));
				} else {
					ratio = 1;
				}
				String mbr = request.getParameter("mbr");
				Envelope env = null;
				Envelope envt = null;
				if (mbr != null) {
					String[] temps = mbr.split(",");

					env = new Envelope(Double.parseDouble(temps[0]), Double.parseDouble(temps[2]),
							Double.parseDouble(temps[1]), Double.parseDouble(temps[3]));
				} else {
					env = tmf.getMbr(tilePath);
				}

				HashMap<String, byte[]> tiles = null;

				tiles = tmf.getTileImages(level, xidx, yidx, ratio, env, null, null, false);

				Set set = tiles.keySet();
				Iterator it = set.iterator();
				while (it.hasNext()) {
					String tilePathS = (String) it.next();
					imgByte = tiles.get(tilePathS);

				}
			}

			if (debug == null) {
				// System.out.println("img len = " + imgByte.length);
				Date date = tmf.getTileDB().getLastModified();

				if (date != null && !this.isCache(date, request, response)) {
					if (imgByte != null) {
						// System.out.println("img len = " + imgByte.length+", timg="+timg);
						response.setContentType("image/png");
						response.getOutputStream().write(imgByte);
						response.setStatus(HttpServletResponse.SC_OK);
						imgSize += imgByte.length;
					} else {
						// System.out.println("empty image = " + this.tempImg.length+", timg="+timg);
						response.setContentType("image/png");
						response.getOutputStream().write(this.tempImg);
						response.setStatus(HttpServletResponse.SC_OK);
						imgSize += this.tempImg.length;
					}
				} else {
					// System.out.println("isCache "+", timg="+timg);
				}

			} else {
				if (imgByte != null) {
					response.setContentType("image/png");
					response.getOutputStream().write(imgByte);
					response.setStatus(HttpServletResponse.SC_OK);
					imgSize += imgByte.length;
				} else {
					response.setContentType("image/png");
					response.getOutputStream().write(this.tempImg);
					response.setStatus(HttpServletResponse.SC_OK);
					imgSize += this.tempImg.length;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		synchronized (TileServiceMng.timg) {
			TileServiceMng.timg.totalCnt++;
			TileServiceMng.timg.addTime(System.currentTimeMillis() - st);
			TileServiceMng.timg.addSize(imgSize);
			TileServiceMng.timg.cnt++;
		}

		return;
	}

	public boolean isCache(java.util.Date _date, HttpServletRequest request, HttpServletResponse response) {
		java.util.Date date = _date;

		long clientDate = request.getDateHeader("If-Modified-Since");
		long serverDate = date.getTime();

		/*
		 * SimpleDateFormat ff = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); Date imsi
		 * = new Date(); imsi.setTime(clientDate); String clientDt = ff.format(imsi);
		 * imsi.setTime(serverDate); String serverDt = ff.format(imsi);
		 * 
		 * System.out.println(clientDate + "-" + clientDt + "    " + serverDate + "-" +
		 * serverDt);
		 */
		if (clientDate != -1 && clientDate / 1000 <= serverDate / 1000) {
			response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			return true;
		}
		response.setDateHeader("Last-Modified", serverDate);
		return false;
	}

	public void setCodes(HashMap<String, HashMap<String, String>> map, String layerName, String code) {
		if (!map.containsKey(layerName)) {
			map.put(layerName, new HashMap<String, String>());
		}

		HashMap<String, String> layer = map.get(layerName);

		if (!layer.containsKey(code)) {
			layer.put(code, code);
		}

	}

	public void setArea(HashMap<String, HashMap<String, Double>> map, String layerName, String code, double _area) {
		if (!map.containsKey(layerName)) {
			map.put(layerName, new HashMap<String, Double>());
		}

		HashMap<String, Double> layer = map.get(layerName);

		if (!layer.containsKey(code)) {
			layer.put(code, 0.0);
		}

		double area = layer.get(code);
		area += _area;
		layer.put(code, area);
	}

	public void setArea2(HashMap<String, HashMap<String, Geometry>> map, String layerName, String code,
			Geometry _area) {
		if (!map.containsKey(layerName)) {
			map.put(layerName, new HashMap<String, Geometry>());
		}

//		if(code.toLowerCase().equals("udx200")) {
//			System.out.println("udx200 = " + _area.getArea());
//		}

		// GeometryFactory factory = FactoryFinder.getGeometryFactory( null );

		HashMap<String, Geometry> layer = map.get(layerName);

		if (!layer.containsKey(code)) {
			layer.put(code, _area);
		} else {
			Geometry geo = layer.get(code);

			Vector geoCol = new Vector();

			geoCol.add(geo);
			geoCol.add(_area);

			GeometryCollection geometryCollection = (GeometryCollection) this.gf.buildGeometry(geoCol);

			// Geometry newGeo = geo.union(_area);
			Geometry newGeo = geometryCollection.buffer(0);
			
			newGeo = extractPolygons(newGeo, this.gf);
			
			if(newGeo != null) {
				layer.put(code, newGeo);
			}
		}
//		double area = layer.get(code);
//		area+=_area;
//		
//		layer.put(code, area);
	}

	public static String getClientIP(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");

		if (ip == null) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null) {
			ip = request.getRemoteAddr();
		}

		return ip;
	}
	
	public static Vector<ListFeatureCollection> getRoadsIntersaction(String tileSet, String pnu, String roadInfo_, double snap){
		
		Vector<ListFeatureCollection> collVector = new Vector();
		Vector<RoadInfo> roadInfoV = new Vector();
		String version = tileSet;
		String pnu_code = pnu;
		com.gis2.storage.Version ver = TileServiceMng.versions.get(version);
		
		String roadInfos = roadInfo_;
		
		//System.out.println("roadInfo=" + roadInfos);
		
		if(roadInfo_ != null) {
			String[] roadInfoArray = roadInfos.split("\\|");
			
			for(String roadInfo : roadInfoArray) {
				RoadInfo ri = new RoadInfo();
				String[] fds = roadInfo.split(",");
				
				ri.layerName = fds[0];
				ri.fieldName = fds[1];
				String[] values = fds[2].split(":");
				ri.values = new Vector();
				
				for(String value: values) {
					ri.values.add(value);
				}
				roadInfoV.add(ri);
			}
		}
		String versionPath = "";

		SimpleFeature pnuSf = null;

		FileLayer jijukFl = null;

		if (ver.jijukName.length() > 0) {
			jijukFl = StorageMng.getFileLayer(ver.layerPath + FileUt.SEPERATOR + ver.jijukName, false);
			try {
				pnuSf = jijukFl.getFeature(pnu_code);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		Geometry selectGeo = (Geometry) pnuSf.getDefaultGeometry();
		
		if(roadInfoV.size() > 0) {
			
			for(RoadInfo roadInfo : roadInfoV) {
				FileLayer fl = ver.layers.get(roadInfo.layerName);
				//ListFeatureCollection lfc = getRoadIntersaction(selectGeo, fl, roadInfo, null);
				ListFeatureCollection lfc = getRoadIntersaction(pnuSf, fl, roadInfo, null, snap);
				if(lfc != null && lfc.size() > 0) {
					collVector.add(lfc);
				}
			}
		}
		else {
			ListFeatureCollection lfc = getRoadIntersaction(pnuSf, jijukFl, null, pnu_code, snap);
			if(lfc != null && lfc.size() > 0) {
				collVector.add(lfc);
			}
		}
		
		ListFeatureCollection pnuLFC = new ListFeatureCollection(jijukFl.getSimpleFeatureType());
		pnuLFC.add(pnuSf);
		
		
		collVector.add(pnuLFC);
		
		return collVector;
	}
	
	public static ListFeatureCollection getRoadIntersaction(SimpleFeature pnusf, FileLayer fl , RoadInfo roadInfo_, String pnu_, double snap){
		
				
		Geometry selectGeo = (Geometry)pnusf.getDefaultGeometry();

		Envelope env = selectGeo.getEnvelopeInternal();
		
		ListFeatureCollection lfc = null;

		ListFeatureCollection roads = new ListFeatureCollection(fl.getSimpleFeatureType());

		try {
			lfc = (ListFeatureCollection) fl.getFeatures(env.getMinX(), env.getMinY(), env.getMaxX(),
					env.getMaxY());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Vector<SimpleFeature> roadList = new Vector();
		
		Iterator it = lfc.iterator();
		while (it.hasNext()) {

			SimpleFeature sf = (SimpleFeature) it.next();
			
			Geometry geo = null;
			
			if(roadInfo_ != null) {
				String value = (String)sf.getAttribute(roadInfo_.fieldName);
				if(roadInfo_.values.contains(value)) {
					geo = (Geometry) sf.getDefaultGeometry();
				}
			}
//			else if(((String) sf.getAttribute("jibun")).endsWith("도") && !sf.getAttribute("pnu").equals(pnu_)) {
//				geo = (Geometry) sf.getDefaultGeometry();
//			}
			else {
				String jibun = (String) sf.getAttribute("jibun");
				
				if(jibun != null && jibun.endsWith("도") && !sf.getAttribute("pnu").equals(pnu_)) {
					geo = (Geometry) sf.getDefaultGeometry();	
				}
				
			}
			
			if(geo != null) {
			roadList.add(sf);	
//				Geometry interGeo = geo.intersection(selectGeo);
//	
//				if (interGeo != null && (interGeo instanceof LineString || interGeo instanceof MultiLineString)) {
//					sf.setDefaultGeometry(interGeo);
//					roads.add(sf);
//				}
			}
			
		}
		
		if(roadList.size() == 0) {
			return null;
		}
		
		SegmentIntersact si = new SegmentIntersact();
		
		ListFeatureCollection resultList = si.getIntersaction(pnusf, roadList, snap);
		
		return resultList;
	}
	
    public static MultiPolygon extractPolygons(Geometry geometry, GeometryFactory geometryFactory) {
        List<Polygon> polygonList = new ArrayList<>();

        collectPolygons(geometry, polygonList);

        if (polygonList.isEmpty()) {
            return null;
        }

        return geometryFactory.createMultiPolygon(polygonList.toArray(new Polygon[0]));
    }

    private static void collectPolygons(Geometry geometry, List<Polygon> polygonList) {
        if (geometry instanceof Polygon) {
            polygonList.add((Polygon) geometry);
        } else if (geometry instanceof MultiPolygon) {
            int numGeometries = geometry.getNumGeometries();
            for (int i = 0; i < numGeometries; i++) {
                collectPolygons(geometry.getGeometryN(i), polygonList);
            }
        } else if (geometry instanceof GeometryCollection) {
            int numGeometries = geometry.getNumGeometries();
            for (int i = 0; i < numGeometries; i++) {
                collectPolygons(geometry.getGeometryN(i), polygonList);
            }
        }
    }
	
}
