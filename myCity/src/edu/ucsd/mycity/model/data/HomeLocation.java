package edu.ucsd.mycity.model.data;

import com.google.android.maps.GeoPoint;

public class HomeLocation extends GeoPoint {
   
    private static HomeLocation location = new HomeLocation (0, 0);
   
    public static HomeLocation getHomeLocation() {
        return location;
    }

    private HomeLocation(int lat1e6, int lon1e6) {
        super(lat1e6, lon1e6);
    }
   
    public static void setLocation(double lat, double lon) {
        location = new HomeLocation((int)(lat*1e6), (int)(lon*1e6));
    }
}