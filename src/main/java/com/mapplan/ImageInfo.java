//
// 이 파일은 JAXB(JavaTM Architecture for XML Binding) 참조 구현 2.2.8-b130911.1802 버전을 통해 생성되었습니다. 
// <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>를 참조하십시오. 
// 이 파일을 수정하면 소스 스키마를 재컴파일할 때 수정 사항이 손실됩니다. 
// 생성 날짜: 2020.05.03 시간 01:42:16 PM KST 
//


package com.mapplan;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>imageInfo complex type에 대한 Java 클래스입니다.
 * 
 * <p>다음 스키마 단편이 이 클래스에 포함되는 필요한 콘텐츠를 지정합니다.
 * 
 * <pre>
 * &lt;complexType name="imageInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="imageName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ratio" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *         &lt;element name="screenWidth" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="screenHeight" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="tilePath" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="imageWidth" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="imageHeight" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="status" type="{}status" minOccurs="0"/>
 *         &lt;element name="imgSize" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="tileSize" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="levelCnt" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="color" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "imageInfo", propOrder = {
    "imageName",
    "ratio",
    "screenWidth",
    "screenHeight",
    "tilePath",
    "imageWidth",
    "imageHeight",
    "status",
    "imgSize",
    "tileSize",
    "levelCnt",
    "color"
})
public class ImageInfo {

    @XmlElement(required = true)
    protected String imageName;
    protected Float ratio;
    protected Integer screenWidth;
    protected Integer screenHeight;
    @XmlElement(required = true)
    protected String tilePath;
    protected Integer imageWidth;
    protected Integer imageHeight;
    @XmlSchemaType(name = "string")
    protected Status status;
    protected Long imgSize;
    protected Long tileSize;
    protected Integer levelCnt;
    protected String color;

    /**
     * imageName 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getImageName() {
        return imageName;
    }

    /**
     * imageName 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setImageName(String value) {
        this.imageName = value;
    }

    /**
     * ratio 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getRatio() {
        return ratio;
    }

    /**
     * ratio 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setRatio(Float value) {
        this.ratio = value;
    }

    /**
     * screenWidth 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getScreenWidth() {
        return screenWidth;
    }

    /**
     * screenWidth 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setScreenWidth(Integer value) {
        this.screenWidth = value;
    }

    /**
     * screenHeight 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getScreenHeight() {
        return screenHeight;
    }

    /**
     * screenHeight 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setScreenHeight(Integer value) {
        this.screenHeight = value;
    }

    /**
     * tilePath 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTilePath() {
        return tilePath;
    }

    /**
     * tilePath 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTilePath(String value) {
        this.tilePath = value;
    }

    /**
     * imageWidth 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getImageWidth() {
        return imageWidth;
    }

    /**
     * imageWidth 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setImageWidth(Integer value) {
        this.imageWidth = value;
    }

    /**
     * imageHeight 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getImageHeight() {
        return imageHeight;
    }

    /**
     * imageHeight 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setImageHeight(Integer value) {
        this.imageHeight = value;
    }

    /**
     * status 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link Status }
     *     
     */
    public Status getStatus() {
        return status;
    }

    /**
     * status 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Status }
     *     
     */
    public void setStatus(Status value) {
        this.status = value;
    }

    /**
     * imgSize 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getImgSize() {
        return imgSize;
    }

    /**
     * imgSize 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setImgSize(Long value) {
        this.imgSize = value;
    }

    /**
     * tileSize 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getTileSize() {
        return tileSize;
    }

    /**
     * tileSize 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setTileSize(Long value) {
        this.tileSize = value;
    }

    /**
     * levelCnt 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getLevelCnt() {
        return levelCnt;
    }

    /**
     * levelCnt 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setLevelCnt(Integer value) {
        this.levelCnt = value;
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

}
