package vn.smartguide;

import java.util.ArrayList;

import android.os.AsyncTask;
import android.view.View;

/**
 * 
 * @author cycrixlaptop
 *	AsyncTask tự động đặt các thuộc tính Invisible, Gone, Disable cho các View
 * trong quá trình chạy  
 */
public abstract class CyAsyncTask extends AsyncTask<Void, Void, Object> {
	
	private ViewList mVisibleViewList = new ViewList();
	private ViewList mInvisibleViewList = new ViewList();
	private ViewList mGoneViewList = new ViewList();
	private ViewList mDisableViewList = new ViewList();
	
	public CyAsyncTask setVisibleView(View... views) {
		mVisibleViewList.add(views);
		return this;
	}
	
	public CyAsyncTask setInvisibleView(View... views) {
		mInvisibleViewList.add(views);
		return this;
	}
	
	public CyAsyncTask setGoneView(View... views) {
		mGoneViewList.add(views);
		return this;
	}
	
	public CyAsyncTask setDisableView(View... views) {
		mDisableViewList.add(views);
		return this;
	}
	
	/**
	 * Phải gọi hàm này trong hàm override
	 */
	@Override
	protected void onPreExecute() {
		for (ViewItem v : mVisibleViewList) {
			v.preSta = v.v.getVisibility();
			v.v.setVisibility(View.VISIBLE);
		}
		
		for (ViewItem v : mInvisibleViewList) {
			v.preSta = v.v.getVisibility();
			v.v.setVisibility(View.INVISIBLE);
		}
		
		for (ViewItem v : mGoneViewList) {
			v.preSta = v.v.getVisibility();
			v.v.setVisibility(View.GONE);
		}
		
		for (ViewItem v : mDisableViewList) {
			v.preSta = v.v.isEnabled() ? 1 : 0;
			v.v.setEnabled(false);
		}
	}
	
	/**
	 * Phải gọi hàm này trong hàm override
	 */
	@Override
	protected void onPostExecute(Object result) {
		for (ViewItem v : mVisibleViewList)
			v.v.setVisibility(v.preSta);
		
		for (ViewItem v : mInvisibleViewList)
			v.v.setVisibility(v.preSta);
		
		for (ViewItem v : mGoneViewList)
			v.v.setVisibility(v.preSta);
		
		for (ViewItem v : mDisableViewList)
			v.v.setEnabled(v.preSta == 1);
	}
	
	private static class ViewItem {
		public View v;
		public int preSta;
	}
	
	private static class ViewList extends ArrayList<ViewItem>{
		
		public void add(View... views) {
			for (View v : views) {
				ViewItem viewItem = new ViewItem();
				viewItem.v = v;
				add(viewItem);
			}
		}
	}
}