package edu.ucsd.mycity;

import java.io.IOException;
import java.util.ArrayList;
import java.lang.Math;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.google.android.maps.GeoPoint;

import edu.ucsd.mycity.localservices.LocalServiceItem;

public class PlacesHandler {

	private final static String KEY = "&key=AIzaSyDsjGIZwOHvaALxTbI4jYMMqrlmDtE2_nQ";
	private final static String SENSOR = "&sensor=false";
	private final static String TAG = "PlacesHandler";
	private final static String URL = "https://maps.googleapis.com/maps/api/place/";
	
	private static ArrayList<LocalServiceItem> placesList;
	
	public static ArrayList<LocalServiceItem> getLocalServices() {
		return placesList;
	}
	
	public static boolean updateLocalServices(GeoPoint center, int latSpan, int lonSpan) {

		placesList = new ArrayList<LocalServiceItem>();
		LocalServiceItem item;
		
		double latitude = center.getLatitudeE6() / 1E6;
		double longitude = center.getLongitudeE6() / 1E6;
		
		int radius = (int) Math.sqrt( Math.pow( latSpan/2, 2 ) + Math.pow( lonSpan/2, 2 ) );
		
		String placesURL = URL + "search/json?location=" + latitude + "," + longitude + "&radius=" + radius + KEY + SENSOR;
		String detailedURL;
		
		JSONArray searchArray = searchForPlaces( placesURL );
		ArrayList<String> types;
		String name = "";
		String phone = "";
		String address = "";
		GeoPoint location;
			
		for( int i = 0; i < searchArray.length(); i++ ) {
			try{
				JSONObject obj = searchArray.getJSONObject( i );
				String ref = obj.getString( "reference" );
						
				detailedURL = URL + "details/json?reference=" + ref + KEY + SENSOR;
				
				JSONObject dObj = detailedPlaces( detailedURL );
				JSONObject resultObj = new JSONObject( dObj.getString( "result" ) );
				
				types = getTypes(resultObj);
				name = getName(resultObj);
				phone = getPhoneNumber(resultObj);
				address = getAddress(resultObj);
				location = getLocation(resultObj);
				item = new LocalServiceItem( types, name, phone, address, location );
				placesList.add( i, item );
				
			} catch( JSONException e ) {
				Log.d( TAG, "Error in parsing JSON in for loop" );
				return false;
			}
		}
		
		return true;
	}

	private static GeoPoint getLocation(JSONObject resultObj) throws JSONException {
		JSONObject locationObj = resultObj.getJSONObject( "geometry" ).getJSONObject( "location" );
		int lat = (int) ( locationObj.getDouble( "lat" ) * 1E6 );
		int lng = (int) ( locationObj.getDouble( "lng" ) * 1E6 );
		return new GeoPoint( lat, lng );
	}

	private static String getAddress(JSONObject resultObj) throws JSONException {
		return resultObj.getString( "formatted_address" );
	}

	private static String getPhoneNumber(JSONObject resultObj) throws JSONException {
		String phone = "";
		
		if( !resultObj.isNull( "formatted_phone_number" ) ) {
		  phone = resultObj.getString( "formatted_phone_number" );
		}
		
		return phone;
	}

	private static String getName(JSONObject resultObj) throws JSONException {
		return resultObj.getString( "name" );
	}

	private static ArrayList<String> getTypes(JSONObject resultObj) throws JSONException {
		JSONArray typesArray = resultObj.getJSONArray( "types" );
		
		ArrayList<String> types = new ArrayList<String>();
		
		for ( int j = 0; j < typesArray.length(); j++ ) {
			types.add( typesArray.getString( j ) );
		}
		return types;
	}
		
	private static JSONArray searchForPlaces( String url ) {
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet( url );
		
		try {
			HttpResponse response = client.execute( request );
			HttpEntity entity = response.getEntity();
			String results = EntityUtils.toString(entity);
			JSONObject placesJSON;
			
			try {
				placesJSON = new JSONObject( results );
				JSONArray array = placesJSON.getJSONArray( "results" );
				
				return array;
				
			} catch ( JSONException e ) {
				Log.d( TAG, "Error in parsing JSON in searchForPlaces" );
			}
			
		} catch ( ClientProtocolException e ) {
			Log.d(TAG, "ClientProtocolException thrown while trying to Connect to Places API");
		} catch( IOException e ) {
			Log.d(TAG, "IOException thrown while trying to Connect to Places API");
		}
		
		return null;
	}
	
	private static JSONObject detailedPlaces( String url ) {
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet( url );
		
		try {
			HttpResponse response = client.execute( request );
			HttpEntity entity = response.getEntity();
			String result = EntityUtils.toString(entity);
			JSONObject detailsJSON;
			
			try {
				detailsJSON = new JSONObject( result );
				
				return detailsJSON;
					
			} catch ( JSONException e ) {
				Log.d( TAG, "Error in parsing JSON in detailedPlaces" );
			}
				
		} catch ( ClientProtocolException e ) {
			Log.d(TAG, "ClientProtocolException thrown while trying to Connect to Places API");
		} catch( IOException e ) {
			Log.d(TAG, "IOException thrown while trying to Connect to Places API");
		}
		
		return null;
	}
}
