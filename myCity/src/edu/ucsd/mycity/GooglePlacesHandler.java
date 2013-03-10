package edu.ucsd.mycity;

import java.util.List;

import org.apache.http.client.HttpResponseException;
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
import edu.ucsd.mycity.localservices.GPlaceList;

@SuppressWarnings("deprecation")
public class GooglePlacesHandler {
	private final static String TAG = "PlacesHandler";
	private final static String API_KEY = "AIzaSyAub7JvkLSud0aJVeZbRlgrFtQRupkqgos";
	private static final String PLACES_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/search/json?";
	//private final static String KEY = "AIzaSyDsjGIZwOHvaALxTbI4jYMMqrlmDtE2_nQ";
	//private final static String SENSOR = "false";
	//private final static String URL = "https://maps.googleapis.com/maps/api/place/";
	
	/** Global instance of the HTTP transport. */
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

	
	private static GPlaceList placesList;
	
	public static List<GPlace> getLocalServices() {
		if (placesList != null)
			return placesList.results;
		
		return null;
	}
	
	public static boolean updateLocalServices(GeoPoint center, int latSpan, int lonSpan, String types) {
		double latitude = center.getLatitudeE6() / 1E6;
		double longitude = center.getLongitudeE6() / 1E6;
		
		int radius = (int) Math.sqrt( Math.pow( latSpan/2, 2 ) + Math.pow( lonSpan/2, 2 ) );
 
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
            // Check log cat for places response status
            Log.d(TAG, "" + placesList.status);
        } catch (HttpResponseException e) {
            Log.e(TAG, "Error: " + e.getMessage());
            return false;
        } catch (Exception e) {
        	Log.e(TAG, "Error: " + e.toString());
        }
        
        return true;
    }
	
	/**
     * Creating http request Factory
     * */
    public static HttpRequestFactory createRequestFactory(final HttpTransport transport) {
        return transport.createRequestFactory(new HttpRequestInitializer() {
            public void initialize(HttpRequest request) {
                GoogleHeaders headers = new GoogleHeaders();
                headers.setApplicationName("AndroidHive-Places-Test");
                request.setHeaders(headers);
                JsonHttpParser parser = new JsonHttpParser(new JacksonFactory());
                request.addParser(parser);
            }
        });
    }
	
	/*
	public static boolean updateLocalServices(GeoPoint center, int latSpan, int lonSpan) {

		placesList = new ArrayList<LocalServiceItem>();
		LocalServiceItem item;
		
		double latitude = center.getLatitudeE6() / 1E6;
		double longitude = center.getLongitudeE6() / 1E6;
		
		int radius = (int) Math.sqrt( Math.pow( latSpan/2, 2 ) + Math.pow( lonSpan/2, 2 ) );
		
		String placesURL = URL + "search/json?location=" + latitude + "," + longitude + "&radius=" + radius
				+ "&key=" + KEY + "&sensor=" + SENSOR;
		String detailedURL;
		
		JSONArray searchArray = searchForPlaces( placesURL );
		ArrayList<String> types;
		String name = "";
		String phone = "";
		String address = "";
		GeoPoint location;
		
		Log.i(TAG, searchArray.toString());
			
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
				Log.e( TAG, "Error in parsing JSON in for loop: " + e.toString() );
				return false;
			}
		}
		
		return true;
	}
	*/
    /*
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
				Log.e( TAG, "Error in parsing JSON in searchForPlaces: " + e.toString() );
			}
			
		} catch ( ClientProtocolException e ) {
			Log.e(TAG, "ClientProtocolException thrown while trying to Connect to Places API: " + e.toString() );
		} catch( IOException e ) {
			Log.e(TAG, "IOException thrown while trying to Connect to Places API: " + e.toString() );
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
				Log.e( TAG, "Error in parsing JSON in detailedPlaces: " + e.toString() );
			}
				
		} catch ( ClientProtocolException e ) {
			Log.e(TAG, "ClientProtocolException thrown while trying to Connect to Places API: " + e.toString() );
		} catch( IOException e ) {
			Log.e(TAG, "IOException thrown while trying to Connect to Places API: " + e.toString() );
		}
		
		return null;
	}
	*/
}
