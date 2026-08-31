import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.jar.Attributes.Name;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.compress.compressors.FileNameUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
//import org.apache.log4j.Logger;
import org.slf4j.Logger;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.image.ImageWorker;
import org.geotools.map.GridCoverageLayer;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.geotools.util.logging.Logging;
import org.locationtech.jts.algorithm.CGAlgorithms;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import org.locationtech.jts.operation.valid.IsValidOp;
import org.locationtech.jts.operation.valid.TopologyValidationError;
import org.locationtech.jts.simplify.TopologyPreservingSimplifier;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.data.file.ShapeFile;
import com.data.exception.FLException;
import com.data.file.FileLayer;
import com.data.file.MetaInfo;
import com.data.util.MapLog;
import com.gis.map.MData;
import com.gis.map.MapData;
import com.gis.map.TileMapFactory2;
import com.gis.protocol.DeleteOverlapMode;
import com.gis.protocol.FillStyle;
import com.gis.protocol.JobTile;
import com.gis.protocol.LevelConfig;
import com.gis.protocol.LevelConfigs;
import com.gis.protocol.LevelSet;
import com.gis.protocol.LineStyle;
import com.gis.protocol.MapInfo;
import com.gis.protocol.Mbr;
import com.gis.protocol.MultiLine;
import com.gis.protocol.OverlapConfig;
import com.gis.protocol.PointStyle;
import com.gis.protocol.PolygonStyle;
import com.gis.protocol.PolylineStyle;
import com.gis.protocol.ScaleInfo;
import com.gis.protocol.ScaleInfos;
import com.gis.protocol.Styles;
import com.gis.protocol.TextStyle;

import com.gis2.map.RasterLayer;
import com.gis2.map.VectorLayer;
import com.gis2.storage.ServiceConfig;
import com.gis2.storage.StorageMng;
import com.gis2.storage.TileServiceMng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.index.rtree.IRect;
import com.index.rtree.RTree;
import com.mapplan.Excel;
import com.mapplan.Field;
import com.mapplan.FieldType;
import com.mapplan.LayerInfo;
import com.util.io.FileUt;

public class Oper {

	String srcPath = "e:\\coops\\2020331";
	String dstPath = "e:\\coops\\service\\2020331";

	String emapPath = "";

	String rasterPath = "";

	GeometryFactory gft = new GeometryFactory();

	Point dogdo1 = null;
	Point dogdo2 = null;

	// String tiles = "tiles";
	// String layers = "layers";

	// String tiles = "d:\\coops\\service\\2020331\\tiles";

	String excelpath = "";

	// pointStyle name = layerName+"_"+shape+"_"+levelId
	// polygonStyle name = layerName+"_"+shape
	// polylineStyle name = layerName+"_"+shape

	boolean isInputSimple = false;

	String layers = "layers";

	XSSFWorkbook workbook = null;

	ScaleInfos sis = new ScaleInfos();;

	Styles styles = new Styles();

	com.gis.protocol.freegis3.Styles nstyles = null;

	MapInfo mainMapInfo = new MapInfo();

	HashMap<String, PolygonStyle> subQuerys = new HashMap();

	HashMap<String, PolylineStyle> subPolylineQuerys = new HashMap();

	HashMap<String, PointStyle> subPointQuerys = new HashMap();

	public static Logger LOG = null;
	HashMap<String, MapInfo> mapInfos = new HashMap();

	boolean isTileShutdown = false;

	TileMapFactory2 tmf = null;

	int stLevel = -1;
	int edLevel = -1;

	Mbr[] selectMbr = null;

	boolean tileMbrUpdate = true;

	HashMap<String, LayerInfo> layerInfos = new HashMap();

	boolean newMode = false;

	// true 이면 layers / layers_ 에 이미 저장된 FileLayer 는 다시 저장하지 않고 건너뛴다.
	boolean saveLayerSkipExist = false;

	/**
	 *
	 * polygon
	 * 
	 * layerName + seper + shape + seper + 엑셀순번
	 * 
	 * 코드가 존재 할 경우 하위 쿼리 이름 layerName + seper + shape + seper + codeV + seper + 엑셀순번
	 * 
	 * 스타일쉬트가 존재할 경우 styleSheetName + seper + shape + seper +
	 * codeFieldName.toLowerCase() + seper + 엑셀순번
	 * 
	 * polyline
	 * 
	 * layerName + seper + shape + seper + 엑셀순번
	 * 
	 * 코드가 존재 할 경우 하위 쿼리 이름 layerName + seper + shape + seper + codeV + seper + 엑셀순번
	 * 
	 * 스타일쉬트가 존재할 경우 styleSheetName + seper + shape + seper +
	 * codeFieldName.toLowerCase() + seper + 엑셀순번
	 * 
	 * point
	 * 
	 * layerName + seper + shape + seper + levelId + seper + 엑셀순번
	 * 
	 * 코드가 존재 할 경우 최상위 쿼리 이름 layerName + seper + shape + seper + 엑셀순번
	 * 코드가 존재 할 경우 하위 쿼리 이름 layerName + seper + shape + seper + codeV + seper + 엑셀순번
	 * 스타일쉬트가 존재할 경우 styleSheetName + seper + shape + seper +
	 * codeFieldName.toLowerCase() + seper + 엑셀순번
	 * 
	 */

	public Oper() {

		this.dogdo1 = this.gft.createPoint(new Coordinate(1387215, 1924766));
		this.dogdo2 = this.gft.createPoint(new Coordinate(1387638, 1924568));

		if (this.LOG == null) {
			// if (MapLog.LOG_PATH.length() == 0) {
			// MapLog.LOG_PATH = System.getProperty("user.dir") + "/";
			// MapLog.consolelog = true;
			// System.out.println("log path = " + MapLog.LOG_PATH);
			// }

			this.LOG = MapLog.getFileLog();

		}
	}

	public void setEmap(String path) {
		this.emapPath = path;
		TileServiceMng.readEmap(this.emapPath);
	}

	public void makeGridLayer(boolean makeGridLayer) {

		List<PointStyle> pss = this.styles.getPointStyle();

		LevelConfigs lcs = this.mainMapInfo.getLevelConfigs();

		ScaleInfos scaleInfos = this.mainMapInfo.getScaleInfos();

		HashMap<String, String> gridLayers = new HashMap();

		for (LevelConfig lc : lcs.getLevelConfig()) {
			for (com.gis.protocol.LayerInfo li : lc.getLayerInfo()) {
				String styleName = li.getSelectStyleName();

				double ppm = 0.0;

				for (ScaleInfo scaleInfo : scaleInfos.getScaleInfo()) {
					if (scaleInfo.getId() == lc.getLevelId()) {
						ppm = scaleInfo.getPixelPerMeter();
					}
				}

				for (PointStyle ps : pss) {
					if (ps.getName().equals(styleName) && ps.getGrid() > 0) {

						String src = this.dstPath + FileUt.SEPERATOR + ServiceConfig.layers + FileUt.SEPERATOR
								+ li.getLayerName();

						File dstPath = new File(this.dstPath + FileUt.SEPERATOR + ServiceConfig.grid);
						if (!dstPath.exists()) {
							dstPath.mkdirs();
						}

						String dst = this.dstPath + FileUt.SEPERATOR + ServiceConfig.grid + FileUt.SEPERATOR
								+ li.getLayerName() + "_" + lc.getLevelId();

						li.setLayerName("grid:" + li.getLayerName() + "_" + lc.getLevelId());

						if (!gridLayers.containsKey(dst)) {
							int grid = (int) (ps.getGrid() * ppm);

							try {
								// System.out.println(li.getLayerName()+"," +
								// styleName+","+lc.getLevelId()+","+ps.getGrid()+","+ps.getGrid()*ppm +" m");
								if (makeGridLayer) {
									this.grid(src, dst, grid);
								}
								gridLayers.put(dst, dst);
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

	// public void saveLayer(String src, String dst, int gridDistance, int level)
	// throws Exception{
	//
	// FileLayer fl = new FileLayer(src, null);
	//
	// FileLayer savefl = new FileLayer(dst, fl.getLayerInfo());
	//
	// Iterator it = fl.iterator();
	//
	// while(it.hasNext()) {
	// SimpleFeature sf = (SimpleFeature) it.next();
	// Geometry geo = (Geometry)sf.getDefaultGeometry();
	// Envelope env = geo.getEnvelopeInternal();
	//
	// if(geo.getEnvelopeInternal().getHeight() > gridDistance
	// || geo.getEnvelopeInternal().getWidth() > gridDistance) {
	// List<Geometry> splits = this.split(geo);
	//
	// for(Geometry sub : splits) {
	//
	// List<Geometry> subSplits = this.split(sub);
	//
	// for(Geometry subsub : subSplits) {
	// if(subsub.getEnvelopeInternal().getHeight() > gridDistance ||
	// subsub.getEnvelopeInternal().getWidth() > gridDistance) {
	// List<Geometry> subsubSplits = this.split(subsub);
	// }
	// }
	// }
	// }
	// }
	// }

	public void grid(String src, String dst, int _width) throws Exception {

		FileLayer orifl = null;
		FileLayer dstfl = null;
		try {
			long st = System.currentTimeMillis();
			orifl = new FileLayer(src, null);
			dstfl = new FileLayer(dst, orifl.getLayerInfo());
			com.data.file.FileLayer.SimpleFeatureIterator it = orifl.iterator();
			while (it.hasNext()) {

				double oriX = 0;
				double oriY = 0;

				SimpleFeature sf = it.next();

				Geometry org = (Geometry) sf.getDefaultGeometry();

				Envelope en = org.getEnvelopeInternal();

				if (en.getWidth() > _width * 2 || en.getHeight() > _width * 2) {

					double xinter = _width;
					double yinter = _width;

					int startXN = 0;
					int startYN = 0;
					int endXN = 0;
					int endYN = 0;

					oriX = (int) en.getMinX();
					oriY = (int) en.getMinY();

					startXN = (int) (((en.getMinX() - oriX) / xinter));
					startYN = (int) ((en.getMinY() - oriY) / xinter);
					endXN = (int) ((en.getMaxX() - oriX) / xinter);
					endYN = (int) ((en.getMaxY() - oriY) / xinter);

					double tempx = oriX + startXN * xinter - xinter;
					double tempy = oriY + startYN * yinter - yinter;

					int xCnt = (int) ((endXN - startXN)) + 2;
					int yCnt = (int) ((endYN - startYN)) + 2;

					int id = Integer.MAX_VALUE;

					int max = Math.max(xCnt, yCnt);

					boolean mode = true;

					int t = 0;
					while (mode) {
						double pre = Math.pow(2, t);
						double lat = Math.pow(2, t + 1);
						if (max >= pre && max <= lat) {
							max = (int) lat;
							mode = false;
						}
						t++;
					}
					xCnt = max;
					yCnt = max;

					Envelope mbr = new Envelope(tempx, tempx + xCnt * xinter, tempy, tempy + yCnt * yinter);

					this.getGrid(mbr, org.getGeometryType(), org, sf, (int) xinter, dstfl);
				} else {
					dstfl.writeFeature(sf);
				}
			}
			long et = System.currentTimeMillis();

			LOG.debug(dstfl.getName() + " end, time = " + (et - st) / 1000 / 60 + " min");
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (orifl != null) {
				orifl.close();
			}
			if (dstfl != null) {
				dstfl.close();
			}
		}

	}

	public void getGrid(Envelope mbr, String _geoType, Geometry _geo, SimpleFeature feature, int gridSize, FileLayer fl)
			throws Exception {

		Geometry geo = _geo;
		WKTReader rdr = new WKTReader();
		int xinter = (int) mbr.getWidth() / 2;
		int yinter = (int) mbr.getHeight() / 2;

		if (xinter != yinter) {
			System.out.println("error");
		}

		int tempx = (int) mbr.getMinX();
		int tempy = (int) mbr.getMinY();
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				long startTime = System.currentTimeMillis();
				Envelope grid = new Envelope((tempx + xinter * j), (tempx + xinter * (j + 1)), (tempy + yinter * i),
						(tempy + yinter * (i + 1)));

				if (grid.getWidth() != grid.getHeight()) {
					System.out.print("error");
				}
				Geometry sec = rdr.read("POLYGON ((" + (int) (tempx + xinter * j) + " " + (int) (tempy + yinter * i)
						+ " , " + " " + (int) (tempx + xinter * j) + " " + (int) (tempy + yinter * (i + 1)) + " ," + " "
						+ (int) (tempx + xinter * (j + 1)) + " " + (int) (tempy + yinter * (i + 1)) + " ," + " "
						+ (int) (tempx + xinter * (j + 1)) + " " + (int) (tempy + yinter * i) + " ," + " "
						+ (int) (tempx + xinter * j) + " " + (int) (tempy + yinter * i) + "))");
				long stime = System.currentTimeMillis();
				Coordinate[] secC = sec.getCoordinates();
				if (geo.getNumPoints() < 2) {
					continue;
				}
				Geometry unionG = null;
				try {
					unionG = geo.intersection(sec);
				} catch (Exception e) {
					geo = geo.buffer(0.0);
					try {
						unionG = geo.intersection(sec);
					} catch (Exception e1) {
						e1.printStackTrace();
						continue;
					}
				}
				long etime = System.currentTimeMillis();
				// System.out.println("grid.getWidth() = " + grid.getWidth());
				if (grid.getWidth() == gridSize) {

					if (unionG != null && unionG.getNumPoints() != 0) {
						if (unionG instanceof Point) {
							continue;
						}
						String geoType = _geoType;// Shapefile.getShapeType(org,
													// 2);
						if (geoType.equals("Polygon") || geoType.equals("MultiPolygon")) {
							if ((unionG instanceof Point)) {
								continue;
							} else if ((unionG instanceof LineString)) {
								continue;
							} else if ((unionG instanceof MultiLineString)) {
								continue;
							} else if ((unionG instanceof GeometryCollection)) {
								for (int u = 0; u < unionG.getNumGeometries(); u++) {
									Geometry obj = unionG.getGeometryN(u);
									if (obj instanceof Polygon || obj instanceof MultiPolygon
											|| obj instanceof LinearRing) {
										// geoConV.add(obj);
										// feature.setGeometry(obj);
										feature.setDefaultGeometry(obj);
										// Object[] objs =
										// feature.getAttributes();
										// objs[objs.length-1] =
										// ((int)grid.getMinX()/gridSize)+"_"+((int)grid.getMinY()/gridSize);
										// this.umdWriter.add(feature);
										fl.writeFeature(feature);
										// dbfW.addRecord(objs);
										// continue;
									}
								}
								continue;
							} else {
								// System.out.println("type="+unionG.getGeometryType());
							}
						} else {
							// System.out.println("geo = " +
							// unionG.getGeometryType());
						}
						// geoConV.add(unionG);
						// feature.setGeometry(unionG);
						feature.setDefaultGeometry(unionG);
						// Object[] objs = feature.getAttributes();
						// objs[objs.length-1] =
						// ((int)grid.getMinX()/gridSize)+"_"+((int)grid.getMinY()/gridSize);
						// this.umdWriter.add(feature);
						fl.writeFeature(feature);
						// dbfW.addRecord(objs);
						continue;
					}
					continue;
				}
				if (unionG instanceof Point) {
					continue;
				}
				int unionCnt = unionG.getNumGeometries();
				for (int h = 0; h < unionCnt; h++) {
					Geometry unionGeo = unionG.getGeometryN(h);
					if (unionG instanceof Point) {
						continue;
					}
					getGrid(grid, _geoType, unionGeo, feature, gridSize, fl);
				}
				// grids.addElement(grid);
			}
		}
		geo = null;
		// return grids;
	}

	public List<Geometry> split(Geometry p, Vector<Geometry> result) {
		List<Geometry> ret = new ArrayList<>();

		final Envelope envelope = p.getEnvelopeInternal();
		double minX = envelope.getMinX();
		double maxX = envelope.getMaxX();
		double midX = minX + (maxX - minX) / 2.0;
		double minY = envelope.getMinY();
		double maxY = envelope.getMaxY();
		double midY = minY + (maxY - minY) / 2.0;

		Envelope llEnv = new Envelope(minX, midX, minY, midY);
		Envelope lrEnv = new Envelope(midX, maxX, minY, midY);
		Envelope ulEnv = new Envelope(minX, midX, midY, maxY);
		Envelope urEnv = new Envelope(midX, maxX, midY, maxY);

		Geometry ll = JTS.toGeometry(llEnv).intersection(p);
		Geometry lr = JTS.toGeometry(lrEnv).intersection(p);
		Geometry ul = JTS.toGeometry(ulEnv).intersection(p);
		Geometry ur = JTS.toGeometry(urEnv).intersection(p);

		ret.add(ll);
		ret.add(lr);
		ret.add(ul);
		ret.add(ur);

		return ret;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		long st = System.currentTimeMillis();
		Oper op = new Oper();
		Properties property = new Properties();
		File path = new File(args[0]);

		System.out.println("Properties read start fileName = " + path.getAbsolutePath());

		if (path.exists()) {
			FileInputStream fiss;
			try {
				fiss = new FileInputStream(path);
				property.load(fiss);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		Runtime rt = Runtime.getRuntime();

		rt.addShutdownHook(new Thread() {
			public void run() {
				op.isTileShutdown = true;

				if (op.tmf != null) {
					op.tmf.stopTileMake();
				}

				// LOG.debug("프로그램 강제 종료 됨");
			}
		});

		try {

			/*
			 * 설정 파일로
			 * 
			 * -Djava.util.logging.config.file=D:/workspace/MapService/map_service/logging.
			 * properties
			 * 
			 * 
			 * # 기본 핸들러 설정
			 * #handlers = java.util.logging.ConsoleHandler, java.util.logging.FileHandler
			 * handlers = java.util.logging.FileHandler
			 * 
			 * # 콘솔 핸들러 설정
			 * java.util.logging.ConsoleHandler.level = INFO
			 * java.util.logging.ConsoleHandler.formatter =
			 * java.util.logging.SimpleFormatter
			 * 
			 * # 파일 핸들러 설정
			 * java.util.logging.FileHandler.level = INFO
			 * java.util.logging.FileHandler.pattern = logs/geotools.log
			 * java.util.logging.FileHandler.limit = 5000000 # 로그 파일 최대 크기 (5MB)
			 * java.util.logging.FileHandler.count = 3 # 순환 로그 파일 갯수
			 * java.util.logging.FileHandler.append = true # 기존 파일에 추가
			 * java.util.logging.FileHandler.formatter = java.util.logging.SimpleFormatter
			 * 
			 * # GeoTools 로그 설정
			 * org.geotools.level = INFO
			 */

			/*
			 * 코드 상에서 설정
			 * java.util.logging.Logger logger =
			 * java.util.logging.Logger.getLogger("org.geotools");
			 * 
			 * try {
			 * // 파일 핸들러 추가
			 * FileHandler fileHandler = new FileHandler("logs/geotools.log", true);
			 * fileHandler.setLevel(Level.INFO);
			 * fileHandler.setFormatter(new SimpleFormatter());
			 * logger.addHandler(fileHandler);
			 * 
			 * // 기본 로그 출력
			 * logger.info("GeoTools 로그가 파일에 기록됩니다.");
			 * 
			 * } catch (IOException e) {
			 * e.printStackTrace();
			 * }
			 */

			java.util.logging.Logger lg = Logging.getLogger(ImageWorker.class);
			lg.setLevel(Level.OFF);

			String excelpath = property.getProperty("excelPath");

			excelpath = new String(excelpath.getBytes("ISO-8859-1"), "UTF-8");

			LOG.debug("excelpath=" + excelpath);
			String srcPath = property.getProperty("shapePath");
			srcPath = new String(srcPath.getBytes("ISO-8859-1"), "UTF-8");

			LOG.debug("shapePath=" + srcPath);
			String dstPath = property.getProperty("dstPath");
			dstPath = new String(dstPath.getBytes("ISO-8859-1"), "UTF-8");
			LOG.debug("dstPath=" + dstPath);
			String makeLayer = property.getProperty("makeLayer");
			LOG.debug("makeLayer=" + makeLayer);
			String makeServiceInfo = property.getProperty("makeServiceInfo");
			LOG.debug("makeServiceInfo=" + makeServiceInfo);

			String makeGridLayer = property.getProperty("makeGridLayer");
			LOG.debug("makeGridLayer=" + makeGridLayer);

			String makeTile = property.getProperty("makeTile");
			LOG.debug("makeTile=" + makeTile);

			String hdMode = property.getProperty("hdMode", "");
			LOG.debug("hdMode=" + hdMode);

			if (hdMode != null && hdMode.trim().equals("true")) {
				TileServiceMng.hdMode = true;
			}

			String colorCnt = property.getProperty("colorCnt", "");
			LOG.debug("colorCnt=" + colorCnt);

			if (colorCnt != null && colorCnt.trim().length() > 0) {
				TileServiceMng.colorCnt = Integer.parseInt(colorCnt);
			}

			String tileThread = property.getProperty("tileThread");

			String tileNames = property.getProperty("tileNames");

			String tileUpdate = property.getProperty("tileUpdate");

			String isSimple = property.getProperty("isSimple");

			String upTile = property.getProperty("tileMbrUpdate");

			String emapPath = property.getProperty("emapPath");

			if (emapPath != null) {
				op.setEmap(emapPath);
			}

			String rasterPath = property.getProperty("rasterPath");

			if (rasterPath != null) {
				op.rasterPath = rasterPath;
			}

			if (upTile != null && upTile.equals("true")) {
				op.tileMbrUpdate = true;
			} else {
				op.tileMbrUpdate = false;
			}

			if (op.tileMbrUpdate) {

				String mbrs = property.getProperty("mbr");
				if (mbrs != null) {
					String[] mbrList = mbrs.split("\\|");

					op.selectMbr = new Mbr[mbrList.length];

					for (int i = 0; i < mbrList.length; i++) {

						Mbr smbr = new Mbr();

						String[] list = mbrList[i].split(",");

						smbr.setMinx(Double.parseDouble(list[0]));
						smbr.setMiny(Double.parseDouble(list[1]));
						smbr.setMaxx(Double.parseDouble(list[2]));
						smbr.setMaxy(Double.parseDouble(list[3]));

						op.selectMbr[i] = smbr;
					}
				}

				String sl = property.getProperty("startLevel");

				if (sl != null) {
					op.stLevel = Integer.parseInt(sl);
				}

				String el = property.getProperty("endLevel");

				if (el != null) {
					op.edLevel = Integer.parseInt(el);
				}
			}

			if (isSimple != null && isSimple.equals("true")) {
				op.isInputSimple = true;
			} else if (isSimple != null) {
				op.isInputSimple = false;
			} else {
				op.isInputSimple = true;
			}

			op.excelpath = FilenameUtils.separatorsToUnix(excelpath);
			op.srcPath = FilenameUtils.separatorsToUnix(srcPath);
			op.dstPath = FilenameUtils.separatorsToUnix(dstPath);

			if (op.excelpath.endsWith(".xlsm")) {
				op.newMode = true;
			}

			// 이미 저장된 FileLayer(layers/layers_)는 다시 저장하지 않는 옵션 (기본 false = 덮어씀)
			String layerSaveSkipExist = property.getProperty("layerSaveSkipExist", "");
			LOG.debug("layerSaveSkipExist=" + layerSaveSkipExist);
			if (layerSaveSkipExist != null && layerSaveSkipExist.trim().equals("true")) {
				op.saveLayerSkipExist = true;
			}

			String fontPath = property.getProperty("fontPath");
			if (fontPath != null && fontPath.length() > 0) {
				StorageMng.fontPath = fontPath;
			}

			
			if(!op.newMode) {
				op.readExcel();
			}
			
			if (makeServiceInfo.equals("true")) {
				if (op.newMode) {
					new GisStyleExcelTool.Importer(op.excelpath).run(op.dstPath);
					//op.readEmapServiceInfo(false);
					//op.saveEmapServiceInfo(false);
				}
			}
			
			if (makeLayer.equals("true")) {

				if (op.newMode) {
					
					op.readEmapServiceInfo(false);
					
					op.saveEmapServiceInfo(true);
					
				} else {
					op.makeLayers();
				}
			}
			
			if (makeServiceInfo.equals("true")) {
				if (!op.newMode) {
					boolean gridLayer = false;
					if (makeGridLayer != null && makeGridLayer.equals("true")) {
						gridLayer = true;
					}
					op.createServiceInfo(gridLayer);

				}
			}


			if (makeTile.equals("true")) {
				if (op.newMode) {
					op.readEmapServiceInfo(true);
				}
				boolean updateMode = false;
				if (tileUpdate != null && tileUpdate.equals("true")) {
					updateMode = true;
				}
				op.makeTiles(tileNames, Integer.parseInt(tileThread), updateMode);
			}

			op.closeExcel();

		} catch (Exception e) {
			e.printStackTrace();
			LOG.debug(e.toString(), e);
		}
		long et = System.currentTimeMillis();
		
		System.out.println("job time = " + (et-st)/1000);
		System.exit(0);
	}

	public void readEmapServiceInfo(boolean process) {
		File file = new File(dstPath);

		File[] subFile = file.listFiles();

		String stylePath = dstPath + File.separator + "tiles" + File.separator + "emp_style.xml";

		String layersPath = dstPath + File.separator + "layers";

		try {
			this.nstyles = ServiceConfig.readNewStyles(stylePath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Vector<MapInfo> mapInfos = new Vector();
		for (File style : subFile) {
			if (style.isDirectory()) {
				File[] tiles = style.listFiles();
				for (File tile : tiles) {
					String tilePath = tile.getAbsolutePath();
					File mapInfoF = new File(tilePath + FileUt.SEPERATOR + "mapInfo" + (process ? "_" : "") + ".xml");
					if (mapInfoF.exists()) {
						try {
							MapInfo mapInfo = ServiceConfig.readMapInfo(mapInfoF.getAbsolutePath());
							// mapInfos.add(mapInfo);
							this.mapInfos.put(mapInfo.getName(), mapInfo);

							System.out.println(
									"mapInfo name= " + mapInfo.getName() + ", path=" + mapInfoF.getAbsolutePath());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	public void saveEmapServiceInfo(boolean save) {

		boolean layerSave = true;
		boolean processSave = true;
		
		
		if(!save) {
			layerSave = false;
			processSave = false;
		}

		String layersPath = dstPath + File.separator + "layers";

		Vector<JobInfo> jobInfos = new Vector();

		HashMap<String, String> layerNames = new HashMap<>();

		Set set = this.mapInfos.keySet();
		Iterator it = set.iterator();
		while (it.hasNext()) {
			String name = (String) it.next();

			MapInfo mapInfo = this.mapInfos.get(name);
			List<LevelConfig> levelConfigs = mapInfo.getLevelConfigs().getLevelConfig();
			for (LevelConfig levelConfig : levelConfigs) {
				List<com.gis.protocol.LayerInfo> layerInfos = levelConfig.getLayerInfo();
				for (com.gis.protocol.LayerInfo layerInfo : layerInfos) {
					String layerName = layerInfo.getLayerName();
					
					
					String[] arry = layerName.split(":");
					
					layerNames.put(arry[arry.length-1], arry[arry.length-1]);
				}
			}
		}

		for (String layerName : layerNames.values()) {
			// 이름 기반으로 하위 디렉토리에서 shape file 검색
			File shpFile = findShapeFile(new File(this.srcPath), layerName);
			if (shpFile == null) {
				System.out.println("Shape file '" + layerName + "' not found in: " + this.srcPath);
			}
		}

		if (layerSave) {

			ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
			Vector<Future> fts = new Vector();

			for (String layerName : layerNames.values()) {

				// if(!layerName.endsWith("tn_ex_ssubwsta_l")) {
				// continue;
				// }

				Future ft = executorService.submit(() -> {
					this.saveNamedShapeToFileLayer(layerName, this.srcPath, layersPath);
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

		// MapInfo mapInfo = this.mapInfos.get(0);

		for (MapInfo mapInfo : mapInfos.values()) {

			ScaleInfos sis = mapInfo.getScaleInfos();

			LevelConfigs lcs = mapInfo.getLevelConfigs();

			List<LevelConfig> lcl = lcs.getLevelConfig();

			Collections.sort(lcl, new LevelComparator());

			String layerName = null;

			Hashtable<String, String> jobIng = new Hashtable();
			Vector<String> saveLayerNames = new Vector();

			for (LevelConfig lc : lcl) {

				List<com.gis.protocol.LayerInfo> lis = lc.getLayerInfo();
				int levelId = lc.getLevelId();

				for (com.gis.protocol.LayerInfo li : lis) {
					long beforeSimpleFeatureStartTime = System.currentTimeMillis();

					FieldType flGeoType = null;
					Object style = null;

					// String layerName = li.getLayerName().toLowerCase();
					layerName = li.getLayerName();

					if (layerName.startsWith("tileMapService")) {
						// this.totalCnt = this.totalCnt - 1;
						continue;
					}

					String fileExtension = "";

					boolean isVector = false;
					boolean isRaster = false;

					// 데이터 종류 분기
					if (layerName.startsWith("raster")) {
						isRaster = true;
					} else if (layerName.startsWith("vector")) {
						isVector = true;
					} else {
						isVector = true;
					}

					String[] temp = layerName.split(":");
					String name = "";
					if (temp.length == 1) {
						name = layerName.toLowerCase();
					} else {
						name = temp[temp.length - 1];
					}

					layerName = name.toLowerCase();

					double simpleValue = this.getErrorValue(sis, levelId);
					// double gridValue = imgWidth * simpleValue * 16;
					double gridValue = 400 * simpleValue * 16;

					String[] nameTemp = layerName.split(":");

					// File saveLayerFile = new File(layersPath + FileUt.SEPERATOR +
					// nameTemp[nameTemp.length - 1].toLowerCase());
					File saveLayerFile = new File(
							layersPath + "_" + FileUt.SEPERATOR + nameTemp[nameTemp.length - 1].toLowerCase());
					String mapInfoName = "";
					Boolean lineText = false;

					if (isVector) {

						File layerFile = new File(layersPath + File.separator + layerName);

						if (!layerFile.exists()) {
							continue;
						}

						LayerInfo layerInfo = null;
						try {
							layerInfo = MetaInfo
									.readLayerInfo(layerFile.getAbsolutePath() + FileUt.SEPERATOR + "layerInfo.xml");
						} catch (FLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						if (layerInfo == null) {
							continue;
						}

						boolean geoTypeSet = false;

						List<Field> fds = layerInfo.getSchema();
						for (Field fd : fds) {
							if ((fd.getName().toLowerCase().equals("geometry")
									|| fd.getName().toLowerCase().equals("shape"))
									&& (fd.getType() == null || fd.getType().equals(FieldType.GEOMETRY))) {
								geoTypeSet = true;
							} else if (fd.getName().toLowerCase().equals("geometry")
									|| fd.getName().toLowerCase().equals("shape")) {
								flGeoType = fd.getType();
							}
						}

						style = this.getStyle(li.getSelectStyleName(), nstyles);

						com.mapplan.Mbr maxMbr = layerInfo.getMaxObjMbr();

						if (maxMbr != null) {
							double maxWidth = maxMbr.getMaxx() - maxMbr.getMinx();
							double maxHeight = maxMbr.getMaxy() - maxMbr.getMiny();

							if (gridValue * 2 > maxWidth && gridValue * 2 > maxHeight) {
								gridValue = 0;
							}
							if ((flGeoType == FieldType.LINE_STRING || flGeoType == FieldType.MULTI_LINE_STRING)
									&& style instanceof PointStyle) {
								gridValue = 0;
								lineText = true;
							}
						} else {
							gridValue = 0;
						}

						String[] layerPaths = layerName.split(":");

						li.setLayerName((layerPaths.length == 1 ? layerPaths[0] : layerPaths[1]));

						if (gridValue > 0) {

							String gridLayer = saveLayerFile.getAbsolutePath();
							saveLayerFile = new File(gridLayer + "_" + gridValue);
							li.setLayerName(li.getLayerName() + "_" + gridValue);

						}

						// 참조로 인한 경량화 제외
						if (jobIng.containsKey(saveLayerFile.getName())) {
							continue;
						} else {
							jobIng.put(saveLayerFile.getName(), saveLayerFile.getName());
						}

						// raster 데이터 경량화
					} else if (isRaster) {
						/*
						 * layerName = layerName.substring(layerName.indexOf(":") + 1,
						 * layerName.length());
						 * mapInfoName = layerName.substring(0, layerName.indexOf(":"));
						 * 
						 * RasterLayer layer = (RasterLayer) tmf.getMapData().getLayer(layerName);
						 * 
						 * fileExtension = layer.getFileExtension();
						 * 
						 * if (!fileExtension.equals("tif") && !fileExtension.equals("tiff")
						 * && !fileExtension.equals("geotiff")) {
						 * this.log.info(fileExtension + " is not tif, tiff or geotiff");
						 * }
						 * 
						 * String rasterPath = saveLayerFile.getParent();
						 * String rasterName = saveLayerFile.getName();
						 * File levelIdFile = new File(rasterPath + FileUt.SEPERATOR + levelId);
						 * 
						 * saveLayerFile = new File(levelIdFile.getAbsolutePath() + FileUt.SEPERATOR +
						 * rasterName);
						 * 
						 * if (layer == null || layer.getGridCoverageLayer() == null) {
						 * // this.totalCnt = this.totalCnt - 1;
						 * if (update && saveLayerFile.exists()) {
						 * FileUtils.forceDelete(saveLayerFile);
						 * deleteLayers.add(tmf.mapInfoName + ":process:" + tms + ":"
						 * + nameTemp[nameTemp.length - 1].toLowerCase());
						 * }
						 * continue;
						 * }
						 * 
						 * GridCoverageLayer gc = layer.getGridCoverageLayer();
						 * 
						 * GridCoverage2D gc2d = gc.getCoverage();
						 * 
						 * AffineTransform2D at = (AffineTransform2D)
						 * gc2d.getGridGeometry().getGridToCRS();
						 * 
						 * double ws = at.getScaleX();
						 * double hs = at.getScaleY();
						 * // gc2d.get
						 * 
						 * // gc2d.get
						 * 
						 * // process명 추가
						 * 
						 * String oriLayerName = li.getLayerName();
						 * String[] layerPath = li.getLayerName().split(":");
						 * li.setLayerName("raster:" + mapName + ":process:" + tms + ":" + levelId + ":"
						 * + layerPath[2].toLowerCase());
						 * 
						 * gridValue = (simpleValue * 2);
						 * 
						 * gridValue = simpleValue;
						 * 
						 * if (ws > simpleValue) {
						 * gridValue = 1;
						 * }
						 * //
						 * //if (gridValue > 1) {
						 * // 2024 11 01 수정
						 * if (gridValue > 2) {
						 * if (!levelIdFile.exists()) {
						 * levelIdFile.mkdirs();
						 * }
						 * 
						 * String gridName = saveLayerFile.getAbsolutePath();
						 * saveLayerFile = new File(gridName + "_" + gridValue);
						 * li.setLayerName(li.getLayerName() + "_" + gridValue);
						 * } else {
						 * li.setLayerName(oriLayerName);
						 * // this.totalCnt = this.totalCnt - 1;
						 * continue;
						 * }
						 * 
						 * if (update) {
						 * 
						 * if (saveLayerFile.exists()) {
						 * // this.totalCnt = this.totalCnt - 1;
						 * 
						 * Monitor.append(saveLayerFile.getName() + " 파일이 이미 존재 함");
						 * continue;
						 * }
						 * }
						 */

					}

					String saveLayerName = li.getLayerName();

					saveLayerNames.add(saveLayerName);

					// 다른 주제도에서 참조하고 있으면 초기화를 해준다.

					JobInfo jobInfo = new JobInfo();

					jobInfo.saveLayerFile = new File(saveLayerFile.getAbsolutePath());
					jobInfo.mapInfoName = new String(mapInfoName);
					jobInfo.isRaster = isRaster;
					jobInfo.simpleValue = simpleValue;
					jobInfo.gridValue = gridValue;
					jobInfo.lineText = new Boolean(lineText);
					jobInfo.fileExtension = new String(fileExtension);
					jobInfo.layerName = new String(layerName);

					int idx = -1;
					for (int i = 0; i < jobInfos.size(); i++) {
						JobInfo info = jobInfos.get(i);

						if (info.saveLayerFile.getName().equals(jobInfo.saveLayerFile.getName())) {
							idx = i;
						}
					}

					if (idx > -1) {

						JobInfo remove = jobInfos.remove(idx);

						//System.out.println("remove jobInfo = " + remove.saveLayerFile);
					}

					jobInfos.add(jobInfo);

				}
			}

			String saveMapInfoFile = dstPath + File.separator + "tiles" + FileUt.SEPERATOR + mapInfo.getName()
					+ FileUt.SEPERATOR + "mapInfo_.xml";
			try {
				ServiceConfig.writeMapInfo(saveMapInfoFile, mapInfo);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		if (processSave) {

			ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
			Vector<Future> fts = new Vector();

			for (JobInfo jobInfo : jobInfos) {
				System.out.println("saveLayerFile" + jobInfo.saveLayerFile + ", simpleValue=" + jobInfo.simpleValue
						+ ", gridValue=" + jobInfo.gridValue);

				// 이미 저장된 FileLayer 이면 건너뛴다 (layerSaveSkipExist=true 일 때)
				if (this.saveLayerSkipExist
						&& new File(jobInfo.saveLayerFile.getAbsolutePath() + File.separator + "layerInfo.xml")
								.exists()) {
					System.out.println("이미 존재하여 저장 생략(layers_): " + jobInfo.saveLayerFile.getAbsolutePath());
					continue;
				}

				Future ft = executorService.submit(() -> {

					try {
						this.process(layersPath + File.separator + jobInfo.layerName,
								jobInfo.saveLayerFile.getAbsolutePath(), jobInfo.gridValue, jobInfo.simpleValue, null,
								jobInfo.lineText);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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

	}

	public void process(String src, String dst, double grid, double distance, String epsg, boolean lineText)
			throws Exception {

		FileLayer orifl = null;
		FileLayer dstfl = null;

		CoordinateReferenceSystem viewCRS = null;
		MathTransform toViewTransform = null;

		RTree rtree = new RTree(0.4f, 10);

		try {

			// UMDReader reader = StorageMng.getUMD1(src, false);

			orifl = StorageMng.getFileLayer(src, false);

			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setPrettyPrinting()
					.registerTypeAdapter(XMLGregorianCalendar.class, new MetaInfo.Deserializer())
					.registerTypeAdapter(XMLGregorianCalendar.class, new MetaInfo.Serializer()).create();

			String json = gson.toJson(orifl.getLayerInfo());

			LayerInfo copyLayerInfo = gson.fromJson(json, LayerInfo.class);

			if (epsg != null) {

				int fileEPSG = -1;
				try {
					fileEPSG = CRS.lookupEpsgCode(orifl.getSimpleFeatureType().getCoordinateReferenceSystem(), false);
				} catch (FactoryException e1) {
					// TODO Auto-generated catch block
					fileEPSG = 5179;
					System.out.println("error :: " + orifl.getName() + " =  src file validate epsg, "
							+ orifl.getSimpleFeatureType().getCoordinateReferenceSystem().toWKT());
					e1.printStackTrace();
				}
				String fileCode = "EPSG:" + fileEPSG;
				if (!fileCode.equals(epsg)) {
					try {
						String code = epsg.split(":")[1];
						viewCRS = CRS.decode(epsg);
						toViewTransform = CRS.findMathTransform(
								orifl.getSimpleFeatureType().getCoordinateReferenceSystem(), viewCRS);
						copyLayerInfo.setProjection(viewCRS.toWKT());
						// orifl.getLayerInfo().setProjection(viewCRS.toWKT());
					} catch (NoSuchAuthorityCodeException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (FactoryException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}

			// double oriX = _oriX;
			// double oriY = _oriY;
			double oriX = 0.0;
			double oriY = 0.0;

			if (grid > 0) {
				copyLayerInfo.setGridSize((int) grid);
			}

			dstfl = new FileLayer(dst, copyLayerInfo);

			com.data.file.FileLayer.SimpleFeatureIterator it = orifl.iterator();
			long iteratorTimeTest = System.currentTimeMillis();
			while (it.hasNext()) {

				// SimpleFeature sf = it.next();
				SimpleFeature sf = SimpleFeatureBuilder.copy(it.next()); // @@ 수정된 코드

				Geometry org = (Geometry) sf.getDefaultGeometry();

				if (org == null) {
					MapLog.getSCLog().debug("src = " + src + ", geo is null," + sf.toString());
					continue;
				}

				if (toViewTransform != null) {
					try {
						org = JTS.transform(org, toViewTransform);
					} catch (TransformException e) {
						e.printStackTrace();
					}
				}

				if (!(org instanceof Point) && !(org instanceof MultiPoint)) {

					if (distance > 0) {
						Geometry deleteGeo = this.getDeleteGeometryM(org, distance);
						// Geometry deleteGeo = org;
						if (deleteGeo == null) {

							Envelope env = org.getEnvelopeInternal();
							IRect rec = new IRect();
							rec.x1 = (int) (env.getMinX() / distance);
							rec.x2 = (int) (env.getMaxX() / distance);
							rec.y1 = (int) (env.getMinY() / distance);
							rec.y2 = (int) (env.getMaxY() / distance);

							Vector data = rtree.searchRect(rec, RTree.SEARCH_INTERSECTION);

							if (data.size() > 0) {
								continue;
							} else {
								rtree.insertEntity(rec, 1);
								if (org instanceof Polygon || org instanceof MultiPolygon) {
									deleteGeo = this.getFakePolygon(org);
								}
							}

						}
						if (deleteGeo == null || deleteGeo.getNumPoints() < 1 || deleteGeo.isEmpty()) {
							continue;
						}
						Geometry simpleGeo = null;
						try {
							simpleGeo = TopologyPreservingSimplifier.simplify(deleteGeo, distance);
						} catch (Exception e) {
							MapLog.getSCLog().debug("", e);
							deleteGeo = deleteGeo.buffer(0.0);
							simpleGeo = TopologyPreservingSimplifier.simplify(deleteGeo, distance);
						}

						if (simpleGeo == null) {
							System.out.println("simpleGeo is null");
						}

						org = simpleGeo;
					}
				}

				Envelope en = org.getEnvelopeInternal();

				if ((en.getWidth() > grid * 2 || en.getHeight() > grid * 2) && grid > 0) {

					double xinter = grid;
					double yinter = grid;

					int startXN = 0;
					int startYN = 0;
					int endXN = 0;
					int endYN = 0;

					startXN = (int) (((en.getMinX() - oriX) / xinter));

					startYN = (int) ((en.getMinY() - oriY) / xinter);

					endXN = (int) ((en.getMaxX() - oriX) / xinter);

					endYN = (int) ((en.getMaxY() - oriY) / xinter);

					double tempx = oriX + startXN * xinter - xinter;
					double tempy = oriY + startYN * yinter - yinter;

					int xCnt = (int) ((endXN - startXN)) + 2;
					int yCnt = (int) ((endYN - startYN)) + 2;

					int id = Integer.MAX_VALUE;

					int max = Math.max(xCnt, yCnt);

					boolean mode = true;

					int t = 0;
					while (mode) {
						double pre = Math.pow(2, t);
						double lat = Math.pow(2, t + 1);
						if (max >= pre && max <= lat) {
							max = (int) lat;
							mode = false;
						}
						t++;
					}
					xCnt = max;
					yCnt = max;

					Envelope mbr = new Envelope(tempx, tempx + xCnt * xinter, tempy, tempy + yCnt * yinter);

					this.getGrid(mbr, org.getGeometryType(), org, sf, (int) xinter, dstfl);
				} else {
					sf.setDefaultGeometry(org);
					long pkid = dstfl.writeFeature(sf);
				}
			}
			// log.debug("####### iterator: " +
			// (System.currentTimeMillis()-iteratorTimeTest));

		} catch (Exception e) {
			MapLog.getSCLog().error("", e);
			File file = dstfl.getPath();
			try {

				if (dstfl != null) {
					dstfl.close();
				}

				dstfl = null;
				FileUtils.forceDelete(file);
				MapLog.getSCLog().error("delete layer name = " + file.getAbsolutePath());
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			throw e;
		} finally {
			// if (orifl != null) {
			// orifl.close();
			// }
			if (dstfl != null) {
				dstfl.close();
			}
		}

	}

	public Geometry getFakePolygon(Geometry geo_) {
		Geometry geo = geo_;
		Coordinate[] coords = new Coordinate[4];
		// 동일 포인트로 구성된 폴리곤은 에러 발생

		Coordinate[] tc = geo.getCoordinates();
		boolean fakePolygon = true;
		if (tc.length <= 5) {
			for (int k = 0; k < tc.length; k++) {
				if (tc[k].x != tc[0].x || tc[k].y != tc[0].y) {
					fakePolygon = false;
				}
			}
		} else {
			fakePolygon = false;
		}
		Coordinate centerP = null;
		if (fakePolygon) {
			centerP = geo.getCoordinates()[0];
		} else {
			centerP = geo.getCentroid().getCoordinate();
		}

		coords[0] = new Coordinate(centerP.x, centerP.y);

		coords[1] = new Coordinate(centerP.x + 0.1, centerP.y + 0.1);
		coords[2] = new Coordinate(centerP.x + 0.1, centerP.y - 0.1);

		coords[3] = new Coordinate(centerP.x, centerP.y);

		// for (int i = 0; i < coords.length; i++) {
		// Coordinate coord = new Coordinate(centerP.x, centerP.y);
		// coords[i] = coord;
		// }

		LinearRing lr = gft.createLinearRing(coords);
		Polygon pg = gft.createPolygon(lr, null);
		return pg;
	}

	public Geometry getDeleteGeometryM(Geometry _geo, double distance) {

		if (_geo.getGeometryType().equals("Polygon") || _geo.getGeometryType().equals("LineString")) {
			return this.getDeleteGeometry(_geo, distance);
		} else if (_geo.getGeometryType().equals("MultiPolygon")) {
			Vector<Polygon> newps = new Vector();
			for (int i = 0; i < _geo.getNumGeometries(); i++) {
				Geometry subgeo = _geo.getGeometryN(i);
				Geometry conv = this.getDeleteGeometry((Polygon) subgeo, distance);
				if (conv != null) {
					newps.add((Polygon) conv);
				}
			}
			if (newps.size() == 0) {
				return null;
			}
			Polygon[] pls = new Polygon[newps.size()];
			for (int i = 0; i < newps.size(); i++) {
				pls[i] = newps.get(i);
			}
			return gft.createMultiPolygon(pls);
		} else if (_geo.getGeometryType().equals("MultiLineString")) {
			Vector<LineString> newps = new Vector();
			for (int i = 0; i < _geo.getNumGeometries(); i++) {
				Geometry subgeo = _geo.getGeometryN(i);
				Geometry conv = this.getDeleteGeometry((LineString) subgeo, distance);
				if (conv != null) {
					newps.add((LineString) conv);
				}
			}
			if (newps.size() == 0) {
				return null;
			}
			LineString[] pls = new LineString[newps.size()];
			for (int i = 0; i < newps.size(); i++) {
				pls[i] = newps.get(i);
			}
			return gft.createMultiLineString(pls);

		}

		return null;
	}

	public Geometry getDeleteGeometry(Geometry _geo, double distance) {

		if (_geo instanceof Polygon) {
			Vector<LineString> interV = new Vector();
			Polygon geo = (Polygon) _geo;
			LineString rl = geo.getExteriorRing();

			double rlArea = Math.abs(CGAlgorithms.signedArea(rl.getCoordinates()));
			if ((distance * distance) > rlArea) {
				return null;
			}
			int interCnt = geo.getNumInteriorRing();

			boolean deleteMode = false;

			for (int i = 0; i < interCnt; i++) {
				LineString inter = geo.getInteriorRingN(i);
				double itArea = Math.abs(CGAlgorithms.signedArea(inter.getCoordinates()));
				if ((distance * distance) < itArea) {
					interV.add(inter);
				} else {
					deleteMode = true;
				}
			}

			if (!deleteMode) {
				return _geo;
			}

			LinearRing[] lrs = new LinearRing[interV.size()];
			for (int i = 0; i < lrs.length; i++) {
				lrs[i] = (LinearRing) interV.get(i);
			}

			return gft.createPolygon((LinearRing) rl, lrs);
		} else if (_geo instanceof LineString) {
			LineString rl = (LineString) _geo;

			if (rl.getLength() < distance) {
				return null;
			}
			return _geo;

		}
		return _geo;
	}

	/**
	 * 특정 디렉토리 하위에서 입력된 이름의 Shapefile을 재귀적으로 검색합니다.
	 *
	 * @param dir        검색을 시작할 디렉토리
	 * @param targetName 찾고자 하는 Shapefile 이름 (확장자 .shp 유무 무관)
	 * @return 발견된 Shapefile 객체, 없으면 null
	 */
	public File findShapeFile(File dir, String targetName) {
		String searchName = targetName.toLowerCase();
		if (!searchName.endsWith(".shp")) {
			searchName += ".shp";
		}

		File[] listFile = dir.listFiles();
		if (listFile == null) {
			return null;
		}

		for (File file : listFile) {
			if (file.isDirectory()) {
				File found = findShapeFile(file, targetName);
				if (found != null) {
					return found;
				}
			} else {
				String name = file.getName().toLowerCase();
				if (name.equals(searchName)) {
					return file;
				}
			}
		}
		return null;
	}

	/**
	 * 특정 디렉토리 하위에서 입력된 이름의 Shapefile을 검색하고,
	 * 찾은 파일을 특정 대상 디렉토리에 동일한 이름의 FileLayer로 변환하여 저장합니다.
	 * 
	 * @param shpName       검색할 Shapefile 이름 (예: "test" 또는 "test.shp")
	 * @param searchDirPath Shapefile을 검색할 디렉토리 경로
	 * @param dstDirPath    변환된 FileLayer 파일이 저장될 대상 디렉토리 경로
	 */
	public void saveNamedShapeToFileLayer(String shpName, String searchDirPath, String dstDirPath) {

		File searchDir = new File(searchDirPath);
		if (!searchDir.exists() || !searchDir.isDirectory()) {
			System.out.println("Search directory does not exist or is not a directory: " + searchDirPath);
			return;
		}

		// 이름 기반으로 하위 디렉토리에서 shape file 검색
		File shpFile = findShapeFile(searchDir, shpName);
		if (shpFile == null) {
			System.out.println("Shape file '" + shpName + "' not found in: " + searchDirPath);
			return;
		}

		System.out.println("Found shape file: " + shpFile.getAbsolutePath());

		File dstDir = new File(dstDirPath);
		if (!dstDir.exists()) {
			dstDir.mkdirs();
		}

		// 저장할 FileLayer 이름 설정
		String cleanShpName = FilenameUtils.getBaseName(shpFile.getName());
		String dstPath = dstDirPath + File.separator + cleanShpName;

		// 이미 저장된 FileLayer 이면 건너뛴다 (layerSaveSkipExist=true 일 때)
		if (this.saveLayerSkipExist && new File(dstPath + File.separator + "layerInfo.xml").exists()) {
			System.out.println("이미 존재하여 저장 생략(layers): " + dstPath);
			return;
		}

		ShapeFile shp = null;
		FileLayer dstLayer = null;
		SimpleFeatureIterator reader = null;

		try {
			// Shapefile을 읽어오기 위한 객체 생성
			shp = new ShapeFile(shpFile, null, "utf-8");
			SimpleFeatureSource source = shp.getSource();
			SimpleFeatureType schema = source.getSchema();

			// Shapefile 스키마 정보를 FileLayer 저장을 위한 LayerInfo로 변환
			LayerInfo layerInfo = FileLayer.FromSimpleFeatureTypeToLayerInfo(schema);
			layerInfo.setName(cleanShpName);

			// 컬럼 이름을 소문자로 변경
			if (layerInfo.getSchema() != null) {
				for (com.mapplan.Field field : layerInfo.getSchema()) {
					if (field.getName() != null) {
						field.setName(field.getName().toLowerCase());
					}
				}
			}

			// FileLayer 생성 및 초기화
			dstLayer = new FileLayer(dstPath, layerInfo);

			reader = shp.getReader();
			SimpleFeatureBuilder sfb = new SimpleFeatureBuilder(dstLayer.getSimpleFeatureType());

			int count = 0;
			while (reader.hasNext()) {
				sfb.reset();
				SimpleFeature feature = reader.next();

				// 모든 속성 복사
				for (AttributeDescriptor descriptor : schema.getAttributeDescriptors()) {
					String attrName = descriptor.getLocalName();
					sfb.set(attrName.toLowerCase(), feature.getAttribute(attrName));
				}

				// Geometry 처리 및 오류 보정 적용
				Geometry geo = (Geometry) feature.getDefaultGeometry();
				if (geo != null && !geo.isEmpty()) {
					if (geo instanceof Polygon || geo instanceof MultiPolygon) {
						Geometry bb = geo.buffer(0.0);
						if (bb == null || bb.isEmpty()) {
							IsValidOp ivo = new IsValidOp(geo);
							if (!ivo.isValid()) {
								TopologyValidationError tve = ivo.getValidationError();
								if (tve.getErrorType() == 2) {
									Polygonizer pz = new Polygonizer(true);
									pz.add(geo);
									geo = pz.getGeometry();
								}
							}
						} else {
							geo = bb;
						}
					}

					// Geometry 바인딩 속성명을 찾아서 대입
					String geoAttrName = schema.getGeometryDescriptor().getLocalName();
					sfb.set(geoAttrName, geo);
				}

				SimpleFeature newft = sfb.buildFeature(feature.getID());
				dstLayer.writeFeature(newft);
				count++;
			}

			System.out.println("Successfully saved " + count + " features to FileLayer: " + dstPath);

		} catch (Exception e) {
			System.err.println("Error processing shape file: " + shpFile.getAbsolutePath());
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (shp != null) {
				try {
					shp.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (dstLayer != null) {
				try {
					dstLayer.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void setType(XSSFSheet sheet) {

		for (int rowIdx = 0; sheet.getRow(rowIdx) != null; rowIdx++) {
			Row row = sheet.getRow(rowIdx);
			for (int celIdx = 0; row.getCell(celIdx) != null; celIdx++) {
				Cell cel = row.getCell(celIdx);
				cel.setCellType(CellType.STRING);
			}
		}
	}

	public HashMap getJijukCodes() {
		HashMap<String, String> codes = new HashMap();

		int sheetCnt = this.workbook.getNumberOfSheets();

		String codeN = "코드값";
		String subN = "연속지독도상 표기";

		int codeIdx = -1;
		int subIdx = -1;

		// this.workbook.get
		for (int k = 0; k < sheetCnt; k++) {
			XSSFSheet sheet = this.workbook.getSheetAt(k);
			String sheetName = this.workbook.getSheetName(k);

			if (sheetName.equals("지목코드 조회자료")) {
				for (int i = 0; sheet.getRow(i) != null; i++) {
					Row row = sheet.getRow(i);

					if (i == 0) {
						for (int j = 0; row.getCell(j) != null; j++) {
							Cell cell = row.getCell(j);
							String name = cell.getStringCellValue();
							// System.out.println(name);

							codeIdx = name.equals(codeN) ? j : codeIdx;
							subIdx = name.equals(subN) ? j : codeIdx;
						}
					} else {
						Cell cell1 = row.getCell(codeIdx);
						String codeValue = cell1.getStringCellValue();

						Cell cell2 = row.getCell(subIdx);
						String subValue = cell2.getStringCellValue();

						codes.put(subValue, codeValue);

						// for(int j=0; row.getCell(j) != null; j++) {
						//
						// Cell cell = row.getCell(j);
						// String name = cell.getStringCellValue();
						//
						// }
					}
				}
			}
		}

		return codes;
	}

	public void createServiceInfo(boolean makeGridLayer) {
		int sheetCnt = this.workbook.getNumberOfSheets();

		// this.workbook.get
		for (int i = 0; i < sheetCnt; i++) {
			XSSFSheet sheet = this.workbook.getSheetAt(i);

			String sheetName = this.workbook.getSheetName(i);
			if (sheetName.equals("축척레벨")) {
				this.setType(sheet);
				this.setScaleInfos(sheet);
				setMapInfoScale(this.sis, this.mainMapInfo);
			} else if (sheetName.indexOf("주제도") > -1) {
				this.setType(sheet);
				this.setStylesMapInfo(sheet, makeGridLayer);
			} else if (sheetName.indexOf("tile") > -1) {
				this.setType(sheet);
				String[] splits = sheetName.split("_");
				this.setTile(sheet, splits[splits.length - 1]);
			}
		}

		try {

			ServiceConfig.writeStyles(
					this.dstPath + FileUt.SEPERATOR + ServiceConfig.tiles + FileUt.SEPERATOR + "style.xml",
					this.styles);
			Set set = this.mapInfos.keySet();
			Iterator it = set.iterator();
			while (it.hasNext()) {
				String tileName = (String) it.next();
				MapInfo mapInfo = this.mapInfos.get(tileName);

				ServiceConfig.writeMapInfo(this.dstPath + FileUt.SEPERATOR + ServiceConfig.tiles + FileUt.SEPERATOR
						+ tileName + FileUt.SEPERATOR + "mapInfo.xml", mapInfo);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void makeLayers() throws Exception {

		this.readServiceLayerInfo();

		Vector<Excel> layers = this.initExcel();

		if (layers == null) {
			return;
		}

		Vector<Excel> jijuk = new Vector();
		Vector<Excel> admin = new Vector();
		Vector<Excel> land = new Vector();
		Vector<Excel> city = new Vector();

		Vector<Excel> road = new Vector();

		for (Excel ex : layers) {
			// if (ex.getName().equals("01.연속지적도")) {
			if (ex.getFieldType().equals("A")) {
				jijuk.add(ex);
				// } else if (ex.getName().equals("00.행정구역")) {
			} else if (ex.getFieldType().equals("B")) {
				admin.add(ex);
				// } else if (ex.getName().equals("02.토지이용계획도")) {
				// land.add(ex);
				// } else if (ex.getName().equals("03.도시계획도")) {
				// city.add(ex);
				// } else if (ex.getName().equals("04.도로하천호수철도")) {
			} else if (ex.getFieldType().equals("Z")) {
				road.add(ex);
			} else {
				land.add(ex);
			}
		}
		System.out.println("jijuk = " + jijuk.size());
		System.out.println("admin = " + admin.size());
		System.out.println("land = " + land.size());
		// System.out.println("city = " + city.size());
		System.out.println("road = " + road.size());

		/**
		 * 
		 * •files
		 * •srcPath
		 * •dstPath
		 * •btreeIdx 엑셀의 "코드필드"에 정의된 컬럼 값을 연동규격의 code 값으로 검색 기능 제공. primaryKey
		 * •codeIndex 엑셀의 "코드필드"에 정의된 컬럼 값 단위로 공간 인덱스를 생성하여 코드 단위로 공간 검색이 가능하도록 제공
		 * •addName "uname" 컬럼을 만들고 엑셀의 지역지구명의 값을 저장하는데 사용
		 * •isSimple 단순화 가공 여부
		 */

		// btreeIdx false, codeIndex false
		// 도로
		this.makeLayer(road, this.srcPath, this.dstPath + FileUt.SEPERATOR + this.layers);

		// btreeIdx true, codeIndex false
		// 지적은 pnu 컬럼으로 검색 가능하도록
		this.makeLayer(jijuk, this.srcPath, this.dstPath + FileUt.SEPERATOR + this.layers);

		// btreeIdx true, codeIndex false
		// 시도 경계
		this.makeLayer(admin, this.srcPath, this.dstPath + FileUt.SEPERATOR + this.layers);

		// btreeIdx true, codeIndex true
		// 지역지구
		this.makeLayer(land, this.srcPath, this.dstPath + FileUt.SEPERATOR + this.layers);

	}

	// this.makeLayer(city, this.srcPath, this.dstPath + FileUt.SEPERATOR +
	// this.layers, true, true, "uname");

	public void makeTiles(String tileNames, int threadCnt, boolean tileUpdate) {

		String className = Geometry.class.getName().replace('.', '/') + ".class";
		String path = Oper.class.getClassLoader().getResource(className).getPath();

		String startWord = "file:/";

		int start = path.indexOf(startWord);

		int end = path.indexOf("!");

		File jarPath = new File(path.substring(start + startWord.length(), end));

		String depenPath = jarPath.getParent();

		System.out.println("dependency path=" + depenPath);// file:/

		File libraryFile = new File(
				"D:\\coops\\jetty-distribution-9.4.35.v20201120_server\\map_service\\lib\\win64\\imagequant.dll");

		if (libraryFile.exists()) {
			try {
				// System.loadLibrary("imagequant");
				System.load(libraryFile.getAbsolutePath());
			} catch (UnsatisfiedLinkError e) {
				e.printStackTrace();
			}
			MapLog.getSCLog().debug("load library imagequant , " + libraryFile);
		} else {

			String libraryPath = "";
			String libraryName = "";
			String cur_dir = System.getProperty("user.dir");

			String osName = System.getProperty("os.name");

			if (osName.toLowerCase().indexOf("win") > -1) {
				libraryName = "imagequant.dll";
				// libraryPath = cur_dir + FileUt.SEPERATOR + "dependency" + FileUt.SEPERATOR +
				// "lib" + FileUt.SEPERATOR
				// + "win64" + FileUt.SEPERATOR + libraryName;
				libraryPath = depenPath + FileUt.SEPERATOR + "lib" + FileUt.SEPERATOR
						+ "win64" + FileUt.SEPERATOR + libraryName;

			} else {
				libraryName = "libimagequant.so";
				// libraryPath = cur_dir + FileUt.SEPERATOR + "dependency" + FileUt.SEPERATOR +
				// "lib" + FileUt.SEPERATOR
				// + "linux64" + FileUt.SEPERATOR + libraryName;
				libraryPath = depenPath + FileUt.SEPERATOR + "lib" + FileUt.SEPERATOR
						+ "linux64" + FileUt.SEPERATOR + libraryName;

			}
			System.out.println("libraryPath path=" + libraryPath);// file:/
			libraryFile = new File(libraryPath);

			if (libraryFile.exists()) {
				try {

					System.load(libraryFile.getAbsolutePath());
				} catch (UnsatisfiedLinkError e) {
					e.printStackTrace();
				}

			} else {
				MapLog.getSCLog().debug("not load library imagequant , " + libraryFile);
			}
			System.out.println("libraryPath end =" + libraryPath);
		}

		String[] tiles = tileNames.split(",");

		for (String tileName : tiles) {
			if (this.mapInfos.containsKey(tileName)) {
				MapInfo mapInfo = this.mapInfos.get(tileName);
				if (this.isTileShutdown == false) {
					
					if(this.newMode) {
						this.makeTile_emap(mapInfo, threadCnt, tileUpdate);
					}
					else {
						this.makeTile(mapInfo, threadCnt, tileUpdate);
					}
				}
			}
		}

	}
	
	
	
	public void makeTile(MapInfo _mapInfo, int threadCnt, boolean tileUpdate) {
		
		System.out.println("makeTile start =" + _mapInfo.getName());

		MapInfo mapInfo = _mapInfo;

		StorageMng.symbolPath = this.dstPath + FileUt.SEPERATOR + "tiles" + FileUt.SEPERATOR + "symbols";

		MapData map = new MapData(mapInfo.getName(), mapInfo.getScaleInfos(), mapInfo.getLevelConfigs(), styles);
		map.layerPath = this.dstPath + FileUt.SEPERATOR + "layers" + FileUt.SEPERATOR;
		map.rasterPath = this.rasterPath + FileUt.SEPERATOR;
		try {
			map.readFileLayers();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("makeTile readFileLayers ");

		String dbPath = this.dstPath + FileUt.SEPERATOR + "tiles" + FileUt.SEPERATOR + mapInfo.getName();

		if (this.tmf != null) {
			this.tmf.stopTileMake();
			this.tmf = null;
		}

		tmf = new TileMapFactory2(mapInfo.getName(), mapInfo.getScaleInfos(), dbPath);
		
		StorageMng.threadCnt = threadCnt;

		tmf.setThreadCnt(threadCnt);
		tmf.setMapData(map);

		JobTile jt = new JobTile();

		jt.setName(mapInfo.getName());

		jt.setDrawCanvasRatio(3);
		jt.setCrs("EPSG:5179");

		jt.setThreadCnt(threadCnt);

		
		List<LevelSet> lss = jt.getLevelSet();
		LevelSet ls = new LevelSet();
		

		int minLevel = Integer.MAX_VALUE;
		int maxLevel = Integer.MIN_VALUE;
		LevelConfigs lcs = mapInfo.getLevelConfigs();
		List<LevelConfig> lcsss = lcs.getLevelConfig();
		for (LevelConfig lc : lcsss) {
			if (lc.getLayerInfo().size() > 0) {
				if (minLevel > lc.getLevelId()) {
					minLevel = lc.getLevelId();
				}
				if (maxLevel < lc.getLevelId()) {
					maxLevel = lc.getLevelId();
				}
			}
		}

		ls.setStartLevel(minLevel);

		if (this.stLevel != -1) {
			if (minLevel < this.stLevel) {
				ls.setStartLevel(this.stLevel);
			}
		}

		ls.setEndLevel(maxLevel);

		if (this.edLevel != -1) {
			if (maxLevel > this.edLevel) {
				ls.setEndLevel(this.edLevel);
			}
		}

		List<Mbr> mbrs = ls.getMbr();
		// 독도
		Mbr m1 = new Mbr();
		m1.setMinx(1392551);
		m1.setMiny(1945271);
		m1.setMaxx(1394884);
		m1.setMaxy(1947451);

		// 울릉도
		Mbr m2 = new Mbr();
		m2.setMinx(1289319);
		m2.setMiny(1943105);
		m2.setMaxx(1305002);
		m2.setMaxy(1956703);
		// 제주도
		Mbr m3 = new Mbr();
		m3.setMinx(870507);
		m3.setMiny(1457936);
		m3.setMaxx(951913);
		m3.setMaxy(1510332);
		// 내륙
		Mbr m4 = new Mbr();
		m4.setMinx(745135);
		m4.setMiny(1537165);
		m4.setMaxx(1192594);
		m4.setMaxy(2070199);

		mbrs.add(m1);
		mbrs.add(m2);
		mbrs.add(m3);
		mbrs.add(m4);
		
		if( _mapInfo.getName().indexOf("emp") > -1 ){
			ls.setStartLevel(6);

			LevelSet empls = new LevelSet();
			empls.setStartLevel(1);
			empls.setEndLevel(5);
			List<Mbr> empmbrs = empls.getMbr();
			
			
			
			Mbr empmbr = new Mbr();
			empmbr.setMinx(-285500);
			empmbr.setMiny(1265322);
			
			
			empmbr.setMaxx(2721971);
			empmbr.setMaxy(2980176);
			empmbrs.add(empmbr);
			
			lss.add(empls);
			
		}
		
		lss.add(ls);

		if (this.selectMbr != null) {
			mbrs.clear();

			for (Mbr sm : this.selectMbr) {
				mbrs.add(sm);
			}

			// mbrs.add(this.selectMbr);
		}

		tmf.makeTile(jt, false, tileUpdate, this.tileMbrUpdate);

	}


	public void makeTile_emap(MapInfo _mapInfo, int threadCnt, boolean tileUpdate) {

		System.out.println("makeTile start =" + _mapInfo.getName());

		MapInfo mapInfo = _mapInfo;

		StorageMng.symbolPath = this.dstPath + FileUt.SEPERATOR + "tiles" + FileUt.SEPERATOR + "symbols";

		MData map = null;

		if (newMode) {
			map = new com.gis2.map.MapData(mapInfo.getName(), mapInfo.getScaleInfos(), mapInfo.getLevelConfigs(),
					this.nstyles);

			com.gis2.map.MapData mapData = (com.gis2.map.MapData) map;

			mapData.layerPath = this.dstPath + FileUt.SEPERATOR + "layers_" + FileUt.SEPERATOR;
			mapData.rasterPath = this.rasterPath + FileUt.SEPERATOR;

			try {
				mapData.loadLayers();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {

			map = new MapData(mapInfo.getName(), sis, mapInfo.getLevelConfigs(), styles);

			MapData mapData = (MapData) map;

			if (mapInfo.getMbrExtend() == null) {
				mapData.setMbrExtend(200);
			} else {
				mapData.setMbrExtend(mapInfo.getMbrExtend());
			}

			mapData.layerPath = this.dstPath + FileUt.SEPERATOR + "layers" + FileUt.SEPERATOR;
			mapData.rasterPath = this.rasterPath + FileUt.SEPERATOR;
			try {
				mapData.readFileLayers();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		System.out.println("makeTile readFileLayers ");

		String dbPath = this.dstPath + FileUt.SEPERATOR + "tiles" + FileUt.SEPERATOR + mapInfo.getName();

		if (this.tmf != null) {
			this.tmf.stopTileMake();
			this.tmf = null;
		}

		tmf = new TileMapFactory2(mapInfo.getName(), mapInfo.getScaleInfos(), dbPath);

		StorageMng.threadCnt = threadCnt;

		tmf.setThreadCnt(threadCnt);
		tmf.setMapData(map);

		JobTile jt = new JobTile();

		jt.setName(mapInfo.getName());

		jt.setDrawCanvasRatio(3);
		jt.setCrs("EPSG:5179");

		jt.setThreadCnt(threadCnt);

		List<LevelSet> lss = jt.getLevelSet();
		LevelSet ls = new LevelSet();

		int minLevel = Integer.MAX_VALUE;
		int maxLevel = Integer.MIN_VALUE;
		LevelConfigs lcs = mapInfo.getLevelConfigs();
		List<LevelConfig> lcsss = lcs.getLevelConfig();
		for (LevelConfig lc : lcsss) {
			if (lc.getLayerInfo().size() > 0) {
				if (minLevel > lc.getLevelId()) {
					minLevel = lc.getLevelId();
				}
				if (maxLevel < lc.getLevelId()) {
					maxLevel = lc.getLevelId();
				}
			}
		}

		ls.setStartLevel(minLevel);

		if (this.stLevel != -1) {
			if (minLevel < this.stLevel) {
				ls.setStartLevel(this.stLevel);
			}
		}

		ls.setEndLevel(maxLevel);

		if (this.edLevel != -1) {
			if (maxLevel > this.edLevel) {
				ls.setEndLevel(this.edLevel);
			}
		}

		List<Mbr> mbrs = ls.getMbr();
		// 독도
		Mbr m1 = new Mbr();
		m1.setMinx(1392551);
		m1.setMiny(1945271);
		m1.setMaxx(1394884);
		m1.setMaxy(1947451);

		// 울릉도
		Mbr m2 = new Mbr();
		m2.setMinx(1289319);
		m2.setMiny(1943105);
		m2.setMaxx(1305002);
		m2.setMaxy(1956703);
		// 제주도
		Mbr m3 = new Mbr();
		m3.setMinx(870507);
		m3.setMiny(1457936);
		m3.setMaxx(951913);
		m3.setMaxy(1510332);
		// 내륙
		Mbr m4 = new Mbr();
		m4.setMinx(745135);
		m4.setMiny(1537165);
		m4.setMaxx(1192594);
		m4.setMaxy(2070199);

		mbrs.add(m1);
		mbrs.add(m2);
		mbrs.add(m3);
		mbrs.add(m4);

		if (_mapInfo.getName().indexOf("emp") > -1) {
			ls.setStartLevel(6);

			LevelSet empls = new LevelSet();
			empls.setStartLevel(1);
			empls.setEndLevel(5);
			List<Mbr> empmbrs = empls.getMbr();

			Mbr empmbr = new Mbr();
			empmbr.setMinx(-285500);
			empmbr.setMiny(1265322);

			empmbr.setMaxx(2721971);
			empmbr.setMaxy(2980176);
			empmbrs.add(empmbr);

			lss.add(empls);

		}

		lss.add(ls);

		if (this.selectMbr != null) {
			mbrs.clear();

			for (Mbr sm : this.selectMbr) {
				mbrs.add(sm);
			}

			// mbrs.add(this.selectMbr);
		}

		tmf.makeTile(jt, false, tileUpdate, this.tileMbrUpdate);

	}

	/**
	 * 
	 * @param files
	 * @param srcPath
	 * @param dstPath
	 * @param btreeIdx  엑셀의 "코드필드"에 정의된 컬럼 값을 연동규격의 code 값으로 검색 기능 제공. primaryKey
	 * @param codeIndex 엑셀의 "코드필드"에 정의된 컬럼 값 단위로 공간 인덱스를 생성하여 코드 단위로 공간 검색이 가능하도록 제공
	 * @param addName   "uname" 컬럼을 만들고 엑셀의 지역지구명의 값을 저장하는데 사용
	 * @param isSimple  단순화 가공 여부
	 */
	public void makeLayer(Vector<Excel> files, String srcPath, String dstPath) {

		if (files.size() == 0) {
			return;
		}

		Excel ji = files.get(0);

		// Vector<String> dstFiles = new Vector();

		HashMap<String, Vector<Excel>> dstFiles = new HashMap();

		if (ji.getFoldName() == null || ji.getFoldName().length() == 0) {
			for (Excel cel : files) {

				String key = cel.getName();

				if (!dstFiles.containsKey(key)) {
					Vector<Excel> subs = new Vector();
					subs.add(cel);
					dstFiles.put(key, subs);
				} else {
					Vector<Excel> subs = dstFiles.get(key);
					subs.add(cel);
				}

			}
		} else if (ji.getFoldName1() == null || ji.getFoldName1().length() == 0) {
			for (Excel cel : files) {
				// dstFiles.add(cel.getName()+FileUt.SEPERATOR+cel.getFoldName());
				// dstFiles.add(cel.getFoldName());

				String key = cel.getName() + FileUt.SEPERATOR + cel.getFoldName();

				if (!dstFiles.containsKey(key)) {
					Vector<Excel> subs = new Vector();
					subs.add(cel);
					dstFiles.put(key, subs);
				} else {
					Vector<Excel> subs = dstFiles.get(key);
					subs.add(cel);
				}

			}
		} else {
			for (Excel cel : files) {
				// dstFiles.add(cel.getName()+FileUt.SEPERATOR+cel.getFoldName());
				// dstFiles.add(cel.getFoldName());
				String key = cel.getName() + FileUt.SEPERATOR + cel.getFoldName();

				if (!dstFiles.containsKey(key)) {
					Vector<Excel> subs = new Vector();
					subs.add(cel);
					dstFiles.put(key, subs);
				} else {
					Vector<Excel> subs = dstFiles.get(key);
					subs.add(cel);
				}
			}
		}

		Set set = dstFiles.keySet();
		Iterator it = set.iterator();
		while (it.hasNext()) {
			String dstFile = (String) it.next();

			File srcFile = new File(srcPath + FileUt.SEPERATOR + dstFile);

			File temp = new File(dstPath + FileUt.SEPERATOR + dstFile);

			File jidst = new File(dstPath + FileUt.SEPERATOR + temp.getName());

			// System.out.println(srcFile.getAbsolutePath());
			// System.out.println(jidst.getAbsolutePath());

			Vector<Excel> ecs = dstFiles.get(dstFile);

			try {
				long st = System.currentTimeMillis();

				// LOG.debug("src = " + srcFile.getAbsolutePath()+", dst = " +
				// jidst.getAbsolutePath());
				//
				// for(Excel ec : ecs) {
				// LOG.debug("|Name=" + ec.getName() + "|FoldName=" + ec.getFoldName() +
				// "|FoldName1="
				// + ec.getFoldName1() + "|FoldName2=" + ec.getFoldName2() + "|CodeField=" +
				// ec.getCodeField()
				// + "|TextField=" + ec.getTextField() + "|Epsg=" + ec.getEpsg() + "|Code=" +
				// ec.getCode()
				// + "|DeleteCode=" + ec.getDeleteCode() + "|FillColor=" + ec.getFillColor() +
				// "|FillWidth="
				// + ec.getFillWidth() + "|LineColor=" + ec.getLineColor() + "|LineWidth=" +
				// ec.getLineWidth()+"|fieldType="+ec.getFieldType());
				//
				// }
				if (srcFile.exists()) {
					System.out.println("");

					// if(!jidst.getName().toLowerCase().equals("aa")) {
					// continue;
					// }

					this.runLayer(ecs, srcFile, jidst, "EPSG:5179");

				} else {
					LOG.debug("not exist " + srcFile.getAbsolutePath());
				}
				long et = System.currentTimeMillis();

				LOG.debug(jidst.getName() + " end, time = " + (et - st) / 1000 / 60 + " min");
				// System.out.println("runJijuk time = " + (et - st) / 1000 / 60 + " min");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	public void getShapeFiles(Vector<File> shps, File _file) {
		File[] listFile = _file.listFiles();
		for (File file : listFile) {
			if (file.isDirectory()) {
				this.getShapeFiles(shps, file);
			} else {
				if (file.getAbsolutePath().lastIndexOf(".shp") > -1
						&& file.getAbsolutePath().lastIndexOf(".xml") == -1) {
					shps.add(file);
				}
			}
		}
	}

	public String getAddFields(Vector<Excel> ecs) {
		String addFields = "";

		for (Excel ec : ecs) {
			String afs = ec.getAddFields();
			if (afs != null && afs.length() > 0) {
				addFields = ec.getAddFields();
				return addFields;
			}
		}
		return null;
	}

	public Field getField(LayerInfo ly, String name) {

		for (Field fd : ly.getSchema()) {
			if (fd.getName().equals(name)) {
				return fd;
			}
		}

		return null;
	}

	public void runLayer(Vector<Excel> ecs, File src, File dst, String epsg) throws Exception {

		File srcPathF = new File(this.srcPath);

		Vector<String> srcFiles = new Vector();

		File tt1 = null;

		srcFiles.add(srcPathF.getName());

		File copyselect1 = new File(srcPathF.getAbsolutePath());
		while ((tt1 = copyselect1.getParentFile()) != null) {
			String nm = tt1.getName();
			if (nm.trim().length() > 0) {
				srcFiles.add(nm);
			} else {
				srcFiles.add(tt1.getAbsolutePath());
			}

			copyselect1 = tt1;
		}

		final String addFields = this.getAddFields(ecs);

		String dstname = dst.getName().toLowerCase();
		final boolean isSido = dstname.equals("fb") ? true : false;
		final boolean isSigungu = dstname.equals("fc") ? true : false;
		final boolean isJijuk = dstname.equals("fa") ? true : false;

		final HashMap<String, String> jijukCodes = isJijuk ? this.getJijukCodes() : null;

		Excel ec = ecs.get(0);

		String dstEPSG = "EPSG:5179";

		LayerInfo layerInfo = this.layerInfos.get(dst.getName().toLowerCase());

		// LayerInfo li = new LayerInfo();

		CoordinateReferenceSystem crs = CRS.decode(dstEPSG);

		layerInfo.setProjection(crs.toWKT());

		HashMap<String, String> codes = new HashMap();

		// addName 은 uname
		if (this.getField(layerInfo, "uname") != null) {

			for (Excel ex : ecs) {
				codes.put(ex.getCode(), ex.getLandCityName());
			}
		}
		// 코드필드
		/*
		 * if (ec.getCodeField() != null && ec.getCodeField().length() > 0) {
		 * Field fd2 = new Field();
		 * fd2.setName(ec.getCodeField().toLowerCase());
		 * fd2.setType(FieldType.STRING);
		 * 
		 * if (btreeIdx) {
		 * fd2.setIsIndex(true);
		 * }
		 * if (codeIndex) {
		 * fd2.setIsCodeIndex(true);
		 * }
		 * 
		 * fds.add(fd2);
		 * }
		 */
		// 주기이름필드
		/*
		 * if (ec.getTextField() != null && ec.getTextField().length() > 0) {
		 * Field fd3 = new Field();
		 * fd3.setName(ec.getTextField().toLowerCase());
		 * fd3.setType(FieldType.STRING);
		 * fds.add(fd3);
		 * }
		 */
		// boolean isOrder = false;
		// String orderFd = "";
		// 디폴트
		/*
		 * if (ec.getDrawOrder() != null && ec.getDrawOrder().length() > 0) {
		 * Field fd3 = new Field();
		 * fd3.setName("order");
		 * fd3.setType(FieldType.DOUBLE);
		 * fds.add(fd3);
		 * // isOrder = true;
		 * // orderFd = ec.getDrawOrder();
		 * }
		 */
		// 지적의 경우
		/*
		 * if (isJijuk) {
		 * Field fd4 = new Field();
		 * fd4.setName("code");
		 * fd4.setType(FieldType.STRING);
		 * fds.add(fd4);
		 * }
		 */
		// 추가필드

		/*
		 * if (addFields != null) {
		 * String[] ads = addFields.split("\\|");
		 * for (String ad : ads) {
		 * Field fd = new Field();
		 * fd.setName(ad.toLowerCase());
		 * fd.setType(FieldType.STRING);
		 * fds.add(fd);
		 * }
		 * Field fdCode = new Field();
		 * fdCode.setName("fd_code");
		 * fdCode.setType(FieldType.STRING);
		 * fds.add(fdCode);
		 * }
		 */

		FileLayer dstLayer = new FileLayer(dst.getAbsolutePath(), layerInfo);

		Vector<File> shps = new Vector();

		this.getShapeFiles(shps, src);

		Vector<File> excelExist = new Vector();

		for (File file : shps) {

			Vector<Excel> values = getExcel(ecs, FilenameUtils.separatorsToUnix(file.getAbsolutePath()));

			if (values.size() > 0) {
				/*
				 * LOG.debug("src:"+file.getAbsolutePath()); for(Excel tt : values) {
				 * LOG.debug("|Name=" + tt.getName() + "|FoldName=" + tt.getFoldName() +
				 * "|FoldName1=" + tt.getFoldName1() + "|FoldName2=" + tt.getFoldName2() +
				 * "|CodeField=" + tt.getCodeField() + "|TextField=" + tt.getTextField() +
				 * "|Epsg=" + tt.getEpsg() + "|Code=" + tt.getCode() + "|DeleteCode=" +
				 * tt.getDeleteCode() + "|FillColor=" + tt.getFillColor() + "|FillWidth=" +
				 * tt.getFillWidth() + "|LineColor=" + tt.getLineColor() + "|LineWidth=" +
				 * tt.getLineWidth()+"|fieldType="+tt.getFieldType());
				 * 
				 * break; }
				 */
				excelExist.add(file);
			} else {
				LOG.debug("not exist excel file :" + file.getAbsolutePath());
			}
		}

		shps = excelExist;

		// ThreadPoolExecutor executorService = (ThreadPoolExecutor)
		// Executors.newFixedThreadPool(10);
		ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

		Vector<Future> fts = new Vector();

		for (File file : shps) {

			Object lock = new Object();

			final File select = file;

			Future ft = executorService.submit(() -> {

				int idx = 0;
				try {

					Vector<Excel> values = getExcel(ecs, FilenameUtils.separatorsToUnix(file.getAbsolutePath()));
					String fieldType = values.get(0).getFieldType();

					MathTransform toViewTransform = null;
					if (!dstEPSG.equals(ec.getEpsg().toUpperCase())) {
						CoordinateReferenceSystem srccrs = CRS.decode(ec.getEpsg().toUpperCase());
						toViewTransform = CRS.findMathTransform(srccrs, crs, true);
					}

					String layerName = FilenameUtils.getBaseName(select.getAbsolutePath());

					System.out.println("layerName = " + layerName + ", epsg=" + ec.getEpsg());

					Vector<String> tempFiles = new Vector();

					File tt = null;

					tempFiles.add(select.getName());

					File copyselect = new File(select.getAbsolutePath());
					while ((tt = copyselect.getParentFile()) != null) {
						String nm = tt.getName();
						if (nm.trim().length() > 0) {
							tempFiles.add(nm);
						} else {
							tempFiles.add(tt.getAbsolutePath());
						}
						copyselect = tt;
					}

					String fdCode = tempFiles.get(tempFiles.size() - srcFiles.size() - 3);

					Map<String, Serializable> shpparams = new HashMap<String, Serializable>();
					shpparams.put("url", select.toURI().toURL());
					shpparams.put("charset", "euc-kr");
					DataStore sdataStore = DataStoreFinder.getDataStore(shpparams);
					String name = sdataStore.getTypeNames()[0];
					SimpleFeatureType featuretype = sdataStore.getSchema(name);

					Query query = new Query(layerName);
					SimpleFeatureSource sfeatureSource = sdataStore.getFeatureSource(name);
					SimpleFeatureCollection shpecollection = sfeatureSource.getFeatures();

					String temp = select.getAbsolutePath().replace(this.srcPath, "");

					// String[] tt = temp.split("\\\\");

					// System.out.println("layerName=" + layerName);

					String testLayer = "UPIS_C_UQ151";

					HashMap<String, String> deleteCodes = getDeleteCode(ecs,
							FilenameUtils.separatorsToUnix(select.getAbsolutePath()));

					if (deleteCodes.size() > 0) {

						System.out.print(select.getAbsolutePath() + ", delete=");

						Set set = deleteCodes.keySet();
						Iterator it = set.iterator();
						while (it.hasNext()) {
							String key = (String) it.next();
							System.out.print(key + ",");
						}
						// System.out.println("");
					}

					SimpleFeatureIterator reader = shpecollection.features();

					boolean isCode = false;

					if (ec.getCodeField() != null && ec.getCodeField().length() > 0) {
						isCode = true;
					}

					boolean isMNUM = false;

					if (fieldType.toLowerCase().equals("c")) {
						isMNUM = true;
					}

					SimpleFeatureBuilder sfb = new SimpleFeatureBuilder(dstLayer.getSimpleFeatureType());

					while (reader.hasNext()) {
						sfb.reset();
						SimpleFeature feature = reader.next();

						// boolean isTest = false;
						//
						// if(layerName.equals(testLayer)) {
						// if(feature.getAttribute("PRESENT_SN") != null
						// && feature.getAttribute("ATRB_SE") != null
						// && feature.getAttribute("WTNNC_SN") != null
						// && feature.getAttribute("PRESENT_SN").equals("41270UQ151PS201811302711")
						// && feature.getAttribute("ATRB_SE").equals("UQS121")
						// && feature.getAttribute("WTNNC_SN").equals("41270URZ201811304087")) {
						// isTest = true;
						// }
						// }

						try {

							if (isCode) {

								String rName = "";
								String sName = "";
								if (ec.getCodeField().indexOf(":") > -1) {
									String[] names = ec.getCodeField().split(":");
									rName = names[0];
									sName = names[1].toLowerCase();
								} else {
									rName = ec.getCodeField();
									sName = ec.getCodeField().toLowerCase();
								}

								if (isMNUM) {
									String value = (String) feature.getAttribute(rName);

									if (value.length() >= 26) {
										String code = value.substring(20, 26);
										sfb.set(sName, code);
									}

								} else {
									// String fdName = ec.getCodeField();
									String code = (String) feature.getAttribute(rName);
									sfb.set(sName, code);
								}
							}

							if (ec.getTextField() != null && ec.getTextField().length() > 0) {

								String rName = "";
								String sName = "";
								if (ec.getTextField().indexOf(":") > -1) {
									String[] names = ec.getTextField().split(":");
									rName = names[0];
									sName = names[1].toLowerCase();
								} else {
									rName = ec.getTextField();
									sName = ec.getTextField().toLowerCase();
								}

								sfb.set(sName, feature.getAttribute(rName));
							}

							Geometry geo = (Geometry) feature.getDefaultGeometry();

							if (geo == null || geo.isEmpty()) {
								continue;
							}

							if (toViewTransform != null) {
								geo = JTS.transform((Geometry) feature.getDefaultGeometry(), toViewTransform);
							}

							if (geo instanceof Polygon || geo instanceof MultiPolygon) {

								Geometry bb = geo.buffer(0.0);

								if (bb == null || bb.isEmpty()) {
									IsValidOp ivo = new IsValidOp(geo);
									if (!ivo.isValid()) {
										TopologyValidationError tve = ivo.getValidationError();
										if (tve.getErrorType() == 2) {
											Polygonizer pz = new Polygonizer(true);
											pz.add(geo);
											geo = pz.getGeometry();
										}
									}
								} else {
									geo = bb;
								}

							}

							Field geofd = this.getField(layerInfo, "the_geom");

							if (geofd.getSimpleArea() > 0 && (geo instanceof Polygon || geo instanceof MultiPolygon)) {
								geo = getSimplePolygonm(geo, geofd.getSimpleArea().intValue());
							}
							if (geofd.getSimpleLine() > 0) {
								geo = TopologyPreservingSimplifier.simplify(geo, geofd.getSimpleLine());
							}

							/*
							 * if (isSimple) {
							 * 
							 * if (isSido == false && isSigungu == false) {
							 * // if(this.isSimple) {
							 * if (this.isInputSimple) {
							 * geo = TopologyPreservingSimplifier.simplify(geo, 0.5);
							 * }
							 * // }
							 * } else if (isSido) {
							 * if (this.isInputSimple) {
							 * geo = getSimplePolygonm(geo, 2000);// 2000
							 * geo = TopologyPreservingSimplifier.simplify(geo, 50);// 200
							 * }
							 * } else if (isSigungu) {
							 * if (this.isInputSimple) {
							 * geo = getSimplePolygonm(geo, 500);// 2000
							 * geo = TopologyPreservingSimplifier.simplify(geo, 10);// 200
							 * }
							 * }
							 * } else {
							 * 
							 * if (isSido == false && isSigungu == false) {
							 * if (this.isInputSimple) {
							 * geo = TopologyPreservingSimplifier.simplify(geo, 0.5);
							 * }
							 * } else if (isSido) {
							 * if (this.isInputSimple) {
							 * geo = getSimplePolygonm(geo, 2000);// 2000
							 * geo = TopologyPreservingSimplifier.simplify(geo, 50);// 200
							 * }
							 * } else if (isSigungu) {
							 * if (this.isInputSimple) {
							 * geo = getSimplePolygonm(geo, 500);// 2000
							 * geo = TopologyPreservingSimplifier.simplify(geo, 10);// 200
							 * }
							 * }
							 * }
							 */
							sfb.set("the_geom", geo);

							SimpleFeature newft = sfb.buildFeature(null);

							if (deleteCodes.size() > 0) {
								String deleteCode = (String) newft.getAttribute(ec.getCodeField().toLowerCase());
								if (deleteCodes.containsKey(deleteCode)) {
									continue;
								}
							}

							if (newft.getFeatureType().indexOf("order") > -1) {
								double orderValue = 0;

								Object obj = feature.getAttribute(ec.getDrawOrder());

								if (obj != null) {
									if (obj instanceof Integer) {
										orderValue = ((Integer) obj);
									} else if (obj instanceof Double) {
										orderValue = ((Double) obj);
									} else if (obj instanceof String) {
										orderValue = Double.parseDouble((String) obj);
									} else if (obj instanceof Long) {
										orderValue = ((Long) obj);
									}
									newft.setAttribute("order", orderValue);
								}
							}

							if (this.getField(layerInfo, "uname") != null) {

								String rName = "";
								String sName = "";
								if (ec.getCodeField().indexOf(":") > -1) {
									String[] names = ec.getCodeField().split(":");
									rName = names[0];
									sName = names[1].toLowerCase();
								} else {
									rName = ec.getCodeField();
									sName = ec.getCodeField().toLowerCase();
								}

								String code = (String) newft.getAttribute(sName);
								if (code != null && code.length() > 1) {
									String addNameValue = codes.get(code);
									newft.setAttribute("uname", addNameValue);
								}

								// sfb.set(addName.toLowerCase(), code);
							}

							if (isJijuk) {
								String jibun = ((String) newft.getAttribute("jibun")).trim().replaceAll(" ", "");

								if (jibun.lastIndexOf("도로") > -1) {
									jibun = jibun.replaceAll("도로", "도");
								}

								jibun = jibun.replaceAll("\\(", "");
								jibun = jibun.replaceAll("\\)", "");
								if (jibun.length() > 0) {
									String suffix = jibun.substring(jibun.length() - 1, jibun.length());

									if (suffix.equals("?")) {
										jibun = jibun.substring(0, jibun.length() - 1);
									}
									newft.setAttribute("jibun", jibun);

									String jijukCode = jijukCodes.get(suffix);

									if (jijukCode == null) {
										// System.out.println("jibun name = " + jibun);
										newft.setAttribute("code", "99");
									} else {
										newft.setAttribute("code", jijukCode);
									}
								} else {
									newft.setAttribute("code", "99");
								}
							}

							if (addFields != null) {

								String[] ads = addFields.split("\\|");
								for (String ad : ads) {

									String rName = "";
									String sName = "";
									if (ad.toLowerCase().indexOf(":") > -1) {
										String[] names = ad.split(":");
										rName = names[0];
										sName = names[1].toLowerCase();
									} else {
										rName = ad;
										sName = ad.toLowerCase();
									}

									if (feature.getType().indexOf(rName) > -1) {
										newft.setAttribute(sName, feature.getAttribute(rName));
									} else {
										LOG.error(name + " shpe file, not exist field =" + rName);
									}
								}

								if (this.getField(layerInfo, "fd_code") != null) {
									newft.setAttribute("fd_code", fdCode);
								}
							}

							dstLayer.writeFeature(newft);
							idx++;
						} catch (Exception e) {
							LOG.debug("error layer=" + select.getAbsolutePath() + ", " + e.toString() + ", idx=" + idx,
									e);
						}
					}

					reader.close();

					sdataStore.dispose();

				} catch (Exception ex) {
					LOG.debug("error layer=" + select.getAbsolutePath() + ", " + ex.toString() + ", idx=" + idx, ex);
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

		dstLayer.close();

		executorService.shutdown();
	}

	public Vector<Excel> getExcel(Vector<Excel> ecs, String _path) {

		Vector<Excel> values = new Vector();

		String temp = _path.replace(this.srcPath, "");

		String[] tt = null;
		String osName = System.getProperty("os.name");
		String sp = "";
		if (osName.toLowerCase().indexOf("win") > -1) {
			// tt = temp.split("\\\\");
			tt = temp.split("\\/");
		} else {
			tt = temp.split("\\/");
		}

		// String[] tt = temp.split("\\\\");

		HashMap<String, String> codes = new HashMap();

		for (Excel ec : ecs) {

			if (ec.getDeleteCode().equals("삭제")) {
				continue;
			}

			boolean insert = true;

			if (tt[1].indexOf(ec.getName()) == -1) {
				insert = false;
			}
			if ((ec.getFoldName().length() > 0) && tt[2].indexOf(ec.getFoldName()) == -1) {
				insert = false;
			}
			if ((ec.getFoldName1().length() > 0) && tt[3].indexOf(ec.getFoldName1()) == -1) {
				insert = false;
			}
			if ((ec.getFoldName2().length() > 0) && tt[4].indexOf(ec.getFoldName2()) == -1) {
				insert = false;
			}
			// System.out.println(ec.getName()+"|"+ec.getFoldName()+"|"+ec.getFoldName1()+"|"+ec.getFoldName2()+"|"+ec.getCode());
			if (insert) {

				values.add(ec);
			}
		}

		return values;
	}

	public HashMap<String, String> getDeleteCode(Vector<Excel> ecs, String _path) {

		String temp = _path.replace(this.srcPath, "");

		String[] tt = null;
		String osName = System.getProperty("os.name");
		String sp = "";
		if (osName.toLowerCase().indexOf("win") > -1) {
			// tt = temp.split("\\\\");
			tt = temp.split("\\/");
		} else {
			tt = temp.split("\\/");
		}

		// String[] tt = temp.split(sp);

		HashMap<String, String> codes = new HashMap();

		for (Excel ec : ecs) {

			if (!ec.getDeleteCode().equals("삭제")) {
				continue;
			}

			boolean insert = true;

			if (tt[1].indexOf(ec.getName()) == -1) {
				insert = false;
			}
			if ((ec.getFoldName().length() > 0) && tt[2].indexOf(ec.getFoldName()) == -1) {
				insert = false;
			}
			if ((ec.getFoldName1().length() > 0) && tt[3].indexOf(ec.getFoldName1()) == -1) {
				insert = false;
			}
			if ((ec.getFoldName2().length() > 0) && tt[4].indexOf(ec.getFoldName2()) == -1) {
				insert = false;
			}

			if (insert) {
				// System.out.println(ec.getName()+"|"+ec.getFoldName()+"|"+ec.getFoldName1()+"|"+ec.getFoldName2()+"|"+ec.getCode());
				codes.put(ec.getCode(), ec.getCode());
			}
		}

		return codes;
	}

	// public LayerInfo getJijuk(File select, Vector<String> fields, String epsg)
	// throws Exception{
	// String layerName = FilenameUtils.getBaseName(select.getAbsolutePath());
	//
	// Map<String, Serializable> shpparams = new HashMap<String, Serializable>();
	// shpparams.put("url", select.toURI().toURL());
	// shpparams.put("charset", "euc-kr");
	// DataStore sdataStore = DataStoreFinder.getDataStore(shpparams);
	// String name = sdataStore.getTypeNames()[0];
	// SimpleFeatureType featuretype = sdataStore.getSchema(name);
	//
	// LayerInfo li = FreeLayer.FromSimpleFeatureTypeToLayerInfo(featuretype);
	//
	// List<Field> fds = li.getSchema();
	//
	// for() {
	//
	// }
	//
	// sdataStore.dispose();
	//
	// return li;
	// }

	public SimpleFeatureType createRequestSchema(SimpleFeatureType oriSft, Vector<String> fieldNames,
			List<String> removeFieldNames) {
		SimpleFeatureTypeBuilder sftb = new SimpleFeatureTypeBuilder();

		SimpleFeatureType sft = oriSft;

		SimpleFeatureType ldongSft = sftb.copy(sft);
		sftb.setName(ldongSft.getName());
		List<AttributeDescriptor> saveADs = new ArrayList();

		List<AttributeDescriptor> tempADs = sft.getAttributeDescriptors();

		for (AttributeDescriptor tempAD : tempADs) {
			boolean addMode = true;
			for (String removeField : removeFieldNames) {
				if (tempAD.getName().equals(removeField)) {
					addMode = false;
				}
			}
			if (addMode) {
				saveADs.add(tempAD);
			}
		}

		sftb.addAll(saveADs);

		if (fieldNames != null) {
			for (String fieldName : fieldNames) {
				AttributeTypeBuilder build = new AttributeTypeBuilder();
				build.binding(String.class);
				build.setNillable(true);
				sftb.add(build.buildDescriptor(fieldName));
			}
		}

		SimpleFeatureType reqLdongSFT = sftb.buildFeatureType();
		return reqLdongSFT;
	}

	public void readExcel() throws Exception {
		FileInputStream fis = new FileInputStream(new File(this.excelpath));
		this.workbook = new XSSFWorkbook(fis);
	}

	public void closeExcel() throws Exception {
		if(this.workbook != null) {
			this.workbook.close();
		}
	}

	public void readServiceLayerInfo() {
		XSSFSheet sheet = this.workbook.getSheet("서비스레이어");

		if (sheet == null) {
			return;
		}

		int layerNameIdx = -1;
		int fieldNameIdx = -1;
		int fieldTypeIdx = -1;
		int pkKeyIdx = -1;
		int fieldDescIdx = -1;
		int simpleValueIdx = -1;
		int codeIndexIdx = -1;

		String layerName = "";
		String fieldName = "";
		String fieldType = "";
		String pkKey = "";
		String fieldDesc = "";
		String simpleValue = "";
		String codeIndex = "";

		int xlsxRows = sheet.getPhysicalNumberOfRows();

		boolean newLayer = false;

		LayerInfo lyInfo = null;

		for (int rownum = 0; rownum < xlsxRows; rownum++) {
			XSSFRow xlsxRow = sheet.getRow(rownum); // 셀정보

			if (xlsxRow == null) {
				continue;
			}
			// System.out.println(rownum);

			short maxCellNum = xlsxRow.getLastCellNum();

			if (rownum == 0) {
				for (int cellIdx = 0; cellIdx < maxCellNum; cellIdx++) {

					XSSFCell cell = xlsxRow.getCell(cellIdx);

					if (cell != null && rownum == 0) {
						String fdN = xlsxRow.getCell(cellIdx).toString();
						if (fdN.equals("레이어이름")) {
							layerNameIdx = cellIdx;
							// epsg:좌표계
						} else if (fdN.equals("컬럼이름")) {
							fieldNameIdx = cellIdx;
							// 코드필드에 존재하는 코드 값
						} else if (fdN.equals("컬럼타입")) {
							fieldTypeIdx = cellIdx;
							// 코드필드에 존재하는 코드 값중 삭제 대상의 경우 "삭제"
						} else if (fdN.equals("PK")) {
							pkKeyIdx = cellIdx;
							//
						} else if (fdN.equals("컬럼설명")) {
							fieldDescIdx = cellIdx;
						} else if (fdN.equals("공간간소화수치")) {
							simpleValueIdx = cellIdx;
						} else if (fdN.equals("코드인덱스")) {
							codeIndexIdx = cellIdx;
						}
					}
				}
			}
			if (rownum == 0) {
				continue;
			}

			layerName = xlsxRow.getCell(layerNameIdx) != null ? xlsxRow.getCell(layerNameIdx).toString() : "";
			fieldName = xlsxRow.getCell(fieldNameIdx) != null ? xlsxRow.getCell(fieldNameIdx).toString() : "";
			fieldType = xlsxRow.getCell(fieldTypeIdx) != null ? xlsxRow.getCell(fieldTypeIdx).toString() : "";
			pkKey = xlsxRow.getCell(pkKeyIdx) != null ? xlsxRow.getCell(pkKeyIdx).toString() : "";
			fieldDesc = xlsxRow.getCell(fieldDescIdx) != null ? xlsxRow.getCell(fieldDescIdx).toString() : "";
			simpleValue = xlsxRow.getCell(simpleValueIdx) != null ? xlsxRow.getCell(simpleValueIdx).toString() : "";
			codeIndex = xlsxRow.getCell(codeIndexIdx) != null ? xlsxRow.getCell(codeIndexIdx).toString() : "";

			if (layerName.length() > 0) {
				lyInfo = new LayerInfo();
				lyInfo.setName(layerName);

				this.layerInfos.put(layerName, lyInfo);
			}

			List<Field> fds = lyInfo.getSchema();

			if (fieldName.length() > 0) {
				Field fd = new Field();
				fd.setName(fieldName);
				fd.setDesc(fieldDesc);

				if (fieldType.equals(FieldType.STRING.value())) {
					fd.setType(FieldType.STRING);
				} else if (fieldType.equals(FieldType.INT.value())) {
					fd.setType(FieldType.INT);
				} else if (fieldType.equals(FieldType.BYTE.value())) {
					fd.setType(FieldType.BYTE);
				} else if (fieldType.equals(FieldType.FLOAT.value())) {
					fd.setType(FieldType.FLOAT);
				} else if (fieldType.equals(FieldType.LONG.value())) {
					fd.setType(FieldType.LONG);
				} else if (fieldType.equals(FieldType.DOUBLE.value())) {
					fd.setType(FieldType.DOUBLE);
				} else if (fieldType.equals(FieldType.SHORT.value())) {
					fd.setType(FieldType.SHORT);
				} else if (fieldType.equals(FieldType.INT.value())) {
					fd.setType(FieldType.INT);
				} else if (fieldType.equals(FieldType.GEOMETRY.value())) {
					fd.setType(FieldType.GEOMETRY);
					fd.setIsIndex(true);
				}

				if (pkKey.equals("o") || pkKey.equals("O")) {
					fd.setIsIndex(true);
				}

				if (codeIndex.equals("o") || codeIndex.equals("O")) {
					fd.setIsCodeIndex(true);
				}

				if (simpleValue.length() > 0) {

					String[] ads = simpleValue.split("\\|");

					fd.setSimpleArea(Float.parseFloat(ads[0]));
					fd.setSimpleLine(Float.parseFloat(ads[1]));

				}
				fds.add(fd);
			}

		}
	}

	public Vector<Excel> initExcel() throws Exception {

		Vector<Excel> objs = new Vector();

		int nameIdx = -1;
		int fnIdx = -1;
		int fnIdx1 = -1;
		int fnIdx2 = -1;
		int epsgIdx = -1;
		int codeIdx = -1;
		int deleteCodeIdx = -1;
		int fillColorIdx = -1;
		int fillWidthIdx = -1;
		int lineColorIdx = -1;
		int lineWidthIdx = -1;
		int codeFieldIdx = -1;
		int textFieldIdx = -1;
		int codeType = -1;
		int landCityNameIdx = -1;
		int fieldTypeIdx = -1;

		int drawOrderIdx = -1;

		int addFieldsIdx = -1;

		// int pkFieldIdx = -1;

		// XSSFSheet sheet = this.workbook.getSheetAt(0);
		XSSFSheet sheet = this.workbook.getSheet("SHP파일폴더분류표");

		if (sheet == null) {
			return null;
		}

		int xlsxRows = sheet.getPhysicalNumberOfRows();

		for (int rownum = 0; rownum < xlsxRows; rownum++) {
			XSSFRow xlsxRow = sheet.getRow(rownum); // 셀정보
			short maxCellNum = xlsxRow.getLastCellNum();
			Excel ec = new Excel();
			for (int cellIdx = 0; cellIdx < maxCellNum; cellIdx++) {

				XSSFCell cell = xlsxRow.getCell(cellIdx);

				// if(cell == null) {
				// continue;
				// }

				if (cell != null && rownum == 0) {
					String fdN = xlsxRow.getCell(cellIdx).toString();
					// if (fdN.equals("대분류 폴더명")) {
					// 저장되는 레이어 이름
					if (fdN.equals("중분류")) {
						fnIdx = cellIdx;
						// epsg:좌표계
					} else if (fdN.equals("좌표계")) {
						epsgIdx = cellIdx;
						// 코드필드에 존재하는 코드 값
					} else if (fdN.equals("코드")) {
						codeIdx = cellIdx;
						// 코드필드에 존재하는 코드 값중 삭제 대상의 경우 "삭제"
					} else if (fdN.equals("삭제대상 지역지구코드(중첩)")) {
						deleteCodeIdx = cellIdx;
						//
					} else if (fdN.equals("면색 RGB")) {
						fillColorIdx = cellIdx;
					} else if (fdN.equals("면선 굵기")) {
						fillWidthIdx = cellIdx;
					} else if (fdN.equals("선색 RGB")) {
						lineColorIdx = cellIdx;
					} else if (fdN.equals("선굵기")) {
						lineWidthIdx = cellIdx;
						// } else if (fdN.equals("서비스폴더명")) {
					} else if (fdN.equals("대분류")) {
						nameIdx = cellIdx;
					} else if (fdN.equals("코드필드")) {
						codeFieldIdx = cellIdx;
					} else if (fdN.equals("주기이름필드")) {
						textFieldIdx = cellIdx;
						// } else if (fdN.equals("중분류 폴더명")) {
					} else if (fdN.equals("소분류")) {
						fnIdx1 = cellIdx;
						// } else if (fdN.equals("소분류 폴더명")) {
					} else if (fdN.equals("주제도 폴더명")) {
						fnIdx2 = cellIdx;
						// uname 필드에 값으로 저장될 값
					} else if (fdN.equals("지역지구명")) {
						landCityNameIdx = cellIdx;
					} else if (fdN.equals("컬럼타입")) {
						fieldTypeIdx = cellIdx;
					} else if (fdN.equals("그리는순서필드")) {
						drawOrderIdx = cellIdx;
					} else if (fdN.equals("추가필드")) {
						addFieldsIdx = cellIdx;
					}
					// else if(fdN.equals("PK필드")) {
					// pkFieldIdx = cellIdx;
					// }

				} else {

					String value = "";

					if (cell != null) {
						value = cell.toString();
					}

					if (cellIdx == nameIdx) {
						ec.setName(value);
					} else if (cellIdx == fnIdx) {
						ec.setFoldName(value);
					} else if (cellIdx == epsgIdx) {
						ec.setEpsg(value);
					} else if (cellIdx == codeIdx) {
						ec.setCode(value);
					} else if (cellIdx == deleteCodeIdx) {
						ec.setDeleteCode(value);
					} else if (cellIdx == fillColorIdx) {
						ec.setFillColor(value);
					} else if (cellIdx == fillWidthIdx) {
						try {
							ec.setFillWidth(Float.valueOf(value));
						} catch (Exception e) {
							// System.out.println("fillWidthIdx="+value);
						}
					} else if (cellIdx == lineColorIdx) {
						ec.setLineColor(value);
					} else if (cellIdx == lineWidthIdx) {
						try {
							ec.setLineWidth(Float.valueOf(value));
						} catch (Exception e) {
							// System.out.println("lineWidthIdx="+value);
						}
					} else if (cellIdx == codeFieldIdx) {
						ec.setCodeField(value == null ? "" : value);
					} else if (cellIdx == textFieldIdx) {
						ec.setTextField(value == null ? "" : value);
					} else if (cellIdx == fnIdx1) {
						ec.setFoldName1(value);
					} else if (cellIdx == fnIdx2) {
						ec.setFoldName2(value);
					} else if (cellIdx == landCityNameIdx) {
						ec.setLandCityName(value);
					} else if (cellIdx == fieldTypeIdx) {
						ec.setFieldType(value);
					} else if (cellIdx == drawOrderIdx) {
						ec.setDrawOrder(value);
					} else if (cellIdx == addFieldsIdx) {
						ec.setAddFields(value);
					}
					// else if(cellIdx == pkFieldIdx) {
					// ec.setPkField(value == null ? "" : value);
					// }
				}

			}
			if (rownum == 0) {
				continue;
			}
			objs.add(ec);

		}
		// fis.close();

		String ck = "X";

		String pname = null;
		String pfdName = null;
		String pfdName1 = null;
		String pfdName2 = null;
		String pepsg = null;
		String pcodeField = null;
		String ptextField = null;
		String pFieldType = null;
		String drawOrder = null;
		String addFields = null;
		// String ppkField = null;
		for (int i = 0; i < objs.size(); i++) {
			Excel ec = objs.get(i);

			if (i == 0) {
				pname = ec.getName();

				pfdName = ec.getFoldName().equals(ck) ? "" : ec.getFoldName();
				ec.setFoldName(ec.getFoldName().equals(ck) ? "" : ec.getFoldName());

				drawOrder = ec.getDrawOrder().equals(ck) ? "" : ec.getDrawOrder();
				ec.setDrawOrder(ec.getDrawOrder().equals(ck) ? "" : ec.getDrawOrder());

				addFields = ec.getAddFields().equals(ck) ? "" : ec.getAddFields();
				ec.setAddFields(ec.getAddFields().equals(ck) ? "" : ec.getAddFields());

				// ppkField = ec.getPkField().equals(ck) ? "" : ec.getPkField();
				// ec.setPkField(ec.getPkField().equals(ck) ? "" : ec.getPkField());

				pfdName1 = ec.getFoldName1().equals(ck) ? "" : ec.getFoldName1();
				ec.setFoldName1(ec.getFoldName1().equals(ck) ? "" : ec.getFoldName1());

				pfdName2 = ec.getFoldName2().equals(ck) ? "" : ec.getFoldName2();
				ec.setFoldName2(ec.getFoldName2().equals(ck) ? "" : ec.getFoldName2());

				pepsg = ec.getEpsg();
				pcodeField = ec.getCodeField().equals(ck) ? "" : ec.getCodeField();
				ec.setCodeField(ec.getCodeField().equals(ck) ? "" : ec.getCodeField());

				ptextField = ec.getTextField().equals(ck) ? "" : ec.getTextField();
				ec.setTextField(ec.getTextField().equals(ck) ? "" : ec.getTextField());

				pFieldType = ec.getFieldType().equals(ck) ? "" : ec.getFieldType();
				ec.setFieldType(pFieldType);

				continue;
			}

			if (ec.getName().equals(ck)) {
				pname = "";
				ec.setName("");

			} else if (ec.getName().length() > 0) {
				pname = ec.getName();
			} else {
				ec.setName(pname);
			}

			if (ec.getFoldName().equals(ck)) {
				pfdName = "";
				ec.setFoldName("");
			} else if (ec.getFoldName().length() > 0) {
				pfdName = ec.getFoldName();
			} else {
				ec.setFoldName(pfdName);
			}

			if (ec.getDrawOrder().equals(ck)) {
				drawOrder = "";
				ec.setDrawOrder("");
			} else if (ec.getDrawOrder().length() > 0) {
				drawOrder = ec.getDrawOrder();
			} else {
				ec.setDrawOrder(drawOrder);
			}

			if (ec.getAddFields().equals(ck)) {
				addFields = "";
				ec.setAddFields("");
			} else if (ec.getAddFields().length() > 0) {
				addFields = ec.getAddFields();
			} else {
				ec.setAddFields(addFields);
			}

			if (ec.getFoldName1().equals(ck)) {
				pfdName1 = "";
				ec.setFoldName1("");
			} else if (ec.getFoldName1().length() > 0) {
				pfdName1 = ec.getFoldName1();
			} else {
				ec.setFoldName1(pfdName1);
			}

			if (ec.getFoldName2().equals(ck)) {
				pfdName2 = "";
				ec.setFoldName2("");
			} else if (ec.getFoldName2().length() > 0) {
				pfdName2 = ec.getFoldName2();
			} else {
				ec.setFoldName2(pfdName2);
			}

			if (ec.getEpsg().equals(ck)) {
				pepsg = "";
				ec.setEpsg("");
			} else if (ec.getEpsg().length() > 0) {
				pepsg = ec.getEpsg();
			} else {
				ec.setEpsg(pepsg);
			}

			if (ec.getCodeField().equals(ck)) {
				pcodeField = ec.getCodeField();
				pcodeField = "";
				ec.setCodeField("");
			} else if (ec.getCodeField().length() > 0) {
				pcodeField = ec.getCodeField();
			} else {
				ec.setCodeField(pcodeField);
			}

			if (ec.getTextField().equals(ck)) {
				ptextField = "";
				ec.setTextField("");
			} else if (ec.getTextField().length() > 0) {
				ptextField = ec.getTextField();
			} else {
				ec.setTextField(ptextField);
			}

			if (ec.getFieldType().equals(ck)) {
				pFieldType = "";
				ec.setFieldType("");
			} else if (ec.getFieldType().length() > 0) {
				pFieldType = ec.getFieldType();
			} else {
				ec.setFieldType(pFieldType);
			}

			// if (ec.getPkField().equals(ck)) {
			// ppkField = "";
			// ec.setPkField("");
			// } else if (ec.getPkField().length() > 0) {
			// ppkField = ec.getPkField();
			// } else {
			// ec.setPkField(ppkField);
			// }

			ec.setEpsg(ec.getEpsg().replaceAll(" ", ""));

			/*
			 * System.out.println("|Name=" + ec.getName() + "|FoldName=" + ec.getFoldName()
			 * + "|FoldName1=" + ec.getFoldName1() + "|FoldName2=" + ec.getFoldName2() +
			 * "|CodeField=" + ec.getCodeField() + "|TextField=" + ec.getTextField() +
			 * "|Epsg=" + ec.getEpsg() + "|Code=" + ec.getCode() + "|DeleteCode=" +
			 * ec.getDeleteCode() + "|FillColor=" + ec.getFillColor() + "|FillWidth=" +
			 * ec.getFillWidth() + "|LineColor=" + ec.getLineColor() + "|LineWidth=" +
			 * ec.getLineWidth() +"|fieldType="+ec.getFieldType() +
			 * "|addFields="+ec.getAddFields());
			 */
		}

		return objs;
	}

	public void setTile(XSSFSheet sheet, String sheetName) {

		// pointStyle name = layerName+"_"+shape+"_"+levelId
		// polygonStyle name = layerName+"_"+shape
		// polylineStyle name = layerName+"_"+shape

		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setPrettyPrinting()
				.registerTypeAdapter(XMLGregorianCalendar.class, new MetaInfo.Deserializer())
				.registerTypeAdapter(XMLGregorianCalendar.class, new MetaInfo.Serializer()).create();

		String json = gson.toJson(this.mainMapInfo);

		MapInfo copyMapInfo = gson.fromJson(json, MapInfo.class);

		copyMapInfo.setName(sheetName);

		List<LevelConfig> lcs = copyMapInfo.getLevelConfigs().getLevelConfig();

		int layerNameIdx = -1;
		int shapeIdx = -1;
		int slIdx = -1;
		int elIdx = -1;
		for (int i = 0; sheet.getRow(i) != null; i++) {
			Row row = sheet.getRow(i);
			if (i == 0) {
				String cellName = row.getCell(0).getStringCellValue();
				System.out.println("sheet name = " + sheetName + ", cellName=" + cellName);
				String subName = "mbrExtend:";
				try {
					if (cellName.indexOf("mbrExtend:") > -1) {
						String pixel = cellName.replaceAll(subName, "").trim();
						int px = Integer.parseInt(pixel);
						copyMapInfo.setMbrExtend(px);
					} else {
						copyMapInfo.setMbrExtend(200);
					}
				} catch (Exception e) {
					copyMapInfo.setMbrExtend(200);
					e.printStackTrace();
				}
				continue;
			}
			if (i == 1) {
				for (int j = 0; row.getCell(j) != null; j++) {
					String cellName = row.getCell(j).getStringCellValue();
					if (cellName.equals("중분류")) {
						layerNameIdx = j;
					}
					if (cellName.equals("Shape")) {
						shapeIdx = j;
					}
					if (cellName.equals("1")) {
						slIdx = j;
					}
					if (cellName.equals("14")) {
						elIdx = j;
					}
				}
			} else {
				String layerName = row.getCell(layerNameIdx).getStringCellValue();
				String shape = row.getCell(shapeIdx).getStringCellValue();
				// System.out.println("layerName=" + layerName + ", shape=" + shape);
				boolean[] onoff = new boolean[15];
				int idx = 1;
				int levelId = 1;
				for (int k = slIdx; k <= elIdx; k++) {
					String value = row.getCell(k).getStringCellValue();

					boolean deleteMode = false;
					if (value.length() > 0) {
						deleteMode = true;
					} else {
						deleteMode = false;
					}

					this.deleteLayer(lcs, levelId, layerName, shape, deleteMode, i);

					levelId++;
				}
			}
		}

		this.mapInfos.put(sheetName, copyMapInfo);
	}

	public void deleteLayer(List<LevelConfig> lcs, int level, String layerName, String shape, boolean draw,
			int drawIdx) {
		List<com.gis.protocol.LayerInfo> lcss = null;
		com.gis.protocol.LayerInfo deleteLi = null;
		for (LevelConfig lc : lcs) {
			if (lc.getLevelId() == level) {
				lcss = lc.getLayerInfo();
				for (com.gis.protocol.LayerInfo li : lcss) {

					String oriName = li.getLayerName();

					String[] temp = oriName.split(":");

					if (temp.length > 1) {
						oriName = temp[1].split("_")[0];
					}

					String styleName = li.getSelectStyleName();

					String[] values = styleName.split("\\|");

					if (values.length < 2) {
						System.out.println("delete");
						continue;
					}

					String oriShape = values[1];

					String oriLevel = lc.getLevelId() + "";

					// addLevelStyle()은 Polygon 텍스처(fillPattern) 스타일에 한해 서브쿼리 이름
					// ("styleSheet|Polygon|codeField|행번호", 4토큰) 뒤에 "|레벨"을 추가로 붙여 5토큰이 된다.
					// 이 경우에만 원래의 행 인덱스가 마지막이 아니라 뒤에서 두 번째 자리로 밀린다.
					// (Point 라벨 서브쿼리는 "styleSheet|Point|레벨|codeField|행번호" 형태로 원래부터 5토큰이고
					// 행 인덱스가 항상 마지막이므로 이 예외를 적용하면 안 된다.)
					boolean levelAppended = oriShape.equalsIgnoreCase("Polygon") && values.length >= 5;
					String idx = levelAppended ? values[values.length - 2] : values[values.length - 1];

					if (oriName.toLowerCase().equals(layerName.toLowerCase())
							&& oriShape.toLowerCase().equals(shape.toLowerCase()) && oriLevel.equals("" + level)
							&& !draw && idx.equals(drawIdx + "")) {
						deleteLi = li;
						break;
					}

				}

			}
		}
		if (lcss != null && deleteLi != null) {
			boolean ok = lcss.remove(deleteLi);
			// System.out.println("delete layer = " + layerName + ", shape=" + shape + ",
			// level=" + level);
		}
	}

	public void setMapInfoScale(ScaleInfos scaleInfos, MapInfo mapInfo) {
		mapInfo.setScaleInfos(scaleInfos);

		LevelConfigs lcss = new LevelConfigs();
		mapInfo.setLevelConfigs(lcss);

		List<LevelConfig> lcs = lcss.getLevelConfig();

		List<ScaleInfo> scs = scaleInfos.getScaleInfo();
		for (ScaleInfo sc : scs) {
			LevelConfig lc = new LevelConfig();
			lc.setLevelId(sc.getId());
			lcs.add(lc);
		}

	}

	public void addLevelLayer(int levelId, float drawOrder, String layerName, String styleName) {
		LevelConfigs lcss = this.mainMapInfo.getLevelConfigs();
		List<LevelConfig> lcs = lcss.getLevelConfig();

		LevelConfig lc = null;

		for (LevelConfig temp : lcs) {
			if (temp.getLevelId() == levelId) {
				lc = temp;
			}
		}

		List<com.gis.protocol.LayerInfo> lis = lc.getLayerInfo();

		com.gis.protocol.LayerInfo li = new com.gis.protocol.LayerInfo();
		li.setDrawOrder(drawOrder);
		li.setLayerName(layerName);
		li.setSelectStyleName(styleName);

		lis.add(li);

	}

	public void setStylesMapInfo(XSSFSheet sheet, boolean makeGridLayer) {

		String seper = "|";

		// MapInfo mapInfo = new MapInfo();

		// List<LevelConfig> lcs = mapInfo.getLevelConfigs().getLevelConfig();

		// Styles styles = new Styles();

		float drawOrder = 10000;

		List<PointStyle> points = styles.getPointStyle();
		List<PolygonStyle> polygons = styles.getPolygonStyle();
		List<PolylineStyle> polylines = styles.getPolylineStyle();

		// MapInfo mapInfo = new MapInfo();

		// String layerNamett = "레이어 파일 이름";
		String layerDesc = "중분류명";
		String layerNamett = "중분류";
		String Shape = "Shape";
		String sheetName = "sheetName";
		String codeField = "codeField";
		String codeValue = "codeValue";
		String trans = "trans";
		String fillColorRgb = "fillColorRgb";
		String fillColor = "fillColor";
		String fillPattern = "fillPattern";
		String lineColorRgb = "lineColorRgb";
		String lineColor = "lineColor";
		String lineWidth = "lineWidth";
		String fontFillColorRgb = "fontFillColorRgb";
		String fontFillColor = "fontFillColor";
		String fontLineColorRgb = "fontLineColorRgb";
		String fontLineColor = "fontLineColor";
		String fontLineWidth = "fontLineWidth";
		String fontName = "fontName";
		String extArea = "extArea";
		String textField = "textField";

		String linePatten = "linePatten";

		String textGrid = "textGrid";

		String drawOrderS = "drawOrder";

		String L1 = "1";
		String L2 = "2";
		String L3 = "3";
		String L4 = "4";
		String L5 = "5";
		String L6 = "6";
		String L7 = "7";
		String L8 = "8";
		String L9 = "9";
		String L10 = "10";
		String L11 = "11";
		String L12 = "12";
		String L13 = "13";
		String L14 = "14";
		String L15 = "15";

		int layerNameIdx = -1;
		int layerDescIdx = -1;
		int ShapeIdx = -1;
		int sheetNameIdx = -1;
		int codeFieldIdx = -1;
		int codeValueIdx = -1;
		int transIdx = -1;
		int fillColorRgbIdx = -1;
		int fillColorIdx = -1;
		int fillPatternIdx = -1;
		int lineColorRgbIdx = -1;
		int lineColorIdx = -1;
		int lineWidthIdx = -1;
		int fontFillColorRgbIdx = -1;
		int fontFillColorIdx = -1;
		int fontLineColorRgbIdx = -1;
		int fontLineColorIdx = -1;
		int fontLineWidthIdx = -1;
		int fontNameIdx = -1;
		int extAreaIdx = -1;
		int textFieldIdx = -1;
		int linePattenIdx = -1;
		int textGridIdx = -1;
		int drawOrderSIdx = -1;
		int L1Idx = -1;
		int L2Idx = -1;
		int L3Idx = -1;
		int L4Idx = -1;
		int L5Idx = -1;
		int L6Idx = -1;
		int L7Idx = -1;
		int L8Idx = -1;
		int L9Idx = -1;
		int L10Idx = -1;
		int L11Idx = -1;
		int L12Idx = -1;
		int L13Idx = -1;
		int L14Idx = -1;
		int L15Idx = -1;

		for (int i = 0; sheet.getRow(i) != null; i++) {
			Row row = sheet.getRow(i);

			if (i == 0) {
				continue;
			} else if (i == 1) {

				for (int j = 0; row.getCell(j) != null; j++) {
					Cell cell = row.getCell(j);
					String name = cell.getStringCellValue();
					// System.out.println(name);

					layerDescIdx = name.equals(layerDesc) ? j : layerDescIdx;
					layerNameIdx = name.equals(layerNamett) ? j : layerNameIdx;
					ShapeIdx = name.equals(Shape) ? j : ShapeIdx;
					sheetNameIdx = name.equals(sheetName) ? j : sheetNameIdx;
					codeFieldIdx = name.equals(codeField) ? j : codeFieldIdx;
					codeValueIdx = name.equals(codeValue) ? j : codeValueIdx;
					transIdx = name.equals(trans) ? j : transIdx;
					fillColorRgbIdx = name.equals(fillColorRgb) ? j : fillColorRgbIdx;
					fillColorIdx = name.equals(fillColor) ? j : fillColorIdx;
					lineColorRgbIdx = name.equals(lineColorRgb) ? j : lineColorRgbIdx;
					lineColorIdx = name.equals(lineColor) ? j : lineColorIdx;
					lineWidthIdx = name.equals(lineWidth) ? j : lineWidthIdx;
					fontFillColorRgbIdx = name.equals(fontFillColorRgb) ? j : fontFillColorRgbIdx;
					fontFillColorIdx = name.equals(fontFillColor) ? j : fontFillColorIdx;
					fontLineColorRgbIdx = name.equals(fontLineColorRgb) ? j : fontLineColorRgbIdx;
					fontLineColorIdx = name.equals(fontLineColor) ? j : fontLineColorIdx;
					fontLineWidthIdx = name.equals(fontLineWidth) ? j : fontLineWidthIdx;
					fontNameIdx = name.equals(fontName) ? j : fontNameIdx;
					textFieldIdx = name.equals(textField) ? j : textFieldIdx;
					extAreaIdx = name.equals(extArea) ? j : extAreaIdx;
					linePattenIdx = name.equals(linePatten) ? j : linePattenIdx;
					textGridIdx = name.equals(textGrid) ? j : textGridIdx;
					fillPatternIdx = name.equals(fillPattern) ? j : fillPatternIdx;

					drawOrderSIdx = name.equals(drawOrderS) ? j : drawOrderSIdx;

					L1Idx = name.equals(L1) ? j : L1Idx;
					L2Idx = name.equals(L2) ? j : L2Idx;
					L3Idx = name.equals(L3) ? j : L3Idx;
					L4Idx = name.equals(L4) ? j : L4Idx;
					L5Idx = name.equals(L5) ? j : L5Idx;
					L6Idx = name.equals(L6) ? j : L6Idx;
					L7Idx = name.equals(L7) ? j : L7Idx;
					L8Idx = name.equals(L8) ? j : L8Idx;
					L9Idx = name.equals(L9) ? j : L9Idx;
					L10Idx = name.equals(L10) ? j : L10Idx;
					L11Idx = name.equals(L11) ? j : L11Idx;
					L12Idx = name.equals(L12) ? j : L12Idx;
					L13Idx = name.equals(L13) ? j : L13Idx;
					L14Idx = name.equals(L14) ? j : L14Idx;
					L15Idx = name.equals(L15) ? j : L15Idx;

					L15Idx = L1Idx + this.sis.getScaleInfo().get(this.sis.getScaleInfo().size() - 1).getId() - 1;
				}
			} else {
				String layerDescN = row.getCell(layerDescIdx).getStringCellValue();
				String layerName = row.getCell(layerNameIdx).getStringCellValue();
				String shape = row.getCell(ShapeIdx).getStringCellValue();

				String drawOrderValue = row.getCell(drawOrderSIdx).getStringCellValue();

				// System.out.println("layerName = " + layerName+", shape = " + shape);
				//
				//
				// if(layerName.equals("PA")) {
				// int k=0;
				// }

				if (shape.toLowerCase().equals("polygon")) {

					Vector<Integer> levels = new Vector();
					int levelId = 1;
					for (int k = L1Idx; k <= L15Idx; k++) {
						Cell cell = row.getCell(k);
						if (cell != null) {
							String fontSize = cell.getStringCellValue() == null
									|| cell.getStringCellValue().length() == 0 ? "" : cell.getStringCellValue();
							if (fontSize.equals("" + 0)) {
								levels.add(levelId);
							}
						}
						levelId++;
					}

					String styleSheetName = row.getCell(sheetNameIdx) != null
							&& row.getCell(sheetNameIdx).getStringCellValue().length() > 0
									? row.getCell(sheetNameIdx).getStringCellValue()
									: "";

					PolygonStyle ps = null;

					boolean add = false;
					if (styleSheetName.length() == 0) {

						String fillC = row.getCell(fillColorRgbIdx) != null
								? row.getCell(fillColorRgbIdx).getStringCellValue()
								: "";

						String fillP = "";
						if (fillPatternIdx > -1) {
							fillP = row.getCell(fillPatternIdx) != null
									? row.getCell(fillPatternIdx).getStringCellValue()
									: "";
						}

						String lineC = row.getCell(lineColorRgbIdx) != null
								? row.getCell(lineColorRgbIdx).getStringCellValue()
								: "";
						float lineW = row.getCell(lineWidthIdx) != null
								&& row.getCell(lineWidthIdx).getStringCellValue().length() != 0
										? Float.parseFloat(row.getCell(lineWidthIdx).getStringCellValue())
										: 0;
						String lineP = row.getCell(linePattenIdx) != null
								? row.getCell(linePattenIdx).getStringCellValue()
								: "";
						int transC = row.getCell(transIdx) != null
								&& row.getCell(transIdx).getStringCellValue().length() != 0
										? Integer.parseInt(row.getCell(transIdx).getStringCellValue())
										: 0;

						String codeV = row.getCell(codeValueIdx).getStringCellValue();
						String codeF = row.getCell(codeFieldIdx).getStringCellValue();
						if (codeV.length() > 0 && codeF.length() > 0) {
							PolygonStyle query = this.getPolygonStyle(fillC, lineC, lineW, lineP, transC, fillP);
							query.setName(layerName + seper + shape + seper + codeV + seper + i);
							polygons.add(query);
							ps = new PolygonStyle();
							ps.setName(layerName + seper + shape + seper + i);
							List<com.gis.protocol.Query> qlist = ps.getQuery();
							com.gis.protocol.Query qy = new com.gis.protocol.Query();
							qy.setName(codeF);
							qy.setValue(codeV);
							qy.setStyleName(query.getName());
							qlist.add(qy);
						} else {
							ps = this.getPolygonStyle(fillC, lineC, lineW, lineP, transC, fillP);
							ps.setName(layerName + seper + shape + seper + i);

						}

						ps.setDrawOrder(drawOrderValue);
						polygons.add(ps);

					} else {
						String codeFieldName = row.getCell(codeFieldIdx).getStringCellValue();

						String subQueryKey = styleSheetName + seper + shape + seper + codeFieldName.toLowerCase()
								+ seper + i;

						if (!this.subQuerys.containsKey(subQueryKey)) {

							PolygonStyle queryP = this.readQueryStyle(styleSheetName, codeFieldName, polygons);

							queryP.setName(subQueryKey);

							this.subQuerys.put(subQueryKey, queryP);
						}

						ps = this.subQuerys.get(subQueryKey);

						ps.setDrawOrder(drawOrderValue);

						boolean isExeist = false;

						for (PolygonStyle pls : polygons) {
							if (pls.getName().equals(ps.getName())) {
								isExeist = true;
								break;
							}
						}

						if (!isExeist) {
							polygons.add(ps);
						}

						add = this.addLevelStyle(ps, levels, polygons);

					}

					levelId = 1;
					for (int k = L1Idx; k <= L15Idx; k++) {
						// for(int k = L1Idx; k <= L14Idx; k++) {
						Cell cell = row.getCell(k);
						if (cell != null) {
							String fontSize = cell.getStringCellValue() == null
									|| cell.getStringCellValue().length() == 0 ? "" : cell.getStringCellValue();
							if (fontSize.equals("" + 0)) {
								if (add == false) {
									this.addLevelLayer(levelId, drawOrder--, layerName, ps.getName());
								} else {
									this.addLevelLayer(levelId, drawOrder--, layerName, ps.getName() + "|" + levelId);
								}
							}
						}
						levelId++;
					}

				} else if (shape.toLowerCase().equals("polyline")) {
					String styleSheetName = row.getCell(sheetNameIdx) != null
							&& row.getCell(sheetNameIdx).getStringCellValue().length() > 0
									? row.getCell(sheetNameIdx).getStringCellValue()
									: "";

					PolylineStyle ps = null;
					if (styleSheetName.length() == 0) {
						String fillC = row.getCell(fillColorRgbIdx) != null
								? row.getCell(fillColorRgbIdx).getStringCellValue()
								: "";
						String lineC = row.getCell(lineColorRgbIdx) != null
								? row.getCell(lineColorRgbIdx).getStringCellValue()
								: "";
						float lineW = row.getCell(lineWidthIdx) != null
								&& row.getCell(lineWidthIdx).getStringCellValue().length() != 0
										? Float.parseFloat(row.getCell(lineWidthIdx).getStringCellValue())
										: 0;
						String lineP = row.getCell(linePattenIdx) != null
								? row.getCell(linePattenIdx).getStringCellValue()
								: "";
						int transC = row.getCell(transIdx) != null
								&& row.getCell(transIdx).getStringCellValue().length() != 0
										? Integer.parseInt(row.getCell(transIdx).getStringCellValue())
										: 0;

						String codeV = row.getCell(codeValueIdx).getStringCellValue();
						String codeF = row.getCell(codeFieldIdx).getStringCellValue();

						if (lineP.length() > 0) {
							ps = new PolylineStyle();
							List<com.gis.protocol.Query> listq = ps.getQuery();

							PolylineStyle ps1 = this.getPolylineStyle(lineC, lineW, "", transC);
							ps1.setName(layerName + seper + shape + seper + i + seper + "1");
							polylines.add(ps1);

							PolylineStyle ps2 = this.getPolylineStyle(fillC, lineW - 2, lineP, transC);
							ps2.setName(layerName + seper + shape + seper + i + seper + "2");
							polylines.add(ps2);

							com.gis.protocol.Query q1 = new com.gis.protocol.Query();
							q1.setPriority(1);
							q1.setStyleName(ps1.getName());

							com.gis.protocol.Query q2 = new com.gis.protocol.Query();
							q2.setPriority(2);
							q2.setStyleName(ps2.getName());

							listq.add(q1);
							listq.add(q2);
						} else {
							if (codeV.length() > 0 && codeF.length() > 0) {
								PolylineStyle query = this.getPolylineStyle(lineC, lineW, lineP, transC);
								query.setName(layerName + seper + shape + seper + codeV + seper + i);
								polylines.add(query);
								ps = new PolylineStyle();
								ps.setName(layerName + seper + shape + seper + i);
								List<com.gis.protocol.Query> qlist = ps.getQuery();
								com.gis.protocol.Query qy = new com.gis.protocol.Query();
								qy.setName(codeF);
								qy.setValue(codeV);
								qy.setStyleName(query.getName());
								qlist.add(qy);
							} else {
								ps = this.getPolylineStyle(lineC, lineW, lineP, transC);
							}
						}
						ps.setName(layerName + seper + shape + seper + i);
						ps.setDrawOrder(drawOrderValue);
						polylines.add(ps);
					} else {
						String codeFieldName = row.getCell(codeFieldIdx).getStringCellValue();

						String subQueryKey = styleSheetName + seper + shape + seper + codeFieldName.toLowerCase()
								+ seper + i;
						if (!this.subPolylineQuerys.containsKey(subQueryKey)) {
							PolylineStyle queryP = this.readQueryPolylineStyle(styleSheetName, codeFieldName,
									polylines);
							queryP.setName(subQueryKey);

							this.subPolylineQuerys.put(subQueryKey, queryP);
						}

						ps = this.subPolylineQuerys.get(subQueryKey);

						boolean isExeist = false;

						for (PolylineStyle pls : polylines) {
							if (pls.getName().equals(ps.getName())) {
								isExeist = true;
								break;
							}
						}
						ps.setDrawOrder(drawOrderValue);
						if (!isExeist) {
							polylines.add(ps);
						}

					}

					int levelId = 1;
					for (int k = L1Idx; k <= L15Idx; k++) {
						// for(int k = L1Idx; k <= L14Idx; k++) {
						Cell cell = row.getCell(k);
						if (cell != null) {
							String fontSize = cell.getStringCellValue() == null
									|| cell.getStringCellValue().length() == 0 ? "" : cell.getStringCellValue();
							if (fontSize.equals("" + 0)) {
								this.addLevelLayer(levelId, drawOrder--, layerName, ps.getName());
							}
						}
						levelId++;
					}

					/*
					 * String fillC = row.getCell(fillColorRgbIdx) != null ?
					 * row.getCell(fillColorRgbIdx).getStringCellValue() : ""; String lineC =
					 * row.getCell(lineColorRgbIdx) != null ?
					 * row.getCell(lineColorRgbIdx).getStringCellValue() : ""; float lineW =
					 * row.getCell(lineWidthIdx) != null &&
					 * row.getCell(lineWidthIdx).getStringCellValue().length() != 0 ?
					 * Float.parseFloat(row.getCell(lineWidthIdx).getStringCellValue()) : 0; String
					 * lineP = row.getCell(linePattenIdx) != null ?
					 * row.getCell(linePattenIdx).getStringCellValue() : ""; PolylineStyle ps =
					 * null; if(lineP.length() > 0) { ps = new PolylineStyle();
					 * List<com.gis.protocol.freegis3.Query> listq = ps.getQuery(); PolylineStyle
					 * ps1 = this.getPolylineStyle(lineC, lineW, "");
					 * ps1.setName(layerName+seper+shape+seper+i+seper+"1"); polylines.add(ps1);
					 * PolylineStyle ps2 = this.getPolylineStyle(fillC, lineW-2, lineP);
					 * ps2.setName(layerName+seper+shape+seper+i+seper+"2"); polylines.add(ps2);
					 * com.gis.protocol.freegis3.Query q1 = new com.gis.protocol.freegis3.Query();
					 * q1.setPriority(1); q1.setStyleName(ps1.getName());
					 * 
					 * com.gis.protocol.freegis3.Query q2 = new com.gis.protocol.freegis3.Query();
					 * q2.setPriority(2); q2.setStyleName(ps2.getName());
					 * 
					 * listq.add(q1); listq.add(q2); } else { ps = this.getPolylineStyle(lineC,
					 * lineW, lineP); }
					 * 
					 * ps.setName(layerName+seper+shape+seper+i); polylines.add(ps); int levelId =
					 * 1; for(int k = L1Idx; k <= L15Idx; k++) { //for(int k = L1Idx; k <= L14Idx;
					 * k++) { Cell cell = row.getCell(k); if(cell != null) { String fontSize =
					 * cell.getStringCellValue() == null || cell.getStringCellValue().length() == 0
					 * ? "" : cell.getStringCellValue(); if(fontSize.equals(""+0)) {
					 * this.addLevelLayer(levelId, drawOrder--, layerName, ps.getName()); } }
					 * levelId++; }
					 */
				} else if (shape.toLowerCase().equals("point")) {
					String styleSheetName = row.getCell(sheetNameIdx) != null
							&& row.getCell(sheetNameIdx).getStringCellValue().length() > 0
									? row.getCell(sheetNameIdx).getStringCellValue()
									: "";

					int levelId = 1;
					for (int k = L1Idx; k <= L15Idx; k++) {
						// for(int k = L1Idx; k <= L14Idx; k++) {
						Cell cell = row.getCell(k);
						if (cell != null) {

							int fontSize = Integer.parseInt(
									cell.getStringCellValue().length() == 0 ? "0" : cell.getStringCellValue());

							if (fontSize > 0) {
								PointStyle ps = null;

								if (styleSheetName.length() == 0) {
									String fontN = row.getCell(fontNameIdx).getStringCellValue();
									String fFillC = row.getCell(fontFillColorRgbIdx) != null
											? row.getCell(fontFillColorRgbIdx).getStringCellValue()
											: "";
									String fLineC = row.getCell(fontLineColorRgbIdx) != null
											? row.getCell(fontLineColorRgbIdx).getStringCellValue()
											: "";
									float fLineW = (row.getCell(fontLineWidthIdx) != null
											&& row.getCell(fontLineWidthIdx).getStringCellValue().length() > 0) ? Float
													.parseFloat(row.getCell(fontLineWidthIdx).getStringCellValue()) : 0;
									String fFieldN = row.getCell(textFieldIdx).getStringCellValue();
									int extAreaC = (row.getCell(extAreaIdx) != null
											&& row.getCell(extAreaIdx).getStringCellValue().length() > 0)
													? Integer.parseInt(row.getCell(extAreaIdx).getStringCellValue())
													: 0;
									int textGridC = 0;
									if (textGridIdx > -1) {
										textGridC = (row.getCell(textGridIdx) != null
												&& row.getCell(textGridIdx).getStringCellValue().length() > 0)
														? Integer
																.parseInt(row.getCell(textGridIdx).getStringCellValue())
														: 0;
									}

									String codeV = row.getCell(codeValueIdx).getStringCellValue();
									String codeF = row.getCell(codeFieldIdx).getStringCellValue();
									if (codeV.length() > 0 && codeF.length() > 0) {
										PointStyle query = this.getPointStyle(fontSize, fontN, fFillC, fLineC, fLineW,
												fFieldN, extAreaC, textGridC);
										query.setName(layerName + seper + shape + seper + codeV + seper + i);
										points.add(query);
										ps = new PointStyle();
										ps.setName(layerName + seper + shape + seper + i);

										List<com.gis.protocol.Query> qlist = ps.getQuery();
										com.gis.protocol.Query qy = new com.gis.protocol.Query();
										qy.setName(codeF);
										qy.setValue(codeV);
										qy.setStyleName(query.getName());
										qlist.add(qy);

									} else {
										ps = this.getPointStyle(fontSize, fontN, fFillC, fLineC, fLineW, fFieldN,
												extAreaC, textGridC);
										ps.setName(layerName + seper + shape + seper + levelId + seper + i);
									}

									ps.setDrawOrder(drawOrderValue);

									points.add(ps);

									// this.addLevelLayer(levelId, drawOrder--, layerName, ps.getName());
								} else {

									String codeFieldName = row.getCell(codeFieldIdx).getStringCellValue();

									int extAreaC = (row.getCell(extAreaIdx) != null
											&& row.getCell(extAreaIdx).getStringCellValue().length() > 0)
													? Integer.parseInt(row.getCell(extAreaIdx).getStringCellValue())
													: 0;

									int textGridC = 0;
									if (textGridIdx > -1) {
										textGridC = (row.getCell(textGridIdx) != null
												&& row.getCell(textGridIdx).getStringCellValue().length() > 0)
														? Integer
																.parseInt(row.getCell(textGridIdx).getStringCellValue())
														: 0;
									}

									String subQueryKey = styleSheetName + seper + shape + seper + levelId + seper
											+ codeFieldName.toLowerCase() + seper + i;
									if (!this.subPointQuerys.containsKey(subQueryKey)) {

										PointStyle queryP = this.readQueryStyle(styleSheetName, codeFieldName, points,
												fontSize, extAreaC, textGridC);
										queryP.setName(subQueryKey);
										queryP.setGrid(textGridC);
										this.subPointQuerys.put(subQueryKey, queryP);
									}

									ps = this.subPointQuerys.get(subQueryKey);

									boolean isExeist = false;

									for (PointStyle pls : points) {
										if (pls.getName().equals(ps.getName())) {
											isExeist = true;
											break;
										}
									}

									if (!isExeist) {
										ps.setDrawOrder(drawOrderValue);
										points.add(ps);
									}

								}

								this.addLevelLayer(levelId, drawOrder--, layerName, ps.getName());
							}

						}
						levelId++;
					}

				} else if (shape.toLowerCase().equals("tile")) {
					int levelId = 1;
					for (int k = L1Idx; k <= L15Idx; k++) {
						// for(int k = L1Idx; k <= L14Idx; k++) {
						Cell cell = row.getCell(k);
						if (cell != null) {
							String fontSize = cell.getStringCellValue() == null
									|| cell.getStringCellValue().length() == 0 ? "" : cell.getStringCellValue();
							if (fontSize.equals("" + 0)) {
								this.addLevelLayer(levelId, drawOrder--, layerName,
										layerName + seper + "TILE" + seper + i);
							}
						}
						levelId++;
					}
				} else if (shape.toLowerCase().equals("raster")) {
					int levelId = 1;
					for (int k = L1Idx; k <= L15Idx; k++) {
						// for(int k = L1Idx; k <= L14Idx; k++) {
						Cell cell = row.getCell(k);
						if (cell != null) {
							String fontSize = cell.getStringCellValue() == null
									|| cell.getStringCellValue().length() == 0 ? "" : cell.getStringCellValue();
							if (fontSize.equals("" + 0)) {
								this.addLevelLayer(levelId, drawOrder--, layerName,
										layerName + seper + "RASTER" + seper + i);
							}
						}
						levelId++;
					}
				}

			}
		}

		this.styles = styles;
		// if(makeGridLayer) {
		makeGridLayer(makeGridLayer);
		// }
	}

	public FillStyle getFillStyle(String color) {
		FillStyle fill = new FillStyle();
		fill.setColor(color);
		return fill;
	}

	public LineStyle getLineStyle(String color, float lineWidth, String patten) {
		LineStyle line = new LineStyle();
		line.setColor(color);
		line.setWidth(lineWidth);
		if (patten != null && patten.length() > 0) {
			line.setPattern(patten);
			// line.setPattern(LinePatten.);
		}
		return line;
	}

	public PointStyle getPointStyle(int fontSize, String fontName, String fillColor, String lineColor, float lineWidth,
			String fieldName, int extArea, int grid) {
		PointStyle pointS = new PointStyle();

		// OverlapConfig oc = pointS.getOverlapConfig();

		OverlapConfig oc = new OverlapConfig();

		if (extArea > 0) {
			oc.setExtensionArea(extArea);
			oc.setDeleteOverlapMode(DeleteOverlapMode.ONLY_STYLE);
		} else {
			oc.setExtensionArea(5);
			oc.setDeleteOverlapMode(DeleteOverlapMode.ALL_L_STYLE);
		}

		pointS.setOverlapConfig(oc);

		TextStyle ts = new TextStyle();
		ts.setFontSize(fontSize);
		ts.setFontName(fontName);
		ts.setFontType("BOLD");

		MultiLine ml = new MultiLine();
		ml.setLineMaxSize(100);

		ts.setMultiLine(ml);
		FillStyle fill = this.getFillStyle(fillColor);

		LineStyle line = this.getLineStyle(lineColor, lineWidth, null);

		ts.setLineStyle(line);
		ts.setFillStyle(fill);

		ts.setFieldName(fieldName.toLowerCase());
		pointS.setPositionType("xxc");

		pointS.setTextStyle(ts);

		pointS.setGrid(grid);

		return pointS;
	}

	public PolylineStyle getPolylineStyle(String lineColor, float lineWidth, String linePatten, int trans) {
		PolylineStyle ps = new PolylineStyle();

		LineStyle line = this.getLineStyle(lineColor, lineWidth, linePatten);
		line.setTransparency(trans);
		ps.setLineStyle(line);
		return ps;
	}

	public PolygonStyle getPolygonStyle(String fillColor, String lineColor, float lineWidth, String linePatten,
			int trans, String fillPattern) {
		PolygonStyle ps = new PolygonStyle();
		FillStyle fill = this.getFillStyle(fillColor);
		fill.setTexturePattern(fillPattern);
		fill.setTransparency(trans);
		LineStyle line = this.getLineStyle(lineColor, lineWidth, linePatten);
		line.setTransparency(trans);
		ps.setLineStyle(line);
		ps.setFillStyle(fill);

		return ps;
	}

	public void setScaleInfos(XSSFSheet sheet) {
		ScaleInfos sis = new ScaleInfos();
		List<ScaleInfo> sislist = sis.getScaleInfo();
		for (int i = 0; sheet.getRow(i) != null; i++) {
			if (i == 0) {
				continue;
			}
			Row row = sheet.getRow(i);
			Cell levelId = row.getCell(0);

			if (levelId == null) {
				break;
			}

			Cell scale = row.getCell(1);
			ScaleInfo si = new ScaleInfo();
			si.setId(new Integer(levelId.getStringCellValue()));
			si.setPixelPerMeter(Double.parseDouble(scale.getStringCellValue()));
			sislist.add(si);
		}

		this.sis = sis;
	}

	// public PointStyle readQueryStyle(String sheetName, String codeField,
	// List<PointStyle> list, int fontSize, int extAreaC) {

	public PointStyle readQueryStyle(String sheetName, String codeField, List<PointStyle> list, int fontSize,
			int extAreaC, int grid) {

		int drawOrder = 10000;
		XSSFSheet sheet = this.workbook.getSheet(sheetName);

		this.setType(sheet);

		PointStyle pointS = new PointStyle();

		OverlapConfig oc = new OverlapConfig();

		if (extAreaC > 0) {
			oc.setExtensionArea(extAreaC);
			oc.setDeleteOverlapMode(DeleteOverlapMode.ONLY_STYLE);
		} else {
			oc.setExtensionArea(5);
			oc.setDeleteOverlapMode(DeleteOverlapMode.ALL_L_STYLE);
		}

		List<com.gis.protocol.Query> querys = pointS.getQuery();

		for (int i = 0; sheet.getRow(i) != null; i++) {
			if (i == 0) {

			} else {
				Row row = sheet.getRow(i);

				String code = row.getCell(0).getStringCellValue();
				String fFillC = row.getCell(1) != null ? row.getCell(1).getStringCellValue() : "";
				String fLineC = row.getCell(3) != null ? row.getCell(3).getStringCellValue() : "";

				String[] lcA = fFillC.split(",");

				if (fFillC.length() > 0 && lcA.length != 3) {
					System.out.println(
							"error : sheetName" + sheetName + ", fillColor i=" + (i + 1) + ", fillColor=" + fFillC);
				}

				lcA = fLineC.split(",");

				if (fLineC.length() > 0 && lcA.length != 3) {
					System.out.println(
							"error : sheetName" + sheetName + ", lineColor i=" + (i + 1) + ", lineColor=" + fFillC);
				}

				float fLineW = (row.getCell(5) != null && row.getCell(5).getStringCellValue().length() > 0)
						? Float.parseFloat(row.getCell(5).getStringCellValue())
						: 0;
				String fontN = row.getCell(6).getStringCellValue();

				// int extAreaC = (row.getCell(7) != null &&
				// row.getCell(7).getStringCellValue().length() > 0)
				// ? Integer.parseInt(row.getCell(7).getStringCellValue()) : 0;

				String fFieldN = row.getCell(8).getStringCellValue();

				// int grid = (row.getCell(9) != null &&
				// row.getCell(9).getStringCellValue().length() > 0)
				// ? Integer.parseInt(row.getCell(9).getStringCellValue())
				// : 0;

				PointStyle ps = this.getPointStyle(fontSize, fontN, fFillC, fLineC, fLineW, fFieldN, extAreaC, grid);

				ps.setOverlapConfig(oc);

				ps.setName(sheetName + "_" + code + "_" + fontSize + "_point");

				list.add(ps);

				com.gis.protocol.Query query = new com.gis.protocol.Query();
				query.setStyleName(sheetName + "_" + code + "_" + fontSize + "_point");
				query.setName(codeField.toLowerCase());
				query.setValue(code);
				query.setPriority(drawOrder--);
				querys.add(query);
			}
		}
		return pointS;
	}

	public PolygonStyle readQueryStyle(String sheetName, String codeField, List<PolygonStyle> list) {
		// System.out.println("sheetName="+sheetName);
		int drawOrder = 10000;
		XSSFSheet sheet = this.workbook.getSheet(sheetName);

		this.setType(sheet);

		PolygonStyle polygonS = new PolygonStyle();

		List<com.gis.protocol.Query> querys = polygonS.getQuery();

		int codeIdx = -1;
		int fillColorRgbIdx = -1;
		int fillColorIdx = -1;
		int fillPatternIdx = -1;
		int lineColorRgbIdx = -1;
		int lineColorIdx = -1;
		int lineWidthIdx = -1;
		int linePattenIdx = -1;
		int transIdx = -1;

		for (int i = 0; sheet.getRow(i) != null; i++) {
			if (i == 0) {
				Row row = sheet.getRow(i);

				for (int k = 0; k < 9; k++) {
					String name = row.getCell(k).getStringCellValue();
					if (name.equals("code")) {
						codeIdx = k;
					} else if (name.equals("fillColorRgb")) {
						fillColorRgbIdx = k;
					} else if (name.equals("fillColor")) {
						fillColorIdx = k;
					} else if (name.equals("fillPattern")) {
						fillPatternIdx = k;
					} else if (name.equals("lineColorRgb")) {
						lineColorRgbIdx = k;
					} else if (name.equals("lineColor")) {
						lineColorIdx = k;
					} else if (name.equals("lineWidth")) {
						lineWidthIdx = k;
					} else if (name.equals("linePatten")) {
						linePattenIdx = k;
					} else if (name.equals("trans")) {
						transIdx = k;
					}
				}

			} else {
				Row row = sheet.getRow(i);

				Cell codeCell = row.getCell(codeIdx);
				if (codeCell == null) {
					continue;
				}

				String code = row.getCell(codeIdx).getStringCellValue();

				row.getCell(fillColorRgbIdx).setCellType(CellType.STRING);
				String fillColor = row.getCell(fillColorRgbIdx).getStringCellValue();

				String[] lcA = fillColor.split(",");

				if (fillColor.length() > 0 && lcA.length != 3) {
					System.out.println(
							"error : sheetName" + sheetName + ", fillColor i=" + (i + 1) + ", fillColor=" + fillColor);
				}

				row.getCell(lineColorRgbIdx).setCellType(CellType.STRING);
				String lineColor = row.getCell(lineColorRgbIdx).getStringCellValue();

				lcA = lineColor.split(",");

				if (lineColor.length() > 0 && lcA.length != 3) {
					System.out.println(
							"error : sheetName" + sheetName + ", lineColor i=" + (i + 1) + ", lineColor=" + lineColor);
				}
				row.getCell(transIdx).setCellType(CellType.STRING);
				int transC = row.getCell(transIdx) != null && row.getCell(transIdx).getStringCellValue().length() != 0
						? Integer.parseInt(row.getCell(transIdx).getStringCellValue())
						: 0;

				row.getCell(lineWidthIdx).setCellType(CellType.STRING);
				float lineWidth = (row.getCell(lineWidthIdx) != null
						&& row.getCell(lineWidthIdx).getStringCellValue().length() > 0)
								? Float.parseFloat(row.getCell(lineWidthIdx).getStringCellValue())
								: 0;

				String lineP = row.getCell(linePattenIdx) != null ? row.getCell(linePattenIdx).getStringCellValue()
						: null;

				String fillP = "";
				if (fillPatternIdx > -1) {
					fillP = row.getCell(fillPatternIdx) != null ? row.getCell(fillPatternIdx).getStringCellValue() : "";
				}

				PolygonStyle ps = this.getPolygonStyle(fillColor, lineColor, lineWidth, lineP, transC, fillP);
				ps.setName(sheetName + "_" + code + "_polygon");

				boolean add = true;
				for (PolygonStyle pp : list) {
					if (pp.getName().equals(ps.getName())) {
						add = false;
					}
				}
				if (add) {
					list.add(ps);
				}

				// list.add(ps);

				com.gis.protocol.Query query = new com.gis.protocol.Query();
				query.setStyleName(sheetName + "_" + code + "_polygon");
				query.setName(codeField.toLowerCase());
				query.setValue(code);
				query.setPriority(drawOrder--);
				querys.add(query);

			}
		}

		return polygonS;
	}

	public PolylineStyle readQueryPolylineStyle(String sheetName, String codeField, List<PolylineStyle> list) {
		int drawOrder = 10000;
		XSSFSheet sheet = this.workbook.getSheet(sheetName);

		this.setType(sheet);

		PolylineStyle polylineS = new PolylineStyle();

		List<com.gis.protocol.Query> querys = polylineS.getQuery();

		int codeIdx = -1;
		int fillColorRgbIdx = -1;
		int fillColorIdx = -1;
		int fillPatternIdx = -1;
		int lineColorRgbIdx = -1;
		int lineColorIdx = -1;
		int lineWidthIdx = -1;
		int linePattenIdx = -1;
		int transIdx = -1;

		for (int i = 0; sheet.getRow(i) != null; i++) {
			if (i == 0) {
				Row row = sheet.getRow(i);

				for (int k = 0; k < 9; k++) {
					String name = row.getCell(k).getStringCellValue();
					if (name.equals("code")) {
						codeIdx = k;
					} else if (name.equals("fillColorRgb")) {
						fillColorRgbIdx = k;
					} else if (name.equals("fillColor")) {
						fillColorIdx = k;
					} else if (name.equals("fillPattern")) {
						fillPatternIdx = k;
					} else if (name.equals("lineColorRgb")) {
						lineColorRgbIdx = k;
					} else if (name.equals("lineColor")) {
						lineColorIdx = k;
					} else if (name.equals("lineWidth")) {
						lineWidthIdx = k;
					} else if (name.equals("linePatten")) {
						linePattenIdx = k;
					} else if (name.equals("trans")) {
						transIdx = k;
					}
				}

			} else {
				Row row = sheet.getRow(i);
				String code = row.getCell(codeIdx).getStringCellValue();

				// row.getCell(1).setCellType( CellType.STRING);
				// String fillColor = row.getCell(1).getStringCellValue();
				//
				// String[] lcA = fillColor.split(",");
				//
				// if(fillColor.length() > 0 && lcA.length != 3) {
				// System.out.println("error : sheetName"+sheetName+", fillColor i="+(i+1)+",
				// fillColor="+fillColor);
				// }

				row.getCell(lineColorRgbIdx).setCellType(CellType.STRING);
				String lineColor = row.getCell(lineColorRgbIdx).getStringCellValue();

				String[] lcA = lineColor.split(",");

				if (lineColor.length() > 0 && lcA.length != 3) {
					System.out.println(
							"error : sheetName" + sheetName + ", lineColor i=" + (i + 1) + ", lineColor=" + lineColor);
				}

				row.getCell(transIdx).setCellType(CellType.STRING);
				int transC = row.getCell(transIdx) != null && row.getCell(transIdx).getStringCellValue().length() != 0
						? Integer.parseInt(row.getCell(transIdx).getStringCellValue())
						: 0;

				row.getCell(lineWidthIdx).setCellType(CellType.STRING);
				float lineWidth = (row.getCell(lineWidthIdx) != null
						&& row.getCell(lineWidthIdx).getStringCellValue().length() > 0)
								? Float.parseFloat(row.getCell(lineWidthIdx).getStringCellValue())
								: 0;

				String lineP = row.getCell(linePattenIdx) != null ? row.getCell(linePattenIdx).getStringCellValue()
						: null;

				PolylineStyle ps = this.getPolylineStyle(lineColor, lineWidth, lineP, transC);

				ps.setName(sheetName + "_" + code + "_polyline");

				list.add(ps);

				com.gis.protocol.Query query = new com.gis.protocol.Query();
				query.setStyleName(sheetName + "_" + code + "_polyline");
				query.setName(codeField.toLowerCase());
				query.setValue(code);
				query.setPriority(drawOrder--);
				querys.add(query);

			}
		}

		return polylineS;
	}

	public Geometry getSimplePolygonm(Geometry _geo, int distance) {

		if (_geo.getGeometryType().equals("Polygon")) {
			return getSimplePolygon((Polygon) _geo, distance);
		} else if (_geo.getGeometryType().equals("MultiPolygon")) {
			Vector<Polygon> newps = new Vector();
			for (int i = 0; i < _geo.getNumGeometries(); i++) {
				Geometry subgeo = _geo.getGeometryN(i);
				Geometry conv = getSimplePolygon((Polygon) subgeo, distance);
				if (conv != null) {
					newps.add((Polygon) conv);
				}
			}

			Polygon[] pls = new Polygon[newps.size()];
			for (int i = 0; i < newps.size(); i++) {
				pls[i] = newps.get(i);
			}
			return gft.createMultiPolygon(pls);
		}

		return null;
	}

	// 36000
	// 1332994
	public Geometry getSimplePolygon(Polygon _geo, int distance) {

		Vector<LineString> interV = new Vector();
		Polygon geo = _geo;
		LineString rl = geo.getExteriorRing();

		double minx = geo.getEnvelopeInternal().getMinX();

		double rlArea = Math.abs(CGAlgorithms.signedArea(rl.getCoordinates()));

		// if( geo.intersects(this.dogdo1) || geo.intersects(dogdo2)) {
		if (minx > 1332994 && rlArea > 36000) {
			// System.out.println();
			// geo.intersection(other)
		} else {
			if ((distance * distance) > rlArea) {
				return null;
			}

		}

		int interCnt = geo.getNumInteriorRing();
		for (int i = 0; i < interCnt; i++) {
			LineString inter = geo.getInteriorRingN(i);
			double itArea = Math.abs(CGAlgorithms.signedArea(inter.getCoordinates()));

			// if( inter.intersects(this.dogdo1) || inter.intersects(dogdo2)) {
			if (minx > 1332994 && rlArea > 36000) {
				interV.add(inter);
			} else {
				if ((distance * distance) < itArea) {
					interV.add(inter);
				}
			}
		}
		LinearRing[] lrs = new LinearRing[interV.size()];
		for (int i = 0; i < lrs.length; i++) {
			lrs[i] = (LinearRing) interV.get(i);
		}
		return gft.createPolygon((LinearRing) rl, lrs);
	}

	public boolean isTexture(PolygonStyle ps, List<PolygonStyle> pls) {
		List<com.gis.protocol.Query> querys = ps.getQuery();
		for (com.gis.protocol.Query qr : querys) {

			PolygonStyle selectSub = null;
			// 패턴 인지 화인
			for (PolygonStyle pl : pls) {
				if (pl.getName().equals(qr.getStyleName())) {
					if (pl.getFillStyle() != null) {
						String imgName = pl.getFillStyle().getTexturePattern();
						if (imgName != null && imgName.length() > 3 && imgName.indexOf("_") == -1) {
							return true;
						}
					}
				}
			}
		}
		return false;

	}

	public PolygonStyle getClone(PolygonStyle ops) {
		PolygonStyle ps = new PolygonStyle();

		ps.setName(ops.getName());
		List<com.gis.protocol.Query> oquers = ps.getQuery();

		List<com.gis.protocol.Query> quers = ops.getQuery();
		for (com.gis.protocol.Query qy : quers) {
			com.gis.protocol.Query newQ = new com.gis.protocol.Query();
			newQ.setName(qy.getName());
			newQ.setOverlapConfig(qy.getOverlapConfig());
			newQ.setPriority(qy.getPriority());
			newQ.setStyleName(qy.getStyleName());
			newQ.setValue(qy.getValue());
			oquers.add(newQ);
		}

		return ps;
	}

	public boolean addLevelStyle(PolygonStyle ops, Vector<Integer> levels, List<PolygonStyle> pls) {

		if (this.isTexture(ops, pls) == false) {
			return false;
		}

		for (Integer level : levels) {

			// PolygonStyle ps = ops;//clone
			PolygonStyle ps = this.getClone(ops);

			PolygonStyle pps = null;
			for (PolygonStyle pl : pls) {
				if (pl.getName().equals(ps.getName())) {
					pps = pl;
				}
			}
			// pls.remove(pps);

			ps.setName(ps.getName() + "|" + level);

			List<com.gis.protocol.Query> querys = ps.getQuery();
			for (com.gis.protocol.Query qr : querys) {

				PolygonStyle selectSub = null;
				// 패턴 인지 화인
				for (PolygonStyle pl : pls) {
					if (pl.getName().equals(qr.getStyleName())) {
						if (pl.getFillStyle() != null) {
							String imgName = pl.getFillStyle().getTexturePattern();
							if (imgName != null && imgName.length() > 3 && imgName.indexOf("_") == -1) {
								selectSub = pl;
							}
						}
					}
				}
				if (selectSub != null) {
					// pls.remove(selectSub);

					String[] names = selectSub.getName().split("_");

					String path = "";

					for (int i = 0; i < names.length; i++) {
						if (i == 0 || i == 1) {
							path += ("/" + names[i]);
						} else if (i == (names.length - 1)) {

						} else {
							path += ("_" + names[i]);
						}
					}

					String imgName = selectSub.getFillStyle().getTexturePattern();
					int idx = imgName.indexOf(".");

					String newImgName = imgName.substring(0, idx) + "_" + level
							+ imgName.substring(idx, imgName.length());

					PolygonStyle newS = this.getPolygonStyle(selectSub.getFillStyle().getColor(),
							selectSub.getLineStyle().getColor(), selectSub.getLineStyle().getWidth(),
							selectSub.getLineStyle().getPattern(), selectSub.getFillStyle().getTransparency(),
							// "/"+names[0] + "/" + names[1] + "/" + newImgName);
							path + "/" + newImgName);

					newS.setName(selectSub.getName() + "-" + level);

					boolean add = true;
					for (PolygonStyle pl : pls) {
						if (pl.getName().equals(newS.getName())) {
							add = false;
							break;
						}
					}
					if (add) {
						pls.add(newS);
					}
					qr.setStyleName(newS.getName());
				}
			}

			boolean add = true;
			for (PolygonStyle pl : pls) {
				if (pl.getName().equals(ps.getName())) {
					add = false;
					break;
				}
			}
			if (add) {
				pls.add(ps);
			}
		}
		return true;
	}

	public MultiPolygon getValidatePgM(MultiPolygon pg) {

		Polygon[] mp = new Polygon[pg.getNumGeometries()];

		for (int i = 0; i < pg.getNumGeometries(); i++) {
			mp[i] = this.getValidatePg(((Polygon) (pg.getGeometryN(i))));
		}

		return gft.createMultiPolygon(mp);
	}

	public Polygon getValidatePg(Polygon pg) {

		LinearRing exls = (LinearRing) pg.getExteriorRing();

		// exls.getCoordinates();

		Polygon outP = gft.createPolygon(exls.getCoordinates());

		Vector<LinearRing> ir = new Vector();

		int cnt = pg.getNumInteriorRing();

		for (int i = 0; i < cnt; i++) {

			LinearRing ls = (LinearRing) pg.getInteriorRingN(i);

			Coordinate[] holscds = ls.getCoordinates();

			boolean isHole = true;

			for (Coordinate hls : holscds) {
				Point pt = this.gft.createPoint(hls);
				if (outP.contains(pt)) {

				} else {
					isHole = false;
					break;
				}
			}

			if (isHole) {
				ir.add(ls);
			}

		}

		System.out.println("-----------------------interior cnt = " + cnt + ", output = " + ir.size());
		LinearRing[] newlr = new LinearRing[ir.size()];

		for (int k = 0; k < ir.size(); k++) {
			newlr[k] = ir.get(k);
		}

		return gft.createPolygon(exls, newlr);
	}

	public double getErrorValue(ScaleInfos sis, int levelId) {
		double value = -1;

		List<ScaleInfo> sil = sis.getScaleInfo();

		for (ScaleInfo si : sil) {
			if (si.getId() == levelId) {
				return si.getPixelPerMeter();
			}
		}
		return value;
	}

	public Object getStyle(String styleName, com.gis.protocol.freegis3.Styles styles) {

		List<com.gis.protocol.freegis3.PointStyle> points = styles.getPointStyle();
		List<com.gis.protocol.freegis3.PolylineStyle> lines = styles.getPolylineStyle();
		List<com.gis.protocol.freegis3.PolygonStyle> fills = styles.getPolygonStyle();

		for (com.gis.protocol.freegis3.PointStyle point : points) {
			if (point.getName().equals(styleName)) {
				return point;
			}
		}
		for (com.gis.protocol.freegis3.PolylineStyle point : lines) {
			if (point.getName().equals(styleName)) {
				return point;
			}
		}
		for (com.gis.protocol.freegis3.PolygonStyle point : fills) {
			if (point.getName().equals(styleName)) {
				return point;
			}
		}
		return null;
	}

	class LevelComparator implements Comparator<LevelConfig> {

		public int compare(LevelConfig one, LevelConfig two) {

			int oneName = one.getLevelId();
			int twoName = two.getLevelId();

			return oneName < twoName ? 1 : -1;

		}
	}

	class JobInfo {
		File saveLayerFile;
		String mapInfoName;
		boolean isRaster;
		double simpleValue;
		double gridValue;
		Boolean lineText;
		String fileExtension;
		String layerName;
	}

}
