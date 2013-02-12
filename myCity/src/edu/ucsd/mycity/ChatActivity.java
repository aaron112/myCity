package edu.ucsd.mycity;

import java.util.ArrayList;
import java.util.Collection;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class ChatActivity extends Activity {
	
  private ArrayList<String> messages = new ArrayList<String>();
  private Handler mHandler = new Handler();

  private EditText recipient;
  private EditText textMessage;
  private ListView listview;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_chat);
    
    GTalkHandler.prefs = getApplicationContext().getSharedPreferences("edu.ucsd.mycity_preferences", 0);
    
    recipient = (EditText) this.findViewById(R.id.toET);
    textMessage = (EditText) this.findViewById(R.id.chatET);
    listview = (ListView) this.findViewById(R.id.listMessages);
    setListAdapter();

    // Set a listener to send a chat text message
    Button send = (Button) this.findViewById(R.id.sendBtn);
    send.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        String to = recipient.getText().toString();
        String text = textMessage.getText().toString();          
        Log.i("XMPPChatDemoActivity ", "Sending text " + text + " to " + to);
        Message msg = new Message(to, Message.Type.chat);  
        msg.setBody(text);
        if (GTalkHandler.connection != null) 
        {
          //String realMsg = MySmack.processAppointment(msg);
          //msg.setBody(realMsg);
          GTalkHandler.connection.sendPacket(msg);
          messages.add(GTalkHandler.connection.getUser() + ":");
          messages.add(text);
          setListAdapter();
        }
      }
    });
  }

  private void setListAdapter() {
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.listitem, messages);
    listview.setAdapter(adapter);
  }

}