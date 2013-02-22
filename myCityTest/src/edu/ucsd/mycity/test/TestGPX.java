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

	public void testBuildGPX()
	{
		ArrayList<String> test;
		String testXML = GPX.buildGPX(33.022482, -117.200775, 1234, 0);
		test = GPX.parseGPX(testXML);
		assertNotNull(test);
		assertEquals(test.size(), 3);
	}

	public void testBuildParseGPX()
	{
		ArrayList<String> test = GPX.parseGPX("");
		assertEquals(test, null);
	}

}
