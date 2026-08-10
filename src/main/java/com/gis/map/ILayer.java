package com.gis.map;


import org.locationtech.jts.geom.Envelope;


import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public interface ILayer {



	/**
	 * Get the title of this layer. If title has not been defined then an empty
	 * string is returned.
	 * 
	 * @return The title of this layer.
	 */
	String getName();

	/**
	 * Set the title of this layer. A {@link LayerEvent} is fired if the new
	 * title is different from the previous one.
	 * 
	 * @param title
	 *            The title of this layer.
	 */
	void setName(String title);


	/**
	 * Returns the definition query (filter) for this layer. If no definition
	 * query has been defined {@link Query.ALL} is returned.
	 * 
	 */
	// Query getQuery();

	/**
	 * Sets a definition query for the layer wich acts as a filter for the
	 * features that the layer will draw.
	 * 
	 * <p>
	 * A consumer must ensure that this query is used in combination with the
	 * bounding box filter generated on each map interaction to limit the number
	 * of features returned to those that complains both the definition query
	 * and relies inside the area of interest.
	 * </p>
	 * <p>
	 * IMPORTANT: only include attribute names in the query if you want them to
	 * be ALWAYS returned. It is desirable to not include attributes at all but
	 * let the layer user (a renderer?) to decide wich attributes are actually
	 * needed to perform its requiered operation.
	 * </p>
	 * 
	 * @param query
	 */
	// void setQuery(Query query);
	/**
	 * find out the bounds of the layer
	 * 
	 * @return - the layer's bounds
	 */
	Envelope getBounds();
	

	public float getDrawPriority();

	public void setDrawPriority(float visible);
}
