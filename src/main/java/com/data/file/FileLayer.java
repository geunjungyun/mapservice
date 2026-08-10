package com.data.file;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;
import java.util.concurrent.ConcurrentMap;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

//import org.apache.lucene.analysis.Analyzer;
//
//import org.apache.lucene.analysis.standard.StandardAnalyzer;
//import org.apache.lucene.document.Document;
//import org.apache.lucene.document.LatLonDocValuesField;
//import org.apache.lucene.document.StringField;
//import org.apache.lucene.index.DirectoryReader;
//import org.apache.lucene.index.IndexReader;
//import org.apache.lucene.index.IndexWriter;
//import org.apache.lucene.index.IndexWriterConfig;
//import org.apache.lucene.index.IndexWriterConfig.OpenMode;
//import org.apache.lucene.index.Term;
//import org.apache.lucene.queries.BoostingQuery;
//import org.apache.lucene.queries.CustomScoreQuery;
//import org.apache.lucene.queries.function.ValueSource;
//import org.apache.lucene.queries.function.valuesource.FloatFieldSource;
//import org.apache.lucene.queryparser.classic.QueryParser;
//import org.apache.lucene.search.BooleanClause;
//import org.apache.lucene.search.BooleanQuery;
//import org.apache.lucene.search.ConstantScoreQuery;
//import org.apache.lucene.search.Explanation;
//import org.apache.lucene.search.IndexSearcher;

//import org.apache.lucene.search.Query;

//import org.apache.lucene.search.ScoreDoc;
//import org.apache.lucene.search.Sort;
//import org.apache.lucene.search.SortField;
//import org.apache.lucene.search.TermQuery;
//import org.apache.lucene.search.TopDocs;
//import org.apache.lucene.search.TopDocsCollector;
//import org.apache.lucene.search.TopFieldCollector;
//import org.apache.lucene.search.TopScoreDocCollector;
//import org.apache.lucene.search.TotalHitCountCollector;
//import org.apache.lucene.search.WildcardQuery;
//import org.apache.lucene.spatial.query.SpatialArgs;
//import org.apache.lucene.spatial.query.SpatialOperation;
//import org.apache.lucene.store.Directory;
//import org.apache.lucene.store.FSDirectory;
//import org.apache.lucene.util.Version;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureTypes;
import org.geotools.feature.collection.SimpleFeatureIteratorImpl;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeImpl;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
//import org.geotools.referencing.CRS;
import org.geotools.util.logging.Logging;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.geometry.MismatchedDimensionException;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.data.exception.ExceptionDesc;
import com.data.exception.FLException;
import com.data.exception.FLWriteException;
import com.data.org.apache.jdbm.DB;
import com.data.org.apache.jdbm.DBAbstract;
import com.data.org.apache.jdbm.DBMaker;
import com.data.org.apache.jdbm.DBStore;
import com.data.org.apache.jdbm.HTree;
import com.data.util.MapLog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapplan.Field;
import com.mapplan.FieldType;
import com.mapplan.LayerInfo;
import com.mapplan.Mbr;

//import com.vividsolutions.jts.io.OutStream;
//import com.vividsolutions.jts.io.ParseException;
//import com.vividsolutions.jump.coordsys.CoordinateSystem;
//import com.vividsolutions.jump.feature.Feature;
//import com.vividsolutions.jump.coordsys.CoordinateSystem;
//import com.vividsolutions.jump.feature.AttributeType;
//import com.vividsolutions.jump.feature.Feature;
//import com.vividsolutions.jump.feature.FeatureSchema;
//import com.vividsolutions.jump.feature.FeatureSchema;
//import org.apache.log4j.Logger;
import org.slf4j.Logger;

/**
 *
 */
public class FileLayer extends Transaction {

	// static String nullValue = "_null_";
	/**
	 * 검색 엔진은 null 조건을 줄 수가 없음. null에 해당되는 상수를 입력하여 조건을 줄수 있도록 하는 기능 메타 정보에 추가 필요함.
	 */
	public static String nullValue = null;

	public static Logger LOG = null;

	String recids = "recids";

	DBStore db = null;
	SimpleFeatureType sft = null;
	SimpleFeatureBuilder sfb = null;

	static String dataPath = "data";
	static String indexPath = "index";
	static String rtreeindexFileName = "rtree.idx";

	// HashMap<String, CoordinateReferenceSystem> customEPSG = new HashMap();

	Envelope mbr = null;

	String DS = "/";

	File path = null;

	String pathS = "";

	LayerInfo lyif = null;

	DataMng dataMng = null;
	GeoIndex geoMng = null;
	MetaInfo metaInfo = null;

	WKBReader wkbReader = new WKBReader();
	WKBWriter wkbWriter = new WKBWriter();

	long st;
	long et;

	public boolean init = false;

	// CoordinateReferenceSystem epsg4326 = null;
	//
	// MathTransform to4326 = null;
	// MathTransform from4326 = null;

	String metaFileName = "layerInfo.xml";

	// int changeSize = 0;
	//
	// int commitSize = 1000;

	boolean isClosed = false;

	public FileLayer() {

	}

	public File getPath() {
		return this.path;
	}

	public FileLayer(String path, LayerInfo layerInfo) throws Exception {

		// System.setProperty("org.geotools.referencing.forceXY", "true");
		if (layerInfo != null) {
			this.create(path, layerInfo);
		} else {
			boolean ok = this.read(path);
			if (!ok) {
			}
		}
	}

	/**
	 * FieldType 의 값이 Geometry 의 경우 MultiPoint, MultiPolygon, MultiLineString 으로 변경
	 * 
	 * @param path
	 * @return
	 */
	public static boolean fixGeometry(String path) {
		boolean isGeo = false;
		MetaInfo metaInfo = new MetaInfo();
		try {

			File file = new File(path);
			if (!file.exists()) {
				return false;
			}

			LayerInfo li = metaInfo.readLayerInfo(path + "/layerInfo.xml");

			SimpleFeatureType sft = FileLayer.getSimpleFeatureType(li);

			List<Field> fds = li.getSchema();

			Field geoFd = null;
			for (Field fd : fds) {
				if (fd.getType().equals(FieldType.GEOMETRY)) {
					isGeo = true;
					geoFd = fd;
				}
			}
			if (isGeo) {
				DataMng dataMng = new DataMng();
				dataMng.createData(new File(path), false);
				Iterator it = dataMng.getIterator();
				while (it.hasNext()) {
					long pageId = (long) it.next();
					SimpleFeature sf = dataMng.read(pageId, sft);
					Geometry geo = (Geometry) sf.getDefaultGeometry();
					if (geo instanceof Point || geo instanceof MultiPoint) {
						geoFd.setType(FieldType.MULTI_POINT);
					} else if (geo instanceof Polygon || geo instanceof MultiPolygon) {
						geoFd.setType(FieldType.MULTI_POLYGON);
					} else if (geo instanceof LineString || geo instanceof MultiLineString) {
						geoFd.setType(FieldType.MULTI_LINE_STRING);
					}
					break;
				}
				dataMng.close();
			}
			metaInfo.writeLayerInfo(path + "/layerInfo.xml", li);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
		return isGeo;
	}

	public FieldType getGeometryType(Geometry geo) {

		if (geo instanceof Point || geo instanceof MultiPoint) {
			return FieldType.MULTI_POINT;
		} else if (geo instanceof Polygon || geo instanceof MultiPolygon) {
			return FieldType.MULTI_POLYGON;
		} else if (geo instanceof LineString || geo instanceof MultiLineString) {
			return FieldType.MULTI_LINE_STRING;
		}
		return FieldType.GEOMETRY;
		// sc.setType(FieldType.GEOMETRY);
	}

	// public CoordinateReferenceSystem getCustomCRS(String code) {
	// CoordinateReferenceSystem crs = null;
	// crs = this.customEPSG.get(code);
	// return crs;
	// }

	/**
	 * 
	 * @return
	 */
	public int getAutoCommitSize() {
		return this.commitSize;
	}

	/*
	 * 데이타 변경( removeFeature or writeFeature 호출 )이 발생 되었을 경우 commit()을 호출 하지 않더라도
	 * 입력된 size 만큼 변경이 발생되면 자동으로 commit()을 수행함. size < 0 경우는 자동으로 commit()을 수행하지 않음.
	 * 
	 * @see com.dawul.data.file.TransactionMng#setAutoCommitSize(int)
	 */
	public void setAutoCommitSize(int size) {
		this.dataMng.setAutoCommitSize(size);
		this.commitSize = size;
	}

	/*
	 * 데이타 변경 사항을 저장소(디스크)에 저장함. commit을 호출 하기 이전의 변경 사항(removeFeature,
	 * writeFeature)은 메모리 캐쉬로 저장되며 commit을 호출 했을때 비로소 저장소에 저장됨.
	 * 
	 * 많은 양의 데이타 변경 시 commit을 최소한으로 실행하도록 하며 메모릭가 부족할 경우 java heap 크기를 늘리도록 한다. 현재
	 * setAutoCommitSize로 자동으로 커믿되는 사이즈를 조절 가능하며 초기 값은 1000 건이다
	 * 
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dawul.data.file.TransactionMng#commit()
	 */
	public int commit() {

		int changeDataSize = this.dataMng.commit();

		this.changeSize = 0;

		return changeDataSize;
	}

	private void init() {
		this.isClosed = false;
		if (this.LOG == null) {
			// if (MapLog.LOG_PATH.length() == 0) {
			// MapLog.LOG_PATH = System.getProperty("user.dir") + "/";
			// System.out.println("log path = " + MapLog.LOG_PATH);
			// }
			this.LOG = MapLog.getFileLog();
		}

		// this.loadCustomEPSG();
	}

	public static void main(String[] args) {

		try {

			FileLayer.fixGeometry("F:\\coops\\2024\\tileset\\output\\reg10\\layers\\ga");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private boolean delete(File layerFile) {
		if (layerFile == null || !layerFile.exists()) {
			return false;
		} else {
			File[] files = layerFile.listFiles();
			for (File file : files) {
				if (!file.isDirectory()) {
					file.delete();
				} else {
					delete(file);
					file.delete();
				}
			}
		}
		return true;
	}

	private void updateDate() throws Exception {
		// Date create = new Date();
		// SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		// lyif.setUpdate(sdf.format(create).toString());
		// this.lyif.setUpdate();
		this.lyif.setModifyDate(this.getTime());
	}

	/**
	 * 레이어의 지도 영역 리턴
	 * 
	 * @return
	 */
	public Envelope getEnvelope() {
		if (this.mbr == null && this.lyif != null) {
			Mbr temp = this.lyif.getMbr();
			this.mbr = new Envelope(temp.getMinx(), temp.getMaxx(), temp.getMiny(), temp.getMaxy());
		}
		return this.mbr;
	}

	/**
	 * pageId 에 해당하는 SimpleFeature 객체 삭제
	 * 
	 * @param pageId
	 * @return
	 * @throws Exception
	 */
	public SimpleFeature removeFeature(long pageId) throws Exception {

		SimpleFeature sf = this.dataMng.remove(pageId, this.sft);
		if (sf == null) {
			return null;
		}

		GeometryDescriptor gd = sft.getGeometryDescriptor();
		if (gd != null) {
			Boolean rtreeIdxMode = (Boolean) gd.getType().getUserData().get("index");
			if (sf.getDefaultGeometry() != null && rtreeIdxMode != null && rtreeIdxMode.equals(true)) {
				Geometry geo = (Geometry) sf.getDefaultGeometry();

				String code = "";

				if (this.btreeField.length() > 0) {
					code = (String) sf.getAttribute(this.btreeField);
				}

				this.geoMng.remove(geo, pageId, code);
			}
		}

		this.changeSize++;
		if (this.commitSize == this.changeSize) {
			this.commit();
		}

		return sf;
		// this.rtree.deleteEntity(rect, pageId);
	}

	/**
	 * 데이타 저장 주소 번지로 공간 객체 리턴
	 * 
	 * @param pageId 페이지 아이디
	 * @return 공간 객체
	 * @throws Exception
	 * @throws ParseException
	 */
	public SimpleFeature readFeature(long pageId) throws Exception, ParseException {
		return this.dataMng.read(pageId, sft);
	}

	/**
	 * 
	 * @param minx
	 * @param miny
	 * @param maxx
	 * @param maxy
	 * @return
	 * @throws Exception
	 */
	public SimpleFeatureCollection getFeatures(double minx, double miny, double maxx, double maxy) throws Exception {
		ListFeatureCollection fc = new ListFeatureCollection(this.sft);
		// Vector pages = this.geoMng.read(minx, miny, maxx, maxy);
		Vector pages = this.getFeatureIdxs(minx, miny, maxx, maxy);
		// System.out.println("size = " + pages.size());
		for (int i = 0; i < pages.size(); i++) {
			long pageId = (Long) pages.get(i);
			SimpleFeature sf = this.dataMng.read(pageId, this.sft);
			fc.add(sf);
		}
		return fc;
	}

	/**
	 * 
	 * @param minx
	 * @param miny
	 * @param maxx
	 * @param maxy
	 * @return
	 * @throws Exception
	 */
	public Vector getCodes(double minx, double miny, double maxx, double maxy) throws Exception {
		ListFeatureCollection fc = new ListFeatureCollection(this.sft);
		// Vector pages = this.geoMng.read(minx, miny, maxx, maxy);
		Vector pages = this.geoMng.readCode(minx, miny, maxx, maxy);
		// System.out.println("size = " + pages.size());

		return pages;
	}

	public SimpleFeature getFeature(String pnu) throws Exception {
		ListFeatureCollection fc = new ListFeatureCollection(this.sft);
		// Vector pages = this.geoMng.read(minx, miny, maxx, maxy);
		// long page = this.geoMng.read(pnu);
		long page = this.dataMng.read(pnu);
		if (page > -1) {
			return this.readFeature(page);
		} else {
			return null;
		}
		// System.out.println("size = " + pages.size());
		// return sf;
	}

	public Vector getFeatureIdxs(double minx, double miny, double maxx, double maxy) throws Exception {
		Vector pages = this.geoMng.read(minx, miny, maxx, maxy);
		return pages;
	}

	public synchronized long writeFeature(SimpleFeature sf) throws Exception {
		
		
		Geometry geom = (Geometry)sf.getDefaultGeometry();
		
		if(geom != null) {
			Envelope env =  geom.getEnvelopeInternal();
			if(this.lyif.getMaxObjMbr() == null) {
				
				
				Mbr mbr = new Mbr();
				mbr.setMinx(env.getMinX());
				mbr.setMiny(env.getMinY());
				mbr.setMaxx(env.getMaxX());
				mbr.setMaxy(env.getMaxY());
				this.lyif.setMaxObjMbr(mbr);
			}
			else {
				Mbr mbr = this.lyif.getMaxObjMbr();
				
				if( (mbr.getMaxx() - mbr.getMinx()) < env.getWidth() && (mbr.getMaxy() - mbr.getMiny()) < env.getHeight() ) {
					mbr.setMinx(env.getMinX());
					mbr.setMiny(env.getMinY());
					mbr.setMaxx(env.getMaxX());
					mbr.setMaxy(env.getMaxY());
				}
				
			}
			
		}

		long page = this.dataMng.write(sf, sft);

		if (page < 0) {
			return -1;
		}

		if (sft.getGeometryDescriptor() != null) {
			Boolean rtreeIdxMode = (Boolean) sft.getGeometryDescriptor().getType().getUserData().get("index");
			if (sf.getDefaultGeometry() != null && rtreeIdxMode != null && rtreeIdxMode.equals(true)) {
				Geometry geo = (Geometry) sf.getDefaultGeometry();

				if (this.btreeField.length() > 0) {
					String key = (String) sf.getAttribute(this.btreeField);
					this.dataMng.write(key, page);
				}

				String code = "";

				if (this.isCodeIndex) {
					code = (String) sf.getAttribute(this.codeField);

				}
				this.geoMng.write(geo, page, code, this.isCodeIndex);

				/*
				 * String code = "";
				 * 
				 * if(this.btreeField.length() > 0) {
				 * code = (String)sf.getAttribute(this.btreeField);
				 * }
				 * 
				 * this.geoMng.write(geo, page, code, this.isCodeIndex);
				 * 
				 * if(this.btreeField.length() > 0 && !this.isCodeIndex && code != null) {
				 * this.dataMng.write(code, page);
				 * }
				 */
			}
		}

		this.changeSize++;

		if (this.commitSize == this.changeSize) {
			this.commit();
		}
		return page;
	}

	public long getObjectSize() {
		return this.dataMng.getSize();
	}

	/**
	 * _interval 간격으로 인덱스의 시작과 끝 리스트를 리턴
	 * 
	 * @param _interval
	 * @return
	 */
	public Vector<long[]> getIntervalList(int _interval) {
		Vector<long[]> list = new Vector();
		long size = this.getObjectSize();

		int interval = (int) (size / _interval);

		int intervalCnt = (int) (size / interval);

		long start = 0;
		long end = 0;
		for (int i = 0; i < intervalCnt; i++) {
			start = i * interval;
			end = i * interval + interval - 1;

			long[] idx = new long[2];
			idx[0] = start;
			idx[1] = end;
			list.add(idx);
			System.out.println("s:" + start + ",e:" + end);
		}
		if (end < size - 1) {
			start = end + 1;
			end = size - 1;

			long[] idx = new long[2];
			idx[0] = start;
			idx[1] = end;
			list.add(idx);
			System.out.println("s:" + start + ",e:" + end);
		}
		return list;
	}

	public Vector<Iterator> getIntervalList1(int _interval) {
		Vector<Iterator> list = new Vector();
		long size = this.getObjectSize();

		int interval = (int) (size / _interval);

		int intervalCnt = (int) (size / interval);

		long start = 0;
		long end = 0;
		for (int i = 0; i < intervalCnt; i++) {
			start = i * interval;
			end = i * interval + interval - 1;

			long[] idx = new long[2];
			idx[0] = start;
			idx[1] = end;
			// list.add(idx);
			list.add(this.iterator(start, end));
			System.out.println("s:" + start + ",e:" + end);
		}
		if (end < size - 1) {
			start = end + 1;
			end = size - 1;

			long[] idx = new long[2];
			idx[0] = start;
			idx[1] = end;
			// list.add(idx);
			list.add(this.iterator(start, end));
			System.out.println("s:" + start + ",e:" + end);
		}

		return list;
	}

	// boolean setGeometryType(Geometry geo){
	// if(geo instanceof Polygon){
	// lyif.setGeoType(GeoType.POLYGON);
	// }
	// else if(geo instanceof MultiPolygon){
	// lyif.setGeoType(GeoType.MULTI_POLYGON);
	// }
	// else if(geo instanceof LineString){
	// lyif.setGeoType(GeoType.LINE_STRING);
	// }
	// else if(geo instanceof MultiLineString){
	// lyif.setGeoType(GeoType.MULTI_LINE_STRING);
	// }
	// else if(geo instanceof Point){
	// lyif.setGeoType(GeoType.POINT);
	// }
	// else if(geo instanceof MultiPoint){
	// lyif.setGeoType(GeoType.MULTI_POINT);
	// }
	// else{
	// return false;
	// }
	// return true;
	// }

	// private void createData(){
	// if(!this.path.exists()){
	// this.path.mkdirs();
	// }
	// File data = new File(path+this.DS+dataPath);
	// if(!data.exists()){
	// data.mkdirs();
	// }
	// File dataP = new File(path+this.DS+dataPath+this.DS+"data");
	// this.db = (DBStore)
	// DBMaker.openFile(dataP.getAbsolutePath()).enableMRUCache().make();
	// }
	//
	//
	//
	// /**
	// *
	// * @param readMode
	// * @param memory
	// */
	// private void createGeoIndex(boolean newcreate) throws Exception{
	// this.rtreePageFile = new RTreeIndexFile(this.getGeoIndexFile(), newcreate);
	// if( newcreate){
	// this.rtree = new RTree( RTree.FILL_FACTOR,RTree.CAPACITY,
	// this.rtreePageFile);
	// }
	// else{
	// this.rtree = new RTree(this.rtreePageFile);
	// }
	// }
	//
	//
	// /**
	// *
	// * @throws Exception
	// */
	// private void createBtreeIndex(boolean newVersion) throws Exception{
	// this.dir = FSDirectory.open(new File(this.getBtreeIndexFile()));
	// //this.analyzer = new StandardAnalyzer(Version.LUCENE_47);
	// this.analyzer = new KoreanAnalyzer(luceneVersion);
	// this.iwc = new IndexWriterConfig(luceneVersion, analyzer);
	// if(newVersion){
	// this.iwc.setOpenMode(OpenMode.CREATE);
	// }
	// else{
	// this.iwc.setOpenMode(OpenMode.APPEND);
	// }
	// this.iw = new IndexWriter(dir, iwc);
	// }

	private String getGeoIndexFile() {
		File file = new File(this.path.getAbsolutePath() + DS + this.indexPath);
		if (!file.exists()) {
			file.mkdirs();
		}
		return this.path.getAbsolutePath() + DS + this.indexPath + DS + this.rtreeindexFileName;
	}

	private String getBtreeIndexFile() {
		return this.path.getAbsolutePath() + DS + this.indexPath;
	}

	// public FieldType getGeoType() {
	// GeometryType gt =
	// this.getSimpleFeatureType().getGeometryDescriptor().getType();
	//
	// }

	public static Class getType(FieldType ft) {
		if (ft.equals(FieldType.INT)) {
			return Integer.class;
		} else if (ft.equals(FieldType.BYTE)) {
			return Byte.class;
		} else if (ft.equals(FieldType.DATE)) {
			return Date.class;
		} else if (ft.equals(FieldType.DOUBLE)) {
			return Double.class;
		} else if (ft.equals(FieldType.LONG)) {
			return Long.class;
		} else if (ft.equals(FieldType.SHORT)) {
			return Short.class;
		} else if (ft.equals(FieldType.FLOAT)) {
			return Float.class;
		} else if (ft.equals(FieldType.STRING)) {
			return String.class;
		} else if (ft.equals(FieldType.TEXT)) {
			return String.class;
		} else if (ft.equals(FieldType.BINARY)) {
			return Object.class;
		} else if (ft.equals(FieldType.POLYGON)) {
			return org.locationtech.jts.geom.Polygon.class;
		} else if (ft.equals(FieldType.POINT)) {
			return org.locationtech.jts.geom.Point.class;
		} else if (ft.equals(FieldType.LINE_STRING)) {
			return org.locationtech.jts.geom.LineString.class;
		} else if (ft.equals(FieldType.MULTI_POINT)) {
			return org.locationtech.jts.geom.MultiPoint.class;
		} else if (ft.equals(FieldType.MULTI_POLYGON)) {
			return org.locationtech.jts.geom.MultiPolygon.class;
		} else if (ft.equals(FieldType.MULTI_LINE_STRING)) {
			return org.locationtech.jts.geom.MultiLineString.class;
		} else if (ft.equals(FieldType.GEOMETRY_COLLECTION)) {
			return org.locationtech.jts.geom.GeometryCollection.class;
		} else if (ft.equals(FieldType.GEOMETRY)) {
			return org.locationtech.jts.geom.Geometry.class;
		} else if (ft.equals(FieldType.BIGDECIMAL)) {
			return java.math.BigDecimal.class;
		}

		return null;
	}

	public SimpleFeatureType getSimpleFeatureType() {
		return this.sft;
	}

	public Class getGeometryClass() {
		return this.sft.getGeometryDescriptor().getType().getBinding();
	}

	private FieldType getFieldTypeFromAttributeType(AttributeType at) {
		if (at.getBinding().equals(Integer.class)) {
			return FieldType.INT;
		} else if (at.getBinding().equals(Byte.class)) {
			return FieldType.BYTE;
		} else if (at.getBinding().equals(Short.class)) {
			return FieldType.SHORT;
		} else if (at.getBinding().equals(Long.class)) {
			return FieldType.LONG;
		} else if (at.getBinding().equals(Double.class)) {
			return FieldType.DOUBLE;
		} else if (at.getBinding().equals(Boolean.class)) {
			return FieldType.BYTE;
		} else if (at.getBinding().equals(Float.class)) {
			return FieldType.FLOAT;
		} else if (at.getBinding().equals(String.class)) {
			return FieldType.STRING;
		} else if (at.getBinding().equals(Date.class)) {
			return FieldType.DATE;
		} else if (at.getBinding().equals(BigDecimal.class)) {
			return FieldType.BIGDECIMAL;
		}
		// else if(at.getBinding().equals(Point.class) ||
		// at.getBinding().equals(MultiPoint.class)){
		// return FieldType.POINT;
		// }
		// else if(at.getBinding().equals(LineString.class) ||
		// at.getBinding().equals(MultiLineString.class)){
		// return FieldType.LINE_STRING;
		// }
		// else if(at.getBinding().equals(Polygon.class) ||
		// at.getBinding().equals(MultiPolygon.class)){
		// return FieldType.POLYGON;
		// }
		else if (at.getBinding().equals(MultiPoint.class)) {
			return FieldType.MULTI_POINT;
		} else if (at.getBinding().equals(MultiLineString.class)) {
			return FieldType.MULTI_LINE_STRING;
		} else if (at.getBinding().equals(MultiPolygon.class)) {
			return FieldType.MULTI_POLYGON;
		} else if (at.getBinding().equals(Point.class)) {
			return FieldType.POINT;
		} else if (at.getBinding().equals(LineString.class)) {
			return FieldType.LINE_STRING;
		} else if (at.getBinding().equals(Polygon.class)) {
			return FieldType.POLYGON;
		} else if (at.getBinding().equals(GeometryCollection.class)) {
			return FieldType.GEOMETRY_COLLECTION;
		} else if (at.getBinding().equals(Geometry.class)) {
			return FieldType.GEOMETRY;
		}
		return null;

	}

	/**
	 * geotools의 SimpleFeatureType 객체를
	 * 
	 * @param sft
	 * @return
	 */
	static public LayerInfo FromSimpleFeatureTypeToLayerInfo(SimpleFeatureType sft) {

		LayerInfo lyif = new LayerInfo();

		CoordinateReferenceSystem crs = sft.getCoordinateReferenceSystem();

		if (crs != null) {
			Integer epsgCode = null;
			try {
				epsgCode = CRS.lookupEpsgCode(crs, true);
				CoordinateReferenceSystem convCRS = null;
				if(epsgCode == null) {
					convCRS = CRS.decode("EPSG:5179", false);
				}
				else {
					convCRS = CRS.decode("EPSG:"+epsgCode, false);
				}
				crs = convCRS;
			} catch (FactoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			}
			
			lyif.setProjection(crs.toWKT());
		}

		List<Field> fds = lyif.getSchema();

		for (int i = 0; i < sft.getAttributeCount(); i++) {
			Field fd = new Field();

			AttributeDescriptor ad = sft.getDescriptor(i);

			String name = ad.getName().toString();

			AttributeType at = (AttributeType) ad.getType();

			Map userData = ad.getUserData();
			Boolean mode = (Boolean) userData.get("index");

			fd.setName(name);

			if (mode != null) {
				fd.setIsIndex(mode);
			} else {
				fd.setIsIndex(false);
			}

			mode = (Boolean) userData.get("isCodeIndex");

			if (mode != null) {
				fd.setIsCodeIndex(mode);
			} else {
				fd.setIsCodeIndex(false);
			}

			FieldType ft = null;

			// System.out.println("fd name="+name+", type="+at.getBinding().toString());
			Class binding = at.getBinding();
			if (binding.toString().equals(Integer.class.toString())) {
				ft = FieldType.INT;
			} else if (at.getBinding().equals(Byte.class)) {
				ft = FieldType.BYTE;
			} else if (at.getBinding().equals(Short.class)) {
				ft = FieldType.SHORT;
			} else if (at.getBinding().equals(Long.class)) {
				ft = FieldType.LONG;
			} else if (at.getBinding().equals(Double.class)) {
				ft = FieldType.DOUBLE;
			} else if (at.getBinding().equals(Boolean.class)) {
				ft = FieldType.BYTE;
			} else if (at.getBinding().equals(Float.class)) {
				ft = FieldType.FLOAT;
			} else if (at.getBinding().equals(String.class)) {
				ft = FieldType.STRING;
			} else if (binding.toString().equals(java.sql.Date.class.toString())) {
				ft = FieldType.DATE;
			} else if (at.getBinding().equals(MultiPoint.class)) {
				ft = FieldType.MULTI_POINT;
			} else if (at.getBinding().equals(MultiLineString.class)) {
				ft = FieldType.MULTI_LINE_STRING;
			} else if (at.getBinding().equals(MultiPolygon.class)) {
				ft = FieldType.MULTI_POLYGON;
			} else if (at.getBinding().equals(Point.class)) {
				ft = FieldType.POINT;
			} else if (at.getBinding().equals(LineString.class)) {
				ft = FieldType.LINE_STRING;
			} else if (at.getBinding().equals(Polygon.class)) {
				ft = FieldType.POLYGON;
			} else if (at.getBinding().equals(GeometryCollection.class)) {
				ft = FieldType.GEOMETRY_COLLECTION;
			} else if (at.getBinding().equals(Geometry.class)) {
				ft = FieldType.GEOMETRY;
			} else if (at.getBinding().equals(java.math.BigDecimal.class)) {
				// 스키마에 이 타입 관련해서 추가 되어야 함
				ft = FieldType.BIGDECIMAL;
			} else if (at.getBinding().equals(java.sql.Timestamp.class)) {
				// 스키마에 이 타입 관련해서 추가 되어야 함
				ft = FieldType.DATE;
			} else if (at.getBinding().equals(java.util.Date.class)) {
				// 스키마에 이 타입 관련해서 추가 되어야 함
				ft = FieldType.DATE;
			}

			fd.setType(ft);

			fds.add(fd);
		}
		return lyif;
	}

	public LayerInfo getLayerInfo() {
		return this.lyif;
	}

	public static SimpleFeatureType getSimpleFeatureType(LayerInfo li) throws Exception {

		SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();

		b.setName(li.getName());

		CoordinateReferenceSystem crs1 = null;
		if (li.getProjection() != null) {

			String prj = li.getProjection();

			String dst = null;

			byte[] prjbyte = prj.getBytes("euc-kr");
			byte vv = prjbyte[0];

			if (vv == 63) {
				byte[] temp = new byte[prjbyte.length - 1];

				System.arraycopy(prjbyte, 1, temp, 0, prjbyte.length - 1);

				dst = new String(temp, "euc-kr");
				li.setProjection(dst);
				crs1 = CRS.parseWKT(dst);
			} else {
				// prj = get5179();
				crs1 = CRS.parseWKT(prj);
			}
			b.setCRS(crs1);

			// CoordinateReferenceSystem crs5179 = CRS.decode("EPSG:5179", false);
			// b.setCRS(crs5179);
		} else {
			// LOG.debug("layer name = " + li.getName()+", not exist projection info");
		}

		List<Field> fds = li.getSchema();

		for (int i = 0; i < fds.size(); i++) {

			AttributeTypeBuilder build = new AttributeTypeBuilder();

			build.setNillable(true);

			Field fd = fds.get(i);

			FieldType ft = fd.getType();

			if (fd.isIsIndex() != null && fd.isIsIndex()) {
				build.addUserData("index", true);
			} else {
				build.addUserData("index", false);
			}

			if (fd.isIsCodeIndex() != null && fd.isIsCodeIndex()) {
				build.addUserData("isCodeIndex", true);
			} else {
				build.addUserData("isCodeIndex", false);
			}

			if (ft == null
					&& (fd.getName().toLowerCase().equals("geometry") || fd.getName().toLowerCase().equals("shape"))) {
				ft = FieldType.GEOMETRY;
				fd.setType(ft);
			}

			build.setBinding(getType(ft));

			if (ft.equals(FieldType.POLYGON) || ft.equals(FieldType.POINT) || ft.equals(FieldType.LINE_STRING)
					|| ft.equals(FieldType.MULTI_POINT) || ft.equals(FieldType.MULTI_POLYGON)
					|| ft.equals(FieldType.MULTI_LINE_STRING) || ft.equals(FieldType.GEOMETRY_COLLECTION)
					|| ft.equals(FieldType.GEOMETRY)) {
				if (crs1 != null) {
					build.setCRS(crs1);
				}
				GeometryType gt = (GeometryType) build.buildGeometryType();
				gt.getUserData().put("index", true);

				b.add(build.buildDescriptor(fd.getName(), gt));
			} else {
				b.add(build.buildDescriptor(fd.getName()));
			}

		}

		SimpleFeatureType sftt = b.buildFeatureType();

		SimpleFeatureBuilder sfbb = new SimpleFeatureBuilder(sftt);

		return sftt;
	}

	// public void create(String path, SimpleFeatureType sft) throws Exception{
	// LayerInfo _li = this.FromSimpleFeatureTypeToLayerInfo(sft);
	// this.create(path, _li);
	// }

	public String getName() {
		return this.path.getName();
	}

	@Deprecated
	public void create(String path, LayerInfo _li) throws Exception {

		this.init();

		this.st = System.currentTimeMillis();

		LOG.debug("create path=" + path);

		this.pathS = path.trim();
		this.path = new File(pathS);

		String tempPath = this.path.getParent();
		String tempName = this.path.getName();

		this.pathS = tempPath + File.separator + tempName.toLowerCase();
		this.path = new File(pathS);

		File parent = this.path.getParentFile();

		if (!parent.exists()) {
			// throw new FLWriteException(ExceptionDesc.write_notPath+",
			// path="+parent.getAbsolutePath());
			parent.mkdirs();
		}

		this.delete(this.path);
		this.path.delete();

		_li.setName(this.path.getName());
		// this.createData();

		LayerInfo li = _li;
		if (li == null) {
			LOG.error("LayerInfo no exist");
			throw new FLWriteException(ExceptionDesc.write_layerInfo);
		}
		this.lyif = null;
		this.lyif = new LayerInfo();
		this.lyif.setCharSet(li.getCharSet());
		this.lyif.setDesc(li.getDesc());
		// this.lyif.setGeoType(li.getGeoType());
		this.lyif.setName(li.getName());
		this.lyif.setProjection(li.getProjection());
		this.lyif.setGridSize(li.getGridSize());

		List<Field> fields = this.lyif.getSchema();
		List<Field> temps = li.getSchema();
		for (Field fd : temps) {
			Field nfd = new Field();
			nfd.setDesc(fd.getDesc());
			nfd.setIsIndex(fd.isIsIndex());
			nfd.setIsCodeIndex(fd.isIsCodeIndex());
			nfd.setName(fd.getName());
			nfd.setScale(fd.getScale());
			nfd.setSize(fd.getSize());
			nfd.setType(fd.getType());

			fields.add(nfd);
		}

		// XMLGregorianCalendar value = XMLGregorianCalendar();
		this.lyif.setCreateDate(this.getTime());
		this.lyif.setModifyDate(this.getTime());

		this.init(true);

		this.metaInfo.writeLayerInfo(this.path.getAbsoluteFile() + "/" + this.metaFileName, li);

		this.init = true;

		// this.metaInfo.writeLayerInfo(path, li);
	}

	static public LayerInfo cloneLayerInfo(LayerInfo li) {

		LayerInfo newLI = new LayerInfo();

		newLI.setName(li.getName());
		// this.createData();

		newLI.setCharSet(li.getCharSet());
		newLI.setDesc(li.getDesc());
		newLI.setProjection(li.getProjection());
		newLI.setGridSize(li.getGridSize());

		List<Field> fields = newLI.getSchema();
		List<Field> temps = li.getSchema();
		for (Field fd : temps) {
			Field nfd = new Field();
			nfd.setDesc(fd.getDesc());
			nfd.setIsIndex(fd.isIsIndex());
			nfd.setIsCodeIndex(fd.isIsCodeIndex());
			nfd.setName(fd.getName());
			nfd.setScale(fd.getScale());
			nfd.setSize(fd.getSize());
			nfd.setType(fd.getType());

			fields.add(nfd);
		}

		newLI.setCreateDate(li.getCreateDate());
		return newLI;
	}

	String btreeField = "";

	boolean isCodeIndex = false;

	String codeField = "";

	public void init(boolean readMode) throws Exception {
		try {
			this.sft = this.getSimpleFeatureType(this.lyif);

			for (Field fd : this.lyif.getSchema()) {

				if (fd.getType().equals(FieldType.STRING) && fd.isIsIndex() != null && fd.isIsIndex()) {
					btreeField = fd.getName();
				}
				if (fd.getType().equals(FieldType.STRING) && fd.isIsCodeIndex() != null && fd.isIsCodeIndex()) {
					isCodeIndex = fd.isIsCodeIndex();
					codeField = fd.getName();
				}

			}

			this.sfb = new SimpleFeatureBuilder(sft);

			this.dataMng = new DataMng();
			this.dataMng.createData(this.path, readMode);

			this.geoMng = new GeoIndex();

			this.geoMng.createIndex(this.pathS, readMode, false);

			this.metaInfo = new MetaInfo();

		} catch (Exception e) {
			if (this.dataMng != null) {
				this.dataMng.close();
			}
			try {
				if (this.geoMng != null) {
					this.geoMng.close();
				}
			} catch (Throwable e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			throw e;
		}

	}

	@Deprecated
	public boolean read(String path) throws Exception {

		this.init();

		this.st = System.currentTimeMillis();

		// LOG.debug("read path = " + path);

		this.pathS = path;
		this.path = new File(pathS);

		String tempPath = this.path.getParent();
		String tempName = this.path.getName();

		this.pathS = tempPath + File.separator + tempName.toLowerCase();
		this.path = new File(pathS);

		this.metaInfo = new MetaInfo();
		LayerInfo li = this.metaInfo.readLayerInfo(this.path.getAbsoluteFile() + "/" + this.metaFileName);

		if (li == null) {
			LOG.error("LayerInfo no exist");

			return false;
		}

		this.lyif = li;

		/*
		 * this.lyif = null; this.lyif = new LayerInfo();
		 * this.lyif.setCharSet(li.getCharSet()); this.lyif.setDesc(li.getDesc());
		 * 
		 * if (li.getName() == null) { this.lyif.setName(this.path.getName()); } else {
		 * this.lyif.setName(li.getName()); }
		 * this.lyif.setProjection(li.getProjection());
		 * this.lyif.setObjectCount(li.getObjectCount()); List<Field> fields =
		 * this.lyif.getSchema(); List<Field> temps = li.getSchema(); for (Field fd :
		 * temps) { Field nfd = new Field(); nfd.setDesc(fd.getDesc());
		 * nfd.setIsIndex(fd.isIsIndex()); nfd.setName(fd.getName());
		 * nfd.setScale(fd.getScale()); nfd.setSize(fd.getSize());
		 * nfd.setType(fd.getType()); nfd.setMorphemeType(fd.getMorphemeType());
		 * nfd.setAddIndexFields(fd.getAddIndexFields());
		 * nfd.setAddGeoIndexType(fd.getAddGeoIndexType()); nfd.setSort(fd.isSort());
		 * nfd.setJaso(fd.isJaso()); fields.add(nfd); }
		 */
		this.init(false);

		return true;

		// this.sft = this.getSimpleFeatureType(this.lyif);
		//
		// this.dataMng = new DataMng();
		// this.dataMng.createData(this.path, false);
		// this.geoMng = new GeoIndex();
		// this.geoMng.createIndex(this.pathS, false, false);
		// this.attrMng = new AttrIndex();
		// this.attrMng.createIndex(this.pathS, false, this.sft);
		//
		// if(sft.getCoordinateReferenceSystem() != null) {
		// this.to4326 = CRS.findMathTransform(sft.getCoordinateReferenceSystem(),
		// this.epsg4326, true);
		// this.to4326 = CRS.findMathTransform(this.epsg4326,
		// sft.getCoordinateReferenceSystem(), true);
		// }
	}

	private XMLGregorianCalendar getTime() throws Exception {
		Date date = new Date();
		TimeZone zone = TimeZone.getTimeZone("Asia/Seoul");

		XMLGregorianCalendar xmlGregorianCalendar = null;
		DatatypeFactory dataTypeFactory = DatatypeFactory.newInstance();

		GregorianCalendar gregorianCalendar = new GregorianCalendar();
		gregorianCalendar.setTime(date);
		gregorianCalendar.setTimeZone(zone);

		return xmlGregorianCalendar = dataTypeFactory.newXMLGregorianCalendar(gregorianCalendar);
	}

	public List<Integer> print() throws IOException {
		this.db.countRecords();
		return this.db.print();
	}

	public long getLastModifyDate() {
		long date = this.getLayerInfo().getModifyDate().toGregorianCalendar().getTime().getTime();
		return date;
	}

	public void updateIndex() throws Exception {

		try {
			this.geoMng.close();
			this.geoMng = null;
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.geoMng = new GeoIndex();
		this.geoMng.createIndex(this.pathS, true, false);

		if (this.dataMng == null || this.dataMng.db.isClosed()) {
			this.dataMng = new DataMng();
			this.dataMng.createData(this.path, false);
		}

		int total = this.dataMng.getSize();

		long st = System.currentTimeMillis();

		Iterator it = this.dataMng.getIterator();

		int idx = 0;
		while (it.hasNext()) {
			long page = (Long) it.next();
			if (page < 0) {
				return;
			}

			SimpleFeature sf = this.readFeature(page);

			if (sft.getGeometryDescriptor() != null) {
				Boolean rtreeIdxMode = (Boolean) sft.getGeometryDescriptor().getType().getUserData().get("index");
				if (sf.getDefaultGeometry() != null && rtreeIdxMode != null && rtreeIdxMode.equals(true)) {
					Geometry geo = (Geometry) sf.getDefaultGeometry();

					String code = "";

					if (this.btreeField.length() > 0) {
						code = (String) sf.getAttribute(this.btreeField);
					}

					this.geoMng.write(geo, page, code, this.isCodeIndex);

					// this.geoMng.write(geo, page);
				}
			}

			idx++;

			if (idx % 100000 == 0) {
				long et = System.currentTimeMillis();

				long jobTime = (et - st);

				long totalTime = (jobTime * total) / idx;

				System.out.println("now cnt = " + idx + ", job time = " + jobTime / 60 / 1000 + " min"
						+ ", remaining time = " + (totalTime - jobTime) / 60 / 1000 + " min");
			}

		}
		// this.close();
	}

	public void close() throws Exception {
		try {

			if (this.isClosed) {
				return;
			}

			if (this.dataMng != null) {
				this.lyif.setObjectCount(this.dataMng.getSize());
			} else {
				this.lyif.setObjectCount(0);
			}

			this.metaInfo.writeLayerInfo(path + "/" + this.metaFileName, lyif);

			try {
				this.geoMng.close();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.dataMng.close();

			this.isClosed = true;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void reOpenClose() throws Exception {
		this.close();
		this.read(this.pathS);
		this.clone();
	}

	public ListFeatureCollection getCollection() {

		ListFeatureCollection fc = new ListFeatureCollection(this.sft);

		SimpleFeatureIterator sfi = this.iterator();
		while (sfi.hasNext()) {
			fc.add(sfi.next());
		}

		return fc;
	}

	public SimpleFeatureIterator iterator() {
		SimpleFeatureIterator fit = new SimpleFeatureIterator();
		return fit;
	}

	/**
	 * 시작 인덱스와 종료 인덱스를 지정하여 읽기, 시작 인덱스는 "0" 부터 시작함
	 * 
	 * @param startIdx
	 * @param endIdx
	 * @return
	 */
	public SimpleFeatureIterator iterator(long startIdx, long endIdx) {
		SimpleFeatureIterator fit = new SimpleFeatureIterator(startIdx, endIdx);
		return fit;
	}

	public class SimpleFeatureIterator implements Iterator<SimpleFeature> {

		// UMDReader reader = null; // synchronized 처리해야된다.
		HTree recid = null;
		Iterator it = null;

		long startIdx = -1;
		long endIdx = -1;
		long currentIdx = -1;
		boolean intervalMode = false;

		public SimpleFeatureIterator() {
			it = dataMng.getIterator();

		}

		public SimpleFeatureIterator(long _startIdx, long _endIdx) {
			it = dataMng.getIterator();
			this.startIdx = _startIdx;
			this.endIdx = _endIdx;
			this.intervalMode = true;
		}

		public boolean hasNext() {
			if (!intervalMode) {
				return it.hasNext();
			} else {
				if (this.currentIdx == -1) {
					this.currentIdx = 0;
					for (int i = 0; i < this.startIdx; i++) {
						if (it.hasNext()) {
							it.next();
							this.currentIdx++;
						}
					}
				}

				if (this.currentIdx > this.endIdx) {
					return false;
				}

				return it.hasNext();
			}
		}

		public SimpleFeature next() {
			SimpleFeature sf = null;
			long key = (Long) it.next();
			try {
				sf = readFeature(key);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (this.intervalMode) {
				this.currentIdx++;
			}

			return sf;
		}

		@Override
		public void remove() {
			// TODO Auto-generated method stub

		}

	}

	public SimpleFeatureCollection getFeatures(double minx, double miny, double maxx, double maxy, String code)
			throws Exception {
		ListFeatureCollection fc = new ListFeatureCollection(this.sft);

		Vector pages = this.geoMng.read(minx, miny, maxx, maxy, code);
		if (pages != null) {
			for (int i = 0; i < pages.size(); i++) {
				long pageId = (Long) pages.get(i);
				SimpleFeature sf = this.dataMng.read(pageId, this.sft);
				fc.add(sf);
			}
		}
		return fc;
	}

	public int getGridSize() {

		if (this.lyif.getGridSize() == null) {
			return -1;
		}

		return this.lyif.getGridSize();
	}

}