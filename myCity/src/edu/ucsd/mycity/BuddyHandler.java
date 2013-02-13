package edu.ucsd.mycity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;

import android.location.Location;
import android.util.Log;

public class BuddyHandler {
	private static final String TAG = "BuddyHandler";
	static HashMap<String, BuddyEntry> buddies = new HashMap<String, BuddyEntry>();
	
	public static void loadBuddiesFromRoster(Roster roster) {
		Log.d(TAG, "loadBuddiesFromRoster");
		
		Collection<RosterEntry> entries = roster.getEntries();
        for (RosterEntry entry : entries) {
        	String bareAddr = StringUtils.parseBareAddress(entry.getUser());
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
	
	public static void setIsMyCityUser(String bareAddr) {
		if ( buddies.containsKey(bareAddr) ) {
			buddies.get(bareAddr).setMyCityUser(true);
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
	
	public static ArrayList<BuddyEntry> getNearbyBuddies(Location loc, float maxDistance) {
		ArrayList<BuddyEntry> res = new ArrayList<BuddyEntry>();
		for (BuddyEntry buddy : buddies.values()) {
			if ( loc.distanceTo(buddy.getLocation()) <= maxDistance ) {
				res.add(buddy);
			}
		}
		
		return res;
	}
}
