import java.io.IOException;
import java.util.Iterator;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.opengis.feature.simple.SimpleFeature;

import com.data.file.FileLayer;

public class LayerTest {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		try {
			FileLayer fl = new FileLayer("f:\\coops\\2024\\tileset\\output\\reg20\\layers\\FA", null);
			int idx = 0;
			Iterator it = fl.iterator();
			while(it.hasNext()) {
				SimpleFeature sf = (SimpleFeature)it.next();
				System.out.println("idx="+(idx++)+", "+sf.toString());
			}
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

	}
	
	public int test1() throws IOException {
		return this.test();
	}
	
	
	public int test() throws IOException  {
		
		int t = 1;
		int s = 1;
		int e = 1;
		if(t == 1) {
			throw new InternalError("");
			
		}
		
		return t;
	}
}
