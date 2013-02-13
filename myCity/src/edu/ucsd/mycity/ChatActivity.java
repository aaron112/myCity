package edu.ucsd.mycity;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
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
  //private Handler mHandler = new Handler();
  private String contact;	// Current Contact for this CharActivity

  //private TextView recipient;
  private Spinner chating_with;
  private EditText textMessage;
  private ListView listview;
  

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_chat);
    
    Bundle b = getIntent().getExtras();
    contact = b.getString("contact");
    
    chating_with = (Spinner) this.findViewById(R.id.chating_with);
    textMessage = (EditText) this.findViewById(R.id.chatET);
    listview = (ListView) this.findViewById(R.id.listMessages);
    
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
        
        if ( GTalkHandler.sendMessage(contact, text) ) {
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
	
	GTalkHandler.updateRoaster();
	
	updateMsgList();
    updateChatList();
    
	// Register with GTalkHandler to get updates
	GTalkHandler.registerObserver(this);
}
  
	public void onUpdate(String from) {
		// TODO: Called when there are new messages
		// Add the incoming message to the list view
		Log.i(TAG, "onUpdate called");
		
		updateChatList();
		
		if (from.equals(this.contact)) {
			updateMsgList();
		}
	}
	
	private void updateMsgList() {
		messages = GTalkHandler.getMessages(contact);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.listitem, messages);
	    listview.setAdapter(adapter);
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