/**
 * 
 */
package edu.ucsd.mycity;

import com.google.android.maps.OverlayItem;

/**
 * @author FLL
 * 
 */
public class UserContOverlayItem extends OverlayItem
{

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 */
	public UserContOverlayItem(UserContEntry entry)
	{
		super(entry.getLocation(), entry.getName(), entry.getDescription());
		// TODO Auto-generated constructor stub
	}

}
