# CLAUDE.md — MapService

지도 타일 생성·스타일 편집 GIS 프로젝트. 이 파일은 세션마다 반복 확인하지 않도록 검증된 사실만 정리한 것이다.

## 빌드 / 환경
- Maven, **JDK 8** (`C:\Program Files\Java\jdk1.8.0_211`). GeoTools 23.0, Apache POI.
- 컴파일: `mvn -o compile -q` (오프라인). 산출물은 `target/classes`.
- **git 미사용** 프로젝트. 위험한 수정 전에는 타임스탬프 백업 파일을 수동 생성한다 (예: `GisStyleExcelTool.java.bak_YYYYMMDD_HHMMSS`).
- 소스 파일이 UTF-8이므로 `javac`로 단독 컴파일 시 `-encoding UTF-8` 필요.
- 터미널이 UTF-8이 아니라 한글 출력이 깨질 수 있음(기능 문제 아님). Python 검증 스크립트는 `sys.stdout`을 UTF-8로 재설정해서 출력한다.
- 6월(개선 작업 시작 시점) 소스 원본이 `D:/workspace/MapService/src/main/java`에 보관되어 있음 — 회귀 의심 시 폴더 diff 대조에 사용.

## 두 갈래 렌더링 파이프라인 (핵심)
같은 이름의 클래스가 두 패키지에 병존하며 기능이 다르다.
- `com.gis.*` : 구(舊) 파이프라인. 타일 저장은 RocksDB(`com.gis.storage.TileDB`).
- `com.gis2.*` : 신(新) 파이프라인. 타일 저장은 jdbm(`com.gis2.storage.TileDB`).
- `Oper`의 `newMode` 플래그가 갈림: 입력 엑셀이 `.xlsm`이면 `newMode=true`(com.gis2 + freegis3 스타일), 아니면 `false`(com.gis).
- 공유 스타일 모델: `com.gis.protocol.freegis3` (신), `com.gis.protocol` (구/서비스).

## 주요 진입점 (default 패키지, `.vscode/launch.json`에 실행 구성 있음)
- `Oper` — properties 파일 1개를 인자로 받아 레이어 생성 / 스타일 반영 / 타일 생성. 예: `map_service/emap2.properties`, `per.properties`. properties의 `hdMode`, `tileNames`, `mbr`, `startLevel/endLevel` 등으로 제어.
- `GisStyleProcessor` — styles.xml + mapInfo.xml 을 받아 규칙 기반 후처리(주제도별 출력, 지하철/출구번호 스타일 조정 등).
- `GisStyleExcelTool` — styles.xml/mapInfo.xml ↔ Excel(.xlsm) 변환.
  - `export <styles.xml> <out.xlsm> <mapInfo1.xml> [mapInfo2...]`
  - `import <in.xlsm> <outBaseDir>` → `outBaseDir/tiles/styles.xml`, `outBaseDir/tiles/{theme}/mapInfo.xml`
  - `GisStyleExcelTool2~5`는 옛 실험 버전. 현행은 `GisStyleExcelTool`.
- `com.gis2.storage.TileDB` — 타일 DB 유틸. `fromDBtoFile <dbPath> <outDir>`로 DB를 개별 png로 추출(검증에 유용).

## 비자명한 규칙 (반드시 기억)
- **drawOrder = 그리기 순서 = 행 순서**. tile_ 시트에서 위쪽 행일수록 먼저 그려짐(아래 깔림), 아래쪽일수록 나중에 그려짐(위에 보임 + 겹침에서 우선). import 시 drawOrder는 셀 값이 아니라 **행 위치에서 재계산**된다. `GisStyleExcelTool`은 레벨별 상대순서를 위상정렬로 보존한다.
- **"이름_숫자" 파티션 레이어**(예: grid_1, grid_2): 용량 분할된 동일 논리 레이어. 자동으로 인접 배치되고 같은 레벨에서 동일 drawOrder 공유.
- **hdMode**(TileServiceMng.hdMode, 정적 전역): true면 타일 512px + pixelPerMeter÷2. 값 설정은 Oper(properties) / TileServiceMng(service_config.properties) 두 곳뿐이며 단방향(true일 때만 켬). ScaleInfos를 in-place로 ÷2 하므로 old/new 모드 간 객체 공유 여부에 따라 그리드가 어긋날 수 있으니 주의.
- **워크스페이스(crowd) 접두어는 제거됨**. GisStyleExcelTool은 레이어명을 접두어 없이 저장한다(렌더링 시 파일 로딩은 콜론 뒷부분만 사용하므로 무해). 특수 접두어 `tileMapService:`/`raster:`/`vector:`만 레이어 종류 판별에 사용됨.
- **EPSG 역방향 조회 불가**: pom에 `gt-epsg-wkt`만 있고 `gt-epsg-hsql`이 없어 `CRS.lookupEpsgCode`가 null 반환. 순방향 `CRS.decode("EPSG:xxxx")`는 동봉된 `src/main/java/org/geotools/referencing/crs/epsg.properties`(표준 EPSG 5669개)로 정상. 데이터 .prj는 ESRI 스타일 WKT(EPSG:5179 UTM-K). 실사용 좌표계는 전부 투영계(5179/5186/5174)라 축순서 사고 위험은 낮음.
- **엑셀 import는 열 위치가 아니라 헤더 이름으로 열을 찾는다** → 사용자가 임의 위치에 커스텀 열을 넣어도 안전. 스타일_포인트만 2행 헤더(데이터는 3행부터).

## 검증 방법 (관행)
- 코드 변경 후 `mvn -o compile -q` + 실데이터 export→import 라운드트립 + Python 스크립트로 결과 XML diff.
- 실데이터 경로: `F:/coops/project/ori/{styles,mapInfo}.xml`, `F:/coops/project/out/`, 타일 산출물 `F:/coops/2024/tileset/output/`.
- 임시 파일은 세션 scratchpad 디렉토리 사용.

## 사용자 매뉴얼
- `엑셀_사용자메뉴얼_v1.doc` — GisStyleExcelTool 엑셀 포맷 사용자용. python-docx로 생성 후 Word COM으로 .doc 변환. XML 엘리먼트 이름은 노출하지 않고 엑셀 관점으로만 서술한다.
