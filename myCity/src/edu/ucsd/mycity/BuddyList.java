package edu.ucsd.mycity;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class BuddyList extends Activity
{

	private ArrayList<String> buddyArray = new ArrayList<String>();
	private ListView listview;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_buddy_list);

		listview = (ListView) this.findViewById(R.id.listMessages);

		final ArrayList<BuddyEntry> roster = BuddyHandler.getBuddies();

		for (BuddyEntry entry : roster)
		{
			String msg;
			if (entry.getPresence().isAvailable())
				msg = "(online)";

			else
				msg = "(offline)";
			buddyArray.add(entry.getName() + " " + msg);
			setListAdapter();
		}

		listview.setOnItemClickListener(new OnItemClickListener()
		{

			public void onItemClick(AdapterView<?> parent, View view,
			         int position, long id)
			{
				String chatContact = roster.get(position).getUser();

				Intent i = new Intent(BuddyList.this, ChatActivity.class);
				Bundle bundle = new Bundle();

				bundle.putString("contact", chatContact);
				i.putExtras(bundle);

				startActivity(i);
			}

		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_buddy_list, menu);
		return true;
	}

	private void setListAdapter()
	{
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
		         R.layout.listbuddy, buddyArray);
		listview.setAdapter(adapter);
	}

}
