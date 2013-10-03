package vn.smartguide;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.Interpolator;

import java.lang.reflect.Field;

public class SViewPager extends ViewPager {

	private boolean enabled;
	public SViewPager(Context context) {
		super(context);
//		postInitViewPager();
		this.enabled = true;
	}

	public SViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
//		postInitViewPager();
	}

	private ScrollerCustomDuration mScroller = null;

//	private void postInitViewPager() {
//		try {
//			Class<?> viewpager = ViewPager.class;
//			Field scroller = viewpager.getDeclaredField("mScroller");
//			scroller.setAccessible(true);
//			Field interpolator = viewpager.getDeclaredField("sInterpolator");
//			interpolator.setAccessible(true);
//
//			mScroller = new ScrollerCustomDuration(getContext(),
//					(Interpolator) interpolator.get(null));
//			scroller.set(this, mScroller);
//		} catch (Exception e) {
//		}
//	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (this.enabled) {
			return super.onTouchEvent(event);
		}

		return false;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if (this.enabled) {
			return super.onInterceptTouchEvent(event);
		}

		return false;
	}

	public void setPagingEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}