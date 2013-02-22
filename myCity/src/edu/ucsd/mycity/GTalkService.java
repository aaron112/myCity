package edu.ucsd.mycity;

import java.util.ArrayList;
import java.util.HashMap;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

/**
 * Service for GTalk
 * @author Aaron
 *
 */


public class GTalkService extends Service implements LocationListener {
	public static final int HANDLER_MSG_LOCATION_UPD = 1;
	public static final int HANDLER_MSG_CHAT_UPD = 2;
	public static final int HANDLER_MSG_CONNECTION_UPD = 3;
	
	private static final String TAG = "GTalkService";
	private static final String HOST = "talk.google.com";
	private static final int PORT = 5222;
	private static final String SERVICE = "gmail.com";
	
	private LocationManager locationManager;
	private Location lastKnownLocation = null;
	private long updateInterval = 0;
	
	// TODO OPTIONAL: setup timer to force update at an interval
	
	private XMPPConnection connection = null;
	private SharedPreferences prefs;
	private OnSharedPreferenceChangeListener prefsListener;
	
	private String lastMsgFrom = null;
	private ArrayList<String> chatsList = new ArrayList<String>();	// List of open chats
	private HashMap<String, ArrayList<String>> messagesList = new HashMap<String, ArrayList<String>>();
	
	private Messenger callbackMessenger;
	
	private Handler mHandler = new Handler();
	String toastCache;
	
	private final IBinder mBinder = new LocalBinder();

	/**
	 * Local Binder returned when binding with Activity. This binder will return
	 * the enclosing BinderService instance.
	 */
	public class LocalBinder extends Binder {
		/**
		 * Return enclosing BinderService instance
		 */
		GTalkService getService() {
			Log.d(TAG, "getService called.");
			return GTalkService.this;
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		Log.d(TAG, "Service onCreate");
		
		// Load Preferences
		prefs = PreferenceManager.getDefaultSharedPreferences( getApplicationContext() );
		
		prefsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
				Log.d(TAG, "DEBUG: pref changed: "+key);
				if (key.equals("min_update_interval")) {
					setLocationListener();
				}
			}
		};
		prefs.registerOnSharedPreferenceChangeListener(prefsListener);
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "Service onDestroy");
		try {
			connection.disconnect();
	    } catch (Exception e) {
	    	
	    }
		super.onDestroy();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "Service onStart");

		// Set up callbackMessenger
		if ( intent != null && intent.getExtras().containsKey("callbackMessenger")) {
			callbackMessenger = (Messenger) intent.getExtras().get("callbackMessenger");
			
			// Set up Location Manager
			setLocationManager();
		} // else: Starting without intent - Run nothing
		
		return START_NOT_STICKY;
	}

	/**
	 * Public method which can be called from bound clients.
	 *
	 */
	
	public boolean isConnected() {
		if ( connection != null )
			return connection.isConnected();
		return false;
	}
	
	public boolean isAuthenticated() {
		if ( connection != null )
			return connection.isAuthenticated();
		return false;
	}
	
	// Connect to GTalk XMPP Server, returns true if successful
	public boolean connect() {
		// Read Config
		String username = prefs.getString("gtalk_username", "");
		String password = prefs.getString("gtalk_password", "");
		
		
		ConnectionConfiguration connConfig = new ConnectionConfiguration(HOST, PORT, SERVICE);
		connection = new XMPPConnection(connConfig);
		Log.i(TAG, "Connecting to " + connection.getHost());
		try {
			connection.connect();
			Log.i(TAG, "Connected to " + connection.getHost());
		} catch (XMPPException e) {
			Log.e(TAG, "Failed to connect to Gtalk server.");
			makeToast("Unable to connect to Google Talk. Please check your internet connection.");
			return false;
		}
		
		try {
			connection.login(username, password);
			Log.i(TAG, "Logged in as " + BuddyHandler.getBareAddr(connection.getUser()) );
		} catch (XMPPException e) {
			Log.e(TAG, "Failed to log in as " + username + ".");
			makeToast("Unable to login to Google Talk. Please check your username and password.");
			return false;
		}
		
		makeToast("Logged in as " + BuddyHandler.getBareAddr(connection.getUser()) + " (GTalk)");
		
		return true;
	}
	
	// Disconnect from XMPP Server
	public void disconnect() {
		connection.disconnect();
	}
	
	/**
	 * Called by Settings dialog when a connection is established with 
	 * the XMPP server
	 */
	public void setConnection() {
	    if (connection != null) {
	    	// Add a connection listener to be notified when connection problem occurs
			connection.addConnectionListener(new ConnectionListener() {
				@Override
				public void connectionClosed() {
					Log.d(TAG, "Smack: connectionClosed");
					makeToast("My City: Disconnected from Google Talk");
					notifyConnection();
				}

				@Override
				public void connectionClosedOnError(Exception e) {
					Log.d(TAG, "Smack: connectionClosedOnError: "+e);
					makeToast("My City: Connection Error - You have been disconnected from Google Talk");
					notifyConnection();
				}

				@Override
				public void reconnectingIn(int in) {
					Log.d(TAG, "Smack: reconnectingIn: "+in);
				}

				@Override
				public void reconnectionFailed(Exception e) {
					Log.d(TAG, "Smack: reconnectionFailed: "+e);
				}

				@Override
				public void reconnectionSuccessful() {
					Log.d(TAG, "Smack: reconnectionSuccessful");
				}
				
			});
			
			// Add a packet listener to get messages sent to us
			PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
			connection.addPacketListener(new PacketListener() {
				@Override
				public void processPacket(Packet packet) {
					Message message = (Message) packet;
					if (message.getBody() != null) {
						String fromAddr = BuddyHandler.getBareAddr(message.getFrom());
						Log.i(TAG, "Text Recieved " + message.getBody() + " from " +  fromAddr);
						
						// Process incoming message for XML Location
						if ( GTalkHandler.processProbe(packet) ) {
							// Return the favor
							if ( !GTalkHandler.sendNewLocation(lastKnownLocation, fromAddr) ) {
								Log.d(TAG, "Location not available, send back probe instead.");
								GTalkHandler.sendProbe(fromAddr);	// If location isn't available, send back probe instead
								// TODO: What if both devices are unavailable?
							}
							return;
						}
						if ( GTalkHandler.processGPX(packet) )
							return;
					
						lastMsgFrom = fromAddr;
						addMessage(fromAddr, GTalkHandler.getUserName(fromAddr) + ":");
						addMessage(fromAddr, message.getBody());
						notifyChat(fromAddr);
						makeChatNotification(fromAddr, message.getBody());
					}
				}
			}, filter);
		}
	}
	
	public Roster getRoster() {
		return connection.getRoster();
	}
	
	public String getLastMsgFrom() {
		return lastMsgFrom;
	}
	
	public ArrayList<String> getChatsList() {
		return chatsList;
	}
	
	public void addToChatsList(String contact) {
		if ( contact == null || contact.equals("") )
			return;
		if ( !chatsList.contains(contact) )
			chatsList.add(contact);
		if ( !messagesList.containsKey(contact) ) 
			messagesList.put(contact, new ArrayList<String>());
	}
	
	public void removeFromChatsList(String contact) {
		if ( !chatsList.contains(contact) )
			return;
		
		chatsList.remove(contact);
		messagesList.remove(contact);
	}
	
	// Retrieve Messages from Messages List
	public ArrayList<String> getMessages(String contact) {
		if ( contact == null || contact.equals("") )
			return null;
		
		addToChatsList(contact);
		return messagesList.get(contact);
	}

	// Add a Message to Messages List
	public void addMessage(String contact, String message) {
		Log.d(TAG, "addMessage: From: "+ contact + ", Msg: " + message);

		addToChatsList(contact);
		messagesList.get(contact).add(message);
	}
	
	public void sendPacket(Packet pkg) {
		connection.sendPacket(pkg);
	}
	
	public XMPPConnection getConnection() {
		return connection;
	}
	
	
	// Helpers ---------------------
	private void makeToast(String msg) {
		toastCache = msg;
		mHandler.post(new Runnable() {
			public void run() {
				Toast.makeText(getApplicationContext(), toastCache, Toast.LENGTH_LONG).show();
			}
		});
	}

	// LocationListener ------------
	private String getBestProvider() {
		Criteria criteria = new Criteria();
	    criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
	    criteria.setAccuracy(Criteria.ACCURACY_FINE);
		return locationManager.getBestProvider(criteria, true);
	}
	
	private void setLocationListener() {
	    this.getLastKnownLocation();
	    
	    if ( this.updateInterval > 0 )	// If listener exists, remove it before requesting
	    	this.locationManager.removeUpdates(this);
	    
	    this.updateInterval = Long.parseLong( prefs.getString("min_update_interval", "300"), 10 )*1000;
	    if ( this.updateInterval > 0 ) {
	    	Log.d(TAG, "Location Updates Requested at Interval: "+this.updateInterval);
	    	this.locationManager.requestLocationUpdates(this.getBestProvider(), this.updateInterval, 1, this);
	    }
	}
	
	private void setLocationManager() {
		this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		setLocationListener();
	}
	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG, "onLocationChanged");
		this.lastKnownLocation = location;
		notifyLocation();
		
		GTalkHandler.sendNewLocation(location, null); // Broadcast
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// Do nothing
	}

	@Override
	public void onProviderEnabled(String arg0) {
		// Do nothing
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// Do nothing
	}
	
	
	public Location getLastKnownLocation() {
	    Location location = locationManager.getLastKnownLocation(this.getBestProvider());
		if ( location == null )
			return this.lastKnownLocation;
		
		Location oldLocation = this.lastKnownLocation;
		this.lastKnownLocation = location;
		
		if ( oldLocation == location )
			onLocationChanged(location);
		
		return location;
	}
	
	/**
	 * Used to make an Android Notification for new messages
	 * @param bareAddr
	 * @param msg
	 */
	private void makeChatNotification(String bareAddr, String msg) {
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.new_message_2, 3)
		        .setContentTitle(bareAddr)
		        .setContentText(msg)
		        .setContentInfo("myCity")
		        .setSubText("Received from Google Talk")
		        .setTicker("New message from "+bareAddr+"!")
		        .setAutoCancel(true);
		

		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, ChatActivity.class);
		
    	Bundle b = new Bundle();
    	b.putString("contact", bareAddr);
    	resultIntent.putExtras(b);
		
		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(ChatActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager =
		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(1, mBuilder.build());

	}
	
	/**
	 * Private methods utilizing Messenger to notify clients
	 * @param from
	 */
	private void notifyChat(String from) {
		android.os.Message msg = new android.os.Message();
		Bundle b = new Bundle();
    	b.putInt("type", GTalkService.HANDLER_MSG_CHAT_UPD);
    	b.putString("contact", from);
    	msg.setData(b);

    	sendNotification(msg);
	}
	
	private void notifyLocation() {
		android.os.Message msg = new android.os.Message();
		Bundle b = new Bundle();
    	b.putInt("type", GTalkService.HANDLER_MSG_LOCATION_UPD);
    	msg.setData(b);
    	
    	Log.d(TAG, "notifyLocation");

    	sendNotification(msg);
	}
	
	private void notifyConnection() {
		android.os.Message msg = new android.os.Message();
		Bundle b = new Bundle();
    	b.putInt("type", GTalkService.HANDLER_MSG_CONNECTION_UPD);
    	msg.setData(b);
    	
    	Log.d(TAG, "notifyConnection");
    	
    	sendNotification(msg);
	}
	
	private void sendNotification(android.os.Message msg) {
		try {
    		callbackMessenger.send(msg);
    	} catch (android.os.RemoteException e1) {
    		Log.w(getClass().getName(), "Exception sending callback message", e1);
    	}
	}
}