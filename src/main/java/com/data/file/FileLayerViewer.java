package com.data.file;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;



import org.geotools.data.FeatureReader;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.collection.ListFeatureCollection;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
//import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.map.MapViewport;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.JMapPane;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
//import org.opengis.geometry.coordinate.GeometryFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.data.exception.FLException;
import com.data.file.FileLayer;

//import com.dawul.data.file.FreeLayer.SimpleFeatureIterator;




import org.locationtech.jts.geom.Envelope;

public class FileLayerViewer implements MouseListener {

	JMapFrame mapFrame = null;
	MapContent mapContent = null;

	FileLayer poi = null;

	JTextField word = null;

	Envelope selectEnv = new Envelope();

	JTextField circleTF = new JTextField("1000");

	JTextField pageNumT = new JTextField("1");

	JTextField pageCntT = new JTextField("5");

	// http://docs.geotools.org/latest/userguide/library/render/style.html

	// http://docs.geoserver.org/latest/en/user/styling/sld-cookbook/points.html
	// http://docs.geoserver.org/latest/en/user/styling/sld-cookbook/lines.html

	public static void main(String[] args) {
		System.setProperty("org.geotools.referencing.forceXY", "true");
		try {
			CoordinateReferenceSystem crs5179 = CRS.decode("EPSG:5179");
			CoordinateReferenceSystem crs5186 = CRS.decode("EPSG:5186");
			
			String kk = "";
		} catch (NoSuchAuthorityCodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		FileLayerViewer mwt = new FileLayerViewer();
		try {
			//mwt.view("F:\\coops\\2024\\tileset\\output\\reg10\\layers\\aa");
			mwt.view("E:\\temp\\output\\emapv\\layers\\tn_ex_knl12_p");
			//mwt.view("E:\\temp\\output\\emapv\\layers\\tn_ex_ssubwsta_l");//지하철포인트
			//mwt.view("E:\\map_service\\data\\crowd\\tn_ex_subwln_l");
			//mwt.view("D:\\coops\\fe\\20210228\\fe");
		} catch (NoSuchAuthorityCodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void view(String file) throws NoSuchAuthorityCodeException, FactoryException {

		mapContent = new MapContent();
		mapContent.setTitle("Quickstart");
		
		FileLayer fl = new FileLayer();

		try {
			fl.read(file);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		CoordinateReferenceSystem crs = fl.getSimpleFeatureType().getCoordinateReferenceSystem();
		

		ListFeatureCollection lfc = new ListFeatureCollection(fl.getSimpleFeatureType());

		/*
		 * try { Analyzer ka = fl.getQueryAnalyzer();
		 * 
		 * QueryParser parser = new QueryParser("sig_cd", ka);
		 * 
		 * parser.setDefaultOperator(Operator.AND);
		 * 
		 * 
		 * Query query = parser.parse("11*");
		 * 
		 * QueryResultSet qrs = new QueryResultSet();
		 * 
		 * int pageNum = Integer.parseInt(this.pageNumT.getText()); int pageCnt =
		 * Integer.parseInt(this.pageCntT.getText());
		 * 
		 * qrs.SetReq(query, null, 0, 0, false);
		 * 
		 * long qst = System.currentTimeMillis(); qrs = fl.getFeatures(qrs); long qet =
		 * System.currentTimeMillis(); if(qrs.totalCnt > 0) { lfc =
		 * (ListFeatureCollection) qrs.sfc; } } catch(Exception e) {
		 * e.printStackTrace(); }
		 * 
		 * String geoType = "MultiLineString";
		 */

		// sig_cd

		// lfc.add(f);

		int width = Integer.MIN_VALUE;
		int height = Integer.MIN_VALUE;

		String geoType = "";
		com.data.file.FileLayer.SimpleFeatureIterator fli = (com.data.file.FileLayer.SimpleFeatureIterator) fl
				.iterator();
		int idx = 0;
		while (fli.hasNext()) {
			// System.out.println("idx=" + idx);
			SimpleFeature sf = fli.next();

			Geometry geo = (Geometry) sf.getDefaultGeometry();
			
			if(geo == null) {
				continue;
			}
			
			if (geo != null) {
				geoType = geo.getGeometryType();
			}
			

//			String code = (String) sf.getAttribute("n_name");
//			
//			if(!code.equals("5호선")) {
//				continue;
//			}
//			System.out.println(code+","+geo.getNumGeometries());
//			if (code.startsWith("11")) {
//				if (width < geo.getEnvelopeInternal().getWidth()) {
//					width = (int) geo.getEnvelopeInternal().getWidth();
//				}
//
//				if (height < geo.getEnvelopeInternal().getHeight()) {
//					height = (int) geo.getEnvelopeInternal().getHeight();
//				}
//			}

			lfc.add(sf);
			idx++;

//			if (idx > 20000) {
//				break;
//			}
		}
		
		//System.setProperty("org.geotools.referencing.forceXY", "false");

		// Style style = SLD.createPolygonStyle(Color.BLACK, Color.RED, (float)
		// 0.5);//createSimpleStyle(fl.getSimpleFeatureType());
		Style style = null;
//		if(geoType.equals("Polygon") || geoType.equals("MultiPolygon")) {
//			style = SLD.createPolygonStyle(Color.black, Color.RED,  0);
//		}
//		else if(geoType.equals("Point") || geoType.equals("MultiPoint")) {
//			style = SLD.createPointStyle("Circle", Color.black, Color.RED, 0, 2);
//		}
//		else if(geoType.equals("MultiLineString") || geoType.equals("LineString")) {
//			style = SLD.createLineStyle(Color.black, 2);
//		}

		style = SLD.createPolygonStyle(Color.black, Color.RED, 0);

		// Style style = SLD.createSimpleStyle(fl.getSimpleFeatureType());

		Layer layer = new FeatureLayer(lfc, style);
		
		
		MapViewport mvp = mapContent.getViewport();
		
		ReferencedEnvelope env = new ReferencedEnvelope(fl.getSimpleFeatureType().getCoordinateReferenceSystem());
		
		//1788375.2, 1169395.7 : 1788446.7, 1169558.5
		env.init(1169395.7, 1169558.5, 1788375.2, 1788446.7);
		
		//mvp.setBounds(layer.getBounds());
		
		mvp.setBounds(env);
		
//		CoordinateReferenceSystem crs5179 = CRS.decode("EPSG:5179");
//		String wkt = crs5179.toWKT();
		
				
		//mvp.setCoordinateReferenceSystem(crs5179);
		mvp.setCoordinateReferenceSystem(crs);
		
		mapContent.setViewport(mvp);
		
		mapContent.addLayer(layer);
		
		
		//CoordinateReferenceSystem mapcrs = mapContent.getCoordinateReferenceSystem();
		//System.setProperty("org.geotools.referencing.forceXY", "false");
		
		//this.saveImage(mapContent, "e://test.png", 400);
		
		layer.setTitle(fl.getLayerInfo().getName() + "_" + lfc.size());

		// Now display the map
		
		

		mapFrame = new JMapFrame(mapContent);
		
		
		// list layers and set them as visible + selected
		mapFrame.enableLayerTable(true);
		mapFrame.enableInputMethods(true);
		mapFrame.enableLayerTable(true);

		// zoom in, zoom out, pan, show all
		mapFrame.enableToolBar(true);
		// location of cursor and bounds of current
		mapFrame.enableStatusBar(true);

		// mapFrame.addMouseListener(this);

		mapFrame.getMapPane().addMouseListener(this);

		JToolBar tb = mapFrame.getToolBar();

		tb.addSeparator();
		JButton search = new JButton("검색");

		search.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub

			}

		});

		// display
		mapFrame.setVisible(true);
		mapFrame.setSize(1500, 900);

		long st = System.currentTimeMillis();
		// this.saveImage(map, "c:/test.png", 5000);
		long et = System.currentTimeMillis();
		System.out.println("time=" + (et - st));

		// this.poi("서울역");

		this.initTable(lfc);

	}

	JTable table;
	ListSelectionListener lsl = null;
	JMapPane mapPane = null;
	boolean repaintTemp = false;

	//CoordinateReferenceSystem crs5179 = null;

	public void initTable(ListFeatureCollection sfc) {

		mapPane = mapFrame.getMapPane();

		JFrame tableF = new JFrame("신규엔진");

		table = new JTable();
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setModel(new DefaultTableModel(10, 10));
		table.setPreferredScrollableViewportSize(new Dimension(500, 200));

		JScrollPane scrollPane = new JScrollPane(table);
		tableF.getContentPane().add(scrollPane, BorderLayout.CENTER);

		tableF.setSize(900, 450);
		tableF.setLocation(1200, 0);
		tableF.show();

		final FeatureModel model = new FeatureModel((ListFeatureCollection) sfc, null);

		this.table.setModel(model);

		this.table.setCellSelectionEnabled(true);
		ListSelectionModel cellSelectionModel = table.getSelectionModel();
		cellSelectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		// if(!tableInit) {

		if (this.lsl != null) {
			cellSelectionModel.removeListSelectionListener(this.lsl);
			this.lsl = null;
		}

		if (this.lsl == null) {
			lsl = new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {

					Object selectedData = null;

					int[] selectedRow = table.getSelectedRows();

					if (selectedRow.length == 1) {

						int rowSelect = selectedRow[0];

						System.out.println("valueChanged  select Row size = " + selectedRow.length + ", select row = "
								+ rowSelect);

						// selectedData = table.getValueAt(selectedRow[i], selectedColumns[j]);
						// System.out.println("Selected: " + selectedData.toString());
						SimpleFeature sf = (SimpleFeature) model.array[rowSelect];

						Geometry geo = (Geometry) sf.getDefaultGeometry();
						org.locationtech.jts.geom.Point pt = geo.getCentroid();
						ReferencedEnvelope nenv = mapPane.getDisplayArea();

						double width = nenv.getWidth();
						double height = nenv.getHeight();

//						ReferencedEnvelope centTenv = new ReferencedEnvelope(crs5179);
//						centTenv.init(pt.getX() - width / 2, pt.getX() + width / 2, pt.getY() - height / 2,
//								pt.getY() + height / 2);
//						mapPane.setDisplayArea(centTenv);

						updateUI();
					}

				}

			};
		}

		cellSelectionModel.addListSelectionListener(this.lsl);

		/*
		 * TableColumn column = table.getColumnModel().getColumn(0);
		 * 
		 * column.setPreferredWidth(100); //column1.setMaxWidth(130);
		 * column.setMinWidth(100); TableColumn column1 =
		 * table.getColumnModel().getColumn(1);
		 * 
		 * column1.setPreferredWidth(130); //column1.setMaxWidth(130);
		 * column1.setMinWidth(130);
		 * 
		 * TableColumn column2 = table.getColumnModel().getColumn(2);
		 * column2.setPreferredWidth(50); //column2.setMaxWidth(60);
		 * column2.setMinWidth(50);
		 * 
		 * TableColumn column3 = table.getColumnModel().getColumn(3);
		 * column3.setPreferredWidth(230); //column3.setMaxWidth(200);
		 * column3.setMinWidth(230);
		 * 
		 * 
		 * TableColumn column4 = table.getColumnModel().getColumn(4);
		 * column4.setPreferredWidth(50); //column3.setMaxWidth(200);
		 * column4.setMinWidth(50);
		 * 
		 * TableColumn column5 = table.getColumnModel().getColumn(5);
		 * column5.setPreferredWidth(50); //column3.setMaxWidth(200);
		 * column5.setMinWidth(50);
		 */
		// tableInit = true;

	}


	public void saveImage(final MapContent map, final String file, final int imageWidth) {

		GTRenderer renderer = new StreamingRenderer();
		
		renderer.setMapContent(map);

		Rectangle imageBounds = null;
		ReferencedEnvelope mapBounds = null;
		try {
			mapBounds = map.getMaxBounds();
			double heightToWidth = mapBounds.getSpan(1) / mapBounds.getSpan(0);
			imageBounds = new Rectangle(0, 0, imageWidth, (int) Math.round(imageWidth * heightToWidth));

		} catch (Exception e) {
			// failed to access map layers
			throw new RuntimeException(e);
		}

		BufferedImage image = new BufferedImage(imageBounds.width, imageBounds.height, BufferedImage.TYPE_INT_RGB);

		Graphics2D gr = image.createGraphics();
		gr.setPaint(Color.WHITE);
		gr.fill(imageBounds);

		try {
			renderer.paint(gr, imageBounds, mapBounds);
			File fileToSave = new File(file);
			ImageIO.write(image, "png", fileToSave);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}



	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		/*
		 * Graphics gs = mapFrame.getMapPane().getGraphics();
		 * 
		 * int x = e.getX(); int y = e.getY();
		 * 
		 * double centerx = 0; double centery = 0;
		 * 
		 * int diameter = Integer.parseInt(this.circleTF.getText());
		 * 
		 * AffineTransform at = this.map.getViewport().getScreenToWorld();
		 * 
		 * Point2D ptSrc = new Point2D.Double(x,y);
		 * 
		 * Point2D ptDst = new Point2D.Double(x,y);
		 * 
		 * at.transform(ptSrc, ptDst);
		 * 
		 * centerx = ptDst.getX(); centery = ptDst.getY();
		 * 
		 * 
		 * this.selectEnv.init(centerx - diameter, centerx + diameter, centery -
		 * diameter, centery+ diameter );
		 * 
		 * Style rect = SLD.createLineStyle(Color.CYAN, 3);
		 * 
		 * 
		 * SimpleFeatureType sftt = this.createFeatureType();
		 * 
		 * DefaultFeatureCollection dfcc = new DefaultFeatureCollection(null, sftt);
		 * SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(sftt);
		 * 
		 * GeometryFactory geometryFactory = (GeometryFactory)
		 * JTSFactoryFinder.getGeometryFactory();
		 * 
		 * //geometryFactory.createPolygon(boundary)
		 * 
		 * Coordinate[] coords = new Coordinate[5]; coords[0] = new
		 * Coordinate(this.selectEnv.getMinX(), this.selectEnv.getMinY()); coords[1] =
		 * new Coordinate(this.selectEnv.getMaxX(), this.selectEnv.getMinY()); coords[2]
		 * = new Coordinate(this.selectEnv.getMaxX(), this.selectEnv.getMaxY());
		 * coords[3] = new Coordinate(this.selectEnv.getMinX(),
		 * this.selectEnv.getMaxY()); coords[4] = new
		 * Coordinate(this.selectEnv.getMinX(), this.selectEnv.getMinY());
		 * 
		 * Geometry geo = geometryFactory.createPolygon(coords);
		 * 
		 * featureBuilder.add(geo);
		 * 
		 * SimpleFeature sf = featureBuilder.buildFeature("");
		 * 
		 * dfcc.add(sf);
		 * 
		 * Layer temp = new FeatureLayer(dfcc, rect); temp.setTitle("rect");
		 * 
		 * List<Layer> layers = this.map.layers();
		 * 
		 * for(Layer ly : layers) { if(ly.getTitle() != null &&
		 * ly.getTitle().equals("rect")) { layers.remove(ly); break; } }
		 * 
		 * this.map.addLayer(temp);
		 * 
		 * this.mapFrame.getMapPane().updateUI(); this.mapFrame.getMapPane().repaint();
		 * 
		 * // this.poi(null);
		 * 
		 * 
		 * // gs.setColor(Color.LIGHT_GRAY); // gs.drawOval(x-diameter/2, y-diameter/2,
		 * diameter, diameter); System.out.println("mouse x="+x+",y="+y+", coord =  " +
		 * centerx+","+centery);
		 */
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	private static SimpleFeatureType createFeatureType() {

		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setName("Location");
		try {
			builder.setCRS(CRS.decode("EPSG:5179"));
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // <- Coordinate reference system

		// add attributes in order
		builder.add("the_geom", Polygon.class);
//        builder.length(15).add("Name", String.class); // <- 15 chars width for name field
//        builder.add("number",Integer.class);

		// build the type
		final SimpleFeatureType LOCATION = builder.buildFeatureType();

		return LOCATION;
	}

	public class FeatureModel extends AbstractTableModel {

		ListFeatureCollection fs = null;

		Object[] array = null;

		Vector<String> selectFields = new Vector();
		Vector<Integer> selectFieldIdxs = new Vector();

		public FeatureModel(ListFeatureCollection _fs, Vector<String> _selectFields) {
			fs = _fs;

			if (_selectFields != null) {
				selectFields = _selectFields;
			}

			SimpleFeatureType sft = fs.getSchema();

			for (int i = 0; i < sft.getAttributeCount(); i++) {
				AttributeDescriptor ad = sft.getDescriptor(i);
				selectFields.add(ad.getName().toString());
				selectFieldIdxs.add(i);
			}

			array = fs.toArray();
		}

		@Override
		public int getRowCount() {
			// TODO Auto-generated method stub
			return this.array.length;
		}

		@Override
		public int getColumnCount() {
			// TODO Auto-generated method stub
			return this.selectFieldIdxs.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			// TODO Auto-generated method stub

			SimpleFeature sf = (SimpleFeature) this.array[rowIndex];

			return sf.getAttribute(selectFieldIdxs.get(columnIndex));
		}

		@Override
		public String getColumnName(int column) {
			return selectFields.get(column);

		}

		@Override
		public boolean isCellEditable(int i, int c) {
			return true;

		}

	}

	public void updateUI() {
		int width = this.mapFrame.getWidth();
		int height = this.mapFrame.getHeight();

		if (repaintTemp == false) {
			width += 1;
			this.mapFrame.resize(width, height);
			repaintTemp = true;
		} else {
			width -= 1;
			this.mapFrame.resize(width, height);
			repaintTemp = false;
		}

	}

}
