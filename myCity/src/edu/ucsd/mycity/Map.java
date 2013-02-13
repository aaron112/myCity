package edu.ucsd.mycity;

import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class Map extends MapActivity implements LocationListener{
	// This is MainActivity
	final String TAG = "MainActivity";
	
	private SharedPreferences prefs;
	
	private MapController mapController;
	private MapView mapView;
	
	private LocationManager locationManager;
	private GeoPoint currentPoint;
	private Location currentLocation = null;
	
	private MyOverlay currPos= null;
	
	private Button refreshBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		prefs = PreferenceManager.getDefaultSharedPreferences( getApplicationContext() );
		GTalkHandler.context = getApplicationContext();
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
		mapView = (MapView)findViewById(R.id.mapView);
		mapView.setBuiltInZoomControls(true);
		mapController = mapView.getController();
		mapController.setZoom(18);
		
		getLastLocation();
		animateToCurrentLocation();

		drawCurrPositionOverlay();
		
        refreshBtn = (Button) findViewById(R.id.updateLocation);
        refreshBtn.setOnClickListener(new View.OnClickListener() {   
            @Override
            public void onClick(View v) {
            	animateToCurrentLocation();
            }
        });
        
        if ( prefs.getBoolean("gtalk_autologin", true) && checkConfig() ) {
		    GTalkConnect();
        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_map, menu);
		return true;
	}

	@Override
	/**
	 * 
	 */
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public void getLastLocation(){
	    String provider = getBestProvider();
	    currentLocation = locationManager.getLastKnownLocation(provider);
	    if(currentLocation != null){
	        setCurrentLocation(currentLocation);
	    }
	    else
	    {
	        Toast.makeText(this, "Location not yet acquired", Toast.LENGTH_LONG).show();
	    }
	}
	public void animateToCurrentLocation(){
	    if(currentPoint!=null){
	        mapController.animateTo(currentPoint);
	    }
	}
	public String getBestProvider(){
	    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    Criteria criteria = new Criteria();
	    criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
	    criteria.setAccuracy(Criteria.ACCURACY_FINE);
	    String bestProvider = locationManager.getBestProvider(criteria, true);
	    return bestProvider;
	}
	public void setCurrentLocation(Location location){
	    int currLatitude = (int) (location.getLatitude()*1E6);
	    int currLongitude = (int) (location.getLongitude()*1E6);
	    currentPoint = new GeoPoint(currLatitude,currLongitude);
	    currentLocation = new Location("");
	    currentLocation.setLatitude(currentPoint.getLatitudeE6() / 1e6);
	    currentLocation.setLongitude(currentPoint.getLongitudeE6() / 1e6);
	}

	@Override
	public void onLocationChanged(Location location) {
		setCurrentLocation(location);
		drawCurrPositionOverlay();
		//animateToCurrentLocation();
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void onResume() {
	    super.onResume();
	    locationManager.requestLocationUpdates(getBestProvider(), 1000, 1, this);
	    
	    //if ( checkConfig() && !GTalkHandler.connection.isConnected() ) {
	    //	GTalkConnect();
	    //}
	}
	
	@Override
	protected void onPause() {
	    super.onPause();
	    locationManager.removeUpdates(this);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		//
	}
	
	public void drawCurrPositionOverlay(){
	    List<Overlay> overlays = mapView.getOverlays();
	    overlays.remove(currPos);
	    Drawable marker = getResources().getDrawable(R.drawable.mylocation);
	    currPos = new MyOverlay(marker,mapView);
	    if(currentPoint!=null){
	        OverlayItem overlayitem = new OverlayItem(currentPoint, "Me", "Here I am!");
	        currPos.addOverlay(overlayitem);
	        overlays.add(currPos);
	        currPos.setCurrentLocation(currentLocation);
	    }
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	    case R.id.menu_buddyList:
	    	startActivity(new Intent(this, BuddyList.class));
	    	return true;
	    
	    case R.id.menu_forceupdate:
	    	// TODO: Force update
    		Toast.makeText(this, "Force Update Invoked. (TODO)", Toast.LENGTH_LONG).show();
	    	return true;
	    	
	    case R.id.menu_settings:
	    	startActivity(new Intent(this, SettingsActivity.class));
	    	return true;
	    	
	    case R.id.menu_login:
	    	if ( GTalkHandler.isAuthenticated() )
	    		Toast.makeText(this, "Already Logged in!", Toast.LENGTH_LONG).show();
	    	else {
	    		if ( checkConfig() ) {
	    	    	GTalkConnect();
	    	    }
	    	}
	    	return true;
	    
	    case R.id.menu_logout:
	    	try {
				GTalkHandler.stopService();
		    } catch (Exception e) {

		    }
	    	Toast.makeText(this, "Logged out of Google Talk.", Toast.LENGTH_LONG).show();
	    	return true;
	    
	    case R.id.menu_chat:
	    	// TODO: DEBUG ONLY
	    	Intent intent = new Intent(this, ChatActivity.class);
	    	Bundle b = new Bundle();
	    	b.putString("contact", "kicked1102@gmail.com");
	    	intent.putExtras(b);
	    	startActivity(intent);
	    	
	    	return true;
	    	
	    default:
	    	return super.onOptionsItemSelected(item);
	    }
	}
	
	
	
	// Helpers -------------------------------------------------------------
	public void GTalkConnect() {
		final ProgressDialog dialog = ProgressDialog.show(this, "Connecting...", "Please wait...", false);
		
	    Thread t = new Thread(new Runnable() {
	      	public void run() {
	      		GTalkHandler.connect();
	    		//GTalkHandler.updateRoaster();
	    		dialog.dismiss();
	    	}
	    });
	    
	    t.start();
	    dialog.show();
	  }
	  
	// Returns true if config is okay.
	private boolean checkConfig() {
		// Check pref for username and password, if undefined, direct user to Settings activity.
        if ( prefs.getString("gtalk_username", "").equals("") || prefs.getString("gtalk_password", "").equals("") ) {
        	// Directs user to Settings activity
        	Toast.makeText(this, "Please Specify Google Talk Username and Password", Toast.LENGTH_LONG).show();
        	startActivity(new Intent(this, SettingsActivity.class));
        	
        	return false;
        }
        
        return true;
	}
	
}