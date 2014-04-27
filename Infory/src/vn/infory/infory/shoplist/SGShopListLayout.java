package vn.infory.infory.shoplist;

import java.util.ArrayList;

import vn.infory.infory.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Scroller;

public class SGShopListLayout extends FrameLayout {
	
	private int mStartX, mStartY;
	private int mStartTransY;
	private Scroller mScroller;
	private GestureDetector mGestureDetector;
	private boolean mTopReach = true;
	private boolean mTrackTop = false;
	private float mFlyVelocity = 0;
	private float mMinVelocity;
	private float mTouchSlop;
	private int mHeaderHeight;
	private int mFirstItemHeight;
//	private OnScrollListener mOnScrollListener;
	private ArrayList<OnScrollListener> mOnScrollListenerList = new ArrayList<OnScrollListener>();
	private boolean mDisallowDispatchTouch;
	
	private ListView mLst;
	private FrameLayout mPad;
	private int mPadOffset;
	private int mMapOffset;
	private View mMapHolder;
	
	private SimpleOnGestureListener mGestureListener = new SimpleOnGestureListener() {
		public boolean onDown(MotionEvent e) {
			return true;
		};
		
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			mFlyVelocity = velocityY;
			return true;
		};
	};
	
	public SGShopListLayout(Context context) {
		super(context);
		init(context);
	}
	
	public SGShopListLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SGShopListLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	private void init(Context context) {
		mLst = new ListView(context);
		mScroller = new Scroller(context);
		mGestureDetector = new GestureDetector(mGestureListener);
		mMinVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();
		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		mPad = new FrameLayout(context);
		addView(mPad);
		addView(mLst);
		mPad.getLayoutParams().width = LayoutParams.MATCH_PARENT;
		mPad.getLayoutParams().height = LayoutParams.MATCH_PARENT;
		mPad.setBackgroundColor(0xFF3F3B38);
		mLst.setDivider(null);
		mLst.setDividerHeight(0);
		mLst.setSelector(new ColorDrawable(0));
		mLst.setOverScrollMode(View.OVER_SCROLL_NEVER);
		mLst.getLayoutParams().height = LayoutParams.MATCH_PARENT;
		mLst.getLayoutParams().width = LayoutParams.MATCH_PARENT;
		mLst.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (view.getChildCount() != 0 &&
						firstVisibleItem == 0 && view.getChildAt(0).getTop() == 0) {
					mTopReach = true;
				} else {
					mTopReach = false;
				}
				
				if (firstVisibleItem == 0 && visibleItemCount > 0) {
					mPadOffset = Math.max(0, view.getChildAt(0).getBottom());
				} else {
					mPadOffset = 0;
				}
				invalidate();
				
				for (OnScrollListener l : mOnScrollListenerList)
					l.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				for (OnScrollListener l : mOnScrollListenerList)
					l.onScrollStateChanged(view, scrollState);
			}
		});
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {

		boolean result = mTopReach;
		
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			
			if (mLst.getTranslationY() > 0) {
//				if (checkDragRegion(event)) 
					result = true;
//				else {
//					result = false;
					mDisallowDispatchTouch = true;
//				}
			} else {
				if (mTopReach) {
					mTrackTop = true;
					mStartX = (int) event.getX();
					mStartY = (int) event.getY();
					mStartTransY = (int) mLst.getTranslationY();
				}
				result = false;
			}
			break;
			
		case MotionEvent.ACTION_MOVE:
			if (mTrackTop) {
				float dx = event.getX() - mStartX;
				float dy = event.getY() - mStartY;
				if (magnitude(dx, dy) > mTouchSlop) {
					if (dy > Math.abs(dx))
						result = true;
					else
						result = false;
					mTrackTop = false;
				} else {
					result = false;
				}
				
			} else {
				result = false;
			}
			break;
			
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			mTrackTop = false;
			mDisallowDispatchTouch = false;
			result = false;
			break;
		}
				
		return result;
	}
	
//	@Override
//	public boolean dispatchTouchEvent(MotionEvent ev) {
//		if (mDisallowDispatchTouch)
//			return false;
//		else
//			return super.dispatchTouchEvent(ev);
//	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		mGestureDetector.onTouchEvent(event);
		
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			
			if (mLst.getTranslationY() > 0 && !checkDragRegion(event)) 
				return false;
			
			mStartX = (int) event.getX();
			mStartY = (int) event.getY();
			mStartTransY = (int) mLst.getTranslationY();
			break;
		case MotionEvent.ACTION_MOVE:
			int transY = (int) (event.getY() - mStartY + mStartTransY);
			transY = Math.max(transY, 0);
			transY = Math.min(transY, mHeaderHeight);
			mLst.setTranslationY(transY);
			mPad.setTranslationY(transY + mPadOffset);
			mMapHolder.setTranslationY(mMapOffset - transY * mMapOffset / mHeaderHeight);
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if (mFlyVelocity > mMinVelocity)
				mScroller.startScroll(0, (int) mLst.getTranslationY(), 0, 
						(int) (mHeaderHeight - mLst.getTranslationY()));
			else if (mFlyVelocity < -mMinVelocity)
				mScroller.startScroll(0, (int) mLst.getTranslationY(), 0, 
						(int) (- mLst.getTranslationY()));
			else if (Math.abs(mStartY - event.getY()) < mTouchSlop)
				mScroller.startScroll(0, (int) mLst.getTranslationY(), 0, 
						(int) (- mLst.getTranslationY())); 
			else if (mLst.getTranslationY() + mFirstItemHeight > getHeight() / 2)
				mScroller.startScroll(0, (int) mLst.getTranslationY(), 0, 
						(int) (mHeaderHeight - mLst.getTranslationY()));
			else
				mScroller.startScroll(0, (int) mLst.getTranslationY(), 0, 
						(int) (- mLst.getTranslationY()));
			invalidate();
			break;
		}
		mFlyVelocity = 0;
		return true;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		
		if (!isInEditMode()) {
			if (mScroller.computeScrollOffset())
				invalidate();
	
			mLst.setTranslationY(mScroller.getCurrY());
			mPad.setTranslationY(mScroller.getCurrY() + mPadOffset);
			mMapHolder.setTranslationY(mMapOffset - mScroller.getCurrY() * mMapOffset / mHeaderHeight);
		}
		
		super.onDraw(canvas);
	}
	
	public ListView getListView() {
		return mLst;
	}
	
	public void toggle(boolean up) {
		if (up) {
			mScroller.startScroll(0, (int) mLst.getTranslationY(), 0, 
					(int) (- mLst.getTranslationY()));
		} else {
			mScroller.startScroll(0, (int) mLst.getTranslationY(), 0, 
					(int) (mHeaderHeight - mLst.getTranslationY()));
		}
		invalidate();
	}
	
//	public void setOnScrollListener(OnScrollListener listener) {
//		mOnScrollListener = listener;
//	}
	
	public void addOnScrollListener(OnScrollListener listener) {
		mOnScrollListenerList.add(listener);
	}
	
	public void setHeaderHeight(int height, int firstItemHeight) {
		mHeaderHeight = height;
		mFirstItemHeight = firstItemHeight;
		mMapOffset = -height / 2;
		mMapHolder.setTranslationY(mMapOffset);
	}
	
	public void setMapHolder(View holder) {
		mMapHolder = holder;
	}
	
	public float magnitude(float x, float y) {
		return (float) Math.sqrt(x*x + y*y);
	}
	
	private boolean checkDragRegion(MotionEvent event) {
		
		if (mLst.getChildCount() == 0)
			return false;
		
		float x = event.getX();
		float y = event.getY();
		
		// Check first item visiblility
		if (mLst.getFirstVisiblePosition() > 0) {
			return checkInner(x, y, mLst);
		} else {
			View firstItem = mLst.getChildAt(0); 
			
			x = x - mLst.getTranslationX();
			y = y - mLst.getTranslationY();
			
			// check normal item region
			if (y >= firstItem.getBottom())
				return true;
			
			// check map button
			View btnMap = firstItem.findViewById(R.id.txtSortBy);
			return checkInner(x, y, btnMap);
		}
	}
	
	private boolean checkInner(float x, float y, View v) {
		return v.getLeft() <= x && x < v.getRight() &&
				v.getTop() <= y && y < v.getBottom();
	}
}
