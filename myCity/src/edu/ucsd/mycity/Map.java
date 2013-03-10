package edu.ucsd.mycity;

/**
 * Map.java - MainActivity
 * 
 * CSE110 Project - myCity
 * 
 * Team Members:
 * Yip-Ming Wong (Aaron)
 * Yui-Yan Chan (Ryan)
 * Ryan Khalili
 * Elliot Yaghoobia
 * Jonas Kabigting
 * Michael Lee
 * 
 */

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import edu.ucsd.mycity.buddy.BuddyEntry;
import edu.ucsd.mycity.buddy.BuddyList;
import edu.ucsd.mycity.buddy.BuddyOverlayItem;
import edu.ucsd.mycity.buddy.BuddyPinsOverlay;
import edu.ucsd.mycity.listeners.BuddyLocationClient;
import edu.ucsd.mycity.listeners.ConnectionClient;
import edu.ucsd.mycity.listeners.LocationClient;
import edu.ucsd.mycity.listeners.RosterClient;
import edu.ucsd.mycity.localservices.GPlace;
import edu.ucsd.mycity.localservices.LocalServiceItem;
import edu.ucsd.mycity.localservices.LocalServiceOverlayItem;
import edu.ucsd.mycity.localservices.LocalServicePinsOverlay;
import edu.ucsd.mycity.maptrack.OnLongpressListener;
import edu.ucsd.mycity.maptrack.OnMapViewChangeListener;
import edu.ucsd.mycity.maptrack.TrackedMapView;
import edu.ucsd.mycity.usercontent.UserContEntry;
import edu.ucsd.mycity.usercontent.UserContOverlayItem;
import edu.ucsd.mycity.usercontent.UserContPinsOverlay;
import edu.ucsd.mycity.utils.PinsOverlay;

public class Map extends MapActivity implements RosterClient, LocationClient,
         ConnectionClient, BuddyLocationClient, OnMapViewChangeListener, View.OnClickListener
{
	private final String TAG = "MainActivity";
	public static final int REFRESH_BTN_STATE_TOGGLE = -1;
	public static final int REFRESH_BTN_STATE_BROWSING = 0;
	public static final int REFRESH_BTN_STATE_FOLLOWING = 1;
	public static final int LOGIN_MENU_STATE_LOGGED_OUT = 0;
	public static final int LOGIN_MENU_STATE_LOGGED_IN = 1;

	private boolean isDrawing;	// to prevent concurrent draws
	private boolean isDrawingUserContent;
	private boolean isDrawingLocalServices;
	
	private SharedPreferences prefs;

	private MapController mapController;
	private TrackedMapView mapView;

	private GeoPoint currentPoint;
	private Location currentLocation = null;

	private PinsOverlay currPosPin = null;
	private PinsOverlay currBuddyPins = null;
	private PinsOverlay currUserContPins = null;
	private PinsOverlay currLocalServicePins = null;
	
	private LoadUserContentAsyncTask mLoadUserContentAsyncTask = null;
	private LoadLocalServicesAsyncTask mLoadLocalServicesAsyncTask = null;
	
	private MenuItem loginMenuItem;
	private int loginMenuItemState = LOGIN_MENU_STATE_LOGGED_OUT;
	private Button refreshBtn;
	private int refreshBtnState;

	public void setLoginMenu(int setToState)
	{
		loginMenuItemState = setToState;

		if (loginMenuItem == null)
			return;

		if (loginMenuItemState == LOGIN_MENU_STATE_LOGGED_OUT)
		{
			loginMenuItem.setTitle(R.string.menu_login);
		}
		else
		{
			loginMenuItem.setTitle(R.string.menu_logout);
		}
	}

	// setToState = -1 for TOGGLE
	private void toggleRefreshBtn(int setToState)
	{
		if (setToState == REFRESH_BTN_STATE_TOGGLE)
		{
			if (refreshBtnState == REFRESH_BTN_STATE_BROWSING)
				refreshBtnState = REFRESH_BTN_STATE_FOLLOWING;
			else
				refreshBtnState = REFRESH_BTN_STATE_BROWSING;
		}
		else
		{
			refreshBtnState = setToState;
		}

		if (refreshBtnState == REFRESH_BTN_STATE_BROWSING)
		{
			refreshBtn.setText(R.string.button_lock_at_my_location);
		}
		else
		{
			refreshBtn.setText(R.string.button_unlock);
		}
		// Save to prefs
		prefs.edit().putInt("pref_refreshbutton_state", refreshBtnState);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		
		isDrawing = false;
		isDrawingUserContent = false;

		prefs = PreferenceManager
		         .getDefaultSharedPreferences(getApplicationContext());
		GTalkHandler.startService(getApplicationContext());

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);

		mapView = (TrackedMapView) findViewById(R.id.mapView);
		mapView.setBuiltInZoomControls(true);
		mapView.setOnChangeListener(this);

		mapView.setOnLongpressListener(new OnLongpressListener()
		{
			public void onLongpress(final MapView view, final GeoPoint longpressLocation) {
				//runOnUiThread(new Runnable()
				//{
				//	public void run()
				//	{
						// Insert your longpress action here
						Intent intent = new Intent(getApplicationContext(),
						         AddUserContActivity.class);
						Bundle b = new Bundle();
						b.putInt("latitude", longpressLocation.getLatitudeE6());
						b.putInt("longitude", longpressLocation.getLongitudeE6());
						intent.putExtras(b);
						startActivity(intent);
				//	}
				//});
			}
		});

		mapController = mapView.getController();
		mapController.setZoom(18);

		updateLocation();
		animateToCurrentLocation();

		refreshBtn = (Button) findViewById(R.id.updateLocation);
		refreshBtnState = prefs.getInt("pref_refreshbutton_state",
		         REFRESH_BTN_STATE_BROWSING);
		toggleRefreshBtn(refreshBtnState);
		refreshBtn.setOnClickListener(this);

		if (prefs.getBoolean("gtalk_autologin", true) && checkConfig())
		{
			GTalkConnect();
		}
		else
		{
			Toast.makeText(this,
			         "Currently in Offline Mode (Auto login disabled)",
			         Toast.LENGTH_LONG).show();
		}
		
		updateUserContent();
	}

	@Override
	protected void onResume()
	{
		Log.i(TAG, "onResume");
		super.onResume();
		GTalkHandler.registerRosterClient(this);
		GTalkHandler.registerLocationClient(this);
		GTalkHandler.registerConnectionClient(this);
		GTalkHandler.registerBuddyLocationClient(this);
	}

	@Override
	protected void onPause()
	{
		Log.i(TAG, "onResume");
		GTalkHandler.removeRosterClient(this);
		GTalkHandler.removeLocationClient(this);
		GTalkHandler.removeConnectionClient(this);
		GTalkHandler.removeBuddyLocationClient(this);
		super.onPause();
	}

	@Override
	protected void onDestroy()
	{
		Log.i(TAG, "onDestroy");
		GTalkHandler.removeRosterClient(this);
		GTalkHandler.removeLocationClient(this);
		GTalkHandler.removeConnectionClient(this);
		GTalkHandler.removeBuddyLocationClient(this);

		if (loginMenuItemState == LOGIN_MENU_STATE_LOGGED_OUT)
		{
			// Stop the service
			GTalkHandler.disconnect();
			GTalkHandler.stopService();
		}

		super.onDestroy();
		//
	}
	
	// On button click (Lock button)
	public void onClick(View v) {
		updateLocation();
		toggleRefreshBtn(REFRESH_BTN_STATE_TOGGLE);
		
		if (refreshBtnState == REFRESH_BTN_STATE_FOLLOWING)
			animateToCurrentLocation();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_map, menu);

		loginMenuItem = (MenuItem) menu.findItem(R.id.menu_login);
		setLoginMenu(loginMenuItemState);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_shout:
			showShoutDialog();
			return true;

		case R.id.menu_chat:
			Intent intent = new Intent(this, ChatActivity.class);
			Bundle b = new Bundle();
			b.putString("contact", "");
			intent.putExtras(b);
			startActivity(intent);
			return true;
			
		case R.id.menu_buddyList:
			startActivity(new Intent(this, BuddyList.class));
			return true;

		case R.id.menu_forceupdate:
			GTalkHandler.sendProbe(null);
			Toast.makeText( this, "Force Update Invoked - Wait for Responses from other Clients",
					Toast.LENGTH_LONG).show();
			return true;

		case R.id.menu_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;

		case R.id.menu_login:
			if (loginMenuItemState == LOGIN_MENU_STATE_LOGGED_IN) {
				// Proceed logout
				GTalkHandler.disconnect();
			} else {
				// Proceed login
				if (GTalkHandler.isAuthenticated())
					Toast.makeText(this, "Error: Already Logged in!",
					         Toast.LENGTH_LONG).show();
				else if (checkConfig())
					GTalkConnect();
			}
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onMapViewChange(MapView mapView, GeoPoint newCenter,
	         GeoPoint oldCenter, int newZoom, int oldZoom)
	{
		Log.d(TAG, "onMapViewChange!");
		// Redraw pins
		drawBuddyPositionOverlay();
		updateUserContent();
		updateLocalServices();
	}

	@Override
	public void onLocationUpdate()
	{
		updateLocation();
		if (refreshBtnState == Map.REFRESH_BTN_STATE_FOLLOWING)
			animateToCurrentLocation();
	}

	@Override
	public void onConnectionUpdate()
	{
		// Check connection state:
		// Only need to handle disconnection
		if (!GTalkHandler.isConnected())
		{
			setLoginMenu(LOGIN_MENU_STATE_LOGGED_OUT);
		}
	}

	@Override
	public void onRosterUpdate()
	{
		// Redraw pins
		Log.d(TAG, "onRosterUpdate");
		drawBuddyPositionOverlay();
	}

	@Override
	public void onBuddyLocationUpdate()
	{
		// Redraw pins
		Log.d(TAG, "onBuddyLocationUpdate");
		drawBuddyPositionOverlay();
	}

	public void updateLocation()
	{
		currentLocation = GTalkHandler.getLastKnownLocation();
		if (currentLocation != null)
		{
			setCurrentLocation(currentLocation);
			drawCurrPositionOverlay();
		}
		else
		{
			Toast.makeText(this, "Location not yet acquired", Toast.LENGTH_SHORT)
			         .show();
		}
	}

	public void animateToCurrentLocation()
	{
		if (currentPoint != null)
		{
			mapController.animateTo(currentPoint);
		}
	}

	public void setCurrentLocation(Location location)
	{
		int currLatitude = (int) (location.getLatitude() * 1E6);
		int currLongitude = (int) (location.getLongitude() * 1E6);
		currentPoint = new GeoPoint(currLatitude, currLongitude);
		currentLocation = new Location("");
		currentLocation.setLatitude(currentPoint.getLatitudeE6() / 1e6);
		currentLocation.setLongitude(currentPoint.getLongitudeE6() / 1e6);
	}

	public void drawCurrPositionOverlay()
	{
		//if ( isDrawing )
		//	return;
		
		//isDrawing = true;
		List<Overlay> overlays = mapView.getOverlays();
		overlays.remove(currPosPin);

		Drawable marker = getResources()
		         .getDrawable(R.drawable.map_pointer_myloc);
		currPosPin = new BuddyPinsOverlay(marker, mapView);
		if (currentPoint != null)
		{
			currPosPin.addOverlay(new BuddyOverlayItem(currentPoint,
			         "Current Location", GTalkHandler.getUserBareAddr()));
			overlays.add(currPosPin);

			mapView.postInvalidate();
		}
		//isDrawing = false;
	}

	public void drawBuddyPositionOverlay()
	{
		if ( isDrawing ) {
			Log.i(TAG, "drawBuddyPositionOverlay skipped - isDrawing = true");
			return;
		}
		
		isDrawing = true;

		Log.i(TAG, "drawBuddyPositionOverlay going ahead");
		
		ArrayList<BuddyEntry> buddies = BuddyHandler.getBuddiesOnMap(
		         mapView.getMapCenter(), mapView.getLatitudeSpan(),
		         mapView.getLongitudeSpan());

		if (!buddies.isEmpty())
		{
			List<Overlay> overlays = mapView.getOverlays();
			overlays.remove(currBuddyPins);

			Drawable marker = getResources().getDrawable(R.drawable.map_pointer);
			currBuddyPins = new BuddyPinsOverlay(marker, mapView);

			for (BuddyEntry buddy : buddies)
			{
				if (buddy.getLocation() == null)
					continue;

				currBuddyPins.addOverlay(new BuddyOverlayItem(toGeoPoint(buddy
				         .getLocation()), buddy));
			}
			overlays.add(currBuddyPins);

			mapView.postInvalidate(); // Tell MapView to update itself
		}

		isDrawing = false;
	}

	public boolean drawUserContOverlay()
	{
		if ( isDrawingUserContent ) {
			Log.i(TAG, "drawUserContOverlay skipped");
			return false;
		}
		isDrawingUserContent = true;
		
		Log.i(TAG, "drawUserContOverlay going ahead");
		
		ArrayList<UserContEntry> usercontents = UserContHandler.getContent();
		if (usercontents == null || usercontents.isEmpty())
		{
			Log.d(TAG, "usercontents is null");
			isDrawingUserContent = false;
			return false;
		}

		List<Overlay> overlays = mapView.getOverlays();
		overlays.remove(currUserContPins);

		Drawable marker = getResources().getDrawable(R.drawable.user_cont_pin);
		currUserContPins = new UserContPinsOverlay(marker, mapView);

		for (UserContEntry entry : usercontents)
		{
			currUserContPins.addOverlay(new UserContOverlayItem(entry));
		}
		overlays.add(currUserContPins);
		mapView.postInvalidate();
		
		isDrawingUserContent = false;

		return true;
	}

	public boolean drawLocalServicesOverlay()
	{
		if ( isDrawingLocalServices ) {
			Log.i(TAG, "drawLocalServicesOverlay skipped");
			return false;
		}
		isDrawingLocalServices = true;
		
		Log.i(TAG, "drawLocalServicesOverlay going ahead");
		
		List<GPlace> localServices = GooglePlacesHandler.getLocalServices();
		if (localServices == null || localServices.isEmpty())
		{
			Log.d(TAG, "localServices is null");
			isDrawingLocalServices = false;
			return false;
		}

		List<Overlay> overlays = mapView.getOverlays();
		overlays.remove(currLocalServicePins);

		Drawable marker = getResources().getDrawable(R.drawable.map_pointer_localservices);
		currLocalServicePins = new LocalServicePinsOverlay(marker, mapView);

		for (LocalServiceItem item : localServices)
		{
			currLocalServicePins.addOverlay(new LocalServiceOverlayItem(item));
		}
		overlays.add(currLocalServicePins);
		mapView.postInvalidate();
		
		isDrawingLocalServices = false;

		return true;
	}

	// Helpers -------------------------------------------------------------
	public void GTalkConnect()
	{
		final ProgressDialog dialog = ProgressDialog.show(this, "Connecting...",
		         "Please wait...", false);

		Thread t = new Thread(new Runnable()
		{
			public void run()
			{
				GTalkHandler.connect();
				if (GTalkHandler.isAuthenticated())
					setLoginMenu(LOGIN_MENU_STATE_LOGGED_IN);

				dialog.dismiss();
			}
		});

		t.start();
		dialog.show();
	}

	// Returns true if config is okay.
	private boolean checkConfig()
	{
		// Check pref for username and password, if undefined, direct user to
		// Settings activity.
		if (prefs.getString("gtalk_username", "").equals("")
		         || prefs.getString("gtalk_password", "").equals(""))
		{
			// Show a dialog to redirect to settings
			AlertDialog.Builder builder = new AlertDialog.Builder( this );
			builder.setTitle("Welcome to My City!");
			builder.setMessage("This is your first time using this app. Please specify your username and password before continuing.");
			builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
					startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
		        }
			});
			
			builder.show();
			return false;
		}

		return true;
	}

	@Override
	protected boolean isRouteDisplayed()
	{
		return false;
	}

	private static GeoPoint toGeoPoint(Location l)
	{
		return new GeoPoint((int) (l.getLatitude() * 1E6),
		         (int) (l.getLongitude() * 1E6));
	}
	
	private void showShoutDialog() {
		final EditText inputTextLayout = new EditText(this);
        
		AlertDialog.Builder builder = new AlertDialog.Builder( this );
		builder.setTitle("Enter shout message: ");
		builder.setView(inputTextLayout);
		
		builder.setPositiveButton("Shout!", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	        	// Check if any buddy in range
	        	ArrayList<BuddyEntry> buddiesInRange = BuddyHandler.getBuddiesOnMap(mapView.getMapCenter(), mapView.getLatitudeSpan(), mapView.getLongitudeSpan());
	        	if ( buddiesInRange.isEmpty() ) {
	        		Toast.makeText(getApplicationContext(), "No buddies in range. Try again later!", Toast.LENGTH_LONG).show();
	        		return;
	        	}// else if ( buddiesInRange.size() == 1 ) {
	        	//	Toast.makeText(getApplicationContext(), "Only one buddy in range. Not initialing shout.", Toast.LENGTH_LONG).show();
	        	//	return;
	        	//}
	        	
	        	// Make shout
	        	if ( GTalkHandler.createMultiChatRoom(buddiesInRange, inputTextLayout.getText().toString().trim() ) ) {
	        		Toast.makeText(getApplicationContext(), "Shout sent successfully!", Toast.LENGTH_LONG).show();
	        		// TODO: Open chat activity after invitation
	        	} else {
	        		Toast.makeText(getApplicationContext(), "Error when shouting. Check your connection.", Toast.LENGTH_LONG).show();
	        	}
	        }
		});
		
		builder.setNegativeButton("Cancel", null);
		builder.show();
	}
	
	protected class MapSpan {
		public GeoPoint gp;
		public int latSpan;
		public int lonSpan;
		
		public MapSpan(GeoPoint gp, int latSpan, int lonSpan) {
			this.gp = gp;
			this.latSpan = latSpan;
			this.lonSpan = lonSpan;
		}
	}
	
	private void updateUserContent() {
		if ( mLoadUserContentAsyncTask == null )
			mLoadUserContentAsyncTask = new LoadUserContentAsyncTask();
		else {
			Status status = mLoadUserContentAsyncTask.getStatus();
			// Check if task is already running
			if ( status == AsyncTask.Status.RUNNING ) {
				// Task is running, request ignored
				Log.d(TAG, "Task is running, request ignored");
				return;
			} else if ( status == AsyncTask.Status.FINISHED || status == AsyncTask.Status.PENDING ) {
				// Previously finished task found, recreating
				Log.d(TAG, "Previously finished task found, recreating");
				mLoadUserContentAsyncTask = new LoadUserContentAsyncTask();
			}
		}
		
		mLoadUserContentAsyncTask.execute( new MapSpan(mapView.getMapCenter(), mapView.getLatitudeSpan(),
		         mapView.getLongitudeSpan()) );
	}
	
	private class LoadUserContentAsyncTask extends AsyncTask<MapSpan, Void, Boolean> {
		private final static String TAG = "LoadUserContentAsyncTask";
	    @Override
	    protected void onPreExecute() {
	    	// update the UI immediately after the task is executed
	    	super.onPreExecute();
	    	Toast.makeText(getApplicationContext(), "Loading user contents...", Toast.LENGTH_SHORT).show();
	    }
	    
		@Override
		protected Boolean doInBackground(MapSpan... params) {
			// TODO: Implement MapSpan
			return UserContHandler.updateContent();
		}
		
		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
		}
		
		@Override
		protected void onPostExecute(Boolean res) {
			super.onPostExecute(res);

			//Toast.makeText(getApplicationContext(), "Done loading!", Toast.LENGTH_SHORT).show();
			Log.d(TAG, "Done loading!");
			
			if (res)
				drawUserContOverlay();
		}
	}
	
	private void updateLocalServices() {
		if ( mLoadLocalServicesAsyncTask == null )
			mLoadLocalServicesAsyncTask = new LoadLocalServicesAsyncTask();
		else {
			Status status = mLoadLocalServicesAsyncTask.getStatus();
			// Check if task is already running
			if ( status == AsyncTask.Status.RUNNING ) {
				// Task is running, request ignored
				Log.d(TAG, "Task is running, request ignored");
				return;
			} else if ( status == AsyncTask.Status.FINISHED || status == AsyncTask.Status.PENDING ) {
				// Previously finished task found, recreating
				Log.d(TAG, "Previously finished task found, recreating");
				mLoadLocalServicesAsyncTask = new LoadLocalServicesAsyncTask();
			}
		}
		
		mLoadLocalServicesAsyncTask.execute( new MapSpan(mapView.getMapCenter(), mapView.getLatitudeSpan(),
		         mapView.getLongitudeSpan()) );
	}
	
	private class LoadLocalServicesAsyncTask extends AsyncTask<MapSpan, Void, Boolean> {
		private final static String TAG = "LoadLocalServicesAsyncTask";
	    @Override
	    protected void onPreExecute() {
	    	// update the UI immediately after the task is executed
	    	super.onPreExecute();
	    	Toast.makeText(getApplicationContext(), "Loading local services...", Toast.LENGTH_SHORT).show();
	    }
	    
		@Override
		protected Boolean doInBackground(MapSpan... params) {
			return GooglePlacesHandler.updateLocalServices(params[0].gp, params[0].latSpan, params[0].lonSpan, "");
		}
		
		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
		}
		
		@Override
		protected void onPostExecute(Boolean res) {
			super.onPostExecute(res);

			Toast.makeText(getApplicationContext(), "Done loading local services.", Toast.LENGTH_SHORT).show();
			Log.d(TAG, "Done loading!");
			
			if (res)
				drawLocalServicesOverlay();
		}
	}
}
