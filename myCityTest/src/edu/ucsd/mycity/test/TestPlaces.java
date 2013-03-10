package edu.ucsd.mycity.test;

import com.google.android.maps.GeoPoint;

import android.test.AndroidTestCase;
import edu.ucsd.mycity.GooglePlacesHandler;

public class TestPlaces extends AndroidTestCase {

	private GooglePlacesHandler places;
	private GeoPoint point;
	
	protected void setUp() throws Exception
	{
		super.setUp();
		places = new GooglePlacesHandler();
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