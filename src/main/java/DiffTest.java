import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;

public class DiffTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		WKTReader reader= new WKTReader();
		//reader.read(wellKnownText);
		List<String> list = new ArrayList();
		try {
			list = FileUtils.readLines(new File("D:\\geo.txt"), "UTF-8");
			Geometry ge = reader.read(list.get(0));
			Geometry g21 = reader.read(list.get(1));
			
			
			ge = ge.buffer(0.1);
			g21 = g21.buffer(0.1);
			Geometry dif = ge.difference(g21);
			
			System.out.println();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
