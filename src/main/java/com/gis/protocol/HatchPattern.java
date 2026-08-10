//
// 이 파일은 JAXB(JavaTM Architecture for XML Binding) 참조 구현 2.2.8-b130911.1802 버전을 통해 생성되었습니다. 
// <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>를 참조하십시오. 
// 이 파일을 수정하면 소스 스키마를 재컴파일할 때 수정 사항이 손실됩니다. 
// 생성 날짜: 2025.12.08 시간 01:50:29 PM KST 
//


package com.gis.protocol;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * 빗금 그리기
 * 
 * <p>hatchPattern complex type에 대한 Java 클래스입니다.
 * 
 * <p>다음 스키마 단편이 이 클래스에 포함되는 필요한 콘텐츠를 지정합니다.
 * 
 * <pre>
 * &lt;complexType name="hatchPattern">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}color"/>
 *         &lt;element ref="{}transparency"/>
 *         &lt;element name="lineWidth" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *         &lt;element name="Width" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="type" type="{}hatchType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "hatchPattern", propOrder = {
    "color",
    "transparency",
    "lineWidth",
    "width",
    "type"
})
public class HatchPattern {

    @XmlElement(required = true)
    protected String color;
    protected int transparency;
    protected float lineWidth;
    @XmlElement(name = "Width")
    protected int width;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected HatchType type;

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
     * lineWidth 속성의 값을 가져옵니다.
     * 
     */
    public float getLineWidth() {
        return lineWidth;
    }

    /**
     * lineWidth 속성의 값을 설정합니다.
     * 
     */
    public void setLineWidth(float value) {
        this.lineWidth = value;
    }

    /**
     * width 속성의 값을 가져옵니다.
     * 
     */
    public int getWidth() {
        return width;
    }

    /**
     * width 속성의 값을 설정합니다.
     * 
     */
    public void setWidth(int value) {
        this.width = value;
    }

    /**
     * type 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link HatchType }
     *     
     */
    public HatchType getType() {
        return type;
    }

    /**
     * type 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link HatchType }
     *     
     */
    public void setType(HatchType value) {
        this.type = value;
    }

}
