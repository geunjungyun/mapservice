//
// 이 파일은 JAXB(JavaTM Architecture for XML Binding) 참조 구현 2.2.8-b130911.1802 버전을 통해 생성되었습니다. 
// <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>를 참조하십시오. 
// 이 파일을 수정하면 소스 스키마를 재컴파일할 때 수정 사항이 손실됩니다. 
// 생성 날짜: 2025.12.08 시간 01:50:29 PM KST 
//


package com.gis.protocol;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.gis.protocol package. 
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

    private final static QName _JobTile_QNAME = new QName("", "jobTile");
    private final static QName _FontType_QNAME = new QName("", "fontType");
    private final static QName _FieldName_QNAME = new QName("", "fieldName");
    private final static QName _PositionType_QNAME = new QName("", "positionType");
    private final static QName _Color_QNAME = new QName("", "color");
    private final static QName _StyleName_QNAME = new QName("", "styleName");
    private final static QName _Priority_QNAME = new QName("", "priority");
    private final static QName _MapInfo_QNAME = new QName("", "mapInfo");
    private final static QName _SelectStyleName_QNAME = new QName("", "selectStyleName");
    private final static QName _FontName_QNAME = new QName("", "fontName");
    private final static QName _Size_QNAME = new QName("", "size");
    private final static QName _Transparency_QNAME = new QName("", "transparency");
    private final static QName _LevelId_QNAME = new QName("", "levelId");
    private final static QName _DrawOrder_QNAME = new QName("", "drawOrder");
    private final static QName _Name_QNAME = new QName("", "name");
    private final static QName _Width_QNAME = new QName("", "width");
    private final static QName _SymbolName_QNAME = new QName("", "symbolName");
    private final static QName _FontSize_QNAME = new QName("", "fontSize");
    private final static QName _Styles_QNAME = new QName("", "styles");
    private final static QName _LayerName_QNAME = new QName("", "layerName");
    private final static QName _Value_QNAME = new QName("", "value");
    private final static QName _Desc_QNAME = new QName("", "desc");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.gis.protocol
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link JobTile }
     * 
     */
    public JobTile createJobTile() {
        return new JobTile();
    }

    /**
     * Create an instance of {@link PolylineStyle }
     * 
     */
    public PolylineStyle createPolylineStyle() {
        return new PolylineStyle();
    }

    /**
     * Create an instance of {@link LineStyle }
     * 
     */
    public LineStyle createLineStyle() {
        return new LineStyle();
    }

    /**
     * Create an instance of {@link FillStyle }
     * 
     */
    public FillStyle createFillStyle() {
        return new FillStyle();
    }

    /**
     * Create an instance of {@link HatchPattern }
     * 
     */
    public HatchPattern createHatchPattern() {
        return new HatchPattern();
    }

    /**
     * Create an instance of {@link Query }
     * 
     */
    public Query createQuery() {
        return new Query();
    }

    /**
     * Create an instance of {@link OverlapConfig }
     * 
     */
    public OverlapConfig createOverlapConfig() {
        return new OverlapConfig();
    }

    /**
     * Create an instance of {@link MultiLine }
     * 
     */
    public MultiLine createMultiLine() {
        return new MultiLine();
    }

    /**
     * Create an instance of {@link SymbolStyle }
     * 
     */
    public SymbolStyle createSymbolStyle() {
        return new SymbolStyle();
    }

    /**
     * Create an instance of {@link PointStyle }
     * 
     */
    public PointStyle createPointStyle() {
        return new PointStyle();
    }

    /**
     * Create an instance of {@link MarkStyle }
     * 
     */
    public MarkStyle createMarkStyle() {
        return new MarkStyle();
    }

    /**
     * Create an instance of {@link TextStyle }
     * 
     */
    public TextStyle createTextStyle() {
        return new TextStyle();
    }

    /**
     * Create an instance of {@link LayerInfo }
     * 
     */
    public LayerInfo createLayerInfo() {
        return new LayerInfo();
    }

    /**
     * Create an instance of {@link MapInfo }
     * 
     */
    public MapInfo createMapInfo() {
        return new MapInfo();
    }

    /**
     * Create an instance of {@link PolygonStyle }
     * 
     */
    public PolygonStyle createPolygonStyle() {
        return new PolygonStyle();
    }

    /**
     * Create an instance of {@link Styles }
     * 
     */
    public Styles createStyles() {
        return new Styles();
    }

    /**
     * Create an instance of {@link LevelConfig }
     * 
     */
    public LevelConfig createLevelConfig() {
        return new LevelConfig();
    }

    /**
     * Create an instance of {@link ScaleInfos }
     * 
     */
    public ScaleInfos createScaleInfos() {
        return new ScaleInfos();
    }

    /**
     * Create an instance of {@link LevelConfigs }
     * 
     */
    public LevelConfigs createLevelConfigs() {
        return new LevelConfigs();
    }

    /**
     * Create an instance of {@link Resource }
     * 
     */
    public Resource createResource() {
        return new Resource();
    }

    /**
     * Create an instance of {@link Layer }
     * 
     */
    public Layer createLayer() {
        return new Layer();
    }

    /**
     * Create an instance of {@link LevelSet }
     * 
     */
    public LevelSet createLevelSet() {
        return new LevelSet();
    }

    /**
     * Create an instance of {@link File }
     * 
     */
    public File createFile() {
        return new File();
    }

    /**
     * Create an instance of {@link ScaleInfo }
     * 
     */
    public ScaleInfo createScaleInfo() {
        return new ScaleInfo();
    }

    /**
     * Create an instance of {@link MakeTile }
     * 
     */
    public MakeTile createMakeTile() {
        return new MakeTile();
    }

    /**
     * Create an instance of {@link Distribute }
     * 
     */
    public Distribute createDistribute() {
        return new Distribute();
    }

    /**
     * Create an instance of {@link Mbr }
     * 
     */
    public Mbr createMbr() {
        return new Mbr();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link JobTile }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "jobTile")
    public JAXBElement<JobTile> createJobTile(JobTile value) {
        return new JAXBElement<JobTile>(_JobTile_QNAME, JobTile.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "fontType")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    public JAXBElement<String> createFontType(String value) {
        return new JAXBElement<String>(_FontType_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "fieldName")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    public JAXBElement<String> createFieldName(String value) {
        return new JAXBElement<String>(_FieldName_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "positionType")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    public JAXBElement<String> createPositionType(String value) {
        return new JAXBElement<String>(_PositionType_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "color")
    public JAXBElement<String> createColor(String value) {
        return new JAXBElement<String>(_Color_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "styleName")
    public JAXBElement<String> createStyleName(String value) {
        return new JAXBElement<String>(_StyleName_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "priority")
    public JAXBElement<Integer> createPriority(Integer value) {
        return new JAXBElement<Integer>(_Priority_QNAME, Integer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MapInfo }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "mapInfo")
    public JAXBElement<MapInfo> createMapInfo(MapInfo value) {
        return new JAXBElement<MapInfo>(_MapInfo_QNAME, MapInfo.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "selectStyleName")
    public JAXBElement<String> createSelectStyleName(String value) {
        return new JAXBElement<String>(_SelectStyleName_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "fontName")
    public JAXBElement<String> createFontName(String value) {
        return new JAXBElement<String>(_FontName_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "size")
    public JAXBElement<Integer> createSize(Integer value) {
        return new JAXBElement<Integer>(_Size_QNAME, Integer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "transparency")
    public JAXBElement<Integer> createTransparency(Integer value) {
        return new JAXBElement<Integer>(_Transparency_QNAME, Integer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "levelId")
    public JAXBElement<Integer> createLevelId(Integer value) {
        return new JAXBElement<Integer>(_LevelId_QNAME, Integer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "drawOrder")
    public JAXBElement<Integer> createDrawOrder(Integer value) {
        return new JAXBElement<Integer>(_DrawOrder_QNAME, Integer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "name")
    public JAXBElement<String> createName(String value) {
        return new JAXBElement<String>(_Name_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Float }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "width")
    public JAXBElement<Float> createWidth(Float value) {
        return new JAXBElement<Float>(_Width_QNAME, Float.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "symbolName")
    public JAXBElement<String> createSymbolName(String value) {
        return new JAXBElement<String>(_SymbolName_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Float }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "fontSize")
    public JAXBElement<Float> createFontSize(Float value) {
        return new JAXBElement<Float>(_FontSize_QNAME, Float.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Styles }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "styles")
    public JAXBElement<Styles> createStyles(Styles value) {
        return new JAXBElement<Styles>(_Styles_QNAME, Styles.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "layerName")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    public JAXBElement<String> createLayerName(String value) {
        return new JAXBElement<String>(_LayerName_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "value")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    public JAXBElement<String> createValue(String value) {
        return new JAXBElement<String>(_Value_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "desc")
    public JAXBElement<String> createDesc(String value) {
        return new JAXBElement<String>(_Desc_QNAME, String.class, null, value);
    }

}
