package edu.ucsd.mycity.test;

import java.util.Iterator;
import java.util.List;

import android.test.AndroidTestCase;

import com.google.android.maps.GeoPoint;

import edu.ucsd.mycity.GooglePlacesHandler;
import edu.ucsd.mycity.localservices.GPlace;

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
		for (int i = 0; i < 50; i++)
		{
			assertTrue(GooglePlacesHandler.updateLocalServices(point, 20, 20,
			         "food"));
			List<GPlace> places = GooglePlacesHandler.getLocalServices();

			Iterator<GPlace> it = places.iterator();

			while (it.hasNext())
			{
				GPlace temp = it.next();
				assertNotNull(temp.getAddress());
				assertNotNull(temp.getName());
				assertNotNull(temp.getPhone());
				assertNotNull(temp.getRef());
				assertNotNull(temp.getLocation());
				assertNotNull(temp.getTypes());
			}
		}
	}
}