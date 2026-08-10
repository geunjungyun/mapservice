package com.gis.projection;

import java.awt.*;
import java.math.*;

import org.locationtech.jts.geom.Envelope;




/**
 * 화면좌표 <--> 실제 좌표 변환 클래스
 * 
 * @author Administrator
 * 
 */
public class ScreenCoordUtil {

	private Rectangle m_screenDimension = null;
	private Envelope rectMbr;

	private double m_dwScale;
	private double m_dwScreenRatio;
	private double realBase;
	private double screenBase;

	/**
	 * 생성자
	 * 
	 * @param scrDimension
	 *            화면 영역
	 * @param rectMbr
	 *            지도 영역
	 */
	public ScreenCoordUtil(Dimension scrDimension, Envelope rectMbr) {
		setScreenDimension(scrDimension);
		setMapRectangle(rectMbr);
		calScale();
	}

	public ScreenCoordUtil() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * 영역 셋팅
	 * 
	 * @param scrDimension
	 *            화면 영역
	 * @param rectMbr
	 *            지도 영역
	 */
	public void setData(Dimension scrDimension, Envelope rectMbr) {
		
		
		setScreenDimension(scrDimension);
		setMapRectangle(rectMbr);
		calScale();
	}

	/**
	 * 화면 영역 셋팅
	 * 
	 * @param _scrDimension
	 */
	private void setScreenDimension(Dimension _scrDimension) {
		if (this.m_screenDimension == null) {
			this.m_screenDimension = new Rectangle();
		}
//		m_screenDimension.bottom = _scrDimension.bottom;
//		m_screenDimension.top = _scrDimension.top;
//		m_screenDimension.left = _scrDimension.left;
//		m_screenDimension.right = _scrDimension.right;
		this.m_screenDimension.setSize(_scrDimension);
		// calScale();
	}

	/**
	 * 화면 영역 리턴
	 * 
	 * @return
	 */
	public Rectangle getScreenDimension() {
		return m_screenDimension;
	}

	/**
	 * 지도 영역 셋팅
	 * 
	 * @param _rectMbr
	 */
	private void setMapRectangle(Envelope _rectMbr) {
		if (this.rectMbr == null) {
			this.rectMbr = new Envelope();
		}
		this.rectMbr.init(_rectMbr);
		calScale();
	}

	/**
	 * 지도 영영 리턴
	 * 
	 * @return
	 */
	public Envelope getMapMBR() {
		return this.rectMbr;
	}

	/**
	 * 화면 비율 셋팅
	 */
	private void calScale() {
		// realBase = (rectMbr.getWidth() <=
		// rectMbr.getHeight())?rectMbr.getHeight() : rectMbr.getWidth();
		// screenBase = (m_screenDimension.width() <=
		// m_screenDimension.height()) ? m_screenDimension.height()
		// :m_screenDimension.width() ;
		// m_dwScale = (screenBase / realBase);
		realBase = rectMbr.getHeight();
		screenBase = m_screenDimension.getHeight();
		m_dwScale = (screenBase / realBase);
	}

	/**
	 * x 좌표를 화면의 x좌표로 변환
	 * @param nMapX
	 * @return
	 */
	public double getMapXToScrX(double nMapX) {
		return (m_dwScale * (nMapX - rectMbr.getMinX()));
	}

	/**
	 * y 좌표를 화면의 y좌표로 변환
	 * @param nMapY
	 * @return
	 */
	public double getMapYToScrY(double nMapY) {
		return (m_screenDimension.getHeight() - ((nMapY - rectMbr.getMinY()) * m_dwScale));
	}
	/**
	 * 화면의 x좌표를 실제 x좌표로 변환
	 * @param nScrX
	 * @returns
	 */
	public double getScrXToMapX(int nScrX) {
		return (float) (nScrX / m_dwScale + rectMbr.getMinX());
	}
	
	/**
	 * 화면의 y좌표를 실제 y좌표 변환
	 * @param nScrY
	 * @return
	 */
	public double getScrYToMapY(int nScrY) {
		return (float) (realBase - (nScrY / m_dwScale) + rectMbr.getMinY());
	}

	/**
	 * 실제 영역과 화면 영역의 축척 비율
	 * @return
	 */
	public double getScale() {
		// int scale = (int)( 1.0/ ((screenBase*this.meterPerPixel) / realBase)
		// );

		return 1.0 / m_dwScale;
	}

}
