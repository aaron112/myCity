package edu.ucsd.mycity;

/**
 * GTalkHandler.java - a singleton class that handles all GTalk and GTalkService related operations
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
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.MultiUserChat;

import edu.ucsd.mycity.listeners.BuddyLocationClient;
import edu.ucsd.mycity.listeners.ChatClient;
import edu.ucsd.mycity.listeners.ConnectionClient;
import edu.ucsd.mycity.listeners.LocationClient;
import edu.ucsd.mycity.listeners.RosterClient;
import edu.ucsd.mycity.utils.GPX;

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


public class GTalkHandler {
	private static final String TAG = "GTalkHandler";
	private static final String PROBEMSG = "myCityProbe";
	public static Context mContext;
	
	private static Roster roster;
	
	private static ArrayList<ChatClient> chatClients = new ArrayList<ChatClient>();
	private static ArrayList<RosterClient> rosterClients = new ArrayList<RosterClient>();
	private static ArrayList<LocationClient> locationClients = new ArrayList<LocationClient>();
	private static ArrayList<ConnectionClient> connectionClients = new ArrayList<ConnectionClient>();
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
				notifyLocationClients();
				break;
			
			case GTalkService.HANDLER_MSG_CONNECTION_UPD:
				Log.d(TAG, "handleMessage: received connection_upd from service");
				notifyConnectionClients();
				break;
				
			default:
				Log.d(TAG, "handleMessage: unknown message received from service");
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
	private static void notifyChatClients(String from) {
		for (int i = 0; i < chatClients.size(); ++i) {
			ChatClient chatClient = (ChatClient)chatClients.get(i);
			chatClient.onChatUpdate(from);
		}
	}

	public static void registerRosterClient(RosterClient o) {
		if ( !rosterClients.contains(o) )
			rosterClients.add(o);
	}
	public static void removeRosterClient(RosterClient o) {
		int i = rosterClients.indexOf(o);
		if (i >= 0) {
			rosterClients.remove(i);
		}
	}
	private static void notifyRosterClients() {
		for (int i = 0; i < rosterClients.size(); ++i) {
			RosterClient rosterClient = (RosterClient)rosterClients.get(i);
			rosterClient.onRosterUpdate();
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
	private static void notifyLocationClients() {
		for (int i = 0; i < locationClients.size(); ++i) {
			LocationClient locationClient = (LocationClient)locationClients.get(i);
			locationClient.onLocationUpdate();
		}
	}

	public static void registerConnectionClient(ConnectionClient o) {
		if ( !connectionClients.contains(o) )
			connectionClients.add(o);
	}
	public static void removeConnectionClient(ConnectionClient o) {
		int i = connectionClients.indexOf(o);
		if (i >= 0) {
			connectionClients.remove(i);
		}
	}
	private static void notifyConnectionClients() {
		for (int i = 0; i < connectionClients.size(); ++i) {
			ConnectionClient connectionClient = (ConnectionClient)connectionClients.get(i);
			connectionClient.onConnectionUpdate();
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
	private static void notifyBuddyLocationClients() {
		for (int i = 0; i < buddyLocationClients.size(); ++i) {
			BuddyLocationClient buddyLocationClient = (BuddyLocationClient)buddyLocationClients.get(i);
			buddyLocationClient.onBuddyLocationUpdate();
		}
	}
	
	public static void startService(Context appContext) {
		if ( isServiceStarted || appContext == null )
			return;
		
		Log.d(TAG, "Attempting to start service.");
		
		mContext = appContext;
		
		Intent intent = new Intent(mContext, GTalkService.class);
		intent.putExtra("callbackMessenger", new Messenger(mHandler));
			
		mContext.startService(intent);
		mContext.bindService(intent, mConn, 0);
	}
	
	public static void stopService() {
		if ( !isServiceStarted )
			return;
		
		isServiceStarted = false;
		BuddyHandler.clear();
		mContext.unbindService(mConn);
		mContext.stopService(new Intent(mContext, GTalkService.class));
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
		//if ( !isServiceStarted )	// Service must be started before connecting
		//	return false;
		
		while ( mService == null ) {}	// Wait until Service is set up
		
		if ( mService.isAuthenticated() )
			return true;
		
		if ( mService.connect() ) {
			mService.setConnection();
			setRoster();
		}
		
		return true;
	}
	
	public static void disconnect() {
		if ( !isServiceStarted )
			return;
		
		mService.disconnect();
	}
	
	public static void setRoster() {
		if ( isServiceStarted && mService.isAuthenticated() ) {
			roster = mService.getRoster();
			BuddyHandler.loadBuddiesFromRoster(roster);
			
			// Roster Update Listener
			roster.addRosterListener(new RosterListener() {
			    public void entriesDeleted(Collection<String> addresses) {
			    	BuddyHandler.loadBuddiesFromRoster(roster);
			    	notifyRosterClients();
			    }
			    public void entriesUpdated(Collection<String> addresses) {
			    	BuddyHandler.loadBuddiesFromRoster(roster);
			    	notifyRosterClients();
			    }
			    public void presenceChanged(Presence presence) {
			    	Log.d(TAG, "Presence changed: " + presence.getFrom() + " " + presence);
			    	BuddyHandler.updatePresense(BuddyHandler.getBareAddr(presence.getFrom()), presence);
			    	
			    	if ( presence.isAvailable() ) {
			    		BuddyEntry buddy = BuddyHandler.getBuddy( BuddyHandler.getBareAddr(presence.getFrom()) );
			    		if ( !buddy.isProbed() )
			    			sendProbe( buddy.getUser() );	// Try to probe user
			    	}
			    	notifyRosterClients();
			    }
				@Override
				public void entriesAdded(Collection<String> arg0) {
					BuddyHandler.loadBuddiesFromRoster(roster);
			    	notifyRosterClients();
				}
			});
		}
		
		/***
		Collection<RosterEntry> entries = roster.getEntries();
        for (RosterEntry entry : entries) {
         // REFERENCE:
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
	
	public static int getChatsListSize() {
		if ( isServiceStarted )
			return mService.getChatsList().size();

		return 0;
	}
	
	// Single user mode only (where chatRoomID == bareAddr)
	public static boolean createChatRoom(String chatRoomID) {
		if ( isServiceStarted )
			return mService.createChatRoom(chatRoomID) != null;
		return false;
	}
	
	// Multi-user mode
	public static boolean createMultiChatRoom(ArrayList<BuddyEntry> buddies, String msg) {
		if ( !isServiceStarted )
			return false;
		
		UUID uid = UUID.randomUUID();
		String chatRoomID = String.format("private-chat-%1s@%2s", uid, "groupchat.google.com");
		MultiUserChat muc = new MultiUserChat(mService.getConnection(), chatRoomID);
		try {
			muc.join( GTalkHandler.getUserBareAddr() );
			
		} catch (XMPPException e) {
			Log.e(TAG, "Error creating multi chat room: " + e.toString());
		}
		
		for ( BuddyEntry buddy : buddies ) {
			muc.invite(buddy.getUser(), msg);
		}
		
		return true;
	}
	
	public static ChatRoom getChatRoom(String chatRoomName) {
		Log.d(TAG, "getChatRoom with "+ chatRoomName);
		if ( isServiceStarted )
			return mService.findChatRoom(chatRoomName);
		
		Log.d(TAG, "Service is not started.");
		return null;
	}
	
	public static HashMap<String, ChatRoom> getChatsList() {
		if ( isServiceStarted )
			return mService.getChatsList();

		return null;
	}
	
	// Return bool indicates if send is successful
	public static boolean sendMessage(Message msg) {
		if ( isServiceStarted && mService.isAuthenticated() ) {
	        mService.sendPacket(msg);
			return true;
		}
		
		return false;
	}
	
	public static String getUserBareAddr() {
		if ( isServiceStarted && mService.isAuthenticated() )
			return BuddyHandler.getBareAddr( mService.getConnection().getUser() );
		return "";
	}
	
	public static RosterEntry lookupUser(String bareAddr) {
		Collection<RosterEntry> entries = mService.getRoster().getEntries();
        for (RosterEntry entry : entries) {
        	if ( BuddyHandler.getBareAddr( entry.getUser() ).equals(bareAddr) )
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
	public static boolean processProbe(Message message) {
		String chatContent = message.getBody();
		
		if (chatContent.matches(PROBEMSG)) {
			Log.d(TAG, "myCityProbe received from " + BuddyHandler.getBareAddr(message.getFrom()));
			BuddyHandler.getBuddy( BuddyHandler.getBareAddr(message.getFrom()) ).setMyCityUser(true);
			return true;
		}
		
		// Return GPX to the prober
		
		return false;
	}
	
	// Returns true if matched.
	public static boolean processGPX(Message message) {
		String chatContent = message.getBody();
		ArrayList<String> matchRes = GPX.parseGPX(chatContent);
		
		if (matchRes != null) {
			Log.d(TAG, "matched GPX received from " + BuddyHandler.getBareAddr(message.getFrom()));
			String bareAddr =  BuddyHandler.getBareAddr(message.getFrom());
			BuddyHandler.getBuddy( BuddyHandler.getBareAddr(message.getFrom()) ).setMyCityUser(true);
			BuddyHandler.updateLocation( bareAddr, 
										 Double.parseDouble(matchRes.get(0)),
										 Double.parseDouble(matchRes.get(1)),
										 matchRes.get(2));
			notifyBuddyLocationClients();
			
			return true;
		}
		
		return false;
	}
	
	// bareAddr: null to probe all flagged users (Force Update)
	public static void sendProbe(String bareAddr) {
		if ( isServiceStarted && mService.isAuthenticated() ) {
			ArrayList<BuddyEntry> buddies = new ArrayList<BuddyEntry>();
			
			if ( bareAddr == null ) {
				// Broadcast
				buddies = BuddyHandler.getMyCityBuddies();
				if (buddies.size() == 0) {
					Log.d(TAG, "No my city buddy on list.");
					return;
				}
			} else {
				buddies.add( BuddyHandler.getBuddy(bareAddr) );
			}
			
			Message msg = new Message("", Message.Type.chat);
			msg.setBody(PROBEMSG);
			
			for (BuddyEntry buddy : buddies) {
				Log.d(TAG, "Probing: "+buddy.getUser());
				buddy.setProbed(true);
				msg.setTo( buddy.getUser() );
				mService.sendPacket(msg);
			}
		}
	}
	
	// sendNewLocation: 
	// loc: new location
	// to: bare address or null for broadcasting
	// Return: turn if sent, false otherwise
	public static boolean sendNewLocation(Location loc, String bareAddr) {
		if ( isServiceStarted && mService.isAuthenticated() ) {
			Log.d(TAG, "Broadcasting new location...");

			ArrayList<BuddyEntry> buddies = new ArrayList<BuddyEntry>();
			
			if ( bareAddr == null ) {
				// Broadcast
				buddies = BuddyHandler.getMyCityBuddies();
				if (buddies.size() == 0) {
					Log.d(TAG, "No my city buddy on list.");
					return false;
				}
			} else {
				buddies.add( BuddyHandler.getBuddy(bareAddr) );
			}
			
			Message msg = new Message("", Message.Type.chat);
			msg.setBody( GPX.buildGPX(loc.getLatitude(), loc.getLongitude(), loc.getAltitude(), loc.getTime()) );
			
			for (BuddyEntry buddy : buddies) {
				Log.d(TAG, "New Location sent to: "+buddy.getUser());
				msg.setTo( buddy.getUser() );
				mService.sendPacket(msg);
			}
			
			return true;
		}
		
		return false;
	}
	
	public static Location getLastKnownLocation() {
		if ( mService != null )
			return mService.getLastKnownLocation();
		return null;
	}
}
