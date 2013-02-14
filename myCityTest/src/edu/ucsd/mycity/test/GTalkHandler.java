package edu.ucsd.mycity.test;

import org.jivesoftware.smack.XMPPConnection;

import android.test.AndroidTestCase;
import edu.ucsd.mycity.MySmack;

public class GTalkHandler extends AndroidTestCase
{
	public void testConnection()
	{
		XMPPConnection test = MySmack.connectToGtalk(
		         "cse110winter2013@gmail.com", "billgriswold");
		assertEquals(test.isConnected(), true);
	}
}
