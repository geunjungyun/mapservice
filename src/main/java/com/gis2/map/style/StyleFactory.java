package com.gis2.map.style;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.ecql.ECQL;


import com.gis.protocol.freegis3.Cap;
//import com.gis.engine.admin.folder.RootFolder;
import com.gis.protocol.freegis3.FillStyle;
import com.gis.protocol.freegis3.HatchPattern;
import com.gis.protocol.freegis3.HatchType;
import com.gis.protocol.freegis3.Join;
import com.gis.protocol.freegis3.LineStyle;
import com.gis.protocol.freegis3.MarkStyle;
import com.gis.protocol.freegis3.MultiLine;
import com.gis.protocol.freegis3.PolygonStyle;
import com.gis.protocol.freegis3.PolylineStyle;
import com.gis.protocol.freegis3.Query;
import com.gis.protocol.freegis3.RasterStyle;
import com.gis.protocol.freegis3.Styles;
import com.gis.protocol.freegis3.SymbolStyle;
import com.gis.protocol.freegis3.TextStyle;
//import com.gis.protocol.freegis2.PointStyle;
import com.gis2.storage.StorageMng;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

//import org.freegis.PointStyle;
//import org.freegis.PolygonStyle;
//import org.freegis.PolylineStyle;

public class StyleFactory {
	String mapInfoName;
	//static StorageMng sm = null;
	
	public StyleFactory(StorageMng sm){
	}
	
	public static float getHdScale() {
		return com.gis2.storage.TileServiceMng.hdMode ? 2.0f : 1.0f;
	}
	
	public static BufferedImage scaleSymbol(BufferedImage src, float scale) {
		if(src == null || scale <= 1.0f) return src;
		int newW = (int)(src.getWidth() * scale);
		int newH = (int)(src.getHeight() * scale);
		BufferedImage scaled = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = scaled.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.drawImage(src, 0, 0, newW, newH, null);
		g2.dispose();
		return scaled;
	}
	
	public static void loadStyles(Styles stls, HashMap<String, Style> nStyles){
		
		float fontSize = Integer.MIN_VALUE;
		List<com.gis.protocol.freegis3.PointStyle> pss = stls.getPointStyle();
		for(com.gis.protocol.freegis3.PointStyle ps : pss){
			if(!nStyles.containsKey(ps.getName())){
				
				PointStyle bse = (PointStyle) StyleFactory.createPointStyle(ps, stls);
				nStyles.put(bse.getName(), bse);
				if(fontSize < bse.getFontSize()) {
					fontSize = bse.getFontSize();
				}
			}
		}
		
		//System.out.println("------------- max font size = " + fontSize);
		
		for(com.gis.protocol.freegis3.PointStyle ps : pss){
			List<Query> querys = ps.getQuery();
			
			if(querys != null && querys.size() == 1) {
				Query query = querys.get(0);
				if(query.getStyleName() == null || query.getStyleName().trim().length() == 0) {
					BasicStyleExtend bse = (BasicStyleExtend) nStyles.get(ps.getName());
					bse.setCql(query.getCql());
					continue;
				}
			}
			
			
			if(querys != null && querys.size() > 0){
				BasicStyleExtend bse = (BasicStyleExtend) nStyles.get(ps.getName());
				Vector<QueryStyle> bseVector = new Vector();
				for(Query query : querys){
					QueryStyle qs = new QueryStyle();
					
					if(query.getCql() != null && query.getCql().length() > 0) {
						try {
							qs.filter = ECQL.toFilter(query.getCql());
							qs.cql = query.getCql();
							
						} catch (CQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else {
						if(query.getName() == null || query.getValue() == null) {
							//System.out.println("error, query name = " + query.getName() +", value = " + query.getValue() +", styleName = " + query.getStyleName());
							//continue;
						}
						else {
							qs.name = query.getName().toLowerCase();
							qs.value = query.getValue();
						}
					}
					
					if(query.getOverlapConfig() != null){
						
						if(query.getOverlapConfig().getDeleteOverlapMode() != null){
							qs.deleteOverlapMode = query.getOverlapConfig().getDeleteOverlapMode().toString();
						}
						if(query.getOverlapConfig().getExtensionArea() != null 
								&& query.getOverlapConfig().getExtensionArea() > 0){
							qs.extenstionArea = query.getOverlapConfig().getExtensionArea();
						}
					}
					if(query.getPriority() != null){
						qs.priority = query.getPriority();
					}
					qs.priority = query.getPriority();
					
					BasicStyleExtend subBse = (BasicStyleExtend) nStyles.get(query.getStyleName());
					
					//subBse.clone();
					
					if (subBse == null && query.getPriority() == -1) {
						System.out.println("PointStyle name=" + ps.getName() + ", subInfo name = " + query.getName() + ", value = " + query.getValue() + ", style = "
								+ query.getStyleName());
						subBse = bse;
					} else {
						//System.out.println(subBse + " ===== " + query.getName() + "====" + query.getValue() + "====" + query.getPriority());
						
						PointStyle subps = (PointStyle)subBse;
						
						boolean createMode = false;
						
						if(query.getOverlapConfig() != null || query.getPriority() != null){
							createMode = true;
						}
						
						if(createMode){
							
							for(com.gis.protocol.freegis3.PointStyle subTemp : pss){
								if(subps.getName().equals(subTemp.getName())){
									PointStyle cloneBse = (PointStyle)StyleFactory.createPointStyle(subTemp, stls);
									
									if(query.getOverlapConfig() != null && query.getOverlapConfig().getExtensionArea() != null){
										cloneBse.extenstionArea = qs.extenstionArea;
									}
									if(query.getPriority() != null){
										cloneBse.priority = qs.priority;
									}
									if(query.getOverlapConfig() != null && query.getOverlapConfig().getDeleteOverlapMode() != null){
										cloneBse.deleteOverlapMode = qs.deleteOverlapMode;
									}
									subBse = cloneBse;
									break;
								}
							}
						}

						/*
						com.gis.map.style.PointStyle baseClone = (com.gis.map.style.PointStyle) bse.clone();
	
						if (query.getPriority() == -1) {
							baseClone.setCompare(subBse);
							subBse = baseClone;
						} else if (query.getStyleName() != null && query.getStyleName().trim().length() > 0) {
							baseClone.setCompare(subBse);
							baseClone.priority = query.getPriority();
							subBse = baseClone;
						} else {
							baseClone.priority = query.getPriority();
							subBse = baseClone;
						}
						*/
						
					}
					
					
					
					qs.style = subBse;
					bseVector.add(qs);
				}
				bseVector.sort((o1, o2) -> Integer.compare(o2.priority, o1.priority));
				bse.setQuerys(bseVector);
			}
			
		}
		
		
		List<PolygonStyle> pls = stls.getPolygonStyle();
		for(PolygonStyle pl : pls){
			if(!nStyles.containsKey(pl.getName())){
				BasicStyleExtend bse = StyleFactory.createPolygonStyle(pl, stls);
				nStyles.put(bse.getName(), bse);
			}
		}
		
		for(PolygonStyle ps : pls){
			List<Query> querys = ps.getQuery();
			
			
			if(querys != null && querys.size() == 1) {
				Query query = querys.get(0);
				if(query.getStyleName() == null || query.getStyleName().trim().length() == 0) {
					BasicStyleExtend bse = (BasicStyleExtend) nStyles.get(ps.getName());
					bse.setCql(query.getCql());
					continue;
				}
			}

			
			
			
			if(querys != null && querys.size() > 0){
				BasicStyleExtend bse = (BasicStyleExtend) nStyles.get(ps.getName());
				Vector<QueryStyle> bseVector = new Vector();
				for(Query query : querys){
					QueryStyle qs = new QueryStyle();
//					qs.name = query.getName();
//					qs.value = query.getValue();
					
					if(query.getCql() != null && query.getCql().length() > 0) {
						try {
							qs.filter = ECQL.toFilter(query.getCql());
							qs.cql = query.getCql();
//							
//							System.out.println();
//							
						} catch (CQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else {
						
//						if(query.getName() == null || query.getValue() == null) {
//							System.out.println("error, query name = " + query.getName() +", value = " + query.getValue() +", styleName = " + query.getStyleName());
//							continue;
//						}
//						
//						qs.name = query.getName().toLowerCase();
//						qs.value = query.getValue();
						
						if(query.getName() == null || query.getValue() == null) {
							System.out.println("error, query name = " + query.getName() +", value = " + query.getValue() +", styleName = " + query.getStyleName());
							//continue;
						}
						else {
							qs.name = query.getName().toLowerCase();
							qs.value = query.getValue();
						}
						
						
					}
					
					qs.priority = query.getPriority();
					BasicStyleExtend subBse = (BasicStyleExtend) nStyles.get(query.getStyleName());
					if (subBse == null && query.getPriority() == -1) {
						System.out.println("PolygonStyle name=" + ps.getName() + ", subInfo name = " + query.getName() + ", value = " + query.getValue() + ", style = "
								+ query.getStyleName());
						subBse = bse;
					} else {
						
						/*
						com.gis.map.style.PointStyle baseClone = (com.gis.map.style.PointStyle) bse.clone();
	
						if (query.getPriority() == -1) {
							baseClone.setCompare(subBse);
							subBse = baseClone;
						} else if (query.getStyleName() != null && query.getStyleName().trim().length() > 0) {
							baseClone.setCompare(subBse);
							baseClone.priority = query.getPriority();
							subBse = baseClone;
						} else {
							baseClone.priority = query.getPriority();
							subBse = baseClone;
						}
						*/
						
					}
					qs.style = subBse;
					bseVector.add(qs);
				}
				bseVector.sort((o1, o2) -> Integer.compare(o2.priority, o1.priority));
				bse.setQuerys(bseVector);
			}
			
		}

		
		List<PolylineStyle> pgs = stls.getPolylineStyle();
		for(PolylineStyle pg : pgs){
			if(!nStyles.containsKey(pg.getName())){
				BasicStyleExtend bse = StyleFactory.createPolylineStyle(pg, stls);
				
				nStyles.put(bse.getName(), bse);
			}
		}
		
		for(PolylineStyle ps : pgs){
			List<Query> querys = ps.getQuery();
			
			
			if(querys != null && querys.size() == 1) {
				Query query = querys.get(0);
				if(query.getStyleName() == null || query.getStyleName().trim().length() == 0) {
					BasicStyleExtend bse = (BasicStyleExtend) nStyles.get(ps.getName());
					bse.setCql(query.getCql());
					continue;
				}
			}

			
			if(querys != null && querys.size() > 0){
				BasicStyleExtend bse = (BasicStyleExtend) nStyles.get(ps.getName());
				Vector<QueryStyle> bseVector = new Vector();
				for(Query query : querys){
					QueryStyle qs = new QueryStyle();
//					qs.name = query.getName();
//					qs.value = query.getValue();
					
					if(query.getCql() != null && query.getCql().length() > 0) {
						try {
							qs.filter = ECQL.toFilter(query.getCql());
							qs.cql = query.getCql();
						} catch (CQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else {
//						if(query.getName() == null || query.getValue() == null) {
//							System.out.println("error, query name = " + query.getName() +", value = " + query.getValue() +", styleName = " + query.getStyleName());
//							continue;
//						}
//						qs.name = query.getName().toLowerCase();
//						qs.value = query.getValue();
						
						if(query.getName() == null || query.getValue() == null) {
							System.out.println("error, query name = " + query.getName() +", value = " + query.getValue() +", styleName = " + query.getStyleName());
							//continue;
						}
						else {
							qs.name = query.getName().toLowerCase();
							qs.value = query.getValue();
						}
						
					}					
					
					
					
					qs.priority = query.getPriority();
					BasicStyleExtend subBse = (BasicStyleExtend) nStyles.get(query.getStyleName());
					if (subBse == null && query.getPriority() == -1) {
						System.out.println("PolylineStyle name=" + ps.getName() + ", subInfo name = " + query.getName() + ", value = " + query.getValue() + ", style = "
								+ query.getStyleName());
						subBse = bse;
					} else {
						
						/*
						com.gis.map.style.PointStyle baseClone = (com.gis.map.style.PointStyle) bse.clone();
	
						if (query.getPriority() == -1) {
							baseClone.setCompare(subBse);
							subBse = baseClone;
						} else if (query.getStyleName() != null && query.getStyleName().trim().length() > 0) {
							baseClone.setCompare(subBse);
							baseClone.priority = query.getPriority();
							subBse = baseClone;
						} else {
							baseClone.priority = query.getPriority();
							subBse = baseClone;
						}
						*/
						
					}
					qs.style = subBse;
					bseVector.add(qs);
				}
				
				bseVector.sort((o1, o2) -> Integer.compare(o2.priority, o1.priority));
				bse.setQuerys(bseVector);
			}
			
		}
		
		List<RasterStyle> rss = stls.getRasterStyle();
		for (RasterStyle rs: rss) {
			if (!nStyles.containsKey(rs.getName())) {
				BasicStyleExtend bse = StyleFactory.createRasterStyle(rs);
				
				nStyles.put(bse.getName(), bse);
			}
		}

	}

	
	static public BasicStyleExtend createPointStyle(com.gis.protocol.freegis3.PointStyle ps_, com.gis.protocol.freegis3.Styles style){
		PointStyle ps = new PointStyle();
		ps.priorityFieldName = "";
		ps.priority = ps_.getPriority();
		ps.positionType = ps_.getPositionType();
		
		if(ps_.getOverlapConfig() != null){
			ps.deleteOverlapMode = ps_.getOverlapConfig().getDeleteOverlapMode().toString();
			if(ps_.getOverlapConfig().getExtensionArea() != null){
				ps.extenstionArea = ps_.getOverlapConfig().getExtensionArea();	
			}
			else{
				ps.extenstionArea = 0;
			}
			
		}
		
		if(ps_.getOffsetX() != null){
			ps.offsetX = ps_.getOffsetX();
		}
		if(ps_.getOffsetY() != null){
			ps.offsetY = ps_.getOffsetY();
		}
		
		if(ps_.isShapeDraw() != null && ps_.isShapeDraw()){
			ps.shapeDraw = true;
		}
		else if(ps_.isShapeDraw() != null && !ps_.isShapeDraw()){
			ps.shapeDraw = false;
		}
		
		ps.setName(ps_.getName());
		
		ps.setDesc(ps.getDesc());

		if (ps_.getMarkStyle() != null) {
			MarkStyle ms = ps_.getMarkStyle();
			
			if(ms.getFillStyle() != null || ms.getLineStyle() != null) {
				ps.setRenderingMark(true);
			}
			else {
				ps.setRenderingMark(false);
			}
			
			ps.markSize = (int)(ms.getSize() * getHdScale());
			if(ms.getFillStyle() != null) {
				setFillStyle(ps.getMarkerStyle(), ms.getFillStyle());
			}
			if(ms.getLineStyle() != null) {
				setLineStyle(ps.getMarkerStyle(), ms.getLineStyle());
			}
			

			
			/*
			else {
				if (ms.getColor() != null && ms.getColor().length() > 0) {
					
					if(!ms.getColor().startsWith("#")) {
						String[] rgbf = ms.getColor().split(",");
						if (rgbf.length != 3) {
							rgbf = ms.getColor().split("/");
						}
						ps.markColor = new Color(Integer.parseInt(rgbf[0].trim()),
								Integer.parseInt(rgbf[1].trim()), Integer.parseInt(rgbf[2]
										.trim()));
					}
					else {
						ps.markColor = Color.decode(ms.getColor());
					}
					ps.setRenderingMark(true);
			
				} else {
					ps.setRenderingMark(false);
				}
			}
			*/
		}
		
		if(ps_.getSymbolStyle() != null){
			SymbolStyle ss = ps_.getSymbolStyle();
			
			if (ss.getSymbolName() != null) {
				
				BufferedImage bi = StorageMng.getSymbol((style.getSymbolPath() != null ? style.getSymbolPath() + File.separator : "") + ss.getSymbolName());
				if(bi != null){
					bi = scaleSymbol(bi, getHdScale());
					ps.setSymbol(bi);
					ps.setRenderingSymbol(true);
				}
				else{
					ps.setRenderingSymbol(false);
				}
				
			}
			if (ss.getFieldName() != null) {
				ps.symbolNameField = ss.getFieldName().toLowerCase();
				ps.setRenderingSymbol(true);
			}
			
			if(style != null && style.getSymbolPath() != null && style.getSymbolPath().trim().length() > 0 ){
				ps.symbolPath = style.getSymbolPath();
			}
			
			if(ss.getWidth() != null && ss.getWidth() > 0) {
				ps.symbolWidth = (int)(ss.getWidth() * getHdScale());
			}
			
			if(ss.getHeight() != null && ss.getHeight() > 0) {
				ps.symbolHeight = (int)(ss.getHeight() * getHdScale());
			}
			
		}
		
		if (ps_.getTextStyle() != null) {
			TextStyle ts = ps_.getTextStyle();
			
			if(ts.getMultiLineSize() != null){
				ps.multiLineSize = ts.getMultiLineSize();
			}
			
			if(ts.getMultiLineSeperator() != null) {
				ps.multiLineSeperator = ts.getMultiLineSeperator();
			}
			
//			if(ts.getMultiLine() != null){
//				MultiLine ml = ts.getMultiLine();
//				if(ml.getLineMaxSize() != null && ml.getLineMaxSize() > 0){
//					ps.multiLineSize = ml.getLineMaxSize();
//				}
//				if(ml.getSeparateString() != null){
//					for(String separateS : ml.getSeparateString()){
//						ps.seperateStrings.add(separateS);
//					}
//				}
//				if(ml.isIsSeparateUnicode() != null && ml.isIsSeparateUnicode() == true){
//					ps.isSeperateUnicode = ml.isIsSeparateUnicode();
//				}
//			}
			
			if(ts.getRotateField() != null &&  ts.getRotateField().length() > 0){
				ps.rotateField = ts.getRotateField().toLowerCase();
			}
			
			if(ts.getRotate() != null && ts.getRotate() > 0){
				ps.rotate = ts.getRotate();
			}
			
			ps.fontName = ts.getFontName();
			//ps.fontSize = ts.getFontSize()*0.7f;
			ps.setFontSize(ts.getFontSize() * getHdScale());
			//ps.setFontSize(ts.getFontSize()+5);
			//서울시 웹 지도
			//ps.fontSize = (int)(ts.getFontSize()*(0.8));
			//ps.nameField = ts.getFieldName().replace("\"", "").toLowerCase();
			ps.nameField = ts.getFieldName() != null ? ts.getFieldName().replace("\"", "") : null;
			
			if(ts.getFontType() == null){
				//System.out.println("plain="+ps_.getName());
				ps.fontType = Font.BOLD;
			}
			else if (ts.getFontType().toUpperCase().equals("BOLD")) {
				ps.fontType = Font.BOLD;
			} else if (ts.getFontType().toUpperCase().equals("ITALIC")) {
				ps.fontType = Font.ITALIC;
			} else if (ts.getFontType().toUpperCase().equals("PLAIN")) {
				ps.fontType = Font.PLAIN;
			}
			
			if(ts.getTracking() != null){
				ps.tracking = ts.getTracking();
			}
			
			if(ts.getLinespace() != null){
				ps.linespace = ts.getLinespace();
			}
			
			if(ts.isLigature() != null){
				ps.ligature = ts.isLigature();
			}
			
			if(ts.isKerning() != null){
				ps.kerning = ts.isKerning();
			}
			
			if(ts.getWidthRatio() != null) {
				ps.widthRatio = ts.getWidthRatio();
			}
			
			if(ts.getLineExtensionWidthRatio() != null && ts.getLineExtensionWidthRatio() > 0) {
				ps.lineExtensionWidthRatio = ts.getLineExtensionWidthRatio();
			}
			else {
				if(style != null && style.getLineExtensionWidthRatio() != null) {
					ps.lineExtensionWidthRatio = style.getLineExtensionWidthRatio();
				}
				
			}
			
			if(ts.getLineIntervalPixel() != null && ts.getLineIntervalPixel() > 0) {
				ps.lineIntervalPixel = ts.getLineIntervalPixel();
			}
			else {
				if(style != null && style.getLineIntervalPixel() != null) {
					ps.lineIntervalPixel = style.getLineIntervalPixel();
				}
			}

			if(ts.isLineOverflow() != null) {
				ps.lineOverflow = ts.isLineOverflow();
			}
			else {
				if(style != null && style.isLineOverflow() != null) {
					ps.lineOverflow = style.isLineOverflow();
				}
			}
			
//			ps.tracking = ts.getTracking() != null ? ts.getTracking() : 0.0f;
//			
//			ps.linespace = ts.getLinespace() != null ? ts.getLinespace() : 0.0f;
//			
//			ps.ligature = ts.isLigature() != null ? ts.isLigature() : false;
//			
//			ps.kerning = ts.isKerning() != null ? ts.isKerning() : false;
			
			
			//float tracking, boolean ligature, boolean kerning
			
			if(ps.fontName == null || ps.fontName.length() == 0) {
				//ps.fontName = "나눔고딕";
				ps.fontName = "Noto Sans KR";
				
			}
			
			
			Font font = StorageMng.getFont(ps.fontName , ps.fontType, ps.getFontSize(), ps.tracking, ps.ligature, ps.kerning, ps.widthRatio);
			if(font == null){
				System.out.println("font is null, name = " + ps.fontName);
			}
			if(font != null){
				ps.setFont(font);
				ps.setRenderingText(true);
			}
			
			ps.setTextNameField(ps_.getTextStyle().getFieldName());
			
			FillStyle fs = ts.getFillStyle();
			
			if(fs != null){
				setFillStyle(ps, fs);
				ps.setRenderingFill(true);
				
			}
			else{
				ps.setRenderingFill(false);
			}
			
			LineStyle ls = ts.getLineStyle();
			
			//System.out.println("Point Style name = " +ps.name +", lineStyle = " + ls);
			if(ls != null && ls.getWidth() > 0){
				
				//ls.setWidth(ls.getWidth());
				
				setLineStyle2(ps, ls);
				ps.outLineFont = StorageMng.getFont(ps.fontName, ps.fontType, ps.getFontSize()+ls.getWidth()*getHdScale(), 
						ps.tracking, ps.ligature, ps.kerning, ps.widthRatio);
				ps.setRenderingLine(true);
			}
			else{
				ps.setRenderingLine(false);
			}
			
		}
		
//		if (ps_.getTransparency() > 0) {
//			ps.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ps_.getTransparency() * 0.01f));
//			ps.setRenderingComposite(true);
//		} else {
//			ps.setRenderingComposite(false);
//		}
		
		if (ps_.getTransparency() > 0) {
			ps.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (100-ps_.getTransparency()) * 0.01f));
			ps.setRenderingComposite(true);
		}
		
		if(ps.positionType == null) {
			String pt = "";
			
			if(ps_.getMarkStyle() != null) {
				pt += "c";
			}
			else {
				pt += "x";
			}
			
			if(ps_.getSymbolStyle() != null) {
				pt += "c";
			}
			else {
				pt += "x";
			}
			
			if(ps_.getTextStyle() != null) {
				pt += "c";
			}
			else {
				pt += "x";
			}
			
			ps.positionType = pt;
		}


		return ps;
	}
	
	static public void setFillStyle(BasicStyleExtend bse, FillStyle fs){
		if (fs.getColor() != null && fs.getColor().length() > 0) {
			
			if(!fs.getColor().startsWith("#")) {
				String[] rgbf = fs.getColor().split(",");
				if (rgbf.length != 3) {
					rgbf = fs.getColor().split("/");
				}
				if(rgbf.length == 3) {
					bse.setFillColor(new Color(Integer.parseInt(rgbf[0].trim()), Integer
							.parseInt(rgbf[1].trim()), Integer.parseInt(rgbf[2].trim())));
				}
				else {
					System.out.println("error, "+ fs.getColor());
				}
			}
			else {
				bse.setFillColor(Color.decode(fs.getColor()));
			}
			bse.setRenderingFill(true);
			
			if(fs.getWidth() > 0){
				bse.setFillStroke(fs.getWidth() * getHdScale());
			}
			
		} else {
			bse.setRenderingFill(false);
		}
		
//		if (fs.getTransparency() > 0) {
//		
//			bse.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fs.getTransparency() * 0.01f));
//			bse.setRenderingComposite(true);
//		} else {
//			bse.setRenderingComposite(false);
//		}
		
		if (fs.getTransparency() > 0) {
//			bse.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (100-fs.getTransparency()) * 0.01f));
//			bse.setRenderingComposite(true);
			bse.setFillTransparency(fs.getTransparency());
		}
		
		if (fs.getTexturePattern() != null) {
			
			BufferedImage bi = StorageMng.getSymbol((bse.symbolPath != null ? bse.symbolPath+File.separator : "") 
					+ fs.getTexturePattern());
			Rectangle2D anchor = new Rectangle2D.Double(0, 0,
					2 * bi.getWidth(), 2 * bi.getHeight());
			TexturePaint fillPattern = new TexturePaint(bi, anchor);
			
			bse.setFillPattern(fillPattern);
			bse.setRenderingFillPattern(true);
			//bse.setRenderingFill(true);
		} 
		else if(fs.getHatchPattern() != null){
			HatchPattern hp = fs.getHatchPattern();
			//hp.getColor();
			
			Color lineColor = null;
			
			if (hp.getColor() != null && hp.getColor().length() > 0) {
				if(!hp.getColor().startsWith("#")) {
					String[] rgbf = hp.getColor().split(",");
					if (rgbf.length != 3) {
						rgbf = hp.getColor().split("/");
					}
					
					lineColor = new Color(Integer.parseInt(rgbf[0].trim()), Integer
							.parseInt(rgbf[1].trim()), Integer.parseInt(rgbf[2].trim()));
				}
				else {
					lineColor = Color.decode(hp.getColor());
				}
			}
			
			Stroke stroke = new BasicStroke(hp.getLineWidth());
			
			BufferedImage image = new BufferedImage(hp.getWidth(), hp.getWidth(), BufferedImage.TYPE_INT_ARGB);
			
			Graphics2D  g = image.createGraphics();
			
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
			g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
			g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			
			g.setStroke(stroke);
		
			g.setColor(lineColor);
			
			if(hp.getType() != null) {
				if(hp.getType().equals(HatchType.HORIZONTAL)) {
					g.drawLine(0, hp.getWidth()/2, hp.getWidth(), hp.getWidth()/2);
				}
				else if(hp.getType().equals(HatchType.VERTICAL)) {
					g.drawLine(hp.getWidth()/2, 0, hp.getWidth()/2, hp.getWidth());
				}
				else if(hp.getType().equals(HatchType.DIAGONAL)) {
					g.drawLine(0, hp.getWidth(), hp.getWidth(), 0);
				}
			}
		
			Rectangle2D anchor = new Rectangle2D.Double(0, 0,hp.getWidth(), hp.getWidth());
			
			TexturePaint fillPattern = new TexturePaint(image, anchor);
			
			bse.setFillPattern(fillPattern);
			bse.setRenderingFillPattern(true);
			//bse.setRenderingFill(true);
		}
		else {
			bse.setRenderingFillPattern(false);
		}
		
		if(fs.isGradient() != null && fs.isGradient()) {
			bse.setGradient(fs.isGradient());
		}

	}
	
	static public void setLineStyle2(BasicStyleExtend bse, LineStyle fs){
		if (fs.getColor() != null && fs.getColor().length() > 0) {
			if(!fs.getColor().startsWith("#")) {
				String[] rgbf = fs.getColor().split(",");
				if (rgbf.length != 3) {
					rgbf = fs.getColor().split("/");
				}
				
				bse.setLineColor(new Color(Integer.parseInt(rgbf[0].trim()), Integer
						.parseInt(rgbf[1].trim()), Integer.parseInt(rgbf[2].trim())));
			}
			else {
				bse.setLineColor(Color.decode(fs.getColor()));
			}
			
			if (fs.getWidth() > 0) {
				bse.setLineWidth((fs.getWidth()+0.5f) * getHdScale());
				bse.setRenderingLine(true);
			} else {
				bse.setRenderingLine(false);
			}
		} else {
			bse.setRenderingLine(false);
		}

//		if (fs.getTransparency() > 0) {
//			bse.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
//					fs.getTransparency() * 0.01f));
//			bse.setRenderingComposite(true);
//		} else {
//			bse.setRenderingComposite(false);
//		}
		
		if (fs.getTransparency() > 0) {
			bse.setLineTransparency(fs.getTransparency());
//			bse.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,(100-fs.getTransparency()) * 0.01f));
//			bse.setRenderingComposite(true);
		}

		if (fs.getPattern() != null && fs.getPattern().length() > 0) {
			String[] pattterns = fs.getPattern().split(",");
			if (pattterns.length >= 2) {
				bse.setRenderingLinePattern(true);
				bse.setLinePattern(fs.getPattern());
				bse.setRenderingFill(false);
			} else {
				pattterns = fs.getPattern().split("/");
				if (pattterns.length >= 2) {
					bse.setRenderingLinePattern(true);
					bse.setLinePattern(fs.getPattern());
					bse.setRenderingFill(false);
				}
			}
		} else {
			bse.setRenderingLinePattern(false);
		}
	}


	static public void setLineStyle(BasicStyleExtend bse, LineStyle fs){
		
		
		if (fs.getColor() != null && fs.getColor().length() > 0) {
			if(!fs.getColor().startsWith("#")) {
				String[] rgbf = fs.getColor().split(",");
				if (rgbf.length != 3) {
					rgbf = fs.getColor().split("/");
				}
				if(rgbf.length == 3) {
					bse.setLineColor(new Color(Integer.parseInt(rgbf[0].trim()), Integer
							.parseInt(rgbf[1].trim()), Integer.parseInt(rgbf[2].trim())));
				}
			}
			else {
				bse.setLineColor(Color.decode(fs.getColor()));
			}
			
			if (fs.getWidth() > 0) {
				bse.setLineWidth(fs.getWidth() * getHdScale());
				bse.setRenderingLine(true);
			} else {
				bse.setRenderingLine(false);
			}
			
			
			if(fs.getShapePattern() != null) {
				String shapePattern = fs.getShapePattern();
				
				String[] array  = shapePattern.split(",");
				
				bse.setShapeStroke(Integer.parseInt(array[0]), Integer.parseInt(array[1]), 
						Integer.parseInt(array[2]), Integer.parseInt(array[3]));
				
			}
			
			
		} else {
			bse.setRenderingLine(false);
		}
		
		
		

//		if (fs.getTransparency() > 0) {
//			bse.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
//					fs.getTransparency() * 0.01f));
//			bse.setRenderingComposite(true);
//		} else {
//			bse.setRenderingComposite(false);
//		}
		if(fs.getWidth() < 0) {
			System.out.println(bse.getName()+" line width ="+fs.getWidth());
		}
		if (fs.getTransparency() > 0) {
			//bse.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,(100-fs.getTransparency()) * 0.01f));
			//bse.setRenderingComposite(true);
			
			bse.setLineTransparency(fs.getTransparency());
		}
		
		if (fs.getPattern() != null && fs.getPattern().length() > 0 && bse.isRenderingLine()) {
			String[] pattterns = fs.getPattern().split(",");
			if (pattterns.length >= 2) {
				bse.setRenderingLinePattern(true);
				bse.setLinePattern(fs.getPattern());
				bse.setRenderingFill(false);
			} else {
				pattterns = fs.getPattern().split("/");
				if (pattterns.length >= 2) {
					bse.setRenderingLinePattern(true);
					bse.setLinePattern(fs.getPattern());
					bse.setRenderingFill(false);
				}
			}
		} else {
			bse.setRenderingLinePattern(false);
		}
	}
	
	
	static public BasicStyleExtend createPolygonStyle(com.gis.protocol.freegis3.PolygonStyle ps, com.gis.protocol.freegis3.Styles style){
		BasicStyleExtend bs = new BasicStyleExtend();
		bs.setName(ps.getName());
		bs.setDesc(ps.getDesc());
		
		
//		if(ps.getFillStyle() != null && ps.getLineStyle() != null) {
//			if(ps.getFillStyle().getColor().equals(ps.getLineStyle().getColor())) {
//				ps.setLineStyle(null);
//			}
//		}
		
		if(ps.getFillStyle() != null){
			setFillStyle(bs, ps.getFillStyle());
		}
		else{
			bs.setRenderingFill(false);
		}
		if(ps.getLineStyle() != null){
			setLineStyle(bs, ps.getLineStyle());
		}
		else{
			bs.setRenderingLine(false);
		}
		
		
		return bs;
	}
	
	static public BasicStyleExtend createPolylineStyle(com.gis.protocol.freegis3.PolylineStyle ps, com.gis.protocol.freegis3.Styles style){
		BasicStyleExtend bs = new BasicStyleExtend();
		bs.setName(ps.getName());
		bs.setDesc(ps.getDesc());
		
		//System.out.println("createPolylineStyle="+ ps.getName());
		
//		if(ps.getName().equals("1-4")) {
//			int k=0;
//		}
		
		
		if(ps.getLineStyle() != null && ps.getLineStyle().getCap() != null) {
			
			//System.out.println("createPolylineStyle="+ ps.getName()+", ps cap="+ps.getLineStyle().getCap().toString());
			
			Cap caps = ps.getLineStyle().getCap();
			if(caps.equals(Cap.BUTT)) {
				bs.cap = BasicStroke.CAP_BUTT;
			}
			else if(caps.equals(Cap.ROUND)) {
				bs.cap = BasicStroke.CAP_ROUND;
			}
			else if(caps.equals(Cap.SQUARE)) {
				bs.cap = BasicStroke.CAP_SQUARE;
			}
		}
		
		if(ps.getLineStyle() != null && ps.getLineStyle().getJoin() != null) {
			Join join = ps.getLineStyle().getJoin();
			if(join.equals(Join.BEVEL)) {
				bs.join = BasicStroke.JOIN_BEVEL;
			}
			else if(join.equals(Join.MITER)) {
				bs.join = BasicStroke.JOIN_MITER;
			}
			else if(join.equals(Join.ROUND)) {
				bs.join = BasicStroke.JOIN_ROUND;
			}
		}

		
		if(ps.getFillStyle() != null){
			setFillStyle(bs, ps.getFillStyle());
		}
		else{
			bs.setRenderingFill(false);
		}
		
		if(ps.getLineStyle() != null){
			setLineStyle(bs, ps.getLineStyle());
		}
		else{
			bs.setRenderingLine(false);
		}
		return bs;
	}
	
	// 항공영상 스타일 지정
	static public BasicStyleExtend createRasterStyle(com.gis.protocol.freegis3.RasterStyle rs) {
		BasicStyleExtend bs = new BasicStyleExtend();
		bs.setName(rs.getName());
		bs.setDesc(rs.getDesc());
		if (rs.getTransparency() != null && rs.getTransparency() > 0) {
			bs.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (100-rs.getTransparency()) * 0.01f));
			bs.setRenderingComposite(true);
		}
		if (rs.isIsAntiAliasing()) {
			bs.setAntiAliasing(true);
		}
		return bs;
	}
	
}
