package edu.ucsd.mycity.chat;

import java.util.ArrayList;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.MultiUserChat;

import android.util.Log;

import edu.ucsd.mycity.GTalkHandler;
import edu.ucsd.mycity.buddy.BuddyEntry;

/**
 * Design by Contract
 * @invariant title != null, isMultiUser is boolean, either sc or muc != null
 * @author Aaron
 *
 */
@SuppressWarnings("unused")
public class ChatRoom {
	public class ChatMessage {
		private BuddyEntry from;
		private String msg;
		
		/**
		 * @pre from is BuddyEntry, msg != null
		 */
		public ChatMessage( BuddyEntry from, String msg ) {
			this.from = from;
			this.msg = msg;
		}
		
		/**
		 * @post return null / BuddyEntry
		 */
		public BuddyEntry getFrom() {
			return from;
		}
		
		/**
		 * @post return String
		 */
		public String getMsg() {
			return msg;
		}
	}
	
	private String title;
	private boolean isMultiUser;
	
	private Chat sc = null;
	private MultiUserChat muc = null;
	
	// Single user mode: user addr, Multi-user mode: chatroom addr
	private BuddyEntry participant;
	
	// Multi-user mode
	private ArrayList<BuddyEntry> participants = new ArrayList<BuddyEntry>();	
	private ArrayList<ChatMessage> messages = new ArrayList<ChatMessage>();
	
	/**
	 * Single-user mode
	 * @pre newsc != null, participant != null
	 * 
	 */
	public ChatRoom(Chat newsc, BuddyEntry participant) {
		isMultiUser = false;
		sc = newsc;
		
		this.participant = participant;
		this.title = participant.getName();
	}
	
	/**
	 * Multi-user mode
	 * @pre newmuc != null, title != null
	 */
	public ChatRoom(MultiUserChat newmuc, String title) {
		isMultiUser = true;
		muc = newmuc;
		
		this.title = title;
	}
	
	/**
	 * @post return boolean
	 */
	public boolean isMultiUser() {
		return isMultiUser;
	}
	
	/**
	 * @post return String != null
	 */
	public String getTitle() {
		if ( title == null )
			return "";
		return title;
	}
	
	/**
	 * @pre title != null
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * Add a message to the message list
	 * @pre contact is BuddyEntry (can be null), msg is String != null
	 */
	public void addMessage(BuddyEntry contact, String msg) {
		messages.add( new ChatMessage(contact, msg) );
	}
	
	/**
	 * @pre newsc != null
	 */
	public void setChat(Chat newsc) {
		sc = newsc;
	}
	
	/**
	 * @pre newmuc != null
	 */
	public void setMulChat(MultiUserChat newmuc) {
		muc = newmuc;
	}
	
	/**
	 * Return boolean indicates if send is successful
	 * @pre message != null
	 * @post returns boolean
	 */
	public boolean sendMessage(String message) {
		addMessage(null, message);	// contact = null indicates myself
		
		if ( isMultiUser ) {
			try {
				Log.i("ChatRoom", "groupchat sendMessage: "+message);
				muc.sendMessage("<from>"+GTalkHandler.getUserBareAddr()+"</from>"+message);
			} catch (XMPPException e) {
				return false;
			}
			return true;
		}
		
		// Single-user mode
		if ( !participant.getPresence().isAvailable() )
			addMessage(null, "(The recipient is offline, message will be delivered when the user is online.)");
		
		Message msg = new Message(participant.getUser(), Message.Type.chat);
		msg.setBody(message);
		return GTalkHandler.sendMessage(msg);
	}
	
	/**
	 * @pre buddles != null, invitemsg != null
	 * @post returns boolean
	 */
	public boolean addParticipants(ArrayList<BuddyEntry> buddies, String invitemsg) {
		// Ignore request if in single-user mode
		if ( !isMultiUser || muc == null )
			return false;
		
		for ( BuddyEntry buddy : buddies ) {
			addMessage(null, "Inviting: " + buddy.getUser());
			muc.invite(buddy.getUser(), invitemsg);
			GTalkHandler.sendGroupChatInvitation( muc, buddy.getUser(), invitemsg );
		}
		
		return true;
	}
	
	/**
	 * @post returns BuddyEntry
	 */
	public BuddyEntry getParticipant() {
		return participant;
	}
	
	/**
	 * @post returns ArrayList<BuddyEntry> != null
	 */
	public ArrayList<BuddyEntry> getParticipants() {
		return participants;
	}
	
	/**
	 * @post returns ArrayList<ChatMessage> != null
	 */
	public ArrayList<ChatMessage> getMessages() {
		return messages;
	}
	
	/**
	 * @pre none
	 */
	public void closeChat() {
		if ( isMultiUser )
			muc.leave();
	}
}
