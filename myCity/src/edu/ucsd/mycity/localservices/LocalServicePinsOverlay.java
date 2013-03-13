package edu.ucsd.mycity.localservices;


import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

import edu.ucsd.mycity.BuddyHandler;
import edu.ucsd.mycity.GTalkHandler;
import edu.ucsd.mycity.GooglePlacesHandler;
import edu.ucsd.mycity.buddy.BuddyEntry;
import edu.ucsd.mycity.utils.PinsOverlay;

public class LocalServicePinsOverlay extends PinsOverlay
{
	public static final String TAG = "LocalServicePinsOverlay";
	private Context mContext;
	
	private ProgressDialog mProgressDialog = null;
	
	private LoadLocalServicesDetailsAsyncTask mLoadLocalServicesDetailsAsyncTask = null;

	/**
	 * @param defaultMarker
	 * @param mapView
	 */
	public LocalServicePinsOverlay(Drawable defaultMarker, MapView mapView)
	{
		super(defaultMarker, mapView);
		mContext = mapView.getContext();
	}

	protected boolean onBalloonTap(int index, OverlayItem in)
	{
		LocalServiceOverlayItem oitem = (LocalServiceOverlayItem) in;
		LocalServiceItem item = oitem.getItem();
		
		if (item == null) {
			Log.d(TAG, "Error: item is empty");
			return false;
		}
		
		if ( mLoadLocalServicesDetailsAsyncTask != null &&
				mLoadLocalServicesDetailsAsyncTask.getStatus() == AsyncTask.Status.RUNNING )
			return false;
		
		mLoadLocalServicesDetailsAsyncTask = new LoadLocalServicesDetailsAsyncTask();
		mLoadLocalServicesDetailsAsyncTask.execute(oitem.getItem());
		
		Log.d(TAG, "showing local service onBalloonTap");

		return true;
	}
	
	private class LoadLocalServicesDetailsAsyncTask extends AsyncTask<LocalServiceItem, Void, GPlaceDetails> {
		private final static String TAG = "LoadLocalServicesDetailsAsyncTask";
		
		private LocalServiceItem mFallback;
		
		protected void showLocalServiceDialog(final LocalServiceItem item) {
			final EditText inputTextLayout = new EditText( mContext );
			
			String message = "Address: " + item.getAddress()
							+ "\nPhone: " + item.getPhone()
							+ "\n\nInvite Message: ";
	        
			AlertDialog.Builder detailDialogBuilder = new AlertDialog.Builder( mContext );
			detailDialogBuilder.setTitle( item.getName() )
				   .setView( inputTextLayout )
				   .setMessage( message );
			
			detailDialogBuilder.setPositiveButton("Meet here", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		        	
		        	// Build buddy list:
		        	final ArrayList<BuddyEntry> buddyList = BuddyHandler.getBuddies();
		        	final CharSequence[] buddyNameList = new CharSequence[buddyList.size()];
		        	for (int i=0; i < buddyList.size(); ++i) {
		        		buddyNameList[i] = buddyList.get(i).getName();
		        	}
		        	
		        	AlertDialog.Builder buddyDialogBuilder = new AlertDialog.Builder( mContext );
		        	buddyDialogBuilder.setTitle( "Select a buddy to meet with: " )
		        	.setItems(buddyNameList, new DialogInterface.OnClickListener() {
		        		@Override
		        		public void onClick(DialogInterface dialog, int which) {
		        			// Buddy selected
		        			GTalkHandler.sendLocalServiceInvitation(item, buddyList.get(which).getUser(),
		        					inputTextLayout.getText().toString().trim());
		        		}
		    		});
		    		
		        	buddyDialogBuilder.setNegativeButton("Cancel", null);
		        	buddyDialogBuilder.show();
		        }
			});
			
			detailDialogBuilder.setNegativeButton("Close", null);
			detailDialogBuilder.show();
		}
		
	    @Override
	    protected void onPreExecute() {
	    	super.onPreExecute();
	    	mProgressDialog = ProgressDialog.show(mContext, "Downloading Details...", "Please wait...", false);
	    }
	    
		@Override
		protected GPlaceDetails doInBackground(LocalServiceItem... params) {
			mFallback = params[0];
			try {
				return GooglePlacesHandler.getPlaceDetails(params[0].getRef());
			} catch (Exception e) {
				Log.e(TAG, "Error getting local service details: " + e.toString());
			}
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
		}
		
		@Override
		protected void onPostExecute(GPlaceDetails res) {
			super.onPostExecute(res);
			
			Log.d(TAG, "Done getting local service details! Result status: " + res.status);

			mProgressDialog.dismiss();
			
			if ( res != null && res.result != null ) {
				// Show a new dialog
				showLocalServiceDialog(res.result);
			} else if ( mFallback != null )
				showLocalServiceDialog(mFallback);
		}
	}
}