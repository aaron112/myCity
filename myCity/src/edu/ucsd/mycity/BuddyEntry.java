package edu.ucsd.mycity;

/**
 * BuddyEntry.java
 * 
 * CSE110 Project - myCity
 * 
 * Team Members:
 * Yip-Ming Wong (Aaron)
 * Yui-Yan Chan (Ryan)
 * Ryan Khalili
 * Elliot Yaghoobia
 * Jonas Kabigting
 * Michael Lee
 * 
 */

import org.jivesoftware.smack.packet.Presence;

import android.location.Location;

public class BuddyEntry {
	
	private String name;
	private String user;
	private Presence presence;
	private boolean isMyCityUser = false;
	private boolean isProbed = false;
	private Location location = null;
	
	BuddyEntry(String name, String user, Presence presence) {
		this.name = name;
		this.user = user;
		this.presence = presence;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}

	public Presence getPresence() {
		return presence;
	}
	public void setPresence(Presence presence) {
		this.presence = presence;
	}
	
	public boolean isMyCityUser() {
		return isMyCityUser;
	}
	public void setMyCityUser(boolean isMyCityUser) {
		this.isMyCityUser = isMyCityUser;
	}
	
	public boolean isProbed() {
		return isProbed;
	}
	public void setProbed(boolean isProbed) {
		this.isProbed = isProbed;
	}

	public Location getLocation() {
		return location;
	}
	public void setLocation(Location location) {
		this.location = location;
	}

}
