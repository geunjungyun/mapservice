package com.gis2.map.style;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Vector;



public class PointStyle extends BasicStyleExtend {

	public String priorityFieldName = null;
	public int priority = -1;

	public String positionType = null;
	
	private float fontSize = -1;
	
	//public int multiLineSize = 4;
	
	public int multiLineSize = 8;
	
	public String multiLineSeperator = null;
	
//	public boolean isMultiLine = false;
//	public boolean isSeperateUnicode = false;
//	public Vector<String> seperateStrings = new Vector();

	// BOLD , CENTER_BASELINE,HANGING_BASELINE
	// ITALIC ,PLAIN

	public int fontType = -1;
	public String fontName = null;
	public Font font = null;
	
	public Font outLineFont = null;
	public String nameField = null;
	public boolean isTextRender = false;

	public Color markColor = null;
	public int markSize = -1;
	public boolean isMarkRender = false;

	public BufferedImage symbol = null;
	public String symbolNameField = null;
	//public String symbolPath = null;
	
	
	public int symbolWidth = 0;
	public int symbolHeight = 0;
	public boolean isSymbolRender = false;
	
	public String textNameField = null;
	
	public String rotateField = null;
	public int rotate = 0;
	
	
	public String deleteOverlapMode = "";
	public int extenstionArea = 0;
	
	public boolean shapeDraw = false;
	
	public float tracking = 0.1f;
	public float linespace = 0.1f;
	public boolean kerning = true;
	public boolean ligature = true;
	
	public int offsetX = 0;
	public int offsetY = 0;

	public float widthRatio = 1.f;
	
	public float lineExtensionWidthRatio = 0.0f;

	public float lineIntervalPixel = 1000f;

	// 라인이 텍스트 폭보다 짧아도 배제하지 않고 라인 양끝으로 오버플로우시켜 표시할지 여부
	public boolean lineOverflow = false;
	
	
	BasicStyleExtend markerStyle = null;
	//public HashMap fonts = new HashMap();

	public void createFont() {

		// if(CreateMapService.foreinLang != null
		// && !CreateMapService.foreinLang.toLowerCase().equals("engname")){
		//
		// if(CreateMapService.foreinLang.toLowerCase().equals("china_gname")){
		// font = new Font("MS Song", Font.BOLD, fontSize);
		// }
		// else
		// if(CreateMapService.foreinLang.toLowerCase().equals("china_bname")){
		// font = new Font("MS Song", Font.BOLD, fontSize);
		// }
		// else if(CreateMapService.foreinLang.toLowerCase().equals("janname")){
		// font = new Font("MS Gothic", Font.BOLD, fontSize);
		// }
		// else if(CreateMapService.foreinLang.toLowerCase().equals("name")){
		// font = new Font(this.fontName, fontType, fontSize);
		// }
		// else{
		// font = new Font(this.fontName, fontType, fontSize);
		// }
		// }
		// else{
		// font = new Font(this.fontName, fontType, fontSize);
		// }
	}
	
	public void setFontSize(float _fontSize) {
		this.fontSize = _fontSize;
	}
	
	public float getFontSize() {
		return this.fontSize;
	}

//	public void setFont(String name) {
//		if (name != null && !name.toLowerCase().equals("engname")) {
//
//			if (name.toLowerCase().equals("china_gname")) {
//				Font tempF = (Font) this.fonts.get(name);
//				if (tempF == null) {
//					// tempF = new Font("MS Song", Font.BOLD, fontSize);
//					tempF = new Font("simsun", Font.PLAIN, fontSize);
//					this.fonts.put(name, tempF);
//				}
//				this.font = tempF;
//
//			} else if (name.toLowerCase().equals("china_bname")) {
//
//				Font tempF = (Font) this.fonts.get(name);
//				if (tempF == null) {
//					tempF = new Font("mingliu", Font.PLAIN, fontSize + 1);
//					// tempF = new Font("MS Song", Font.BOLD, fontSize);
//					this.fonts.put(name, tempF);
//				}
//				this.font = tempF;
//
//			} else if (name.toLowerCase().equals("janname")) {
//
//				Font tempF = (Font) this.fonts.get(name);
//				if (tempF == null) {
//					tempF = new Font("MS Gothic", Font.BOLD, fontSize);
//					this.fonts.put(name, tempF);
//				}
//				this.font = tempF;
//
//			} else if (name.toLowerCase().equals("name")) {
//
//				Font tempF = (Font) this.fonts.get(name);
//				if (tempF == null) {
//					tempF = new Font(this.fontName, fontType, fontSize);
//					;
//					this.fonts.put(name, tempF);
//				}
//				this.font = tempF;
//
//			} else {
//				Font tempF = (Font) this.fonts.get(name);
//				if (tempF == null) {
//					tempF = new Font(this.fontName, fontType, fontSize);
//					;
//					this.fonts.put(name, tempF);
//				}
//				this.font = tempF;
//			}
//		} else {
//			Font tempF = (Font) this.fonts.get(name);
//			if (tempF == null) {
//				tempF = new Font(this.fontName, fontType, fontSize);
//				;
//				this.fonts.put(name, tempF);
//			}
//			this.font = tempF;
//		}
//	}

	/*
	 * public void setFont(Font _font){ this.font = _font; }
	 */
	public Font getFont() {
		
		return this.font;
	}
	
	public void setFont(Font font){
		this.font = font;
	}

	public void setRenderingText(boolean mode) {
		this.isTextRender = mode;
	}

	public boolean isRenderingText() {
		return this.isTextRender;
	}

	public void setRenderingMark(boolean mode) {
		this.isMarkRender = mode;
		if(mode) {
			this.markerStyle = new BasicStyleExtend ();
		}
	}
	
	public BasicStyleExtend getMarkerStyle() {
		return this.markerStyle;
	}

	public boolean isRenderingMark() {
		return this.isMarkRender;
	}

	public void setRenderingSymbol(boolean mode) {
		this.isSymbolRender = mode;
	}

	public boolean isRenderingSymbol() {
		return this.isSymbolRender;
	}

	public void setSymbol(BufferedImage _symbol) {
		this.symbol = _symbol;
	}
	
	public void setTextNameField(String name){
		this.textNameField = name;
	}
	
	public String getTextnameField(){
		return this.textNameField;
	}

	// 부모 스타일과 자식 스타일을 비교하여 자식 스타일을 따라 가도록 셋팅
	public void setCompare(PointStyle _ps) {

		PointStyle ps = _ps;
		if (ps.priorityFieldName != null && ps.priorityFieldName.length() != 0) {
			this.priorityFieldName = ps.priorityFieldName;
		}

		if (ps.priority != -1) {
			this.priority = ps.priority;
		}

		if (ps.positionType != null && ps.positionType.length() != 0) {
			this.positionType = ps.positionType;
		}

		if (ps.fontType != -1 && ps.fontName != null && ps.font != null) {
			this.fontType = ps.fontType;
			this.fontName = ps.fontName;
			this.font = ps.font;
			this.isTextRender = ps.isTextRender;

			this.setFillColor(ps.getFillColor());
			this.setLineWidth(ps.getLineWidth());
			
			this.setLineColor(ps.getLineColor());

			this.setRenderingFill(ps.isRenderingFill());
			this.setRenderingLine(ps.isRenderingLine());
		}

		if (ps.nameField != null) {
			this.nameField = ps.nameField;
		}
		
		if(ps.rotateField != null){
			this.rotateField = ps.rotateField;
		}
		
		if(ps.rotate != 0){
			this.rotate = ps.rotate;
		}

		if (ps.markColor == null || ps.markSize == -1) {
			this.markColor = ps.markColor;
			this.markSize = ps.markSize;
			this.isMarkRender = ps.isMarkRender;
		}

		if (ps.symbol != null) {
			this.symbol = ps.symbol;
			this.isSymbolRender = ps.isSymbolRender;
		}

		if (ps.symbolNameField != null) {
			this.symbolNameField = ps.symbolNameField;
			this.isSymbolRender = ps.isSymbolRender;
		}
		
		if(ps.getTextnameField() != null){
			this.setTextNameField(ps.getTextnameField());
		}
		
		if(ps.extenstionArea != 0){
			this.extenstionArea = ps.extenstionArea;
		}
		
		if(ps.deleteOverlapMode != null && ps.deleteOverlapMode.length() > 0){
			this.deleteOverlapMode = ps.deleteOverlapMode;
		}
		
		this.kerning = ps.kerning;
		this.ligature = ps.ligature;
		this.linespace = ps.linespace;
		this.tracking = ps.tracking;
		this.offsetX = ps.offsetX;
		this.offsetY = ps.offsetY;

	}

}
