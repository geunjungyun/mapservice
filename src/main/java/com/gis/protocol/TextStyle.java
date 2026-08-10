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
 *         &lt;element name="rotate" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="rotateField" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="multiLineSize" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element ref="{}multiLine" minOccurs="0"/>
 *         &lt;element ref="{}fontName"/>
 *         &lt;element ref="{}fontType"/>
 *         &lt;element ref="{}fontSize"/>
 *         &lt;element ref="{}lineStyle"/>
 *         &lt;element ref="{}fillStyle"/>
 *         &lt;element ref="{}fieldName"/>
 *         &lt;element name="tracking" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *         &lt;element name="kerning" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="ligature" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="linespace" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *         &lt;element name="widthRatio" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *         &lt;element name="lineIntervalPixel" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="lineExtensionWidthRatio" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
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
    "rotate",
    "rotateField",
    "multiLineSize",
    "multiLine",
    "fontName",
    "fontType",
    "fontSize",
    "lineStyle",
    "fillStyle",
    "fieldName",
    "tracking",
    "kerning",
    "ligature",
    "linespace",
    "widthRatio",
    "lineIntervalPixel",
    "lineExtensionWidthRatio"
})
@XmlRootElement(name = "textStyle")
public class TextStyle {

    protected Integer rotate;
    protected String rotateField;
    protected Integer multiLineSize;
    protected MultiLine multiLine;
    @XmlElement(required = true)
    protected String fontName;
    @XmlElement(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String fontType;
    protected float fontSize;
    @XmlElement(required = true)
    protected LineStyle lineStyle;
    @XmlElement(required = true)
    protected FillStyle fillStyle;
    @XmlElement(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String fieldName;
    protected Float tracking;
    protected Boolean kerning;
    protected Boolean ligature;
    protected Float linespace;
    protected Float widthRatio;
    protected Integer lineIntervalPixel;
    protected Float lineExtensionWidthRatio;

    /**
     * rotate 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getRotate() {
        return rotate;
    }

    /**
     * rotate 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setRotate(Integer value) {
        this.rotate = value;
    }

    /**
     * rotateField 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRotateField() {
        return rotateField;
    }

    /**
     * rotateField 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRotateField(String value) {
        this.rotateField = value;
    }

    /**
     * multiLineSize 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMultiLineSize() {
        return multiLineSize;
    }

    /**
     * multiLineSize 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMultiLineSize(Integer value) {
        this.multiLineSize = value;
    }

    /**
     * multiLine 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link MultiLine }
     *     
     */
    public MultiLine getMultiLine() {
        return multiLine;
    }

    /**
     * multiLine 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link MultiLine }
     *     
     */
    public void setMultiLine(MultiLine value) {
        this.multiLine = value;
    }

    /**
     * fontName 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFontName() {
        return fontName;
    }

    /**
     * fontName 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFontName(String value) {
        this.fontName = value;
    }

    /**
     * fontType 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFontType() {
        return fontType;
    }

    /**
     * fontType 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFontType(String value) {
        this.fontType = value;
    }

    /**
     * fontSize 속성의 값을 가져옵니다.
     * 
     */
    public float getFontSize() {
        return fontSize;
    }

    /**
     * fontSize 속성의 값을 설정합니다.
     * 
     */
    public void setFontSize(float value) {
        this.fontSize = value;
    }

    /**
     * lineStyle 속성의 값을 가져옵니다.
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
     * fillStyle 속성의 값을 가져옵니다.
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
     * fieldName 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * fieldName 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFieldName(String value) {
        this.fieldName = value;
    }

    /**
     * tracking 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getTracking() {
        return tracking;
    }

    /**
     * tracking 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setTracking(Float value) {
        this.tracking = value;
    }

    /**
     * kerning 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isKerning() {
        return kerning;
    }

    /**
     * kerning 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setKerning(Boolean value) {
        this.kerning = value;
    }

    /**
     * ligature 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isLigature() {
        return ligature;
    }

    /**
     * ligature 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setLigature(Boolean value) {
        this.ligature = value;
    }

    /**
     * linespace 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getLinespace() {
        return linespace;
    }

    /**
     * linespace 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setLinespace(Float value) {
        this.linespace = value;
    }

    /**
     * widthRatio 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getWidthRatio() {
        return widthRatio;
    }

    /**
     * widthRatio 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setWidthRatio(Float value) {
        this.widthRatio = value;
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

}
