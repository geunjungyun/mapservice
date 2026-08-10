package com.gis2.map.render;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * @author Administrator
 * 항공영상 jpg로 타일링 하기위한 임시 클래스,, 추후 삭제
 */
public class JpgImagePool extends BasePooledObjectFactory {
	
	int width;
	int height;
	
	public JpgImagePool(int width, int height) {
		// TODO Auto-generated constructor stub
		this.width = width;
		this.height = height;
	}

	@Override
	public Object create() throws Exception {
		// TODO Auto-generated method stub
		BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = image.createGraphics();
		g2d.setColor(Color.white);
		g2d.fillRect(0, 0, this.width, this.height);
		return image;
	}

	@Override
	public PooledObject wrap(Object obj) {
		// TODO Auto-generated method stub
		DefaultPooledObject dpo = new DefaultPooledObject<BufferedImage>((BufferedImage)obj);
		return dpo;
	}
	
	/**
	 * 객체 재사용을 위한 객체 초기화
	 */
	@Override
    public void passivateObject(PooledObject p)
            throws Exception {
		//System.out.println("passivateObject");
		BufferedImage img = (BufferedImage)p.getObject();
		Graphics2D g2d = (Graphics2D) img.createGraphics();
//		g2d.setComposite(composite);
		g2d.setColor(Color.white);
		g2d.fillRect(0, 0, img.getWidth(), img.getHeight());
    }

}
