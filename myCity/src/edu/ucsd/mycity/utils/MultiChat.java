package edu.ucsd.mycity.utils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultiChat {
	private static final Pattern fromtag = Pattern.compile("<from>[^>]*</from>");
	
	// Returns array: 0: From user, 1: Message with from tags removed.
	public static ArrayList<String> parseFrom(String input) {
		ArrayList<String> res = new ArrayList<String>();
		String matchedFrom;
		
		Matcher tagmatch = fromtag.matcher(input);
		if (tagmatch.find()) {
			matchedFrom = tagmatch.group().replaceFirst("<from>", "").replaceFirst("</from>", "");
			res.add( 0, matchedFrom );
			res.add( 1, tagmatch.group().replaceFirst("<from>" + matchedFrom + "</from>", "") );
			return res;
		}
		
		return null;
	}
}
