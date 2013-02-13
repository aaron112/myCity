package edu.ucsd.mycity;

import java.util.ArrayList;
import java.util.HashMap;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Service for GTalk
 * @author Aaron
 *
 */


public class GTalkService extends Service {
	private static final String TAG = "GTalkService";
	private static final String HOST = "talk.google.com";
	private static final int PORT = 5222;
	private static final String SERVICE = "gmail.com";

	private XMPPConnection connection = null;
	private SharedPreferences prefs;
	
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
		Log.d(TAG, "Service onCreate");
		
		// Load Preferences
		prefs = PreferenceManager.getDefaultSharedPreferences( getApplicationContext() );
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "Service onDestroy");
		try {
			connection.disconnect();
	    } catch (Exception e) {
	    	
	    }
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int f =  super.onStartCommand(intent, flags, startId);
		Log.d(TAG, "Service onStart");
		
		// Set up callbackMessenger
		callbackMessenger = (Messenger) intent.getExtras().get("callbackMessenger");
		
		return f;
	}

	/**
	 * Public method which can be called from bound clients.
	 *
	 */
	public void newMessageReceived(String from) {
		android.os.Message msg = new android.os.Message();
		Bundle b = new Bundle();
    	b.putString("contact", from);
    	msg.setData(b);
    	
    	// send message to the handler with the current message handler
    	try {
    		callbackMessenger.send(msg);
    	} catch (android.os.RemoteException e1) {
    		Log.w(getClass().getName(), "Exception sending callback message", e1);
    	}
	        
		//Notification notification = new Notification();
	}
	
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
		Log.i(TAG, "DEBUG: perfs = " + prefs);
		
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
			Log.i(TAG, "Logged in as " + StringUtils.parseBareAddress(connection.getUser()) );
		} catch (XMPPException e) {
			Log.e(TAG, "Failed to log in as " + username + ".");
			makeToast("Unable to login to Google Talk. Please check your username and password.");
			return false;
		}
		
		makeToast("Logged in as " + StringUtils.parseBareAddress(connection.getUser()) + " (GTalk)");
		
		return true;
	}
	
	/**
	 * Called by Settings dialog when a connection is established with 
	 * the XMPP server
	 */
	public void setConnection() {
	    if (connection != null) {
	      // Add a packet listener to get messages sent to us
	      PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
	      connection.addPacketListener(new PacketListener() {
	    	  
	        @Override
	        public void processPacket(Packet packet) {
	          Message message = (Message) packet;
	          if (message.getBody() != null) {
	            String fromName = StringUtils.parseBareAddress(message.getFrom());
	            Log.i(TAG, "Text Recieved " + message.getBody() + " from " +  fromName);

	            // Process incoming message for XML Location
	            if ( GTalkHandler.processProbe(packet) || GTalkHandler.processGPX(packet) )
	            	return;
	            
	            addMessage(fromName, GTalkHandler.getUserName(fromName) + ":");
	            addMessage(fromName, message.getBody());
	            newMessageReceived(fromName);
	          }
	        }
	        
	      }, filter);
	    }
	}
	
	public Roster getRoster() {
		return connection.getRoster();
	}
	
	public ArrayList<String> getChatsList() {
		return chatsList;
	}
	
	// Retrieve Messages from Messages List
	public ArrayList<String> getMessages(String contact) {
		if ( !chatsList.contains(contact))
			chatsList.add(contact);
		if ( !messagesList.containsKey(contact) ) 
			messagesList.put(contact, new ArrayList<String>());
		
		return messagesList.get(contact);
	}

	// Add a Message to Messages List
	public void addMessage(String contact, String message) {
		Log.d(TAG, "addMessage: From: "+ contact + ", Msg: " + message);

		if ( !chatsList.contains(contact))
			chatsList.add(contact);
		if ( !messagesList.containsKey(contact) )
			messagesList.put(contact, new ArrayList<String>());
		
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
}