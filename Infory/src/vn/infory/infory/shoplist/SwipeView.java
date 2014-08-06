package vn.infory.infory.shoplist;

import vn.infory.infory.CyUtils;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import android.widget.Scroller;

public class SwipeView extends RelativeLayout {

	private GestureDetector mGestureDetector;
	private Scroller mScroller;
	private int mWidth;
	private int mScroll;
	private Listener mListener = new Listener();

	public SwipeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SwipeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	public void setListener(Listener listener) {
		if (listener == null)
			listener = new Listener();
		mListener = listener;
	}
	
	public void toggle(boolean isShow) {
		if (isShow) {
			int childW = getChildAt(0).getWidth();
			mScroller.startScroll(mScroll, 0, childW - mScroll, 0);
		} else {
			mScroller.startScroll(mScroll, 0, -mScroll, 0);
		}
		invalidate();
	}

	private void init(Context context) {
		if (isInEditMode())
			return;
		
		mScroller = new Scroller(context);
		mGestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
			
			public boolean onDown(MotionEvent e) {
				return true;
			}
			
			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2,
					float distanceX, float distanceY) {
				mScroll += (int) distanceX;
				boolean r = mScroll > 0;
				requestDisallowInterceptTouchEvent(r);
				mScroll = Math.max(0, mScroll);
				mScroll = Math.min(getChildAt(0).getWidth(), mScroll);
				getChildAt(1).setTranslationX(-mScroll);
				getChildAt(0).setTranslationX(mWidth - mScroll);
				return r;
			}
			
			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				mListener.onTap(SwipeView.this);
				return true;
			}
		});
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		mWidth = right;
		getChildAt(0).setTranslationX(right);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			int childW = getChildAt(0).getWidth();
			if (mScroll < childW/2)
				mScroller.startScroll(mScroll, 0, -mScroll, 0);
			else
				mScroller.startScroll(mScroll, 0, childW - mScroll, 0);
			invalidate();
			break;
		}
		boolean r = mGestureDetector.onTouchEvent(event); 
		debugLog(event.toString() + r);
		return r;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		
		if (isInEditMode()) {
			super.onDraw(canvas);
			return;
		}
			
		if (mScroller.computeScrollOffset())
			invalidate();
		mScroll = mScroller.getCurrX();
		getChildAt(1).setTranslationX(-mScroll);
		getChildAt(0).setTranslationX(mWidth - mScroll);
		
		super.onDraw(canvas);
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Listener
	///////////////////////////////////////////////////////////////////////////
	
	public static class Listener {
		public void onTap(SwipeView view) {}
	}

	///////////////////////////////////////////////////////////////////////////
	// Debug stuff
	///////////////////////////////////////////////////////////////////////////
	private static final boolean isDebug = false;
	private static final String TAG = "CycrixDebug";
	private static final String HEADER = "SwipeView";
	private static void debugLog(String message) {
		if (CyUtils.isDebug && isDebug) Log.d(TAG, HEADER + " " + message);
	}
}