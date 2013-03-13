/**
 * 
 */
package edu.ucsd.mycity.localservices;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.maps.GeoPoint;
import edu.ucsd.mycity.utils.ParcelableGeoPoint;

/**
 * @author Aaron
 *
 */
// Need to be Parcelable In order to be passed over by Android Intent
public class LocalServiceItem implements Parcelable {

	private ArrayList<String> types;
	private String name = "";
	private String phone = "";
	private String address = "";
	private GeoPoint location;
	
	public LocalServiceItem(ArrayList<String> types, String name, String phone, String address, GeoPoint location) {
		this.types = types;
		this.name = name;
		this.phone = phone;
		this.address = address;
		this.location = location;
	}
	
	public ArrayList<String> getTypes() {
		return types;
	}
	public void setType(ArrayList<String> types) {
		this.types = types;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}

	public GeoPoint getLocation() {
		return location;
	}
	public void setLocation(GeoPoint location) {
		this.location = location;
	}
	
	public String getRef() {
		return "";
	}
	// ------------ Start Parcelable --------------

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		//private ArrayList<String> types;
		//private String name = "";
		//private String phone = "";
		//private String address = "";
		//private GeoPoint location;
		
		dest.writeStringList(types);
		dest.writeString(name);
		dest.writeString(phone);
		dest.writeString(address);
		dest.writeParcelable( new ParcelableGeoPoint(location), flags );
	}
    
    public LocalServiceItem(Parcel in) {
    	// Read from parcel
    	in.readStringList(this.types);
    	this.name = in.readString();
    	this.phone = in.readString();
    	this.address = in.readString();
    	this.location = ((ParcelableGeoPoint) in.readParcelable( ParcelableGeoPoint.class.getClassLoader() )).getGeoPoint();
    }
    
    public static final Parcelable.Creator<LocalServiceItem> CREATOR = new Parcelable.Creator<LocalServiceItem>() {
		public LocalServiceItem createFromParcel(Parcel in) {
		    return new LocalServiceItem(in);
		}
		
		public LocalServiceItem[] newArray(int size) {
		    return new LocalServiceItem[size];
		}
	};
}