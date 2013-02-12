package edu.ucsd.mycity;

import java.util.Collection;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;

import android.content.SharedPreferences;
import android.util.Log;

public class GTalkHandler {
	public static final String HOST = "talk.google.com";
	public static final int PORT = 5222;
	public static final String SERVICE = "gmail.com";
	
	public static SharedPreferences prefs;
	public static Roster roster;
	
	//public static final String USERNAME = "cse110winter2013@gmail.com";
	//public static final String PASSWORD = "billgriswold";
	
	public static XMPPConnection connection;
	
	// Connect to GTalk XMPP Server, returns true if successful
	public static boolean connect() {
		// Read Config
	    String username = prefs.getString("gtalk_username", "");
	    String password = prefs.getString("gtalk_password", "");
	    
	    ConnectionConfiguration connConfig = new ConnectionConfiguration(HOST, PORT, SERVICE);
		connection = new XMPPConnection(connConfig);
		try {
			connection.connect();
			Log.i("GTalkHandler - Connection", "Connected to " + connection.getHost());
		} catch (XMPPException e) {
			Log.e("GTalkHandler - Connection", "Failed to connect to Gtalk server.");
			return false;
		}
		try {
			connection.login(username, password);
			Log.i("GTalkHandler - Logging in", "Logged in as " + connection.getUser());
		} catch (XMPPException e) {
			Log.e("GTalkHandler - Logging in", "Failed to log in as " + username + ".");
			return false;
		}
	    
	    return true;
	}

	/**
	 * Called by Settings dialog when a connection is establised with 
	 * the XMPP server
	 */
	public static void setupConnection() {
	    if (connection != null) {
	      // Add a packet listener to get messages sent to us
	      PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
	      connection.addPacketListener(new PacketListener() {
	        @Override
	        public void processPacket(Packet packet) {
	          Message message = (Message) packet;
	          if (message.getBody() != null) {
	            String fromName = StringUtils.parseBareAddress(message.getFrom());
	            Log.i("GTalkHandler ", " Text Recieved " + message.getBody() + " from " +  fromName);
	            
	            
	            // TODO: Process incoming message
	            //messages.add(fromName + ":");
	            //messages.add(message.getBody());
	            
	            // Add the incoming message to the list view
	            //mHandler.post(new Runnable() {
	            //  public void run() {
	            //    setListAdapter();
	            //  }
	            //});
	          }
	        }
	      }, filter);
	    }
	}
	
	public static void updateRoaster() {
		roster = connection.getRoster();
		Collection<RosterEntry> entries = roster.getEntries();
        for (RosterEntry entry : entries) {
         // TODO:
          Log.d("XMPPChatDemoActivity",  "--------------------------------------");
          Log.d("XMPPChatDemoActivity", "RosterEntry " + entry);
          Log.d("XMPPChatDemoActivity", "User: " + entry.getUser());
          Log.d("XMPPChatDemoActivity", "Name: " + entry.getName());
          Log.d("XMPPChatDemoActivity", "Status: " + entry.getStatus());
          Log.d("XMPPChatDemoActivity", "Type: " + entry.getType());
          Presence entryPresence = roster.getPresence(entry.getUser());

          Log.d("XMPPChatDemoActivity", "Presence Status: "+ entryPresence.getStatus());
          Log.d("XMPPChatDemoActivity", "Presence Type: " + entryPresence.getType());

          Presence.Type type = entryPresence.getType();
          if (type == Presence.Type.available)
            Log.d("XMPPChatDemoActivity", "Presence AVIALABLE");
          Log.d("XMPPChatDemoActivity", "Presence : " + entryPresence);
       }
	}
	
}
