package edu.ucsd.mycity.test;

import android.test.ActivityInstrumentationTestCase2;

import com.google.android.maps.MapActivity;

import edu.ucsd.mycity.Map;

public class TestMap extends ActivityInstrumentationTestCase2<Map>
{
	private MapActivity mActivity;

	@SuppressWarnings("deprecation")
	public TestMap()
	{
		super("edu.ucsd.mycity", Map.class);
	}

	protected void setUp() throws Exception
	{
		super.setUp();
		setActivityInitialTouchMode(false);

		mActivity = getActivity();
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}

	public void testMapUI()
	{
		boolean ok = getInstrumentation().invokeMenuActionSync(mActivity,
		         edu.ucsd.mycity.R.id.menu_buddyList, 0);

		getInstrumentation().waitForIdleSync();
		assertTrue(ok);

		ok = getInstrumentation().invokeMenuActionSync(mActivity,
		         edu.ucsd.mycity.R.id.menu_chat, 0);

		getInstrumentation().waitForIdleSync();
		assertTrue(ok);

		ok = getInstrumentation().invokeMenuActionSync(mActivity,
		         edu.ucsd.mycity.R.id.menu_forceupdate, 0);

		getInstrumentation().waitForIdleSync();
		assertTrue(ok);

		ok = getInstrumentation().invokeMenuActionSync(mActivity,
		         edu.ucsd.mycity.R.id.menu_settings, 0);

		getInstrumentation().waitForIdleSync();
		assertTrue(ok);

		ok = getInstrumentation().invokeMenuActionSync(mActivity,
		         edu.ucsd.mycity.R.id.menu_login, 0);

		getInstrumentation().waitForIdleSync();
		assertTrue(ok);
	}

}
