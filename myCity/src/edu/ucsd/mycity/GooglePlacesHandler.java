package edu.ucsd.mycity;

import java.util.List;

import org.apache.http.client.HttpResponseException;

import android.location.Location;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpParser;
import com.google.api.client.json.jackson.JacksonFactory;

import edu.ucsd.mycity.localservices.GPlace;
import edu.ucsd.mycity.localservices.GPlaceDetails;
import edu.ucsd.mycity.localservices.GPlaceList;

@SuppressWarnings("deprecation")
public class GooglePlacesHandler {
	private final static String TAG = "GooglePlacesHandler";
	private final static String API_KEY = "AIzaSyAub7JvkLSud0aJVeZbRlgrFtQRupkqgos";
	private static final String PLACES_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/search/json?";
	private static final String PLACES_DETAILS_URL = "https://maps.googleapis.com/maps/api/place/details/json?";
	 
	/** Global instance of the HTTP transport. */
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

	
	private static GPlaceList placesList;
	
	public static List<GPlace> getLocalServices() {
		if (placesList != null)
			return placesList.results;
		
		return null;
	}
	
	public static boolean updateLocalServices(GeoPoint centergp, int latSpan, int lonSpan, String types) {
		Log.i(TAG, "updateLocalServices init");
		
		double latitude = centergp.getLatitudeE6() / 1E6;
		double longitude = centergp.getLongitudeE6() / 1E6;
		
		Location center = new Location("");
		center.setLatitude(latitude);
		center.setLongitude(longitude);
		
		Location west = new Location("");
		west.setLatitude(centergp.getLatitudeE6() / 1E6);
		west.setLongitude( (centergp.getLongitudeE6() + (lonSpan/2)) / 1E6 );
		
		Location south = new Location("");
		south.setLatitude( (centergp.getLatitudeE6() + (latSpan/2)) / 1E6 );
		south.setLongitude(centergp.getLongitudeE6() / 1E6);
		
		int radius = (int) Math.ceil(Math.sqrt( Math.pow( center.distanceTo(west), 2 ) + Math.pow( center.distanceTo(south), 2 ) ));
		
        try {
 
            HttpRequestFactory httpRequestFactory = createRequestFactory(HTTP_TRANSPORT);
            HttpRequest request = httpRequestFactory.buildGetRequest(new GenericUrl(PLACES_SEARCH_URL));
            request.getUrl().put("key", API_KEY);
            request.getUrl().put("location", latitude + "," + longitude);
            request.getUrl().put("radius", radius); // in meters
            request.getUrl().put("sensor", "false");
            if( types != null && !types.equals("") )
                request.getUrl().put("types", types);
            
    		placesList = request.execute().parseAs(GPlaceList.class);
        } catch (HttpResponseException e) {
            Log.e(TAG, "Error: " + e.getMessage());
            return false;
        } catch (Exception e) {
        	Log.e(TAG, "Error: " + e.toString());
        }
        
        return true;
    }
	
	/**
     * Searching single place full details
     * @param refrence - reference id of place
     *                 - which you will get in search api request
     * */
    public static GPlaceDetails getPlaceDetails(String reference) throws Exception {
    	Log.d(TAG, "getPlaceDetails for ref ID: " + reference);
    	
        try {
 
            HttpRequestFactory httpRequestFactory = createRequestFactory(HTTP_TRANSPORT);
            HttpRequest request = httpRequestFactory
                    .buildGetRequest(new GenericUrl(PLACES_DETAILS_URL));
            request.getUrl().put("key", API_KEY);
            request.getUrl().put("reference", reference);
            request.getUrl().put("sensor", "false");
 
            GPlaceDetails place = request.execute().parseAs(GPlaceDetails.class);
 
            return place;
 
        } catch (HttpResponseException e) {
            Log.e(TAG, "Error in Perform Details: " + e.getMessage());
            throw e;
        }
    }
	
	/**
     * Creating http request Factory
     * */
    public static HttpRequestFactory createRequestFactory(final HttpTransport transport) {
        return transport.createRequestFactory(new HttpRequestInitializer() {
            public void initialize(HttpRequest request) {
                GoogleHeaders headers = new GoogleHeaders();
                headers.setApplicationName("My City");
                request.setHeaders(headers);
                JsonHttpParser parser = new JsonHttpParser(new JacksonFactory());
                request.addParser(parser);
            }
        });
    }
}