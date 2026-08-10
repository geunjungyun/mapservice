package com.gis.map;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.IndexColorModel;
import java.awt.image.RGBImageFilter;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.io.EndianUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
//import org.apache.log4j.Logger;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.data.DataSourceException;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.GridCoverageLayer;
import org.geotools.map.MapContent;
import org.geotools.map.MapViewport;
import org.geotools.referencing.CRS;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.PrecisionModel;
import org.opengis.coverage.grid.GridCoordinates;
import org.opengis.coverage.grid.GridEnvelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.pngquant.PngQuant;

import com.gis2.storage.ServiceConfig;
import com.gis2.storage.TileDB;
//import com.gis.storage.TileDB;
import com.gis2.storage.TileServiceMng;
import com.util.io.FileUt;

import com.util2.thread.JobResource;

import com.util2.thread.TileThreadPoolExecutor;

import com.data.util.JobMonitorLog;
import com.data.util.MapLog;
import com.gis.map.render.ImagePool;
import com.gis.map.ui.Scale;
import com.gis.projection.ScreenCoordUtil;
import com.gis.protocol.JobTile;
import com.gis.protocol.LevelSet;
import com.gis.protocol.MakeTile;
import com.gis.protocol.Mbr;
import com.gis.protocol.ScaleInfo;
import com.gis.protocol.ScaleInfos;

public class TileMapFactory2 {

	public static int FILE = 0;
	public static int JDBM = 1;
	public static int OBJECT = 2;
	public static int URL = 2;

	public int dataMode = FILE;

	public String crs = "";

	private String name = "";

	public static int LEFT_TOP = 1;
	public static int LEFT_BOTTOM = 2;

	public static int EMAP = 3;
	public static int REALESTATE = 2;
	public static int EUM = 1;

	TileThreadPoolExecutor executorService = null;

	public int tileMode = REALESTATE;

	public double oriX = -5423200.0;
	public double oriY = 6294600.0;

	public int imgWidth = 256;
	public int imgHeight = 256;

	public Scale scales = new Scale();

	public Envelope mbr = null;

	public String filePath = null;
	public String urlPath = null;
	public String dbPath = null;

	public String suffixPath = null;

	public String extName = null;

	public int startPointQuadrant = LEFT_BOTTOM;

	// public int tileMode = DAWUL_RULE;

	public String pathPatten = "${z-1}/${x/1000}/${x%1000}/${y%1000}/${z-1}_${x}_${y}.png";

	public boolean tileNameNumMode = false;

	// DB readDB = null;
	// ConcurrentMap readCM;

	public TileDB tileDb = null;

	public int imageBit = 8;

	// private MapData mapData;
	private MData mapData;

	public String mapInfoName;

	// 실시간 타일 요청 모드
	public boolean realTileMode = true;

	Object lock = new Object();

	HashMap<Integer, GenericObjectPool<BufferedImage>> imgObjPools = null;

	boolean runTile = false;

	org.opengis.referencing.crs.CoordinateReferenceSystem epsgTarget = null;

	// ReplicationMng rm = null;

	public static JobMonitorLog jobLog = null;

	public int threadCnt = 15;

	public boolean shutdown = false;

	public TileMapFactory2() {

	}

	public MData getMapData() {
		return this.mapData;
	}

	public void setMapData(MData mapData) {
		this.mapData = mapData;
	}

	public void setName(String _name) {
		this.name = _name;
	}

	public TileMapFactory2(String name, ScaleInfos sis, String path) {
		// TileMapFactory2 tmf = new TileMapFactory2();

		this.name = name;
		this.mapInfoName = name;

		this.crs = "epsg:5179";

		if (!crs.toLowerCase().equals("epsg:5179")) {
			try {

				epsgTarget = CRS.decode(crs);
				// MathTransform transform = CRS.findMathTransform(sourceCRS, epsgTarget);

			} catch (NoSuchAuthorityCodeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FactoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// hd 지도 모드

		if (TileServiceMng.hdMode) {
			imgHeight = 512;
			imgWidth = 512;
		} else {
			imgHeight = 256;
			imgWidth = 256;
		}

		// imgHeight = 256;
		// imgWidth = 256;

		// [-20037508.34, -20037508.34, 20037508.34, 20037508.34]
		// this.oriX = 90112;
		// this.oriY = 1192896;

		this.oriX = -200000.0;
		this.oriY = 4000000.0;

		// tmf.oriX = 90112;
		// tmf.oriY = 1192896;

		// ScaleInfos sis = tms.getScaleInfos();

		List<ScaleInfo> silist = sis.getScaleInfo();
		for (ScaleInfo si : silist) {
			scales.addScale(si.getId(), si.getPixelPerMeter());
		}

		startPointQuadrant = TileMapFactory2.LEFT_TOP;
		tileMode = TileMapFactory2.REALESTATE;

		this.dbPath = path;
		this.dataMode = JDBM;

		// this.filePath = path;
		// this.dataMode = FILE;

		extName = ".png";

		this.imageBit = 8;

		this.imgObjPools = new HashMap<Integer, GenericObjectPool<BufferedImage>>();

	}

	static public TileMapFactory2 initEMap() {

		TileMapFactory2 tmf = new TileMapFactory2();

		// tmf.urlPath =
		// "http://h0.maps.daum-img.net/map/image/G03/h/2200keery/";

		tmf.imgHeight = 256;
		tmf.imgWidth = 256;

		tmf.oriX = -200000.0;
		tmf.oriY = 4000000.0;

		tmf.crs = "epsg:5179";

		if (!tmf.crs.toLowerCase().equals("epsg:5179")) {
			try {

				tmf.epsgTarget = CRS.decode(tmf.crs);
				// MathTransform transform = CRS.findMathTransform(sourceCRS, epsgTarget);

			} catch (NoSuchAuthorityCodeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FactoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		tmf.scales.addScale(1, 2088.96);
		tmf.scales.addScale(2, 1044.48);
		tmf.scales.addScale(3, 522.24);
		tmf.scales.addScale(4, 261.12);
		tmf.scales.addScale(5, 130.56);
		tmf.scales.addScale(6, 65.28);
		tmf.scales.addScale(7, 32.64);
		tmf.scales.addScale(8, 16.32);
		tmf.scales.addScale(9, 8.16);
		tmf.scales.addScale(10, 4.08);
		tmf.scales.addScale(11, 2.04);
		tmf.scales.addScale(12, 1.02);
		tmf.scales.addScale(13, 0.51);
		tmf.scales.addScale(14, 0.255);
		// tmf.scales.addScale(15, 0.1275);

		tmf.startPointQuadrant = TileMapFactory2.LEFT_TOP;
		tmf.tileMode = TileMapFactory2.EMAP;
		tmf.tileNameNumMode = true;

		tmf.extName = ".png";

		tmf.imageBit = 8;

		// tmf.filePath = path;
		tmf.dataMode = FILE;

		tmf.imgObjPools = new HashMap<Integer, GenericObjectPool<BufferedImage>>();

		return tmf;
	}

	static public TileMapFactory2 initEMapHD() {

		TileMapFactory2 tmf = new TileMapFactory2();

		// tmf.urlPath =
		// "http://h0.maps.daum-img.net/map/image/G03/h/2200keery/";

		tmf.imgHeight = 512;
		tmf.imgWidth = 512;

		tmf.oriX = -200000.0;
		tmf.oriY = 4000000.0;

		tmf.crs = "epsg:5179";

		if (!tmf.crs.toLowerCase().equals("epsg:5179")) {
			try {

				tmf.epsgTarget = CRS.decode(tmf.crs);
				// MathTransform transform = CRS.findMathTransform(sourceCRS, epsgTarget);

			} catch (NoSuchAuthorityCodeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FactoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		tmf.scales.addScale(1, 1044.48);
		tmf.scales.addScale(2, 522.24);
		tmf.scales.addScale(3, 261.12);
		tmf.scales.addScale(4, 130.56);
		tmf.scales.addScale(5, 65.28);
		tmf.scales.addScale(6, 32.64);
		tmf.scales.addScale(7, 16.32);
		tmf.scales.addScale(8, 8.16);
		tmf.scales.addScale(9, 4.08);
		tmf.scales.addScale(10, 2.04);
		tmf.scales.addScale(11, 1.02);
		tmf.scales.addScale(12, 0.51);
		tmf.scales.addScale(13, 0.255);
		tmf.scales.addScale(14, 0.1275);

		// tmf.scales.addScale(1, 2088.96);
		// tmf.scales.addScale(2, 1044.48);
		// tmf.scales.addScale(3, 522.24);
		// tmf.scales.addScale(4, 261.12);
		// tmf.scales.addScale(5, 130.56);
		// tmf.scales.addScale(6, 65.28);
		// tmf.scales.addScale(7, 32.64);
		// tmf.scales.addScale(8, 16.32);
		// tmf.scales.addScale(9, 8.16);
		// tmf.scales.addScale(10, 4.08);
		// tmf.scales.addScale(11, 2.04);
		// tmf.scales.addScale(12, 1.02);
		// tmf.scales.addScale(13, 0.51);
		// tmf.scales.addScale(14, 0.255);

		tmf.startPointQuadrant = TileMapFactory2.LEFT_TOP;
		tmf.tileMode = TileMapFactory2.EMAP;
		tmf.tileNameNumMode = true;

		tmf.extName = ".png";

		tmf.imageBit = 8;

		// tmf.filePath = path;
		tmf.dataMode = FILE;

		tmf.imgObjPools = new HashMap<Integer, GenericObjectPool<BufferedImage>>();

		return tmf;
	}

	public String getName() {
		return this.name;
	}

	HashMap<String, BufferedImage> savaImg = new HashMap();

	Geometry deleteObj = null;
	PrecisionModel pm = new PrecisionModel();
	GeometryFactory fact = new GeometryFactory(pm);

	// Object lock = new Object();

	public void setDBPath(String path, boolean createMode) {

		File file = new File(path);

		// if(!file.exists()){
		// this.tileDb = new TileDB(path, false, true);
		// }
		// else{
		// this.tileDb = new TileDB(path, false, true);
		// }

		if (this.tileDb != null) {
			this.tileDb.close();
			this.tileDb = null;
		}

		this.tileDb = new TileDB(path, false, createMode);

	}

	public void setDBPath(String path, boolean readOnly, boolean createMode) {

		File file = new File(path);

		// if(!file.exists()){
		// this.tileDb = new TileDB(path, false, true);
		// }
		// else{
		// this.tileDb = new TileDB(path, false, true);
		// }

		if (this.tileDb != null) {

			this.tileDb.close();

			this.tileDb = null;
		}

		this.tileDb = new TileDB(path, readOnly, createMode);

	}

	public void deleteTile() throws Exception {

		if (this.dataMode == FILE) {
			FileUtils.forceDelete(new File(this.filePath));
		} else if (this.dataMode == JDBM) {

			if (this.tileDb != null) {
				this.tileDb.close();
			}

			File deleteFile = new File(this.dbPath);

			if (deleteFile.exists()) {
				// FileUtils.deleteDirectory(deleteFile);

				String name = deleteFile.getName();

				File[] tileFiles = deleteFile.listFiles();
				for (File del : tileFiles) {
					if (del.getName().startsWith(name)) {
						FileUtils.forceDelete(del);
						// del.deleteOnExit();
					}
				}

			}
			synchronized (this.lock) {
				this.tileDb = new TileDB(this.dbPath, false, false);
			}
		}
	}

	/**
	 * 해당 레벨에 env 영역을 포함하는 이미지 리턴
	 * 
	 * @param env
	 * @param level
	 * @return
	 */
	public BufferedImage getImage(Envelope env, int level) {

		double scaleValue = this.scales.getScaleRatio(level);

		int imgWidth = (int) (env.getWidth() / scaleValue);
		int imgHeight = (int) (env.getHeight() / scaleValue);

		BufferedImage image = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB);// TYPE_INT_RGB

		this.draw(env, level, image, imgWidth, imgHeight);

		return image;
	}

	/**
	 * 축척이 결정된 상태에서 Graphics2D 객체에 지도 영역을 그린다.
	 * 
	 * @param env
	 * @param level
	 * @param g
	 * @param width_
	 * @param height_
	 */
	public void draw(Envelope env, int level, Graphics2D g, int width_, int height_) {

		int width = width_;
		int height = height_;

		Graphics saveG = g;

		Envelope nowEnv = env;

		Dimension nowRect = new Dimension();
		nowRect.setSize(width, height);
		// nowRect.setSize(this.imgWidth, this.imgHeight);

		ScreenCoordUtil scu = new ScreenCoordUtil(nowRect, nowEnv);

		double scaleValue = this.scales.getScaleRatio(level) * this.imgWidth;

		double scaleValuey = this.scales.getScaleRatio(level) * this.imgHeight;

		// 좌하단 타일 인덱스
		int startXN = 0;
		int startYN = 0;

		// 우상단 타일 인덱스
		int endXN = 0;
		int endYN = 0;

		if (startPointQuadrant == LEFT_TOP) {
			startXN = (int) ((nowEnv.getMinX() - this.oriX) / scaleValue);
			startYN = (int) ((this.oriY - nowEnv.getMaxY()) / scaleValuey);
			endXN = (int) ((nowEnv.getMaxX() - this.oriX) / scaleValue);
			endYN = (int) ((this.oriY - nowEnv.getMinY()) / scaleValuey);
		} else {
			startXN = (int) (((nowEnv.getMinX() - this.oriX) / scaleValue));
			startYN = (int) ((nowEnv.getMinY() - this.oriY) / scaleValuey);
			endXN = (int) ((nowEnv.getMaxX() - this.oriX) / scaleValue);
			endYN = (int) ((nowEnv.getMaxY() - this.oriY) / scaleValuey);
		}

		int xCnt = (int) ((endXN - startXN));
		int yCnt = (int) ((endYN - startYN));

		double minTileX = 0;
		double minTileY = 0;

		if (startPointQuadrant == LEFT_TOP) {
			// minTileX = nowEnv.getMinX() - (nowEnv.getMinX() - this.oriX) % scaleValue;
			minTileX = this.oriX + (startXN) * scaleValue;
			// minTileY = nowEnv.getMaxY() + (this.oriY - nowEnv.getMaxY()) * scaleValuey;
			minTileY = this.oriY - (startYN) * scaleValuey;

		} else {
			minTileX = this.oriX + (startXN) * scaleValue;
			minTileY = this.oriY + (startYN) * scaleValuey;
		}

		double startScreenX = scu.getMapXToScrX(minTileX);
		double startScreenY = scu.getMapYToScrY(minTileY);

		int drawCnt = 0;
		int yStart = 0;
		int xStart = 0;
		if (startPointQuadrant == LEFT_TOP)
			// yStart = -1;

			// ThreadQueue tq = new ThreadQueue(1);
			// tq.add(this);
			//
			// tq.add(new Runnable() {
			// @Override
			// public void run() {
			//
			//
			//
			// }
			// }
			// );

			for (int i = yStart; i <= yCnt; i++) {
				for (int j = xStart; j <= xCnt; j++) {

					try {

						final int xidx = startXN + j;
						final int yidx = startYN + i;
						//
						// final int jj = j;
						// final int ii = i;
						//
						// final Graphics saveGG = saveG;

						// tq.add(new Runnable() {
						// @Override
						// public void run() {
						// synchronized (lock) {
						String tileName = getTilePath(level, xidx, yidx);

						if (realTileMode) {
							Envelope tileEnv = getMbr(tileName);
							tileName += "&level=" + level + "&xidx=" + (xidx) + "&yidx=" + (yidx) + "&mbr="
									+ tileEnv.getMinX() + "," + tileEnv.getMinY() + "," + tileEnv.getMaxX() + ","
									+ tileEnv.getMaxY() + "&tiles=create&ratio=1";
						}

						Image bakImg = null;
						try {
							bakImg = getTileImage(tileName);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						if (bakImg != null) {
							int centerx = (int) (startScreenX + (imgWidth * j));
							if (startPointQuadrant == LEFT_TOP) {
								int centery = (int) (startScreenY + (imgHeight * i));
								saveG.drawImage(bakImg, centerx, centery, null);
							} else {
								int centery = (int) (startScreenY - (imgHeight * i));

								saveG.drawImage(bakImg, centerx, centery - imgHeight, null);
								// saveG.setColor(Color.red);
								// saveG.drawRect(centerx, centery - imgHeight, imgHeight, imgHeight);
								// saveG.drawString(tileName, centerx + imgHeight/2, centery - imgHeight + +
								// imgHeight/2);
							}
						}

						// }});

						// System.out.println(drawCnt + " tile name="+tileName);
					} catch (Exception e) {

						e.printStackTrace();

					}
				}
			}

		// try {
		// synchronized (this.lock) {
		// this.lock.wait();
		// }
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

	}

	public void draw(Envelope env, int level, BufferedImage img, int width_, int height_) {

		int width = width_;
		int height = height_;

		Graphics saveG = null;
		if (img != null) {
			saveG = img.getGraphics();
		}

		Envelope nowEnv = env;

		Dimension nowRect = new Dimension();
		nowRect.setSize(width, height);
		// nowRect.setSize(this.imgWidth, this.imgHeight);

		ScreenCoordUtil scu = new ScreenCoordUtil(nowRect, nowEnv);

		double scaleValue = this.scales.getScaleRatio(level) * this.imgWidth;

		double scaleValuey = this.scales.getScaleRatio(level) * this.imgHeight;

		int startXN = 0;
		int startYN = 0;
		int endXN = 0;
		int endYN = 0;

		if (startPointQuadrant == LEFT_TOP) {
			startXN = (int) ((nowEnv.getMinX() - this.oriX) / scaleValue);
			startYN = (int) ((this.oriY - nowEnv.getMaxY()) / scaleValuey);
			endXN = (int) ((nowEnv.getMaxX() - this.oriX) / scaleValue);
			endYN = (int) ((this.oriY - nowEnv.getMinY()) / scaleValuey);
		} else {
			startXN = (int) (((nowEnv.getMinX() - this.oriX) / scaleValue));
			startYN = (int) ((nowEnv.getMinY() - this.oriY) / scaleValuey);
			endXN = (int) ((nowEnv.getMaxX() - this.oriX) / scaleValue);
			endYN = (int) ((nowEnv.getMaxY() - this.oriY) / scaleValuey);
		}

		int xCnt = (int) ((endXN - startXN));
		int yCnt = (int) ((endYN - startYN));

		double minTileX = 0;
		double minTileY = 0;

		if (startPointQuadrant == LEFT_TOP) {
			// minTileX = nowEnv.getMinX() - (nowEnv.getMinX() - this.oriX) % scaleValue;
			// minTileY = nowEnv.getMaxY() + (this.oriY - nowEnv.getMaxY()) % scaleValuey;

			minTileX = this.oriX + (startXN) * scaleValue;
			minTileY = this.oriY - (startYN) * scaleValuey;
		} else {
			minTileX = this.oriX + (startXN) * scaleValue;
			minTileY = this.oriY + (startYN) * scaleValuey;
		}

		double startScreenX = scu.getMapXToScrX(minTileX);
		double startScreenY = scu.getMapYToScrY(minTileY);

		int drawCnt = 0;
		int yStart = 0;
		int xStart = 0;
		if (startPointQuadrant == LEFT_TOP)
			yStart = -1;

		// ThreadQueue tq = new ThreadQueue(1);
		// tq.add(this);
		//
		// tq.add(new Runnable() {
		// @Override
		// public void run() {
		//
		//
		//
		// }
		// }
		// );

		for (int i = yStart; i <= yCnt; i++) {
			for (int j = xStart; j <= xCnt; j++) {

				try {

					final int xidx = startXN + j;
					final int yidx = startYN + i;
					//
					// final int jj = j;
					// final int ii = i;
					//
					// final Graphics saveGG = saveG;

					// tq.add(new Runnable() {
					// @Override
					// public void run() {
					// synchronized (lock) {
					String tileName = getTilePath(level, xidx, yidx);

					if (realTileMode) {
						Envelope tileEnv = getMbr(tileName);
						tileName += "&level=" + level + "&xidx=" + (xidx) + "&yidx=" + (yidx) + "&mbr="
								+ tileEnv.getMinX() + "," + tileEnv.getMinY() + "," + tileEnv.getMaxX() + ","
								+ tileEnv.getMaxY() + "&tiles=create&ratio=1";
					}

					Image bakImg = null;
					try {
						bakImg = getTileImage(tileName);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					if (bakImg != null) {
						int centerx = (int) (startScreenX + (imgWidth * j));
						if (startPointQuadrant == LEFT_TOP) {
							int centery = (int) (startScreenY + (imgHeight * i));
							saveG.drawImage(bakImg, centerx, centery, null);
						} else {
							int centery = (int) (startScreenY - (imgHeight * i));

							saveG.drawImage(bakImg, centerx, centery - imgHeight, null);
							// saveG.setColor(Color.red);
							// saveG.drawRect(centerx, centery - imgHeight, imgHeight, imgHeight);
							// saveG.drawString(tileName, centerx + imgHeight/2, centery - imgHeight + +
							// imgHeight/2);
						}
					}

					// }});

					// System.out.println(drawCnt + " tile name="+tileName);
				} catch (Exception e) {

					e.printStackTrace();

				}
			}
		}

		// try {
		// synchronized (this.lock) {
		// this.lock.wait();
		// }
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

	}

	public void draw(Envelope env, BufferedImage img) {

		BufferedImage saveImage = img;
		Envelope nowEnv = env;

		double xscale = nowEnv.getWidth() / img.getWidth();

		double yscale = nowEnv.getHeight() / img.getHeight();

		int selectLevel = this.scales.getLikeLevel(xscale);

		double selectScale = this.scales.getScaleRatio(selectLevel);

		// System.out.println("--level="+selectLevel+", call Scale="+xscale+", arc
		// scale="+selectScale+", ardwidth="+(nowEnv.getWidth()/selectScale)+",
		// 3600="+xscale*3600.0);

		int arcWidth = (int) (nowEnv.getWidth() / selectScale);
		int arcHeight = (int) (nowEnv.getHeight() / selectScale);

		BufferedImage image = new BufferedImage(arcWidth, arcHeight, BufferedImage.TYPE_INT_ARGB);// TYPE_INT_RGB

		this.draw(nowEnv, selectLevel, image, arcWidth, arcHeight);

		saveImage.getGraphics().drawImage(image, 0, 0, img.getWidth(), img.getHeight(), 0, 0, arcWidth, arcHeight,
				null);
	}

	public void draw(Envelope env, Graphics2D img, int imgWidth, int imgHeight) {

		Envelope nowEnv = env;

		double xscale = nowEnv.getWidth() / imgWidth;

		double yscale = nowEnv.getHeight() / imgHeight;

		int selectLevel = this.scales.getLikeLevel(xscale);

		// selectLevel--;

		double selectScale = this.scales.getScaleRatio(selectLevel);

		this.draw(nowEnv, selectLevel, img, imgWidth, imgHeight);
		// this.draw(nowEnv, 1, img, imgWidth, imgHeight);
	}

	public String getTilePath(int level, int xidx, int yidx) {
		// return level + "/" + (int) xidx / 50 + "/" + (int) yidx / 50 + "/" + xidx +
		// "_" + yidx + this.extName;

		if (this.tileMode == REALESTATE) {
			// return "L"+getCode((int) ((level)) + "", 2) + "/" + (xidx) + "/" + (yidx) +
			// ".png";
			return level + "/" + (xidx) + "/" + (yidx) + ".png";
		} else if (this.tileMode == EUM) {
			return level + "/" + xidx + "_" + yidx + this.extName;
		} else if (this.tileMode == EMAP) {
			return "L" + getCode((int) ((level + 4)) + "", 2) + "/" + (xidx) + "/" + (yidx) + ".png";
			// return null;
		}
		return null;
		// return level + "/" + xidx + "_" + yidx + this.extName;
	}

	public String getCode(String value, int maxLength) {
		int maxCode = maxLength;
		String symbos = value;
		if (symbos.length() < maxCode) {
			int symbolLen = symbos.length();
			for (int h = 0; h < (maxCode - symbolLen); h++) {
				symbos = "0" + symbos;
			}
		}
		return symbos;
	}

	// Vector<TileImg> saveTile = new Vector();

	double imgCnt = 0;
	public double imageSaveSize = 0;
	Object obj = new Object();

	public boolean putTileImage(String tileName, byte[] img) throws Exception {

		if (this.dbPath != null) {
			if (this.tileDb != null) {
				// System.out.println("tileName="+tileName+", img size = " + img.length);

				if (this.shutdown == true) {
					return false;
				}

				this.tileDb.putTile(tileName, img);
				synchronized (obj) {
					imgCnt++;
					imageSaveSize += img.length;
				}
			}
		} else if (this.filePath != null) {
			File tileFile = new File(this.filePath + tileName);
			File parent = tileFile.getParentFile();
			if (!parent.exists()) {
				parent.mkdirs();
			}
			// ImageIO.write(img, "png", tileFile);

			if (this.shutdown == true) {
				return false;
			}

			FileUtils.writeByteArrayToFile(tileFile, img);

			synchronized (obj) {
				imgCnt++;
				imageSaveSize += tileFile.length();
			}
		}

		return true;
	}

	public byte[] getTileByte(String tile) throws Exception {

		byte[] data = null;
		if (this.dbPath != null) {

			String saveKey = FilenameUtils.separatorsToUnix(tile);
			synchronized (this.lock) {
				if (this.tileDb == null) {
					this.tileDb = new TileDB(this.dbPath, true, false);
				}
			}

			if (!FileUt.checkRoot(saveKey)) {
				saveKey = "/" + saveKey;
			}

			String[] saveKeyTemp = saveKey.split("&");

			if (saveKeyTemp.length > 1) {
				saveKey = saveKeyTemp[0];
			}

			data = this.tileDb.getByte(saveKey);

		}
		return data;
	}

	public TileDB getTileDB() {
		return this.tileDb;
	}

	public BufferedImage getTileImage(String tile) throws Exception {

		boolean existFile = false;

		BufferedImage tileImg = null;

		byte[] imgArray = null;
		if (this.dbPath != null) {

			String saveKey = FilenameUtils.separatorsToUnix(tile);
			synchronized (this.lock) {
				if (this.tileDb == null) {
					this.tileDb = new TileDB(this.dbPath, false, false);
				}
			}

			if (!FileUt.checkRoot(saveKey)) {
				saveKey = "/" + saveKey;
			}

			String[] saveKeyTemp = saveKey.split("&");

			if (saveKeyTemp.length > 1) {
				saveKey = saveKeyTemp[0];
			}

			byte[] imgByte = (byte[]) this.tileDb.getByte(saveKey);

			if (imgByte != null && imgByte.length > 0) {
				tileImg = ImageIO.read(new ByteArrayInputStream(imgByte));
			}

		} else if (this.filePath != null) {
			String tileName = tile;

			if (this.filePath.toLowerCase().indexOf("naver") > -1) {
				tileName = tileName.replaceAll(this.suffixPath, ".png");
			} else if (this.filePath.toLowerCase().indexOf("google") > -1) {
				String vars[] = tileName.replaceAll(this.suffixPath, "").split("/");
				tile = "&x=" + vars[1] + "&y=" + vars[2] + "&z=" + vars[0] + this.suffixPath;
				tileName = vars[0] + "/" + vars[0] + "_" + vars[1] + "_" + vars[2] + ".png";
			} else if (this.filePath.toLowerCase().indexOf("daum") > -1) {
				tileName = tileName.replace(this.suffixPath, ".jpg");
			}

			File fileImg = new File(this.filePath + File.separator + tileName);

			// System.out.println("*tile=" + this.filePath + tileName);

			if (fileImg.exists()) {
				tileImg = ImageIO.read(fileImg);
				existFile = true;
			} else {
				if (this.urlPath != null && this.urlPath.length() > 0) {
					/* wms에서 네이버타일 로컬저장시 사용.. */
					URL url = new URL(this.urlPath + tile);
					System.out.println("*url" + url);
					URLConnection con = url.openConnection();
					con.setRequestProperty("User-Agent",
							"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
					con.setConnectTimeout(1000 * 10);
					con.setReadTimeout(1000 * 10);

					InputStream is;
					BufferedReader br;
					is = con.getInputStream();
					long st = System.currentTimeMillis();

					byte[] data = new byte[2048];
					int len = 0;
					ByteArrayOutputStream bis = new ByteArrayOutputStream();
					while ((len = is.read(data)) > 0) {
						bis.write(data, 0, len);
					}
					is.close();

					byte[] imgByte = bis.toByteArray();
					if (imgByte != null && imgByte.length > 0) {
						tileImg = ImageIO.read(new ByteArrayInputStream(imgByte));
					}

					File path = fileImg.getParentFile();
					if (!path.exists()) {
						path.mkdirs();
					}

					FileOutputStream out = new FileOutputStream(fileImg);
					out.write(imgByte);
					out.flush();
					out.close();
				}
				existFile = false;
			}
		} else if (this.urlPath != null) {
			long st = System.currentTimeMillis();
			URL url = new URL(this.urlPath + tile);

			URLConnection con = url.openConnection();
			con.setConnectTimeout(10000 * 10);
			con.setReadTimeout(10000 * 10);

			byte[] imgByte = IOUtils.toByteArray(con);
			IOUtils.close(con);
			if (imgByte != null && imgByte.length > 0) {
				tileImg = ImageIO.read(new ByteArrayInputStream(imgByte));
			}
			long et = System.currentTimeMillis();
			// System.out.println(this.urlPath + tile+", time = " + (et-st));
		}
		return tileImg;
	}

	public String getTilePath(int level, double x, double y) {
		// String path = "";

		double realTileWidth = this.scales.getScaleRatio(level) * this.imgWidth;

		int xIdx = (int) ((x - this.oriX) / realTileWidth);
		int yIdx = (int) ((y - this.oriY) / realTileWidth);

		String tilePath = this.getTilePath(level, xIdx, yIdx);

		return tilePath;
	}

	/**
	 * 
	 * @param tileName
	 * @return
	 */
	public int getLevel(String tileName) {

		if (this.tileMode == EUM) {

			String name = FileUt.getOnlyName(tileName);

			String[] splits = tileName.split(FileUt.SEPARATOR_LINUX);
			int level = -1;
			if (splits[0].trim().length() > 0) {
				// System.out.println(splits[0]);
				level = Integer.parseInt(splits[0]);
			} else {
				level = Integer.parseInt(splits[1]);
			}

			return level;
		} else if (this.tileMode == REALESTATE) {
			String name = FileUt.getOnlyName(tileName);

			String[] splits = tileName.split(FileUt.SEPARATOR_LINUX);
			int level = -1;

			String levelS = "";

			if (splits[0].trim().length() > 0) {
				levelS = splits[0];
			} else {
				levelS = splits[1];
			}

			// String levelSS = levelS.substring(1, levelS.length());
			String levelSS = levelS;

			level = Integer.parseInt(levelSS);
			return level;
		} else if (this.tileMode == EMAP) {
			String name = FileUt.getOnlyName(tileName);

			String[] splits = tileName.split(FileUt.SEPARATOR_LINUX);
			int level = -1;

			String levelS = "";

			if (splits[0].trim().length() > 0) {
				levelS = splits[0];
			} else {
				levelS = splits[1];
			}

			String levelSS = levelS.substring(1, levelS.length());
			// String levelSS = levelS;

			level = Integer.parseInt(levelSS);
			return level;

		}
		return -1;
	}

	public int getXIdx(String tileName) {

		if (this.tileMode == EUM) {
			String name = FileUt.getOnlyName(tileName);

			String[] splits = name.split("_");
			int xIdx = -1;
			xIdx = Integer.parseInt(splits[0]);

			return xIdx;
		} else if (this.tileMode == REALESTATE) {
			String[] splits = tileName.split(FileUt.SEPARATOR_LINUX);
			int xIdx = -1;
			xIdx = Integer.parseInt(splits[1]);
			return xIdx;
		} else if (this.tileMode == EMAP) {
			String[] splits = tileName.split(FileUt.SEPARATOR_LINUX);
			int xIdx = -1;
			xIdx = Integer.parseInt(splits[1]);
			return xIdx;
		}
		return -1;
	}

	public int getYIdx(String tileName) {
		if (this.tileMode == EUM) {
			String name = FileUt.getOnlyName(tileName);

			String[] splits = name.split("_");
			int yIdx = -1;
			yIdx = Integer.parseInt(splits[1]);
			return yIdx;
		} else if (this.tileMode == REALESTATE) {
			String name = FileUt.getOnlyName(tileName);
			int yIdx = -1;

			yIdx = Integer.parseInt(name);

			return yIdx;
		} else if (this.tileMode == EMAP) {
			String name = FileUt.getOnlyName(tileName);
			int yIdx = -1;

			yIdx = Integer.parseInt(name);

			return yIdx;

		}
		return -1;
	}

	/**
	 * 
	 * @param tileName
	 * @return
	 */
	public Envelope getMbr(String _tileName) {

		if (this.tileMode == EUM) {

			String tileName = _tileName.split("&")[0];

			String name = FileUt.getOnlyName(tileName);

			String[] splits = tileName.split(FileUt.SEPARATOR_LINUX);
			int level = -1;
			if (splits[0].trim().length() > 0) {
				// System.out.println(splits[0]);
				level = Integer.parseInt(splits[0]);
			} else {
				level = Integer.parseInt(splits[1]);
			}

			String[] array = name.split("_");

			double v = this.scales.getScaleRatio(level);

			double x = this.oriX + this.imgWidth * this.scales.getScaleRatio(level) * Integer.parseInt(array[0]);
			double y = this.oriY + this.imgWidth * this.scales.getScaleRatio(level) * Integer.parseInt(array[1]);
			Envelope mbr = new Envelope(x, x + this.imgWidth * this.scales.getScaleRatio(level), y,
					y + this.imgWidth * this.scales.getScaleRatio(level));
			return mbr;
		} else if (this.tileMode == REALESTATE) {

			String tileName = _tileName;

			if (_tileName.startsWith("/")) {
				tileName = _tileName.substring(1, _tileName.length());
			}

			int level = this.getLevel(tileName);
			int xIdx = this.getXIdx(tileName);
			int yIdx = this.getYIdx(tileName);

			double mapWidth = this.imgWidth * this.scales.getScaleRatio(level);
			double mapHeight = this.imgWidth * this.scales.getScaleRatio(level);

			double x = this.oriX + mapWidth * xIdx;
			double y = this.oriY - (mapHeight * yIdx + mapHeight);

			Envelope mbr = new Envelope(x, x + mapWidth, y, y + mapHeight);
			return mbr;

		} else if (this.tileMode == EMAP) {
			int level = this.getLevel(_tileName);
			int xIdx = this.getXIdx(_tileName);
			int yIdx = this.getYIdx(_tileName);

			double mapWidth = this.imgWidth * this.scales.getScaleRatio(level);
			double mapHeight = this.imgWidth * this.scales.getScaleRatio(level);

			double x = this.oriX + mapWidth * xIdx;
			double y = this.oriY - (mapHeight * yIdx + mapHeight);

			Envelope mbr = new Envelope(x, x + mapWidth, y, y + mapHeight);
			return mbr;

		}

		return null;

	}

	public String getSavePath() {

		String path = null;

		if (this.dataMode == FILE) {
			path = this.filePath;
		} else if (this.dataMode == JDBM) {
			path = this.dbPath;
		}
		return path;
	}

	public void clearImage(BufferedImage image) {

		AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f);
		Graphics2D g2d = (Graphics2D) image.createGraphics();
		g2d.setComposite(composite);
		// g2d.setColor(new Color(0, 0, 0, 0));
		g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
	}

	public void stopTileMake() {
		this.runTile = false;
		this.executorService.saveLastJob(this.dbPath + FileUt.SEPERATOR + "write.txt");

		// BlockingQueue<Runnable> qu = this.executorService.getQueue();
		//
		// System.out.println("qu size = " + qu.size());
		//
		// Object[] objs = qu.toArray();
		//
		// Future<?> rl = (Future<?>) objs[objs.length-1];
		// try {
		// JobResource obj = (JobResource) rl.get();
		// System.out.println();
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (ExecutionException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// Iterator it = qu.iterator();
		//
		// while(it.hasNext()) {
		// Future<?> rl = (Future<?>) it.next();
		// try {
		// JobResource obj = (JobResource) rl.get();
		// System.out.println(obj.dataSize);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (ExecutionException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// System.out.println();
		// }

		// String data = (int)level+","+(int)x+","+(int)starty+","+k+",";
		// try {
		// FileUtils.writeStringToFile(ing, data, "utf-8", false);
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

	public void writeLog(String msg) {
		MapLog.getFileLog().debug(msg);
	}

	StringBuffer tileCreateLog = new StringBuffer();

	// Object lock = new Object();

	public void makeTile(JobTile jt, boolean isProcess, boolean tileUpdate, boolean tileMbrUpdate) {

		System.out.println("tmf makeTile start =" + this.name);

		int startLevel = -1;
		int startXIdx = -1;
		int startYIdx = -1;
		int startMbrIdx = -1;

		File ing = new File(this.dbPath + FileUt.SEPERATOR + "write.txt");
		if (tileUpdate && ing.exists()) {
			try {
				String data = FileUtils.readFileToString(ing, "utf-8");
				writeLog("read = " + ing.getAbsolutePath() + ", value=" + data);
				String[] datas = data.split(",");
				startLevel = Integer.parseInt(datas[0].trim());
				startXIdx = Integer.parseInt(datas[1].trim());
				startYIdx = Integer.parseInt(new String(datas[2].getBytes(), "utf-8"));
				startMbrIdx = Integer.parseInt(new String(datas[3].getBytes(), "utf-8"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				if (tileMbrUpdate == false) {
					this.deleteTile();
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// 중단 지점 부터 다시 시작할 경우
		boolean startIdxMode = false;

		if (startLevel > -1 && startXIdx > -1 && startYIdx > -1) {
			startIdxMode = true;
		}

		this.runTile = true;

		if (startIdxMode) {
			writeLog("start update JobTile name = " + jt.getName());
		} else {
			writeLog("start create JobTile name = " + jt.getName());
		}

		Comparator<LevelSet> comparator1 = new Comparator<LevelSet>() {
			@Override
			public int compare(LevelSet mt1, LevelSet mt2) {
				if (mt1.getPriority() > mt2.getPriority()) {
					return 1;
				} else if (mt1.getPriority() < mt2.getPriority()) {
					return -1;
				} else {
					return 0;
				}
			}
		};

		if (jt.isUpdate() != null && !jt.isUpdate()) {
			if (this.dataMode == FILE) {
				File file = new File(this.filePath);
				FileUtils.deleteQuietly(file);
			} else if (this.dataMode == JDBM) {
				File file = new File(this.dbPath);
				FileUtils.deleteQuietly(file);
			}
		} else if (jt.isUpdate() == null || jt.isUpdate()) {
			if (this.dataMode == FILE && this.filePath != null) {
				File file = new File(this.filePath);
				try {
					FileUtils.forceMkdir(file);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (this.dataMode == JDBM && this.dbPath != null) {
				this.setDBPath(this.dbPath, false);
			}
		}

		String drive;

		double totalSpace, usedSpace, freeSpace, usableSpace;
		File[] roots = File.listRoots();
		for (File root : roots) {
			// 루트 드라이버의 절대 경로
			drive = root.getAbsolutePath();

			if (!this.checkDrive(drive)) {
				continue;
			}

			// 하드디스크 전체 용량
			totalSpace = root.getTotalSpace() / Math.pow(1024, 3);
			// 사용가능한 디스크 용량
			usableSpace = root.getUsableSpace() / Math.pow(1024, 3);
			// 여유 디스크 용량
			freeSpace = root.getFreeSpace() / Math.pow(1024, 3);
			// 사용한 디스크 용량
			usedSpace = totalSpace - usableSpace;

			writeLog("하드 디스크 드라이버 : " + drive);
			writeLog("총 디스크 용량 : " + totalSpace + "GB");
			writeLog("사용 가능한 디스크 용량 : " + usableSpace + "GB");
			writeLog("여유 디스크 용량 : " + freeSpace + "GB");
			writeLog("사용한 디스크 용량 : " + usedSpace + "GB");
			writeLog("");
		}

		List<LevelSet> lss = jt.getLevelSet();
		Collections.sort(lss, comparator1);

		int totalCnt = 0;

		// 전체 타일 개수 사이즈 산정

		Vector<Envelope> saveMbr = new Vector();

		for (LevelSet ls : lss) {
			ls.getStartLevel();
			ls.getEndLevel();
			ls.getMbr();

			for (int level = ls.getStartLevel(); level <= ls.getEndLevel(); level++) {

				int layerCnt = this.mapData.getLayerSize(level);

				if (layerCnt == 0) {
					continue;
				}

				List<Mbr> mbrs = ls.getMbr();
				int mbrIdx = 0;
				for (int k = 0; k < mbrs.size(); k++) {

					Mbr mbr = mbrs.get(k);

					saveMbr.add(new Envelope(mbr.getMinx(), mbr.getMaxx(), mbr.getMiny(), mbr.getMaxy()));
				}
			}
		}

		for (LevelSet ls : lss) {
			ls.getStartLevel();
			ls.getEndLevel();
			ls.getMbr();

			for (int level = ls.getStartLevel(); level <= ls.getEndLevel(); level++) {

				int layerCnt = this.mapData.getLayerSize(level);

				if (layerCnt == 0) {
					continue;
				}

				List<Mbr> mbrs = ls.getMbr();
				int mbrIdx = 0;
				for (int k = 0; k < mbrs.size(); k++) {

					Mbr mbr = mbrs.get(k);

					double value = this.scales.getScaleRatio(level);

					double tileWidth = value * (double) this.imgWidth * jt.getDrawCanvasRatio();
					double tileHeight = value * (double) this.imgHeight * jt.getDrawCanvasRatio();

					double startx = (int) ((mbr.getMinx() - this.oriX) / tileWidth);
					double endx = (int) ((mbr.getMaxx() - this.oriX) / tileWidth);
					double starty = -1;
					double endy = -1;

					/*
					 * starty = (int) ((mbr.getMiny() - this.oriY) / tileHeight);
					 * endy = (int) ((mbr.getMaxy() - this.oriY) / tileHeight);
					 */

					if (this.startPointQuadrant == LEFT_BOTTOM) {
						starty = (int) ((mbr.getMiny() - this.oriY) / tileHeight);
						endy = (int) ((mbr.getMaxy() - this.oriY) / tileHeight);
					} else if (this.startPointQuadrant == LEFT_TOP) {
						starty = (int) ((this.oriY - mbr.getMaxy()) / tileHeight);
						endy = (int) ((this.oriY - mbr.getMiny()) / tileHeight);
					}

					System.out.println("level=" + level + ", mbr idx = " + k + ", sx=" + startx + ", ex=" + endx
							+ ", sy=" + starty + ", ey=" + endy);

					if (startIdxMode) {

						if (k == startMbrIdx && level == startLevel && startx <= startXIdx && endx >= startXIdx &&
								starty <= startYIdx && endy >= startYIdx) {

							totalCnt += (((endx - startXIdx) * (endy - starty)) + (endy - startYIdx));
							startIdxMode = false;

							System.out.println("restart startMbrIdx=" + startMbrIdx + ", startLevel=" + startLevel
									+ ",startx=" + startXIdx + ",endy=" + endy);
						}

						continue;

					}

					System.out.println("add mbridx=" + k);
					totalCnt += ((endx - startx + 1) * (endy - starty));
					mbrIdx++;
				}
			}
		}

		if (startLevel > -1 && startXIdx > -1 && startYIdx > -1) {
			startIdxMode = true;
		}

		Envelope[] saveMbrArray = new Envelope[saveMbr.size()];
		for (int i = 0; i < saveMbr.size(); i++) {
			saveMbrArray[i] = saveMbr.get(i);
		}

		if (executorService != null) {
			if (!executorService.isShutdown()) {
				executorService.shutdown();
			}
			executorService = null;
		}

		executorService = new TileThreadPoolExecutor(threadCnt,
				threadCnt, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), this.lock);
		executorService.ip = "localhost";
		executorService.port = 123;

		long startTime = System.currentTimeMillis();
		// File pfile, Logger _log, int _unit

		// JobMonitorLog jobLog = null;
		if (TileMapFactory2.jobLog != null) {
			TileMapFactory2.jobLog = null;

		}
		TileMapFactory2.jobLog = new JobMonitorLog(new File(this.getSavePath()), MapLog.getFileLog(), 0);
		// TileMapMng.jobLog = new JobMonitorLog(new File(this.getSavePath()),
		// MapLog.getFileLog(), -2);

		TileMapFactory2.jobLog.setTotalCnt(totalCnt);
		TileMapFactory2.jobLog.setStartTime(startTime);

		writeLog("start JobTile name = " + jt.getName() + ", total Tile Cnt = " + totalCnt);
		int DrawCanvasRatio = jt.getDrawCanvasRatio();

		BufferedImage image = new BufferedImage(this.imgWidth * DrawCanvasRatio, this.imgHeight * DrawCanvasRatio,
				BufferedImage.TYPE_INT_ARGB);// TYPE_INT_RGB
		BufferedImage subImage = new BufferedImage(this.imgWidth, this.imgHeight, BufferedImage.TYPE_INT_ARGB);// TYPE_INT_RGB

		int nowCnt = 0;
		for (LevelSet ls : lss) {
			if (this.runTile == false) {
				break;
			}

			ls.getStartLevel();
			ls.getEndLevel();
			ls.getMbr();

			for (int level = ls.getStartLevel(); level <= ls.getEndLevel(); level++) {
				
				
//				if(level > 5) {
//					continue;
//				}
				
				if (this.runTile == false) {
					break;
				}

				int layerCnt = this.mapData.getLayerSize(level);

				if (layerCnt == 0) {
					continue;
				}

				final int runLevel = level;

				List<Mbr> mbrs = ls.getMbr();
				// int mbrIdx = 0;
				for (int k = 0; k < mbrs.size(); k++) {
					if (this.runTile == false) {
						break;
					}

					Mbr mbr = mbrs.get(k);
					double value = this.scales.getScaleRatio(level);

					double tileWidth = value * (double) this.imgWidth * DrawCanvasRatio;
					double tileHeight = value * (double) this.imgHeight * DrawCanvasRatio;

					double startx = (int) ((mbr.getMinx() - this.oriX) / tileWidth);
					double endx = (int) ((mbr.getMaxx() - this.oriX) / tileWidth);

					double starty = -1;
					double endy = -1;
					/*
					 * starty = (int) ((mbr.getMiny() - this.oriY) / tileHeight);
					 * endy = (int) ((mbr.getMaxy() - this.oriY) / tileHeight);
					 */
					if (this.startPointQuadrant == LEFT_BOTTOM) {
						starty = (int) ((mbr.getMiny() - this.oriY) / tileHeight);
						endy = (int) ((mbr.getMaxy() - this.oriY) / tileHeight);
					} else if (this.startPointQuadrant == LEFT_TOP) {
						starty = (int) ((this.oriY - mbr.getMaxy()) / tileHeight);
						endy = (int) ((this.oriY - mbr.getMiny()) / tileHeight);
					}

					for (double x = startx; x <= endx; x++) {
						if (this.runTile == false) {
							break;
						}

						for (double y = starty; y <= endy; y++) {

							if (this.runTile == false) {
								break;
							}

							if (startIdxMode) {

								if (k == startMbrIdx && level == startLevel && x == startXIdx && y == startYIdx) {
									startIdxMode = false;
								}

								if (startIdxMode == false) {

								} else {
									continue;
								}

							}

							final int runX = (int) x;
							final int runY = (int) y;

							double minx = this.oriX + x * tileWidth;
							double maxx = this.oriX + x * tileWidth + tileWidth;

							double miny = -1;
							double maxy = -1;

							/*
							 * miny = this.oriY + y * tileHeight;
							 * maxy = this.oriY + y * tileHeight + tileHeight;
							 */
							if (this.startPointQuadrant == LEFT_BOTTOM) {
								miny = this.oriY + y * tileHeight;
								maxy = this.oriY + y * tileHeight + tileHeight;
							} else if (this.startPointQuadrant == LEFT_TOP) {
								miny = this.oriY - (y * tileHeight) - tileHeight;
								maxy = this.oriY - (y * tileHeight);
							}

							Envelope tileEnv = new Envelope(minx, maxx, miny, maxy);

							JobResource jr = new JobResource();
							jr.setRunLevel(runLevel);
							jr.setRunX(runX);
							jr.setRunY(runY);
							jr.setTileEnv(tileEnv);
							jr.setTmf(this);
							jr.setDrawCanvasRatio(DrawCanvasRatio);
							jr.setTmsName(jt.getTileMapServiceName());
							jr.setProcess(isProcess);
							jr.setSaveMbrs(saveMbrArray);
							jr.mbrIdx = k;

							synchronized (lock) {
								if (executorService.getQueue().size() > 100) {
									try {
										lock.wait();
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}

							executorService.run(jr);

							nowCnt += 1;

							String writer = TileMapFactory2.jobLog.getNowJob(nowCnt);

							if (writer != null) {

								this.writeLog(writer);

								this.writeLog("작업 축척 레벨 = " + runLevel + ", 타일 영역 = " + tileEnv.toString() + ", xidx="
										+ runX + ", yidx=" + runY + ", nowCnt=" + nowCnt + ", totalCnt=" + totalCnt
										+ ", 진행율=" + (int) ((((float) nowCnt / (float) totalCnt)) * 100.0)
										+ " %, mbridx=" + k
										+ ", active cnt = " + executorService.getActiveCount()
										+ ", queue cnt = " + executorService.getQueue().size());

							}

							// System.out.println("active cnt = " + executorService.getActiveCount()
							// +", queue cnt = " + executorService.getQueue().size());

							if (nowCnt == totalCnt) {
								System.out.println("for loop end = " + (int) level + "," + (int) x + "," + (int) starty
										+ "," + k + ",");
							}

						}

						// if(nowCnt > 100) {
						//
						// stopTileMake();
						// }
						// this.dbPath
						// File ing = new File(this.dbPath+FileUt.SEPERATOR+"write.txt");
						String data = (int) level + "," + (int) x + "," + (int) starty + "," + k + ",";
						try {
							FileUtils.writeStringToFile(ing, data, "utf-8", false);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						// writeStringToFile(File file, String data, Charset charset, boolean append)

					}

				}
			}
		}

		long stopSt = System.currentTimeMillis();

		while (true) {
			long stopEt = System.currentTimeMillis();
			try {
				System.out.println("종료 대기 중,  " + (stopEt - stopSt) / 1000 / 60 + " 분" + ", Active Cnt = "
						+ executorService.getActiveCount() + ", queue size = " + executorService.getQueue().size());
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (executorService.getActiveCount() == 0) {
				break;
			}
		}

		executorService.shutdown();

		if (this.tileDb != null) {
			this.tileDb.commit();

			// if(tileMbrUpdate) {
			// this.tileDb.defrag();
			// }

			this.tileDb.close();
		}

		MapLog.getFileLog().debug("end JobTile name = " + jt.getName() + ", total Tile Cnt = " + totalCnt);

	}

	public boolean isSaveMbrInterect(Envelope[] mbrs, Envelope tileMbr) {
		for (Envelope saveEnv : mbrs) {
			if (saveEnv.intersects(tileMbr)) {
				return true;
			}
		}
		return false;
	}

	public GenericObjectPool getObjectPool(int width, int height) {
		GenericObjectPoolConfig gop = new GenericObjectPoolConfig();
		int maxThreadCnt = this.getThreadCnt();
		gop.setMaxTotal(maxThreadCnt + 5);
		gop.setMaxWaitMillis(2000);
		GenericObjectPool newobjectPool = new GenericObjectPool<BufferedImage>(
				new ImagePool(width, height), gop);
		return newobjectPool;
	}

	/*
	 * public void createImagePool(int cnt, int timeout, Vector<Integer> imgSizes) {
	 * 
	 * this.imgObjPools = new HashMap<Integer, GenericObjectPool<BufferedImage>>();
	 * 
	 * for (Integer width : imgSizes) { GenericObjectPoolConfig gop = new
	 * GenericObjectPoolConfig(); gop.setMaxTotal(cnt);
	 * gop.setMaxWaitMillis(timeout); GenericObjectPool objectPool = new
	 * GenericObjectPool<BufferedImage>(new ImagePool(width, width), gop);
	 * this.imgObjPools.put(width, objectPool); }
	 * 
	 * }
	 */
	public HashMap<String, byte[]> getTileImages(int level, int x, int y, int DrawCanvasRatio, Envelope tileEnv,
			BufferedImage _image, BufferedImage _subImage, boolean debug) throws Exception {

		HashMap<String, byte[]> tiles = new HashMap();

		BufferedImage image = _image;

		// boolean test = true;
		//
		// BufferedImage imageTemp = null;
		//
		// if(test) {
		// imageTemp = new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_ARGB);
		// }

		GenericObjectPool<BufferedImage> imageObjectPool = null;

		int imageObjectPoolSize = 0;

		BufferedImage subImage = _subImage;

		GenericObjectPool<BufferedImage> subImageObjectPool = null;

		imageObjectPoolSize = this.imgHeight * DrawCanvasRatio;

		Context context = this.mapData.getMapContext(level);

		ScreenCoordUtil scu = new ScreenCoordUtil();
		Dimension dimension = new Dimension(this.imgHeight * DrawCanvasRatio, this.imgHeight * DrawCanvasRatio);
		scu.setData(dimension, tileEnv);

		Vector<Layer> layers = context.loadMemLayers(tileEnv, scu);

		if (layers.size() == 0) {
			return tiles;
		}

		try {
			if (_image == null) {
				synchronized (this.imgObjPools) {
					if (!this.imgObjPools.containsKey(this.imgHeight * DrawCanvasRatio)) {

						// GenericObjectPoolConfig gop = new GenericObjectPoolConfig();
						//
						// int maxThreadCnt = this.getThreadCnt();
						// gop.setMaxTotal(maxThreadCnt);
						// gop.setMaxWaitMillis(2000);
						// GenericObjectPool newobjectPool = new GenericObjectPool<BufferedImage>(
						// new ImagePool(this.imgHeight * DrawCanvasRatio, this.imgHeight *
						// DrawCanvasRatio), gop);

						GenericObjectPool newobjectPool = this.getObjectPool(this.imgHeight * DrawCanvasRatio,
								this.imgHeight * DrawCanvasRatio);

						this.imgObjPools.put(this.imgHeight * DrawCanvasRatio, newobjectPool);
						imageObjectPool = newobjectPool;
					} else {
						imageObjectPool = this.imgObjPools.get(this.imgHeight * DrawCanvasRatio);
						int maxThreadCnt = this.getThreadCnt();
						if (imageObjectPool.getMaxTotal() != (maxThreadCnt + 5)) {
							imageObjectPool.clear();
							imageObjectPool.close();

							this.imgObjPools.remove(this.imgHeight * DrawCanvasRatio);

							// GenericObjectPoolConfig gop = new GenericObjectPoolConfig();
							// gop.setMaxTotal(maxThreadCnt);
							// gop.setMaxWaitMillis(2000);
							// GenericObjectPool newobjectPool = new GenericObjectPool<BufferedImage>(
							// new ImagePool(this.imgHeight * DrawCanvasRatio, this.imgHeight *
							// DrawCanvasRatio), gop);

							GenericObjectPool newobjectPool = this.getObjectPool(this.imgHeight * DrawCanvasRatio,
									this.imgHeight * DrawCanvasRatio);

							this.imgObjPools.put(this.imgHeight * DrawCanvasRatio, newobjectPool);
							imageObjectPool = newobjectPool;
						}
					}
					image = imageObjectPool.borrowObject();
				}

			}

			if (DrawCanvasRatio > 1 && _subImage == null) {
				synchronized (this.imgObjPools) {

					if (!this.imgObjPools.containsKey(this.imgWidth)) {
						// GenericObjectPoolConfig gop = new GenericObjectPoolConfig();
						// int maxThreadCnt = this.getThreadCnt();
						// gop.setMaxTotal(maxThreadCnt);
						// gop.setMaxWaitMillis(2000);
						// GenericObjectPool newobjectPool = new GenericObjectPool<BufferedImage>(
						// new ImagePool(this.imgWidth, this.imgHeight), gop);

						GenericObjectPool newobjectPool = this.getObjectPool(this.imgWidth, this.imgHeight);

						this.imgObjPools.put(this.imgWidth, newobjectPool);
						subImageObjectPool = newobjectPool;
					} else {
						subImageObjectPool = this.imgObjPools.get(this.imgWidth);
						int maxThreadCnt = this.getThreadCnt();

						if (subImageObjectPool.getMaxTotal() != (maxThreadCnt + 5)) {
							subImageObjectPool.clear();
							subImageObjectPool.close();
							this.imgObjPools.remove(this.imgWidth);

							GenericObjectPool newobjectPool = this.getObjectPool(this.imgWidth, this.imgHeight);
							this.imgObjPools.put(this.imgWidth, newobjectPool);
							subImageObjectPool = newobjectPool;
						}
					}
					subImage = subImageObjectPool.borrowObject();
				}
			}

			// MapContext mc = this.mapData.getMapContext(level);

			// if(test) {
			// image = imageTemp;
			// }

			context.drawMap(image.createGraphics(), tileEnv, scu, layers, debug);

			// this.mapData.getMap(level, tileEnv, image);

			// context.drawMap(img.createGraphics(), mbr, scu, layers);
			if (DrawCanvasRatio > 1) {
				// TileServiceMng.DEBUG = false;
				Graphics2D g = subImage.createGraphics();

				int startXName = (int) (x * DrawCanvasRatio);
				int startYName = (int) (y * DrawCanvasRatio);

				for (int xx = 0; xx < DrawCanvasRatio; xx++) {
					for (int yy = 0; yy < DrawCanvasRatio; yy++) {
						// 이미지의 원점은 좌상단이므로 y축에 변환이 필요함.

						this.clearImage(subImage);
						// 이미지의 원점은 좌상단이므로 y축에 변환이 필요함.
						int x1 = xx * this.imgWidth;
						int y1 = -1;
						if (startPointQuadrant == LEFT_TOP) {
							y1 = yy * this.imgHeight;
						} else {
							y1 = this.imgWidth * DrawCanvasRatio - yy * this.imgHeight - this.imgHeight;
						}

						g.drawImage(image, 0, 0, this.imgWidth, this.imgHeight, x1, y1, x1 + this.imgWidth,
								y1 + this.imgHeight, null);

						String tilePath = "/" + this.getTilePath(level, startXName + xx, startYName + yy);

						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						if (imageBit == 8) {
							BufferedImage convImg = null;
							PngQuant pngQuant = new PngQuant();
							try {
								// pngQuant.setMaxColors(100);

								//pngQuant.setMaxColors(100);
								//pngQuant.setQuality(30, 50);
								
								pngQuant.setMaxColors(TileServiceMng.colorCnt);
								pngQuant.setSpeed(1);
								

								if (subImage != null) {
									convImg = pngQuant.getRemapped(subImage);
									if (convImg != null) {
										ImageIO.write(convImg, "png", bos);
									} else {
										
										PngQuant errorQuant = new PngQuant();
										
										try {
											errorQuant.setMaxColors(100);
											
											convImg = errorQuant.getRemapped(subImage);
											
											if(convImg != null) {
												ImageIO.write(convImg, "png", bos);
											}
											else {
												System.out.println("tilePath = " + tilePath + ", convImg is null DrawCanvasRatio="+DrawCanvasRatio);	
											}
										}
										catch(Exception e) {
											e.printStackTrace();
										} finally {
											errorQuant.close();
										}
										
									}
								} else {
									System.out.println("tilePath = " + tilePath + ", subImage is null");
								}
							} catch (Exception e) {
								// this.get

								Envelope mbr = this.getMbr(tilePath);

								System.out.println("image save error = " + tilePath +
										", imgW=" + convImg.getWidth() + ", imgH=" + convImg.getHeight() +
										", x=" + mbr.getMinX() + ",y=" + mbr.getMinY());
								e.printStackTrace();

							} finally {
								
								pngQuant.close();
							}

						} else {
							ImageIO.write(subImage, "png", bos);
						}

						bos.flush();
						byte[] data = bos.toByteArray();
						bos.close();

						boolean insert = true;

						if (insert) {
							tiles.put(tilePath, data);
						}
					}
				}
				// TileServiceMng.DEBUG = true;
			} else {

				String tilePath = "/" + this.getTilePath(level, x, y);

				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				// imageBit = 32;
				if (imageBit == 8) {

					PngQuant pngQuant = null;
					try {
						pngQuant = new PngQuant();
						
						
						//pngQuant.setMaxColors(TileServiceMng.colorCnt);
						pngQuant.setMaxColors(TileServiceMng.colorCnt);
						pngQuant.setSpeed(1);
						//pngQuant.setQuality(30, 80);
						//pngQuant.setMinPosterization(1);
						//pngQuant.setQuality(50);
						//pngQuant.setQuality(50, 90);
						
						//System.out.println("color="+countColorsUsingSet(image));
						
						//pngQuant.setMaxColors(20);
						//pngQuant.setSpeed(1);
						//pngQuant.setSpeed(imageObjectPoolSize);
						//pngQuant.setQuality(20, 30);
						
//						BufferedImage tmpImg = new BufferedImage(512,512, BufferedImage.TYPE_USHORT_555_RGB);
//						
//						tmpImg.getGraphics().drawImage(image, 0, 0, null);

						BufferedImage convImg = pngQuant.getRemapped(image);
						
						if(convImg != null) {
							ImageIO.write(convImg, "png", bos);	
						}
						else {
							PngQuant errorQuant = new PngQuant();
							
							try {
								errorQuant.setMaxColors(100);
								
								convImg = errorQuant.getRemapped(image);
								
								if(convImg != null) {
									ImageIO.write(convImg, "png", bos);
								}
								else {
									System.out.println("tilePath = " + tilePath + ", convImg is null DrawCanvasRatio=1");	
								}
							}
							catch(Exception e) {
								e.printStackTrace();
							} finally {
								errorQuant.close();
							}

						}
						
					} catch (Exception e) {
						e.printStackTrace();
						ImageIO.write(image, "png", bos);
					} finally {
						if (pngQuant != null) {
							pngQuant.close();
						}
					}

				} else {
					ImageIO.write(image, "png", bos);
				}

				bos.flush();
				byte[] data = bos.toByteArray();
				bos.close();

				boolean insert = true;

				if (insert) {
					tiles.put(tilePath, data);
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (_image == null) {

				if (image == null) {
					System.out.println(
							"error --------------------- img is null, getNumActive()=" + imageObjectPool.getNumActive()
									+ ", getMaxTotal()=" + imageObjectPool.getMaxTotal());
				}

				if (imageObjectPoolSize != image.getWidth()) {
					System.out.println(
							"error ---------------------" + imageObjectPoolSize + ", img.width=" + image.getWidth());
				}
				if (image != null) {

					// if(test == false) {
					imageObjectPool.returnObject(image);
					// }
				}
			}
			if (DrawCanvasRatio > 1 && _subImage == null && subImage != null) {
				subImageObjectPool.returnObject(subImage);
			}
		}

		return tiles;
	}
	
	public static int countColorsUsingSet(BufferedImage image) {
	    int width = image.getWidth();
	    int height = image.getHeight();
	    int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);

	    Set<Integer> uniqueColors = new HashSet<>();
	    for (int pixel : pixels) {
	        uniqueColors.add(pixel); // ARGB 값이 Integer로 저장됨
	    }
	    
	    return uniqueColors.size();
	}

	public int getThreadCnt() {

		return this.threadCnt;

	}

	public void setThreadCnt(int cnt) {

		// return this.threadCnt ;
		this.threadCnt = cnt;

	}

	public boolean checkDrive(String drive) {
		if (this.dataMode == FILE) {
			try {
				String drive_temp = FilenameUtils.separatorsToUnix(drive);
				String dbPath_temp = FilenameUtils.separatorsToUnix(this.filePath);
				boolean result = FilenameUtils.directoryContains(drive_temp, dbPath_temp);
				return result;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (this.dataMode == JDBM) {
			try {
				String drive_temp = FilenameUtils.separatorsToUnix(drive);
				String dbPath_temp = FilenameUtils.separatorsToUnix(this.dbPath);
				boolean result = FilenameUtils.directoryContains(drive_temp, dbPath_temp);
				return result;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// if(this.dbPath.indexOf(drive) > -1) {
			// return true;
			// }
		}
		return false;
	}

	public static void main(String[] args) {
		TileMapFactory2 tileMng = TileMapFactory2.initEMap();
		BufferedImage image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
		// tileMng.draw(null, FILE, null, EUM, EMAP);
	}

	public void close() {
		if (this.tileDb != null) {
			this.tileDb.close();
		}
	}

}
