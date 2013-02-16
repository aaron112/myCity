package edu.ucsd.mycity;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class BuddyOverlayItem extends OverlayItem {
	
	BuddyEntry buddyEntry;
	
	public BuddyOverlayItem(GeoPoint arg0, String arg1, String arg2, BuddyEntry buddyEntry) {
		super(arg0, arg1, arg2);
		this.buddyEntry = buddyEntry;
	}
	
	public BuddyEntry getBuddyEntry() {
		return buddyEntry;
	}

}
