package edu.ucsd.mycity.test;

import android.test.AndroidTestCase;

import com.google.android.maps.GeoPoint;

import edu.ucsd.mycity.GooglePlacesHandler;

public class TestPlaces extends AndroidTestCase
{

	private GeoPoint point;

	protected void setUp() throws Exception
	{
		super.setUp();
		point = new GeoPoint(32877425, -117232189);
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}

	public void testgetLocalServices()
	{
		assertTrue(GooglePlacesHandler.updateLocalServices(point, 20, 20, "food"));
		assertNotNull(GooglePlacesHandler.getLocalServices());
	}
}