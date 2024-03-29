package vn.infory.infory;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

public class ScrollerCustomDuration extends Scroller {

	public ScrollerCustomDuration(Context context) {
		super(context);
	}

	public ScrollerCustomDuration(Context context, Interpolator interpolator) {
		super(context, interpolator);
	}

	@SuppressLint("NewApi")
	public ScrollerCustomDuration(Context context, Interpolator interpolator, boolean flywheel) {
		super(context, interpolator, flywheel);
	}

	/**
	 * Set the factor by which the duration will change
	 */

	@Override
	public void startScroll(int startX, int startY, int dx, int dy, int duration) {
		super.startScroll(startX, startY, dx, dy, 500);
	}
}