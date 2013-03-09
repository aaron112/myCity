package edu.ucsd.mycity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class BuddyPinsOverlay extends PinsOverlay
{

	private Context mContext;

	public BuddyPinsOverlay(Drawable defaultMarker, MapView mapView)
	{
		super(defaultMarker, mapView);
		mContext = mapView.getContext();
	}

	// ------ Change this method to change it's onClick behavior --------
	protected boolean onBalloonTap(int index, OverlayItem item)
	{
		BuddyOverlayItem buddy_item = (BuddyOverlayItem) item;

		if (buddy_item.getBuddyEntry() == null)
			return false;

		// Start ChatActivity
		Intent i = new Intent(mContext, ChatActivity.class);
		Bundle bundle = new Bundle();

		bundle.putString("contact", buddy_item.getBuddyEntry().getUser());
		i.putExtras(bundle);

		mContext.startActivity(i);

		return true;
	}
}
