package edu.ucsd.mycity.localservices;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.EditText;

import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
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
	
	private void showLocalServiceDialog(LocalServiceItem item) {
		final EditText inputTextLayout = new EditText( mContext );
        
		AlertDialog.Builder builder = new AlertDialog.Builder( mContext );
		builder.setTitle( item.getName() )
			   .setView( inputTextLayout )
			   .setMessage( item.getAddress() );
		
		builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	        	
	        }
		});
		
		builder.setNegativeButton("Cancel", null);
		builder.show();
	}
}