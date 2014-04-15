package vn.infory.infory.network;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

/**
 * 
 * @author cycrixlaptop
 *	AsyncTask tự động đặt các thuộc tính Invisible, Gone, Disable cho các View
 * trong quá trình chạy  
 */
public abstract class CyAsyncTask extends AsyncTask<Object, Void, Object> {
	
	public static AtomicBoolean hold = new AtomicBoolean();
	
	private ViewList mVisibleViewList = new ViewList();
	private ViewList mInvisibleViewList = new ViewList();
	private ViewList mGoneViewList = new ViewList();
	private ViewList mDisableViewList = new ViewList();
	protected Context mContext;
	protected Exception mEx;
	private Listener2 mListener;
	protected List<CyAsyncTask> mInnerTaskList;
	
	public CyAsyncTask(Context c) {		
		mContext = c;
	}
	
	public void setTaskList(List<CyAsyncTask> taskList) {
		mInnerTaskList = taskList;
	}
	
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
	
	@Override
	public CyAsyncTask clone() {
		return null;
	}
	
	@Override
	protected Object doInBackground(Object... arg0) {
		if (hold.get()) {
			synchronized (hold) {
				try {
					Log.d("CycrixDebug", "CyAsyncTask wait " + this.getClass().getName());
					hold.wait();
					Log.d("CycrixDebug", "CyAsyncTask resume " + this.getClass().getName());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		return null;
	}
	
	public void setListener(Listener2 listener) {
		mListener = listener;
	}
	
	public void setPage(int page) {}
	
	public static void hold() {
		hold.set(true);
	}
	
	public static void resume() {
		hold.set(false);
		synchronized (hold) {
			hold.notifyAll();
		}
	}
	
	/**
	 * Phải g�?i hàm này trong hàm override
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
	 * Phải g�?i hàm này trong hàm override
	 */
	@Override
	protected void onPostExecute(Object result) {
		if (mInnerTaskList != null)
			mInnerTaskList.remove(this);
		
		onCancelled();
		
		try {
			if (mEx != null)
				throw mEx;
			
			onCompleted(result);
			if (mListener != null)
				mListener.onCompleted(result);
		} catch (Exception e) {
			onFail(mEx);
			if (mListener != null)
				mListener.onFail(mEx);
		}
		
//		Log.d("CycrixDebug", "Completed " + this.getClass().getName());
	}
	
	@Override
	protected void onCancelled() {
		for (ViewItem v : mVisibleViewList)
			v.v.setVisibility(v.preSta);
		
		for (ViewItem v : mInvisibleViewList)
			v.v.setVisibility(v.preSta);
		
		for (ViewItem v : mGoneViewList)
			v.v.setVisibility(v.preSta);
		
		for (ViewItem v : mDisableViewList)
			v.v.setEnabled(v.preSta == 1);
	}
	
	protected void onCompleted(Object result) throws Exception {}
	protected void onFail(Exception e) {}
	
	private static class ViewItem {
		public View v;
		public int preSta;
	}
	
	private static class ViewList extends ArrayList<ViewItem> {
		
		private static final long serialVersionUID = 1L;

		public void add(View... views) {
			for (View v : views) {
				ViewItem viewItem = new ViewItem();
				viewItem.v = v;
				add(viewItem);
			}
		}
	}
	
	public static String readWholeFile(Context ct, int rid) {
		try {
			InputStream istream = ct.getResources().openRawResource(rid);
			StringBuilder builder = new StringBuilder();
			InputStreamReader reader = new InputStreamReader(istream);
			char[] buffer = new char[10000];
			int justRead = 0;
			do {
				justRead = reader.read(buffer);
				if (justRead == -1)
					break;
				else
					builder.append(buffer, 0, justRead);
			} while (true);
			return builder.toString();
		} catch (Exception e) {

		}
		
		return null;
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Listener
	///////////////////////////////////////////////////////////////////////////
	
	public interface Listener2 {
		public void onCompleted(Object result);
		public void onFail(Exception e);
	}
}