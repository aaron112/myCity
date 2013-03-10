package edu.ucsd.mycity.localservices;

import java.util.List;

import com.google.api.client.util.Key;

public class GPlaceList {
	 
    @Key
    public String status;
 
    @Key
    public List<GPlace> results;
 
}