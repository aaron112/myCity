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
import com.google.android.maps.OverlayItem;
import com.readystatesoftware.mapviewballoons.BalloonItemizedOverlay;

public class PinsOverlay extends BalloonItemizedOverlay<OverlayItem>
{

	private ArrayList<OverlayItem> overlays = new ArrayList<OverlayItem>();

	public PinsOverlay(Drawable defaultMarker, MapView mapView)
	{
		super(boundCenter(defaultMarker), mapView);
		boundCenter(defaultMarker);
	}

	@Override
	protected OverlayItem createItem(int i)
	{
		return overlays.get(i);
	}

	@Override
	public int size()
	{
		return overlays.size();
	}

	public void addOverlay(OverlayItem overlay)
	{
		overlays.add(overlay);
		populate();
	}
}
