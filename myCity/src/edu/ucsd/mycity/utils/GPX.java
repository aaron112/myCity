package edu.ucsd.mycity.utils;

import android.annotation.SuppressLint;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GPX {

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
	
	@SuppressLint("SimpleDateFormat")
	public static String buildGPX(double lat, double lon, double alt, long time) {
		// Build GPX Message:
		DecimalFormat dForm = new DecimalFormat("###.######");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		String message = "<trkpt lat=\""+dForm.format(lat)+"\" lon=\""+dForm.format(lon)+"\">";
		message += "<ele>"+alt+"</ele>";
		message += "<time>"+sdf.format(new Date(time))+"</time></trkpt>";
		
		return message;
	}
}
