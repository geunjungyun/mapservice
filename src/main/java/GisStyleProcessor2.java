import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

/**
 * GIS 스타일 처리기 (최종)
 *
 * 처리 순서:
 *   1. Pattern A     — 폴리곤·선형 다중 스타일 → 단일 부모(query) 통합 (23개 레이어)
 *   2. Strategy A    — 동일 레벨 다수 포인트 통합 (clrtnode_p / nrdnm_l / clrtroute_l)
 *   3. Strategy C    — 레벨간 동일 시각 포인트 통합 (hwrdcenl_l / cityhwrdcenl_l / snexrd_l)
 *   4. 단순화        — query 1개 스타일: leaf 인라인 병합, name/value→CQL(field 소문자)
 *                      단, mapInfo 직접참조 pointStyle 제외
 *   5. 미연결 삭제   — mapInfo에서 참조 안 되는 스타일 제거
 *   6. mapInfo 정렬  — levelId 오름차순 → drawOrder 오름차순
 *   7. 중복명 통합   — 설정 동일·이름 다른 스타일 4가지 규칙으로 명명
 *   8. 미연결 재삭제 — 중복 통합 후 불필요해진 스타일 제거
 *   9. query 최적화  — ①동일 field+leaf 다중 query → IN절 통합
 *                      ②name/value → CQL(field 소문자)
 *                      ③연속 정수 IN → BETWEEN
 *
 * 사용법:
 *   javac GisStyleProcessor.java
 *   java -Dfile.encoding=UTF-8 GisStyleProcessor [styIn] [mapIn] [styOut] [mapOut]
 */
public class GisStyleProcessor2 {

    // ── Pattern A 대상 (원본 7 + 확장 16 = 23개) ──────────
    private static final Map<String,String> PATTERN_A = new LinkedHashMap<>();
    static {
        // 원본 7개
        PATTERN_A.put("tn_buld",             "건물_용도별");
        PATTERN_A.put("tn_fclty_zone_bndry", "시설구역_종류별");
        PATTERN_A.put("tn_ex_natlpark_a",    "자연공원_구분");
        PATTERN_A.put("tn_rlroad_ctln",      "철도중심선_노선별");
        PATTERN_A.put("tn_ex_subwln_l",      "지하철노선_구분");
        // tn_arrfc: drawOrder 기준을 PATTERN_A_DRAW_REF(교통시설물_육교)로 설정
        PATTERN_A.put("tn_arrfc", "교통시설물_종류별");
        PATTERN_A.put("tn_ex_clrtnode_p",    "등산로노드_종류별");
        // 확장 10개
        // ❌ 제외: tn_rodway_bndry(다중타일), tn_rodway_ctln(다중타일)
        // ❌ 제외: tn_ex_hwrdcenl_l, tn_ex_cityhwrdcenl_l, tn_ex_spa_a, tn_ex_clrtroute_l (사이레이어 존재)
        // ✅ tn_ctrln: elevation 값이 겹치지 않아 priority 순서 무관 → 안전
        PATTERN_A.put("tn_ctrln",             "등고선_종류별");
        // 등산로: 기타/일반/주요 조건이 상호 배타적 → 안전
        PATTERN_A.put("tn_ex_clrtroute_l",    "등산로배경_종류별");
        // ❌ 제외: 동일 피처가 여러 query에 중복 매칭 → GIS서버가 priority 1만 렌더
        //   tn_rodway_ctln  : 배경+자동생성이 동일 road_se CQL 공유 → max drawOrder 방식으로 적용
        PATTERN_A.put("tn_rodway_ctln", "도로중심선_종류별");
        //   tn_rodway_bndry : 외부+내부 모두 조건 없이 전 피처 매칭 → 사용자 요청으로 적용
        PATTERN_A.put("tn_rodway_bndry", "도로경계_종류별");
        //   tn_ex_hwrdcenl_l/tn_ex_cityhwrdcenl_l : 선형+자동생성이 동일 s_level CQL 공유
        //   tn_ex_spa_a     : 외부+내부 모두 조건 없이 전 피처 매칭
        PATTERN_A.put("tn_ex_sdbdry_l",       "시도경계_종류별");
        PATTERN_A.put("tn_ex_sggbdry_l",      "시군구경계_종류별");
        PATTERN_A.put("tn_ex_mainrdcenl_l",   "주요도로중심선_종류별");
        PATTERN_A.put("tn_ex_localrdcenl_l",  "일반도로중심선_종류별");
        PATTERN_A.put("tn_ex_nardcenl_l",     "국도중심선_종류별");
        PATTERN_A.put("tn_ex_snr_a",          "특수도로면_종류별");
        PATTERN_A.put("tn_arpgr",             "보행노면_종류별");
        PATTERN_A.put("tn_lnpgr",             "선형보행_종류별");
        PATTERN_A.put("walk_area",            "보행구역_종류별");
    }

    // Pattern A drawOrder 기준 스타일 (min 대신 해당 스타일의 drawOrder 사용)
    private static final Map<String,String> PATTERN_A_DRAW_REF = new LinkedHashMap<>();
    static {
        // tn_arrfc: 주차장구역(draw 낮음)이 아닌 교통시설물_육교의 drawOrder 기준 사용
        PATTERN_A_DRAW_REF.put("tn_arrfc", "tn_arrfc+교통시설물_육교");
        // tn_rodway_bndry: 외부선(min draw) 대신 내부선의 drawOrder 기준 사용
        //   → 부모가 draw=113~122 위치에서 렌더 (priority=1:외부, priority=2:내부)
        PATTERN_A_DRAW_REF.put("tn_rodway_bndry", "tn_rodway_bndry(내부)+면형일반도로");
    }

    // Pattern A drawOrder 최댓값 기준 레이어 (자동생성 등 나중 스타일 위치 사용)
    private static final Set<String> PATTERN_A_DRAW_MAX = new HashSet<>(Arrays.asList(
        "tn_rodway_ctln"   // 배경(min) 대신 자동생성(max) drawOrder 기준 사용
    ));

    // Pattern A 그룹에서 제외할 스타일 → mapInfo에 원본 drawOrder 그대로 유지
    private static final Map<String,Set<String>> PATTERN_A_EXCLUDE = new LinkedHashMap<>();
    static {
        PATTERN_A_EXCLUDE.put("tn_arrfc", new HashSet<>(Arrays.asList("tn_arrfc+주차장구역")));
    }

    // getLeafQueries 대신 자식을 직접 styleName으로 참조 (동일 조건·leaf 중복 방지)
    private static final Set<String> PATTERN_A_DIRECT_REF = new HashSet<>(Arrays.asList(
        "tn_arpgr"
    ));

    private static final Map<String,String> STRATEGY_A = new LinkedHashMap<>();
    static {
        STRATEGY_A.put("tn_ex_clrtnode_p", "등산로노드_종류별");
        STRATEGY_A.put("tn_ex_nrdnm_l",    "일반도로명_종류별");
        STRATEGY_A.put("tn_ex_clrtroute_l","등산로주기_종류별");
        STRATEGY_A.put("tn_buld",           "tn_buld+건물주기");  // 동번호+건물번호 통합
    }

    private static final Map<String,String> STRATEGY_C = new LinkedHashMap<>();
    static {
        STRATEGY_C.put("tn_ex_hwrdcenl_l",    "고속도로명_레벨별");
        STRATEGY_C.put("tn_ex_cityhwrdcenl_l","도시고속도로명_레벨별");
        STRATEGY_C.put("tn_ex_snexrd_l",      "북한고속도로명_레벨별");
    }

    private static final Map<String,String> COLOR_KR = new LinkedHashMap<>();
    static {
        COLOR_KR.put("202/199/167","황갈"); COLOR_KR.put("199/197/162","연황갈");
        COLOR_KR.put("193/191/153","회황"); COLOR_KR.put("179/177/145","회녹");
        COLOR_KR.put("155/155/125","회");   COLOR_KR.put("137/137/105","암회");
        COLOR_KR.put("207/201/158","연황"); COLOR_KR.put("194/188/148","황녹");
        COLOR_KR.put("174/168/130","회갈"); COLOR_KR.put("157/151/115","진회");
        COLOR_KR.put("210/210/165","연녹"); COLOR_KR.put("185/185/140","녹회");
    }

    private static final String[] STYLE_TAGS = {"polygonStyle","polylineStyle","pointStyle"};
    private static final String[] CONTOUR_SERIES = {
        "남한등고선10m","북한등고선50m","북한등고선100m","남한등고선50m","남한등고선100m",
        "한반도등고","북한등고","남한면형등고","독도등고10m"
    };
    private static final Set<String> VISUAL_TAGS = new HashSet<>(Arrays.asList(
        "fillStyle","lineStyle","textStyle","symbolStyle","markStyle",
        "overlapConfig","positionType","priority","transparency"
    ));

    private static class LayerEntry {
        String fullLayerName, baseLayerName, styleName, styleTag;
        double drawOrder; Element layerInfoElem;
    }
    private static class LeafQuery { String cql, field, value, leafStyle; }
    private static class LvInfo {
        String styleName, full; LeafQuery lq; String vh; Element li; double draw;
    }

    private Document styDoc, mapDoc;
    private final Map<String,Element> styleMap = new LinkedHashMap<>();
    private static PrintStream out;

    // ═══════════════════════════════════════════════════════
    public static void main(String[] args) throws Exception {
        out = new PrintStream(System.out, true, StandardCharsets.UTF_8.name());
        String si=args.length>0?args[0]:"styles.xml", mi=args.length>1?args[1]:"mapInfo.xml";
        String so=args.length>2?args[2]:"styles.xml", mo=args.length>3?args[3]:"mapInfo.xml";
        new GisStyleProcessor2().process(si, mi, so, mo);
    }

    public void process(String si, String mi, String so, String mo) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        styDoc = db.parse(new File(si)); mapDoc = db.parse(new File(mi));
        buildStyleMap();
        out.printf("[로드] styles:%d / layerInfo:%d%n",
                   styleMap.size(), mapDoc.getElementsByTagName("layerInfo").getLength());

        int[] pa = applyPatternA();
        out.printf("[Pattern A] 부모 %d개 / layerInfo교체 %d개%n", pa[0], pa[1]);

        buildStyleMap();
        int sa = applyStrategyA();
        out.printf("[Strategy A] 부모 %d개%n", sa);

        buildStyleMap();
        int sc = applyStrategyC();
        out.printf("[Strategy C] 부모 %d개%n", sc);

        buildStyleMap();
        Set<String> mapRefPoints = getMapInfoPointRefs();
        int simp = simplifyOneQueryStyles(mapRefPoints);
        out.printf("[단순화] %d개 (mapInfo직참 pointStyle 포함 전체 적용)%n", simp);

        buildStyleMap();
        int rm1 = removeUnreferenced();
        out.printf("[삭제1] %d개%n", rm1);

        sortMapInfo();
        out.printf("[정렬] 완료%n");

        buildStyleMap();
        int[] nr = applyNamingRules();
        out.printf("[명명규칙] 규칙1=%d 규칙2=%d 규칙3=%d 규칙4=%d(보류) 절감=%d%n",
                   nr[0],nr[1],nr[2],nr[3],nr[4]);

        buildStyleMap();
        int rm2 = removeUnreferenced();
        out.printf("[삭제2] %d개%n", rm2);

        buildStyleMap();
        int[] qr = optimizeQueries();
        out.printf("[query최적화] IN통합=%d절감 CQL변환=%d BETWEEN변환=%d%n",
                   qr[0], qr[1], qr[2]);

        // Step 9 이후 leaf가 순수 시각 스타일로 바뀐 것들을 2차 단순화
        buildStyleMap();
        int simp2 = simplifyOneQueryStyles(Collections.emptySet());
        if(simp2>0){
            buildStyleMap();
            int rm3=removeUnreferenced();
            out.printf("[단순화2] %d개 / 삭제 %d개%n", simp2, rm3);
        }

        buildStyleMap();
        out.printf("[완료] 스타일:%d / layerInfo:%d%n",
                   styleMap.size(), mapDoc.getElementsByTagName("layerInfo").getLength());
        saveXml(styDoc, so); saveXml(mapDoc, mo);
    }

    // ═══════════════════════════════════════════════════════
    // 개별 스타일 속성 오버라이드
    private static final java.util.Map<String,java.util.Map<String,java.util.Map<String,String>>> STYLE_OVERRIDE_MAP = new LinkedHashMap<>();
    static {
        java.util.Map<String,String> ls=new LinkedHashMap<>();
        ls.put("width","1.0"); ls.put("color","241/233/230");
        java.util.Map<String,java.util.Map<String,String>> ov=new LinkedHashMap<>();
        ov.put("lineStyle",ls);
        STYLE_OVERRIDE_MAP.put("tn_arpgr(\uB0B4\uBD80)+\uBCF4\uD589\uB178\uBA74",ov);
    }
    private void applyStyleOverride(Document doc, Element e, String name) {
        java.util.Map<String,java.util.Map<String,String>> ov=STYLE_OVERRIDE_MAP.get(name); if(ov==null) return;
        for(java.util.Map.Entry<String,java.util.Map<String,String>> te:ov.entrySet()) {
            Element t=getFirstChild(e,te.getKey()); if(t==null) continue;
            for(java.util.Map.Entry<String,String> pe:te.getValue().entrySet()) {
                Element p=getFirstChild(t,pe.getKey()); if(p!=null) p.setTextContent(pe.getValue());
            }
        }
    }

    // STEP 1: Pattern A (polygon/polyline 다중 → 부모 통합)
    // drawOrder 오름차순 = priority 1,2,3 순서 보장
    // ═══════════════════════════════════════════════════════
    private int[] applyPatternA() {
        // ssLevels : base → ssKey → [levels]
        Map<String,Map<String,List<String>>> ssLevels = new LinkedHashMap<>();
        // styleOrder : base → ssKey → drawOrder 정렬된 스타일 리스트
        Map<String,Map<String,List<String>>> styleOrder = new LinkedHashMap<>();

        NodeList lcs = mapDoc.getElementsByTagName("levelConfig");
        for (int i=0;i<lcs.getLength();i++) {
            Element lc=(Element)lcs.item(i); String level=getChildText(lc,"levelId");
            Map<String,List<LayerEntry>> bkt=new LinkedHashMap<>();
            for (Element li:getDirectChildren(lc,"layerInfo")) {
                LayerEntry e=makeEntry(li);
                if(e==null||!PATTERN_A.containsKey(e.baseLayerName)) continue;
                Set<String> excl1=PATTERN_A_EXCLUDE.getOrDefault(e.baseLayerName,Collections.emptySet());
                if(excl1.contains(e.styleName)) continue;
                bkt.computeIfAbsent(e.baseLayerName,k->new ArrayList<>()).add(e);
            }
            for (Map.Entry<String,List<LayerEntry>> me:bkt.entrySet()) {
                if(me.getValue().size()<=1) continue;
                // drawOrder 오름차순 정렬
                List<LayerEntry> sorted = new ArrayList<>(me.getValue());
                sorted.sort(Comparator.comparingDouble(e->e.drawOrder));
                // 중복 없는 ordered 스타일 리스트
                List<String> orderedList = new ArrayList<>();
                Set<String> seen = new LinkedHashSet<>();
                for(LayerEntry e:sorted) if(seen.add(e.styleName)) orderedList.add(e.styleName);
                // 정렬 무관 일관 key (TreeSet)
                String key = String.join("|", new TreeSet<>(orderedList));
                ssLevels.computeIfAbsent(me.getKey(),k->new LinkedHashMap<>())
                        .computeIfAbsent(key,k->new ArrayList<>()).add(level);
                styleOrder.computeIfAbsent(me.getKey(),k->new LinkedHashMap<>())
                          .putIfAbsent(key, orderedList);
            }
        }

        Set<String> created=new HashSet<>(); int createdCnt=0,reducedCnt=0;
        for (int i=0;i<lcs.getLength();i++) {
            Element lc=(Element)lcs.item(i);
            // 타일별 그룹핑 (레이어명 포함)
            Map<String,List<LayerEntry>> bkt=new LinkedHashMap<>();
            for (Element li:getDirectChildren(lc,"layerInfo")) {
                LayerEntry e=makeEntry(li);
                if(e==null||!PATTERN_A.containsKey(e.baseLayerName)) continue;
                Set<String> excl2=PATTERN_A_EXCLUDE.getOrDefault(e.baseLayerName,Collections.emptySet());
                if(excl2.contains(e.styleName)) continue;
                bkt.computeIfAbsent(layerName(e),k->new ArrayList<>()).add(e);
            }
            for (Map.Entry<String,List<LayerEntry>> me:bkt.entrySet()) {
                List<LayerEntry> entries=me.getValue(); if(entries.size()<=1) continue;
                String base=entries.get(0).baseLayerName;
                String key=String.join("|",new TreeSet<>(
                    entries.stream().map(e->e.styleName).collect(Collectors.toSet())));
                List<String> levels=ssLevels.getOrDefault(base,Collections.emptyMap())
                                            .getOrDefault(key,Collections.emptyList());
                if(levels.isEmpty()) continue;
                // drawOrder 순 정렬된 스타일 리스트 (priority 순서 결정)
                List<String> ordered=styleOrder.getOrDefault(base,Collections.emptyMap())
                                               .getOrDefault(key,
                                                   new ArrayList<>(new TreeSet<>(
                                                       entries.stream().map(e->e.styleName).collect(Collectors.toList()))));
                String lsuf=levelSuffix(levels);
                Map<String,List<LayerEntry>> typeMap=new LinkedHashMap<>();
                for(LayerEntry e:entries) if(e.styleTag!=null)
                    typeMap.computeIfAbsent(e.styleTag,k->new ArrayList<>()).add(e);
                List<String> nonPt=new ArrayList<>();
                for(String t:typeMap.keySet()) if(!t.equals("pointStyle")) nonPt.add(t);
                if(nonPt.isEmpty()) continue;
                for(String tag:nonPt) {
                    List<LayerEntry> te=typeMap.get(tag);
                    String tsuf=nonPt.size()==1?"":(tag.equals("polygonStyle")?" 면형":" 선형");
                    String pname=PATTERN_A.get(base)+tsuf+lsuf;
                    // drawOrder 순서 반영된 리스트로 부모 생성 → priority 보장
                    boolean dRef=PATTERN_A_DIRECT_REF.contains(base);
                    if(!created.contains(pname)){createElement(tag,pname,ordered,dRef); created.add(pname); createdCnt++;}
                    for(LayerEntry e:te){lc.removeChild(e.layerInfoElem); reducedCnt++;}
                    // drawOrder 결정: DRAW_MAX → max, DRAW_REF → 기준 스타일, 기본 → min
                    String refSty=PATTERN_A_DRAW_REF.get(base);
                    double md;
                    if(PATTERN_A_DRAW_MAX.contains(base)) {
                        md=te.stream().mapToDouble(e->e.drawOrder).max().orElse(0);
                    } else if(refSty!=null) {
                        java.util.OptionalDouble refDraw=te.stream()
                            .filter(e->e.styleName.equals(refSty))
                            .mapToDouble(e->e.drawOrder).findFirst();
                        md=refDraw.isPresent()?refDraw.getAsDouble()
                          :te.stream().mapToDouble(e->e.drawOrder).min().orElse(0);
                    } else {
                        md=te.stream().mapToDouble(e->e.drawOrder).min().orElse(0);
                    }
                    addLayerInfo(lc,entries.get(0).fullLayerName,pname,(int)md+".0");
                }
            }
        }
        return new int[]{createdCnt,reducedCnt};
    }

    // ═══════════════════════════════════════════════════════
    // STEP 2: Strategy A (동일 레벨 포인트 통합)
    // ═══════════════════════════════════════════════════════
    private int applyStrategyA() {
        int created=0;
        NodeList lcs=mapDoc.getElementsByTagName("levelConfig");
        for (Map.Entry<String,String> entry:STRATEGY_A.entrySet()) {
            String base=entry.getKey(), newBase=entry.getValue();
            Map<String,Map<String,List<LayerEntry>>> lvPt=new LinkedHashMap<>();
            for(int i=0;i<lcs.getLength();i++) {
                Element lc=(Element)lcs.item(i); String level=getChildText(lc,"levelId");
                for(Element li:getDirectChildren(lc,"layerInfo")) {
                    LayerEntry e=makeEntry(li);
                    if(e==null||!e.baseLayerName.equals(base)||!"pointStyle".equals(e.styleTag)) continue;
                    lvPt.computeIfAbsent(level,k->new LinkedHashMap<>())
                        .computeIfAbsent(layerName(e),k->new ArrayList<>()).add(e);
                }
            }
            // ssKey(정렬무관) → 레벨 목록 + drawOrder 정렬된 스타일 목록
            Map<String,List<String>> ssLevels2=new LinkedHashMap<>();
            Map<String,List<String>> ssOrderedStyles=new LinkedHashMap<>();
            for(Map.Entry<String,Map<String,List<LayerEntry>>> le:lvPt.entrySet())
                for(Map.Entry<String,List<LayerEntry>> le2:le.getValue().entrySet()) {
                    List<LayerEntry> sorted=new ArrayList<>(le2.getValue());
                    sorted.sort(Comparator.comparingDouble(e->e.drawOrder));
                    Set<String> ss=new TreeSet<>(); for(LayerEntry e:sorted) ss.add(e.styleName);
                    if(ss.size()>1) {
                        String key=String.join("|",ss);
                        ssLevels2.computeIfAbsent(key,k->new ArrayList<>()).add(le.getKey());
                        if(!ssOrderedStyles.containsKey(key)){
                            List<String> ord=new ArrayList<>();
                            Set<String> seen2=new LinkedHashSet<>();
                            for(LayerEntry e:sorted) if(seen2.add(e.styleName)) ord.add(e.styleName);
                            ssOrderedStyles.put(key,ord);
                        }
                    }
                }
            Map<String,String> ssParent=new LinkedHashMap<>();
            for(Map.Entry<String,List<String>> se:ssLevels2.entrySet()) {
                String key=se.getKey();
                String lsuf=levelSuffix(se.getValue()), pname=newBase+lsuf;
                if(!ssParent.containsKey(pname)){
                    // drawOrder 정렬된 리스트로 부모 생성 → priority 역순 올바르게 배정
                    List<String> ordered=ssOrderedStyles.getOrDefault(key,new ArrayList<>());
                    createElement("pointStyle",pname,ordered);
                    ssParent.put(pname,pname); created++;
                }
            }
            for(int i=0;i<lcs.getLength();i++) {
                Element lc=(Element)lcs.item(i); String level=getChildText(lc,"levelId");
                if(!lvPt.containsKey(level)) continue;
                for(Map.Entry<String,List<LayerEntry>> le:lvPt.get(level).entrySet()) {
                    List<LayerEntry> entries=le.getValue(); if(entries.size()<=1) continue;
                    Set<String> ss=new TreeSet<>(); for(LayerEntry e:entries) ss.add(e.styleName);
                    String key=String.join("|",ss);
                    String lsuf=levelSuffix(ssLevels2.get(key)), pname=newBase+lsuf;
                    double md=entries.stream().mapToDouble(e->e.drawOrder).min().orElse(0);
                    for(LayerEntry e:entries) lc.removeChild(e.layerInfoElem);
                    addLayerInfo(lc,entries.get(0).fullLayerName,pname,(int)md+".0");
                }
            }
        }
        return created;
    }

    // ═══════════════════════════════════════════════════════
    // STEP 3: Strategy C (레벨간 동일 시각 포인트 통합)
    // ═══════════════════════════════════════════════════════
    private int applyStrategyC() {
        int created=0;
        NodeList lcs=mapDoc.getElementsByTagName("levelConfig");
        for(Map.Entry<String,String> entry:STRATEGY_C.entrySet()) {
            String base=entry.getKey(), newBase=entry.getValue();
            Map<String,LvInfo> lvInfo=new LinkedHashMap<>();
            for(int i=0;i<lcs.getLength();i++) {
                Element lc=(Element)lcs.item(i); String level=getChildText(lc,"levelId");
                for(Element li:getDirectChildren(lc,"layerInfo")) {
                    LayerEntry e=makeEntry(li);
                    if(e==null||!e.baseLayerName.equals(base)||!"pointStyle".equals(e.styleTag)) continue;
                    List<LeafQuery> lqs=getLeafQueries(e.styleName); if(lqs.isEmpty()) continue;
                    LeafQuery lq=lqs.get(0);
                    Element leafElem=styleMap.get(lq.leafStyle); if(leafElem==null) continue;
                    LvInfo li2=new LvInfo(); li2.styleName=e.styleName; li2.lq=lq;
                    li2.vh=elemToSettings(leafElem); li2.li=li; li2.draw=e.drawOrder; li2.full=e.fullLayerName;
                    lvInfo.put(level,li2);
                }
            }
            Map<String,List<Map.Entry<String,LvInfo>>> vhGrps=new LinkedHashMap<>();
            for(Map.Entry<String,LvInfo> me:lvInfo.entrySet())
                vhGrps.computeIfAbsent(me.getValue().vh,k->new ArrayList<>()).add(me);
            Map<String,String> levelParent=new LinkedHashMap<>();
            for(Map.Entry<String,List<Map.Entry<String,LvInfo>>> vg:vhGrps.entrySet()) {
                List<Map.Entry<String,LvInfo>> lvis=vg.getValue();
                List<String> lvs=new ArrayList<>(); for(Map.Entry<String,LvInfo> x:lvis) lvs.add(x.getKey());
                String pname=newBase+levelSuffix(lvs);
                List<String[]> cqlLeafs=new ArrayList<>(); Set<String> seenCql=new HashSet<>();
                for(Map.Entry<String,LvInfo> x:lvis) {
                    LvInfo li2=x.getValue();
                    String ct=li2.lq.cql!=null?li2.lq.cql:li2.lq.field+"="+li2.lq.value;
                    if(seenCql.add(ct)) cqlLeafs.add(new String[]{ct,li2.lq.leafStyle});
                }
                createParentCql("pointStyle",pname,cqlLeafs,lvis.get(0).getValue().styleName);
                for(Map.Entry<String,LvInfo> x:lvis) levelParent.put(x.getKey(),pname);
                created++;
            }
            for(int i=0;i<lcs.getLength();i++) {
                Element lc=(Element)lcs.item(i); String level=getChildText(lc,"levelId");
                if(!levelParent.containsKey(level)) continue;
                LvInfo li2=lvInfo.get(level); if(li2==null) continue;
                lc.removeChild(li2.li);
                addLayerInfo(lc,li2.full,levelParent.get(level),(int)li2.draw+".0");
            }
        }
        return created;
    }

    // ═══════════════════════════════════════════════════════
    // STEP 4: query 1개 단순화 (mapInfo직참 pointStyle 제외)
    // ═══════════════════════════════════════════════════════
    private Set<String> getMapInfoPointRefs() {
        Set<String> refs=new HashSet<>();
        NodeList sl=mapDoc.getElementsByTagName("selectStyleName");
        for(int i=0;i<sl.getLength();i++) {
            String n=sl.item(i).getTextContent().trim();
            Element e=styleMap.get(n);
            if(e!=null&&"pointStyle".equals(e.getTagName())) refs.add(n);
        }
        return refs;
    }

    private int simplifyOneQueryStyles(Set<String> excludePoints) {
        // excludePoints 매개변수 유지(하위 호환)하되 실제 제외는 하지 않음
        // GIS 서버가 styleName 없는 query의 CQL도 필터로 정상 인식함을 확인
        int cnt=0;
        List<Element> all=new ArrayList<>();
        for(String t:STYLE_TAGS) all.addAll(getDirectChildElements(styDoc.getDocumentElement(),t));
        for(Element elem:all) {
            List<Element> qs=getDirectChildren(elem,"query"); if(qs.size()!=1) continue;
            Element q=qs.get(0); Element sn=getFirstChild(q,"styleName"); if(sn==null) continue;
            Element leaf=styleMap.get(sn.getTextContent().trim()); if(leaf==null) continue;
            // leaf가 또 query를 가지면 체인 깊이 > 1 → 건너뜀
            if(!getDirectChildren(leaf,"query").isEmpty()) continue;

            // ① 기존 시각 요소 제거 (priority 포함)
            for(Node c=elem.getFirstChild();c!=null;){
                Node nx=c.getNextSibling();
                if(c.getNodeType()==Node.ELEMENT_NODE&&VISUAL_TAGS.contains(c.getNodeName())) elem.removeChild(c);
                c=nx;
            }
            // 최상위 priority 제거 (query 1개라 불필요)
            Element topPri=getFirstChild(elem,"priority");
            if(topPri!=null) elem.removeChild(topPri);

            // ② leaf 시각 요소 복사 (name, query, priority 제외)
            for(Node c=leaf.getFirstChild();c!=null;c=c.getNextSibling()) {
                if(c.getNodeType()!=Node.ELEMENT_NODE) continue;
                String cn=c.getNodeName();
                if(cn.equals("name")||cn.equals("query")||cn.equals("priority")) continue;
                elem.insertBefore(styDoc.importNode(c,true),q);
            }

            // ③ name/value → CQL 변환
            Element cqlEl=getFirstChild(q,"cql"),fldEl=getFirstChild(q,"name"),valEl=getFirstChild(q,"value");
            if(cqlEl==null&&fldEl!=null) {
                String ct=toCql(fldEl.getTextContent().trim(),valEl!=null?valEl.getTextContent().trim():"");
                q.removeChild(fldEl); if(valEl!=null) q.removeChild(valEl);
                Element nc=styDoc.createElement("cql"); nc.setTextContent(ct); q.insertBefore(nc,q.getFirstChild());
            }

            // ④ styleName 제거
            q.removeChild(sn);

            // ⑤ query 내 priority 제거 (query 1개라 불필요)
            Element qPri=getFirstChild(q,"priority");
            if(qPri!=null) q.removeChild(qPri);

            cnt++;
        }
        return cnt;
    }

    // ═══════════════════════════════════════════════════════
    // STEP 5/8: 미연결 스타일 삭제
    // ═══════════════════════════════════════════════════════
    private int removeUnreferenced() {
        Set<String> reachable=new LinkedHashSet<>();
        NodeList sl=mapDoc.getElementsByTagName("selectStyleName");
        for(int i=0;i<sl.getLength();i++) collectReachable(sl.item(i).getTextContent().trim(),reachable);
        int cnt=0; Element root=styDoc.getDocumentElement();
        for(String t:STYLE_TAGS)
            for(Element e:getDirectChildElements(root,t))
                if(!reachable.contains(getChildText(e,"name"))){root.removeChild(e);cnt++;}
        return cnt;
    }
    private void collectReachable(String name,Set<String> r) {
        if(name==null||r.contains(name)||!styleMap.containsKey(name)) return;
        r.add(name);
        for(Element q:getDirectChildren(styleMap.get(name),"query")) {
            Element sn=getFirstChild(q,"styleName"); if(sn!=null) collectReachable(sn.getTextContent().trim(),r);
        }
    }

    // ═══════════════════════════════════════════════════════
    // STEP 6: mapInfo 정렬 (levelId 오름차순 → drawOrder 오름차순)
    // ═══════════════════════════════════════════════════════
    private void sortMapInfo() {
        NodeList nl=mapDoc.getElementsByTagName("levelConfigs");
        if(nl.getLength()==0) return;
        Element lcsParent=(Element)nl.item(0);
        List<Element> lcs=getDirectChildren(lcsParent,"levelConfig");
        for(Element lc:lcs) lcsParent.removeChild(lc);
        lcs.sort(Comparator.comparingInt(e->Integer.parseInt(getChildText(e,"levelId"))));
        for(Element lc:lcs) {
            List<Element> lis=getDirectChildren(lc,"layerInfo");
            for(Element li:lis) lc.removeChild(li);
            lis.sort(Comparator.comparingDouble(e->Double.parseDouble(getChildText(e,"drawOrder"))));
            for(Element li:lis) lc.appendChild(li);
            lcsParent.appendChild(lc);
        }
    }

    // ═══════════════════════════════════════════════════════
    // STEP 7: 중복명 통합 (4가지 규칙, 이름 충돌 감지)
    // ═══════════════════════════════════════════════════════
    private int[] applyNamingRules() {
        Map<String,List<String>> settingsMap=new LinkedHashMap<>();
        for(String t:STYLE_TAGS)
            for(Element e:getDirectChildElements(styDoc.getDocumentElement(),t)) {
                String n=getChildText(e,"name"), s=elemToSettings(e);
                settingsMap.computeIfAbsent(s,k->new ArrayList<>()).add(n);
            }
        Map<String,String> renameMap=new HashMap<>(); Set<String> deleteSet=new HashSet<>();
        int r1=0,r2=0,r3=0,r4=0,saved=0;
        Map<String,Integer> colorSeriesCnt=new HashMap<>();
        Set<String> usedNewNames=new HashSet<>();

        for(Map.Entry<String,List<String>> me:settingsMap.entrySet()) {
            List<String> names=me.getValue(); if(names.size()<=1) continue;
            String settings=me.getKey(), newName=null; int rule=0;
            if(isMeinstyleGroup(names)){
                String prefix=names.get(0).replaceAll("\\+메인스타일\\+L\\d+$","");
                newName=prefix+"+메인스타일"; rule=1;
            } else if(isContourGroup(names)){
                String color=extractColor(settings);
                String colorKr=COLOR_KR.getOrDefault(color,"색"+color.replace("/","_"));
                String series=getContourSeries(names), ck=series+"_"+colorKr;
                colorSeriesCnt.merge(ck,1,Integer::sum); int idx=colorSeriesCnt.get(ck);
                newName=series+"_"+colorKr+(idx>1?"_"+idx:""); rule=3;
            } else if(isLevelRangeGroup(names)){
                newName=buildMergedName(names); rule=2;
            } else { r4++; continue; }

            if(newName==null){r4++;continue;}
            if(usedNewNames.contains(newName)){r4++;continue;}  // 충돌 → 보류
            usedNewNames.add(newName);
            renameMap.put(names.get(0),newName);
            for(int i=1;i<names.size();i++){renameMap.put(names.get(i),newName); deleteSet.add(names.get(i));}
            saved+=names.size()-1;
            if(rule==1)r1++;else if(rule==2)r2++;else r3++;
        }
        Element root=styDoc.getDocumentElement();
        for(String t:STYLE_TAGS){
            List<Element> toRm=new ArrayList<>();
            for(Element e:getDirectChildElements(root,t)){
                String old=getChildText(e,"name");
                if(deleteSet.contains(old)) toRm.add(e);
                else if(renameMap.containsKey(old)) setChildText(e,"name",renameMap.get(old));
                for(Element q:getDirectChildren(e,"query")){
                    Element sn=getFirstChild(q,"styleName");
                    if(sn!=null&&renameMap.containsKey(sn.getTextContent().trim()))
                        sn.setTextContent(renameMap.get(sn.getTextContent().trim()));
                }
            }
            for(Element e:toRm) root.removeChild(e);
        }
        NodeList sl=mapDoc.getElementsByTagName("selectStyleName");
        for(int i=0;i<sl.getLength();i++){
            String t=sl.item(i).getTextContent().trim();
            if(renameMap.containsKey(t)) sl.item(i).setTextContent(renameMap.get(t));
        }
        return new int[]{r1,r2,r3,r4,saved};
    }

    // ── 명명규칙 헬퍼 ──────────────────────────────────────
    private static boolean isMeinstyleGroup(List<String> names) {
        Set<String> pfx=new HashSet<>();
        for(String n:names){if(!n.matches(".*\\+메인스타일\\+L\\d+$"))return false;
            pfx.add(n.replaceAll("\\+메인스타일\\+L\\d+$",""));}
        return pfx.size()==1;
    }
    private static boolean isContourGroup(List<String> names) {
        for(String n:names){boolean ok=false; for(String s:CONTOUR_SERIES)if(n.contains(s)){ok=true;break;}
            if(!ok||(!n.contains("+ctrln_hg+")&&!n.contains("+con_value+")))return false;} return true;
    }
    private static String getContourSeries(List<String> names) {
        for(String s:CONTOUR_SERIES)if(names.stream().allMatch(n->n.contains(s)))return s; return "등고선";
    }
    private static String extractColor(String s) {
        Matcher m=Pattern.compile("\"color\":\"([^\"]+)\"").matcher(s); return m.find()?m.group(1):"";
    }
    private static boolean isLevelRangeGroup(List<String> names) {
        Set<String> bases=new HashSet<>();
        for(String n:names) bases.add(n.replaceAll("\\d+_\\d+레벨|\\d+레벨","LVL"));
        if(bases.size()==1&&bases.iterator().next().contains("LVL"))return true;
        for(String p:new String[]{"지하철노선","지하철역명","지하철노선명","건물_용도별","지하철노선_구분"})
            if(names.stream().allMatch(n->n.contains(p)))return true;
        return false;
    }
    private static String buildMergedName(List<String> names) {
        int minL=Integer.MAX_VALUE,maxL=Integer.MIN_VALUE;
        Pattern p1=Pattern.compile("(\\d+)_(\\d+)레벨"),p2=Pattern.compile("(\\d+)레벨");
        for(String n:names){Matcher m=p1.matcher(n);
            if(m.find()){minL=Math.min(minL,Integer.parseInt(m.group(1)));maxL=Math.max(maxL,Integer.parseInt(m.group(2)));}
            else{Matcher m2=p2.matcher(n);if(m2.find()){int v=Integer.parseInt(m2.group(1));minL=Math.min(minL,v);maxL=Math.max(maxL,v);}}}
        if(minL==Integer.MAX_VALUE)return names.get(0);
        String ref=names.get(0); Matcher ml=Pattern.compile("(\\d+_\\d+레벨|\\d+레벨)").matcher(ref);
        if(!ml.find())return ref;
        String newLv;
        if(minL==maxL)newLv=minL+"레벨";
        else if(ref.contains("~"))newLv=minL+"~"+maxL+"레벨";
        else if(ml.group(1).contains("_"))newLv=minL+"_"+maxL+"레벨";
        else if(ref.contains(" ")&&ref.contains("레벨"))newLv=minL+"~"+maxL+"레벨";
        else newLv=minL+"_"+maxL+"레벨";
        return ref.substring(0,ml.start())+newLv+ref.substring(ml.end());
    }

    // ── 설정값 직렬화 (중복 판별용) ───────────────────────
    private static String elemToSettings(Element elem) {
        StringBuilder sb=new StringBuilder(); serializeElem(elem,sb); return sb.toString();
    }
    private static void serializeElem(Element elem,StringBuilder sb) {
        sb.append('{');
        Map<String,List<String>> cm=new TreeMap<>();
        for(Node c=elem.getFirstChild();c!=null;c=c.getNextSibling()) {
            if(c.getNodeType()!=Node.ELEMENT_NODE||c.getNodeName().equals("name")) continue;
            Element ce=(Element)c; StringBuilder val=new StringBuilder();
            boolean leaf=true;
            for(Node cc=ce.getFirstChild();cc!=null;cc=cc.getNextSibling())if(cc.getNodeType()==Node.ELEMENT_NODE){leaf=false;break;}
            if(leaf)val.append('"').append(ce.getTextContent().trim()).append('"');
            else serializeElem(ce,val);
            cm.computeIfAbsent(ce.getNodeName(),k->new ArrayList<>()).add(val.toString());
        }
        boolean first=true;
        for(Map.Entry<String,List<String>> me:cm.entrySet()){
            if(!first)sb.append(','); first=false;
            sb.append('"').append(me.getKey()).append("\":");
            if(me.getValue().size()==1)sb.append(me.getValue().get(0));
            else sb.append('[').append(String.join(",",me.getValue())).append(']');
        }
        sb.append('}');
    }

    // ═══════════════════════════════════════════════════════
    // STEP 9: query 최적화 (전략2→1→3)
    // ═══════════════════════════════════════════════════════
    private int[] optimizeQueries() {
        int inSaved=0, cqlConverted=0, betweenConverted=0;
        // 전략 2: 동일 field+leaf → IN절
        for(String tag:STYLE_TAGS) {
            for(Element elem:getDirectChildElements(styDoc.getDocumentElement(),tag)) {
                List<Element> qs=getDirectChildren(elem,"query"); if(qs.size()<2) continue;
                Map<String,List<Object[]>> groups=new LinkedHashMap<>();
                for(Element q:qs) {
                    Element fld=getFirstChild(q,"name"),val=getFirstChild(q,"value");
                    Element sn=getFirstChild(q,"styleName"),cql=getFirstChild(q,"cql");
                    if(fld!=null&&val!=null&&sn!=null&&cql==null) {
                        String key=fld.getTextContent().trim()+"\u0000"+sn.getTextContent().trim();
                        groups.computeIfAbsent(key,k->new ArrayList<>())
                              .add(new Object[]{val.getTextContent().trim(),q});
                    }
                }
                boolean merged=false;
                for(Map.Entry<String,List<Object[]>> e:groups.entrySet()) {
                    List<Object[]> items=e.getValue(); if(items.size()<2) continue;
                    String[] parts=e.getKey().split("\u0000",2);
                    String field=parts[0].toLowerCase(), leaf=parts[1];
                    List<String> vals=new ArrayList<>();
                    for(Object[] it:items){vals.add((String)it[0]); elem.removeChild((Element)it[1]);}
                    boolean allInt=vals.stream().allMatch(v->{try{Integer.parseInt(v);return true;}catch(Exception ex){return false;}});
                    String ct;
                    if(allInt){ct=field+" IN ("+String.join(", ",vals)+")";}
                    else{StringBuilder sb=new StringBuilder(field+" IN (");
                        for(int i=0;i<vals.size();i++){if(i>0)sb.append(", ");sb.append("'").append(vals.get(i)).append("'");}
                        ct=sb.append(")").toString();}
                    Element nq=styDoc.createElement("query");
                    addText(styDoc,nq,"cql",ct); addText(styDoc,nq,"styleName",leaf); elem.appendChild(nq);
                    inSaved+=items.size()-1; merged=true;
                }
                if(merged){List<Element> allQs=getDirectChildren(elem,"query");
                    for(int i=0;i<allQs.size();i++){Element p=getFirstChild(allQs.get(i),"priority");
                        if(p!=null)p.setTextContent(String.valueOf(i+1));
                        else addText(styDoc,allQs.get(i),"priority",String.valueOf(i+1));}}
            }
        }
        // 전략 1: name/value → CQL
        for(String tag:STYLE_TAGS)
            for(Element elem:getDirectChildElements(styDoc.getDocumentElement(),tag))
                for(Element q:getDirectChildren(elem,"query")) {
                    Element fld=getFirstChild(q,"name"),val=getFirstChild(q,"value"),cql=getFirstChild(q,"cql");
                    if(fld==null||cql!=null) continue;
                    String ct=toCql(fld.getTextContent().trim(),val!=null?val.getTextContent().trim():"");
                    q.removeChild(fld); if(val!=null)q.removeChild(val);
                    Element nc=styDoc.createElement("cql"); nc.setTextContent(ct);
                    q.insertBefore(nc,q.getFirstChild()); cqlConverted++;
                }
        // 전략 3: 연속 정수 IN → BETWEEN
        Pattern inPat=Pattern.compile("^(\\w+)\\s+IN\\s*\\((\\d+(?:,\\s*\\d+)+)\\)$",Pattern.CASE_INSENSITIVE);
        for(String tag:STYLE_TAGS)
            for(Element elem:getDirectChildElements(styDoc.getDocumentElement(),tag))
                for(Element q:getDirectChildren(elem,"query")) {
                    Element cqlEl=getFirstChild(q,"cql"); if(cqlEl==null) continue;
                    Matcher m=inPat.matcher(cqlEl.getTextContent().trim()); if(!m.find()) continue;
                    List<Integer> nums=new ArrayList<>(); boolean allInt=true;
                    for(String v:m.group(2).split(",")){try{nums.add(Integer.parseInt(v.trim()));}catch(NumberFormatException ex){allInt=false;break;}}
                    if(!allInt||nums.size()<3) continue;
                    Collections.sort(nums); boolean consec=true;
                    for(int i=1;i<nums.size();i++)if(nums.get(i)-nums.get(i-1)!=1){consec=false;break;}
                    if(consec){cqlEl.setTextContent(m.group(1)+" BETWEEN "+nums.get(0)+" AND "+nums.get(nums.size()-1)); betweenConverted++;}
                }
        return new int[]{inSaved,cqlConverted,betweenConverted};
    }

    // ── 부모 스타일 생성 ────────────────────────────────────
    // childStyles: drawOrder 오름차순 정렬 리스트 → priority 1,2,3... 순서 보장
    private void createElement(String tag, String pname, List<String> childStyles) {
        createElement(tag, pname, childStyles, false);
    }
    private void createElement(String tag, String pname, List<String> childStyles, boolean directRef) {
        Element parent=styDoc.createElement(tag);
        addText(styDoc,parent,"name",pname);
        if(tag.equals("pointStyle")) addText(styDoc,parent,"priority","0");
        boolean isPointParent=tag.equals("pointStyle");
        // 부모는 순수 dispatcher: 시각 요소(lineStyle, fillStyle, textStyle 등) 복사 안 함
        // 모든 시각 설정은 styleName이 가리키는 leaf 스타일에 존재
        // directRef: 자식 CQL → 부모 query, leaf 시각 → 자식 인라인, 자식 query 제거
        if(directRef) {
            for(int i=0;i<childStyles.size();i++) {
                String cn=childStyles.get(i);
                if(!isPointParent&&getStyleTag(cn)!=null&&getStyleTag(cn).equals("pointStyle")) continue;
                Element q=styDoc.createElement("query");
                Element childElem=styleMap.get(cn);
                if(childElem!=null) {
                    List<Element> childQs=getDirectChildren(childElem,"query");
                    if(!childQs.isEmpty()) {
                        Element cq=childQs.get(0);
                        // ① 부모 query에 CQL 추가
                        Element cqlEl=getFirstChild(cq,"cql");
                        Element fldEl=getFirstChild(cq,"name");
                        Element valEl=getFirstChild(cq,"value");
                        if(cqlEl!=null) {
                            addText(styDoc,q,"cql",cqlEl.getTextContent().trim());
                        } else if(fldEl!=null) {
                            addText(styDoc,q,"cql",toCql(fldEl.getTextContent().trim(),
                                valEl!=null?valEl.getTextContent().trim():""));
                        }
                        // ② leaf 시각 요소를 자식에 인라인 (simplification과 동일)
                        Element snEl=getFirstChild(cq,"styleName");
                        if(snEl!=null) {
                            Element leafElem=styleMap.get(snEl.getTextContent().trim());
                            if(leafElem!=null) {
                                for(Node lc2=leafElem.getFirstChild();lc2!=null;lc2=lc2.getNextSibling()) {
                                    if(lc2.getNodeType()!=Node.ELEMENT_NODE) continue;
                                    String lTag=lc2.getNodeName();
                                    if(lTag.equals("name")||lTag.equals("query")||lTag.equals("priority")) continue;
                                    childElem.insertBefore(styDoc.importNode(lc2,true),childQs.get(0));
                                }
                            }
                        }
                        // ③ 자식 query·priority 제거 → 순수 시각 leaf
                        for(Element cqr:new ArrayList<>(childQs)) childElem.removeChild(cqr);
                        Element childPri=getFirstChild(childElem,"priority");
                        if(childPri!=null) childElem.removeChild(childPri);
                        // ④ 개별 속성 오버라이드 적용
                        applyStyleOverride(styDoc,childElem,cn);
                    }
                }
                addText(styDoc,q,"styleName",cn);
                addText(styDoc,q,"priority",String.valueOf(i+1));
                parent.appendChild(q);
            }
            if(tag.equals("pointStyle")) addText(styDoc,parent,"transparency","0");
            styDoc.getDocumentElement().appendChild(parent); styleMap.put(pname,parent);
            return;
        }
        // ── drawOrder 오름차순으로 leaf query 수집 (중복 제거) ──────────────────
        // drawOrder 낮음(먼저 그려야 할 것) → priority 낮은 값(1) → 먼저 렌더(아래)
        // drawOrder 높음(나중에 그려야 할 것) → priority 높은 값 → 나중 렌더(위)
        // ※ drawOrder와 priority 모두 작은 값이 먼저 렌더됨
        List<LeafQuery> allLqs=new ArrayList<>();
        Set<String> seen=new LinkedHashSet<>();
        for(String s:childStyles) {
            if(!isPointParent&&getStyleTag(s)!=null&&getStyleTag(s).equals("pointStyle")) continue;
            for(LeafQuery lq:getLeafQueries(s)) {
                String key=lq.cql+"|"+lq.field+"|"+lq.value+"|"+lq.leafStyle;
                if(seen.add(key)) allLqs.add(lq);
            }
        }
        // drawOrder 오름차순 → priority 오름차순 (작은 값이 먼저 그려짐)
        int total=allLqs.size();
        for(int idx=0;idx<total;idx++) {
            LeafQuery lq=allLqs.get(idx);
            int pri=idx+1;              // 첫 번째(낮은 drawOrder) → 가장 낮은 priority(먼저 그려짐)
            Element q=styDoc.createElement("query");
            if(lq.cql!=null) addText(styDoc,q,"cql",lq.cql);
            else if(lq.field!=null){addText(styDoc,q,"name",lq.field); addText(styDoc,q,"value",lq.value);}
            if(lq.leafStyle!=null) addText(styDoc,q,"styleName",lq.leafStyle);
            addText(styDoc,q,"priority",String.valueOf(pri));
            parent.appendChild(q);
        }
        if(tag.equals("pointStyle")) addText(styDoc,parent,"transparency","0");
        styDoc.getDocumentElement().appendChild(parent); styleMap.put(pname,parent);
    }

    private void createParentCql(String tag,String pname,List<String[]> cqlLeafs,String baseStyle) {
        Element parent=styDoc.createElement(tag); addText(styDoc,parent,"name",pname);
        if(tag.equals("pointStyle")) addText(styDoc,parent,"priority","0");
        Element base=styleMap.get(baseStyle);
        if(base!=null) for(Node c=base.getFirstChild();c!=null;c=c.getNextSibling()) {
            if(c.getNodeType()!=Node.ELEMENT_NODE) continue;
            String cn=c.getNodeName();
            if(cn.equals("name")||cn.equals("query")||cn.equals("priority")||cn.equals("transparency")) continue;
            parent.appendChild(styDoc.importNode(c,true));
        }
        int pri=1; Set<String> seenCql=new HashSet<>();
        for(String[] cl:cqlLeafs) {
            if(!seenCql.add(cl[0])) continue;
            Element q=styDoc.createElement("query"); addText(styDoc,q,"cql",cl[0]);
            addText(styDoc,q,"styleName",cl[1]); addText(styDoc,q,"priority",String.valueOf(pri++)); parent.appendChild(q);
        }
        if(tag.equals("pointStyle")) addText(styDoc,parent,"transparency","0");
        styDoc.getDocumentElement().appendChild(parent); styleMap.put(pname,parent);
    }

    // ── leaf query 수집 ────────────────────────────────────
    private List<LeafQuery> getLeafQueries(String styleName) {
        Element elem=styleMap.get(styleName); if(elem==null) return Collections.emptyList();
        List<Element> qList=getDirectChildren(elem,"query");
        // query 없는 순수 leaf → 자기 자신을 조건 없이 참조
        if(qList.isEmpty()) {
            LeafQuery o=new LeafQuery(); o.leafStyle=styleName; return Collections.singletonList(o);
        }
        List<LeafQuery> res=new ArrayList<>();
        for(Element q:qList) {
            Element cqlEl=getFirstChild(q,"cql"),fldEl=getFirstChild(q,"name"),
                    valEl=getFirstChild(q,"value"),snEl=getFirstChild(q,"styleName");
            if(snEl==null) continue;
            String leafName=snEl.getTextContent().trim(); Element leafElem=styleMap.get(leafName);
            if(leafElem!=null&&!getDirectChildren(leafElem,"query").isEmpty()) {
                for(Element lq:getDirectChildren(leafElem,"query")) {
                    Element lsn=getFirstChild(lq,"styleName"); if(lsn==null) continue;
                    LeafQuery o=new LeafQuery();
                    Element lcql=getFirstChild(lq,"cql"),lfld=getFirstChild(lq,"name"),lval=getFirstChild(lq,"value");
                    o.cql=lcql!=null?lcql.getTextContent().trim():cqlEl!=null?cqlEl.getTextContent().trim():null;
                    o.field=lfld!=null?lfld.getTextContent().trim():null;
                    o.value=lval!=null?lval.getTextContent().trim():null;
                    o.leafStyle=lsn.getTextContent().trim(); res.add(o);
                }
            } else {
                LeafQuery o=new LeafQuery();
                o.cql=cqlEl!=null?cqlEl.getTextContent().trim():null;
                o.field=fldEl!=null?fldEl.getTextContent().trim():null;
                o.value=valEl!=null?valEl.getTextContent().trim():null;
                o.leafStyle=leafName; res.add(o);
            }
        }
        return res;
    }

    // ── 유틸리티 ──────────────────────────────────────────
    private void buildStyleMap() {
        styleMap.clear();
        for(String t:STYLE_TAGS)
            for(Element e:getDirectChildElements(styDoc.getDocumentElement(),t)){
                String n=getChildText(e,"name"); if(n!=null)styleMap.put(n,e);}
    }
    private LayerEntry makeEntry(Element li) {
        String full=getChildText(li,"layerName"),sn=getChildText(li,"selectStyleName"),
               draw=getChildText(li,"drawOrder");
        if(full==null||sn==null||draw==null)return null;
        String layer=full.contains(":")?full.split(":")[1]:full;
        LayerEntry e=new LayerEntry(); e.fullLayerName=full;
        e.baseLayerName=layer.replaceAll("_\\d+$","");
        e.styleName=sn; e.drawOrder=Double.parseDouble(draw);
        e.layerInfoElem=li; e.styleTag=getStyleTag(sn); return e;
    }
    private String layerName(LayerEntry e){return e.fullLayerName.contains(":")?e.fullLayerName.split(":")[1]:e.fullLayerName;}
    private String getStyleTag(String name){Element e=styleMap.get(name);return e!=null?e.getTagName():null;}
    private static String toCql(String field,String value){String f=field.toLowerCase();
        try{Integer.parseInt(value);return f+"="+value;}catch(NumberFormatException ex){return f+"='"+value+"'";}}
    private static String levelSuffix(List<String> levels) {
        int mn=Integer.MAX_VALUE,mx=Integer.MIN_VALUE;
        for(String lv:levels){int v=Integer.parseInt(lv.trim());if(v<mn)mn=v;if(v>mx)mx=v;}
        return mn==mx?" "+mn+"레벨":" "+mn+"~"+mx+"레벨";
    }
    private void addLayerInfo(Element lc,String layerName,String styleName,String drawOrder){
        Element li=mapDoc.createElement("layerInfo"); addText(mapDoc,li,"layerName",layerName);
        addText(mapDoc,li,"selectStyleName",styleName); addText(mapDoc,li,"drawOrder",drawOrder);
        lc.appendChild(li);
    }
    private static List<Element> getDirectChildElements(Element parent,String tag){
        List<Element> r=new ArrayList<>();
        for(Node c=parent.getFirstChild();c!=null;c=c.getNextSibling())
            if(c.getNodeType()==Node.ELEMENT_NODE&&c.getNodeName().equals(tag))r.add((Element)c);
        return r;
    }
    private static List<Element> getDirectChildren(Element parent,String tag){return getDirectChildElements(parent,tag);}
    private static Element getFirstChild(Element parent,String tag){List<Element>l=getDirectChildren(parent,tag);return l.isEmpty()?null:l.get(0);}
    private static String getChildText(Element parent,String tag){Element c=getFirstChild(parent,tag);return c!=null?c.getTextContent().trim():null;}
    private static void setChildText(Element parent,String tag,String text){Element c=getFirstChild(parent,tag);if(c!=null)c.setTextContent(text);}
    private static void addText(Document doc,Element parent,String tag,String text){
        Element c=doc.createElement(tag);c.setTextContent(text);parent.appendChild(c);}
    // 공백만 있는 TEXT_NODE 제거 → 직렬화 시 불필요한 빈 줄 방지
    private static void normalizeWhitespace(Node node) {
        NodeList children=node.getChildNodes();
        List<Node> toRemove=new ArrayList<>();
        for(int i=0;i<children.getLength();i++) {
            Node child=children.item(i);
            if(child.getNodeType()==Node.TEXT_NODE
                    &&child.getNodeValue().trim().isEmpty()) {
                toRemove.add(child);
            } else {
                normalizeWhitespace(child);
            }
        }
        for(Node n:toRemove) node.removeChild(n);
    }
    private static void saveXml(Document doc,String path) throws Exception {
        normalizeWhitespace(doc);   // 공백 텍스트 노드 제거
        TransformerFactory tf=TransformerFactory.newInstance(); Transformer tr=tf.newTransformer();
        tr.setOutputProperty(OutputKeys.ENCODING,"UTF-8"); tr.setOutputProperty(OutputKeys.INDENT,"yes");
        tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","4");
        try(FileOutputStream fos=new FileOutputStream(path)){
            tr.transform(new DOMSource(doc),new StreamResult(new OutputStreamWriter(fos,StandardCharsets.UTF_8)));
        }
        out.printf("  → %s (%.0fKB)%n",path,new File(path).length()/1024.0);
    }
}
