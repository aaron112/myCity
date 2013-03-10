package edu.ucsd.mycity.buddy;

/**
 * BuddyOverlayItem.java - Customized overlay item for buddies
 * 
 * CSE110 Project - myCity
 * 
 * Team Members:
 * Yip-Ming Wong (Aaron)
 * Yui-Yan Chan (Ryan)
 * Ryan Khalili
 * Elliot Yaghoobia
 * Jonas Kabigting
 * Michael Lee
 * 
 */

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class BuddyOverlayItem extends OverlayItem {
	private BuddyEntry buddyEntry = null;
	
	// ------- Change here to change this display text on bubble -----
	public BuddyOverlayItem(GeoPoint gp, BuddyEntry buddyEntry) {
		super(gp, buddyEntry.getName(), buddyEntry.getPresence().toString());
		this.buddyEntry = buddyEntry;
	}
	
	public BuddyOverlayItem(GeoPoint gp, String line1, String line2) {
		super(gp, line1, line2);
	}
	
	public BuddyEntry getBuddyEntry() {
		return this.buddyEntry;
	}
}
