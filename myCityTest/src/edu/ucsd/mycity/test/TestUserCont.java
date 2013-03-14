package edu.ucsd.mycity.test;

import java.util.ArrayList;
import java.util.Iterator;

import android.test.AndroidTestCase;

import com.google.android.maps.GeoPoint;

import edu.ucsd.mycity.GTalkHandler;
import edu.ucsd.mycity.UserContHandler;
import edu.ucsd.mycity.usercontent.UserContEntry;

public class TestUserCont extends AndroidTestCase
{

	public void testupdateContent()
	{
		assertTrue(UserContHandler.updateContent());
	}

	public void testgetContent()
	{
		if (!GTalkHandler.isConnected())
			GTalkHandler.connect();
		assertTrue(UserContHandler.updateContent());
		assertNotNull(UserContHandler.getContent());
	}

	public void testgetImage()
	{
		assertNotNull(UserContHandler
		         .getImageFromWeb("AMIfv97fLC0tv_8XUX5wlgzJ-gRLLnjKQruI2I87Qa04DpwqJuvl2nn2tZiYrC21HqSXCZu0ayY9l0m4R57f9xyuSvyRFUZDluCgmn3NvDgpUUw-i49LOisuQyzH17s5IsJRQgBGctnHvsWEJIJ-BLMRe4rgTeCW5jP52BnytwVxnLFddIr6qxE"));
	}

	public void testgetMoreContent()
	{
		assertTrue(UserContHandler.updateContent());
		ArrayList<UserContEntry> content = UserContHandler.getContent();

		Iterator<UserContEntry> it = content.iterator();

		while (it.hasNext())
		{
			UserContEntry test = it.next();
			assertNotNull(test);
			assertNotNull(test.getDescription());
			assertNotNull(test.getLocation());
			assertNotNull(test.getName());
			assertNotNull(test.getUser());

		}
	}

	public void testgetRadiusContent()
	{
		assertTrue(UserContHandler.updateContent());

		for (int i = 0; i < 20; i++)
		{
			ArrayList<UserContEntry> content = UserContHandler.getContentOnMap(
			         new GeoPoint(37421701, -122084216), i, i);

			Iterator<UserContEntry> it = content.iterator();

			while (it.hasNext())
			{
				UserContEntry test = it.next();
				assertNotNull(test);
				assertNotNull(test.getDescription());
				assertNotNull(test.getLocation());
				assertNotNull(test.getName());
				assertNotNull(test.getUser());

			}
		}
	}

}
