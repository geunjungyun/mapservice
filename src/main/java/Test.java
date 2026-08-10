import com.util.io.CadConvert;

public class Test {
    public static void main(String[] args) {
        // 입력 폴더 (SHP 파일들이 있는 곳)
        String inputShpDir = "/data1/save/source/2024/20240318/a647e385-5333-4527-8812-1c12306a0b79";

        // 출력 폴더 (DXF 파일 또는 ZIP 파일이 저장될 곳)
        String outputDxfDir = "/data1/save/output/2024/20240318/a647e385-5333-4527-8812-1c12306a0b79";

        // 압축 파일명 (확장자 제외, 자동으로 .zip 추가)
        String zipFileName = "klandmap-250318-1010-23.zip";

        // cad_yn = "Y"이면 SHP → DXF 변환 후 압축, "N"이면 SHP 폴더를 압축
        String cadYn = "Y";  // "N"으로 변경하면 SHP 폴더 압축

        // 변환 및 압축 실행
        String zipFilePath = CadConvert.transfer(inputShpDir, outputDxfDir, zipFileName, cadYn);

        // 결과 출력
        if (zipFilePath != null) {
            System.out.println("변환 및 압축 성공: " + zipFilePath);
        } else {
            System.out.println("변환 또는 압축 실패!");
        }
    }
}

