package edu.ucsd.mycity.maptrack;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;


public class TrackedMapView extends MapView
{	
	
	// ------------------------------------------------------------------------
	// MEMBERS
	// ------------------------------------------------------------------------
	
	private TrackedMapView mThis;
	private long mEventsTimeout = 250L; 	// Set this variable to your preferred timeout
	private boolean mIsTouched = false;
	private GeoPoint mLastCenterPosition;
	private int mLastZoomLevel;
	//private Timer mChangeDelayTimer = new Timer();
	private OnMapViewChangeListener mChangeListener = null;
	
	// ------------------------------------------------------------------------
	// RUNNABLES
	// ------------------------------------------------------------------------
	
	private Runnable mOnChangeTask = new Runnable()
	{
		@Override
		public void run()
		{
			if (mChangeListener != null) mChangeListener.onMapViewChange(mThis, getMapCenter(), mLastCenterPosition, getZoomLevel(), mLastZoomLevel);
			mLastCenterPosition = getMapCenter();
			mLastZoomLevel = getZoomLevel();
		}
	};
	
	// ------------------------------------------------------------------------
	// CONSTRUCTORS
	// ------------------------------------------------------------------------
	
	public TrackedMapView(Context context, String apiKey)
	{
		super(context, apiKey);
		init();
	}
	
	public TrackedMapView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}
	
	public TrackedMapView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init();
	}
	
	private void init()
	{
		mThis = this;
		mLastCenterPosition = this.getMapCenter();
		mLastZoomLevel = this.getZoomLevel();
	}
	
	// ------------------------------------------------------------------------
	// GETTERS / SETTERS
	// ------------------------------------------------------------------------
	
	public void setOnChangeListener(OnMapViewChangeListener l)
	{
		mChangeListener = l;
	}

	// ------------------------------------------------------------------------
	// EVENT HANDLERS
	// ------------------------------------------------------------------------
	
	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{		
		// Set touch internal
		mIsTouched = (ev.getAction() != MotionEvent.ACTION_UP);

		return super.onTouchEvent(ev);
	}

	@Override
	public void computeScroll()
	{
		super.computeScroll();
		
		// Check for change
		if (isSpanChange() || isZoomChange())
		{
			// If computeScroll called before timer counts down we should drop it and 
			// start counter over again
			resetMapChangeTimer();
		}
	}

	// ------------------------------------------------------------------------
	// TIMER RESETS
	// ------------------------------------------------------------------------
	
	private void resetMapChangeTimer()
	{
		TrackedMapView.this.removeCallbacks(mOnChangeTask);
		TrackedMapView.this.postDelayed(mOnChangeTask, mEventsTimeout);
	}
	
	// ------------------------------------------------------------------------
	// CHANGE FUNCTIONS
	// ------------------------------------------------------------------------
	
	private boolean isSpanChange()
	{
		return !mIsTouched && !getMapCenter().equals(mLastCenterPosition);
	}
	
	private boolean isZoomChange()
	{
		return (getZoomLevel() != mLastZoomLevel);
	}
	
}