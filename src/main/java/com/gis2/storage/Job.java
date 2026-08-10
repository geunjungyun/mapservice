package com.gis2.storage;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.io.FileUtils;
import org.geotools.geometry.jts.JTS;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.simple.SimpleFeature;

import com.data.file.FileLayer;
import com.data.file.FileLayer.SimpleFeatureIterator;
import com.data.file.MetaInfo;
import com.data.file.ShapeFile;
import com.data.util.MapLog;
import com.gis.map.Layer;
import com.gis.map.MapContext;
import com.gis.map.MapData;
import com.gis.map.VectorLayer;
import com.gis2.servlet.Constant;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapplan.Header;
import com.mapplan.Protocol;
import com.mapplan.User;
import com.mapplan.Users;
import com.util.io.CadConvert;
import com.util2.thread.TileThreadPoolExecutor;

public class Job {

	public static final int STANDBY = 0;
	public static final int ING = 1;
	public static final int END = 2;
	public static final int STOP = 3;
	public static final int ERROR = -1;

	public static final int ERROR_NO = 0;
	public static final int ERROR_SAVE_LAYER_NOT_EXIST = 1;
	public static final int ERROR_JOBID_NOT_EXIST = 2;
	public static final int ERROR_SERVER_ERROR = 3;

	public String id = null;

	public String errorMsg = "";

	public int error = 0;

	public int current = Job.STANDBY;

	String tileSet = null;
	// String tile = null;
	// String levelId = null;
	String mbr = null;
	String option = null;
	String layers = null;

	File savePath = null;

	GeometryFactory gft = new GeometryFactory();

	public boolean runStop = false;

	public int queueCnt = -1;
	
	public String dxf = "";
	
	
	public String dayPath = null;
	
	long regTime = -1;
	
	long startTime = -1;
	long endTime = -1;

	public Job(String tileSet, String tile, String levelId, String mbr, String option, String layers, String dxf) {

		UUID uniqueKey = UUID.randomUUID();

		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy"+File.separator+"yyyyMMdd"+File.separator);
		dayPath = formatter.format(calendar.getTime());

		
		id = dayPath + uniqueKey.toString();

		this.tileSet = tileSet;
//		this.tile = tile;
//		this.levelId = levelId;
		this.mbr = mbr;
		this.option = option;
		this.layers = layers;
		this.dxf = dxf;
		
		regTime = System.currentTimeMillis();
	}

	public String printCur() {
		if (this.current == Job.STANDBY) {
			return "STANDBY";
		} else if (this.current == Job.ING) {
			return "ING";
		} else if (this.current == Job.END) {
			return "END";
		} else if (this.current == Job.ERROR) {
			return "ERROR";
		} else if (this.current == Job.STOP) {
			return "STOP";
		}
		return null;
	}

	public String getId() {
		return this.id;
	}

	public boolean validate() {
		boolean ok = true;
		this.current = ERROR;
		this.error = this.ERROR_SAVE_LAYER_NOT_EXIST;

		Version version = TileServiceMng.versions.get(tileSet);

		if (version == null) {
			MapLog.getSCLog().error("tileSet is null");
			return false;
		}

		Vector<String> vlns = new Vector();

		if (this.layers == null) {
			MapLog.getSCLog().error("layser name is null");
			return false;
		}
		
		if (this.layers != null) {
			String[] layerNames = this.layers.split(",");
			for (String ln : layerNames) {

				String[] lyinfo = ln.split("\\|");
				vlns.add(lyinfo[0]);
			}
		}

		int cnt = 0;
		Set set = version.layers.keySet();
		Iterator it = set.iterator();
		while (it.hasNext()) {
			String layerName = (String) it.next();
			if (vlns.size() > 0 && !vlns.contains(layerName)) {
				continue;
			}
			cnt++;
		}

		if (cnt == 0) {
			return false;
		}

		this.current = this.STANDBY;
		this.error = this.ERROR_NO;
		return ok;
	}

	public void save() {
		this.startTime = System.currentTimeMillis();
		try {
			if (runStop) {
				current = Job.STOP;
				this.release(null);
				return;
			}
			current = Job.ING;

			Version version = TileServiceMng.versions.get(tileSet);

			Vector<String> vlns = new Vector();

			if (this.layers != null) {
				String[] layerNames = this.layers.split(",");
				for (String ln : layerNames) {
					vlns.add(ln);
				}
			}

			String pathS = JobMng.savePath + File.separator + this.id;

			this.savePath = new File(pathS);

			this.savePath.mkdirs();

			double minx = -1;
			double miny = -1;
			double maxx = -1;
			double maxy = -1;

			String[] coords = mbr.split(",");

			minx = Double.parseDouble(coords[0]);
			miny = Double.parseDouble(coords[1]);
			maxx = Double.parseDouble(coords[2]);
			maxy = Double.parseDouble(coords[3]);

			Envelope env = new Envelope(minx, maxx, miny, maxy);

			Polygon pEnv = JTS.toGeometry(env);
			if (runStop) {
				current = Job.STOP;
				this.release(null);
				return;
			}
			// HashMap<String, String> savedLayer = new HashMap();

			for (String layerName : vlns) {

				// if (ly instanceof VectorLayer && !savedLayer.containsKey(ly.getName())) {

				String[] lyinfo = layerName.split("\\|");

				FileLayer fl = version.layers.get(lyinfo[0]);

				if (fl == null || fl.getObjectSize() == 0) {
					continue;
				}

				if (runStop) {
					current = Job.STOP;
					this.release(null);
					return;
				}

				
				int selectCodeIdx = -1;

				if (fl.getSimpleFeatureType().indexOf("mnum") > -1) {
					selectCodeIdx = fl.getSimpleFeatureType().indexOf("mnum");
				}

				if (selectCodeIdx == -1 && fl.getSimpleFeatureType().indexOf("atrb_se") > -1) {
					selectCodeIdx = fl.getSimpleFeatureType().indexOf("atrb_se");
				}
				
				boolean multiSave = false;
				
				HashMap<String, ShapeFile> saves = new HashMap();
				
				Vector<String> codes = new Vector();
				
				if(lyinfo.length > 1) {
					for(int i=1; i<lyinfo.length ; i++) {
						codes.add(lyinfo[i]);
					}
					multiSave = true;
				}
				else {
//					saves.put("all", null);
				}
				
				
				//ShapeFile shp = null;

				if (runStop) {
					current = Job.STOP;
					this.release(saves);
					return;
				}
				try {
					Vector pageIds = fl.getFeatureIdxs(minx, miny, maxx, maxy);

					//System.out.println("save vl name = " + fl.getName() + ", size=" + pageIds.size());
					if (runStop) {
						current = Job.STOP;
						this.release(saves);
						return;
					}

					for (int i = 0; i < pageIds.size(); i++) {
						if (runStop) {
							current = Job.STOP;
							this.release(saves);
							return;
						}
						long pageId = (long) pageIds.get(i);
						SimpleFeature sf = fl.readFeature(pageId);
						Geometry geo = (Geometry) sf.getDefaultGeometry();
						if (option.equals("1") && pEnv.intersects(geo)) {
							
							ShapeFile save = null;
							
							if(multiSave) {
								
								String code = (String)sf.getAttribute(selectCodeIdx);
								
								if(!codes.contains(code)) {
									continue;
								}
								
								save = saves.get(code);
								if(save == null) {
									save = new ShapeFile(new File(pathS + File.separator + fl.getName()+"_"+code +".shp"),
											fl.getSimpleFeatureType(), "utf-8");
									saves.put(code, save);
								}
								
							}
							else {
								save = saves.get("all");
								if(save == null) {
									save = new ShapeFile(new File(pathS + File.separator + fl.getName() + ".shp"),
											fl.getSimpleFeatureType(), "utf-8");
									saves.put("all", save);
								}
							}
							
							save.addFeature(sf);
							
							
						} else if (option.equals("2")) {
							if (pEnv.contains(geo)) {
								ShapeFile save = null;
								
								if(multiSave) {
									
									String code = (String)sf.getAttribute(selectCodeIdx);
									
									if(!codes.contains(code)) {
										continue;
									}
									
									save = saves.get(code);
									if(save == null) {
										save = new ShapeFile(new File(pathS + File.separator + fl.getName()+"_"+code +".shp"),
												fl.getSimpleFeatureType(), "utf-8");
										saves.put(code, save);
									}
									
								}
								else {
									save = saves.get("all");
									if(save == null) {
										save = new ShapeFile(new File(pathS + File.separator + fl.getName() + ".shp"),
												fl.getSimpleFeatureType(), "utf-8");
										saves.put("all", save);
									}
								}
								
								save.addFeature(sf);
							} else if (pEnv.intersects(geo)) {
								ShapeFile save = null;
								
								if(multiSave) {
									
									String code = (String)sf.getAttribute(selectCodeIdx);
									
									if(!codes.contains(code)) {
										continue;
									}
									
									save = saves.get(code);
									if(save == null) {
										save = new ShapeFile(new File(pathS + File.separator + fl.getName()+"_"+code +".shp"),
												fl.getSimpleFeatureType(), "utf-8");
										saves.put(code, save);
									}
									
								}
								else {
									save = saves.get("all");
									if(save == null) {
										save = new ShapeFile(new File(pathS + File.separator + fl.getName() + ".shp"),
												fl.getSimpleFeatureType(), "utf-8");
										saves.put("all", save);
									}
								}
								Geometry clipGeo = geo.intersection(pEnv);
								sf.setDefaultGeometry(clipGeo);
								save.addFeature(sf);
							}
						}
					}
					
					Set set = saves.keySet();
					Iterator it = set.iterator();
					while(it.hasNext()) {
						ShapeFile sf = saves.get(it.next());
						if(sf != null) {
							sf.close();
						}
					}
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					this.release(saves);
					MapLog.getSCLog().debug(e.toString());
					this.errorMsg = e.toString();
					this.current = Job.ERROR;
					return;
				}
			}
			
			String outpath = JobMng.convPath + File.separator + this.id;
			
			String zipFilePath = CadConvert.transfer(pathS, outpath, "klandmap", dxf);
			
			this.endTime = System.currentTimeMillis();
	        // 결과 출력
	        if (zipFilePath != null) {
	            System.out.println("변환 및 압축 성공: " + zipFilePath);
	        } else {
	        	throw new Exception("변환 또는 압축 실패!");
	            //System.out.println("변환 또는 압축 실패!");
	        }
			
			
		} catch (Exception e) {
			this.release(null);
			MapLog.getSCLog().debug(e.toString());
			e.printStackTrace();
			this.current = Job.ERROR;
			this.errorMsg = e.toString();
			return;
		}
		
		
		
		current = Job.END;
	}

//	public void release(ShapeFile shp) {
//
//		if (shp != null) {
//			try {
//				shp.close();
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		try {
//			if (this.savePath.exists()) {
//				FileUtils.deleteDirectory(savePath);
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	public void release(HashMap<String, ShapeFile> shps) {

		if(shps != null) {
			Set set = shps.keySet();
			Iterator it = set.iterator();
			while(it.hasNext()) {
				ShapeFile sf = shps.get(it.next());
				if(sf != null) {
					try {
					sf.close();
					}catch (Exception e) {
						e.printStackTrace();
					}
					
				}
			}
		}
		
		try {
			if (this.savePath.exists()) {
				FileUtils.deleteDirectory(savePath);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	/**
	 * srcGeo 타입과 같은 타입의 객체를 리턴
	 * 
	 * @param geo
	 * @param srcGeo
	 * @return
	 */
	public Geometry getGeometry(Geometry geo, Geometry srcGeo) {

		if (srcGeo instanceof Polygon || srcGeo instanceof MultiPolygon) {
			if (geo instanceof Polygon || geo instanceof MultiPolygon) {
				return geo;
			} else if (geo instanceof GeometryCollection) {
				Vector<Polygon> newps = new Vector();
				GeometryCollection geoc = (GeometryCollection) geo;
				int cnt = geoc.getNumGeometries();
				for (int i = 0; i < cnt; i++) {
					Geometry subGeo = geoc.getGeometryN(i);
					if (subGeo instanceof Polygon) {
						newps.add((Polygon) subGeo);
					} else if (subGeo instanceof MultiPolygon) {
						int mcnt = subGeo.getNumGeometries();
						for (int k = 0; k < mcnt; k++) {
							Geometry subPg = subGeo.getGeometryN(k);
							newps.add((Polygon) subPg);
						}
					}
				}

				Polygon[] pls = new Polygon[newps.size()];
				for (int m = 0; m < newps.size(); m++) {
					pls[m] = newps.get(m);
				}
				return gft.createMultiPolygon(pls);
			}
		} else if (srcGeo instanceof LineString || srcGeo instanceof MultiLineString) {
			if (geo instanceof LineString || geo instanceof MultiLineString) {
				return geo;
			} else if (geo instanceof GeometryCollection) {
				Vector<LineString> newps = new Vector();
				GeometryCollection geoc = (GeometryCollection) geo;
				int cnt = geoc.getNumGeometries();
				for (int i = 0; i < cnt; i++) {
					Geometry subGeo = geoc.getGeometryN(i);
					if (subGeo instanceof LineString) {
						newps.add((LineString) subGeo);
					} else if (subGeo instanceof MultiLineString) {
						int mcnt = subGeo.getNumGeometries();
						for (int k = 0; k < mcnt; k++) {
							Geometry subPg = subGeo.getGeometryN(k);
							newps.add((LineString) subPg);
						}
					}
				}

				LineString[] pls = new LineString[newps.size()];
				for (int m = 0; m < newps.size(); m++) {
					pls[m] = newps.get(m);
				}
				return gft.createMultiLineString(pls);
			}
		} else if (srcGeo instanceof Point || srcGeo instanceof MultiPoint) {
			if (geo instanceof Point || geo instanceof MultiPoint) {
				return geo;
			} else if (geo instanceof GeometryCollection) {
				Vector<Point> newps = new Vector();
				GeometryCollection geoc = (GeometryCollection) geo;
				int cnt = geoc.getNumGeometries();
				for (int i = 0; i < cnt; i++) {
					Geometry subGeo = geoc.getGeometryN(i);
					if (subGeo instanceof Point) {
						newps.add((Point) subGeo);
					} else if (subGeo instanceof MultiPoint) {
						int mcnt = subGeo.getNumGeometries();
						for (int k = 0; k < mcnt; k++) {
							Geometry subPg = subGeo.getGeometryN(k);
							newps.add((Point) subPg);
						}
					}
				}

				Point[] pls = new Point[newps.size()];
				for (int m = 0; m < newps.size(); m++) {
					pls[m] = newps.get(m);
				}
				return gft.createMultiPoint(pls);
			}
		}

		return null;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Job job = new Job("reg10", "1", "1", "925460,1895455,1014437,1953620", "1",
				"aa|UQB300|UQB200|UQB100,ab|UQI100|UQM120|UQM110,ca|UQS510|UQV330|UQS103,da,fd,fc,fe","Y");
		TileServiceMng tsm = new TileServiceMng();
		tsm.init();
		// public Job(String tileSet, String tile, String levelId, String mbr, String
		// option, String layers) {

		job.save();
		System.out.println("job id=" + job.getId());
	}

}
