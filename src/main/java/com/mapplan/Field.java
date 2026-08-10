//
// 이 파일은 JAXB(JavaTM Architecture for XML Binding) 참조 구현 2.2.8-b130911.1802 버전을 통해 생성되었습니다. 
// <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>를 참조하십시오. 
// 이 파일을 수정하면 소스 스키마를 재컴파일할 때 수정 사항이 손실됩니다. 
// 생성 날짜: 2026.06.22 시간 02:58:37 PM KST 
//


package com.mapplan;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 				"fieldType"이 Geometry,GeometryCollection,MultiPolygon,MultiLineString,MultiPoint,Polygon,LineString,Point
 * 				의 경우 "name"의 값은 "the_geom"으로 해야 데이타 변환 호환성에 문제가 없음. 
 * 			
 * 
 * <p>field complex type에 대한 Java 클래스입니다.
 * 
 * <p>다음 스키마 단편이 이 클래스에 포함되는 필요한 콘텐츠를 지정합니다.
 * 
 * <pre>
 * &lt;complexType name="field">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="desc" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="type" type="{}fieldType" minOccurs="0"/>
 *         &lt;element name="size" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="scale" type="{http://www.w3.org/2001/XMLSchema}short" minOccurs="0"/>
 *         &lt;element name="isIndex" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="simpleLine" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *         &lt;element name="simpleArea" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *         &lt;element name="isCodeIndex" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "field", propOrder = {
    "name",
    "desc",
    "type",
    "size",
    "scale",
    "isIndex",
    "simpleLine",
    "simpleArea",
    "isCodeIndex"
})
public class Field {

    protected String name;
    protected String desc;
    @XmlSchemaType(name = "string")
    protected FieldType type;
    protected Integer size;
    protected Short scale;
    protected Boolean isIndex;
    protected Float simpleLine;
    protected Float simpleArea;
    protected Boolean isCodeIndex;

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
     * desc 속성의 값을 가져옵니다.
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
     * type 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link FieldType }
     *     
     */
    public FieldType getType() {
        return type;
    }

    /**
     * type 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link FieldType }
     *     
     */
    public void setType(FieldType value) {
        this.type = value;
    }

    /**
     * size 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getSize() {
        return size;
    }

    /**
     * size 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSize(Integer value) {
        this.size = value;
    }

    /**
     * scale 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getScale() {
        return scale;
    }

    /**
     * scale 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setScale(Short value) {
        this.scale = value;
    }

    /**
     * isIndex 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsIndex() {
        return isIndex;
    }

    /**
     * isIndex 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsIndex(Boolean value) {
        this.isIndex = value;
    }

    /**
     * simpleLine 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getSimpleLine() {
        return simpleLine;
    }

    /**
     * simpleLine 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setSimpleLine(Float value) {
        this.simpleLine = value;
    }

    /**
     * simpleArea 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getSimpleArea() {
        return simpleArea;
    }

    /**
     * simpleArea 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setSimpleArea(Float value) {
        this.simpleArea = value;
    }

    /**
     * isCodeIndex 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsCodeIndex() {
        return isCodeIndex;
    }

    /**
     * isCodeIndex 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsCodeIndex(Boolean value) {
        this.isCodeIndex = value;
    }

}
