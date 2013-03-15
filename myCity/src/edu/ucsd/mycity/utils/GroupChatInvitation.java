package edu.ucsd.mycity.utils;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GroupChatInvitation {
	// Pattern: <groupchatinv chatroom=""><msg></msg></groupchatinv>
	
	private static final Pattern gcinvtag = Pattern.compile("<groupchatinv\\b[^>]*chatroom=\"[^>]*>");
	private static final Pattern chatroom = Pattern.compile("chatroom=\"[^>]*?\"");
	private static final Pattern msgtag = Pattern.compile("<msg>[^>]*</msg>");
	
	public static HashMap<String, String> parseRequest(String input) {
		HashMap<String, String> res = new HashMap<String, String>();
		
		Matcher tagmatch = gcinvtag.matcher(input);
		if (tagmatch.find()) {
			Matcher matcher = chatroom.matcher(tagmatch.group());
			matcher.find();
			res.put("chatroom", matcher.group().replaceFirst("chatroom=\"", "").replaceFirst("\"", "") );
		} else {
			return null;
		}
		
		tagmatch = msgtag.matcher(input);
		if (tagmatch.find()) {
			res.put( "msg", tagmatch.group().replaceFirst("<msg>", "").replaceFirst("</msg>", "") );
		} else {
			return null;
		}
		
		return res;
	}
	
	public static String buildRequest(String chatroom, String msg) {
		// Pattern: <groupchatinv chatroom=""><msg></msg></groupchatinv>
		String res = "<groupchatinv chatroom=\""+chatroom+"\"><msg>"+msg+"</msg></groupchatinv>";
		
		return res;
	}
}
