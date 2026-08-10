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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element ref="{}name"/>
 *         &lt;element ref="{}desc"/>
 *         &lt;element ref="{}lineStyle"/>
 *         &lt;element ref="{}fillStyle"/>
 *         &lt;element ref="{}query" maxOccurs="unbounded" minOccurs="0"/>
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
    "name",
    "desc",
    "lineStyle",
    "fillStyle",
    "query"
})
@XmlRootElement(name = "polylineStyle")
public class PolylineStyle {

    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true)
    protected String desc;
    @XmlElement(required = true)
    protected LineStyle lineStyle;
    @XmlElement(required = true)
    protected FillStyle fillStyle;
    protected List<Query> query;

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
     * 라인을 그리기 위한 스타일
     * 
     * @return
     *     possible object is
     *     {@link LineStyle }
     *     
     */
    public LineStyle getLineStyle() {
        return lineStyle;
    }

    /**
     * lineStyle 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link LineStyle }
     *     
     */
    public void setLineStyle(LineStyle value) {
        this.lineStyle = value;
    }

    /**
     * 형상을 체우기 위한 스타일 정보
     * 
     * @return
     *     possible object is
     *     {@link FillStyle }
     *     
     */
    public FillStyle getFillStyle() {
        return fillStyle;
    }

    /**
     * fillStyle 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link FillStyle }
     *     
     */
    public void setFillStyle(FillStyle value) {
        this.fillStyle = value;
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

}
