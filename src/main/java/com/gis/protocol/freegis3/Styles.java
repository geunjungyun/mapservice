//
// 이 파일은 JAXB(JavaTM Architecture for XML Binding) 참조 구현 2.2.8-b130911.1802 버전을 통해 생성되었습니다. 
// <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>를 참조하십시오. 
// 이 파일을 수정하면 소스 스키마를 재컴파일할 때 수정 사항이 손실됩니다. 
// 생성 날짜: 2026.06.22 시간 02:12:30 PM KST 
//


package com.gis.protocol.freegis3;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 스타일 정보를 담고 있는 최상위 엘리먼트
 * 
 * <p>styles complex type에 대한 Java 클래스입니다.
 * 
 * <p>다음 스키마 단편이 이 클래스에 포함되는 필요한 콘텐츠를 지정합니다.
 * 
 * <pre>
 * &lt;complexType name="styles">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="update" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element ref="{}polygonStyle" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{}polylineStyle" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{}pointStyle" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{}rasterStyle" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="resource" type="{}resource" minOccurs="0"/>
 *         &lt;element name="lineIntervalPixel" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="lineExtensionWidthRatio" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *         &lt;element name="symbolPath" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "styles", propOrder = {
    "name",
    "update",
    "polygonStyle",
    "polylineStyle",
    "pointStyle",
    "rasterStyle",
    "resource",
    "lineIntervalPixel",
    "lineExtensionWidthRatio",
    "lineOverflow",
    "symbolPath"
})
public class Styles {

    protected String name;
    protected String update;
    protected List<PolygonStyle> polygonStyle;
    protected List<PolylineStyle> polylineStyle;
    protected List<PointStyle> pointStyle;
    protected List<RasterStyle> rasterStyle;
    protected Resource resource;
    protected Integer lineIntervalPixel;
    protected Float lineExtensionWidthRatio;
    protected Boolean lineOverflow;
    protected String symbolPath;

    /**
     * name 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * name 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * update 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUpdate() {
        return update;
    }

    /**
     * update 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUpdate(String value) {
        this.update = value;
    }

    /**
     * Gets the value of the polygonStyle property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the polygonStyle property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPolygonStyle().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PolygonStyle }
     * 
     * 
     */
    public List<PolygonStyle> getPolygonStyle() {
        if (polygonStyle == null) {
            polygonStyle = new ArrayList<PolygonStyle>();
        }
        return this.polygonStyle;
    }

    /**
     * Gets the value of the polylineStyle property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the polylineStyle property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPolylineStyle().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PolylineStyle }
     * 
     * 
     */
    public List<PolylineStyle> getPolylineStyle() {
        if (polylineStyle == null) {
            polylineStyle = new ArrayList<PolylineStyle>();
        }
        return this.polylineStyle;
    }

    /**
     * Gets the value of the pointStyle property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the pointStyle property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPointStyle().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PointStyle }
     * 
     * 
     */
    public List<PointStyle> getPointStyle() {
        if (pointStyle == null) {
            pointStyle = new ArrayList<PointStyle>();
        }
        return this.pointStyle;
    }

    /**
     * Gets the value of the rasterStyle property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the rasterStyle property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRasterStyle().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RasterStyle }
     * 
     * 
     */
    public List<RasterStyle> getRasterStyle() {
        if (rasterStyle == null) {
            rasterStyle = new ArrayList<RasterStyle>();
        }
        return this.rasterStyle;
    }

    /**
     * resource 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link Resource }
     *     
     */
    public Resource getResource() {
        return resource;
    }

    /**
     * resource 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Resource }
     *     
     */
    public void setResource(Resource value) {
        this.resource = value;
    }

    /**
     * lineIntervalPixel 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getLineIntervalPixel() {
        return lineIntervalPixel;
    }

    /**
     * lineIntervalPixel 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setLineIntervalPixel(Integer value) {
        this.lineIntervalPixel = value;
    }

    /**
     * lineExtensionWidthRatio 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getLineExtensionWidthRatio() {
        return lineExtensionWidthRatio;
    }

    /**
     * lineExtensionWidthRatio 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setLineExtensionWidthRatio(Float value) {
        this.lineExtensionWidthRatio = value;
    }

    /**
     * lineOverflow 속성의 값을 가져옵니다.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isLineOverflow() {
        return lineOverflow;
    }

    /**
     * lineOverflow 속성의 값을 설정합니다.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setLineOverflow(Boolean value) {
        this.lineOverflow = value;
    }

    /**
     * symbolPath 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSymbolPath() {
        return symbolPath;
    }

    /**
     * symbolPath 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSymbolPath(String value) {
        this.symbolPath = value;
    }

}
