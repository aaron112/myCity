package edu.ucsd.mycity;

import java.util.ArrayList;

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
	
	private ArrayList<String> messages = new ArrayList<String>();
	private ArrayList<String> chats = new ArrayList<String>();
	
	private String contact;	// Current Contact for this CharActivity

	private Spinner chating_with;
	private EditText textMessage;
	private ListView listview;
  

	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
    
		Bundle b = getIntent().getExtras();
		contact = b.getString("contact");
    
		chating_with = (Spinner) this.findViewById(R.id.chating_with);
		textMessage = (EditText) this.findViewById(R.id.chatET);
		listview = (ListView) this.findViewById(R.id.listMessages);
		listview.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
    
		// Set a listener to switch chat "window"
		chating_with.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				changeChat( (String)parent.getSelectedItem() );
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
        
				Log.i(TAG, "Sending text " + text + " to " + contact);
        
				if ( GTalkHandler.sendMessage(contact, text, GTalkHandler.SENDMSG_NORMAL) ) {
					updateMsgList();
				} else {
					runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(ChatActivity.this, "Cannot send message when offline!", Toast.LENGTH_LONG).show();
						}
					});
				}
			}
		});
		
		if (contact.equals(""))
			contact = GTalkHandler.getLastMsgFrom();
		if (contact == null)
			finish();
	
		updateMsgList();
		updateChatList();
    
		// Register with GTalkHandler to get updates
		GTalkHandler.registerChatClient(this);
	}
  
	@Override
	protected void onResume() {
	    super.onResume();
	    GTalkHandler.registerChatClient(this);
	}
	
	@Override
	protected void onPause() {
	    super.onPause();
	    GTalkHandler.removeChatClient(this);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	    GTalkHandler.removeChatClient(this);
		//
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
	    	GTalkHandler.removeFromChatsList(contact);
    		updateChatList();
    		
	    	if ( chats.isEmpty() )
	    		finish();
	    	else {
	    		contact = chats.get(0);
	    		updateChatList();
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
		
		// Refresh chat list from GTalkHandler and GTalkService
		updateChatList();
		if (from.equals(this.contact)) {
			updateMsgList();
		}
	}
	
	private void updateMsgList() {
		messages = GTalkHandler.getMessages(contact);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.listitem, messages);
	    listview.setAdapter(adapter);
	    listview.setStackFromBottom(true);
	}
	
	private void updateChatList() {
		chats = GTalkHandler.getChatsList();
	    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, chats);
	    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    chating_with.setAdapter(spinnerArrayAdapter);
	}
	
	private void changeChat(String contact) {
		this.contact = contact;
		updateMsgList();
	}

}