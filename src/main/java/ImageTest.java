import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.GridCoverageLayer;
import org.geotools.map.MapContent;
import org.geotools.map.MapViewport;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.locationtech.jts.geom.Envelope;
import org.opengis.coverage.grid.GridCoordinates;
import org.opengis.coverage.grid.GridEnvelope;

public class ImageTest {
	
	MapContent mapContent = new MapContent();
	
	org.opengis.referencing.crs.CoordinateReferenceSystem gcCrs = null;
	
	GTRenderer renderer = new StreamingRenderer();
	
	GridCoverage2D gc = null;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		long st = System.currentTimeMillis();
		ImageTest test = new ImageTest();
//		test.readGeoTiff(new File("D:\\coops\\heat\\APT.tif"));
		test.readGeoTiff(new File("F:\\coops\\2024\\tileset\\origin\\raster\\35_color.tif"));
		
		Envelope2D env = test.gc.getEnvelope2D();
		
		Envelope mbr = new Envelope(env.getMinX(), env.getMaxX(), env.getMinY(), env.getMaxY());
		//mbr.expandBy(1000);
		
		double width = mbr.getWidth()/2.0;
		double height = mbr.getHeight()/2.0;
		
		Envelope mbr1 = new Envelope(869547, 1404321, 1326131, 1860904);
		
		BufferedImage image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
		
		test.drawMap(mbr1, image);
		
		try {
			ImageIO.write(image, "png", new File("F:\\coops\\2024\\tileset\\origin\\raster\\test.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		long ed = System.currentTimeMillis();
		System.out.println("time = " + (ed-st)/1000 +" 초");
	}
	
	public void readGeoTiff(java.io.File file) {
		try {

			GridCoverage2DReader reader = new GeoTiffReader(file);

			this.gc = reader.read(null);

			StyleFactory sf = CommonFactoryFinder.getStyleFactory();

			// sf.getDefaultRasterSymbolizer();
			RasterSymbolizer rs = sf.getDefaultRasterSymbolizer();

			this.gcCrs = reader.getCoordinateReferenceSystem();

			Style style = SLD.wrapSymbolizers(rs);

			GridCoverageLayer gcl = new GridCoverageLayer(gc, style);

			mapContent.addLayer(gcl);
			
			Map<Object, Object> hints = new HashMap<>();
			hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			//renderer.setJava2DHints(hints);
			
			Map<Object, Object> tt = this.renderer.getRendererHints();
			
			this.renderer.setRendererHints(hints);

			this.renderer.setMapContent(mapContent);

			GeneralEnvelope env = reader.getOriginalEnvelope();

			GridEnvelope dimensions = reader.getOriginalGridRange();
			GridCoordinates maxDimensions = dimensions.getHigh();
			int w = maxDimensions.getCoordinateValue(0) + 1;
			int h = maxDimensions.getCoordinateValue(1) + 1;

			System.out.println(env + " | w = " + w + ", h = " + h);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public void drawMap(Envelope mbr, BufferedImage img) {
		
		
		double pixelPerMeter = mbr.getWidth() / img.getWidth();


		MapViewport mvp = mapContent.getViewport();

		Rectangle rect = new Rectangle(0, 0, img.getWidth(), img.getHeight());

		mvp.setScreenArea(rect);

		ReferencedEnvelope renv = new ReferencedEnvelope(mbr.getMinX(), mbr.getMaxX(), mbr.getMinY(), mbr.getMaxY(),
				this.gcCrs);

		mvp.setBounds(renv);

		mapContent.setViewport(mvp);

		Graphics2D g = img.createGraphics();
		//Graphics2D g = img.getGraphics();
		
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);


		renderer.paint(g, rect, renv);
	}
	

}
