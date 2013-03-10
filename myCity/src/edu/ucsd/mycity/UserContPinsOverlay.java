/**
 * 
 */
package edu.ucsd.mycity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

/**
 * @author FLL
 * 
 */
public class UserContPinsOverlay extends PinsOverlay
{
	public static final String TAG = "UserContPinsOverlay";
	private Context mContext;

	/**
	 * @param defaultMarker
	 * @param mapView
	 */
	public UserContPinsOverlay(Drawable defaultMarker, MapView mapView)
	{
		super(defaultMarker, mapView);
		mContext = mapView.getContext();
		// TODO Auto-generated constructor stub
	}

	protected boolean onBalloonTap(int index, OverlayItem item)
	{
		UserContOverlayItem user_item = (UserContOverlayItem) item;
		if (user_item.getEntry() == null)
		{
			Log.d(TAG, "no user content to show");
			return false;
		}

		// Start ChatActivity
		Intent i = new Intent(mContext, ShowUserContActivity.class);
		Bundle bundle = new Bundle();

		bundle.putString("name", user_item.getEntry().getName());
		bundle.putString("user", user_item.getEntry().getUser());
		bundle.putString("description", user_item.getEntry().getDescription());
		bundle.putString("picKey", user_item.getEntry().getPicKey());
		i.putExtras(bundle);

		mContext.startActivity(i);

		Log.d(TAG, "showing user content onBalloonTap");

		return true;
	}
}
