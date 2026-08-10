//
// 이 파일은 JAXB(JavaTM Architecture for XML Binding) 참조 구현 2.2.8-b130911.1802 버전을 통해 생성되었습니다. 
// <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>를 참조하십시오. 
// 이 파일을 수정하면 소스 스키마를 재컴파일할 때 수정 사항이 손실됩니다. 
// 생성 날짜: 2026.06.22 시간 02:58:37 PM KST 
//


package com.mapplan;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>excel complex type에 대한 Java 클래스입니다.
 * 
 * <p>다음 스키마 단편이 이 클래스에 포함되는 필요한 콘텐츠를 지정합니다.
 * 
 * <pre>
 * &lt;complexType name="excel">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="foldName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="foldName1" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="foldName2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="epsg" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="code" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="deleteCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="fillColor" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="fillWidth" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *         &lt;element name="lineColor" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="lineWidth" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *         &lt;element name="codeField" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="textField" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="landCityName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="fieldType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="drawOrder" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="addFields" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "excel", propOrder = {
    "name",
    "foldName",
    "foldName1",
    "foldName2",
    "epsg",
    "code",
    "deleteCode",
    "fillColor",
    "fillWidth",
    "lineColor",
    "lineWidth",
    "codeField",
    "textField",
    "landCityName",
    "fieldType",
    "drawOrder",
    "addFields"
})
public class Excel {

    protected String name;
    protected String foldName;
    protected String foldName1;
    protected String foldName2;
    protected String epsg;
    protected String code;
    protected String deleteCode;
    protected String fillColor;
    protected Float fillWidth;
    protected String lineColor;
    protected Float lineWidth;
    protected String codeField;
    protected String textField;
    protected String landCityName;
    protected String fieldType;
    protected String drawOrder;
    protected String addFields;

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
     * foldName 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFoldName() {
        return foldName;
    }

    /**
     * foldName 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFoldName(String value) {
        this.foldName = value;
    }

    /**
     * foldName1 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFoldName1() {
        return foldName1;
    }

    /**
     * foldName1 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFoldName1(String value) {
        this.foldName1 = value;
    }

    /**
     * foldName2 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFoldName2() {
        return foldName2;
    }

    /**
     * foldName2 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFoldName2(String value) {
        this.foldName2 = value;
    }

    /**
     * epsg 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEpsg() {
        return epsg;
    }

    /**
     * epsg 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEpsg(String value) {
        this.epsg = value;
    }

    /**
     * code 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCode() {
        return code;
    }

    /**
     * code 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCode(String value) {
        this.code = value;
    }

    /**
     * deleteCode 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDeleteCode() {
        return deleteCode;
    }

    /**
     * deleteCode 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDeleteCode(String value) {
        this.deleteCode = value;
    }

    /**
     * fillColor 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFillColor() {
        return fillColor;
    }

    /**
     * fillColor 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFillColor(String value) {
        this.fillColor = value;
    }

    /**
     * fillWidth 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getFillWidth() {
        return fillWidth;
    }

    /**
     * fillWidth 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setFillWidth(Float value) {
        this.fillWidth = value;
    }

    /**
     * lineColor 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLineColor() {
        return lineColor;
    }

    /**
     * lineColor 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLineColor(String value) {
        this.lineColor = value;
    }

    /**
     * lineWidth 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getLineWidth() {
        return lineWidth;
    }

    /**
     * lineWidth 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setLineWidth(Float value) {
        this.lineWidth = value;
    }

    /**
     * codeField 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCodeField() {
        return codeField;
    }

    /**
     * codeField 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCodeField(String value) {
        this.codeField = value;
    }

    /**
     * textField 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTextField() {
        return textField;
    }

    /**
     * textField 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTextField(String value) {
        this.textField = value;
    }

    /**
     * landCityName 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLandCityName() {
        return landCityName;
    }

    /**
     * landCityName 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLandCityName(String value) {
        this.landCityName = value;
    }

    /**
     * fieldType 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFieldType() {
        return fieldType;
    }

    /**
     * fieldType 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFieldType(String value) {
        this.fieldType = value;
    }

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
     * addFields 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAddFields() {
        return addFields;
    }

    /**
     * addFields 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAddFields(String value) {
        this.addFields = value;
    }

}
