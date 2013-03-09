package edu.ucsd.mycity;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class LoadUserContActivity extends Activity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_load_user_cont);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.load_user_cont, menu);
		return true;
	}

}
