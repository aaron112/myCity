package edu.ucsd.mycity;

import com.google.android.maps.GeoPoint;

public class UserContEntry
{
	private String user;
	private String name;
	private GeoPoint location;
	private String description;
	private String uid;

	public UserContEntry(String user, String name, String description,
	         GeoPoint gp)
	{
		this.user = user;
		this.name = name;
		this.location = gp;
		this.description = description;
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

}
