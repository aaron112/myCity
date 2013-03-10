package edu.ucsd.mycity.usercontent;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.maps.GeoPoint;

import edu.ucsd.mycity.utils.ParcelableGeoPoint;

//Need to be Parcelable In order to be passed over by Android Intent
public class UserContEntry implements Parcelable
{
	private String user;
	private String name;
	private GeoPoint location;
	private String description;
	private String picKey;

	public UserContEntry(String user, String name, String description,
	         GeoPoint gp, String picKey)
	{
		this.user = user;
		this.name = name;
		this.location = gp;
		this.description = description;
		this.picKey = picKey;
	}

	public String getName()
	{
		return this.name;
	}

	public String getUser()
	{
		return this.user;
	}

	public String getDescription()
	{
		return this.description;
	}

	public GeoPoint getLocation()
	{
		return this.location;
	}

	public String getPicKey()
	{
		return this.picKey;
	}

	// ------------ Start Parcelable --------------

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		//private String user;
		//private String name;
		//private GeoPoint location;
		//private String description;
		//private String picKey;

		dest.writeString(user);
		dest.writeString(name);
		dest.writeParcelable( new ParcelableGeoPoint(location), flags );
		dest.writeString(description);
		dest.writeString(picKey);
	}
    
    public UserContEntry(Parcel in) {
    	// Read from parcel
    	this.user = in.readString();
    	this.name = in.readString();
    	this.location = ((ParcelableGeoPoint) in.readParcelable( ParcelableGeoPoint.class.getClassLoader() )).getGeoPoint();
    	this.description = in.readString();
    	this.picKey = in.readString();
    }
    
    public static final Parcelable.Creator<UserContEntry> CREATOR = new Parcelable.Creator<UserContEntry>() {
		public UserContEntry createFromParcel(Parcel in) {
		    return new UserContEntry(in);
		}
		
		public UserContEntry[] newArray(int size) {
		    return new UserContEntry[size];
		}
	};
}
