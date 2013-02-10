package edu.ucsd.mycity.controller;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;

public class LocationController {
   
    private MapController mapController;
    private GeoPoint point;
   
    public LocationController(MapController mapController, GeoPoint point) {
        this.mapController = mapController;
        this.point = point;
    }
   
    public void MoveHome() {
        mapController.animateTo(point);
    }
   
}