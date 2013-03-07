/**
 * 
 */
package edu.ucsd.mycity.localservices;

import java.util.ArrayList;

import com.google.android.maps.GeoPoint;

/**
 * @author Aaron
 *
 */
public interface LocalServiceProviderAdapter {
	/**
	 * @param center
	 * @param latSpan
	 * @param lonSpan
	 * @return
	 * ***** Use BuddyHandler.getBuddiesOnMap as a reference ******
	 */
	ArrayList<LocalServiceItem> getLocalServices(GeoPoint center, int latSpan, int lonSpan);
	
}
