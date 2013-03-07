package edu.ucsd.mycity;

/**
 * PinsOverlay.java - Customized Overlay class for BuddyOverlayItem
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

import java.util.ArrayList;

import android.graphics.drawable.Drawable;

import com.google.android.maps.MapView;
import com.readystatesoftware.mapviewballoons.BalloonItemizedOverlay;

public class PinsOverlay extends BalloonItemizedOverlay<BuddyOverlayItem> {
	
	private ArrayList<BuddyOverlayItem> overlays = new ArrayList<BuddyOverlayItem>();
	
	public PinsOverlay(Drawable defaultMarker, MapView mapView) {
	    super(boundCenter(defaultMarker), mapView);
	    boundCenter(defaultMarker);
	}
	
	@Override
	protected BuddyOverlayItem createItem(int i) {
	    return overlays.get(i);
	}
	
	@Override
	    public int size() {
	    return overlays.size();
	}
	
	public void addOverlay(BuddyOverlayItem overlay) {
		overlays.add(overlay);
	    populate();
	}
}
