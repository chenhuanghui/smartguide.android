package vn.infory.infory;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.Interpolator;

import java.lang.reflect.Field;

public class NonSlidePager extends ViewPager {
	
	private boolean mCanDrag = false;

	public NonSlidePager(Context context) {
		super(context);
		postInitViewPager();
	}

	public NonSlidePager(Context context, AttributeSet attrs) {
		super(context, attrs);
		postInitViewPager();
	}
	
	public void setDragable(boolean canDrag) {
		mCanDrag = canDrag;
	}

	private ScrollerCustomDuration mScroller = null;

	private void postInitViewPager() {
		try {
			Class<?> viewpager = ViewPager.class;
			Field scroller = viewpager.getDeclaredField("mScroller");
			scroller.setAccessible(true);
			Field interpolator = viewpager.getDeclaredField("sInterpolator");
			interpolator.setAccessible(true);

			mScroller = new ScrollerCustomDuration(getContext(),
					(Interpolator) interpolator.get(null));
			scroller.set(this, mScroller);
		} catch (Exception e) {
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent arg0) {
		if (mCanDrag)
			return super.onTouchEvent(arg0);
		else
			return true;
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent arg0) {
		if (mCanDrag)
			return super.onInterceptTouchEvent(arg0);
		else
			return false;
	}
}