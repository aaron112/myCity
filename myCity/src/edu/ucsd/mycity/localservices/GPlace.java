package edu.ucsd.mycity.localservices;

import java.util.ArrayList;

import com.google.android.maps.GeoPoint;
import com.google.api.client.util.Key;

public class GPlace extends LocalServiceItem {
	public GPlace(ArrayList<String> types, String name, String phone,
			String address, GeoPoint location) {
		super(types, name, phone, address, location);
		// TODO Auto-generated constructor stub
	}
	
	public GPlace() {
		super(null, null, null, null, null);
	}

	@Key
	public String id;

	@Key
	public String name;
	 
	@Key
	public String reference;
	 
	@Key
	public String icon;
	 
	@Key
	public String vicinity;
	 
	@Key
	public Geometry geometry;
	 
	@Key
	public String formatted_address;
	 
	@Key
	public String formatted_phone_number;
	 
	@Override
	public String toString() {
		return name + " - " + id + " - " + reference;
	}
	 
	public static class Geometry {
		@Key
		public Location location;
	}
	 
	public static class Location {
		@Key
		public double lat;
	 
		@Key
		public double lng;
	}
	
	// Adapt to LocaServiceItem
	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getPhone() {
		return this.formatted_phone_number;
	}
	
	@Override
	public String getAddress() {
		return this.vicinity;
	}

	@Override
	public GeoPoint getLocation() {
		return new GeoPoint( (int)(this.geometry.location.lat* 1E6), (int)(this.geometry.location.lng* 1E6));
	}
}