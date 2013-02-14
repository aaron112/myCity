package edu.ucsd.mycity;

import java.util.ArrayList;
import java.util.Collection;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;

import edu.ucsd.mycity.listeners.BuddyLocationClient;
import edu.ucsd.mycity.listeners.ChatClient;
import edu.ucsd.mycity.listeners.LocationClient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
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
	public static final int SENDMSG_HIDDEN = 0;
	public static final int SENDMSG_NORMAL = 1;
	
	private static final String TAG = "GTalkHandler";
	private static final String PROBEMSG = "myCityProbe";
	public static Context context;
	
	private static Roster roster;
	
	private static ArrayList<ChatClient> chatClients = new ArrayList<ChatClient>();
	private static ArrayList<LocationClient> locationClients = new ArrayList<LocationClient>();
	private static ArrayList<BuddyLocationClient> buddyLocationClients = new ArrayList<BuddyLocationClient>();
	
	// Handler for GTalkService
	private static Handler mHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			Bundle b = msg.getData();
			int msgtype = b.getInt("type");
			
			switch (msgtype) {
			case GTalkService.HANDLER_MSG_CHAT_UPD:
				Log.d(TAG, "handleMessage: received chat_upd from service");
				notifyChatClients( b.getString("contact") );
				break;
				
			case GTalkService.HANDLER_MSG_LOCATION_UPD:
				Log.d(TAG, "handleMessage: received location_upd from service");
				Location location = (Location)b.getParcelable("location");
				notifyLocationClients( location );
				break;
			}
			
		}
	};
	  
	static GTalkService mService = null;
	static boolean isServiceStarted = false;
	
	private static ServiceConnection mConn = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			Log.d(TAG, "Connected to service.");
			mService = ((GTalkService.LocalBinder) binder).getService();
			isServiceStarted = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			Log.d(TAG, "Disconnected from service.");
			mService = null;
			isServiceStarted = false;
		}
	};
	
	// Subject in Observer Pattern
	public static void registerChatClient(ChatClient o) {
		if ( !chatClients.contains(o) )
			chatClients.add(o);
	}
	public static void removeChatClient(ChatClient o) {
		int i = chatClients.indexOf(o);
		if (i >= 0) {
			chatClients.remove(i);
		}
	}
	public static void notifyChatClients(String from) {
		for (int i = 0; i < chatClients.size(); ++i) {
			ChatClient chatClient = (ChatClient)chatClients.get(i);
			chatClient.onChatUpdate(from);
		}
	}

	public static void registerLocationClient(LocationClient o) {
		if ( !locationClients.contains(o) )
			locationClients.add(o);
	}
	public static void removeLocationClient(LocationClient o) {
		int i = locationClients.indexOf(o);
		if (i >= 0) {
			locationClients.remove(i);
		}
	}
	public static void notifyLocationClients(Location location) {
		for (int i = 0; i < locationClients.size(); ++i) {
			LocationClient locationClient = (LocationClient)locationClients.get(i);
			locationClient.onLocationUpdate(location);
		}
	}

	public static void registerBuddyLocationClient(BuddyLocationClient o) {
		if ( !buddyLocationClients.contains(o) )
			buddyLocationClients.add(o);
	}
	public static void removeBuddyLocationClient(BuddyLocationClient o) {
		int i = buddyLocationClients.indexOf(o);
		if (i >= 0) {
			buddyLocationClients.remove(i);
		}
	}
	public static void notifyBuddyLocationClients() {
		for (int i = 0; i < buddyLocationClients.size(); ++i) {
			BuddyLocationClient buddyLocationClient = (BuddyLocationClient)buddyLocationClients.get(i);
			buddyLocationClient.onBuddyLocationUpdate();
		}
	}
	
	public static void startService() {
		if ( isServiceStarted )
			return;
		
		Log.d(TAG, "Attempting to start service.");
			
		Intent intent = new Intent(context, GTalkService.class);
		intent.putExtra("callbackMessenger", new Messenger(mHandler));
			
		context.startService(intent);
		context.bindService(intent, mConn, 0);
	}
	
	public static void stopService() {
		if ( !isServiceStarted )
			return;
		
		isServiceStarted = false;
		BuddyHandler.clear();
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
		startService();
		
		while ( mService == null ) {}	// Wait until Service is set up
		
		if ( mService.isAuthenticated() )
			return true;
		
		if ( mService.connect() ) {
			mService.setConnection();
			setRoaster();
		}
		
		return true;
	}
	
	public static void disconnect() {
		if ( !isServiceStarted )
			return;
		
		mService.disconnect();
	}
	
	public static void setRoaster() {
		if ( isServiceStarted && mService.isAuthenticated() ) {
			roster = mService.getRoster();
			BuddyHandler.loadBuddiesFromRoster(roster);
			
			// Roster Update Listener
			roster.addRosterListener(new RosterListener() {
			    public void entriesDeleted(Collection<String> addresses) {
			    	BuddyHandler.loadBuddiesFromRoster(roster);
			    }
			    public void entriesUpdated(Collection<String> addresses) {
			    	BuddyHandler.loadBuddiesFromRoster(roster);
			    }
			    public void presenceChanged(Presence presence) {
			    	Log.d(TAG, "Presence changed: " + presence.getFrom() + " " + presence);
			    	BuddyHandler.updatePresense(StringUtils.parseBareAddress(presence.getFrom()), presence);
			    }
				@Override
				public void entriesAdded(Collection<String> arg0) {
					BuddyHandler.loadBuddiesFromRoster(roster);
				}
			});
		}
		
		/***
		Collection<RosterEntry> entries = roster.getEntries();
        for (RosterEntry entry : entries) {
         // TODO:
          Log.d(TAG,  "--------------------------------------");
          Log.d(TAG, "RosterEntry " + entry);
          Log.d(TAG, "User: " + entry.getUser());
          Log.d(TAG, "Name: " + entry.getName());
          Log.d(TAG, "Status: " + entry.getStatus());
          Log.d(TAG, "Type: " + entry.getType());
          Presence entryPresence = roster.getPresence(entry.getUser());

          Log.d(TAG, "Presence Status: "+ entryPresence.getStatus());
          Log.d(TAG, "Presence Type: " + entryPresence.getType());

          Presence.Type type = entryPresence.getType();
          if (type == Presence.Type.available)
            Log.d(TAG, "Presence AVIALABLE");
          Log.d(TAG, "Presence : " + entryPresence);
       }
       */
	}
	
	public static String getLastMsgFrom() {
		if ( isServiceStarted )
			return mService.getLastMsgFrom();

		return null;
	}
	
	public static void removeFromChatsList(String contact) {
		if ( isServiceStarted )
			mService.removeFromChatsList(contact);
	}
	
	public static ArrayList<String> getChatsList() {
		if ( isServiceStarted )
			return mService.getChatsList();

		return new ArrayList<String>();
	}
	
	public static ArrayList<String> getMessages(String contact) {
		if ( isServiceStarted )
			return mService.getMessages(contact);
		
		return new ArrayList<String>();
	}
	
	// Return bool indicates if send is successful
	public static boolean sendMessage(String contact, String message, int mode) {
		if ( isServiceStarted && mService.isAuthenticated() ) {
			Message msg = new Message(contact, Message.Type.chat);
	        msg.setBody(message);
	        
	        mService.sendPacket(msg);
	        
	        if ( mode != GTalkHandler.SENDMSG_HIDDEN ) {
	        	mService.addMessage(contact, getUserBareAddr() + ":");
				mService.addMessage(contact, message);
	        }
			return true;
		}
		
		return false;
	}
	
	public static String getUserBareAddr() {
		if ( isServiceStarted )
			return StringUtils.parseBareAddress( mService.getConnection().getUser() );
		return "";
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
	
	
	// Returns true if matched.
	public static boolean processProbe(Packet packet) {
		Message message = (Message) packet;
		String chatContent = message.getBody();
		
		if (chatContent.matches(PROBEMSG)) {
			Log.d(TAG, "myCityProbe received from " + StringUtils.parseBareAddress(message.getFrom()));
			
			BuddyHandler.setIsMyCityUser( StringUtils.parseBareAddress(message.getFrom()) );
			return true;
		}
		
		// Return GPX to the prober
		
		return false;
	}

	// Returns true if matched.
	public static boolean processGPX(Packet packet) {
		Message message = (Message) packet;
		String chatContent = message.getBody();
		ArrayList<String> matchRes = GPXParser.parseGPX(chatContent);
		
		if (matchRes != null) {
			Log.d(TAG, "matched GPX received from " + StringUtils.parseBareAddress(message.getFrom()));
			String bareAddr =  StringUtils.parseBareAddress(message.getFrom());
			BuddyHandler.setIsMyCityUser(bareAddr);
			BuddyHandler.updateLocation( bareAddr, 
										 Double.parseDouble(matchRes.get(0)),
										 Double.parseDouble(matchRes.get(1)),
										 matchRes.get(2));
			notifyBuddyLocationClients();
			
			return true;
		}
		
		return false;
	}
	
	// to: null to probe all flagged users (Force Update)
	public static void probeUser(String to) {
		if ( isServiceStarted && mService.isAuthenticated() ) {
			ArrayList<BuddyEntry> buddies = new ArrayList<BuddyEntry>();
			
			Message msg = new Message("", Message.Type.chat);
			msg.setBody(PROBEMSG);
			
			if ( to == null ) {
				// Broadcast
				Log.d(TAG, "Broadcasting PROBEMSG...");
				buddies = BuddyHandler.getMyCityBuddies();
			} else {
				buddies.add( new BuddyEntry("", to, null) );
			}
			
			for (BuddyEntry buddy : buddies) {
				msg.setTo( buddy.getUser() );
				mService.sendPacket(msg);
			}
		}
	}
	
	public static Location getLastKnownLocation() {
		if ( mService != null )
			return mService.getLastKnownLocation();
		return null;
	}
}
