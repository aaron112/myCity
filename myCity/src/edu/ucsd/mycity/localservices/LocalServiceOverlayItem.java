package edu.ucsd.mycity.localservices;

import com.google.android.maps.OverlayItem;

public class LocalServiceOverlayItem extends OverlayItem
{
	private LocalServiceItem item;

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 */
	public LocalServiceOverlayItem(LocalServiceItem item)
	{
		super(item.getLocation(), item.getName(), item.getAddress());
		this.item = item;
		// TODO Auto-generated constructor stub
	}

	public LocalServiceItem getItem()
	{
		return this.item;
	}

}
