//
// 이 파일은 JAXB(JavaTM Architecture for XML Binding) 참조 구현 2.2.8-b130911.1802 버전을 통해 생성되었습니다. 
// <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>를 참조하십시오. 
// 이 파일을 수정하면 소스 스키마를 재컴파일할 때 수정 사항이 손실됩니다. 
// 생성 날짜: 2026.06.22 시간 02:58:37 PM KST 
//


package com.mapplan;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.mapplan package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _LayerInfo_QNAME = new QName("", "layerInfo");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.mapplan
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link LayerInfo }
     * 
     */
    public LayerInfo createLayerInfo() {
        return new LayerInfo();
    }

    /**
     * Create an instance of {@link Codes }
     * 
     */
    public Codes createCodes() {
        return new Codes();
    }

    /**
     * Create an instance of {@link CodeArea }
     * 
     */
    public CodeArea createCodeArea() {
        return new CodeArea();
    }

    /**
     * Create an instance of {@link PnuGeo }
     * 
     */
    public PnuGeo createPnuGeo() {
        return new PnuGeo();
    }

    /**
     * Create an instance of {@link Analysis }
     * 
     */
    public Analysis createAnalysis() {
        return new Analysis();
    }

    /**
     * Create an instance of {@link Layer }
     * 
     */
    public Layer createLayer() {
        return new Layer();
    }

    /**
     * Create an instance of {@link Users }
     * 
     */
    public Users createUsers() {
        return new Users();
    }

    /**
     * Create an instance of {@link Content }
     * 
     */
    public Content createContent() {
        return new Content();
    }

    /**
     * Create an instance of {@link Excel }
     * 
     */
    public Excel createExcel() {
        return new Excel();
    }

    /**
     * Create an instance of {@link Tiles }
     * 
     */
    public Tiles createTiles() {
        return new Tiles();
    }

    /**
     * Create an instance of {@link Protocol }
     * 
     */
    public Protocol createProtocol() {
        return new Protocol();
    }

    /**
     * Create an instance of {@link Field }
     * 
     */
    public Field createField() {
        return new Field();
    }

    /**
     * Create an instance of {@link Tileset }
     * 
     */
    public Tileset createTileset() {
        return new Tileset();
    }

    /**
     * Create an instance of {@link Function }
     * 
     */
    public Function createFunction() {
        return new Function();
    }

    /**
     * Create an instance of {@link Header }
     * 
     */
    public Header createHeader() {
        return new Header();
    }

    /**
     * Create an instance of {@link LayerGeo }
     * 
     */
    public LayerGeo createLayerGeo() {
        return new LayerGeo();
    }

    /**
     * Create an instance of {@link LayerArea }
     * 
     */
    public LayerArea createLayerArea() {
        return new LayerArea();
    }

    /**
     * Create an instance of {@link Mbr }
     * 
     */
    public Mbr createMbr() {
        return new Mbr();
    }

    /**
     * Create an instance of {@link User }
     * 
     */
    public User createUser() {
        return new User();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LayerInfo }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "layerInfo")
    public JAXBElement<LayerInfo> createLayerInfo(LayerInfo value) {
        return new JAXBElement<LayerInfo>(_LayerInfo_QNAME, LayerInfo.class, null, value);
    }

}
