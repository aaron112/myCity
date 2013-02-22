package edu.ucsd.mycity.test;

import android.test.AndroidTestCase;
import edu.ucsd.mycity.GTalkService;

public class TestGTalkService extends AndroidTestCase
{
	private GTalkService mService;

	protected void setUp() throws Exception
	{
		super.setUp();
		mService = new GTalkService();

	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}

	public void testisConnected()
	{
		assertFalse(mService.isConnected());
	}

	public void testisAuthenticated()
	{
		assertFalse(mService.isAuthenticated());
	}

}
