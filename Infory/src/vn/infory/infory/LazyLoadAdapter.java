package vn.infory.infory;

import java.util.ArrayList;

import vn.infory.infory.network.CyAsyncTask;
import vn.infory.infory.network.NetworkManager;
import vn.infory.infory.network.CyAsyncTask.Listener2;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;

public abstract class LazyLoadAdapter extends BaseAdapter implements OnScrollListener, Listener2 {
	
	public static final int ITEM_PER_PAGE = 10;
	
	protected CyAsyncTask mLoader;
	protected int mLoadingRID;
	protected ArrayList mItemList;
	protected LayoutInflater mInflater;
	public boolean mLoading, mIsMore = true;
	public boolean mHideLoading = true;
	protected int mPageNum = 0;
	protected static Activity mAct;
	protected int mContentTypeCount;
	
	private OnScrollListener mOnScrollListener;
	
	public LazyLoadAdapter(Activity act, CyAsyncTask loader,
			int loadingRID, int contentTypeCount, ArrayList itemList) {
		mAct 		= act;
		mLoader 	= loader;
		mLoader.setListener(this);
		mInflater 	= act.getLayoutInflater();
		mLoadingRID = loadingRID;
		mItemList 	= new ArrayList(itemList);
		mContentTypeCount = contentTypeCount;
	}
	
	public void setOnScrollListener(OnScrollListener listener) {
		mOnScrollListener = listener;
	}

	@Override
	public final int getCount() {
		return mItemList.size() + (mIsMore || !mHideLoading ? 1 : 0);
	}
	
	@Override
	public final int getViewTypeCount() {
		return  mContentTypeCount + 1;
	}
	
	/**
	 * Return view type:<br/>
	 * 	- return <code>contentTypeCount</code> if this is loading item <br/>
	 * 	- return -1 if this is content item, in that case the child class need to provide
	 *	actual view type
	 */
	@Override
	public int getItemViewType(int position) {
		if (position == mItemList.size())
			return mContentTypeCount;
		else
			return -1;
	}
	
	@Override
	public final long getItemId(int pos) {
		return pos;
	}
	
	@Override
	public Object getItem(int position) {;
		return mItemList.get(position);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null && position == mItemList.size())
			convertView = mInflater.inflate(mLoadingRID, parent, false);
		
		return convertView;
	}
	
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if (firstVisibleItem + visibleItemCount >= getCount())
			loadMore();
		
		if (mOnScrollListener != null)
			mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
	}
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (mOnScrollListener != null)
			mOnScrollListener.onScrollStateChanged(view, scrollState);
	}
	
	public void setPage(int page) {
		mPageNum = page;
	}
	
	public void loadMore() {
		if (!mIsMore || mLoading)
			return;
		
		mLoading = true;
		mLoader.setPage(mPageNum);
		mLoader.executeOnExecutor(NetworkManager.THREAD_POOL);
	}
	
	@Override
	public void onCompleted(Object result) {
		
		ArrayList itemList = (ArrayList) result;
		
		mLoading = false;
		if (itemList.size() > 0) {
			mItemList.addAll(itemList);
			mPageNum++;			
//			CyUtils.showToast("Loaded " + mPageNum + " pages", mAct);
		}
		
		if (itemList.size() < ITEM_PER_PAGE)
			mIsMore = false;
		
		notifyDataSetChanged();
		mLoader = mLoader.clone();
		mLoader.setListener(this);
	}
	
	@Override
	public void onFail(Exception e) {
		mLoading = false;
		mIsMore = false;
		notifyDataSetChanged();
//		CyUtils.showError("Không thể lấy thêm", e, mAct);
		
		/*AlertDialog.Builder builder = new Builder(mAct);
		builder.setCancelable(false);
		builder.setMessage("Không có dữ liệu!");
		builder.setPositiveButton("OK", new OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				mAct.finish();
			}
		});
		builder.create().show();*/
	}
}