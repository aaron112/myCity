/**
 * 
 */
package edu.ucsd.mycity.localservices;

import com.google.android.maps.GeoPoint;

/**
 * @author Aaron
 *
 */
public class LocalServiceItem {

	private String[] type;
	private String name = "";
	private String phone = "";
	private String address = "";
	private GeoPoint location;
	
	public LocalServiceItem(String[] type, String name, String phone, String address, GeoPoint location) {
		this.type = type;
		this.name = name;
		this.phone = phone;
		this.address = address;
		this.location = location;
	}
	
	public String[] getType() {
		return type;
	}
	public void setType(String[] type) {
		this.type = type;
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
}
