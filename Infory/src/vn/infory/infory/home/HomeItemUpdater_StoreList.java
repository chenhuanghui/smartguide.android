package vn.infory.infory.home;

import java.util.ArrayList;
import java.util.List;

import vn.infory.infory.CyImageLoader;
import vn.infory.infory.CyUtils;
import vn.infory.infory.FontsCollection;
import vn.infory.infory.R;
import vn.infory.infory.data.home.HomeItem;
import vn.infory.infory.data.home.HomeItem_StoreItem;
import vn.infory.infory.data.home.HomeItem_StoreList;
import vn.infory.infory.network.CyAsyncTask;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class HomeItemUpdater_StoreList extends HomeItemUpdater {
	
	private LayoutInflater mInflater;
	
	@Override
	public void update(View view, HomeItem item, HomeFragment caller) {
		
		mInflater = caller.getActivity().getLayoutInflater();
		
		HomeItem_StoreList storeList = (HomeItem_StoreList) item;
		
		ViewPager pager = (ViewPager) view.findViewById(R.id.pager);
		StoreListListAdapter adapter = new StoreListListAdapter(storeList.stores,
				pager, caller);
		
		pager.setAdapter(adapter);
	}
	
	private class StoreListListAdapter extends PagerAdapter {
		private ArrayList<ViewGroup> mViewList = new ArrayList<ViewGroup>();
		private List<HomeItem_StoreItem> mStoreList;
		private HomeListener mListener;
		
		public StoreListListAdapter(List<HomeItem_StoreItem> storeList, ViewGroup container,
				HomeListener listener) {
			mStoreList = storeList;
			mListener = listener;
			
			for (int i = 0; i < mStoreList.size(); i++) {
				View view = mInflater.inflate(R.layout.home_block_2_item, container, false);
				FontsCollection.setFont(view);
				mViewList.add((ViewGroup) view);
			}
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			ViewGroup view = mViewList.get(position);
			
			TextView txtName = (TextView) view.findViewById(R.id.txtName);
			TextView txtNumber = (TextView) view.findViewById(R.id.txtNumber);
			TextView txtUnit = (TextView) view.findViewById(R.id.txtUnit);
			final View layoutImage = view.findViewById(R.id.layoutImage);
			
			final HomeItem_StoreItem mItem = mStoreList.get(position);
			txtName.setText(mItem.storeInfo.storeName);
			txtNumber.setText(mItem.numOfPurchase);
			txtUnit.setText(mItem.content);
			CyImageLoader.instance().loadImage(mItem.cover, new CyImageLoader.Listener() {
				@Override
				public void loadFinish(int from, Bitmap image, String url, CyAsyncTask task) {
					layoutImage.setBackgroundDrawable(new BitmapDrawable(image));
				}
			}, HomeAdapter.mListSize, view.getContext());
			
			CyUtils.setHoverEffect(layoutImage, false);
			layoutImage.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mListener.onStoreItemClick("" + mItem.storeInfo.idStore);
				}
			});
			
			container.addView(view);
			return view;
		}

		@Override
		public int getCount() {
			return mViewList.size();
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View)object);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return (view == object);
		}
		
		@Override
		public int getItemPosition(Object object) {
			int index = mViewList.indexOf(object);
			if (index < 0)
				return PagerAdapter.POSITION_NONE;
			else
				return index;
		}
	}

}
