package edu.ucsd.mycity;

import java.util.ArrayList;
import java.util.HashMap;

import org.jivesoftware.smack.AndroidConnectionConfiguration;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.provider.MUCUserProvider;

import edu.ucsd.mycity.buddy.BuddyEntry;
import edu.ucsd.mycity.chat.ChatRoom;
import edu.ucsd.mycity.utils.MultiChat;

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
import android.net.Uri;
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

public class GTalkService extends Service implements LocationListener, ChatManagerListener, MessageListener, PacketListener, InvitationListener {
	public static final int HANDLER_MSG_LOCATION_UPD = 1;
	public static final int HANDLER_MSG_CHAT_UPD = 2;
	public static final int HANDLER_MSG_CONNECTION_UPD = 3;
	
	public static final int CHAT_TYPE_CHAT = 1;
	public static final int CHAT_TYPE_SHOUT = 2;
	
	private static final String TAG = "GTalkService";
	private static final String HOST = "talk.google.com";
	private static final int PORT = 5222;
	private static final String SERVICE = "gmail.com";
	
	private LocationManager locationManager;
	private Location lastKnownLocation = null;
	private long updateInterval = 0;

	private SharedPreferences prefs;
	private XMPPConnection connection = null;
	private OnSharedPreferenceChangeListener prefsListener;
	private ChatManager chatManager;
	
	private String lastMsgFrom = null;
	private HashMap<String, ChatRoom> chatsList = new HashMap<String, ChatRoom>();
	
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
		
		// As required by new version of aSmack
		//SmackAndroid.init( getApplicationContext() );
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
		
		AndroidConnectionConfiguration connConfig = new AndroidConnectionConfiguration(HOST, PORT, SERVICE);
		//ConnectionConfiguration connConfig = new ConnectionConfiguration(HOST, PORT, SERVICE);
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

			// Switching over to ChatManager
			chatManager = connection.getChatManager();
			chatManager.addChatListener(this);
			
			// Crucial for MultiUserChat
			ProviderManager pm = ProviderManager.getInstance();
			pm.addExtensionProvider("x", "http://jabber.org/protocol/muc#user", new MUCUserProvider());
			
			// For multi-user chat
			MultiUserChat.addInvitationListener(connection, this);
		}
	}
	
	public Roster getRoster() {
		return connection.getRoster();
	}
	
	public String getLastMsgFrom() {
		return lastMsgFrom;
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
	private void makeChatNotification(String bareAddr, String msg, int chattype) {
		String type;
		
		if ( chattype == CHAT_TYPE_SHOUT ) {
			type = "shout";
		} else {
			type = "message";
		}
		
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.new_message_2, 3)
		        .setContentTitle(bareAddr)
		        .setContentText(msg)
		        .setContentInfo("myCity")
		        .setSubText("Received from Google Talk")
		        .setTicker("New "+type+" from "+
		        		findChatRoom(bareAddr).getTitle()+"!")
		        .setAutoCancel(true);
		
		// Make some noise!
		String ringtoneUrl = prefs.getString("msg_notification", "");
		if ( !ringtoneUrl.equals("") )
			mBuilder.setSound(Uri.parse(ringtoneUrl));

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

    	sendNotificationMsg(msg);
	}
	
	private void notifyLocation() {
		android.os.Message msg = new android.os.Message();
		Bundle b = new Bundle();
    	b.putInt("type", GTalkService.HANDLER_MSG_LOCATION_UPD);
    	msg.setData(b);
    	
    	Log.d(TAG, "notifyLocation");

    	sendNotificationMsg(msg);
	}
	
	private void notifyConnection() {
		android.os.Message msg = new android.os.Message();
		Bundle b = new Bundle();
    	b.putInt("type", GTalkService.HANDLER_MSG_CONNECTION_UPD);
    	msg.setData(b);
    	
    	Log.d(TAG, "notifyConnection");
    	
    	sendNotificationMsg(msg);
	}
	
	private void sendNotificationMsg(android.os.Message msg) {
		try {
    		callbackMessenger.send(msg);
    	} catch (android.os.RemoteException e1) {
    		Log.w(getClass().getName(), "Exception sending callback message", e1);
    	}
	}
	
	@Override
	// ******* ChatManagerListener *******
	// NOTE: For single-user chat only
	public void chatCreated(Chat chat, boolean createdLocally) {
		// Ignore if created locally, we only want remotely created chats
		if ( createdLocally )
			return;
		
		// See if already have a chatroom, if not, create it.
		if ( findChatRoom(chat) == null )
			createChatRoom(chat);
		
		chat.addMessageListener(this);
	}
	
	//@Override
	// ******* InvitationListener *******
	// NOTE: For MULTI-user chat only
    public void invitationReceived(Connection conn, String room, String inviter, String reason,
    								String password, Message message) {
        Log.i(TAG, "Multi-chat invition received from: " + inviter);
        Log.i(TAG, "Invitation Reason: " + reason);
        
        MultiUserChat muc = new MultiUserChat(conn, room);  
        
        try {
            muc.join(GTalkHandler.getUserBareAddr(), password);  
        } catch (XMPPException e) {
        	Log.e(TAG, "Unable to join chat room.");
        	return;
        }
        
        ChatRoom chatRoom = createChatRoom(muc, reason);		// Map reason to chat title

        Log.i(TAG, "Going to getBuddy.");
        BuddyEntry buddy = BuddyHandler.getBuddy(inviter);
        
        Log.i(TAG, "Going to addMessage.");
        //chatRoom.addMessage(buddy, reason);						// Add invite message as the first message
        
        Log.i(TAG, "Going to makeChatNotification.");
		makeChatNotification( buddy.getName(), reason, CHAT_TYPE_SHOUT );

		notifyChat(muc.getRoom());
        muc.addMessageListener(this);
    }

	@Override
	// ******* MessageListener *******
	// NOTE: For single-user chat only
	public void processMessage(Chat chat, Message message) {
		if (message.getBody() == null)
			return;
		
		String fromAddr = BuddyHandler.getBareAddr(message.getFrom());
		Log.d(TAG, "Text Recieved " + message.getBody() + " from " +  fromAddr);
		
		// Process incoming message for XML Location
		if ( GTalkHandler.processProbe(message) ) {
			// Return the favor
			if ( !GTalkHandler.sendNewLocation(lastKnownLocation, fromAddr) ) {
				Log.d(TAG, "Location not available, send back probe instead.");
				GTalkHandler.sendProbe(fromAddr);	// If location isn't available, send back probe instead
				// TODO: What if both devices are unavailable?
			}
			return;		// Stop if parsed to be probe message
		}
		
		if ( GTalkHandler.processGPX(message) )
			return;		// Stop if parsed to be GPX message
		
		boolean isChat = false;
		String dispMsg = GTalkHandler.processLocalServiceInvitation(message);
		if ( dispMsg == null ) {
			dispMsg = message.getBody();
			isChat = true;
		}
		
		Log.d(TAG, "Going on to add chat.");
		
		ChatRoom chatRoom;
		if ( (chatRoom = findChatRoom(chat)) == null )
			chatRoom = createChatRoom(chat);
		lastMsgFrom = chatRoom.getParticipant().getUser();
		chatRoom.addMessage(chatRoom.getParticipant(), dispMsg);
		notifyChat(fromAddr);
		if (isChat)
			makeChatNotification(fromAddr, dispMsg, CHAT_TYPE_CHAT);
	}

	@Override
	// ******* PacketListener *******
	// NOTE: For multi-user chat only
	public void processPacket(Packet packet) {
		Message message = (Message) packet;
		
		if (message.getBody() == null)
			return;

		String fromAddr = BuddyHandler.getBareAddr(message.getFrom());
		ChatRoom chatRoom = findChatRoom(fromAddr);
		if (chatRoom == null)
			return;		// ignore message if chatroom not exist.
		
		// Try to parse from addr
		ArrayList<String> matchres = MultiChat.parseFrom(message.getBody());
		lastMsgFrom = fromAddr;	// Chat room address
		
		BuddyEntry buddy = BuddyHandler.getBuddy( matchres.get(0) );
		
		chatRoom.addMessage( buddy, matchres.get(1) );
		makeChatNotification( buddy.getName(), matchres.get(1), CHAT_TYPE_CHAT );
		
		notifyChat(fromAddr);
	}
	
	/**
	 * @pre	  chatroom not exist
	 * @param fromAddr
	 * @return newly created ChatRoom
	 */
	public ChatRoom createChatRoom(String bareAddr) {
		// Single-user mode based on bareAddr
		// Add chat first
		Log.d(TAG, "createChatRoom");
		return createChatRoom( chatManager.createChat(bareAddr, this) );
	}
	
	public ChatRoom createChatRoom(Chat chat) {
		// Single-user mode based on pre-created chat
		String bareAddr = BuddyHandler.getBareAddr(chat.getParticipant());
		ChatRoom newChatRoom = new ChatRoom(chat, BuddyHandler.getBuddy(bareAddr));
		chatsList.put(bareAddr, newChatRoom);
		return newChatRoom;
	}
	
	public ChatRoom createChatRoom(MultiUserChat muc, String title) {
		// Multi-user mode based on pre-created muc
		Log.d(TAG, "createChatRoom (MUC) called: " + title);
		chatsList.put(muc.getRoom(), new ChatRoom(muc, title));
		return chatsList.get(muc.getRoom());
	}

	/**
	 * Finds a chat room with specified parameters
	 * @param bareAddr
	 * @return associated ChatRoom
	 */
	public ChatRoom findChatRoom(String bareAddr) {
		if ( chatsList.containsKey(bareAddr) ) {
			Log.d(TAG, "findChatRoom: existing chat found!");
			
			return chatsList.get(bareAddr);
		}
		
		// if not found, call createChatRoom to create it.
		return null;
	}
	
	public ChatRoom findChatRoom(Chat chat) {
		// Single-user mode based on chat
		String bareAddr = BuddyHandler.getBareAddr(chat.getParticipant());
		if ( chatsList.containsKey(bareAddr) )
			return chatsList.get(bareAddr);
		
		return null;
	}
	
	public ChatRoom findChatRoom(MultiUserChat muc) {
		// Multi-user mode based on muc
		if ( chatsList.containsKey(muc.getRoom()) )
			return chatsList.get(muc.getRoom());
		
		return null;
	}
	
	public HashMap<String, ChatRoom> getChatsList() {
		return chatsList;
	}

}