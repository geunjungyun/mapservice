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
import javax.xml.bind.annotation.XmlType;


/**
 * <p>mapInfo complex type에 대한 Java 클래스입니다.
 * 
 * <p>다음 스키마 단편이 이 클래스에 포함되는 필요한 콘텐츠를 지정합니다.
 * 
 * <pre>
 * &lt;complexType name="mapInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="mbr" type="{}mbr"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="desc" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="scaleInfos" type="{}scaleInfos"/>
 *         &lt;element name="backgroundColor" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="levelConfigs" type="{}levelConfigs" minOccurs="0"/>
 *         &lt;element name="stylesName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="update" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="backgroundTileMapService" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="mbrExtend" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "mapInfo", propOrder = {
    "mbr",
    "name",
    "desc",
    "scaleInfos",
    "backgroundColor",
    "levelConfigs",
    "stylesName",
    "update",
    "backgroundTileMapService",
    "mbrExtend"
})
public class MapInfo {

    @XmlElement(required = true)
    protected Mbr mbr;
    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true)
    protected String desc;
    @XmlElement(required = true)
    protected ScaleInfos scaleInfos;
    @XmlElement(required = true)
    protected String backgroundColor;
    protected LevelConfigs levelConfigs;
    protected String stylesName;
    protected String update;
    protected List<String> backgroundTileMapService;
    protected Integer mbrExtend;

    /**
     * mbr 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link Mbr }
     *     
     */
    public Mbr getMbr() {
        return mbr;
    }

    /**
     * mbr 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Mbr }
     *     
     */
    public void setMbr(Mbr value) {
        this.mbr = value;
    }

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
     * scaleInfos 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link ScaleInfos }
     *     
     */
    public ScaleInfos getScaleInfos() {
        return scaleInfos;
    }

    /**
     * scaleInfos 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link ScaleInfos }
     *     
     */
    public void setScaleInfos(ScaleInfos value) {
        this.scaleInfos = value;
    }

    /**
     * backgroundColor 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * backgroundColor 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBackgroundColor(String value) {
        this.backgroundColor = value;
    }

    /**
     * levelConfigs 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link LevelConfigs }
     *     
     */
    public LevelConfigs getLevelConfigs() {
        return levelConfigs;
    }

    /**
     * levelConfigs 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link LevelConfigs }
     *     
     */
    public void setLevelConfigs(LevelConfigs value) {
        this.levelConfigs = value;
    }

    /**
     * stylesName 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStylesName() {
        return stylesName;
    }

    /**
     * stylesName 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStylesName(String value) {
        this.stylesName = value;
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
     * Gets the value of the backgroundTileMapService property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the backgroundTileMapService property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBackgroundTileMapService().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getBackgroundTileMapService() {
        if (backgroundTileMapService == null) {
            backgroundTileMapService = new ArrayList<String>();
        }
        return this.backgroundTileMapService;
    }

    /**
     * mbrExtend 속성의 값을 가져옵니다.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMbrExtend() {
        return mbrExtend;
    }

    /**
     * mbrExtend 속성의 값을 설정합니다.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMbrExtend(Integer value) {
        this.mbrExtend = value;
    }

}
