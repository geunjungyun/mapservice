package com.gis2.storage;

import java.awt.Color;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;

import javax.imageio.ImageIO;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.io.FileUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.data.file.FileLayer;
import com.data.util.MapLog;
import com.gis.map.render.ImagePool;
import com.gis.map.render.RasterRender;
import com.gis.map.render.RasterRenderPool;
//import com.gis.protocol.freegis3.GisPlatform;
//import com.gis.protocol.freegis3.ServiceDataJob;
//import com.gis.protocol.freegis3.Storage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.util.io.FileUt;


public class StorageMng {
	
	static Hashtable<String, FileLayer> fls = new Hashtable();

	static HashMap<String, BufferedImage> symbols = new HashMap();
	static HashMap<String, String> symbolNotExist = new HashMap();
	
	static HashMap<String, Font> fonts = new HashMap();

	//static String layerPath = "";
	static public String symbolPath = "";
	
	static public String fontPath = "";
	
	static HashMap<String, GridCoverage2D> gc = new HashMap();
	
	public static int threadCnt = 50;
	
	public static Object lock = new Object();
	
	public static CoordinateReferenceSystem crs_5179 = null;
	
	public static GenericObjectPool<RasterRender> rasterPool = null; 
	
	public static RasterRender borrowRasterRender() throws Exception{
		synchronized(lock) {
			if(rasterPool == null) {
				GenericObjectPoolConfig gop = new GenericObjectPoolConfig();
				gop.setMaxTotal(threadCnt+5);
				gop.setMaxWaitMillis(2000);
				rasterPool = new GenericObjectPool<RasterRender>(new RasterRenderPool(), gop);
			}
		}
		return rasterPool.borrowObject();
	}
	
	public static void returnRasterRender(RasterRender render) {
		rasterPool.returnObject(render);
	}
	
	public static CoordinateReferenceSystem getCrs5179() {
		synchronized(lock) {
			if(crs_5179 == null)
				try {
					crs_5179 = CRS.decode("EPSG:5179");
				} catch (NoSuchAuthorityCodeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FactoryException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return crs_5179;
	}

	/**
	 * 
	 * @param name 절대 패스
	 * @param readOnly
	 * @return
	 */
	public static FileLayer getFileLayer(String name, boolean readOnly) {
		
		if (!fls.containsKey(name.toLowerCase())) {

			FileLayer fileReader;
			try {
				String readName = "";
				if(name.indexOf("grid:") > -1) {
					File file = new File(name);
					File parentFile = file.getParentFile();
					File version = parentFile.getParentFile();
					String gridPath = version.getAbsolutePath()+FileUt.SEPERATOR+"tiles"
					+FileUt.SEPERATOR+"grid"+FileUt.SEPERATOR+file.getName().replaceAll("grid:", "");
					readName = gridPath;
				}
				else {
					readName = name;
				}
				//System.out.println("readName = " + readName);
				try {
					FileLayer.fixGeometry(readName);
				}
				catch(Exception e) {
					
				}
				
				fileReader = new FileLayer(readName, null);
				
				
				fls.put(name.toLowerCase(), fileReader);

				MapLog.getSCLog().debug("filelayer read ok=" + name);
				return fileReader;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				
				MapLog.getSCLog().debug("filelayer read fail =" + name);
				return null;
			}

		}
		return fls.get(name.toLowerCase());
	}
	


	public static synchronized void removeFileLayer() {
		Set set = fls.keySet();
		Iterator it = set.iterator();
		while (it.hasNext()) {
			Object key = it.next();
			FileLayer fileReader = fls.get(key);
			if (fileReader != null) {
				try {
					fileReader.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
		fls.clear();
	}


	public static Font getFont(String _name, int style, float _size, float tracking, 
			boolean ligature, boolean kerning, float widthRatio){

		float size = _size;
		
		String name = _name;
		
		String fontKeyName = name + "_" + style + "_" + size + "_" + tracking + "_" + ligature
				+ "_" + kerning+"_"+widthRatio;
		
//		System.out.println("font="+name+",style="+style+",size="+size+",tracing="+tracking+
//				",ligature="+ligature+",kerning="+kerning+",widthRatio="+widthRatio);
		
		if (!fonts.containsKey(fontKeyName)) {
			try {

				
				
				Font font = null;
				
				if(name.endsWith(".ttf")) {
					font = Font.createFont(Font.TRUETYPE_FONT, new File(StorageMng.fontPath+File.separator+name));
				}
				else {
					font = new Font(name, style, (int) size);	
				}
				
				//font = new Font("나눔고딕 BOLD", Font.PLAIN, (int) size);
				
				
				if (tracking != 0 || ligature == true || kerning == true) {

					Map<TextAttribute, Object> attributes = new HashMap<TextAttribute, Object>();
					
					attributes.put(TextAttribute.SIZE, size);
					
					if (tracking != 0) {
						attributes.put(TextAttribute.TRACKING, tracking);
					}
					if (kerning) {
						attributes.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
					}
					if (ligature) {
						attributes.put(TextAttribute.LIGATURES, TextAttribute.LIGATURES_ON);
					}
			    	if(widthRatio != 1){
			    		attributes.put(TextAttribute.WIDTH, widthRatio);
			    	}
			    	
					font = font.deriveFont(attributes);
				}
				
				if (font != null) {
					fonts.put(fontKeyName, font);
				}

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return fonts.get(fontKeyName);
	}

	public static BufferedImage getSymbol(String name) {
		
		if (!symbols.containsKey(name)) {
			
			File symbolFile = new File(name);
			
			if(!symbolFile.exists() && symbolFile.isAbsolute()) {
				//System.out.println("sybmol not exist=" + symbolFile.getAbsolutePath());
				return null;
			}
			
			if(!symbolFile.exists() && !symbolFile.isAbsolute()) {
				symbolFile = new File(symbolPath +FileUt.SEPERATOR+ name.replaceAll(":", Matcher.quoteReplacement(FileUt.SEPERATOR+"")));
			}
			
			//File symbolFile = new File(symbolPath +FileUt.SEPERATOR+ name.replaceAll(":", FileUt.SEPERATOR+""));
			
			if (symbolFile.exists() == false && !symbolFile.isAbsolute()) {
				symbolFile = new File(symbolPath + name.replaceAll(":", Matcher.quoteReplacement(FileUt.SEPERATOR+"")).replaceAll(".bmp", ".png"));
				if (symbolFile.exists() == false) {
					return null;
				}
				if (symbolFile.exists() == false) {
					return null;
				}
			}
			
			try {
				BufferedImage bi = ImageIO.read(symbolFile);
				symbols.put(name, bi);
			} catch (IOException e) {
				
				if(!symbolNotExist.containsKey(symbolFile.getAbsolutePath())) {
					symbolNotExist.put(symbolFile.getAbsolutePath(), symbolFile.getAbsolutePath());
					MapLog.getSCLog().debug("sybmol not exist=" + symbolFile.getAbsolutePath());
				}
				//System.out.println("sybmol not exist=" + symbolFile.getAbsolutePath());
				//MapLog.getSCLog().debug("sybmol not exist=" + symbolFile.getAbsolutePath());
			}
		}
		return symbols.get(name);
	}
	
	
	public static GridCoverage2D getGridCoverage2D(String name) {
		
		if (!gc.containsKey(name.toLowerCase())) {

			File oriLayerFile = new File(name+".tif");
			
			if (!oriLayerFile.exists()) {
				MapLog.getSCLog().debug("GridCoverage2D read fail=" + oriLayerFile.getAbsolutePath());
				return null;
			}
			
			try {
				
				if(oriLayerFile.length() > 1024*1024*1024 ) {
					throw new Exception(oriLayerFile.getAbsolutePath() +" Greater than 1 GByte in size");
				}
				
				//System.out.println("getGridCoverage2D : " + oriLayerFile.getAbsolutePath());
				
				AbstractGridFormat format = GridFormatFinder.findFormat(oriLayerFile);
				GridCoverage2DReader reader = format.getReader(oriLayerFile);
				if (reader == null) {
					MapLog.getSCLog().debug("GrideCoverage2D reader is null = " + oriLayerFile.getAbsolutePath());
					return null;
				}
				
				
				GridCoverage2D gc2d = reader.read(null);
				
				gc.put(FileUt.getOnlyName(name), gc2d);
				
				MapLog.getSCLog().debug("GridCoverage2D read ok=" + oriLayerFile.getAbsolutePath());
				
				return gc2d;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				MapLog.getSCLog().debug("GridCoverage2D read fail=" + oriLayerFile.getAbsolutePath());
				e.printStackTrace();
				return null;
			}
		}
		return gc.get(FileUt.getOnlyName(name).toLowerCase());
		
	}
	

}
