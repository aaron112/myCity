package edu.ucsd.mycity;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GPXParser {

	private static final Pattern trkpttag = Pattern.compile("<trkpt\\b[^>]*lat=\"[^>]*>");
	private static final Pattern lat = Pattern.compile("lat=\"[^>]*?\"");
	private static final Pattern lon = Pattern.compile("lon=\"[^>]*?\"");
	private static final Pattern timetag = Pattern.compile("<time>[^>]*</time>");
	
	public static ArrayList<String> parseGPX(String input) {
		ArrayList<String> res = new ArrayList<String>();
		
		Matcher tagmatch = trkpttag.matcher(input);
		if (tagmatch.find()) {
			Matcher matcher = lat.matcher(tagmatch.group());
			matcher.find();
			res.add( 0, matcher.group().replaceFirst("lat=\"", "").replaceFirst("\"", "") );
			
			matcher = lon.matcher(tagmatch.group());
			matcher.find();
			res.add( 1, matcher.group().replaceFirst("lon=\"", "").replaceFirst("\"", "") );
		} else {
			return null;
		}
		
		tagmatch = timetag.matcher(input);
		if (tagmatch.find()) {
			res.add( 2, tagmatch.group().replaceFirst("<time>", "").replaceFirst("</time>", "") );
		} else {
			return null;
		}
		
		return res;
	}
}
