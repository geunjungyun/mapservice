//
// 이 파일은 JAXB(JavaTM Architecture for XML Binding) 참조 구현 2.2.8-b130911.1802 버전을 통해 생성되었습니다. 
// <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>를 참조하십시오. 
// 이 파일을 수정하면 소스 스키마를 재컴파일할 때 수정 사항이 손실됩니다. 
// 생성 날짜: 2026.06.22 시간 02:12:30 PM KST 
//


package com.gis.protocol.freegis3;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
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
 *         &lt;element ref="{}width"/>
 *         &lt;element ref="{}color"/>
 *         &lt;element ref="{}transparency"/>
 *         &lt;element name="type" type="{}linePatten" minOccurs="0"/>
 *         &lt;element name="pattern" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="shapePattern" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="cap" type="{}cap"/>
 *         &lt;element name="join" type="{}join"/>
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
    "width",
    "color",
    "transparency",
    "type",
    "pattern",
    "shapePattern",
    "cap",
    "join"
})
@XmlRootElement(name = "lineStyle")
public class LineStyle {

    protected float width;
    @XmlElement(required = true)
    protected String color;
    protected int transparency;
    @XmlSchemaType(name = "string")
    protected LinePatten type;
    @XmlElement(required = true)
    protected String pattern;
    @XmlElement(required = true)
    protected String shapePattern;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected Cap cap;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected Join join;

    /**
     * 라인 두께
     * 
     */
    public float getWidth() {
        return width;
    }

    /**
     * width 속성의 값을 설정합니다.
     * 
     */
    public void setWidth(float value) {
        this.width = value;
    }

    /**
     * color 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getColor() {
        return color;
    }

    /**
     * color 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setColor(String value) {
        this.color = value;
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
     * type 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link LinePatten }
     *     
     */
    public LinePatten getType() {
        return type;
    }

    /**
     * type 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link LinePatten }
     *     
     */
    public void setType(LinePatten value) {
        this.type = value;
    }

    /**
     * pattern 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * pattern 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPattern(String value) {
        this.pattern = value;
    }

    /**
     * shapePattern 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShapePattern() {
        return shapePattern;
    }

    /**
     * shapePattern 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShapePattern(String value) {
        this.shapePattern = value;
    }

    /**
     * cap 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link Cap }
     *     
     */
    public Cap getCap() {
        return cap;
    }

    /**
     * cap 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Cap }
     *     
     */
    public void setCap(Cap value) {
        this.cap = value;
    }

    /**
     * join 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link Join }
     *     
     */
    public Join getJoin() {
        return join;
    }

    /**
     * join 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Join }
     *     
     */
    public void setJoin(Join value) {
        this.join = value;
    }

}
