package edu.ucsd.mycity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AddUserContActivity extends Activity
{
	public static final String TAG = "AddUserContActivity";

	private Bundle extras;
	private EditText place_box;
	private EditText description_box;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_user_cont);
		Button submit = (Button) findViewById(R.id.submit_btn);
		place_box = (EditText) findViewById(R.id.place_box);
		description_box = (EditText) findViewById(R.id.description_box);
		extras = getIntent().getExtras();

		submit.setOnClickListener(new View.OnClickListener()
		{

			public void onClick(View v)
			{
				postdata();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_add_user_cont, menu);
		return true;
	}

	private void postdata()
	{
		final ProgressDialog dialog = ProgressDialog.show(this,
		         "Posting Data...", "Please wait...", false);
		Thread t = new Thread()
		{

			public void run()
			{
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(UserContHandler.USER_CONT_URI);

				try
				{
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
					         1);
					nameValuePairs.add(new BasicNameValuePair("user", GTalkHandler
					         .getUserBareAddr()));
					nameValuePairs.add(new BasicNameValuePair("name", place_box
					         .getText().toString()));
					nameValuePairs.add(new BasicNameValuePair("description",
					         description_box.getText().toString()));

					nameValuePairs.add(new BasicNameValuePair("latitude", Integer
					         .toString(extras.getInt("latitude"))));
					nameValuePairs.add(new BasicNameValuePair("longitude", Integer
					         .toString(extras.getInt("longitude"))));

					nameValuePairs.add(new BasicNameValuePair("action", "put"));
					post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

					HttpResponse response = client.execute(post);
					BufferedReader rd = new BufferedReader(new InputStreamReader(
					         response.getEntity().getContent()));
					String line = "";
					while ((line = rd.readLine()) != null)
					{
						Log.d(TAG, line);
					}

				}
				catch (IOException e)
				{
					Log.d(TAG, "IOException while trying to conect to GAE");
				}
				dialog.dismiss();
				finish();
			}
		};

		t.start();
		dialog.show();
	}
}
