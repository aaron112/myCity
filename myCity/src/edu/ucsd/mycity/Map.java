package edu.ucsd.mycity;

import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
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

import edu.ucsd.mycity.maptrack.OnMapViewChangeListener;
import edu.ucsd.mycity.maptrack.TrackedMapView;

public class Map extends MapActivity implements LocationClient, OnMapViewChangeListener {
	// This is MainActivity
	final String TAG = "MainActivity";
	
	private SharedPreferences prefs;
	
	private MapController mapController;
	private TrackedMapView mapView;
	
	private GeoPoint currentPoint;
	private Location currentLocation = null;
	
	private MyOverlay currPos= null;
	
	private Button refreshBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);
	    
		prefs = PreferenceManager.getDefaultSharedPreferences( getApplicationContext() );
		GTalkHandler.context = getApplicationContext();
		GTalkHandler.startService();
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
		mapView = (TrackedMapView)findViewById(R.id.mapView);
		mapView.setBuiltInZoomControls(true);
		mapView.setOnChangeListener(this);
		
		mapController = mapView.getController();
		mapController.setZoom(18);
		
		updateLocation();
		animateToCurrentLocation();
		
        refreshBtn = (Button) findViewById(R.id.updateLocation);
        refreshBtn.setOnClickListener(new View.OnClickListener() {   
            @Override
            public void onClick(View v) {
            	updateLocation();
            	animateToCurrentLocation();
            }
        });
        
        if ( prefs.getBoolean("gtalk_autologin", true) && checkConfig() ) {
		    GTalkConnect();
        } else {
        	Toast.makeText(this, "Currently in Offline Mode (Auto login disabled)", Toast.LENGTH_LONG).show();
        }
	}

	@Override
	protected void onResume() {
	    Log.i(TAG, "onResume");
	    super.onResume();
	    GTalkHandler.registerLocationClient(this);
	}
	
	@Override
	protected void onPause() {
	    Log.i(TAG, "onResume");
	    GTalkHandler.removeLocationClient(this);
	    super.onPause();
	}
	
	@Override
	protected void onDestroy() {
	    Log.i(TAG, "onDestroy");
	    GTalkHandler.removeLocationClient(this);
		super.onDestroy();
		//
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_map, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	    case R.id.menu_chat:
	    	if ( !GTalkHandler.getChatsList().isEmpty() ) {
		    	Intent intent = new Intent(this, ChatActivity.class);
		    	Bundle b = new Bundle();
		    	b.putString("contact", "");
		    	intent.putExtras(b);
		    	startActivity(intent);
	    	} else {
	    		Toast.makeText(this, "Start a new conversation from Map or Contact List", Toast.LENGTH_SHORT).show();
	    	}
	    	return true;
	    	
	    case R.id.menu_buddyList:
	    	startActivity(new Intent(this, BuddyList.class));
	    	return true;
	    
	    case R.id.menu_forceupdate:
	    	GTalkHandler.probeUser(null);
    		Toast.makeText(this, "Force Update Invoked", Toast.LENGTH_LONG).show();
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
	    	GTalkHandler.disconnect();
	    	Toast.makeText(this, "Logged out of Google Talk", Toast.LENGTH_LONG).show();
	    	return true;
	    	
	    default:
	    	return super.onOptionsItemSelected(item);
	    }
	}
	
	
	@Override
	public void onMapViewChange(MapView mapView, GeoPoint newCenter,
			GeoPoint oldCenter, int newZoom, int oldZoom) {
		Log.d(TAG, "onMapViewChange!");
		// TODO Redraw pins
	}
	
	public void updateLocation(){
		currentLocation = GTalkHandler.getLastKnownLocation();
	    if(currentLocation != null) {
	        setCurrentLocation(currentLocation);
			drawCurrPositionOverlay();
	    } else {
	        Toast.makeText(this, "Location not yet acquired", Toast.LENGTH_SHORT).show();
	    }
	}
	
	public void animateToCurrentLocation() {
	    if(currentPoint!=null){
	        mapController.animateTo(currentPoint);
	    }
	}
	
	
	public void setCurrentLocation(Location location){
	    int currLatitude = (int) (location.getLatitude()*1E6);
	    int currLongitude = (int) (location.getLongitude()*1E6);
	    currentPoint = new GeoPoint(currLatitude,currLongitude);
	    currentLocation = new Location("");
	    currentLocation.setLatitude(currentPoint.getLatitudeE6() / 1e6);
	    currentLocation.setLongitude(currentPoint.getLongitudeE6() / 1e6);
	}
	
	public void drawCurrPositionOverlay(){
	    List<Overlay> overlays = mapView.getOverlays();
	    overlays.remove(currPos);
	    Drawable marker = getResources().getDrawable(R.drawable.map_pointer);
	    currPos = new MyOverlay(marker,mapView);
	    if(currentPoint!=null){
	        OverlayItem overlayitem = new OverlayItem(currentPoint, "Me", GTalkHandler.getUserBareAddr());
	        currPos.addOverlay(overlayitem);
	        overlays.add(currPos);
	        currPos.setCurrentLocation(currentLocation);
	    }
	}
	
	
	// Helpers -------------------------------------------------------------
	public void GTalkConnect() {
		final ProgressDialog dialog = ProgressDialog.show(this, "Connecting...", "Please wait...", false);
		
	    Thread t = new Thread(new Runnable() {
	      	public void run() {
	      		GTalkHandler.connect();
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

	@Override
	public void onLocationUpdate(Location location) {
		setCurrentLocation(location);
		animateToCurrentLocation();
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}