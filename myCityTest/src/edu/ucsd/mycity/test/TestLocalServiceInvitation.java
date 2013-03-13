package edu.ucsd.mycity.test;

import java.util.ArrayList;
import java.util.HashMap;

import android.test.AndroidTestCase;

import edu.ucsd.mycity.GooglePlacesHandler;
import edu.ucsd.mycity.utils.LocalServiceInvitation;

public class TestLocalServiceInvitation extends AndroidTestCase
{
	// Pattern: <locinv name="" address="" phone="" lat="" lon=""><msg></msg></locinv>
	
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
	
	private void buildAndParseInv(String name, String address, String phone, int lat, int lon, String msg) {
		HashMap<String, String> testres;
		
		String testParse = LocalServiceInvitation.buildRequest(name, address, phone, lat, lon, msg);
		testres = LocalServiceInvitation.parseRequest(testParse);
		
		assertTrue( testres.get("name").equals(name) );
		assertTrue( testres.get("address").equals(address) );
		assertTrue( testres.get("phone").equals(phone) );
		assertEquals( Integer.parseInt(testres.get("lat")), lat );
		assertEquals( Integer.parseInt(testres.get("lon")), lon );
		assertTrue( testres.get("msg").equals(msg) );
	}

	public void testBuildParseInv()
	{
		buildAndParseInv("UCSD", "Gilman Drive", "(999) 999-9999", 111111, 222222, "Parking sucks");
		
		buildAndParseInv("", "", "", 0, 0, "");
		
		buildAndParseInv("UCSD", "", "(999) 999-9999", -1, -3, "");
		
		buildAndParseInv("", "", "(999) 999-9999", -999999999, -999999999, "");
	}

	
	public void testParseInv()
	{
		HashMap<String, String> testres = LocalServiceInvitation.parseRequest("");
		assertEquals(testres, null);

		testres = LocalServiceInvitation.parseRequest(
				"<locinv name=\"The Melting Pot\" address=\"8980 University Center Lane, San Diego\" phone=\"(858) 638-1700\" lat=\"32870044\" lon=\"-117224634\"><msg>Yummy yummy</msg></locinv>");
		
		assertNotNull(testres);
		assertTrue(testres.get("name").equals("The Melting Pot"));
		assertTrue(testres.get("address").equals("8980 University Center Lane, San Diego"));
		assertTrue(testres.get("phone").equals("(858) 638-1700"));
		assertTrue(testres.get("lat").equals("32870044"));
		assertTrue(testres.get("lon").equals("-117224634"));
		assertTrue(testres.get("msg").equals("Yummy yummy"));
	}
	
}