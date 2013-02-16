package edu.ucsd.mycity;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.maps.MapView;
import com.readystatesoftware.mapviewballoons.BalloonItemizedOverlay;

public class PinsOverlay extends BalloonItemizedOverlay<BuddyOverlayItem> {
	
	private Context mContext;
	private ArrayList<BuddyOverlayItem> overlays = new ArrayList<BuddyOverlayItem>();
	
	public PinsOverlay(Drawable defaultMarker, MapView mapView) {
	    super(boundCenter(defaultMarker), mapView);
	    boundCenter(defaultMarker);
	    mContext = mapView.getContext();
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
	
	@Override
	protected boolean onBalloonTap(int index, BuddyOverlayItem item) {
		if ( item.getBuddyEntry() == null )
			return false;
		
	    // Start ChatActivity
		Intent i = new Intent(mContext, ChatActivity.class);
		Bundle bundle = new Bundle();

		bundle.putString("contact", item.getBuddyEntry().getUser());
		i.putExtras(bundle);

		mContext.startActivity(i);
		
	    return true;
	}
	

}
