package edu.ucsd.mycity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;

import com.google.android.maps.GeoPoint;

import android.location.Location;
import android.util.Log;

public class BuddyHandler {
	private static final String TAG = "BuddyHandler";
	private static HashMap<String, BuddyEntry> buddies = new HashMap<String, BuddyEntry>();
	
	public static void clear() {
		buddies = new HashMap<String, BuddyEntry>();
	}
	
	public static void loadBuddiesFromRoster(Roster roster) {
		Log.d(TAG, "loadBuddiesFromRoster");
		
		Collection<RosterEntry> entries = roster.getEntries();
        for (RosterEntry entry : entries) {
        	String bareAddr = getBareAddr(entry.getUser());
        	String userName = entry.getName();
        	
        	if (userName == null)
        		userName = bareAddr;
        	
        	if ( buddies.containsKey( bareAddr ) ) {
        		// Already exists in buddies, update info if needed
        		buddies.get(bareAddr).setName( userName );
        		buddies.get(bareAddr).setPresence( roster.getPresence( entry.getUser()) );
        	} else {
        		// New entry
        		buddies.put(bareAddr, new BuddyEntry( userName, bareAddr, roster.getPresence(entry.getUser()) ));
        	}
        }
	}
	
	public static void updatePresense(String bareAddr, Presence presence) {
		Log.d(TAG, "updatePresense: " + bareAddr);
		
		if ( buddies.containsKey(bareAddr) ) {
			buddies.get(bareAddr).setPresence(presence);
		}
	}
	
	public static void updateLocation(String bareAddr, double lat, double lon, String time) {
		if ( buddies.containsKey(bareAddr) ) {
			Log.d(TAG, "updateLocation for "+bareAddr+": "+lat+", "+lon+", "+time);
			
			Location loc = new Location("XMPP");
			loc.setLatitude(lat);
			loc.setLongitude(lon);
			// time ignored
			
			buddies.get(bareAddr).setLocation(loc);
		}
	}
	
	public static ArrayList<BuddyEntry> getBuddiesOnMap(GeoPoint center, int latSpan, int lonSpan) {
		ArrayList<BuddyEntry> res = new ArrayList<BuddyEntry>();
		
		int latMin = center.getLatitudeE6()-(latSpan/2);
		int latMax = center.getLatitudeE6()+(latSpan/2);
		int lonMin = center.getLongitudeE6()-(lonSpan/2);
		int lonMax = center.getLongitudeE6()+(lonSpan/2);
		
		for (BuddyEntry buddy : buddies.values()) {
			if (buddy.getLocation() == null)
				continue;
			
			int lat = (int) (buddy.getLocation().getLatitude() * 1E6);
			int lon = (int) (buddy.getLocation().getLongitude() * 1E6);
			// Show only available myCity users
			if ( buddy.getPresence().isAvailable() && buddy.isMyCityUser() &&
				 lat >= latMin && lat <= latMax && lon >= lonMin && lon <= lonMax ) {
				res.add(buddy);
			}
		}
		
		return res;
	}
	
	public static ArrayList<BuddyEntry> getMyCityBuddies() {
		ArrayList<BuddyEntry> res = new ArrayList<BuddyEntry>();
		for (BuddyEntry buddy : buddies.values()) {
			if ( buddy.isMyCityUser() ) {
				res.add(buddy);
			}
		}
		
		return res;
	}
	
	public static ArrayList<BuddyEntry> getBuddies() {
		return new ArrayList<BuddyEntry>( buddies.values() );
	}
	
	public static BuddyEntry getBuddy(String bareAddr) {
		if ( buddies.containsKey(bareAddr) )
			return buddies.get(bareAddr);
		return null;
	}
	
	public static String getBareAddr(String addr) {
		return StringUtils.parseBareAddress(addr);
	}
}
