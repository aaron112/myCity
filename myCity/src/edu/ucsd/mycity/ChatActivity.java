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
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class ChatActivity extends Activity {

  public static final String HOST = "talk.google.com";
  public static final int PORT = 5222;
  public static final String SERVICE = "gmail.com";
  public static final String USERNAME = "cse110winter2013@gmail.com";
  public static final String PASSWORD = "billgriswold";

  private XMPPConnection connection;
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
        if (connection != null) 
        {
          //String realMsg = MySmack.processAppointment(msg);
          //msg.setBody(realMsg);
          connection.sendPacket(msg);
          messages.add(connection.getUser() + ":");
          messages.add(text);
          setListAdapter();
        }
      }
    });
    connect();
  }

  /**
   * Called by Settings dialog when a connection is establised with 
   * the XMPP server
   */
  public void setConnection(XMPPConnection connection) {
    this.connection = connection;
    if (connection != null) {
      // Add a packet listener to get messages sent to us
      PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
      connection.addPacketListener(new PacketListener() {
        @Override
        public void processPacket(Packet packet) {
          Message message = (Message) packet;
          if (message.getBody() != null) {
            String fromName = StringUtils.parseBareAddress(message.getFrom());
            Log.i("XMPPChatDemoActivity ", " Text Recieved " + message.getBody() + " from " +  fromName);
            
            messages.add(fromName + ":");
            messages.add(MySmack.processAppointment(packet));
            //messages.add(message.getBody());
            // Add the incoming message to the list view
            mHandler.post(new Runnable() {
              public void run() {
                setListAdapter();
              }
            });
          }
        }
      }, filter);
    }
  }

  private void setListAdapter() {
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.listitem, messages);
    listview.setAdapter(adapter);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    try {
      connection.disconnect();
    } catch (Exception e) {

    }
  }

  public void connect() {

    final ProgressDialog dialog = ProgressDialog.show(this, "Connecting...", "Please wait...", false);
    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {

         //try {
        	 XMPPConnection connection = MySmack.connectToGtalk(USERNAME, PASSWORD, "available");

            //connection.login(USERNAME, PASSWORD);
            Log.i("XMPPChatDemoActivity",  "Logged in as" + connection.getUser());

            // Set the status to available
            //Presence presence = new Presence(Presence.Type.available);
            //connection.sendPacket(presence);
            setConnection(connection);
            

            Roster roster = connection.getRoster();
            Collection<RosterEntry> entries = roster.getEntries();
            for (RosterEntry entry : entries) {

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
             /* } catch (XMPPException ex) {
                Log.e("XMPPChatDemoActivity", "Failed to log in as "+  USERNAME);
                Log.e("XMPPChatDemoActivity", ex.toString());
                setConnection(null);
              }*/
              dialog.dismiss();
           }
      });
    t.start();
    dialog.show();
  }
}