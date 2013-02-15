package edu.ucsd.mycity;

import java.util.ArrayList;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class BuddyList extends Activity {

	private ArrayList<String> buddyArray = new ArrayList<String>();
	private ListView listview;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_buddy_list);
		
		listview = (ListView) this.findViewById(R.id.listMessages);
		
		ArrayList<BuddyEntry> roster = BuddyHandler.getBuddies();
		
		for(BuddyEntry entry : roster)
		{
			buddyArray.add(entry.getUser() + " " + entry.getPresence().getStatus());
			setListAdapter();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_buddy_list, menu);
		return true;
	}
	
	private void setListAdapter()
	{
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.listitem, buddyArray);
		listview.setAdapter(adapter);
	}

}
