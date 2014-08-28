package vn.infory.infory.mywidget;

import vn.infory.infory.R;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.fortysevendeg.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.swipelistview.SwipeListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshSwipsListView;

public class MyPTRAndSwipeListView extends RelativeLayout {

	private static final String TAG = "MyPTRAndSwipeDeleteListView";

	private Context mcontext;
	private LayoutInflater mInflater;
	
	private FrameLayout proNotifications;
	private SwipeListView swipeListView;
	private PullToRefreshSwipsListView mPullRefreshListView;
	
	// footer load more view
	private FrameLayout mFooterView;
	private FrameLayout mProgressBarLoadMore;
	
	private boolean mIsLoadingMore = false;
//	private int mCurrentScrollState;
	
	private OnActionPullToRefreshAndLoadMoreListView listener;
	
	public interface OnActionPullToRefreshAndLoadMoreListView {
		public void onRefreshListView(PullToRefreshSwipsListView mPullRefreshListView, FrameLayout proNotifications, boolean isShowProgressBar);

		public void onLoadMoreListView(boolean mIsLoadingMore, FrameLayout mProgressBarLoadMore);

		public void onOpenedItem(int position);
		public void onClosedItem(int position);
		public void onClickBackViewListView(int position);
		public void onClickFrontViewListView(int position);
	}
	
	public void setOnActionPullToRefreshAndLoadMoreListView(OnActionPullToRefreshAndLoadMoreListView ltn){
		listener = ltn;
	}
	
	public MyPTRAndSwipeListView(Context context) {
		super(context);
		mcontext = context;
		LayoutInflater inflater = LayoutInflater.from(context);
		View convert = inflater.inflate(R.layout.my_ptr_swipe_listview_layout, this);
		if(!isInEditMode()){
			init(convert,null,0);
		}
	}
	
	public MyPTRAndSwipeListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mcontext = context;
		LayoutInflater inflater = LayoutInflater.from(context);
		View convert = inflater.inflate(R.layout.my_ptr_swipe_listview_layout, this);
		if(!isInEditMode()){
			init(convert,attrs,0);
		}
	}
	
	public MyPTRAndSwipeListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mcontext = context;
		LayoutInflater inflater = LayoutInflater.from(context);
		View convert = inflater.inflate(R.layout.my_ptr_swipe_listview_layout, this);
		if(!isInEditMode()){
			init(convert,attrs,defStyle);
		}
	}
	
	private void init(View convert, AttributeSet attrs, int defStyle){
		proNotifications = (FrameLayout) convert.findViewById(R.id.progressBar);
		FrameLayout loadProgressBar = (FrameLayout) convert.findViewById(R.id.loadProgressBar);
		mPullRefreshListView = (PullToRefreshSwipsListView) findViewById(R.id.listView);
		swipeListView = mPullRefreshListView.getRefreshableView();

		((AnimationDrawable) loadProgressBar.getBackground()).start();
		initFooterLoadMore(mcontext);
		initEvent();
	}
	
	private void initFooterLoadMore(Context context) {
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mFooterView = (FrameLayout) mInflater.inflate(R.layout.load_more_footer_layout, null, false);
		mProgressBarLoadMore = (FrameLayout) mFooterView.findViewById(R.id.load_more_progressBar);
		((AnimationDrawable) mProgressBarLoadMore.getBackground()).start();

//		swipeListView.addFooterView(mFooterView);
	}
	
	
	/**
	 * Set Context to MyPullToRefreshListView
	 * @param context
	 */
	public void setContext(Context context){
		this.mcontext = context;
	}
	
	/**
	 * Active MyPullToRefreshListView
	 */
	public void activePullToRefeshAndLoadMoreListView(){
		if (listener != null) {
			listener.onRefreshListView(mPullRefreshListView, proNotifications, true);
		}
	}
	
	/**
	 * Get ListView on MyPullToRefreshListView
	 * @return
	 */
	public SwipeListView getListView(){
		return swipeListView;
	}

	/**
	 * Set mIsLoadingMore
	 * @param mIsLoadingMore
	 */

	public void setMyIsLoadingMore(boolean mIsLoadingMore) {
		this.mIsLoadingMore = mIsLoadingMore;
		swipeListView.removeFooterView(mFooterView);
		mProgressBarLoadMore.setVisibility(View.GONE);
	}
	
	private void initEvent(){
		// Set a listener to be invoked when the list should be refreshed.
		mPullRefreshListView.setOnRefreshListener(new OnRefreshListener<SwipeListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<SwipeListView> refreshView) {
				mPullRefreshListView.setLastUpdatedLabel(DateUtils.formatDateTime(mcontext,
						System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE
								| DateUtils.FORMAT_ABBREV_ALL));
				// Do work to refresh the list here.
				if (listener != null) {
					listener.onRefreshListView(mPullRefreshListView, proNotifications, false);
				}
			}
		});
		
//		mPullRefreshListView.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {
//
//			@Override
//			public void onLastItemVisible() {
//				if (!mIsLoadingMore && swipeListView.getFirstVisiblePosition() != 0) {
//					swipeListView.addFooterView(mFooterView);
//					mProgressBarLoadMore.setVisibility(View.VISIBLE);
//					mIsLoadingMore = true;
//					onLoadMore();
//				} else {
//					swipeListView.removeFooterView(mFooterView);
//					mProgressBarLoadMore.setVisibility(View.GONE);
//					mIsLoadingMore = false;
//				}
//			}
//			
//			public void onLoadMore() {
//				Log.d(TAG, "onLoadMore");
//				if (listener != null) {
//					listener.onLoadMoreListView(mIsLoadingMore,
//							mProgressBarLoadMore);
//				}
//			}
//		});
		
		swipeListView.setSwipeListViewListener(new BaseSwipeListViewListener(){
			
			@Override
			public void onOpened(int position, boolean toRight){
				Log.d("swipe", String.format("onOpened %d", position));
				if(listener != null) {
					int positionReturn = position - swipeListView.getHeaderViewsCount(); 
					listener.onOpenedItem(positionReturn);
				}
			}

			@Override
			public void onClosed(int position, boolean fromRight){
				Log.d("swipe", String.format("onClosed %d", position));
				if(listener != null) {
					int positionReturn = position - swipeListView.getHeaderViewsCount(); 
					listener.onClosedItem(positionReturn);
				}
			}

			@Override
			public void onListChanged(){
				
			}

			@Override
			public void onMove(int position, float x){
			}

			@Override
			public void onStartOpen(int position, int action, boolean right){
				Log.d("swipe", String.format("onStartOpen %d - action %d", position, action));
			}

			@Override
			public void onStartClose(int position, boolean right){
				Log.d("swipe", String.format("onStartClose %d", position));
			}

			@Override
			public void onClickFrontView(int position){
				Log.d("swipe", String.format("onClickFrontView %d", position));
				if(listener != null) {
					int positionReturn = position - swipeListView.getHeaderViewsCount(); 
					listener.onClickFrontViewListView(positionReturn);
				}
			}

			@Override
			public void onClickBackView(final int position){
				Log.d("swipe", String.format("onClickBackView %d", position));
				if(listener != null) {
					int positionReturn = position - swipeListView.getHeaderViewsCount(); 
					listener.onClickBackViewListView(positionReturn);
				}
				
			}

			@Override
			public void onDismiss(int[] reverseSortedPositions){
				
			}
			
			@Override
		    public void onFirstListItem() {
				
		    }
			
			@Override
			public void onLastListItem() {
				super.onLastListItem();
//				Log.e(TAG,"listPushFeeds.size(): "+ listPushFeeds.size());
//				Log.e(TAG,"pushFeedsCount: "+ pushFeedsCount);
//				if(listPushFeeds.size() < pushFeedsCount){
//					if(!isLoadingMore){
//						isLoadingMore = true;
//						loadPushFeed(
//							Integer.valueOf(
//								listPushFeedsId.get(listPushFeedsId.size()-1))
//							);
//					}
//				}
				if (!mIsLoadingMore) {
					swipeListView.addFooterView(mFooterView);
					mProgressBarLoadMore.setVisibility(View.VISIBLE);
					mIsLoadingMore = true;
					onLoadMore();
				} else {
					swipeListView.removeFooterView(mFooterView);
					mProgressBarLoadMore.setVisibility(View.GONE);
					mIsLoadingMore = false;
				}
			}

			public void onLoadMore() {
				Log.d(TAG, "onLoadMore");
				if (listener != null) {
					listener.onLoadMoreListView(mIsLoadingMore, mProgressBarLoadMore);
				}
			
			}
			
		});
	}


}
