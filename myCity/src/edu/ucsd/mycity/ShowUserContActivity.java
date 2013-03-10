package edu.ucsd.mycity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ShowUserContActivity extends Activity
{
	public static final String TAG = "LoadUserContActivity";
	private TextView name;
	private TextView description;
	private TextView creatorView;
	private ImageView mImageView;
	private ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_user_cont);

		Bundle b = getIntent().getExtras();

		name = (TextView) findViewById(R.id.nameView);
		name.setText(b.getString("name"));

		description = (TextView) findViewById(R.id.descriptionView);
		description.setText(b.getString("description"));
		
		creatorView = (TextView) findViewById(R.id.creatorView);
		creatorView.setText( "Created by: " + b.getString("user") );
		
		mImageView = (ImageView) findViewById(R.id.showImage);
    	mImageView.setImageBitmap(null);

    	if ( !b.getString("picKey", "").equals("") ) {
    		dialog = ProgressDialog.show(this, "Downloading image...", "Please wait...", false);
    		new DownloadImageAsyncTask().execute( b.getString("picKey") );
    	}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.load_user_cont, menu);
		return true;
	}
	
	private class DownloadImageAsyncTask extends AsyncTask<String, Integer, Bitmap>{
	    @Override
	    protected void onPreExecute() {
	    	// update the UI immediately after the task is executed
	    	super.onPreExecute();
	    	//Toast.makeText(getApplicationContext(), "Downloading Image...", Toast.LENGTH_SHORT).show();
	    }
	    
		@Override
		protected Bitmap doInBackground(String... params) {
			return UserContHandler.getImageFromWeb(params[0]);
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}
		
		@Override
		protected void onPostExecute(Bitmap resBitmap) {
			super.onPostExecute(resBitmap);
			
			//Toast.makeText(getApplicationContext(), "Done downloading!", Toast.LENGTH_SHORT).show();
			Log.d(TAG, "Done Downloading!");
			
			scaleImage(mImageView, resBitmap);
			dialog.dismiss();
		}
		
		@SuppressWarnings("deprecation")
		private void scaleImage(ImageView view, Bitmap bitmap) {
			String TAG = "scaleImage";
		    // Get current dimensions AND the desired bounding box
		    int width = bitmap.getWidth();
		    int height = bitmap.getHeight();
		    Display display = getWindowManager().getDefaultDisplay();
		    int bounding = display.getWidth(); 
		    
		    //int bounding = dpToPx(R.layout.activity_load_user_cont);
		    Log.i(TAG, "original width = " + Integer.toString(width));
		    Log.i(TAG, "original height = " + Integer.toString(height));
		    Log.i(TAG, "bounding = " + Integer.toString(bounding));

		    // Determine how much to scale: the dimension requiring less scaling is
		    // closer to the its side. This way the image always stays inside your
		    // bounding box AND either x/y axis touches it.  
		    float xScale = ((float) bounding) / width;
		    float yScale = ((float) bounding) / height;
		    float scale = (xScale <= yScale) ? xScale : yScale;
		    Log.i(TAG, "xScale = " + Float.toString(xScale));
		    Log.i(TAG, "yScale = " + Float.toString(yScale));
		    Log.i(TAG, "scale = " + Float.toString(scale));

		    // Create a matrix for the scaling and add the scaling data
		    Matrix matrix = new Matrix();
		    matrix.postScale(scale, scale);

		    // Create a new bitmap and convert it to a format understood by the ImageView 
		    Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
		    width = scaledBitmap.getWidth(); // re-use
		    height = scaledBitmap.getHeight(); // re-use
			BitmapDrawable result = new BitmapDrawable(scaledBitmap);
		    //mImageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, mImageView.getWidth(), mImageView.getHeight(), false));
		    
		    //BitmapDrawable result = new BitmapDrawable(scaledBitmap);
		    Log.i(TAG, "scaled width = " + Integer.toString(width));
		    Log.i(TAG, "scaled height = " + Integer.toString(height));

		    // Apply the scaled bitmap
		    view.setImageDrawable(result);

		    // Now change ImageView's dimensions to match the scaled image
		    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams(); 
		    params.width = width;
		    params.height = height;
		    view.setLayoutParams(params);

		    Log.i(TAG, "done");
		}

		//private int dpToPx(int dp)
		//{
		//    float density = getApplicationContext().getResources().getDisplayMetrics().density;
		//    return Math.round((float)dp * density);
		//}
	}
}
