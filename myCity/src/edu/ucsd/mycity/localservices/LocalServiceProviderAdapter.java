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
	 * ***** Use BuddyHandler.getBuddiesOnMap as a reference ******
	 * @param center
	 * @param latSpan
	 * @param lonSpan
	 * @return
	 */
	ArrayList<LocalServiceItem> getLocalServices(GeoPoint center, int latSpan, int lonSpan);
	
}
