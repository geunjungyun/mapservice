package com.gis.map;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.imageio.ImageIO;

import com.data.file.FileLayer;
import com.data.util.MapLog;
import com.gis.map.render.GridObjectRender;
import com.gis.map.render.IRender;
import com.gis.map.render.OverlapMng;
import com.gis.map.render.PointRender;
import com.gis.map.render.PolygonRender;
import com.gis.map.render.PolylineRender;
import com.gis.map.render.RasterRender;
import com.gis.map.render.RasterRenderPool;
import com.gis.map.render.TextRender;
import com.gis.map.render.TextStroke;
import com.gis.map.render.OverlapMng.Overlap;
import com.gis.map.render.java2D.Java2DConverter;
import com.gis.map.render.java2D.Java2DConverter.PointConverter;
import com.gis.map.style.BasicStyleExtend;
import com.gis.map.style.PointStyle;
import com.gis.map.style.QueryStyle;
import com.gis.projection.ScreenCoordUtil;
//import com.gis2.storage.ServiceMng;
import com.gis2.storage.StorageMng;
import com.index.rtree.IRect;

import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.filter.SortByImpl;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapViewport;
import org.geotools.referencing.CRS;

import org.locationtech.jts.algorithm.CGAlgorithms;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.GeometryType;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.sort.SortOrder;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

//import com.vividsolutions.jump.feature.Feature;
//import com.vividsolutions.jump.feature.FeatureSchema;
//import com.vividsolutions.jump.workbench.ui.renderer.java2D.Java2DConverter;
//import com.vividsolutions.jump.workbench.ui.renderer.java2D.Java2DConverter.PointConverter;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

//public static interface PointConverter {
//	public Point2D toViewPoint(Coordinate modelCoordinate)
//		throws NoninvertibleTransformException;
//}

public class MapContext implements Context {

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

	public String foreignMode = null;

	public StorageMng sm = null;

	public long date = -1;

	public boolean largeFontMode = true;

	public float maxFontSize = Float.MIN_VALUE;

	public static boolean debugOverlap = false;

	public int levelId = -1;
	// IRender render;
	
	public MapData mapData = null;

	public MapContext(String foreignMode, StorageMng sm, MapData _mapData) {
		this.foreignMode = foreignMode;
		// this.backgroundColor = Color.white;
		this.backgroundColor = new Color(190, 232, 255);
		this.sm = sm;
		this.mapData = _mapData;
	}

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

//		if (this.date < layer.date) {
//			this.date = layer.date;
//		}

//		if (fullEnvelope != null) {
//			this.fullEnvelope.expandToInclude(layer.getBounds());
//		} else {
//			Envelope env = layer.getBounds();
//			this.fullEnvelope = new Envelope(env.getMinX(), env.getMaxX(), env.getMinY(), env.getMaxY());
//		}

		return true;
	}

	public void sort() {
		Collections.sort(this.layerList, Collections.reverseOrder());
	}

	public void sort1() {

		Layer jijuk = null;
		int jijukIdx = -1;
		int pointStartIdx = -1;
		for (int i = 0; i < this.layerList.size(); i++) {

			Layer ly = this.layerList.get(i);

			if (ly instanceof VectorLayer) {

				VectorLayer vl = (VectorLayer) ly;

				if (vl.getStyleName().toLowerCase().indexOf("point") > -1 && pointStartIdx == -1) {
					pointStartIdx = i;
				}

				if (vl.getStyleName().toLowerCase().indexOf("point") > -1 && vl.getName().toLowerCase().equals("fa")) {
					jijukIdx = i;
				}
			}
			/*
			 * VectorLayer vl = (VectorLayer)this.layerList.get(i);
			 * 
			 * if(vl.getStyleName().toLowerCase().indexOf("point") > -1 && pointStartIdx ==
			 * -1) { pointStartIdx = i; }
			 * 
			 * if(vl.getStyleName().toLowerCase().indexOf("point") > -1 &&
			 * vl.getName().toLowerCase().equals("fa")) { jijukIdx = i; }
			 */
		}
		if (jijukIdx > -1 && pointStartIdx > -1) {
			jijuk = this.layerList.remove(jijukIdx);

			this.layerList.add(pointStartIdx, jijuk);
		}

	}

	public Vector<Layer> getLayers() {
		return this.layerList;
	}

	public void clearMap(Graphics2D _g) {
		_g.setBackground(this.backgroundColor);
		_g.setColor(this.backgroundColor);
		_g.fillRect(0, 0, 256, 256);

	}

	String serviceName = "";

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

		}
	}

	public void setServiceName(String serviceName_) {
		this.serviceName = serviceName_;
		this.poinRender.setServiceName(serviceName);
	}

	public Vector<Layer> loadMemLayers(Envelope __e, ScreenCoordUtil _scu) {

		Vector<Layer> layers = new Vector();

		// CoordinateReferenceSystem viewCRS = _viewCRS;

		int totalObjSize = 0;

		Envelope viewEnv = null;

		for (int i = 0; i < this.layerList.size(); i++) {

			MathTransform toFileTransform = null;
			MathTransform toViewTransform = null;

			boolean isSort = false;
			Object[] objsKey = null;

			Layer layerTemp = this.layerList.get(i);

			if (layerTemp instanceof VectorLayer) {
				// continue;
				VectorLayer layer = (VectorLayer) layerTemp;

				boolean isPointStyleExist = false;

				for (Layer layerT : this.layerList) {

					if (layerT instanceof VectorLayer) {
						VectorLayer vl = (VectorLayer) layerT;
						if (vl.getStyle() instanceof PointStyle) {
							isPointStyleExist = true;
						}
					}

				}

				if (layer.getFileReader() == null) {
					System.out.println("loadMemLayer layerName = " + layer.getName() + " is null");
					continue;
				}

				FileLayer fLayer = layer.getFileReader();

				double objCnt = fLayer.getObjectSize();

				if (objCnt == 0) {
					continue;
				}

				viewEnv = __e;

				VectorLayer copyLayer = new VectorLayer();

				layer.clone(copyLayer);

				VectorLayer preLayer = null;
				for (Layer ly : layers) {
					if (ly.getName().equals(copyLayer.getName())) {
						preLayer = (VectorLayer) ly;
					}
				}

				if (preLayer == null) {

					ListFeatureCollection lfc = null;
					try {

						Envelope drawEnv = new Envelope(viewEnv);

						/*
						 * if(layer.style instanceof PointStyle) { PointStyle ps = (PointStyle)
						 * layer.style; int expansion = 200; if(this.largeFontMode) { expansion = 800; }
						 * else { expansion = 200; } int temp = (int) _scu.getScrXToMapX(expansion) -
						 * (int)_scu.getScrXToMapX(0); drawEnv.expandBy(temp*2, temp*2); } else { int
						 * expansion = 100; int temp = (int) _scu.getScrXToMapX(expansion) -
						 * (int)_scu.getScrXToMapX(0); drawEnv.expandBy(temp*2, temp*2); }
						 */
						
						
						/*
						if (isPointStyleExist) {
							double addw = drawEnv.getWidth() * 2.0;
							double addh = drawEnv.getHeight() * 2.0;
							drawEnv.expandBy(addw, addh);
						} else {
							int expansion = 100;
							int temp = (int) _scu.getScrXToMapX(expansion) - (int) _scu.getScrXToMapX(0);
							drawEnv.expandBy(temp * 2, temp * 2);
						}
						*/
						//961899,1908852 9
						
						
						if (isPointStyleExist) {
							int expansion = this.mapData.getMbrExtend();
							int temp = (int) _scu.getScrXToMapX(expansion) - (int) _scu.getScrXToMapX(0);
							drawEnv.expandBy(temp , temp);
						} else {
							int expansion = 200;
							int temp = (int) _scu.getScrXToMapX(expansion) - (int) _scu.getScrXToMapX(0);
							drawEnv.expandBy(temp , temp );
						}
						
//						int expansion = 100;
//						int temp = (int) _scu.getScrXToMapX(expansion) - (int) _scu.getScrXToMapX(0);
//						drawEnv.expandBy(temp * 2, temp * 2);
						

						lfc = (ListFeatureCollection) fLayer.getFeatures(drawEnv.getMinX(), drawEnv.getMinY(),
								drawEnv.getMaxX(), drawEnv.getMaxY());
						// System.out.println("layer = " + layer.getName()+", size = " + lfc.size()+",
						// style="+layer.styleName);
					} catch (Exception e) {
						System.out.println("loadMemLayer error layerName = " + layer.getName());
						e.printStackTrace();
					}

					if (lfc == null || lfc.size() == 0) {
						continue;
					} else {
						totalObjSize += lfc.size();
					}
					copyLayer.setFeatures(lfc);
				} else {
					copyLayer.setFeatures(preLayer.features);
				}
				layers.add(copyLayer);
			} else if (layerTemp instanceof TileLayer) {
				layers.add(layerTemp);
			} else if (layerTemp instanceof RasterLayer) {
				layers.add(layerTemp);
			}

		}
		return layers;
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

//			Double order1 = (Double)o1.getAttribute("p_id");

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

	
	public int drawMap(Graphics2D _g, Envelope _e, ScreenCoordUtil scu, Vector<Layer> layers, boolean debug) {

//		System.out.println("drawMap");
		HashMap<String, Shape> temps = new HashMap();
		boolean debugTime = false;

		Composite oldComposite = _g.getComposite();

		ScreenConverter sct = new ScreenConverter(scu);

		Java2DConverter j2d = new Java2DConverter(sct);

		OverlapMng olm = new OverlapMng();

		int totalObjSize = 0;
		_g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		_g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		_g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);

//		_g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		// _g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
		// RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

		_g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		_g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		_g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		boolean tempRun = true;

		long drawStart = System.currentTimeMillis();

		int drawLayerCnt = 0;

		for (int i = 0; i < layers.size(); i++) {
			Color oldColor = _g.getColor();
			Stroke oldStroke = _g.getStroke();
			Paint oldPaint = _g.getPaint();

			long st = System.currentTimeMillis();

			Layer layerTemp = layers.get(i);

			ListFeatureCollection objsKey = null;

			if (layerTemp instanceof VectorLayer) {

				VectorLayer layer = (VectorLayer) layerTemp;
				// FreeLayer fLayer = layer.getUMDReader().getFreeLayer();
				BasicStyleExtend bs = (BasicStyleExtend) layer.style;

				objsKey = layer.features;

				if (objsKey == null || objsKey.size() == 0) {
					continue;
				} else {
					totalObjSize += objsKey.size();
				}

				drawLayerCnt++;

				if (bs == null) {
					System.out.println("layer name =" + layer.getName() + " render is null");
					continue;
				}


				if (!(bs instanceof PointStyle)) {

					// Vector<Style> addStyles = bs.getAddStyle();

					String drawOrder = bs.getDrawOrder();

					boolean isDrawOrder = false;

					if (drawOrder != null && (drawOrder.equals("a") || drawOrder.equals("d"))) {
						isDrawOrder = true;
					}

					int defaultPriority = 100000;
					Vector<FeatureAndStyle> features = new Vector();
					for (SimpleFeature obj : objsKey) {

						SimpleFeature feature = obj;

						if (isDrawOrder == false) {

							boolean subway = false;

							Vector<QueryStyle> queryStyles = bs.getQueryStyle(feature);
							if (queryStyles != null && queryStyles.size() > 0) {
								for (QueryStyle queryStyle : queryStyles) {
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
								FeatureAndStyle fas = new FeatureAndStyle(defaultPriority, feature, bs);
								features.add(fas);
							}
						} else {
							boolean subway = false;

							int defaultValue = 0;

							if (drawOrder.equals("a")) {
								defaultValue = Integer.MAX_VALUE;
							} else if (drawOrder.equals("b")) {
								defaultValue = Integer.MIN_VALUE;
							}
							double orderValue = 0;
							if (feature.getFeatureType().indexOf("order") > -1) {
								Object oo = feature.getAttribute("order");
								if (oo != null) {
									orderValue = (Double) oo;
								} else {
									orderValue = defaultValue;
								}
							} else {
								orderValue = defaultValue;
							}

							Vector<QueryStyle> queryStyles = bs.getQueryStyle(feature);
							if (queryStyles != null && queryStyles.size() > 0) {
								for (QueryStyle queryStyle : queryStyles) {
									if (queryStyle != null) {
										FeatureAndStyle fas = new FeatureAndStyle((int) orderValue, feature,
												queryStyle.style);
										features.add(fas);
									} else if (subway) {
										FeatureAndStyle fas = new FeatureAndStyle((int) orderValue, feature, bs);
										features.add(fas);
									} else {
										FeatureAndStyle fas = new FeatureAndStyle((int) orderValue, feature, bs);
										features.add(fas);
									}
								}
							} else {
								FeatureAndStyle fas = new FeatureAndStyle((int) orderValue, feature, bs);
								features.add(fas);
							}

						}
					}

					if (isDrawOrder) {
						if (drawOrder.equals("d")) {
							Collections.sort(features);
							// Collections.sort(features, Collections.reverseOrder());
						} else {
							Collections.sort(features, Collections.reverseOrder());
							// Collections.sort(features);
						}
					} else {
						Collections.sort(features, Collections.reverseOrder());
					}
					//
					int idx = 0;
					for (FeatureAndStyle fas : features) {
						Geometry geo = (Geometry) fas.ft.getDefaultGeometry();

						BasicStyleExtend bse = (BasicStyleExtend) fas.st;

						Shape shape = null;
						try {
							shape = j2d.toShape(geo);
						} catch (NoninvertibleTransformException e) {
							// TODO Auto-generated catch block
							// MapLog.getSCLog().error(e);
							e.printStackTrace();
						}

						if (bse.isRenderingComposite() == true) {
							_g.setComposite(bse.getComposite());
						}

						if (geo instanceof Polygon || geo instanceof MultiPolygon) {
							double gridSize = 400;

							if (layer.getName().toLowerCase().startsWith("a")
									|| layer.getName().toLowerCase().startsWith("b")
									|| layer.getName().toLowerCase().startsWith("c")
									|| layer.getName().toLowerCase().startsWith("d")
									|| layer.getName().toLowerCase().startsWith("e")) {
								fas.ft.getUserData().put("area", shape.getBounds2D());
							}

							this.polygonRender.draw(_g, shape, fas.st, (int) gridSize);
						} else if (geo instanceof LineString || geo instanceof MultiLineString) {

							this.polylineRender.draw(_g, shape, fas.st);
						}

						if (bse.isRenderingComposite() == true) {
							_g.setComposite(oldComposite);
						}
						idx++;
					}

				} else {
					
					String drawOrder = bs.getDrawOrder();

					boolean isDrawOrder = false;

					if (drawOrder != null && (drawOrder.equals("a") || drawOrder.equals("d"))) {
						isDrawOrder = true;
					}
					
					//System.out.println("layer name=" + layerTemp.getName()+", drawOrder="+ isDrawOrder);
					
					int defaultPriority = 100000;
					Vector<FeatureAndStyle> features = new Vector();
					for (SimpleFeature obj : objsKey) {
						SimpleFeature feature = obj;
						
						

						if (isDrawOrder == false) {
							boolean subway = false;
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
								FeatureAndStyle fas = new FeatureAndStyle(defaultPriority, feature, bs);
								features.add(fas);
								// System.out.println("layer name =" + layer.name+", style="+bs.getName());
							}
						}						
						else {
							boolean subway = false;

							int defaultValue = 0;

							if (drawOrder.equals("a")) {
								defaultValue = Integer.MAX_VALUE;
							} else if (drawOrder.equals("b")) {
								defaultValue = Integer.MIN_VALUE;
							}
							double orderValue = 0;
							if (feature.getFeatureType().indexOf("order") > -1) {
								Object oo = feature.getAttribute("order");
								if (oo != null) {
									orderValue = (Double) oo;
								} else {
									orderValue = defaultValue;
								}
							} else {
								orderValue = defaultValue;
							}
							
							Vector<QueryStyle> queryStyles = bs.getQueryStyle(feature);
							if (queryStyles != null && queryStyles.size() > 0) {
								for (QueryStyle queryStyle : queryStyles) {
									if (queryStyle != null) {
										FeatureAndStyle fas = new FeatureAndStyle((int) orderValue, feature,
												queryStyle.style);
										features.add(fas);
									} else if (subway) {
										FeatureAndStyle fas = new FeatureAndStyle((int) orderValue, feature, bs);
										features.add(fas);
									} else {
										FeatureAndStyle fas = new FeatureAndStyle((int) orderValue, feature, bs);
										features.add(fas);
									}
								}
							} else {
								FeatureAndStyle fas = new FeatureAndStyle((int) orderValue, feature, bs);
								features.add(fas);
							}
							
							
						}
						
					}
					
					if (isDrawOrder) {
						if (drawOrder.equals("d")) {
							Collections.sort(features);
							// Collections.sort(features, Collections.reverseOrder());
						} else {
							Collections.sort(features, Collections.reverseOrder());
							// Collections.sort(features);
						}
					}
					else {
						if (layer.getName().equals("fa")) {
							Collections.sort(features, Collections.reverseOrder());
						} else {
							Collections.sort(features);
						}
					}

					int idx = 0;
					for (FeatureAndStyle fas : features) {
						Geometry geo = (Geometry) fas.ft.getDefaultGeometry();

						if (geo instanceof Polygon || geo instanceof MultiPolygon) {
							try {

//								if(!layer.getName().toLowerCase().startsWith("f") 
//										&& !layer.getName().toLowerCase().startsWith("g") ) {
								if (layer.getName().toLowerCase().startsWith("a")
										|| layer.getName().toLowerCase().startsWith("b")
										|| layer.getName().toLowerCase().startsWith("c")
										|| layer.getName().toLowerCase().startsWith("d")
										|| layer.getName().toLowerCase().startsWith("e")) {

									
									Rectangle rec = (Rectangle) fas.ft.getUserData().get("area");
									
									if(rec == null) {
										Envelope env = geo.getEnvelopeInternal();
										Polygon pg = JTS.toGeometry(env);
										Shape sh = j2d.toShape(pg);
										rec = (Rectangle) sh.getBounds2D();
									}
									
									double area = rec.getWidth() * rec.getHeight();
									if (area < 2000) {
										continue;
									}
									
								}

								Geometry point = geo.getCentroid();

								if (geo.contains(point)) {
									geo = point;
								} else {
									geo = geo.getInteriorPoint();
								}

							} catch (Exception e) {
								geo = geo.getCentroid();
								// System.out.println("layerName = " + layer.getName()+" topology = " +
								// fas.ft.toString());
								// e.printStackTrace();
								// continue;
							}
						} else if (geo instanceof LineString || geo instanceof MultiLineString) {
							// geo = geo.getInteriorPoint();
							geo = geo.getCentroid();
						}

						PointStyle bse = (PointStyle) fas.st;

						if (this.limitedEnvelope != null && !this.limitedEnvelope.intersects(geo.getCoordinate())) {
							continue;
						}

						// String name = (String) fas.ft.getAttribute("n_name");

						Shape shape = null;

						try {
							shape = j2d.toShape(geo);
						} catch (NoninvertibleTransformException e) {
							// TODO Auto-generated catch block
							// MapLog.getSCLog().error(e);
							e.printStackTrace();
						}

						if (bse.isRenderingComposite() == true) {
							_g.setComposite(bse.getComposite());
						}

						PointStyle ps = (PointStyle) bs;

						if (geo instanceof LineString || geo instanceof MultiLineString) {
							this.poinRender.draw(_g, shape, fas.ft, fas.st, false, scu, olm, levelId, layer.getName(),
									ps);
						} else {
							this.poinRender.draw(_g, shape, fas.ft, fas.st, false, scu, olm, levelId, layer.getName(),
									ps);
						}

						if (bse.isRenderingComposite() == true) {
							_g.setComposite(oldComposite);
						}
						idx++;
					}

				}

			} else if (layerTemp instanceof TileLayer) {
				int k = 0;
				TileLayer tileLayer = (TileLayer) layerTemp;
				// public void draw(Envelope env, int level, Graphics2D g, int width_, int
				// height_) {
				tileLayer.draw(_e, _g, scu.getScreenDimension().width, scu.getScreenDimension().height);
				
				//tileLayer.draw(_e, levelId,  _g, scu.getScreenDimension().width, scu.getScreenDimension().height);
			} else if (layerTemp instanceof RasterLayer) {
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
						// ReferencedEnvelope renv = new ReferencedEnvelope(_e.getMinX(), _e.getMaxX(),
						// _e.getMinY(), _e.getMaxY(),
						// rasterLayer.getGridCoverage2D().getCoordinateReferenceSystem());

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
				System.out.println("draw layerName=" + layerTemp.getName() + ", time=" + (jobTime) + ", size="
						+ (objsKey == null ? "" : objsKey.size()));
			}
		}

		if (debugTime) {

			long et = System.currentTimeMillis();
			long jobTime = et - drawStart;

			System.out.println("drawMap , time=" + (jobTime) + ", draw layerCnt=" + drawLayerCnt);
		}

		if (MapContext.debugOverlap) {
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
		}

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
//			float x = (float) this.scu.getMapXToScrX(modelCoordinate.x);
//			float y = (float) this.scu.getMapYToScrY(modelCoordinate.y);

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
}
