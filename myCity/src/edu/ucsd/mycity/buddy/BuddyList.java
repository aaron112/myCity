package edu.ucsd.mycity.buddy;

/**
 * BuddyList.java - BuddyList Activity
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

import edu.ucsd.mycity.BuddyHandler;
import edu.ucsd.mycity.ChatActivity;
import edu.ucsd.mycity.GTalkHandler;
import edu.ucsd.mycity.R;
import edu.ucsd.mycity.SettingsActivity;
import edu.ucsd.mycity.listeners.RosterClient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class BuddyList extends Activity implements RosterClient
{
	private final static String TAG = "BuddyList";

	private ArrayAdapter<String> listviewAdapter = null;
	private ArrayList<BuddyEntry> roster;
	private ListView listview;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_buddy_list);
		listview = (ListView) this.findViewById(R.id.listMessages);
		
		listview.setOnItemClickListener(new OnItemClickListener()
		{

			public void onItemClick(AdapterView<?> parent, View view,
			         int position, long id)
			{
				String chatContact = roster.get(position).getUser();

				Intent i = new Intent(getApplicationContext(), ChatActivity.class);
				Bundle bundle = new Bundle();

				bundle.putString("contact", chatContact);
				i.putExtras(bundle);

				startActivity(i);
			}

		});

		listviewAdapter = new ArrayAdapter<String>(this, R.layout.listbuddy);
		listview.setAdapter(listviewAdapter);
		buildList();
		
		// Register with GTalkHandler to get updates
		GTalkHandler.registerRosterClient(this);
	}

	@Override
	protected void onResume() {
	    super.onResume();
	    GTalkHandler.registerRosterClient(this);
	}
	
	@Override
	protected void onPause() {
	    super.onPause();
	    GTalkHandler.removeRosterClient(this);
	}
	
	@Override
	protected void onDestroy() {
	    GTalkHandler.removeRosterClient(this);
		super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_buddy_list, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	    case R.id.menu_settings:
	    	startActivity(new Intent(this, SettingsActivity.class));
	    	return true;
	    	
	    default:
	    	return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	public void onRosterUpdate() {
		Log.d(TAG, "onRosterUpdate");

		// runOnUiThread needed to change UI components
		runOnUiThread(new Runnable() {
		    public void run() {
		    	buildList();
		    }
		});
	}
	
	private void buildList()
	{
		roster = BuddyHandler.getBuddies();
		listviewAdapter.clear();
		
		for (BuddyEntry entry : roster)
		{
			String display;
			
			if ( !entry.getName().equals(entry.getUser()) )
				display = entry.getName() + " (" + entry.getUser() + ")\n";
			else
				display = entry.getName() + "\n";
			
			if ( entry.getPresence().isAvailable() )
				display += "(Online)";
			else
				display += "(Offline)";
			
			listviewAdapter.add(display);
		}
		
		listviewAdapter.notifyDataSetChanged();
	}


}
