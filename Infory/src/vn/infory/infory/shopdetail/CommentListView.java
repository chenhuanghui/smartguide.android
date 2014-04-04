package vn.infory.infory.shopdetail;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.ListView;

public class CommentListView extends ListView implements AbsListView.OnScrollListener {

	private boolean mTopReach = true;
	private float mTouchSlop;
	private int mStartX, mStartY;
	private boolean mTrackTop = false;
	private boolean mPassThrough = false;

	public CommentListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public CommentListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public CommentListView(Context context) {
		super(context);
		init(context);
	}
	
	private void init(Context context) {
		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		getParent().requestDisallowInterceptTouchEvent(true);
		
		return true;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean result = false;
		
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			if (mTopReach) {
				mTrackTop = true;
				mStartX = (int) event.getX();
				mStartY = (int) event.getY();
				result = true;
			} else {
				result = super.onTouchEvent(event);
			}
			break;

		case MotionEvent.ACTION_MOVE:
			float dx = event.getX() - mStartX;
			float dy = event.getY() - mStartY;
			if (mTrackTop) {
				
				if (magnitude(dx, dy) > mTouchSlop) {
					
					if (dy > Math.abs(dx)) { 	// If drag down
						result = false;			// dont eat event
						mPassThrough = false;
						getParent().requestDisallowInterceptTouchEvent(false);
					} else {					// if drag up
						result = true;			// redirect event to super
						mPassThrough = true;
						MotionEvent e2 = MotionEvent.obtain(event);
						e2.setAction(event.getAction() & (~MotionEvent.ACTION_MASK) | 
							MotionEvent.ACTION_DOWN);
						super.onTouchEvent(e2);
						e2.recycle();
					}
					
					mTrackTop = false;
				} else {
					result = true;
				}
			} else {
				result = super.onTouchEvent(event);
			}
			
			if (mPassThrough) {
				result = super.onTouchEvent(event);
//				result = true;
			}
			break;

		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			
			if (mTrackTop) {
				result = true;
			} else {
				result = super.onTouchEvent(event);
			}

			mTrackTop = false;
			break;
		}
		
		return result;
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		mTopReach = view.getChildCount() != 0 &&
				firstVisibleItem == 0 && view.getChildAt(0).getTop() == 0;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}
	
	private static float magnitude(float x, float y) {
		return (float) Math.sqrt(x*x + y*y);
	}
}
