package edu.ucsd.mycity;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.android.maps.GeoPoint;

import edu.ucsd.mycity.usercontent.UserContEntry;

public class UserContHandler
{
	private static String TAG = "UserContHandler";
	private static ArrayList<UserContEntry> usercontent;
	public static String USER_CONT_URI = "http://mycity-110.appspot.com/product";
	public static String UPLOAD_URI = "http://mycity-110.appspot.com/geturl";
	private static Bitmap pic;

	public static void clear()
	{
		usercontent = new ArrayList<UserContEntry>();
	}

	public static ArrayList<UserContEntry> getContent()
	{
		// updateContent();
		return usercontent;
	}

	public static ArrayList<UserContEntry> getContentOnMap(GeoPoint center,
	         int latSpan, int lonSpan) {
		ArrayList<UserContEntry> res = new ArrayList<UserContEntry>();

		int latMin = center.getLatitudeE6() - (latSpan / 2);
		int latMax = center.getLatitudeE6() + (latSpan / 2);
		int lonMin = center.getLongitudeE6() - (lonSpan / 2);
		int lonMax = center.getLongitudeE6() + (lonSpan / 2);

		for (UserContEntry userCont : usercontent)
		{
			int lat = (int) (userCont.getLocation().getLatitudeE6());
			int lon = (int) (userCont.getLocation().getLongitudeE6());
			// Show only available user content within area
			if (lat >= latMin && lat <= latMax && lon >= lonMin && lon <= lonMax)
			{
				res.add(userCont);
			}
		}

		return res;
	}

	public static boolean updateContent() {
		usercontent = new ArrayList<UserContEntry>();
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(USER_CONT_URI);
			HttpResponse response = client.execute(request);
			HttpEntity entity = response.getEntity();
			String data = EntityUtils.toString(entity);
			//Log.d(TAG, data);
			JSONObject myjson;

			//try {
				myjson = new JSONObject(data);
				JSONArray array = myjson.getJSONArray("data");

				//Log.d(TAG, "JSON array: " + array.toString());

				for (int i = 0; i < array.length(); i++) {
					JSONObject obj = array.getJSONObject(i);
					String user = obj.getString("user");

					if (BuddyHandler.isBuddy(user) || GTalkHandler.getUserBareAddr().equals(user)) {
						GeoPoint gp = new GeoPoint(Integer.parseInt(obj
								.getString("latitude")), Integer.parseInt(obj
								 .getString("longitude")));
						
						UserContEntry temp = new UserContEntry(
								obj.getString("user"), obj.getString("name"),
								obj.getString("description"), gp,
								obj.getString("picKey"));
						
						//Log.d(TAG, "adding item: " + temp.getName());
						usercontent.add(temp);
					} else
						continue;
				}
			} catch (JSONException e) {
				Log.e(TAG, "Error in parsing JSON");
				return false;
			//}
		} catch (ClientProtocolException e) {
			Log.e(TAG, "ClientProtocolException thrown while trying to Connect to GAE");
			return false;
		} catch (IOException e) {
			Log.e(TAG, "IOException thrown while trying to Connect to GAE");
			return false;
		} catch (Exception e) {
			Log.e(TAG, "Error connecting to GAE: " + e.toString());
			return false;
		}
		return true;
	}

	public static Bitmap getImageFromWeb(final String picKey) {
		Bitmap bmpImage;
		URL imgURL;
		
		try {
			String imglink = "http://mycity-110.appspot.com/serve?blob-key="+picKey;
			
			imgURL = new URL(imglink);
			
			URLConnection conn = imgURL.openConnection();
			conn.connect();
			
			InputStream is = conn.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);
			
			bmpImage = BitmapFactory.decodeStream(bis);
			bis.close();
			is.close();
			pic = bmpImage;
		} catch (Exception e) {
			Log.d(TAG, e.toString());
		}
		
		return pic;
	}
}
