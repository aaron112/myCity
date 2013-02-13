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
	public static final int SENDMSG_HIDDEN = 0;
	public static final int SENDMSG_NORMAL = 1;
	
	private static final String TAG = "GTalkHandler";
	private static final String PROBEMSG = "myCityProbe";
	public static Context context;
	
	private static Roster roster;
	
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
		if ( !isServiceStarted )
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
	
	public static ArrayList<String> getChatsList() {
		return mService.getChatsList();
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
		
		return false;
	}

	// Returns true if matched.
	public static boolean processGPX(Packet packet) {
		Message message = (Message) packet;
		String chatContent = message.getBody();
		ArrayList<String> matchRes = GPXParser.parseGPX(chatContent);
		
		if (matchRes != null) {
			Log.d(TAG, "matched GPX received from " + StringUtils.parseBareAddress(message.getFrom()));
			
			BuddyHandler.updateLocation( StringUtils.parseBareAddress(message.getFrom()), 
										 Double.parseDouble(matchRes.get(0)),
										 Double.parseDouble(matchRes.get(1)),
										 matchRes.get(2));
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
}
