package edu.ucsd.mycity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

public class LoadUserContActivity extends Activity
{
	public static final String TAG = "LoadUserContActivity";
	private TextView name;
	private TextView description;
	private ImageView pic;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_load_user_cont);

		Bundle b = getIntent().getExtras();

		name = (TextView) findViewById(R.id.nameView);
		name.setText(b.getString("name"));

		description = (TextView) findViewById(R.id.descriptionView);
		description.setText(b.getString("description"));

		String url = "http://mycity-110.appspot.com/serve?blob-key="
		         + b.getString("picKey");

		pic = (ImageView) findViewById(R.id.showImage);

		if (b.getString("picKey").equals(""))
			pic.setImageBitmap(null);
		else
			pic.setImageBitmap(UserContHandler.getImageFromWeb(url));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.load_user_cont, menu);
		return true;
	}

}
