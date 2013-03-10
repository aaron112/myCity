package edu.ucsd.mycity.test;

import com.google.android.maps.GeoPoint;

import android.test.AndroidTestCase;
import edu.ucsd.mycity.PlacesHandler;

public class TestPlaces extends AndroidTestCase {

	private PlacesHandler places;
	private GeoPoint point;
	
	protected void setUp() throws Exception
	{
		super.setUp();
		places = new PlacesHandler();
		point = new GeoPoint( 32877425, -117232189 );
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
	
	public void testgetLocalServices() {
		places.getLocalServices( point, 707, 707 );
	}
}