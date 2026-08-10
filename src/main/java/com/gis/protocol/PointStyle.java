//
// 이 파일은 JAXB(JavaTM Architecture for XML Binding) 참조 구현 2.2.8-b130911.1802 버전을 통해 생성되었습니다. 
// <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>를 참조하십시오. 
// 이 파일을 수정하면 소스 스키마를 재컴파일할 때 수정 사항이 손실됩니다. 
// 생성 날짜: 2025.12.08 시간 01:50:29 PM KST 
//


package com.gis.protocol;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>anonymous complex type에 대한 Java 클래스입니다.
 * 
 * <p>다음 스키마 단편이 이 클래스에 포함되는 필요한 콘텐츠를 지정합니다.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="drawOrder" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element ref="{}desc"/>
 *         &lt;element ref="{}name"/>
 *         &lt;element ref="{}priority"/>
 *         &lt;element ref="{}positionType"/>
 *         &lt;element ref="{}markStyle" minOccurs="0"/>
 *         &lt;element ref="{}symbolStyle" minOccurs="0"/>
 *         &lt;element ref="{}textStyle" minOccurs="0"/>
 *         &lt;element ref="{}query" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="tr" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="transparency" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="shapeDraw" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element ref="{}overlapConfig" minOccurs="0"/>
 *         &lt;element name="offsetX" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="offsetY" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="grid" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "drawOrder",
    "desc",
    "name",
    "priority",
    "positionType",
    "markStyle",
    "symbolStyle",
    "textStyle",
    "query",
    "tr",
    "transparency",
    "shapeDraw",
    "overlapConfig",
    "offsetX",
    "offsetY",
    "grid"
})
@XmlRootElement(name = "pointStyle")
public class PointStyle {

    @XmlElement(required = true)
    protected String drawOrder;
    @XmlElement(required = true)
    protected String desc;
    @XmlElement(required = true)
    protected String name;
    protected int priority;
    @XmlElement(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String positionType;
    protected MarkStyle markStyle;
    protected SymbolStyle symbolStyle;
    protected TextStyle textStyle;
    protected List<Query> query;
    @XmlElement(required = true)
    protected String tr;
    protected int transparency;
    protected Boolean shapeDraw;
    protected OverlapConfig overlapConfig;
    protected Integer offsetX;
    protected Integer offsetY;
    protected int grid;

    /**
     * drawOrder 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDrawOrder() {
        return drawOrder;
    }

    /**
     * drawOrder 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDrawOrder(String value) {
        this.drawOrder = value;
    }

    /**
     * 스타일 설명
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDesc() {
        return desc;
    }

    /**
     * desc 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDesc(String value) {
        this.desc = value;
    }

    /**
     * 스타일 이름
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
     * 우선 순위
     * 
     */
    public int getPriority() {
        return priority;
    }

    /**
     * priority 속성의 값을 설정합니다.
     * 
     */
    public void setPriority(int value) {
        this.priority = value;
    }

    /**
     * positionType 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPositionType() {
        return positionType;
    }

    /**
     * positionType 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPositionType(String value) {
        this.positionType = value;
    }

    /**
     * markStyle 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link MarkStyle }
     *     
     */
    public MarkStyle getMarkStyle() {
        return markStyle;
    }

    /**
     * markStyle 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link MarkStyle }
     *     
     */
    public void setMarkStyle(MarkStyle value) {
        this.markStyle = value;
    }

    /**
     * symbolStyle 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link SymbolStyle }
     *     
     */
    public SymbolStyle getSymbolStyle() {
        return symbolStyle;
    }

    /**
     * symbolStyle 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link SymbolStyle }
     *     
     */
    public void setSymbolStyle(SymbolStyle value) {
        this.symbolStyle = value;
    }

    /**
     * textStyle 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link TextStyle }
     *     
     */
    public TextStyle getTextStyle() {
        return textStyle;
    }

    /**
     * textStyle 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link TextStyle }
     *     
     */
    public void setTextStyle(TextStyle value) {
        this.textStyle = value;
    }

    /**
     * Gets the value of the query property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the query property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getQuery().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Query }
     * 
     * 
     */
    public List<Query> getQuery() {
        if (query == null) {
            query = new ArrayList<Query>();
        }
        return this.query;
    }

    /**
     * tr 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTr() {
        return tr;
    }

    /**
     * tr 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTr(String value) {
        this.tr = value;
    }

    /**
     * transparency 속성의 값을 가져옵니다.
     * 
     */
    public int getTransparency() {
        return transparency;
    }

    /**
     * transparency 속성의 값을 설정합니다.
     * 
     */
    public void setTransparency(int value) {
        this.transparency = value;
    }

    /**
     * shapeDraw 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isShapeDraw() {
        return shapeDraw;
    }

    /**
     * shapeDraw 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShapeDraw(Boolean value) {
        this.shapeDraw = value;
    }

    /**
     * overlapConfig 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link OverlapConfig }
     *     
     */
    public OverlapConfig getOverlapConfig() {
        return overlapConfig;
    }

    /**
     * overlapConfig 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link OverlapConfig }
     *     
     */
    public void setOverlapConfig(OverlapConfig value) {
        this.overlapConfig = value;
    }

    /**
     * offsetX 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getOffsetX() {
        return offsetX;
    }

    /**
     * offsetX 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setOffsetX(Integer value) {
        this.offsetX = value;
    }

    /**
     * offsetY 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getOffsetY() {
        return offsetY;
    }

    /**
     * offsetY 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setOffsetY(Integer value) {
        this.offsetY = value;
    }

    /**
     * grid 속성의 값을 가져옵니다.
     * 
     */
    public int getGrid() {
        return grid;
    }

    /**
     * grid 속성의 값을 설정합니다.
     * 
     */
    public void setGrid(int value) {
        this.grid = value;
    }

}
