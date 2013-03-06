package edu.ucsd.mycity.maptrack;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

public interface OnLongpressListener
{
	public void onLongpress(MapView view, GeoPoint longpressLocation);
}
