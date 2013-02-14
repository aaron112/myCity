package edu.ucsd.mycity.maptrack;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

public interface OnMapViewChangeListener
{
	public void onMapViewChange(MapView view, GeoPoint newCenter, GeoPoint oldCenter, int newZoom, int oldZoom);
}