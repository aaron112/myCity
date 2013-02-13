package edu.ucsd.mycity;

import java.util.ArrayList;
import java.util.Collection;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.util.StringUtils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;

/**
 * This works as an adapter between Activities and GTalkService
 * @author Aaron
 *
 */

public class GTalkHandler {
	private static final String TAG = "GTalkHandler";
	public static Context context;
	
	private static ArrayList<ChatClient> observers = new ArrayList<ChatClient>();
	
	// Handler for GTalkService
	private static Handler mHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			Bundle b = msg.getData();
			notifyObservers( b.getString("contact") );
		}
	};
	  
	static GTalkService mService = null;
	static boolean isServiceStarted = false;
	
	private static ServiceConnection mConn = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			Log.d(TAG, "Connected to service.");
			mService = ((GTalkService.LocalBinder) binder).getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			Log.d(TAG, "Disconnected from service.");
			mService = null;
			isServiceStarted = false;
		}
	};
	
	// Subject in Observer Pattern
	public static void registerObserver(ChatClient o) {
		observers.add(o);
	}

	public static void removeObserver(ChatClient o) {
		int i = observers.indexOf(o);
		if (i >= 0) {
			observers.remove(i);
		}
	}

	public static void notifyObservers(String from) {
		for (int i = 0; i < observers.size(); ++i) {
			ChatClient observer = (ChatClient)observers.get(i);
			observer.onUpdate(from);
		}
	}
	
	public static void newMessageReceived(String from) {
		notifyObservers(from);
	}
	
	
	public static void startService() {
		Log.d(TAG, "Attempting to start service.");
		
		Intent intent = new Intent(context, GTalkService.class);
		intent.putExtra("callbackMessenger", new Messenger(mHandler));
		
		context.startService(intent);
		context.bindService(intent, mConn, Context.BIND_AUTO_CREATE);
		isServiceStarted = true;
	}
	
	public static void stopService() {
		isServiceStarted = false;
		context.unbindService(mConn);
		context.stopService(new Intent(context, GTalkService.class));
	}
	
	public static boolean isConnected() {
		if ( isServiceStarted )
			return mService.isConnected();
		return false;
	}
	
	public static boolean isAuthenticated() {
		if ( isServiceStarted )
			return mService.isAuthenticated();
		return false;
	}
	
	// Connect to GTalk XMPP Server, returns true if successful
	public static boolean connect() {
		// Start Service if not started, then call to connect method
		if ( !isServiceStarted )
			startService();
		
		while ( mService == null ) {}
		
		Log.d(TAG, "DEBUG: mService: " + mService);
		
		if ( mService.isAuthenticated() )
			return true;
		
		if ( mService.connect() ) {
			mService.setupConnection();
			mService.updateRoaster();
		}
		
		return true;
	}
	
	public static ArrayList<String> getChatsList() {
		return mService.getChatsList();
	}
	
	public static ArrayList<String> getMessages(String contact) {
		if ( isServiceStarted )
			return mService.getMessages(contact);
		
		return new ArrayList<String>();
	}
	
	// Return bool indicates if send is successful
	public static boolean sendMessage(String contact, String message) {
		if ( isServiceStarted && mService.isAuthenticated() ) {
			Message msg = new Message(contact, Message.Type.chat);
	        msg.setBody(message);
			
			mService.sendPacket(msg);
			mService.addMessage(contact, getUserBareAddr() + ":");
			mService.addMessage(contact, message);
			return true;
		}
		
		return false;
	}
	
	public static String getUserBareAddr() {
		if ( isServiceStarted )
			return StringUtils.parseBareAddress( mService.getConnection().getUser() );
		return "";
	}
	
	public static void updateRoaster() {
		mService.updateRoaster();
	}
	
	public static RosterEntry lookupUser(String bareAddr) {
		Collection<RosterEntry> entries = mService.getRoster().getEntries();
        for (RosterEntry entry : entries) {
        	if ( StringUtils.parseBareAddress( entry.getUser() ).equals(bareAddr) )
        		return entry;
        }
        return null;
	}
	
	public static String getUserName(String bareAddr) {
		RosterEntry res = lookupUser(bareAddr);
		if ( res != null && res.getName() != null )
			return res.getName();
		
		return bareAddr;
	}
}
