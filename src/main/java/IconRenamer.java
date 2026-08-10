import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 아이콘 파일을 규칙에 따라 복사/이름변경 한다.
 *
 * 규칙:
 *  1. 파일명을 "_" 구분자로 나누어 첫 단어(분류명)를 제거한 나머지를 파일명으로 저장한다.
 *  2. 최종 파일명에서 괄호 "(...)" 와 그 안의 글자는 삭제한다.
 *
 * 사용법:
 *   javac -encoding UTF-8 IconRenamer.java
 *   java -Dfile.encoding=UTF-8 IconRenamer [원본폴더] [출력폴더]
 *   (인자를 생략하면 기본값 E:\claude\줄인거 -> E:\claude\out 사용)
 */
public class IconRenamer {

    public static void main(String[] args) throws IOException {
        Path srcDir = Paths.get(args.length > 0 ? args[0] : "E:\\claude\\icon\\2배키운거");
        Path destDir = Paths.get(args.length > 1 ? args[1] : "E:\\claude\\icon\\symbols2");

        if (!Files.isDirectory(srcDir)) {
            System.out.println("원본 폴더가 존재하지 않습니다: " + srcDir);
            return;
        }
        Files.createDirectories(destDir);

        List<Path> files;
        try (Stream<Path> walk = Files.walk(srcDir)) {
            files = walk.filter(Files::isRegularFile).collect(Collectors.toList());
        }

        int count = 0;
        for (Path file : files) {
            String originalName = file.getFileName().toString();
            String newName = transformName(originalName);
            Path target = resolveCollision(destDir, newName);

            Files.copy(file, target, StandardCopyOption.COPY_ATTRIBUTES);
            System.out.println(originalName + "  ->  " + target.getFileName());
            count++;
        }

        System.out.println();
        System.out.println("총 " + count + "개 파일 처리 완료 (" + destDir + ")");
    }

    /** 파일명 변경 규칙 적용 */
    private static String transformName(String name) {
        String result = name;

        int idx = name.indexOf('_');
        if (idx >= 0) {
            result = name.substring(idx + 1);
        }

        // 괄호와 괄호 안 내용 제거
        result = result.replaceAll("\\([^)]*\\)", "");

        return result;
    }

    /** 같은 이름의 파일이 이미 있으면 "_1", "_2" ... 접미사를 붙여 충돌을 피한다 */
    private static Path resolveCollision(Path destDir, String name) {
        Path target = destDir.resolve(name);
        if (!Files.exists(target)) {
            return target;
        }

        String base = name;
        String ext = "";
        int dot = name.lastIndexOf('.');
        if (dot > 0) {
            base = name.substring(0, dot);
            ext = name.substring(dot);
        }

        int i = 1;
        Path candidate;
        do {
            candidate = destDir.resolve(base + "_" + i + ext);
            i++;
        } while (Files.exists(candidate));

        return candidate;
    }
}
