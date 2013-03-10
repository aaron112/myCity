package edu.ucsd.mycity.utils;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocalServiceInvitation {
	// Pattern: <locinv name="" address="" lat="" lon=""><msg></msg></locinv>
	
	private static final Pattern locinvtag = Pattern.compile("<locinv\\b[^>]*name=\"[^>]*>");
	private static final Pattern name = Pattern.compile("name=\"[^>]*?\"");
	private static final Pattern address = Pattern.compile("address=\"[^>]*?\"");
	private static final Pattern lat = Pattern.compile("lat=\"[^>]*?\"");
	private static final Pattern lon = Pattern.compile("lon=\"[^>]*?\"");
	private static final Pattern msgtag = Pattern.compile("<msg>[^>]*</msg>");
	
	public static HashMap<String, String> parseRequest(String input) {
		HashMap<String, String> res = new HashMap<String, String>();
		
		Matcher tagmatch = locinvtag.matcher(input);
		if (tagmatch.find()) {
			Matcher matcher = name.matcher(tagmatch.group());
			matcher.find();
			res.put("name", matcher.group().replaceFirst("name=\"", "").replaceFirst("\"", "") );
			
			matcher = address.matcher(tagmatch.group());
			matcher.find();
			res.put( "address", matcher.group().replaceFirst("address=\"", "").replaceFirst("\"", "") );
			
			matcher = lat.matcher(tagmatch.group());
			matcher.find();
			res.put( "lat", matcher.group().replaceFirst("lat=\"", "").replaceFirst("\"", "") );
			
			matcher = lon.matcher(tagmatch.group());
			matcher.find();
			res.put( "lon", matcher.group().replaceFirst("lon=\"", "").replaceFirst("\"", "") );
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
	
	public static String buildRequest(String name, String address, int lat, int lon, String msg) {
		// Pattern: <locinv name="" address="" lat="" lon=""><msg></msg></locinv>
		String res = "<locinv name=\""+name+"\" address=\""+address+"\" lat=\""+lat+"\" lon=\""+lon+"\"><msg>"+msg+"</msg></locinv>";
		
		return res;
	}
	
	

}
