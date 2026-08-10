package com.util.io;
import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.*;

public class CadConvert {
    private static final String LOG_DIR = "/data1/logs/";

    public static String transfer(String inputSrc, String outputSrc, String fileName, String cad_yn) {
        String logFilePath = getLogFilePath();
        logMessage(logFilePath, "변환 시작: " + inputSrc);

        File inputDir = new File(inputSrc);
        File outputDir = new File(outputSrc);

        if (!inputDir.exists() || !inputDir.isDirectory()) {
            logMessage(logFilePath, "입력 경로 오류: 존재하지 않거나 디렉토리가 아님 - " + inputSrc);
            return null;
        }

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        String zipFilePath = outputSrc + File.separator + fileName + ".zip";

        if ("Y".equalsIgnoreCase(cad_yn)) {
            File[] shpFiles = inputDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".shp"));

            if (shpFiles == null || shpFiles.length == 0) {
                logMessage(logFilePath, "변환할 SHP 파일 없음: " + inputSrc);
                return null;
            }

            boolean allSuccess = true;

            for (File shpFile : shpFiles) {
                String shpFilePath = shpFile.getAbsolutePath();
                String dxfFilePath = outputSrc + File.separator + shpFile.getName().replace(".shp", ".dxf");

                boolean success = convertShpToDxf(shpFilePath, dxfFilePath, logFilePath);
                if (!success) {
                    allSuccess = false;
                }
            }

            if (allSuccess) {
                boolean zipSuccess = zipDxfFiles(outputSrc, zipFilePath, logFilePath);
                logMessage(logFilePath, "변환 완료: " + zipFilePath);
                return zipSuccess ? zipFilePath : null;
            }

        } else if ("N".equalsIgnoreCase(cad_yn)) {
            boolean zipSuccess = zipFolder(inputSrc, zipFilePath, logFilePath);
            logMessage(logFilePath, "SHP 폴더 압축 완료: " + zipFilePath);
            return zipSuccess ? zipFilePath : null;
        }

        logMessage(logFilePath, "변환 실패!");
        return null;
    }

    private static boolean convertShpToDxf(String inputShp, String outputDxf, String logFilePath) {
        try {
            String command = String.format("ogr2ogr -f \"DXF\" \"%s\" \"%s\" -a_srs EPSG:5179 -select \"\"",
                    outputDxf, inputShp);

            logMessage(logFilePath, "변환 실행: " + command);

            ProcessBuilder processBuilder = new ProcessBuilder("/bin/sh", "-c", command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                logMessage(logFilePath, line);
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                logMessage(logFilePath, "변환 성공: " + outputDxf);
                return true;
            } else {
                logMessage(logFilePath, "변환 실패: " + outputDxf);
                return false;
            }
        } catch (Exception e) {
            logMessage(logFilePath, "변환 오류: " + e.getMessage());
            return false;
        }
    }

    private static boolean zipDxfFiles(String outputSrc, String zipFilePath, String logFilePath) {
        return zipFolder(outputSrc, zipFilePath, logFilePath, ".dxf");
    }

    private static boolean zipFolder(String folderPath, String zipFilePath, String logFilePath) {
        return zipFolder(folderPath, zipFilePath, logFilePath, null);
    }

    private static boolean zipFolder(String folderPath, String zipFilePath, String logFilePath, String extensionFilter) {
        try (FileOutputStream fos = new FileOutputStream(zipFilePath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            File folder = new File(folderPath);
            File[] files = (extensionFilter == null)
                    ? folder.listFiles()
                    : folder.listFiles((dir, name) -> name.toLowerCase().endsWith(extensionFilter));

            if (files == null || files.length == 0) {
                logMessage(logFilePath, "압축할 파일이 없음: " + folderPath);
                return false;
            }

            for (File file : files) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    ZipEntry zipEntry = new ZipEntry(file.getName());
                    zos.putNextEntry(zipEntry);

                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                    zos.closeEntry();
                }
            }

            logMessage(logFilePath, "압축 성공: " + zipFilePath);
            return true;
        } catch (IOException e) {
            logMessage(logFilePath, "압축 오류: " + e.getMessage());
            return false;
        }
    }

    private static void logMessage(String logFilePath, String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String logEntry = timestamp + " - " + message;

        // 콘솔 출력
        System.out.println(logEntry);

        // 로그 파일에 기록
        try (FileWriter fw = new FileWriter(logFilePath, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(logEntry);
            bw.newLine();
        } catch (IOException e) {
            System.err.println("로그 파일 기록 오류: " + e.getMessage());
        }
    }

    private static String getLogFilePath() {
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String logFilePath = LOG_DIR + date + ".log";

        // 로그 디렉토리 생성
        File logDir = new File(LOG_DIR);
        if (!logDir.exists()) {
            logDir.mkdirs();
        }

        return logFilePath;
    }
}


