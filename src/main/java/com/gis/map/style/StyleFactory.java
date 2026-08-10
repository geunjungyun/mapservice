package com.gis.map.style;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;

import com.data.util.MapLog;
import com.gis.protocol.FillStyle;
import com.gis.protocol.LineStyle;
import com.gis.protocol.MarkStyle;
import com.gis.protocol.MultiLine;
import com.gis.protocol.PolygonStyle;
import com.gis.protocol.PolylineStyle;
import com.gis.protocol.Query;
import com.gis.protocol.Styles;
import com.gis.protocol.SymbolStyle;
import com.gis.protocol.TextStyle;
//import com.gis.protocol.freegis2.PointStyle;
import com.gis2.storage.StorageMng;
import com.gis2.storage.TileServiceMng;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

//import org.freegis.PointStyle;
//import org.freegis.PolygonStyle;
//import org.freegis.PolylineStyle;

public class StyleFactory {
	
	//static StorageMng sm = null;
	
	// HD 모드 스케일 팩터 (hdMode=true이면 2.0, 아니면 1.0)
	public static float getHdScale() {
		return TileServiceMng.hdMode ? 2.0f : 1.0f;
	}
	
	/**
	 * HD 모드일 때 심볼 이미지를 2배로 스케일링
	 */
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
	
	public StyleFactory(StorageMng sm){
	}
	
	public static void loadStyles(Styles stls, HashMap<String, Style> nStyles){
		
		float fontSize = Integer.MIN_VALUE;
		List<com.gis.protocol.PointStyle> pss = stls.getPointStyle();
		for(com.gis.protocol.PointStyle ps : pss){
			if(!nStyles.containsKey(ps.getName())){
				
				PointStyle bse = (PointStyle) StyleFactory.createPointStyle(ps, stls);
				nStyles.put(bse.getName(), bse);
				if(fontSize < bse.getFontSize()) {
					fontSize = bse.getFontSize();
				}
			}
		}
		for(com.gis.protocol.PointStyle ps : pss){
			List<Query> querys = ps.getQuery();
			if(querys != null && querys.size() > 0){
				BasicStyleExtend bse = (BasicStyleExtend) nStyles.get(ps.getName());
				Vector<QueryStyle> bseVector = new Vector();
				for(Query query : querys){
					QueryStyle qs = new QueryStyle();
					qs.name = query.getName().toLowerCase();
					qs.value = query.getValue();
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
					query.getPriority();
					if(query == null) {
						System.out.println("---------------------------styleName="+bse.getName() +", qury dis null");
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
							
							for(com.gis.protocol.PointStyle subTemp : pss){
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
				bse.setQuerys(bseVector);
			}
			
		}
		
		
		List<PolygonStyle> pls = stls.getPolygonStyle();
		for(PolygonStyle pl : pls){
			if(!nStyles.containsKey(pl.getName())){
				BasicStyleExtend bse = StyleFactory.createPolygonStyle(pl);
				nStyles.put(bse.getName(), bse);
			}
		}
		
		for(PolygonStyle ps : pls){
			List<Query> querys = ps.getQuery();
			if(querys != null && querys.size() > 0){
				BasicStyleExtend bse = (BasicStyleExtend) nStyles.get(ps.getName());
				Vector<QueryStyle> bseVector = new Vector();
				for(Query query : querys){
					QueryStyle qs = new QueryStyle();
					qs.name = query.getName();
					qs.value = query.getValue();
					
					qs.priority = query.getPriority() == null ? 0 : query.getPriority();
					BasicStyleExtend subBse = (BasicStyleExtend) nStyles.get(query.getStyleName());
					if (subBse == null && query.getPriority() == -1) {
						System.out.println("PointStyle name=" + ps.getName() + ", subInfo name = " + query.getName() + ", value = " + query.getValue() + ", style = "
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
				bse.setQuerys(bseVector);
			}
			
		}

		
		List<PolylineStyle> pgs = stls.getPolylineStyle();
		for(PolylineStyle pg : pgs){
			if(!nStyles.containsKey(pg.getName())){
				BasicStyleExtend bse = StyleFactory.createPolylineStyle(pg);
				
				nStyles.put(bse.getName(), bse);
			}
		}
		
		
		
		for(PolylineStyle ps : pgs){
			List<Query> querys = ps.getQuery();
			if(querys != null && querys.size() > 0){
				BasicStyleExtend bse = (BasicStyleExtend) nStyles.get(ps.getName());
				Vector<QueryStyle> bseVector = new Vector();
				for(Query query : querys){
					
					
					QueryStyle qs = new QueryStyle();
					qs.name = query.getName();
					qs.value = query.getValue();
					qs.priority = query.getPriority();
					BasicStyleExtend subBse = (BasicStyleExtend) nStyles.get(query.getStyleName());
					if (subBse == null && query.getPriority() == -1) {
						System.out.println("PointStyle name=" + ps.getName() + ", subInfo name = " + query.getName() + ", value = " + query.getValue() + ", style = "
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
				bse.setQuerys(bseVector);
			}
			
		}

	}

	
	static public BasicStyleExtend createPointStyle(com.gis.protocol.PointStyle ps_, com.gis.protocol.Styles style){
		PointStyle ps = new PointStyle();
		ps.priorityFieldName = "";
		ps.priority = ps_.getPriority();
		ps.positionType = ps_.getPositionType();
		ps.setDrawOrder(ps_.getDrawOrder());
		
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
			
			if (ms.getColor() != null && ms.getColor().length() > 0) {
				String[] rgbf = ms.getColor().split(",");
				if (rgbf.length != 3) {
					rgbf = ms.getColor().split("/");
				}
				ps.markColor = new Color(Integer.parseInt(rgbf[0].trim()),
						Integer.parseInt(rgbf[1].trim()), Integer.parseInt(rgbf[2]
								.trim()));
				ps.markSize = (int)(ms.getSize() * getHdScale());
				ps.setRenderingMark(true);
			} else {
				ps.setRenderingMark(false);
			}
		}
		
		if(ps_.getSymbolStyle() != null){
			SymbolStyle ss = ps_.getSymbolStyle();
			
			
			
			if (ss.getSymbolName() != null) {
				
				BufferedImage bi = StorageMng.getSymbol(ss.getSymbolName());
				bi = scaleSymbol(bi, getHdScale());
				if(bi != null){
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
			
			if( style.getSymbolPath() != null && style.getSymbolPath().trim().length() > 0 ){
				ps.symbolPath = style.getSymbolPath();
			}
		}
		
		if (ps_.getTextStyle() != null) {
			TextStyle ts = ps_.getTextStyle();
			
			if(ts.getMultiLineSize() != null){
				ps.multiLineSize = ts.getMultiLineSize();
			}
			
			if(ts.getMultiLine() != null){
				MultiLine ml = ts.getMultiLine();
				if(ml.getLineMaxSize() != null && ml.getLineMaxSize() > 0){
					ps.multiLineSize = ml.getLineMaxSize();
				}
				if(ml.getSeparateString() != null){
					for(String separateS : ml.getSeparateString()){
						ps.seperateStrings.add(separateS);
					}
				}
				if(ml.isIsSeparateUnicode() != null && ml.isIsSeparateUnicode() == true){
					ps.isSeperateUnicode = ml.isIsSeparateUnicode();
				}
			}
			
			if(ts.getRotateField() != null &&  ts.getRotateField().length() > 0){
				ps.rotateField = ts.getRotateField().toLowerCase();
			}
			
			if(ts.getRotate() != null && ts.getRotate() > 0){
				ps.rotate = ts.getRotate();
			}
			
			ps.fontName = ts.getFontName();
			//ps.fontSize = ts.getFontSize()*0.7f;
			ps.setFontSize((ts.getFontSize()+1) * getHdScale());
			//서울시 웹 지도
			//ps.fontSize = (int)(ts.getFontSize()*(0.8));
			ps.nameField = ts.getFieldName().replace("\"", "").toLowerCase();
			
			if(ts.getFontType() == null){
				//System.out.println("plain="+ps_.getName());
				ps.fontType = Font.PLAIN;
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
				if(style.getLineExtensionWidthRatio() != null) {
					ps.lineExtensionWidthRatio = style.getLineExtensionWidthRatio();
				}
				
			}
			
			if(ts.getLineIntervalPixel() != null && ts.getLineIntervalPixel() > 0) {
				ps.lineIntervalPixel = ts.getLineIntervalPixel();
			}
			else {
				if(style.getLineIntervalPixel() != null) {
					ps.lineIntervalPixel = style.getLineIntervalPixel();
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
			Font font = StorageMng.getFont(ps.fontName, ps.fontType, ps.getFontSize(), ps.tracking, ps.ligature, ps.kerning, ps.widthRatio);
			//HD 모드 로그

			if(font == null){
				System.out.println("font is null, name = " + ps.fontName);
			}
			else {
				//System.out.println("font name = " + ps.fontName+", Font obj="+font.toString());
			}
			
			if(font != null){
				ps.setFont(font);
				ps.setRenderingText(true);
			}
			
			ps.setTextNameField(ps_.getTextStyle().getFieldName().toLowerCase());
			
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


		return ps;
	}
	
	static public void setFillStyle(BasicStyleExtend bse, FillStyle fs){
		if (fs.getColor() != null && fs.getColor().length() > 0) {
			String[] rgbf = fs.getColor().split(",");
			if (rgbf.length != 3) {
				rgbf = fs.getColor().split("/");
			}
			bse.setFillColor(new Color(Integer.parseInt(rgbf[0].trim()), Integer
					.parseInt(rgbf[1].trim()), Integer.parseInt(rgbf[2].trim())));
			bse.setRenderingFill(true);
			
//			if(fs.getWidth() > 0){
//				bse.setFillStroke(fs.getWidth());
//			}
			
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
			bse.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (100-fs.getTransparency()) * 0.01f));
			bse.setRenderingComposite(true);
		}
		
		if (fs.getTexturePattern() != null && fs.getTexturePattern().length() > 0) {
			
			BufferedImage bi = StorageMng.getSymbol(fs.getTexturePattern());
			if(bi != null) {
				Rectangle2D anchor = new Rectangle2D.Double(0, 0,
						2 * bi.getWidth(), 2 * bi.getHeight());
				TexturePaint fillPattern = new TexturePaint(bi, anchor);
				
				bse.setFillPattern(fillPattern);
				bse.setRenderingFillPattern(true);
			}
			else {
				System.out.println("image path = " + fs.getTexturePattern());
			}
			
		} else {
			bse.setRenderingFillPattern(false);
		}

	}
	
	static public void setLineStyle2(BasicStyleExtend bse, LineStyle fs){
		if (fs.getColor() != null && fs.getColor().length() > 0) {
			String[] rgbf = fs.getColor().split(",");
			if (rgbf.length != 3) {
				rgbf = fs.getColor().split("/");
			}
			
			bse.setLineColor(new Color(Integer.parseInt(rgbf[0].trim()), Integer
					.parseInt(rgbf[1].trim()), Integer.parseInt(rgbf[2].trim())));
//			bse.setLineWidth(1.5f);
//			bse.setRenderingLine(true);
			
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
			bse.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,(100-fs.getTransparency()) * 0.01f));
			bse.setRenderingComposite(true);
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
			String[] rgbf = fs.getColor().split(",");
			if (rgbf.length != 3) {
				rgbf = fs.getColor().split("/");
			}
			//System.out.println("color = " + fs.getColor());
			bse.setLineColor(new Color(Integer.parseInt(rgbf[0].trim()), Integer
					.parseInt(rgbf[1].trim()), Integer.parseInt(rgbf[2].trim())));
			if (fs.getWidth() > 0) {
				bse.setLineWidth(fs.getWidth() * getHdScale());
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
		if(fs.getWidth() < 0) {
			//System.out.println(bse.getName()+" line width ="+fs.getWidth());
			MapLog.getSCLog().debug(bse.getName()+" line width ="+fs.getWidth());
		}
		if (fs.getTransparency() > 0) {
			bse.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,(100-fs.getTransparency()) * 0.01f));
			bse.setRenderingComposite(true);
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
	
	
	static public BasicStyleExtend createPolygonStyle(com.gis.protocol.PolygonStyle ps){
		BasicStyleExtend bs = new BasicStyleExtend();
		bs.setName(ps.getName());
		bs.setDesc(ps.getDesc());
		
		bs.setDrawOrder(ps.getDrawOrder());
		
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
	
	static public BasicStyleExtend createPolylineStyle(com.gis.protocol.PolylineStyle ps){
		BasicStyleExtend bs = new BasicStyleExtend();
		bs.setName(ps.getName());
		bs.setDesc(ps.getDesc());
		bs.setDrawOrder(ps.getDrawOrder());
		if(ps.getLineStyle() != null){
			setLineStyle(bs, ps.getLineStyle());
		}
		else{
			bs.setRenderingLine(false);
		}
		if(ps.getFillStyle() != null){
			setFillStyle(bs, ps.getFillStyle());
		}
		else{
			bs.setRenderingFill(false);
		}
		return bs;
	}
	
}
