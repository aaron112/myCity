package edu.ucsd.mycity.localservices;


import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.EditText;

import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

import edu.ucsd.mycity.BuddyHandler;
import edu.ucsd.mycity.GTalkHandler;
import edu.ucsd.mycity.buddy.BuddyEntry;
import edu.ucsd.mycity.utils.PinsOverlay;

public class LocalServicePinsOverlay extends PinsOverlay
{
	public static final String TAG = "LocalServicePinsOverlay";
	private Context mContext;

	/**
	 * @param defaultMarker
	 * @param mapView
	 */
	public LocalServicePinsOverlay(Drawable defaultMarker, MapView mapView)
	{
		super(defaultMarker, mapView);
		mContext = mapView.getContext();
		// TODO Auto-generated constructor stub
	}

	protected boolean onBalloonTap(int index, OverlayItem in)
	{
		LocalServiceOverlayItem oitem = (LocalServiceOverlayItem) in;
		LocalServiceItem item = oitem.getItem();
		
		if (item == null) {
			Log.d(TAG, "Error: item is empty");
			return false;
		}
		
		showLocalServiceDialog(item);

		// Start ChatActivity
		//Intent i = new Intent(mContext, ShowUserContActivity.class);
		//Bundle bundle = new Bundle();
		//bundle.putParcelable("localServiceItem", item);
		//i.putExtras(bundle);
		//mContext.startActivity(i);

		Log.d(TAG, "showing local service onBalloonTap");

		return true;
	}
	
	private void showLocalServiceDialog(final LocalServiceItem item) {
		final EditText inputTextLayout = new EditText( mContext );
        
		AlertDialog.Builder detailDialogBuilder = new AlertDialog.Builder( mContext );
		detailDialogBuilder.setTitle( item.getName() )
			   .setView( inputTextLayout )
			   .setMessage( "Address: " + item.getAddress() + "\nInvite Message: \n" );
		
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
}