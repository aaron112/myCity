package edu.ucsd.mycity.test;

import java.util.HashMap;
import android.test.AndroidTestCase;
import edu.ucsd.mycity.utils.GroupChatInvitation;

public class TestGroupChatInvitation extends AndroidTestCase
{
	// Pattern: <groupchatinv chatroom=""><msg></msg></groupchatinv>
	
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
	
	private void buildAndParseInv(String chatroom, String msg) {
		HashMap<String, String> testres;
		
		String testParse = GroupChatInvitation.buildRequest(chatroom, msg);
		testres = GroupChatInvitation.parseRequest(testParse);
		
		assertTrue( testres.get("chatroom").equals(chatroom) );
		assertTrue( testres.get("msg").equals(msg) );
	}

	public void testBuildParseInv()
	{
		buildAndParseInv("private-chat-FF68D720-8DC9-11E2-9E96-0800200C9A66@groupchat.google.com", "Let's talk!");
		buildAndParseInv("", "Let's talk!");
		buildAndParseInv("private-chat-FF68D720-8DC9-11E2-9E96-0800200C9A66@groupchat.google.com", "");
		buildAndParseInv("", "");
	}

	
	public void testParseInv()
	{
		HashMap<String, String> testres = GroupChatInvitation.parseRequest("");
		assertEquals(testres, null);

		testres = GroupChatInvitation.parseRequest(
				"<groupchatinv chatroom=\"private-chat-FF68D720-8DC9-11E2-9E96-0800200C9A66@groupchat.google.com\"><msg>Let's talk!</msg></groupchatinv>");
		
		assertNotNull(testres);
		assertTrue(testres.get("chatroom").equals("private-chat-FF68D720-8DC9-11E2-9E96-0800200C9A66@groupchat.google.com"));
		assertTrue(testres.get("msg").equals("Let's talk!"));
	}
	
}