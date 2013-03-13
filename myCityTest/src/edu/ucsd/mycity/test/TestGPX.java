package edu.ucsd.mycity.test;

import java.util.ArrayList;

import android.test.AndroidTestCase;
import edu.ucsd.mycity.utils.GPX;

public class TestGPX extends AndroidTestCase
{

	protected void setUp() throws Exception
	{
		super.setUp();
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
	
	private void buildAndParseGPX(double lat, double lon) {
		ArrayList<String> testres;
		// Elevation is ignored on implementation
		String testXML = GPX.buildGPX(lat, lon, 0, 0);
		testres = GPX.parseGPX(testXML);
		assertNotNull(testres);
		assertEquals(testres.size(), 3);
		assertEquals(Double.parseDouble(testres.get(0)), lat);
		assertEquals(Double.parseDouble(testres.get(1)), lon);
		assertFalse(testres.get(2).equals(""));
	}
	
	public void testBuildParseGPX()
	{
		buildAndParseGPX(-99999.999999, -99999.999999);
		buildAndParseGPX(0.0, 0.0);
		buildAndParseGPX(33.022482, -117.200775);
		buildAndParseGPX(9999.999999, 9999.999999);
	}

	public void testParseGPX()
	{
		ArrayList<String> testres = GPX.parseGPX("");
		assertEquals(testres, null);

		testres = GPX.parseGPX("<trkpt lat=\"46.57608333\" lon=\"8.89241667\"><ele>2376</ele><time>2007-10-14T10:09:57Z</time></trkpt>");
		assertEquals(Double.parseDouble(testres.get(0)), 46.57608333);
		assertEquals(Double.parseDouble(testres.get(1)), 8.89241667);
		assertTrue(testres.get(2).equals("2007-10-14T10:09:57Z"));
	}

}
