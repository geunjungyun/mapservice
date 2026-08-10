package com.gis.map;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Vector;

import org.locationtech.jts.geom.Envelope;

import com.gis.projection.ScreenCoordUtil;

public interface Context {
	Vector<Layer> loadMemLayers(Envelope __e, ScreenCoordUtil _scu);
	int drawMap(Graphics2D _g, Envelope _e, ScreenCoordUtil scu, Vector<Layer> layers, boolean isDebug);
}
