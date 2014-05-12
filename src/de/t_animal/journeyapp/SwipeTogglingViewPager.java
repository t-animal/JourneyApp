package de.t_animal.journeyapp;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;

/**
 * When set via setSwipingOnlyFromBorder this view pager reacts to swipes only from the border (10% of the screen) or
 * when two fingers are used
 * 
 * @author ar79yxiw
 * 
 */
public class SwipeTogglingViewPager extends ViewPager {

	private static final String TAG = "NonScrollingViewPager";

	private boolean swipeOnlyFromBorder = true;
	private boolean ignoreUntilUp = false;
	private Context context;

	public SwipeTogglingViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (ignoreUntilUp) {
			return false;
		}

		return super.onTouchEvent(event);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if (swipeOnlyFromBorder && event.getAction() == MotionEvent.ACTION_DOWN) {
			// detect if movement is at the edge
			WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			int width = display.getWidth();

			if (!(event.getX() <= width * 0.10 || event.getX() >= width * 0.90)) {
				ignoreUntilUp = true;
			}
		}

		// When two fingers, restart event process so that swiping happens in super class
		if (ignoreUntilUp && event.getPointerCount() > 1) {
			ignoreUntilUp = false;
			event.setAction(MotionEvent.ACTION_DOWN);
		}

		if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
			ignoreUntilUp = false;
		}

		if (ignoreUntilUp) {
			return false;
		}

		return super.onInterceptTouchEvent(event);
	}

	/**
	 * Set wether swipes have to start from border or must be two-fingered
	 * 
	 * @param isEnabled
	 *            if true, swipes must be started from border or must be two-fingered
	 */
	public void setSwipingOnlyFromBorder(boolean isEnabled) {
		swipeOnlyFromBorder = isEnabled;
	}
}