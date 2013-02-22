package edu.ucsd.mycity;

import java.util.ArrayList;
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
import edu.ucsd.mycity.listeners.BuddyLocationClient;
import edu.ucsd.mycity.listeners.ConnectionClient;
import edu.ucsd.mycity.listeners.LocationClient;
import edu.ucsd.mycity.maptrack.OnMapViewChangeListener;
import edu.ucsd.mycity.maptrack.TrackedMapView;

public class Map extends MapActivity implements LocationClient, ConnectionClient, BuddyLocationClient, OnMapViewChangeListener {
	// This is MainActivity
	private final String TAG = "MainActivity";
	public static final int REFRESH_BTN_STATE_TOGGLE = -1;
	public static final int REFRESH_BTN_STATE_BROWSING = 0;
	public static final int REFRESH_BTN_STATE_FOLLOWING = 1;
	public static final int LOGIN_MENU_STATE_LOGGED_OUT = 0;
	public static final int LOGIN_MENU_STATE_LOGGED_IN = 1;
	
	private SharedPreferences prefs;
	
	private MapController mapController;
	private TrackedMapView mapView;
	
	private GeoPoint currentPoint;
	private Location currentLocation = null;
	
	private PinsOverlay currPosPin = null;
	private PinsOverlay currBuddyPins = null;

	private MenuItem loginMenuItem;
	private int loginMenuItemState = LOGIN_MENU_STATE_LOGGED_OUT;
	private Button refreshBtn;
	private int refreshBtnState;
	
	
	public void setLoginMenu(int setToState) {
		loginMenuItemState = setToState;
		
		if ( loginMenuItem == null)
			return;
		
		if (loginMenuItemState == LOGIN_MENU_STATE_LOGGED_OUT) {
			loginMenuItem.setTitle(R.string.menu_login);
		} else {
			loginMenuItem.setTitle(R.string.menu_logout);
		}
	}
	
	// setToState = -1 for TOGGLE
	private void toggleRefreshBtn(int setToState) {
		if (setToState == REFRESH_BTN_STATE_TOGGLE) {
			if (refreshBtnState == REFRESH_BTN_STATE_BROWSING)
				refreshBtnState = REFRESH_BTN_STATE_FOLLOWING;
			else
				refreshBtnState = REFRESH_BTN_STATE_BROWSING;
		} else {
			refreshBtnState = setToState;
		}
		
		if (refreshBtnState == REFRESH_BTN_STATE_BROWSING) {
			refreshBtn.setText(R.string.button_lock_at_my_location);
		} else {
			refreshBtn.setText(R.string.button_unlock);
		}
		// Save to prefs
		prefs.edit().putInt("pref_refreshbutton_state", refreshBtnState);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);
	    
		prefs = PreferenceManager.getDefaultSharedPreferences( getApplicationContext() );
		GTalkHandler.startService( getApplicationContext() );
		
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
		refreshBtnState = prefs.getInt("pref_refreshbutton_state", REFRESH_BTN_STATE_BROWSING);
		toggleRefreshBtn(refreshBtnState);
        refreshBtn.setOnClickListener(new View.OnClickListener() {   
            @Override
            public void onClick(View v) {
            	updateLocation();
        		toggleRefreshBtn(REFRESH_BTN_STATE_TOGGLE);
            	
            	if (refreshBtnState == REFRESH_BTN_STATE_FOLLOWING)
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
	    GTalkHandler.registerConnectionClient(this);
	    GTalkHandler.registerBuddyLocationClient(this);
	}
	
	@Override
	protected void onPause() {
	    Log.i(TAG, "onResume");
	    GTalkHandler.removeLocationClient(this);
	    GTalkHandler.removeConnectionClient(this);
	    GTalkHandler.removeBuddyLocationClient(this);
	    super.onPause();
	}
	
	@Override
	protected void onDestroy() {
	    Log.i(TAG, "onDestroy");
	    GTalkHandler.removeLocationClient(this);
	    GTalkHandler.removeConnectionClient(this);
	    GTalkHandler.removeBuddyLocationClient(this);
	    
	    if ( loginMenuItemState == LOGIN_MENU_STATE_LOGGED_OUT ) {
	    	// Stop the service
	    	GTalkHandler.disconnect();
	    	GTalkHandler.stopService();
	    }
	    
		super.onDestroy();
		//
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_map, menu);

		loginMenuItem = (MenuItem) menu.findItem(R.id.menu_login);
		setLoginMenu(loginMenuItemState);
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
	    	GTalkHandler.sendProbe(null);
    		Toast.makeText(this, "Force Update Invoked - Wait for Responses from other Clients", Toast.LENGTH_LONG).show();
	    	return true;
	    	
	    case R.id.menu_settings:
	    	startActivity(new Intent(this, SettingsActivity.class));
	    	return true;
	    	
	    case R.id.menu_login:
	    	if ( loginMenuItemState == LOGIN_MENU_STATE_LOGGED_IN ) {
	    		// Proceed logout
	    		GTalkHandler.disconnect();
	    	} else {
	    		// Proceed login
	    		if ( GTalkHandler.isAuthenticated() )
		    		Toast.makeText(this, "Error: Already Logged in!", Toast.LENGTH_LONG).show();
		    	else
		    		if ( checkConfig() )
		    			GTalkConnect();
	    	}
	    	return true;
	    	
	    default:
	    	return super.onOptionsItemSelected(item);
	    }
	}
	
	
	@Override
	public void onMapViewChange(MapView mapView, GeoPoint newCenter,
			GeoPoint oldCenter, int newZoom, int oldZoom) {
		Log.d(TAG, "onMapViewChange!");
		// Redraw pins
		drawBuddyPositionOverlay();
	}

	@Override
	public void onLocationUpdate() {
		updateLocation();
		if (refreshBtnState == Map.REFRESH_BTN_STATE_FOLLOWING)
			animateToCurrentLocation();
	}

	@Override
	public void onConnectionUpdate() {
		// Check connection state:
		// Only need to handle disconnection
		if ( !GTalkHandler.isConnected() ) {
			setLoginMenu(LOGIN_MENU_STATE_LOGGED_OUT);
		}
	}
	
	@Override
	public void onBuddyLocationUpdate() {
		// Redraw pins
		Log.d(TAG, "onBuddyLocationUpdate");
		drawBuddyPositionOverlay();
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
	    if (currentPoint != null) {
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
	    overlays.remove(currPosPin);
	    
	    Drawable marker = getResources().getDrawable(R.drawable.map_pointer_myloc);
	    currPosPin = new PinsOverlay(marker, mapView);
	    if (currentPoint != null) {
	        currPosPin.addOverlay( new BuddyOverlayItem(currentPoint, "Me", GTalkHandler.getUserBareAddr(), null) );
	        overlays.add(currPosPin);

		    mapView.postInvalidate();
	    }
	}
	
	public void drawBuddyPositionOverlay() {
		ArrayList<BuddyEntry> buddies = BuddyHandler.getBuddiesOnMap(mapView.getMapCenter(),
		    														 mapView.getLatitudeSpan(),
		    														 mapView.getLongitudeSpan());
		
		if ( !buddies.isEmpty() ) {
			List<Overlay> overlays = mapView.getOverlays();
			overlays.remove(currBuddyPins);
			
			Drawable marker = getResources().getDrawable(R.drawable.map_pointer);
			currBuddyPins = new PinsOverlay(marker, mapView);
			
			for (BuddyEntry buddy : buddies) {
				if (buddy.getLocation() == null)
					continue;
		    		
				currBuddyPins.addOverlay(new BuddyOverlayItem( toGeoPoint(buddy.getLocation()),
										 buddy.getUser(), buddy.getPresence().toString(), buddy ));
			}
			overlays.add(currBuddyPins);
			
			mapView.postInvalidate();
		}
	}
	
	
	// Helpers -------------------------------------------------------------
	public void GTalkConnect() {
		final ProgressDialog dialog = ProgressDialog.show(this, "Connecting...", "Please wait...", false);
		
		
		
	    Thread t = new Thread(new Runnable() {
	      	public void run() {
	      		GTalkHandler.connect();
	      		if ( GTalkHandler.isAuthenticated() )
	      			setLoginMenu(LOGIN_MENU_STATE_LOGGED_IN);
	      		
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
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	private static GeoPoint toGeoPoint(Location l) {
		return new GeoPoint( (int)(l.getLatitude()*1E6), (int)(l.getLongitude()*1E6) );
	}

}