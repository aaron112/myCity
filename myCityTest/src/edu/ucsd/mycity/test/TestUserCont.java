package edu.ucsd.mycity.test;

import android.test.AndroidTestCase;
import edu.ucsd.mycity.UserContHandler;

public class TestUserCont extends AndroidTestCase
{

	public void testupdateContent()
	{
		assertTrue(UserContHandler.updateContent());
	}

	public void testgetContent()
	{
		assertTrue(UserContHandler.updateContent());
		assertNotNull(UserContHandler.getContent());
	}

	public void testgetImage()
	{
		assertNotNull(UserContHandler
		         .getImageFromWeb("AMIfv97fLC0tv_8XUX5wlgzJ-gRLLnjKQruI2I87Qa04DpwqJuvl2nn2tZiYrC21HqSXCZu0ayY9l0m4R57f9xyuSvyRFUZDluCgmn3NvDgpUUw-i49LOisuQyzH17s5IsJRQgBGctnHvsWEJIJ-BLMRe4rgTeCW5jP52BnytwVxnLFddIr6qxE"));
	}

}
