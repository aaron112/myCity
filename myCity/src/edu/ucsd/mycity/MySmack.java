package edu.ucsd.mycity;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import android.util.Log;

/****
 * 
 * @author cs110w
 * 
 *         This class defines a layer between aSmack library and the application, such that:
 * 
 *         1. it has a simpler interface to work with 
 *         2. the library can be more readable 
 *         3. it will make the test easier
 * 
 *         In this class we will require you to define your own interface on top of aSmack
 * 
 */

public class MySmack {

	/* Section-1 */
	// A sample of setting up a logged-in connection to Gtalk Server
	// 		Usage: connectToGtalk("username", "password");
	public static XMPPConnection connectToGtalk(String username, String password) {
		ConnectionConfiguration connConfig = new ConnectionConfiguration(
				"talk.google.com", 5222, "gmail.com");
		XMPPConnection connection = new XMPPConnection(connConfig);
		try {
			connection.connect();
			Log.i("XMPP - Connection", "Connected to " + connection.getHost());
		} catch (XMPPException e) {
			Log.e("XMPP - Connection", "Failed to connect to  Gtalk server.");
		}
		try {
			connection.login(username, password);
			Log.i("XMPP - Logging in", "Logged in as " + connection.getUser());
		} catch (XMPPException e) {
			Log.e("XMPP - Logging in", "Failed to log in as " + username + ".");
		}
		return connection;
	}

	
	/* Section-2 */
	// An exercise of setting up a logged-in connection to Gtalk Server,
	// which sets the present status upon logging in.
	// 		Usage: connectToGtalk("username", "password", "available");
	// Note that the last parameter is the presence type, which can be:
	// 		either "available", or "unavailability"
	// Also note that the last parameter is of type String.
	public static XMPPConnection connectToGtalk(String username, String password, String avaliability) {
		ConnectionConfiguration connConfig = new ConnectionConfiguration(
				"talk.google.com", 5222, "gmail.com");
		XMPPConnection connection = new XMPPConnection(connConfig);
		try {
			connection.connect();
			Log.i("XMPP - Connection", "Connected to " + connection.getHost());
		} catch (XMPPException e) {
			Log.e("XMPP - Connection", "Failed to connect to  Gtalk server.");
		}
		try {
			connection.login(username, password);
			Log.i("XMPP - Logging in", "Logged in as " + connection.getUser());
			
			if(avaliability.compareTo("available") == 0)
			{
				Presence presence = new Presence(Presence.Type.available);
				presence.setStatus("I'm available");
				connection.sendPacket(presence);
			}
			else
			{
				Presence presence = new Presence(Presence.Type.unavailable);
				presence.setStatus("I'm unavailable");
				connection.sendPacket(presence);
			}
			
		} catch (XMPPException e) {
			Log.e("XMPP - Logging in", "Failed to log in as " + username + ".");
		}
		
		return connection;
	}
	
	/* Section-3 */
	// An sample enhanced functionality
	// 		Usage:
	// Upon receiving a chat message, check to see if it is in the format
	// 		[APPOINTMENT]sometime@somelocation (e.g. "[APPOINTMENT]12:30pm@UCSD Library")
	// if it is not, treat it as a normal chat message
	// but if it is, process it and, before posting it, reformat it into
	// 		You have an appointment on sometime at somelocation.
	// 		e.g. "You have an appointment on 12:30pm at UCSD Library."
	public static String processAppointment(Packet packet) {
		Message message = (Message) packet;
		String chatContent = message.getBody();
		
		if (chatContent.matches(
				"\\[APPOINTMENT\\]([1-9]|1[012]):[0-5][0-9](?i)(am|pm)@[a-zA-Z0-9]+(\\s[a-zA-Z0-9]+)*")) {
			String shortNotice = chatContent.substring(13);
			String[] elements = shortNotice.split("@");
			return "You have an appointment on " + elements[0] + " at " + elements[1] + ".";
		}
		else
			return chatContent;
	}
	
}
