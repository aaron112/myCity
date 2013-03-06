package edu.ucsd.mycity.maptrack;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

public class TrackedMapView extends MapView
{
	static final int LONGPRESS_THRESHOLD = 500;

	private TrackedMapView mThis;
	private long mEventsTimeout = 250L; // Set this variable to your preferred
	                                    // timeout
	private boolean mIsTouched = false;
	private GeoPoint mLastCenterPosition;
	private int mLastZoomLevel;
	// private Timer mChangeDelayTimer = new Timer();
	private OnMapViewChangeListener mChangeListener = null;

	private Timer longpressTimer = new Timer();
	private OnLongpressListener longpressListener;

	private Runnable mOnChangeTask = new Runnable()
	{
		@Override
		public void run()
		{
			if (mChangeListener != null)
				mChangeListener.onMapViewChange(mThis, getMapCenter(),
				         mLastCenterPosition, getZoomLevel(), mLastZoomLevel);
			mLastCenterPosition = getMapCenter();
			mLastZoomLevel = getZoomLevel();
		}
	};

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

	public void setOnChangeListener(OnMapViewChangeListener l)
	{
		mChangeListener = l;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{
		// Set touch internal
		mIsTouched = (ev.getAction() != MotionEvent.ACTION_UP);

		// handle long press action
		handleLongpress(ev);

		return super.onTouchEvent(ev);
	}

	@Override
	public void computeScroll()
	{
		super.computeScroll();

		// Check for change
		if (isSpanChange() || isZoomChange())
		{
			// If computeScroll called before timer counts down we should drop it
			// and
			// start counter over again
			resetMapChangeTimer();
		}
	}

	private void resetMapChangeTimer()
	{
		TrackedMapView.this.removeCallbacks(mOnChangeTask);
		TrackedMapView.this.postDelayed(mOnChangeTask, mEventsTimeout);
	}

	private boolean isSpanChange()
	{
		return !mIsTouched && !getMapCenter().equals(mLastCenterPosition);
	}

	private boolean isZoomChange()
	{
		return (getZoomLevel() != mLastZoomLevel);
	}

	public void setOnLongpressListener(OnLongpressListener listener)
	{
		longpressListener = listener;
	}

	private void handleLongpress(final MotionEvent event)
	{

		if (event.getAction() == MotionEvent.ACTION_DOWN)
		{
			// Finger has touched screen.
			longpressTimer = new Timer();
			longpressTimer.schedule(new TimerTask()
			{
				@Override
				public void run()
				{
					GeoPoint longpressLocation = getProjection().fromPixels(
					         (int) event.getX(), (int) event.getY());

					/*
					 * Fire the listener. We pass the map location of the longpress
					 * as well, in case it is needed by the caller.
					 */
					longpressListener.onLongpress(TrackedMapView.this,
					         longpressLocation);
				}

			}, LONGPRESS_THRESHOLD);

			mLastCenterPosition = getMapCenter();
		}

		if (event.getAction() == MotionEvent.ACTION_MOVE)
		{

			if (!getMapCenter().equals(mLastCenterPosition))
			{
				// User is panning the map, this is no longpress
				longpressTimer.cancel();
			}

			mLastCenterPosition = getMapCenter();
		}

		if (event.getAction() == MotionEvent.ACTION_UP)
		{
			// User has removed finger from map.
			longpressTimer.cancel();
		}

		if (event.getPointerCount() > 1)
		{
			// This is a multitouch event, probably zooming.
			longpressTimer.cancel();
		}
	}

}