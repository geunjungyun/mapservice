import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.mapplan.Excel;
import com.util.io.FileUt;

public class ExcelConv {
	
	//String excelpath = "";
	
	String excelpath = "d:\\coops\\문서\\SHP파일폴더분류표_200428_1.xlsx";

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ExcelConv ec = new ExcelConv();
		try {
			ec.readExcel();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Vector<Excel> readExcel() throws Exception{
		XSSFWorkbook workbook = null;
		Vector<Excel> objs = new Vector();
		try {
//            FileInputStream fis = new FileInputStream("D:\\coops\\문서\\SHP파일폴더분류표_200428_1.xlsx");
//            HSSFWorkbook workbook = new HSSFWorkbook(fis);

			int nameIdx = -1;
			int fnIdx = -1;
			int fnIdx1 = -1;
			int fnIdx2 = -1;
			int epsgIdx = -1;
			int codeIdx = -1;
			int deleteCodeIdx = -1;
			int fillColorIdx = -1;
			int fillWidthIdx = -1;
			int lineColorIdx = -1;
			int lineWidthIdx = -1;
			int codeFieldIdx = -1;
			int textFieldIdx = -1;

			FileInputStream fis = new FileInputStream(new File(this.excelpath));
			//OPCPackage pkg = null;
//			try {
//				pkg = OPCPackage.open(this.excelpath);
//			} catch (InvalidFormatException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
			workbook = new XSSFWorkbook(fis);

			XSSFSheet sheet = workbook.getSheetAt(0);
			
			
			
			String name = WorkbookUtil.createSafeSheetName("배경레이어구성");
			
			XSSFSheet layers = workbook.createSheet(name);
			
			
			
			//Row row = layers.createRow(0);
			//Cell cell = row.createCell(0);
			//cell.setCellValue("test");
			
			XSSFSheet areaColor = workbook.createSheet("배경색상표");
			XSSFSheet pointColor = workbook.createSheet("배경주기구성");
			
//			this.set(layers, workbook);
//			this.set(areaColor, workbook);
//			this.set(pointColor, workbook);
			
			int xlsxRows = sheet.getPhysicalNumberOfRows();

			for (int rownum = 0; rownum < xlsxRows; rownum++) {
				XSSFRow xlsxRow = sheet.getRow(rownum); // 셀정보
				short maxCellNum = xlsxRow.getLastCellNum();
				Excel ec = new Excel();
				for (int cellIdx = 0; cellIdx < maxCellNum; cellIdx++) {

					XSSFCell cell = xlsxRow.getCell(cellIdx);
					
					

//					if(cell == null) {
//						continue;
//					}

					if (cell != null && rownum == 0) {
						String fdN = xlsxRow.getCell(cellIdx).toString();
						if (fdN.equals("대분류 폴더명")) {
							fnIdx = cellIdx;
						} else if (fdN.equals("좌표계")) {
							epsgIdx = cellIdx;
						} else if (fdN.equals("코드")) {
							codeIdx = cellIdx;
						} else if (fdN.equals("삭제대상 지역지구코드(중첩)")) {
							deleteCodeIdx = cellIdx;
						} else if (fdN.equals("면색 RGB")) {
							fillColorIdx = cellIdx;
						} else if (fdN.equals("면선 굵기")) {
							fillWidthIdx = cellIdx;
						} else if (fdN.equals("선색 RGB")) {
							lineColorIdx = cellIdx;
						} else if (fdN.equals("선굵기")) {
							lineWidthIdx = cellIdx;
						} else if (fdN.equals("업무구분")) {
							nameIdx = cellIdx;
						} else if (fdN.equals("코드필드")) {
							codeFieldIdx = cellIdx;
						} else if (fdN.equals("주기이름필드")) {
							textFieldIdx = cellIdx;
						} else if (fdN.equals("중분류 폴더명")) {
							fnIdx1 = cellIdx;
						} else if (fdN.equals("소분류 폴더명")) {
							fnIdx2 = cellIdx;
						}
					} else {

						String value = "";

						if (cell != null) {
							value = cell.toString();
						}

						if (cellIdx == nameIdx) {
							ec.setName(value);
						} else if (cellIdx == fnIdx) {
							ec.setFoldName(value);
						} else if (cellIdx == epsgIdx) {
							ec.setEpsg(value);
						} else if (cellIdx == codeIdx) {
							ec.setCode(value);
						} else if (cellIdx == deleteCodeIdx) {
							ec.setDeleteCode(value);
						} else if (cellIdx == fillColorIdx) {
							ec.setFillColor(value);
						} else if (cellIdx == fillWidthIdx) {
							try {
								ec.setFillWidth(Float.valueOf(value));
							} catch (Exception e) {
								// System.out.println("fillWidthIdx="+value);
							}
						} else if (cellIdx == lineColorIdx) {
							ec.setLineColor(value);
						} else if (cellIdx == lineWidthIdx) {
							try {
								ec.setLineWidth(Float.valueOf(value));
							} catch (Exception e) {
								// System.out.println("lineWidthIdx="+value);
							}
						} else if (cellIdx == codeFieldIdx) {
							ec.setCodeField(value == null ? "" : value);
						} else if (cellIdx == textFieldIdx) {
							ec.setTextField(value == null ? "" : value);
						} else if (cellIdx == fnIdx1) {
							ec.setFoldName1(value);
						} else if (cellIdx == fnIdx2) {
							ec.setFoldName2(value);
						}

					}

				}
				if (rownum == 0) {
					continue;
				}
				objs.add(ec);

			}
			//fis.close();
			
			

			
			String pname = null;
			String pfdName = null;
			String pfdName1 = null;
			String pfdName2 = null;
			String pepsg = null;
			String pcodeField = null;
			String ptextField = null;
			for (int i = 0; i < objs.size(); i++) {
				Excel ec = objs.get(i);

				if (i == 0) {
					pname = ec.getName();
					pfdName = ec.getFoldName().equals("x") ? "" : ec.getFoldName();
					ec.setFoldName(ec.getFoldName().equals("x") ? "" : ec.getFoldName());
					pfdName1 = ec.getFoldName1().equals("x") ? "" : ec.getFoldName1();
					ec.setFoldName1(ec.getFoldName1().equals("x") ? "" : ec.getFoldName1());
					pfdName2 = ec.getFoldName2().equals("x") ? "" : ec.getFoldName2();
					ec.setFoldName2(ec.getFoldName2().equals("x") ? "" : ec.getFoldName2());
					pepsg = ec.getEpsg();
					pcodeField = ec.getCodeField().equals("x") ? "" : ec.getCodeField();
					ec.setCodeField(ec.getCodeField().equals("x") ? "" : ec.getCodeField());
					ptextField = ec.getTextField().equals("x") ? "" : ec.getTextField();
					ec.setTextField(ec.getTextField().equals("x") ? "" : ec.getTextField());

					continue;
				}

				if (ec.getName().equals("x")) {
					pname = "";
					ec.setName("");

				} else if (ec.getName().length() > 0) {
					pname = ec.getName();
				} else {
					ec.setName(pname);
				}

				if (ec.getFoldName().equals("x")) {

					pfdName = "";
					ec.setFoldName("");
				} else if (ec.getFoldName().length() > 0) {
					pfdName = ec.getFoldName();
				} else {
					ec.setFoldName(pfdName);
				}

				if (ec.getFoldName1().equals("x")) {
					pfdName1 = "";
					ec.setFoldName1("");
				} else if (ec.getFoldName1().length() > 0) {
					pfdName1 = ec.getFoldName1();
				} else {
					ec.setFoldName1(pfdName1);
				}

				if (ec.getFoldName2().equals("x")) {
					pfdName2 = "";
					ec.setFoldName2("");
				} else if (ec.getFoldName2().length() > 0) {
					pfdName2 = ec.getFoldName2();
				} else {
					ec.setFoldName2(pfdName2);
				}

				if (ec.getEpsg().equals("x")) {
					pepsg = "";
					ec.setEpsg("");
				} else if (ec.getEpsg().length() > 0) {
					pepsg = ec.getEpsg();
				} else {
					ec.setEpsg(pepsg);
				}

				if (ec.getCodeField().equals("x")) {
					pcodeField = ec.getCodeField();
					pcodeField = "";
					ec.setCodeField("");
				} else if (ec.getCodeField().length() > 0) {
					pcodeField = ec.getCodeField();
				} else {
					ec.setCodeField(pcodeField);
				}

				if (ec.getTextField().equals("x")) {
					ptextField = ec.getTextField();
					ptextField = "";
					ec.setTextField("");
				} else if (ptextField.length() > 0) {
					ptextField = ec.getTextField();
				} else {
					ec.setTextField(ptextField);
				}

				System.out.println("|Name=" + ec.getName() + "|FoldName=" + ec.getFoldName() + "|FoldName1="
						+ ec.getFoldName1() + "|FoldName2=" + ec.getFoldName2() + "|CodeField=" + ec.getCodeField()
						+ "|TextField=" + ec.getTextField() + "|Epsg=" + ec.getEpsg() + "|Code=" + ec.getCode()
						+ "|DeleteCode=" + ec.getDeleteCode() + "|FillColor=" + ec.getFillColor() + "|FillWidth="
						+ ec.getFillWidth() + "|LineColor=" + ec.getLineColor() + "|LineWidth=" + ec.getLineWidth());
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		this.createColor(objs, workbook);
		
		
		File oriFile = new File(this.excelpath);
		
		String parentPath = oriFile.getParent();
		
		FileOutputStream fos = new FileOutputStream(new File(parentPath+FileUt.SEPERATOR+"mapplan.xlsx."));
		workbook.write(fos);
		workbook.close();
		
		
		
		
		return objs;
	}
	
	public void createColor(Vector<Excel> objs, XSSFWorkbook workbook) throws Exception{
		
		
		HashMap<String, Excel> map = new HashMap();
		for(Excel cel : objs) {
			if(cel.getCode() != null && cel.getCode().length() > 2 && (cel.getName().equals("토지이용계획도")||cel.getName().equals("도시계획도") )) {
				
				if(!map.containsKey(cel.getCode())){
					map.put(cel.getCode(), cel);	
				}
				else {
					Excel save = map.get(cel.getCode());
					if(!save.getFillColor().equals(cel.getFillColor()) || !save.getLineColor().equals(cel.getLineColor())) {
						System.out.println("code = " +cel.getCode()+", save, fc="+ save.getFillColor()+", lc=" + save.getLineColor() +", new = , fc = "+ cel.getFillColor()+", lc = " + cel.getLineColor());
					}
				}
			}
		}
		
		
		map.keySet().stream().sorted(). // 정렬(기본: 오름차순) 
		forEach(key -> System.out.println(key + ": " + map.get(key))); // 출력 
		
		Vector<String> rowName = new Vector();
		rowName.add("code");
		rowName.add("fillColorRgb");
		rowName.add("fillColor");
		rowName.add("lineColorRgb");
		rowName.add("lineColor");
		rowName.add("lineWidth");
		
		XSSFSheet sheet = workbook.createSheet("토지도시색상표");
		
		Set set = map.keySet();
		Iterator it = set.iterator();
		
		int rowIdx = 0;
		while(it.hasNext()) {
			
			Excel cel = map.get(it.next());
			Row row = sheet.createRow(rowIdx);
			
			if(rowIdx == 0) {
				for(int j=0; j<rowName.size(); j++) {
					Cell cell = row.createCell(j);
					cell.setCellValue(rowName.get(j));
				}
			}
			else {
				row.createCell(0).setCellValue(cel.getCode().trim());
				row.createCell(1).setCellValue(cel.getFillColor().trim());
				if(cel.getFillColor().trim().length() > 0) {
					row.createCell(2).setCellStyle(this.getStyle(workbook, cel.getFillColor().trim()));
				}
				row.createCell(3).setCellValue(cel.getLineColor().trim());
				if(cel.getLineColor().trim().length() > 0) {
					row.createCell(4).setCellStyle(this.getStyle(workbook, cel.getLineColor().trim()));
				}
				
				row.createCell(5).setCellValue(cel.getLineWidth() == null ? 0 : cel.getLineWidth());
			}
			rowIdx++;
		}
		
		
	}
	
	public XSSFCellStyle getStyle(XSSFWorkbook workbook, String rgb) {
		
		String[] ss = rgb.split(",");
		
	    XSSFCellStyle style1 = workbook.createCellStyle();
	    style1.setFillForegroundColor(new XSSFColor(new java.awt.Color(Integer.parseInt(ss[0]), Integer.parseInt(ss[1]), Integer.parseInt(ss[2]))));
	    //style1.setFillForegroundColor((short)128);
	    style1.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    	return style1;
	}
	
}
