package com.gis.map.render;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

//import org.apache.commons.pool.BasePoolableObjectFactory;

public class ImagePool extends BasePooledObjectFactory {

	int width;
	int height;
	
	AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f);
	
	public ImagePool(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		GenericObjectPoolConfig gop = new GenericObjectPoolConfig();
		gop.setMaxTotal(10);
		gop.setMaxWaitMillis(2000);

		GenericObjectPool<BufferedImage> objectPool = new GenericObjectPool<BufferedImage>(new ImagePool(400,400), gop);

		for (int i = 0; i < 10; i++) {
			BufferedImage image = null;
			try {
				image = objectPool.borrowObject();
				objectPool.returnObject(image);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

//	@Override
//	public PooledObject<BufferedImage> makeObject() throws Exception {
//		System.out.println("makeObject");
//		// TODO Auto-generated method stub
//		BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
//		DefaultPooledObject dpo = new DefaultPooledObject<BufferedImage>(image);
//		return dpo;
//	}

	@Override
	public Object create() throws Exception {
		//System.out.println("create");
		// TODO Auto-generated method stub
		BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
		return image;
	}

	@Override
	public PooledObject wrap(Object obj) {
		// TODO Auto-generated method stub
		//System.out.println("wrap");
		//BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
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
		g2d.setComposite(composite);
		//g2d.setColor(new Color(0, 0, 0, 0));
		g2d.fillRect(0, 0, img.getWidth(), img.getHeight());
    }

}
