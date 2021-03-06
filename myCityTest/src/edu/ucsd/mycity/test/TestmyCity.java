package edu.ucsd.mycity.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TestmyCity
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite(TestmyCity.class.getName());
		// $JUnit-BEGIN$
		suite.addTestSuite(TestGPX.class);
		suite.addTestSuite(TestGTalkService.class);
		suite.addTestSuite(TestMap.class);
		suite.addTestSuite(TestUserCont.class);
		suite.addTestSuite(TestPlaces.class);
		suite.addTestSuite(TestLocalServiceInvitation.class);
		// $JUnit-END$
		return suite;
	}

}
