package vn.infory.infory;

import java.util.ArrayList;

import vn.infory.infory.network.CyAsyncTask;
import vn.infory.infory.network.GetShopGallery;
import vn.infory.infory.network.NetworkManager;
import vn.infory.infory.network.CyAsyncTask.Listener2;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class LazyPagerAdapter extends FragmentStatePagerAdapter 
implements Listener2, OnPageChangeListener {
	
	protected FragmentActivity mAct;
	protected CyAsyncTask mLoader;
	protected int mLoadingRID;
	protected ArrayList mItemList;
	protected LayoutInflater mInflater;
	protected ArrayList<ItemFragment> mFragList= new ArrayList<ItemFragment>();
	
	public boolean mLoading, mIsMore = true;
	protected int mPageNum = 0;

	public LazyPagerAdapter(FragmentActivity act, CyAsyncTask loader,
			int loadingID, ArrayList itemList) {
		super(act.getSupportFragmentManager());
		
		mAct 		= act;
		mLoader 	= loader;
		mLoader.setListener(this);
		mInflater 	= act.getLayoutInflater();
		mLoadingRID = loadingID;
		mItemList 	= new ArrayList(itemList);
	}

	@Override
	public Fragment getItem(int pos) {
		
		ItemFragment frag = new ItemFragment(pos);
		
		return frag;
	}
	abstract public View getView(LayoutInflater inflater, ViewGroup container, 
			int pos, Object dataItem, boolean isLoading);
	abstract public void transform(View convertView, int pos, Object dataItem);
	
	public void setPage(int page) {
		mPageNum = page;
	}
	
	private void loadMore() {
		if (!mIsMore || mLoading)
			return;
		
		mLoading = true;
		mLoader.setPage(mPageNum);
		mLoader.executeOnExecutor(NetworkManager.THREAD_POOL);
	}

	@Override
	public int getCount() {
		return mItemList.size() + (mIsMore ? 1 : 0);
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
		
		if (itemList.size() < LazyLoadAdapter.ITEM_PER_PAGE)
			mIsMore = false;
		
		for (ItemFragment item : mFragList) {
			if (item.mLoading) {
				item.mLoading = false;
				transform(item.getView(), item.mPos, mItemList.get(item.mPos));
			}
		}
		
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

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageSelected(int position) {
		if (position >= mItemList.size()) {
			loadMore();
		}
	}
	
	@Override
	public int getItemPosition(Object object) {
		for (int i = 0; i < mItemList.size(); i++)
			if (object == mItemList.get(i))
				return i;

		return PagerAdapter.POSITION_NONE;
	}
	
	@SuppressLint("ValidFragment")
	public class ItemFragment extends Fragment {
		
		public int mPos;
		public boolean mLoading;
		
		public ItemFragment() {}
		
		public ItemFragment(int pos) {
			mPos = pos;
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			
			Object dataItem = null;
			if (mPos < mItemList.size())
				dataItem = mItemList.get(mPos);
			return LazyPagerAdapter.this.getView(inflater, container, 
					mPos, dataItem, mPos >= mItemList.size());
		}
		
		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			super.onViewCreated(view, savedInstanceState);
			
			while (mFragList.size() < mPos + 1)
				mFragList.add(null);
			
			mFragList.set(mPos, this);
		}
		
		@Override
		public void onDestroyView() {
			super.onDestroyView();
			
			mFragList.set(mPos, null);
		}
	}
}
