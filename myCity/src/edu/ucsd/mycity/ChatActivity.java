package edu.ucsd.mycity;

/**
 * ChatActivity.java - Chat Activity
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
import java.util.HashMap;

import edu.ucsd.mycity.R;
import edu.ucsd.mycity.buddy.BuddyList;
import edu.ucsd.mycity.chat.ChatMsgArrayAdapter;
import edu.ucsd.mycity.chat.ChatRoom;
import edu.ucsd.mycity.chat.ChatRoom.ChatMessage;
import edu.ucsd.mycity.listeners.ChatClient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

public class ChatActivity extends Activity implements ChatClient {
	private final static String TAG = "ChatActivity";
	
	public final static int CHAT_MODE_SINGLE = 0;
	public final static int CHAT_MODE_MULTI = 1;
	
	private ChatRoom mChatRoom;
	private String mChatRoomID;
	private HashMap<String, ChatRoom> chatRooms;
	private ArrayList<String> chatIDs;
	private ArrayList<String> chatTitles;	// Display name of Chat rooms
	
	private ArrayList<ChatMessage> messages = new ArrayList<ChatMessage>();
	private ChatMsgArrayAdapter msgListAdapter;
	//private ArrayList<String> chats = new ArrayList<String>();
	//private String contact;	// Current Contact for this CharActivity

	private Spinner chating_with;
	private EditText textMessage;
	private ListView listview;
  
	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
	    Log.d(TAG, "onCreate");

		chating_with = (Spinner) this.findViewById(R.id.chating_with);
		textMessage = (EditText) this.findViewById(R.id.chatET);
		listview = (ListView) this.findViewById(R.id.listMessages);
		listview.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

		//msgListAdapter = new ChatMsgArrayAdapter(this, R.layout.chatmsg_listitem, R.id.chattext, messages);
	    //listview.setAdapter(msgListAdapter);
	    //listview.setStackFromBottom(true);
		
		Bundle b = getIntent().getExtras();
		mChatRoomID = b.getString("contact");
		
		buildChatList();
		
		if ( mChatRoomID == null || mChatRoomID.equals("") ) {
			Log.d(TAG, "getLastMsgFrom");
			mChatRoomID = GTalkHandler.getLastMsgFrom();
			
			if ( mChatRoomID == null || mChatRoomID.equals("") ) {
				Log.d(TAG, "Falling back to the first conversation on list.");
				if (chatRooms.keySet().iterator().hasNext())
					mChatRoomID = chatRooms.keySet().iterator().next();
			}
		}
		
		if ( mChatRoomID == null || mChatRoomID.equals("") ) {
			Log.d(TAG, "finishing because no lastmsgfrom and chat is empty.");
			Toast.makeText(this, "Start a new conversation from Map or Contact List",
					Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		
		Log.i(TAG, "mChatRoomID = " + mChatRoomID);

		// If chat room does not exist, create it
		if ( !chatRooms.containsKey(mChatRoomID) ) {
			Log.d(TAG, "Chat room does not exist, asking GTalkHandler to create it.");
			GTalkHandler.createChatRoom(mChatRoomID);
		}

		// Initial chat room switch done when spinner initialized
		buildChatList();
		Log.d(TAG, "Initial chat room switch.");
		switchChatRoom(mChatRoomID);
		//Log.d(TAG, "Initial chat room switch done.");
		
		// Set a listener to switch chat "window"
		chating_with.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				//switchChatRoom( (String)parent.getSelectedItem() );
				Log.d(TAG, "onItemSelected called.");
				Log.i(TAG, "pos = "+pos+", chatIDs = "+chatIDs.toString());
				switchChatRoom( chatIDs.get(pos) );
			}
    	
			@Override
			public void onNothingSelected(AdapterView <? > parentView) {
				// Do Nothing
			}
		});

		// Set a listener to send a chat text message
		Button send = (Button) this.findViewById(R.id.sendBtn);
		send.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				String text = textMessage.getText().toString();
				textMessage.setText(""); // Clear textfield
        
				Log.i(TAG, "Sending text " + text + " to " + mChatRoomID);
				
				if ( mChatRoom.sendMessage(text) ) {
					buildMsgList();
				} else {
					runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(ChatActivity.this, "Cannot send message when offline!", Toast.LENGTH_LONG).show();
						}
					});
				}
			}
		});
    
		// Register with GTalkHandler to get updates
		GTalkHandler.registerChatClient(this);
	}
  
	@Override
	protected void onResume() {
	    super.onResume();
	    Log.d(TAG, "onResume");
	    GTalkHandler.registerChatClient(this);
	}
	
	@Override
	protected void onPause() {
	    super.onPause();
	    Log.d(TAG, "onPause");
	    GTalkHandler.removeChatClient(this);
	}
	
	@Override
	protected void onDestroy() {
	    GTalkHandler.removeChatClient(this);
	    Log.d(TAG, "onDestroy");
		super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_chat, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	    case R.id.menu_buddyList:
	    	startActivity(new Intent(this, BuddyList.class));
	    	return true;
	    	
	    case R.id.menu_closechat:
	    	GTalkHandler.removeFromChatsList(mChatRoomID);
    		buildChatList();
    		
	    	if ( chatRooms.isEmpty() )
	    		finish();
	    	else {
	    		switchChatRoom(chatRooms.keySet().iterator().next());
	    	}
	    	
	    	return true;

	    case R.id.menu_settings:
	    	startActivity(new Intent(this, SettingsActivity.class));
	    	return true;
	    	
	    default:
	    	return super.onOptionsItemSelected(item);
	    }
	}
	
  
	public void onChatUpdate(String from) {
		Log.i(TAG, "onChatUpdate called");
		
		// runOnUiThread needed to change UI components
		runOnUiThread(new Runnable() {
		    public void run() {
		    	// Refresh chat list from GTalkHandler and GTalkService
				buildChatList();
		    }
		});
		
		if (from.equals(this.mChatRoomID)) {
			runOnUiThread(new Runnable() {
			    public void run() {
			    	buildMsgList();
			    }
			});
		}
	}

	private void buildChatList() {
		chatRooms = GTalkHandler.getChatsList();
		chatIDs = new ArrayList<String>();	// Array of chat IDs NEEDED to match spinner options later
		chatTitles = new ArrayList<String>();
		
		for (HashMap.Entry<String, ChatRoom> entry : chatRooms.entrySet()) {
			chatIDs.add(entry.getKey());
			chatTitles.add(entry.getValue().getTitle());
		}
		
	    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, chatTitles);
	    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    chating_with.setAdapter(spinnerArrayAdapter);
	    chating_with.setSelection( chatIDs.indexOf( mChatRoomID ), true);
	}
	
	private void buildMsgList() {
		Log.d(TAG, "updateMsgList: mChatRoomID = " + mChatRoomID);
		
		if (mChatRoom == null)
			return;
		
		messages = mChatRoom.getMessages();
		
		msgListAdapter = new ChatMsgArrayAdapter(this, R.layout.chatmsg_listitem, R.id.chat_text, messages, mChatRoom.isMultiUser());
	    listview.setAdapter(msgListAdapter);
	    listview.setStackFromBottom(true);
	}
	
	private void switchChatRoom(String chatRoomID) {
		Log.i(TAG, "switchChatRoom: " + chatRoomID);
		
		this.mChatRoomID = chatRoomID;
		this.mChatRoom = chatRooms.get(chatRoomID);
		buildMsgList();
	}

}