package edu.ucsd.mycity;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

public class AddUserContActivity extends Activity
{
	public static final String TAG = "AddUserContActivity";

	private static int RESULT_LOAD_IMAGE = 1;

	private Bundle extras;
	private EditText place_box;
	private EditText description_box;
	private String picPath = null;
	private Spinner spinner;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_user_cont);
		Button submit = (Button) findViewById(R.id.submit_btn);
		Button loadPic = (Button) findViewById(R.id.buttonLoadPicture);

		place_box = (EditText) findViewById(R.id.place_box);
		description_box = (EditText) findViewById(R.id.description_box);

		addItemsSpinner();

		extras = getIntent().getExtras();

		loadPic.setOnClickListener(new View.OnClickListener()
		{

			public void onClick(View v)
			{
				Intent i = new Intent(
				         Intent.ACTION_PICK,
				         android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

				startActivityForResult(i, RESULT_LOAD_IMAGE);

			}
		});

		submit.setOnClickListener(new View.OnClickListener()
		{

			public void onClick(View v)
			{
				postdata(picPath);
			}
		});
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK
		         && null != data)
		{
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };

			Cursor cursor = getContentResolver().query(selectedImage,
			         filePathColumn, null, null, null);
			cursor.moveToFirst();

			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String picturePath = cursor.getString(columnIndex);
			cursor.close();

			ImageView imageView = (ImageView) findViewById(R.id.showImage);
			imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));

			Log.d(TAG, picturePath);

			picPath = picturePath;
		}

	}

	private void postdata(final String filepath)
	{
		final ProgressDialog dialog = ProgressDialog.show(this,
		         "Posting Data...", "Please wait...", false);
		Thread t = new Thread()
		{

			public void run()
			{
				String picKey = null;

				if (filepath != null)
				{
					HttpClient httpClient = new DefaultHttpClient();
					HttpGet httpGet = new HttpGet(UserContHandler.UPLOAD_URI);

					try
					{
						HttpResponse urlResponse = httpClient.execute(httpGet);

						String result = EntityUtils.toString(urlResponse.getEntity());
						File file = new File(filepath);

						FileBody fileBody = new FileBody(file, "multipart/form-data");
						StringBody stringBody = new StringBody(file.getName());

						MultipartEntity entity = new MultipartEntity(
						         HttpMultipartMode.BROWSER_COMPATIBLE);
						entity.addPart("myFile", fileBody);
						entity.addPart("string", stringBody);

						HttpPost httpPost = new HttpPost(result);

						httpPost.setEntity(entity);

						HttpResponse response = httpClient.execute(httpPost);
						response.getStatusLine();
						picKey = EntityUtils.toString(response.getEntity());

						Log.v(TAG, "received http response " + response);
						Log.v(TAG, "received http entity " + entity);
						Log.d(TAG, "blobKey " + picKey);

					}
					catch (Exception e)
					{
						Log.d(TAG, e.toString());
					}
				}

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
					nameValuePairs.add(new BasicNameValuePair("picKey", picKey));
					nameValuePairs.add(new BasicNameValuePair("public", spinner
					         .getSelectedItem().toString()));

					nameValuePairs.add(new BasicNameValuePair("action", "put"));
					// picKey = null;
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

	private void addItemsSpinner()
	{
		spinner = (Spinner) findViewById(R.id.visibilitySpinner);
		ArrayList<String> list = new ArrayList<String>();
		list.add("private");
		list.add("buddies");
		list.add("public");

		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
		         android.R.layout.simple_spinner_item, list);
		spinner.setAdapter(dataAdapter);
	}
}
