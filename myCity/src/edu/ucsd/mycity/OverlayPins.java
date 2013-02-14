package edu.ucsd.mycity;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.readystatesoftware.mapviewballoons.BalloonItemizedOverlay;

public class OverlayPins extends BalloonItemizedOverlay<OverlayItem> {
	
	private Context mContext;
	private ArrayList<OverlayItem> overlays = new ArrayList<OverlayItem>();
	
	public OverlayPins(Drawable defaultMarker, MapView mapView) {
	    super(boundCenter(defaultMarker), mapView);
	    boundCenter(defaultMarker);
	    mContext = mapView.getContext();
	}
	
	@Override
	protected OverlayItem createItem(int i) {
	    // TODO Auto-generated method stub
	    return overlays.get(i);
	}
	
	@Override
	    public int size() {
	    // TODO Auto-generated method stub
	    return overlays.size();
	}
	
	public void addOverlay(OverlayItem overlay) {
		overlays.add(overlay);
	    populate();
	}
	
	@Override
	protected boolean onBalloonTap(int index, OverlayItem item) {
	    Toast.makeText(mContext, "Overlay Item " + index + " tapped!",
	            Toast.LENGTH_LONG).show();
	    return true;
	}
	

}
