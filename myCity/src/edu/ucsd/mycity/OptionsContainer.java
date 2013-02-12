package edu.ucsd.mycity;

class OptionsContainer {
	
	static private String G_USERNAME = "";	// GTalk Username
	static private String G_PASSWORD = "";	// GTalk Password
	static private int updateInterval = 10;	// In seconds
	
	public static String getGUsername() { return G_USERNAME; }
	public static String getGPassword() { return G_PASSWORD; }
	public static int getUpdateInterval() { return updateInterval; }
	
	public static void setGUsername( String name ) { G_USERNAME = name; }
	public static void setGPassword( String pass ) { G_PASSWORD = pass; }
	public static void setUpdateInterval( int interval ) { updateInterval = interval; } 
}
