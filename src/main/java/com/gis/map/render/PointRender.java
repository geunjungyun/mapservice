package com.gis.map.render;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.imageio.ImageIO;

import com.gis.map.MapContext;
import com.gis.map.MapData;
import com.gis.map.render.OverlapMng.Overlap;
import com.gis.map.style.BasicStyleExtend;
import com.gis.map.style.PointStyle;
import com.gis.projection.ScreenCoordUtil;
import com.gis.protocol.DeleteOverlapMode;
import com.gis2.storage.StorageMng;
import com.gis2.storage.TileServiceMng;
import com.gis.map.style.StyleFactory;
import com.index.rtree.IRect;
import com.util.io.FileUt;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

//import com.vividsolutions.jump.feature.Feature;
//import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public class PointRender implements IRender {

	// private Rectangle viewR = null;

//	public OverlapMng olm = null;
//	Overlap ol = null;

	// TextStroke textStroke = new TextStroke();

	// double scale = 0.0;
	// boolean textSrokeMode = false;

	String serviceName = "";

	public static String seperatorMultiLine = "\n";

	public PointRender() {
//		this.olm = new OverlapMng();
//		this.ol = this.olm.getOverlay("main");
	}

//	public void setViewSize(Rectangle viewR) {
//		this.viewR = viewR;
//		this.textStroke.setViewSize(this.viewR);
//	}
//
//	public Rectangle getViewSize() {
//		return this.viewR;
//	}

	public void setServiceName(String name) {
		this.serviceName = name;
	}

//	public void clearOverlap() {
//		// this.overlaps.clear();
//		// this.textStroke.removeAllOverlapData();
//		this.ol.clear();
//		this.olm.clear();
//		this.ol = this.olm.getOverlay("main");
//	}

	public void draw(Graphics2D g, Shape geo, Style style) {

	}

//	public void draw(Graphics2D g, Shape geo, SimpleFeature feature, Style style, StorageMng sm, boolean textSrokeMode) {
//		//this.scale = scale;
//		this.textSrokeMode = true;
//		this.draw(g, geo, feature, style, sm);
//		this.textSrokeMode = false;
//	}

	public void draw(Graphics2D g, Shape geo, SimpleFeature feature, Style style, boolean textSrokeMode,
			ScreenCoordUtil scu, OverlapMng olm, int level, String layerName, PointStyle mainStyle) {
		// TODO Auto-generated method stub

		double scale = scu.getScale();

		TextStroke3 textStroke = new TextStroke3();

		Rectangle viewR = new Rectangle();
		viewR.x = 0;
		viewR.y = 0;
		viewR.width = scu.getScreenDimension().width;
		viewR.height = scu.getScreenDimension().height;

//		this.poinRender.setViewSize(viewR);

		textStroke.setViewSize(viewR);

		Overlap ol = olm.getOverlay("main");

		//boolean debugOverlap = true;
		
		PointStyle mainPs = mainStyle;
		
		String mainOverlap = mainPs.deleteOverlapMode;

		PointStyle ps = (PointStyle) style;

		ps.shapeDraw = true;

		boolean deleteOverlap = true;

		String overlapName = "";
		
		if(mainPs.deleteOverlapMode != null) {
			overlapName = mainPs.getName();
		}
		else {
			overlapName = ps.getName();
		}

		if (ps.deleteOverlapMode.equals(DeleteOverlapMode.ALL_L_STYLE.toString())) {
			deleteOverlap = true;
		} else if (ps.deleteOverlapMode.equals(DeleteOverlapMode.ONLY_STYLE.toString())) {
			deleteOverlap = true;
			if (olm.getOverlay(overlapName) == null) {
				olm.createOverlay(overlapName);
			}
		} else if (ps.deleteOverlapMode.equals(DeleteOverlapMode.NOT_DELETE.toString())) {
			deleteOverlap = false;
		}
		else {
			ps.deleteOverlapMode = DeleteOverlapMode.NOT_DELETE.toString();
		}


		int rotate = 0;

		if (ps.rotate > 0) {
			rotate = rotate;
		} else if (ps.rotateField != null && feature.getFeatureType().indexOf(ps.rotateField.toLowerCase()) > -1) {
			// String value = (String)feature.getAttribute(ps.rotateField);
			// rotate = (int) Double.parseDouble(value);
			// rotate = 180 - (rotate - 180);

			Object value = feature.getAttribute(ps.rotateField);
			if (value instanceof Double) {
				rotate = ((Double) value).intValue();
			} else if (value instanceof String) {
				rotate = (int) Double.parseDouble((String) value);
			} else if (value instanceof BigDecimal) {
				rotate = ((BigDecimal) value).intValue();
			} else if (value instanceof Integer) {
				rotate = ((Integer) value).intValue();
			} else if (value instanceof Short) {
				rotate = ((Short) value).intValue();
			} else if (value instanceof Long) {
				rotate = ((Long) value).intValue();
			} else if (value instanceof Byte) {
				rotate = ((Long) value).intValue();
			}
			rotate = 180 - (rotate - 180);
			// rotate -= 45;
		}

		Rectangle bound = geo.getBounds();
		
		//지도 좌표의 화면 X좌표
		float pointX = bound.x + bound.width / 2;
		//지도 좌표의 화면 Y좌표
		float pointY = bound.y + bound.height / 2;
		


		if (ps.offsetX != 0 || ps.offsetY != 0) {
			AffineTransform at = new AffineTransform();
			at.rotate(Math.toRadians(rotate));
			at.translate(ps.offsetX, ps.offsetY);

			Point2D ptSrc1 = new Point2D.Double(0, 0);
			Point2D ptDst1 = at.transform(ptSrc1, null);

			pointX += ptDst1.getX();
			pointY += ptDst1.getY();
		}

		float markInterval = 0;
		float symbolInterval = 0;
		float interval = 2;

		// int symbolHeight = 0;
		byte markPosition = 'x';
		byte symbolPosition = 'x';
		byte textPosition = 'x';

		if (ps.positionType != null) {
			markPosition = ps.positionType.getBytes()[0];
			symbolPosition = ps.positionType.getBytes()[1];
			textPosition = ps.positionType.getBytes()[2];
		}

		Rectangle center = new Rectangle();

		float symbolX = 0;
		float symbolY = 0;

		float markX = 0;
		float markY = 0;

		float markWidth = 0;
		float markHeight = 0;

		boolean markRender = false;
		Rectangle2D markRT = null;

		float symX = 0;
		float symY = 0;
		Rectangle2D symbolRT = null;
		boolean symbolRender = false;

		// int textX = 0;
		// int textY = 0;
		// int textWidth = 0;
		// int textHeight = 0;
		boolean textRender = false;
		// Vector<LineText> mlts = new Vector();
		// String name = null;
		// Shape shape = null;
		// Rectangle textRT = null;
		
		boolean isJijuk14 = false;
		
		
		if(level >= 14 && layerName.toLowerCase().equals("fa")) {
			isJijuk14 = true;
//			ps.extenstionArea = 0;
		}
		
		
		Shape roadNameShape = null;

		MultiLineText mlt = new MultiLineText();
		
		if (markPosition != 'x') {
			if (ps.markColor != null && ps.markSize > 0) {
				markInterval = ps.markSize;
				g.setColor(ps.markColor);

				markX = pointX - markInterval / 2;
				markY = pointY - markInterval / 2;
				markWidth = markInterval;
				markHeight = markInterval;

				markRT = new Rectangle2D.Float(pointX - markInterval / 2, pointY - markInterval / 2, markInterval,
						markInterval);

				if (ps.deleteOverlapMode.equals(DeleteOverlapMode.ALL_L_STYLE.toString())) {
					if (ol.chechOverlap(markRT, ps.extenstionArea)) {
						return;
					}
				} else if (ps.deleteOverlapMode.equals(DeleteOverlapMode.ONLY_STYLE.toString())) {
					if (olm.chechOverlap(ps.getName(), markRT, ps.extenstionArea)) {
						return;
					}
				}

				markRender = true;
				// this.overlaps.add(rec);
			}
			markInterval = markInterval / 2.0f;
			// interval = 2;
		}

		SimpleFeatureType fs = feature.getFeatureType();
		BufferedImage symbol = null;

		if (symbolPosition != 'x') {

			if (ps.symbol != null) {
				symbol = ps.symbol;
			} else if (ps.symbolNameField != null && fs.indexOf(ps.symbolNameField.toLowerCase()) > -1) {
				String symbolName = (String) feature.getAttribute(ps.symbolNameField);
				if (symbolName != null && symbolName.length() > 0) {
					String[] value = FileUt.getFileNameSplit(symbolName);
					String ext = value[FileUt.FILE_EXT];
					if (ext == null || ext.length() == 0) {
						symbolName += ".png";
					}
				symbol = StorageMng.getSymbol((ps.symbolPath == null || ps.symbolPath.trim().length() == 0 ) 
						? symbolName : (ps.symbolPath+":"+symbolName));
					// HD 모드 동적 심볼 스케일링
					symbol = StyleFactory.scaleSymbol(symbol, StyleFactory.getHdScale());
				}
			} else {
				symbol = ps.symbol;
			}
			if (symbol != null) {

				float drawX = 0;
				float drawY = 0;

				symbolInterval = symbol.getHeight();

				drawX = pointX - symbol.getWidth() / 2.0f;
				drawY = pointY + markInterval + interval;
				if (markPosition == 'x') {
					drawY = pointY - symbol.getHeight() / 2.0f;
					symbolInterval = symbol.getHeight() / 2.0f;
				}
				// interval += 2;
				symbolY = drawY + symbol.getHeight() / 2.0f;
				// g.drawImage(symbol, drawX, drawY,null);

				symX = drawX;
				symY = drawY;
				symbolRT = new Rectangle2D.Float(symX, symY, (float) symbol.getWidth(), (float) symbol.getHeight());

				if (ps.deleteOverlapMode.equals(DeleteOverlapMode.ALL_L_STYLE.toString())) {
					if (ol.chechOverlap(symbolRT, ps.extenstionArea)) {
						if (MapContext.debugOverlap) {
							g.setColor(Color.RED);
							g.setStroke(new BasicStroke(1));
							g.drawRect((int) (symbolRT.getMinX() - ps.extenstionArea),
									(int) (symbolRT.getMinY() - ps.extenstionArea),
									(int) (symbolRT.getWidth() + ps.extenstionArea * 2),
									(int) (symbolRT.getHeight() + ps.extenstionArea * 2));
						}
						return;
					}
				} else if (ps.deleteOverlapMode.equals(DeleteOverlapMode.ONLY_STYLE.toString())) {
					if (olm.chechOverlap(ps.getName(), symbolRT, ps.extenstionArea)) {
						if (MapContext.debugOverlap) {
							g.setColor(Color.BLUE);
							g.setStroke(new BasicStroke(1));
							g.drawRect((int) (symbolRT.getMinX() - ps.extenstionArea),
									(int) (symbolRT.getMinY() - ps.extenstionArea),
									(int) (symbolRT.getWidth() + ps.extenstionArea * 2),
									(int) (symbolRT.getHeight() + ps.extenstionArea * 2));
						}

						return;
					}
				}

				symbolRender = true;
			}
		}

		Vector<Coordinate> list = new Vector();

		if (textPosition != 'x') {
			if (ps.nameField == null || ps.nameField.length() == 0) {
				System.out.println(" ps name = " + ps.getName() + ", fieldName = " + ps.nameField);
			}

			String name = "";

			if (feature.getFeatureType().indexOf(ps.getTextnameField().toUpperCase()) > -1) {
				String temp = feature.getAttribute(ps.getTextnameField().toUpperCase()) + "";
				if (temp != null) {
					name = temp;
				}
			} else if (!(feature.getFeatureType().indexOf(ps.getTextnameField().toLowerCase()) > -1)) {
				String[] fields = ps.getTextnameField().toLowerCase().split(",");
				for (String field : fields) {
					String temp = (String) feature.getAttribute(field.replace("\"", ""));
					if (temp != null) {
						name += feature.getAttribute(field.replace("\"", "").trim());
					}
				}
			} else {
				String temp = (String) feature.getAttribute(ps.getTextnameField().toLowerCase());
				if (temp != null) {
					name = feature.getAttribute(ps.getTextnameField().toLowerCase()) + "";
				}
			}
			
			
			//order 디버깅
			//System.out.println("layerName="+ layerName+", name="+name+", order="+feature.getAttribute("order"));

			if (name != null && name.length() > 0 && textSrokeMode == false) {
				
				if(level < 4) {
					if(name.equals("경기도")) {
						pointY-=30;
						pointX-=30;
					}
					if(name.equals("인천광역시")) {
						pointY+=10;
						pointX-=40;
					}
					if(name.equals("충청남도")) {
						pointY-=30;
						pointX-=25;
					}
					if(name.equals("충청북도")) {
						pointY-=30;
						pointX +=25;
					}
					if(name.equals("대전광역시")) {
						pointY+=10;
						//pointX +=25;
					}
				}
				else {
					if(name.equals("서울특별시")) {
						//pointY-=30;
						pointX+=30;
					}
				}
				
				Vector<String> multiLineWord = this.getWordList(name, ps);
				
				Vector<String> tempV = new Vector();

				for (String line : multiLineWord) {
					if (line.length() != 0) {
						tempV.add(line);
					}
				}
				multiLineWord = tempV;

				float multiLineHeight = 0;
				float lineInteval = 0;
				float textAllWidth = Float.MIN_VALUE;

				Vector<TextLayout> layouts = new Vector();

				FontRenderContext frc = g.getFontRenderContext();
				// Rectangle textRc = null;

				float tempLineHeightAvg = 0;

				/*
				 * 라인 간격을 픽셀 단위 값으로 저장
				 */
				float lineAvg = 0.0f;
				lineAvg = ps.linespace ;
				
				if(!isJijuk14) {
					for (int k = 0; k < multiLineWord.size(); k++) {
	
						String line = multiLineWord.get(k);
	
						if (line.trim().length() == 0) {
							continue;
						}
	
						TextLayout tl = new TextLayout(multiLineWord.get(k), ps.getFont(), frc);
	
						AffineTransform atf = new AffineTransform();
											
						
						Shape shape = tl.getOutline(atf);
	
						Rectangle rt = shape.getBounds();// new
						//Rectangle rt = tl.getBounds().getBounds();
	
						if (textAllWidth < rt.getWidth()) {
							textAllWidth = (float) rt.getWidth();
						}
	
						tempLineHeightAvg += rt.getHeight();
	
						layouts.add(tl);
	
						LineText lt = new LineText();
						lt.shape = shape;
						//lt.textRect = rt;
						lt.textRects = new Rectangle[1];
						lt.textRects[0] = rt;
						lt.word = line;
	
						mlt.add(lt);
					}
	
					mlt.lineSpace = (tempLineHeightAvg / multiLineWord.size()) * lineAvg;
	
					g.setStroke(ps.getLineStroke());
	
					double lineHeight = 0;
	
					String debugName = "GS25";
					
					
					
					for (int k = 0; k < mlt.lineCnt(); k++) {
	
						LineText lt = mlt.get(k);
	
						TextLayout tl = layouts.get(k);
						
						Rectangle rt = lt.textRects[0];// new
	
						lineHeight += mlt.lineSpace;
						lineHeight += mlt.maxHeight;
	
						float textYInterval = 0;
						if (textPosition == 'b') {
							textYInterval = (float) (lineHeight + markInterval + symbolInterval);
						} else if (textPosition == 't') {
							textYInterval = (int) (-markInterval - symbolInterval - interval);
						} else {
							float textAllHeight = mlt.getHegiht();
							textYInterval = (float) (lineHeight - mlt.getHegiht() / 2.0);
							//textYInterval = (float) (rt.getHeight());
						}
	
						float textXInterval = 0;
						if (textPosition == 'l') {
							textXInterval = (int) (-(int) rt.getWidth() - markInterval - symbolInterval - interval
									- (textAllWidth - (float) rt.getWidth()) / 2);
						} else if (textPosition == 'r') {
							textXInterval = markInterval + symbolInterval + interval
									+ (textAllWidth - (float) rt.getWidth()) / 2;
						} else {
							textXInterval = (float) -(rt.getWidth() / 2.f);
						}
	
						if (k > 0) {
							LineText prelt = mlt.get(k - 1);
							if ((prelt.getHeight() - lt.getHeight()) < 0) {
								textYInterval -= Math.abs(prelt.getHeight() - lt.getHeight()) / 2.0;
							}
						}
	
						textYInterval -= (mlt.maxHeight - lt.getHeight()) / 2;
	
						float drawX = pointX + textXInterval;
	
						float drawY = pointY + textYInterval;
						
						if (symbolPosition == textPosition && symbol != null) {
							drawX = pointX - ((int) rt.getWidth() / 2);
							drawY = symbolY + ((int) rt.getHeight() / 2);
							
							boolean first1 = false;
							
							boolean allNum = true;
							
							char[] charArray = name.toCharArray();
							
							for(int m=0; m<charArray.length; m++) {
							
								if (charArray[m]>='0' && charArray[m]<='9'){
									if(m==0 && charArray[m] == '1') {
										first1 = true;
									}
								}
								else {
									allNum = false;
								}
							}
							
							if(!allNum) {
								drawY -= 2;
							}
							if(first1) {
								drawX -=1;
							}
							
						}
	
						AffineTransform atf = new AffineTransform();
						atf.translate(drawX, drawY);
	
						double anchorx = 0;
						double anchory = 0;
	
	
						if (rotate != 0) {
							anchorx = rt.getWidth() / 2.0;
	
							anchory = -textYInterval;
	
							atf.rotate(Math.toRadians(rotate), anchorx, anchory);
						}
						
						Shape shape = atf.createTransformedShape(lt.shape);
						
						lt.word = multiLineWord.get(k);
						lt.shape = shape;
						
	
						if (rotate != 0) {
							lt.rotate = rotate;
							lt.rotateX = (float) anchorx;
							lt.rotateY = (float) anchory;
							
							lt.textRects = new Rectangle[tl.getCharacterCount()];
							for(int i=0 ; i < tl.getCharacterCount(); i++) {
								Shape box = tl.getBlackBoxBounds(i, i+1);
								box = atf.createTransformedShape(box);
								lt.textRects[i] = box.getBounds();
							}
							
						}
						else {
							lt.textRects = new Rectangle[1];
							lt.textRects[0] = shape.getBounds();
						}
	
						if (ps.deleteOverlapMode.equals(DeleteOverlapMode.ALL_L_STYLE.toString())) {
							for(int i=0; i<lt.textRects.length; i++) {
								if (ol.chechOverlap(lt.textRects[i], ps.extenstionArea)) {
									if (MapContext.debugOverlap) {
										
										Color old = g.getColor();
										
										g.setColor(Color.red);
										g.setStroke(new BasicStroke(1f));
										
										for(int j=0; j<lt.textRects.length; j++) {
											g.drawRect(lt.textRects[j].x - ps.extenstionArea, lt.textRects[j].y - ps.extenstionArea,
													lt.textRects[j].width + ps.extenstionArea * 2, lt.textRects[j].height + ps.extenstionArea * 2);
										}
										g.fill(shape);
										g.setColor(old);
									}
									return;
								}
							}
						} else if (ps.deleteOverlapMode.equals(DeleteOverlapMode.ONLY_STYLE.toString())) {
							
							
							for(int i=0; i<lt.textRects.length; i++) {
								if (olm.chechOverlap(overlapName, lt.textRects[i], ps.extenstionArea)) {
									
									if (MapContext.debugOverlap) {
										
										Color old = g.getColor();
										g.setColor(Color.BLUE);
										g.setStroke(new BasicStroke(1f));
										
										for(int j=0; j<lt.textRects.length; j++) {
											g.drawRect(lt.textRects[j].x - ps.extenstionArea, lt.textRects[j].y - ps.extenstionArea,
													lt.textRects[j].width + ps.extenstionArea * 2, lt.textRects[j].height + ps.extenstionArea * 2);
										}
										g.fill(shape);
										g.setColor(old);
									}
									return;
									
								}
								
								if (ol.chechOverlap(lt.textRects[i], 0)) {
									return;
								}
								
							}
						}
	
					}
					textRender = true;
				}
				else {
					//http://localhost:8070/ver1/mapservice?req=tile&path=reg10/reg11/14/17689/31407.png&preview=1760734375308&key=981edekdi1298d12ddk121
					Vector<Font> fonts = new Vector();
					
					// 폰트 최소 사이즈 8까지 줄여서 저장
					int idx = 0;
					for(int i = (int) ps.getFontSize() ; i > 6 ; i--) {
						// HD 모드에서는 최소 폰트 크기도 스케일 유지
						Font font = StorageMng.getFont(ps.fontName, ps.fontType, i, ps.tracking, ps.ligature, ps.kerning, ps.widthRatio-(idx*0.1f));
						idx++;
						fonts.add(font);
					}
					
					int fontIdx = 0;
					for(Font font : fonts) {
						fontIdx++;
						mlt.mlt.clear();
						
						boolean overlap = false;
						
						for (int k = 0; k < multiLineWord.size(); k++) {
							
							String line = multiLineWord.get(k);
		
							if (line.trim().length() == 0) {
								continue;
							}
		
							TextLayout tl = new TextLayout(multiLineWord.get(k), font, frc);
		
							AffineTransform atf = new AffineTransform();
												
							Shape shape = tl.getOutline(atf);
		
							Rectangle rt = shape.getBounds();// new
							//Rectangle rt = tl.getBounds().getBounds();
		
							if (textAllWidth < rt.getWidth()) {
								textAllWidth = (float) rt.getWidth();
							}
		
							tempLineHeightAvg += rt.getHeight();
		
							layouts.add(tl);
		
							LineText lt = new LineText();
							lt.shape = shape;
							//lt.textRect = rt;
							lt.textRects = new Rectangle[1];
							lt.textRects[0] = rt;
							lt.word = line;
		
							mlt.add(lt);
						}
		
						mlt.lineSpace = (tempLineHeightAvg / multiLineWord.size()) * lineAvg;
		
						g.setStroke(ps.getLineStroke());
		
						double lineHeight = 0;
		
						String debugName = "GS25";
						
						for (int k = 0; k < mlt.lineCnt(); k++) {
		
							LineText lt = mlt.get(k);
		
							TextLayout tl = layouts.get(k);
							
							Rectangle rt = lt.textRects[0];// new
		
							lineHeight += mlt.lineSpace;
							lineHeight += mlt.maxHeight;
		
							float textYInterval = 0;
							if (textPosition == 'b') {
								textYInterval = (float) (lineHeight + markInterval + symbolInterval);
							} else if (textPosition == 't') {
								textYInterval = (int) (-markInterval - symbolInterval - interval);
							} else {
								float textAllHeight = mlt.getHegiht();
								textYInterval = (float) (lineHeight - mlt.getHegiht() / 2.0);
								//textYInterval = (float) (rt.getHeight());
							}
		
							float textXInterval = 0;
							if (textPosition == 'l') {
								textXInterval = (int) (-(int) rt.getWidth() - markInterval - symbolInterval - interval
										- (textAllWidth - (float) rt.getWidth()) / 2);
							} else if (textPosition == 'r') {
								textXInterval = markInterval + symbolInterval + interval
										+ (textAllWidth - (float) rt.getWidth()) / 2;
							} else {
								textXInterval = (float) -(rt.getWidth() / 2.f);
							}
		
							if (k > 0) {
								LineText prelt = mlt.get(k - 1);
								if ((prelt.getHeight() - lt.getHeight()) < 0) {
									textYInterval -= Math.abs(prelt.getHeight() - lt.getHeight()) / 2.0;
								}
							}
		
							textYInterval -= (mlt.maxHeight - lt.getHeight()) / 2;
		
							float drawX = pointX + textXInterval;
		
							float drawY = pointY + textYInterval;
							
							if (symbolPosition == textPosition && symbol != null) {
								drawX = pointX - ((int) rt.getWidth() / 2);
								drawY = symbolY + ((int) rt.getHeight() / 2);
								
								boolean first1 = false;
								
								boolean allNum = true;
								
								char[] charArray = name.toCharArray();
								
								for(int m=0; m<charArray.length; m++) {
								
									if (charArray[m]>='0' && charArray[m]<='9'){
										if(m==0 && charArray[m] == '1') {
											first1 = true;
										}
									}
									else {
										allNum = false;
									}
								}
								
								if(!allNum) {
									drawY -= 2;
								}
								if(first1) {
									drawX -=1;
								}
								
							}
		
							AffineTransform atf = new AffineTransform();
							atf.translate(drawX, drawY);
		
							double anchorx = 0;
							double anchory = 0;
		
		
							if (rotate != 0) {
								anchorx = rt.getWidth() / 2.0;
		
								anchory = -textYInterval;
		
								atf.rotate(Math.toRadians(rotate), anchorx, anchory);
							}
							
							Shape shape = atf.createTransformedShape(lt.shape);
							
							lt.word = multiLineWord.get(k);
							lt.shape = shape;
							
		
							if (rotate != 0) {
								lt.rotate = rotate;
								lt.rotateX = (float) anchorx;
								lt.rotateY = (float) anchory;
								
								lt.textRects = new Rectangle[tl.getCharacterCount()];
								for(int i=0 ; i < tl.getCharacterCount(); i++) {
									Shape box = tl.getBlackBoxBounds(i, i+1);
									box = atf.createTransformedShape(box);
									lt.textRects[i] = box.getBounds();
								}
								
							}
							else {
								lt.textRects = new Rectangle[1];
								lt.textRects[0] = shape.getBounds();
							}
		
							if (ps.deleteOverlapMode.equals(DeleteOverlapMode.ALL_L_STYLE.toString())) {
								for(int i=0; i<lt.textRects.length; i++) {
									if (ol.chechOverlap(lt.textRects[i], ps.extenstionArea)) {
										if (MapContext.debugOverlap) {
											
											Color old = g.getColor();
											
											g.setColor(Color.red);
											g.setStroke(new BasicStroke(1f));
											
											for(int j=0; j<lt.textRects.length; j++) {
												g.drawRect(lt.textRects[j].x - ps.extenstionArea, lt.textRects[j].y - ps.extenstionArea,
														lt.textRects[j].width + ps.extenstionArea * 2, lt.textRects[j].height + ps.extenstionArea * 2);
											}
											g.fill(shape);
											g.setColor(old);
										}
										overlap = true;
									}
								}
							} else if (ps.deleteOverlapMode.equals(DeleteOverlapMode.ONLY_STYLE.toString())) {
								for(int i=0; i<lt.textRects.length; i++) {
									if (olm.chechOverlap(overlapName, lt.textRects[i], ps.extenstionArea)) {
										
										if (MapContext.debugOverlap) {
											
											Color old = g.getColor();
											g.setColor(Color.BLUE);
											g.setStroke(new BasicStroke(1f));
											
											for(int j=0; j<lt.textRects.length; j++) {
												g.drawRect(lt.textRects[j].x - ps.extenstionArea, lt.textRects[j].y - ps.extenstionArea,
														lt.textRects[j].width + ps.extenstionArea * 2, lt.textRects[j].height + ps.extenstionArea * 2);
											}
											g.fill(shape);
											g.setColor(old);
										}
										overlap = true;
									}
									
									if (ol.chechOverlap(lt.textRects[i], 0)) {
										overlap = true;
									}
									
								}
							}
						}
						
						if(overlap == true) {
							continue;
						}
						else {
							break;
						}

					}
					
					
					// 최종 겹침이 발생했을때 리컨하는 코드로 수정해야함.
					if(fontIdx >= (fonts.size())) {
						return;
					}
					
					
					textRender = true;
					
				}
			} else if (name != null && name.length() > 0 && textSrokeMode == true) {

				if ((this.serviceName.indexOf("jan") > -1 || this.serviceName.indexOf("chinag") > -1
						|| this.serviceName.indexOf("chinab") > -1) && feature.getFeatureType().indexOf("eng") > -1) {
					String engName = (String) feature.getAttribute("eng");
					if (!engName.equals(name) && engName.length() > 0) {
						name += ("(" + engName + ")");
					}
				}
				
				double pixelValue = 0.0;


				if (feature.getFeatureType().indexOf("bt") > -1) {
					Object roadbt = feature.getAttribute("bt");

					double roadwidth = 0.0;

					if (roadbt instanceof Integer) {
						roadwidth = ((Integer) roadbt).intValue();
					} else if (roadbt instanceof Double) {
						roadwidth = ((Double) roadbt).doubleValue();
					}

					pixelValue = ((double) (roadwidth) / scale);

					int txtSize = (int) (pixelValue);
					int txtMaxSize = 16;

					String cls = (String) feature.getAttribute("ROA_CLS_SE");

					if (cls.equals("01") || cls.equals("02")) {
						txtMaxSize = 16;
					} else {
						txtMaxSize = 14;
					}

					if (txtSize > txtMaxSize) {
						txtSize = txtMaxSize;
					}
				} else {
					pixelValue = 10;
				}
				// Font font = sm.getFont(ps.fontName, ps.fontType, txtSize);

				if (pixelValue >= 10 && name.length() > 0) {
					
					if (ps.deleteOverlapMode.equals(DeleteOverlapMode.ALL_L_STYLE.toString())) {
						roadNameShape = textStroke.createStrokedShape(geo, name, ps.getFont(), g, ol, 
								ps.extenstionArea,ps.tracking, ps.lineExtensionWidthRatio, ps.lineIntervalPixel);
						
					} else if (ps.deleteOverlapMode.equals(DeleteOverlapMode.ONLY_STYLE.toString())) {
						Overlap olt = olm.getOverlay(ps.getName());
						// olt.extensionArea = 70;
						roadNameShape = textStroke.createStrokedShape(geo, name, ps.getFont(), g, olt, ps.extenstionArea,
								ps.tracking, ps.lineExtensionWidthRatio, ps.lineIntervalPixel);
					} else {
						roadNameShape = textStroke.createStrokedShape(geo, name, ps.getFont(), g, null, 0,
								ps.tracking, ps.lineExtensionWidthRatio, ps.lineIntervalPixel);
					}
					
					if (roadNameShape != null) {
						textRender = true;
					}
				}
			}
		}


		if (markRender) {
			g.setColor(ps.markColor);
			g.fillRect((int) markRT.getMinX(), (int) markRT.getMinY(), (int) markRT.getWidth(),
					(int) markRT.getHeight());

			if (ps.deleteOverlapMode.equals(DeleteOverlapMode.ALL_L_STYLE.toString())) {
				markRT.setRect(markRT.getMinX(), markRT.getMinY(), markRT.getWidth(), markRT.getHeight());
//				if (markRT.getMinX() == -20 && markRT.getMinY() == -20) {
//					System.out.println();
//				}
				// markRT.grow(this.ol.addAreaValue, this.ol.addAreaValue);
				ol.add(markRT, ps.extenstionArea);
			} else if (ps.deleteOverlapMode.equals(DeleteOverlapMode.ONLY_STYLE.toString())) {
				Overlap olt = olm.getOverlay(ps.getName());
				// markRT.grow(olt.addAreaValue, olt.addAreaValue);
				markRT.setRect(markRT.getMinX(), markRT.getMinY(), markRT.getWidth(), markRT.getHeight());
				olm.add(ps.getName(), markRT, ps.extenstionArea);
			}

		}
		
		if (symbolRender) {
			
			if(scu.getScreenDimension().intersects(symbolRT)) {
				g.drawImage(symbol, (int) symbolRT.getMinX(), (int) symbolRT.getMinY(), null);
			}

			if (ps.deleteOverlapMode.equals(DeleteOverlapMode.ALL_L_STYLE.toString())) {

				// symbolRT.setBounds(symbolRT.x, symbolRT.y, symbolRT.width, symbolRT.height);
				symbolRT.setRect(symbolRT.getMinX(), symbolRT.getMinY(), symbolRT.getWidth(), symbolRT.getHeight());

				// symbolRT.grow(this.ol.addAreaValue, this.ol.addAreaValue);
				ol.add(symbolRT, ps.extenstionArea);
			} else if (ps.deleteOverlapMode.equals(DeleteOverlapMode.ONLY_STYLE.toString())) {
				Overlap olt = olm.getOverlay(ps.getName());
				// symbolRT.grow(olt.addAreaValue, olt.addAreaValue);
				// symbolRT.setBounds(symbolRT.x, symbolRT.y, symbolRT.width, symbolRT.height);
				symbolRT.setRect(symbolRT.getMinX(), symbolRT.getMinY(), symbolRT.getWidth(), symbolRT.getHeight());
				olt.add(symbolRT, ps.extenstionArea);
			}

		}
		if (textRender) {
			if (textSrokeMode == true) {

				// roadNameShape.

				if (ps.isRenderingLine()) {
					g.setColor(ps.getLineColor());
					g.setStroke(ps.getLineStroke());
					g.draw(roadNameShape);
				}
				if (ps.isRenderingFill()) {
					g.setColor(ps.getFillColor());
					g.fill(roadNameShape);
				}

				// g.setColor(Color.red);
				// g.draw(geo);
				// roadNameShape = null;
			} else {
				
				for (int m = 0; m < mlt.lineCnt(); m++) {

					LineText lt = mlt.get(m);

					Shape shape = lt.shape;

					Rectangle textRTT = shape.getBounds();

					String name = lt.word;
					
					if (scu.getScreenDimension().intersects(textRTT) && ps.isRenderingLine()) {
						g.setColor(ps.getLineColor());
						// g.setColor(Color.red);

						g.setStroke(ps.getLineStroke());
						// g.setStroke(new BasicStroke(3));
						// System.out.println("line width = " + ps.getLineWidth());
						g.draw(shape);
					}
				}
				//ps.shapeDraw = false;
				for (int m = 0; m < mlt.lineCnt(); m++) {

					LineText lt = mlt.get(m);

					Shape shape = lt.shape;

					Rectangle textRTT = shape.getBounds();

					String name = lt.word;

					if (scu.getScreenDimension().intersects(textRTT) && ps.isRenderingFill()) {

						if (ps.shapeDraw) {
							g.setColor(ps.getFillColor());
							g.fill(shape);
						} else {
							AffineTransform nowAT = g.getTransform();

							AffineTransform newAt = new AffineTransform();
							if (lt.rotate != 0) {
								// newAt.rotate(Math.toRadians(lt.rotate), textRT.x + mlt.rotateX, textRT.y +
								// mlt.rotateY);
								// newAt.rotate(Math.toRadians(lt.rotate),
								// textRTT.getMinX()+textRTT.getWidth()/2, textRTT.getMinY() -
								// textRTT.getHeight()/2);
								g.setTransform(newAt);
								// g.rotate(Math.toRadians(mlt.rotate),
								// textRT.x+ mlt.rotateX, textRT.y+mlt.rotateY);
							}
							g.setColor(ps.getFillColor());
							g.setFont(ps.getFont());
							g.drawString(name, textRTT.x, textRTT.y);
							// g.setTransform(nowAT);
							// if (mlt.rotate != 0) {
							g.setTransform(nowAT);
						}

					}


					if (ps.deleteOverlapMode.equals(DeleteOverlapMode.ALL_L_STYLE.toString())) {
						for(int i=0; i<lt.textRects.length; i++) {
							ol.add(lt.textRects[i], ps.extenstionArea);
						}
					} else if (ps.deleteOverlapMode.equals(DeleteOverlapMode.ONLY_STYLE.toString())) {

						Overlap olt = olm.getOverlay(overlapName);

						for(int i=0; i<lt.textRects.length; i++) {
							olt.add(lt.textRects[i], ps.extenstionArea);
							ol.add(lt.textRects[i], 0);
						}

					}

				}
			}

		}

	}

	public Vector<String> getWordList(String name, PointStyle ps) {
		int maxWidth = ps.multiLineSize;

		Vector<String> list = new Vector();

		String[] explicitWord = name.split(PointRender.seperatorMultiLine);
		String[] words = name.split(" ");

		int distance = 0;
		String addWord = explicitWord[0];

		if (explicitWord.length > 1) {
			for (int i = 1; i < explicitWord.length; i++) {
				int len = explicitWord[i].length() + 1;
				int nowLen = addWord.length();

				list.add(addWord.trim());
				addWord = "";
				addWord += explicitWord[i];

				if (i == explicitWord.length - 1) {
					list.add(addWord.trim());
				}
			}
		} else if (words.length > 1) {
			addWord = words[0];
			for (int i = 1; i < words.length; i++) {
				int len = words[i].length() + 1;
				int nowLen = addWord.length();

				if ((nowLen + len) < maxWidth) {
					addWord += " " + words[i];
				} else {
					list.add(addWord.trim());
					addWord = "";
					addWord += words[i];
				}
				if (i == words.length - 1) {
					list.add(addWord.trim());
				}
			}
		} else {

			// 서울시 다국어
			// list.add(addWord);

			// 다울 중국어
			Vector<String> mw = this.getAutoWords(addWord, ps);

			list.addAll(mw);

		}

		return list;
	}

	public class LineText {
		String word;
		Shape shape;
		Rectangle[] textRects;
		float rotateX = 0;
		float rotateY = 0;
		int rotate = 0;
		
		public float getWidth() {
			float width = 0;
			for(int i=0; i<textRects.length; i++) {
				width += textRects[i].width;
			}
			return width;
		}
		
		public float getHeight() {
			float height = Float.MIN_VALUE;
			for(int i=0; i<textRects.length; i++) {
				if(textRects[i].height > height) {
					height = textRects[i].height;
				}
				
			}
			return height;
		}
		
	}

	public class MultiLineText {

		Vector<LineText> mlt = new Vector();
		float lineSpace = 0.0f;

		float maxWidth = Float.MIN_VALUE;

		float minWidth = Float.MAX_VALUE;

		float minHeight = Float.MAX_VALUE;

		float maxHeight = Float.MIN_VALUE;

		public void add(LineText lt) {
			mlt.add(lt);
			if (maxWidth < lt.getWidth()) {
				maxWidth = lt.getWidth();
			}
			if (minWidth > lt.getWidth()) {
				minWidth = lt.getWidth();
			}
			if (maxHeight < lt.getHeight()) {
				maxHeight = lt.getHeight();
			}
			if (minHeight > lt.getHeight()) {
				minHeight = lt.getHeight();
			}

		}

		public LineText get(int idx) {
			return mlt.get(idx);
		}

		public float getHegiht() {
			float height = 0.0f;

			for (LineText lt : mlt) {
				height += lineSpace;
				// height +=lt.textRect.height;
				height += this.maxHeight;
			}
			height += lineSpace;
			return height;
		}

		public int lineCnt() {
			return this.mlt.size();
		}
	}

	/*
	 * 회전좌표 구하기 Point P1 = new Point(iOldX, iOldY); //회전하려고 하는 좌표 Point P2 = new
	 * Point(2, 2); //시키기 위한 기준 좌표 double dSetDegree = Math.toRadians(newDegree);
	 * double cosq = Math.cos(dSetDegree); double sinq = Math.sin(dSetDegree);
	 * double sx = P1.x - P2.x; double sy = P1.y - P2.y; double rx = (sx * cosq - sy
	 * * sinq) + P2.x; //결과 좌표 x double ry = (sx * sinq + sy * cosq) + P2.y; //결과 좌표
	 * y
	 * 
	 * int iResultX = (int) Math.round(rx); int iResultY = (int) Math.round(ry);
	 */

//	public String getWordList() {
//
//		return "";
//	}

	/*
	 * public Vector<String> getWordList(String name, PointStyle ps) { // int
	 * maxWidth = width;
	 * 
	 * Vector<String> list = new Vector();
	 * 
	 * if (!ps.isMultiLine) { list.add(name); return list; }
	 * 
	 * // list.add(name);
	 * 
	 * int width_ = ps.multiLineSize;
	 * 
	 * char[] chars = name.toCharArray();
	 * 
	 * if (chars.length <= width_) { list.add(name); return list; }
	 * 
	 * int interval = chars.length / width_;
	 * 
	 * int width = chars.length / (interval + 1);
	 * 
	 * int idx = 0; String saveTemp = ""; for (int i = 0; i < chars.length; i++) {
	 * if (idx > width) { if (saveTemp.trim().length() > 0) { list.add(saveTemp); }
	 * saveTemp = ""; idx = 0; } else if (i == (chars.length - 1)) { if
	 * (saveTemp.trim().length() > 0) { saveTemp += chars[i]; list.add(saveTemp); }
	 * } saveTemp += chars[i]; idx++; }
	 * 
	 * return list; }
	 */

	/*
	 * public Vector<String> getWordList2(String name, int width_) { // int maxWidth
	 * = width;
	 * 
	 * Vector<String> list = new Vector(); // list.add(name);
	 * 
	 * char[] chars = name.toCharArray();
	 * 
	 * if (chars.length <= width_) { list.add(name); return list; }
	 * 
	 * int interval = chars.length / width_;
	 * 
	 * int width = chars.length / (interval + 1);
	 * 
	 * int idx = 0; String saveTemp = ""; for (int i = 0; i < chars.length; i++) {
	 * if (idx > width) { if (saveTemp.trim().length() > 0) { list.add(saveTemp); }
	 * saveTemp = ""; idx = 0; } else if (i == (chars.length - 1)) { if
	 * (saveTemp.trim().length() > 0) { saveTemp += chars[i]; list.add(saveTemp); }
	 * } saveTemp += chars[i]; idx++; } // list.add(saveTemp);
	 * 
	 * return list; }
	 */

	public static void main(String[] args) {
		String name = "다울 지오  인포 테스트";
		PointRender pr = new PointRender();
		PointStyle ps = new PointStyle();
		Vector<String> lines = pr.getWordList(name, ps);

		for (String line : lines) {
			System.out.println(line);
		}
	}

	/**
	 * name 을 대문자,소문자,숫자,특수문자,한글로 구분하고 PointStyle.multiLineSize 사이즈로 구분된 단위로 멀티 라인
	 * 처리함.
	 * 
	 * @param name
	 * @param ps
	 * @return
	 */
	public Vector<String> getAutoWords(String name, PointStyle ps) {

		if (name.trim().length() == 1) {
			Vector<String> result = new Vector();
			result.add(name);
			return result;
		}

		Vector<String> strs = this.getSeperateWords(name);

		int maxLength = ps.multiLineSize;
		String line = "";
		Vector<String> multiLine = new Vector();
		// for(String str : strs){
		for (int i = 0; i < strs.size(); i++) {

			String str = strs.get(i);

			int curLineSize = line.length();
			int restLineSize = maxLength - curLineSize;

			if (restLineSize < str.length() && (str.length() - restLineSize) / maxLength > 0) {
				int nowRestLineSize = 0;
				if (curLineSize < (maxLength / 2)) {
					line += str.substring(0, restLineSize);
					multiLine.add(line);
					line = "";
					nowRestLineSize = restLineSize;
				} else {
					multiLine.add(line);
					line = "";
				}

				String nextLine = str.substring(nowRestLineSize, str.length());

				int len = nextLine.length() / maxLength;
				int lastLen = nextLine.length() % maxLength;

				for (int k = 0; k < len; k++) {
					String addLine = nextLine.substring(maxLength * k, maxLength * (k + 1));
					multiLine.add(addLine);
				}
				line += nextLine.substring(maxLength * (len), nextLine.length());
			} else if (restLineSize < str.length() && (str.length() - restLineSize) / maxLength == 0) {
				// line+=str;

				int temp = str.length() - restLineSize;
				if (restLineSize > (maxLength / 2) && temp > maxLength / 3) {
					line += str.substring(0, restLineSize);
					multiLine.add(line);
					line = "";
					line += str.substring(restLineSize, str.length());
				} else {
					multiLine.add(line);
					line = "";
					line += str;
				}

			} else {
				line += str;
			}

		}
		multiLine.add(line);

		if (multiLine.size() > 1) {
			String temp = multiLine.lastElement();
			// if(temp.length() < (maxLength/3)){
			if (temp.length() < 3) {
				multiLine.set(multiLine.size() - 2, multiLine.get(multiLine.size() - 2) + temp);
				multiLine.remove(multiLine.size() - 1);
			}
		}

		if (multiLine.size() > 1) {
			String temp = multiLine.lastElement();
			if (temp.length() == 0) {
				multiLine.remove(multiLine.size() - 1);
			}
		}

		String temp = multiLine.firstElement();
		if (temp.length() == 0) {
			multiLine.remove(0);
		}

		return multiLine;

	}

	public Vector<Integer> getIdx(String input) {
		Vector<Integer> vts = new Vector();
		char[] charArray = input.toCharArray();
		for (int j = 0; j < charArray.length - 1; j++) {
			int oneMode = 0;
			int twoMode = 0;

			if (charArray[j] >= 'A' && charArray[j] <= 'Z') {
				// System.out.println(charArray[j] + "=>" + "대문자(English)");
				oneMode = 1;
			} else if (charArray[j] >= 'a' && charArray[j] <= 'z') {
				oneMode = 2;
				// System.out.println(charArray[j] + "=>" + "소문자(English)");
			} else if (charArray[j] >= '\uAC00' && charArray[j] <= '\uD7A3') {
				oneMode = 3;
				// System.out.print(charArray[j] + " =>" + "(한글)");
			} else if ((charArray[j] >= '!' && charArray[j] <= '/') || (charArray[j] >= '[' && charArray[j] <= 0x60)
					|| (charArray[j] >= '{' && charArray[j] <= '~') || (charArray[j] >= ':' && charArray[j] <= '@')) {
				oneMode = 4;
				// System.out.println(charArray[j] + " =>" + "(특수 문자)");
			} else if (charArray[j] >= '0' && charArray[j] <= '9') {
				oneMode = 5;
				// System.out.println(charArray[j] + "=>" + "(숫자)");
			} else {
				// System.out.println("나머지");
				oneMode = 6;
			}

			j++;

			if (charArray[j] >= 'A' && charArray[j] <= 'Z') {
				twoMode = 1;
			} else if (charArray[j] >= 'a' && charArray[j] <= 'z') {
				twoMode = 2;
			} else if (charArray[j] >= '\uAC00' && charArray[j] <= '\uD7A3') {
				twoMode = 3;
			} else if ((charArray[j] >= '!' && charArray[j] <= '/') || (charArray[j] >= '[' && charArray[j] <= 0x60)
					|| (charArray[j] >= '{' && charArray[j] <= '~') || (charArray[j] >= ':' && charArray[j] <= '@')) {
				twoMode = 4;
			} else if (charArray[j] >= '0' && charArray[j] <= '9') {
				twoMode = 5;
			} else {
				twoMode = 6;
			}

			if (oneMode != twoMode) {
				vts.add(j);
			}
			j--;
		}

		return vts;
	}

	/**
	 * 대문자,소문자,한글,특수문자,숫자로 구분하여 리턴
	 * 
	 * @param input
	 * @return
	 */
	public Vector<String> getSeperateWords(String input) {
		Vector<String> vts = new Vector();
		char[] charArray = input.toCharArray();
		String word = "";
		for (int j = 0; j < charArray.length - 1; j++) {
			int oneMode = 0;
			int twoMode = 0;

			if (charArray[j] >= 'A' && charArray[j] <= 'Z') {
				// System.out.println(charArray[j] + "=>" + "대문자(English)");
				oneMode = 1;
			} else if (charArray[j] >= 'a' && charArray[j] <= 'z') {
				oneMode = 2;
				// System.out.println(charArray[j] + "=>" + "소문자(English)");
			} else if (charArray[j] >= '\uAC00' && charArray[j] <= '\uD7A3') {
				oneMode = 3;
				// System.out.print(charArray[j] + " =>" + "(한글)");
			} else if ((charArray[j] >= '!' && charArray[j] <= '/') || (charArray[j] >= '[' && charArray[j] <= 0x60)
					|| (charArray[j] >= '{' && charArray[j] <= '~') || (charArray[j] >= ':' && charArray[j] <= '@')) {
				oneMode = 4;
				// System.out.println(charArray[j] + " =>" + "(특수 문자)");
			} else if (charArray[j] >= '0' && charArray[j] <= '9') {
				oneMode = 5;
				// System.out.println(charArray[j] + "=>" + "(숫자)");
			} else {
				// System.out.println("나머지");
				oneMode = 6;
			}

			j++;

			if (charArray[j] >= 'A' && charArray[j] <= 'Z') {
				twoMode = 1;
			} else if (charArray[j] >= 'a' && charArray[j] <= 'z') {
				twoMode = 2;
			} else if (charArray[j] >= '\uAC00' && charArray[j] <= '\uD7A3') {
				twoMode = 3;
			} else if ((charArray[j] >= '!' && charArray[j] <= '/') || (charArray[j] >= '[' && charArray[j] <= 0x60)
					|| (charArray[j] >= '{' && charArray[j] <= '~') || (charArray[j] >= ':' && charArray[j] <= '@')) {
				twoMode = 4;
			} else if (charArray[j] >= '0' && charArray[j] <= '9') {
				twoMode = 5;
			} else {
				twoMode = 6;
			}

			if (oneMode != twoMode && !(oneMode == 1 && twoMode == 2) && oneMode != 4) {
				word += charArray[j - 1];
				vts.add(word);
				word = "";
				j--;

				if (j == charArray.length - 2) {
					vts.add(charArray[j + 1] + "");
					// vts.add(word);
				}

				continue;
			}

			j--;

			word += charArray[j];

			if (j == charArray.length - 2) {
				word += charArray[j + 1];
				vts.add(word);
			}
		}

		return vts;
	}

}
