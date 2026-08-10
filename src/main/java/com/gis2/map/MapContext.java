package com.gis2.map;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.imageio.ImageIO;

import com.data.file.FileLayer;
import com.data.util.MapLog;
import com.index.rtree.IRect;

import com.gis.protocol.freegis3.PolygonStyle;
import com.gis.protocol.freegis3.RasterStyle;
import com.gis2.map.render.GTRendererPool;

//import com.gis.engine.admin.folder.RootFolder;
//import com.gis.engine.datamanager.umdfile.UMDBlock;
//import com.gis.engine.gis.rtree.IRect;
//import com.gis.engine.util.MapLog;
//import com.gis.engine.util.ShapeTypes;

import com.gis2.map.render.GridObjectRender;
import com.gis2.map.render.IRender;
import com.gis2.map.render.ImagePool;
import com.gis2.map.render.OverlapMng;
import com.gis2.map.render.PointRender;
import com.gis2.map.render.PolygonRender;
import com.gis2.map.render.PolylineRender;
import com.gis2.map.render.TextRender;
//import com.gis2.map.render.TextStroke; // TextStroke 클래스 없음 (TextStroke3 존재)
import com.gis2.map.render.java2D.Java2DConverter;
import com.gis2.map.render.java2D.Java2DConverter.PointConverter;
import com.gis2.map.render.OverlapMng.Overlap;
import com.gis2.map.style.BasicStyleExtend;
import com.gis2.map.style.PointStyle;
import com.gis2.map.style.QueryStyle;
import com.gis.map.Context;
import com.gis.map.Layer;
import com.gis.map.MapData;
import com.gis.map.render.RasterRender;
//import com.gis2.operation.Raster;
import com.gis.projection.ScreenCoordUtil;

import com.gis2.storage.StorageMng;
import com.gis2.storage.TileServiceMng;
import com.sun.corba.se.spi.orb.Operation;

import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.processing.CoverageProcessor;
import org.geotools.coverage.processing.Operations;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.filter.AndImpl;
import org.geotools.filter.SortByImpl;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.image.ImageWorker;
import org.geotools.map.GridCoverageLayer;
import org.geotools.map.MapContent;
import org.geotools.map.MapViewport;
import org.geotools.referencing.CRS;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.RendererUtilities;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.StyleBuilder;
import org.locationtech.jts.algorithm.CGAlgorithms;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
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
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.GeometryType;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.sort.SortOrder;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

//import com.vividsolutions.jump.feature.Feature;
//import com.vividsolutions.jump.feature.FeatureSchema;
//import com.vividsolutions.jump.workbench.ui.renderer.java2D.Java2DConverter;
//import com.vividsolutions.jump.workbench.ui.renderer.java2D.Java2DConverter.PointConverter;
import com.vividsolutions.jump.workbench.ui.renderer.style2.BasicStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

//public static interface PointConverter {
//	public Point2D toViewPoint(Coordinate modelCoordinate)
//		throws NoninvertibleTransformException;
//}

public class MapContext implements Context{

	// Java2DConverter j2d;

	Vector<Layer> layerList = new Vector<Layer>();

	Envelope fullEnvelope = null;
	Envelope limitedEnvelope = null;

	// ScreenCoordUtil scu = null;

	Color backgroundColor = null;

	public static HashMap<String, BufferedImage> imageStorage = new HashMap();

	PointRender poinRender = new PointRender();
	GridObjectRender gridRender = new GridObjectRender();
	PolygonRender polygonRender = new PolygonRender();
	PolylineRender polylineRender = new PolylineRender();
	TextRender textRender = new TextRender();
	//GenericObjectPool<GTRenderer> gtRendererPool = null;
	//GenericObjectPool<BufferedImage> imagePool = null;
	// HashMap<Integer, GenericObjectPool<BufferedImage>> imagePools = new
	// HashMap<Integer, GenericObjectPool<BufferedImage>>();

	public String foreignMode = null;

	// public StorageMng sm = null;

	public long date = -1;

	public boolean largeFontMode = false;

	public float maxFontSize = Float.MIN_VALUE;

	// public static boolean debugOverlap = false;
	// IRender render;

	public boolean isProcess = false;

	public boolean makeRasterTile = false;

	private int level = -1;

	GeometryFactory gft = new GeometryFactory();

	public void setMakeRasterTile(boolean makeRasterTile) {
		this.makeRasterTile = makeRasterTile;
	}

	public MapContext(String foreignMode) {
		this.foreignMode = foreignMode;
		// this.backgroundColor = Color.white;
		this.backgroundColor = new Color(190, 232, 255);
		// this.sm = sm;

//		this.gtRendererPool = this.getGTRendererPool();

	}

	public void setLevel(int level_) {
		this.level = level_;
		this.poinRender.setLevel(this.level);
	}

	public int getLevel() {
		return this.level;
	}
	
	/*
	public int getMaxThread() {

		int maxThreadCnt = ServiceMng.MaxTileThread;
		Vector<String> localIps = ServiceMng.getLocalServerIps();
		if (ServiceMng.gpf != null) {
			Replication rplt = ServiceMng.gpf.getReplication();
			if (rplt != null) {
				List<ReplicationServer> lss = rplt.getServer();
				for (String localIp : localIps) {
					for (ReplicationServer ls : lss) {
						if (localIp.equals(ls.getIp())) {
							maxThreadCnt = ls.getTileJobThread();
						}
					}
				}
			} else {
				maxThreadCnt = ServiceMng.MaxTileThread;
			}
		} else {
			maxThreadCnt = ServiceMng.MaxTileThread;
		}


		return -1;
	}

	public GenericObjectPool<BufferedImage> getImagePool(int width, int height) {
		int maxThreadCnt = this.getMaxThread();

		GenericObjectPoolConfig<BufferedImage> gop = new GenericObjectPoolConfig<BufferedImage>();
		gop.setMaxTotal(maxThreadCnt + 20);
		gop.setMaxWaitMillis(2000);

		GenericObjectPool<BufferedImage> imagePool = new GenericObjectPool<BufferedImage>(new ImagePool(width, height),
				gop);
		return imagePool;
	}

	public GenericObjectPool<GTRenderer> getGTRendererPool() {
		int maxThreadCnt = this.getMaxThread();

		GenericObjectPoolConfig<GTRenderer> gop = new GenericObjectPoolConfig<GTRenderer>();
		gop.setMaxTotal(maxThreadCnt + 20);
		gop.setMaxWaitMillis(2000);

		GenericObjectPool<GTRenderer> gtRendererPool = new GenericObjectPool<GTRenderer>(new GTRendererPool(), gop);
		return gtRendererPool;
	}
	*/
	public void setLimitedEnvelope(Envelope limited) {
		this.limitedEnvelope = limited;
	}

	/*
	 * public void roadImage() { String file = RootFolder.DIR_ICON;
	 * 
	 * File datafile = new File(file);
	 * 
	 * 
	 * File vendors[] = datafile.listFiles();
	 * 
	 * for (File vendor : vendors) { if (vendor.isDirectory() == false) continue;
	 * 
	 * File[] files = vendor.listFiles();
	 * 
	 * String path = RootFolder.DIR_ICON + vendor.getName() + "/"; //
	 * maplist.append("\"").append(vendor.getName()).append("\":[");
	 * 
	 * File imagePath = new File(path); File[] imagePaths = imagePath.listFiles();
	 * for (int i = 0; i < imagePaths.length; i++) { // String imageFullPath =
	 * path+this.ss.symbolName; // File imageFile = new File(imageFullPath); File
	 * imageFile = imagePaths[i]; if (imageFile.exists() == true) { BufferedImage
	 * gi; try { gi = ImageIO.read(imageFile); // String imageName = iamge } catch
	 * (Exception e) { e.printStackTrace(); } } } }
	 * 
	 * }
	 */

	public boolean addLayer(int index, Layer layer) {
		if (layerList.contains(layer)) {
			return false;
		}

		layerList.add(index, layer);

		// if (this.date < layer.date) {
		// this.date = layer.date;
		// }

		// if (fullEnvelope != null) {
		// this.fullEnvelope.expandToInclude(layer.getBounds());
		// } else {
		// Envelope env = layer.getBounds();
		// this.fullEnvelope = new Envelope(env.getMinX(), env.getMaxX(), env.getMinY(),
		// env.getMaxY());
		// }

		return true;
	}

	public void sort() {
		Collections.sort(this.layerList, Collections.reverseOrder());
	}

	public Vector<Layer> getLayers() {
		return this.layerList;
	}

	public void clearMap(Graphics2D _g) {
		_g.setBackground(this.backgroundColor);
		_g.setColor(this.backgroundColor);
		int tileSize = com.gis2.storage.TileServiceMng.hdMode ? 512 : 256;
		_g.fillRect(0, 0, tileSize, tileSize);

	}

	String serviceName = "";

	/**
	 * 레이어에 스타일 등록
	 * 
	 * @param styles
	 */
	public void setStyle(HashMap<String, Style> styles) {
		for (int i = 0; i < this.layerList.size(); i++) {
			Layer layer = this.layerList.get(i);
			
			if (layer instanceof VectorLayer) {

				VectorLayer vl = (VectorLayer) layer;

				Style style = styles.get(vl.getStyleName());
				if (style instanceof PointStyle) {
					PointStyle ps = (PointStyle) style;

					if (ps.getFontSize() > this.maxFontSize) {
						this.maxFontSize = ps.getFontSize();
					}
				}

				vl.setStyle(style);

			}			
			/*
			if (layer instanceof VectorLayer) {

				VectorLayer vl = (VectorLayer) layer;

				Style style = styles.get(vl.getStyleName());
				if (style instanceof PointStyle) {
					PointStyle ps = (PointStyle) style;

					if (ps.getFontSize() > this.maxFontSize) {
						this.maxFontSize = ps.getFontSize();
					}
				}

				vl.setStyle(style);

			} else if (layer instanceof RasterLayer) {
				RasterLayer rl = (RasterLayer) layer;

				Style style = styles.get(rl.getStylename());

				rl.setStyle(style);
			}
			*/
		}
	}

	public void setServiceName(String serviceName_) {
		this.serviceName = serviceName_;
		this.poinRender.setServiceName(serviceName);
	}

	public Vector<Layer> loadMemLayers(Envelope __e, ScreenCoordUtil _scu, CoordinateReferenceSystem _viewCRS,
			String epsg, Vector<Layer> lys) {

		Vector<Layer> layers = new Vector();

		CoordinateReferenceSystem viewCRS = _viewCRS;

		int totalObjSize = 0;

		Envelope viewEnv = null;

		for (int i = 0; i < this.layerList.size(); i++) {

			MathTransform toFileTransform = null;
			MathTransform toViewTransform = null;

			boolean isSort = false;
			Object[] objsKey = null;

			Layer layerTemp = this.layerList.get(i);

			if (layerTemp instanceof VectorLayer) {
				VectorLayer layer = (VectorLayer) layerTemp;
				
				
				
				
//				if (layer.getUMDReader() == null || layer.getUMDReader().getFreeLayer() == null) {
//					continue;
//				}
//				FreeLayer fLayer = layer.getUMDReader().getFreeLayer();
				
				if (layer.getFileReader() == null) {
					System.out.println("loadMemLayer layerName = " + layer.getName() + " reader  is null");
					continue;
				}

				FileLayer fLayer = layer.getFileReader();
				
				double objCnt = fLayer.getObjectSize();

				if (objCnt == 0) {
					// if (layer.style instanceof PointStyle) {
					// System.out.println("loadMemLayer layerName = " + layer.name+", cnt = " + 0);
					// }
					continue;
				}

				if (epsg != null && _viewCRS != null) {
					int fileEPSG = -1;
					fileEPSG = layer.getEPSGCode();
					if (fileEPSG == -1) {
						if (!CRS.equalsIgnoreMetadata(fLayer.getSimpleFeatureType().getCoordinateReferenceSystem(),
								viewCRS)) {
							try {
								toFileTransform = CRS.findMathTransform(viewCRS,
										fLayer.getSimpleFeatureType().getCoordinateReferenceSystem());
								toViewTransform = CRS.findMathTransform(
										fLayer.getSimpleFeatureType().getCoordinateReferenceSystem(), viewCRS);

							} catch (NoSuchAuthorityCodeException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (FactoryException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					} else {
						String fileCode = "EPSG:" + fileEPSG;
						if (!fileCode.equals(epsg)) {

							try {
								String code = epsg.split(":")[1];

								toFileTransform = CRS.findMathTransform(viewCRS,
										fLayer.getSimpleFeatureType().getCoordinateReferenceSystem());
								toViewTransform = CRS.findMathTransform(
										fLayer.getSimpleFeatureType().getCoordinateReferenceSystem(), viewCRS);

							} catch (NoSuchAuthorityCodeException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (FactoryException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
					}
				}

				if (toFileTransform != null) {

					try {
						viewEnv = JTS.transform(__e, toFileTransform);
					} catch (TransformException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				} else {
					viewEnv = __e;
				}

				VectorLayer copyLayer = new VectorLayer();

				layer.clone(copyLayer);

				VectorLayer regLayer = this.isLikeFreeLayerAndStyle(layers, copyLayer);

				if (regLayer != null) {
					copyLayer.setFeatures(regLayer.getFeatures());
				}

				ListFeatureCollection lfc = null;

				try {

					VectorLayer likeLayer = null;

					if (lys != null) {
						for (int m = 0; m < lys.size(); m++) {

							VectorLayer ly = (VectorLayer) lys.get(m);

							if (ly.getStyleName() != null) {

								if (ly.getName().equals(copyLayer.getName())
										&& ly.getStyleName().equals(copyLayer.getStyleName())) {
									likeLayer = (VectorLayer) ly;
									break;
								}

							} else {
								if (ly.getName().equals(copyLayer.getName())) {
									likeLayer = (VectorLayer) ly;
									break;
								}

							}

						}
					}
					if (likeLayer == null) {
						Envelope drawEnv = new Envelope(viewEnv);

						if (layer.getStyle() instanceof PointStyle) {
							PointStyle ps = (PointStyle) layer.getStyle();
							int expansion = 200;
							if (this.largeFontMode) {
								expansion = 400;
							} else {
								expansion = 200;
							}
							int temp = (int) _scu.getScrXToMapX(expansion) - (int) _scu.getScrXToMapX(0);
							drawEnv.expandBy(temp * 2, temp * 2);
						}

						lfc = (ListFeatureCollection) fLayer.getFeatures(drawEnv.getMinX(), drawEnv.getMinY(),
								drawEnv.getMaxX(), drawEnv.getMaxY());
					} else {
						lfc = likeLayer.getFeatures();

						if (likeLayer.getStyleName() != null) {
							copyLayer.setStyle(likeLayer.getStyle());
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

				if (lfc == null || lfc.size() == 0) {
					continue;
				} else {
					totalObjSize += lfc.size();
				}

				if (toViewTransform != null) {
					Iterator it = lfc.iterator();

					while (it.hasNext()) {
						SimpleFeature sf = (SimpleFeature) it.next();
						try {
							if (sf.getDefaultGeometry() != null) {

								Geometry newConv = JTS.transform((Geometry) sf.getDefaultGeometry(), toViewTransform);
								sf.setDefaultGeometry(newConv);

							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				}

				Iterator it = lfc.iterator();
				while(it.hasNext()) {
					SimpleFeature sf = (SimpleFeature)it.next();
					String korName = (String)sf.getAttribute("lb_name_k");
					if(layer.getName().endsWith("tn_ex_fl5_p") && korName.equals("서울")) {
						int mm = 0;
					}
				}
				
				copyLayer.setFeatures(lfc);

				if (fLayer.getSimpleFeatureType().indexOf("p_id") > -1) {
					PropertyName propertyName = CommonFactoryFinder.getFilterFactory2(null).property("p_id");
					copyLayer.setFeatures((ListFeatureCollection) layer.getFeatures()
							.sort(new SortByImpl(propertyName, SortOrder.DESCENDING)));
				}

				layers.add(copyLayer);

			} else if (layerTemp instanceof TileLayer) {
				layers.add(layerTemp);
			}
			// 레스터 데이터일 경우
			else if (layerTemp instanceof RasterLayer) {
				layers.add(layerTemp);
			}

		}
		return layers;
	}

	/**
	 * 앞서 로딩된 레이어 중에 데이타와 스타일 종류가 같은 레이어를 리턴한다.
	 * 
	 * @param layers
	 * @param copyLayer
	 * @return
	 */
	public VectorLayer isLikeFreeLayerAndStyle(Vector<Layer> layers, VectorLayer copyLayer) {
		for (Layer layer : layers) {
			if (!(layer instanceof VectorLayer)) {
				continue;
			}
			VectorLayer vl = (VectorLayer) layer;
			if (vl.getFileReader().getName()
					.equals(copyLayer.getFileReader().getName()) &&
					vl.getStyle().getClass().equals(copyLayer.getStyle().getClass())) {
				return vl;
			}
		}
		return null;
	}

	class Comp implements Comparator<SimpleFeature> {
		public int compare(SimpleFeature o1, SimpleFeature o2) {

			Object obj1 = o1.getAttribute("p_id");
			Object obj2 = o2.getAttribute("p_id");

			Double order1 = null;
			Double order2 = null;

			if (obj1 instanceof String) {
				order1 = Double.parseDouble((String) obj1);
			} else if (obj1 instanceof Double) {
				order1 = (Double) obj1;
			} else {
				order1 = (double) ((Integer) obj1).intValue();
			}

			// Double order1 = (Double)o1.getAttribute("p_id");

			if (obj2 instanceof String) {
				order2 = Double.parseDouble((String) obj2);
			} else if (obj2 instanceof Double) {
				order2 = (Double) obj2;
			} else {
				order2 = (double) ((Integer) obj2).intValue();
			}

			// Double order2 = (Double)o2.getAttribute("p_id");

			// return order1 > order2 ? 1 : (order1 == order2 ? 0 : -1);

			int returnV = 0;

			if (order1 > order2) {
				returnV = 1;
			} else if (order1 == order2) {
				returnV = 0;
			} else {
				returnV = -1;
			}

			return returnV;
		}
	}

	public int drawMap(Graphics2D _g, Envelope _e, ScreenCoordUtil scu, Vector<Layer> layers, boolean isDebug) {

		boolean debugTime = false;

		Composite oldComposite = _g.getComposite();

		ScreenConverter sct = new ScreenConverter(scu);

		Java2DConverter j2d = new Java2DConverter(sct);

		OverlapMng olm = new OverlapMng();

		TileServiceMng.DEBUG = false;

		int totalObjSize = 0;
		// this.scu = _scu;

		// this.poinRender.clearOverlap();

		// if(this.poinRender.getViewSize() == null){
		// Rectangle viewR = new Rectangle();
		// viewR.x = 0;
		// viewR.y = 0;
		// viewR.width = scu.getScreenDimension().width;
		// viewR.height = scu.getScreenDimension().height;
		// this.poinRender.setViewSize(viewR);
		// }

		_g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		_g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		_g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		_g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

		_g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		_g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		boolean tempRun = true;

		long drawStart = System.currentTimeMillis();

		int drawLayerCnt = 0;

		int antialiasLevel = 1;
		//ReferencedEnvelope re = new ReferencedEnvelope(_e, _viewCRS);

		int debugIdx = 0;

		// System.out.println("-----------------------------------------------------------------------------------");

		// GenericObjectPool<BufferedImage> imagePool = null;
		// MapContent containMapContent = new MapContent();
		MapContent containMapContent = null;

		for (int i = 0; i < layers.size(); i++) {
			Color oldColor = _g.getColor();
			Stroke oldStroke = _g.getStroke();
			Paint oldPaint = _g.getPaint();

			long st = System.currentTimeMillis();

			Layer layerTemp = layers.get(i);

			ListFeatureCollection objsKey = null;

			String styleName = "";

			if (layerTemp instanceof VectorLayer) {
				VectorLayer layer = (VectorLayer) layerTemp;
				BasicStyleExtend bs = (BasicStyleExtend) layer.getStyle(0);

				if (bs == null) {
					System.out.println("layer name = " + layerTemp.getName() + " not match style name = " );
				}
				styleName = bs.getName();
			}

			
//			
//			if(  !(layerTemp.getName().indexOf("tn_rodway_bndry") > -1) ) {
//				continue;
//			}			
//			System.out.println("layerTemp.getName()=" + layerTemp.getName() +", style Name = " + styleName);
			
//			if(  (layerTemp.getName().indexOf("tn_arrfc") > -1) ) {
//				continue;
//			}

//			if(!styleName.equals("도로경계_종류별 11~14레벨")) {
//				continue;
//			}
			
			
			//
			// if(styleName.equals("tn_ex_ferry_l+페리항로주기2_4레벨")) {
			// int k=0;
			// }

			if (layerTemp instanceof VectorLayer) {

				VectorLayer layer = (VectorLayer) layerTemp;

				int styleCnt = layer.getStyleCnt();

				for (int m = 0; m < styleCnt; m++) {

					BasicStyleExtend bs = (BasicStyleExtend) layer.getStyle(m);

					objsKey = layer.getFeatures();

					if (objsKey == null || objsKey.size() == 0) {
						continue;
					} else {
						totalObjSize += objsKey.size();
					}
					
					
					//System.out.println("layer name = " + layerTemp.getName() + ", style=" + styleName);
					
					
//					if( (layerTemp.getName().indexOf("tn_arpgr") > -1)) {
//						//continue;
//					}
//					else {
//						continue;
//					}

					drawLayerCnt++;

					if (bs == null) {
						// System.out.println("layer name =" + layer.getName() + " render is null");
						continue;
					}

					if (!(bs instanceof PointStyle)) {

						// Vector<Style> addStyles = bs.getAddStyle();

						// Filter filter = CQL.toFilter("dwithin(\"the_geom\","+point+", 1000,
						// meters)");

						// System.out.println("obj size = " + objsKey.size());

						int defaultPriority = 100000;
						Vector<FeatureAndStyle> features = new Vector();
						for (SimpleFeature obj : objsKey) {
							SimpleFeature feature = obj;

							boolean subway = false;

							Vector<QueryStyle> queryStyles = bs.getQueryStyle(feature);
							if (queryStyles != null && queryStyles.size() > 0) {
								for (QueryStyle queryStyle : queryStyles) {
									// System.out.println(queryStyle.style.getName());
									if (queryStyle != null) {
										FeatureAndStyle fas = new FeatureAndStyle(queryStyle.priority, feature,
												queryStyle.style);
										features.add(fas);
									} else if (subway) {
										FeatureAndStyle fas = new FeatureAndStyle(1, feature, bs);
										features.add(fas);
									} else {
										FeatureAndStyle fas = new FeatureAndStyle(defaultPriority, feature, bs);
										features.add(fas);
									}
								}
							} else {
								
								boolean isDraw = true;
								
								if(bs.getCql() != null && bs.getCql().length() > 0) {
									if(bs.filterValidate(feature.getFeatureType())) {
										if(bs.getFilter() != null && !bs.getFilter().evaluate(feature)) {
											isDraw = false;
										}
									}
								}
								
								if(!isDraw) {
									continue;
								}
								
								
								FeatureAndStyle fas = new FeatureAndStyle(defaultPriority, feature, bs);
								features.add(fas);
							}
						}

						Collections.sort(features, Collections.reverseOrder());
						
//						features.sort((o1, o2) -> 
//					    Integer.compare(o2.priority, o1.priority));

						
						
						//Collections.sort(features);
						int idx = 0;
						for (FeatureAndStyle fas : features) {
							
							if (idx != 0) {
								// continue;
							}
							
							//System.out.println("priority=" + fas.priority);
							

							idx++;
							Geometry geom = (Geometry) fas.ft.getDefaultGeometry();

							if (geom == null) {
								continue;
							}

							if (geom instanceof Point || geom instanceof MultiPoint || geom instanceof LineString
									|| geom instanceof MultiLineString || geom instanceof Polygon
									|| geom instanceof MultiPolygon) {

								Geometry geo = geom;

								// System.out.println(geo.toText());

								if (geom == null || geom.isEmpty()) {
									continue;
								}

								BasicStyleExtend bse = (BasicStyleExtend) fas.st;

								Shape shape = null;
								try {
									shape = j2d.toShape(geo);
								} catch (NoninvertibleTransformException e) {
									// TODO Auto-generated catch block
									MapLog.getSCLog().error("", e);
									e.printStackTrace();
								}

								if (bse == null) {
									continue;
								}

								if (bse.isRenderingComposite() == true) {
									_g.setComposite(bse.getComposite());
								}

								if (geo instanceof Polygon || geo instanceof MultiPolygon) {
									// 속도 개선을 위하여 데이타가 상대적으로 큰 레이어를 물리적으로 격자로 자르고 랜더링함.

									if (layer.getGridSize() > 0) {
										this.polygonRender.drawFill(_g, shape, fas.st);

										Shape lineShape = null;
										try {
											lineShape = j2d.toShape(geo, layer.getGridSize());
										} catch (NoninvertibleTransformException e) {
											// TODO Auto-generated catch block
											MapLog.getSCLog().error("", e);
											e.printStackTrace();
										}

										this.polygonRender.drawLine(_g, lineShape, fas.st);
									} else {
										// this.polygonRender.draw(_g, shape, fas.st, 3000);
										this.polygonRender.draw(_g, shape, fas.st);
									}

								} else if (geo instanceof LineString || geo instanceof MultiLineString) {
									this.polylineRender.draw(_g, shape, fas.st);
								}

								if (bse.isRenderingComposite() == true) {
									_g.setComposite(oldComposite);
								}

							} else {

								for (int j = 0; j < geom.getNumGeometries(); j++) {
									Geometry geo = geom.getGeometryN(j);

									BasicStyleExtend bse = (BasicStyleExtend) fas.st;

									Shape shape = null;
									try {
										shape = j2d.toShape(geo);
									} catch (NoninvertibleTransformException e) {
										// TODO Auto-generated catch block
										MapLog.getSCLog().error("", e);
										e.printStackTrace();
									}

									if (bse.isRenderingComposite() == true) {
										_g.setComposite(bse.getComposite());
									}

									if (geo instanceof Polygon || geo instanceof MultiPolygon) {

										if (layer.getGridSize() > 0) {
											this.polygonRender.drawFill(_g, shape, fas.st);

											Shape lineShape = null;
											try {
												lineShape = j2d.toShape(geo, layer.getGridSize());
											} catch (NoninvertibleTransformException e) {
												// TODO Auto-generated catch block
												MapLog.getSCLog().error("", e);
												e.printStackTrace();
											}

											this.polygonRender.drawLine(_g, lineShape, fas.st);
										} else {
											// this.polygonRender.draw(_g, shape, fas.st, 3000);
											this.polygonRender.draw(_g, shape, fas.st);
										}

									} else if (geo instanceof LineString || geo instanceof MultiLineString) {
										this.polylineRender.draw(_g, shape, fas.st);
									}

									if (bse.isRenderingComposite() == true) {
										_g.setComposite(oldComposite);
									}

								}

							}

						}

					} else {
						// System.out.println(debugIdx++ +", point layer = " + layerTemp.getName());
						int defaultPriority = 100000;

						Vector<FeatureAndStyle> features = new Vector();

						for (SimpleFeature obj : objsKey) {
							SimpleFeature feature = obj;
							
							
							if(layer.getName().endsWith("tn_ex_fl5_p")) {
								
								String korName = (String)feature.getAttribute("lb_name_k");
								
								if(korName.equals("서울")) {
									int mm = 0;
								}
								
							}


							boolean subway = false;
							
//							if(obj.getFeatureType().indexOf("batc_nm") > -1) {
//								System.out.println(feature.getAttribute("batc_nm"));
//							}
							
							Vector<QueryStyle> queryStyles = bs.getQueryStyle(feature);

							if (queryStyles != null && queryStyles.size() > 0) {
								for (QueryStyle queryStyle : queryStyles) {
									Style featureStyle = null;
									if (queryStyle != null) {
										featureStyle = queryStyle.style;
									}
									if (featureStyle != null) {
										PointStyle ps = (PointStyle) featureStyle;

										FeatureAndStyle fas = new FeatureAndStyle(ps.priority, feature, ps);
										features.add(fas);
									} else if (subway) {
										FeatureAndStyle fas = new FeatureAndStyle(1, feature, bs);
										features.add(fas);
									} else {
										FeatureAndStyle fas = new FeatureAndStyle(defaultPriority, feature, bs);
										features.add(fas);
									}
								}
							} else {
								
								boolean isDraw = true;
								
								if(bs.getCql() != null && bs.getCql().length() > 0) {
									if(bs.filterValidate(feature.getFeatureType())) {
										if(bs.getFilter() != null && !bs.getFilter().evaluate(feature)) {
											isDraw = false;
										}
									}
								}
								
								if(!isDraw) {
									continue;
								}


								FeatureAndStyle fas = new FeatureAndStyle(defaultPriority, feature, bs);
								features.add(fas);
							}
						}

						Collections.sort(features, Collections.reverseOrder());
						//Collections.sort(features, Collections.);
						
						
//						features.sort((o1, o2) -> 
//					    Integer.compare(o2.priority, o1.priority));
						
						int idx = 0;
						for (FeatureAndStyle fas : features) {
							
//							if(fas.ft.getFeatureType().indexOf("batc_nm") > -1) {
//								PointStyle bse = (PointStyle) fas.st;
//								System.out.println("draw idx="+idx+" " + fas.ft.getAttribute("batc_nm")+", "+bse.getName()+", priority=" + fas.priority);
//							}
							
							Geometry geom = (Geometry) fas.ft.getDefaultGeometry();

							// System.out.println("name=" + fas.ft.getAttribute("name"));

							if (geom instanceof Point || geom instanceof MultiPoint || geom instanceof LineString
									|| geom instanceof MultiLineString || geom instanceof Polygon
									|| geom instanceof MultiPolygon) {
								Geometry geo = geom;

								if (geo instanceof Polygon || geo instanceof MultiPolygon) {
									geo = geo.getCentroid();
								}

								PointStyle bse = (PointStyle) fas.st;

								if (this.limitedEnvelope != null
										&& !this.limitedEnvelope.intersects(geo.getCoordinate())) {
									continue;
								}

								if (bse.isRenderingComposite() == true) {
									_g.setComposite(bse.getComposite());
								}

								/*
								 * Shape shape = null;
								 * 
								 * try {
								 * shape = j2d.toShape(geo);
								 * 
								 * } catch (NoninvertibleTransformException e) {
								 * // TODO Auto-generated catch block
								 * MapLog.getSCLog().error("", e);
								 * e.printStackTrace();
								 * }
								 * 
								 * 
								 * if (geo instanceof LineString || geo instanceof MultiLineString) {
								 * this.poinRender.draw(_g, shape, fas.ft, fas.st, true, scu, olm, isDebug);
								 * } else {
								 * this.poinRender.draw(_g, shape, fas.ft, fas.st, false, scu, olm, isDebug);
								 * }
								 */

								Shape shape = null;
								try {

									if (geo instanceof LineString || geo instanceof MultiLineString) {

										if (geo instanceof LineString) {
											shape = j2d.toShape(geo);
											this.poinRender.draw(_g, shape, fas.ft, fas.st, true, scu, olm, isDebug);
										} else {
											for (int k = 0; k < geo.getNumGeometries(); k++) {
												Geometry subGeo = geo.getGeometryN(k);
												shape = j2d.toShape(subGeo);
												this.poinRender.draw(_g, shape, fas.ft, fas.st, true, scu, olm,
														isDebug);
											}
										}

									} else {
										shape = j2d.toShape(geo);
										this.poinRender.draw(_g, shape, fas.ft, fas.st, false, scu, olm, isDebug);
									}

								} catch (NoninvertibleTransformException e) {
									MapLog.getSCLog().error("", e);
									e.printStackTrace();
								}

								if (bse.isRenderingComposite() == true) {
									_g.setComposite(oldComposite);
								}

								idx++;

							} else {

								for (int j = 0; j < geom.getNumGeometries(); j++) {
									Geometry geo = geom.getGeometryN(j);

									if (geo instanceof Polygon || geo instanceof MultiPolygon) {
										geo = geo.getCentroid();
									}

									PointStyle bse = (PointStyle) fas.st;

									if (this.limitedEnvelope != null
											&& !this.limitedEnvelope.intersects(geo.getCoordinate())) {
										continue;
									}

									Shape shape = null;

									try {
										shape = j2d.toShape(geo);

									} catch (NoninvertibleTransformException e) {
										// TODO Auto-generated catch block
										MapLog.getSCLog().error("", e);
										e.printStackTrace();
									}

									if (bse.isRenderingComposite() == true) {
										_g.setComposite(bse.getComposite());
									}

									if (geo instanceof LineString || geo instanceof MultiLineString) {
										this.poinRender.draw(_g, shape, fas.ft, fas.st, true, scu, olm, isDebug);

									} else {
										this.poinRender.draw(_g, shape, fas.ft, fas.st, false, scu, olm, isDebug);
									}

									if (bse.isRenderingComposite() == true) {
										_g.setComposite(oldComposite);
									}
									idx++;

								}

							}

						}

					}
				}

			} else if (layerTemp instanceof TileLayer) {
				((TileLayer) layerTemp).draw(_e, _g, scu.getScreenDimension().width, scu.getScreenDimension().height);
			} else if (layerTemp instanceof RasterLayer) {

				long st1 = System.currentTimeMillis();
				RasterLayer rasterLayer = (RasterLayer) layerTemp;

				if (rasterLayer.isIntersect(_e)) {
					RasterRender render = null;
					try {
						render = StorageMng.borrowRasterRender();
						render.addRasterLayer(rasterLayer);

						MapViewport mvp = render.mapContent.getViewport();

						Rectangle rect = new Rectangle(0, 0, scu.getScreenDimension().width,
								scu.getScreenDimension().height);

						mvp.setScreenArea(rect);

						ReferencedEnvelope renv = new ReferencedEnvelope(_e.getMinX(), _e.getMaxX(), _e.getMinY(),
								_e.getMaxY(), StorageMng.getCrs5179());
						mvp.setBounds(renv);

						render.mapContent.setViewport(mvp);

						render.renderer.paint(_g, rect, renv);

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						if (render != null) {
							StorageMng.returnRasterRender(render);
						}
					}

				}
			}

			_g.setColor(oldColor);
			_g.setPaint(oldPaint);
			_g.setStroke(oldStroke);

			long et = System.currentTimeMillis();

			if (debugTime) {
				long jobTime = et - st;
				System.out.println("draw layerName=" + layerTemp.getName() + ", time=" +
						(jobTime) + ", size=" + objsKey.size());
			}
		}

		if (containMapContent != null) {
			containMapContent.dispose();
			containMapContent = null;
		}

		// if (imagePool != null) {
		// imagePool = null;
		// }

		// if(debugTime) {
		//
		// long et = System.currentTimeMillis();
		// long jobTime = et - drawStart;
		//
		// System.out.println("drawMap , time=" + (jobTime)/1000.0 +", draw layerCnt="+
		// drawLayerCnt);
		// }

		if (isDebug) {
			// _g.setColor(Color.RED);
			Color oldColor = _g.getColor();
			Stroke old = _g.getStroke();

			BasicStroke bs = new BasicStroke(0.7f);
			_g.setStroke(bs);
			Set set = olm.ols.keySet();
			Iterator it = set.iterator();
			while (it.hasNext()) {
				String key = (String) it.next();
				if (key.equals("main")) {
					_g.setColor(Color.yellow);
				} else {
					_g.setColor(Color.green);
				}
				Overlap ol1 = olm.ols.get(key);
				Vector<IRect> rects = ol1.overlaps.dumpString();
				for (IRect rect : rects) {
					_g.drawRect(rect.x1, rect.y1, rect.x2 - rect.x1, rect.y2 - rect.y1);
				}
			}

			_g.setColor(oldColor);
			_g.setStroke(old);

			Color oldColor1 = _g.getColor();
			Stroke oldStroke1 = _g.getStroke();

			_g.setColor(Color.black);
			_g.setStroke(new BasicStroke(2));
			_g.drawRect(0, 0, scu.getScreenDimension().width, scu.getScreenDimension().height);

			_g.setColor(oldColor1);
			_g.setStroke(oldStroke1);

		}

		// Set poolSet = this.imagePools.keySet();
		// Iterator it = poolSet.iterator();
		// while (it.hasNext()) {
		// int key = (int) it.next();
		// if (this.imagePools.get(key).get)
		// }
		// if (layers.size() > 4) {
		// this.imagePools.clear();
		// }

		Overlap ol = olm.getOverlay("main");
		ol.clear();
		olm.clear();
		/*
		 * _g.setColor(Color.red); int h = _scu.getScreenDimension().height; int w =
		 * _scu.getScreenDimension().width; _g.drawLine(0, h/2, w, h/2);
		 * _g.drawLine(w/2, 0, w/2, h);
		 * 
		 * 
		 * FontRenderContext frc = _g.getFontRenderContext(); // Rectangle textRc =
		 * null;
		 * 
		 * TextLayout tl = new TextLayout("공식 가이드 북", _g.getFont(), frc);
		 * 
		 * //_g.drawString("东大门历史文化公园站", w/2, h/2); //tl.draw(_g, w/2, h/2);
		 * 
		 * //_g.drawGlyphVector(tl, w/2, h/2);
		 * 
		 * //_g.setStroke(new BasicStroke(1));
		 * 
		 * AffineTransform now = _g.getTransform();
		 * 
		 * AffineTransform atf = new AffineTransform(); atf.translate(w/2, h/2);
		 * atf.rotate(45);
		 * 
		 * _g.setTransform(atf); tl.draw(_g, 0, 0); _g.setTransform(now); Shape shape =
		 * tl.getOutline(atf);
		 */
		// _g.draw(shape);
		/*
		 * Set key = this.poinRender.olm.ols.keySet(); Iterator it = key.iterator();
		 * while(it.hasNext()){ Overlap ol =
		 * (com.gis.map.render.OverlapMng.Overlap)this.poinRender.olm.ols.get(it.next())
		 * ;
		 * 
		 * System.out.println("overlay id = " + ol.id);
		 * 
		 * if(ol.id.equals("main")){ _g.setColor(Color.red); } else{
		 * _g.setColor(Color.GREEN); }
		 * 
		 * Vector<IRect> rects = ol.overlaps.getArea(); for(IRect rect : rects){
		 * _g.drawRect(rect.x1, rect.y1, rect.x2 - rect.x1, rect.y2 - rect.y1); } }
		 */
		// outLineText(_g);
		return totalObjSize;
	}

	public void testDrawText(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Font font = new Font("Serif", Font.PLAIN, 40);

		String name = new String("한글");
		AttributedString as1;
		try {
			as1 = new AttributedString(new String(name.getBytes(), "UTF-8"));
			as1.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON, 10, 15);
			as1.addAttribute(TextAttribute.BACKGROUND, Color.LIGHT_GRAY, 2, 9);
			as1.addAttribute(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON, 2, 8);
			as1.addAttribute(TextAttribute.FONT, font);

			g.drawString(as1.getIterator(), 15, 60);

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// drawText
	public void outLineText(Graphics2D g, SimpleFeature feature, Style style) {
		String name = "";
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		FontRenderContext frc = g.getFontRenderContext();
		Font font = new Font("Gulim", Font.PLAIN, 50);
		TextLayout tl = new TextLayout("테스트 xptmx", font, frc);
		Shape shape = tl.getOutline(AffineTransform.getTranslateInstance(0, 100));
		g.setColor(Color.white);
		g.draw(shape);
		g.setColor(Color.red);
		g.fill(shape);
	}

	class ScreenConverter implements PointConverter {
		ScreenCoordUtil scu = null;

		ScreenConverter(ScreenCoordUtil _scu) {
			scu = _scu;
		}

		@Override
		public Point2D toViewPoint(Coordinate modelCoordinate) throws NoninvertibleTransformException {
			// TODO Auto-generated method stub
			// float x = (float) this.scu.getMapXToScrX(modelCoordinate.x);
			// float y = (float) this.scu.getMapYToScrY(modelCoordinate.y);

			float x = (float) this.scu.getMapXToScrX(modelCoordinate.x);
			float y = (float) this.scu.getMapYToScrY(modelCoordinate.y);

			/*
			 * if(x > 0 && x < 255){ System.out.println(); } if(y > 0 && y < 255){
			 * System.out.println(); }
			 */
			Point2D.Float p1 = new Point2D.Float(x, y);
			return p1;
		}

	}

	public MultiLineString getGridLines(Geometry geo, int gridSize) {

		MultiLineString mls = null;

		Vector<LineString> lss = new Vector();

		if (geo instanceof Polygon) {
			Polygon pg = (Polygon) geo;
			Coordinate[] cds = pg.getCoordinates();
			for (int i = 0; i < cds.length - 1; i++) {
				Coordinate one = cds[i];
				Coordinate two = cds[i + 1];

				double tempx = one.x % gridSize;

				double tempy = one.y % gridSize;

				if ((one.x == two.x) && tempx == 0) {
					Coordinate[] coords = new Coordinate[2];
					coords[0] = one;
					coords[1] = two;
					lss.add(gft.createLineString(coords));
				} else if ((one.y == two.y) && tempy == 0) {
					Coordinate[] coords = new Coordinate[2];
					coords[0] = one;
					coords[1] = two;
					lss.add(gft.createLineString(coords));
				}
			}
		} else if (geo instanceof MultiPolygon) {
			MultiPolygon mpg = (MultiPolygon) geo;
			for (int j = 0; j < mpg.getNumGeometries(); j++) {
				Polygon pg = (Polygon) mpg.getGeometryN(j);
				Coordinate[] cds = pg.getCoordinates();
				for (int i = 0; i < cds.length - 1; i++) {
					Coordinate one = cds[i];
					Coordinate two = cds[i + 1];

					double tempx = one.x % gridSize;

					double tempy = one.y % gridSize;

					if ((one.x == two.x) && tempx == 0) {
						Coordinate[] coords = new Coordinate[2];
						coords[0] = one;
						coords[1] = two;
						lss.add(gft.createLineString(coords));
					} else if ((one.y == two.y) && tempy == 0) {
						Coordinate[] coords = new Coordinate[2];
						coords[0] = one;
						coords[1] = two;
						lss.add(gft.createLineString(coords));
					}
				}
			}
		}

		if (lss.size() > 0) {
			LineString[] arrayList = new LineString[lss.size()];
			for (int i = 0; i < lss.size(); i++) {
				arrayList[i] = lss.get(i);
			}

			mls = gft.createMultiLineString(arrayList);
		}

		return mls;
	}

	@Override
	public Vector<Layer> loadMemLayers(Envelope __e, ScreenCoordUtil _scu) {
		// TODO Auto-generated method stub
		return this.loadMemLayers(__e, _scu, null, null, null);
	}
}
