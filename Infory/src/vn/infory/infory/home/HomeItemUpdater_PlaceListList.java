package vn.infory.infory.home;

import java.util.ArrayList;
import java.util.List;

import vn.infory.infory.CyImageLoader;
import vn.infory.infory.FontsCollection;
import vn.infory.infory.R;
import vn.infory.infory.data.PlaceList;
import vn.infory.infory.data.home.HomeItem;
import vn.infory.infory.data.home.HomeItem_PlaceListList;
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

public class HomeItemUpdater_PlaceListList extends HomeItemUpdater {
	
	private LayoutInflater mInflater;
	
	@Override
	public void update(View view, HomeItem item, HomeFragment caller) {
		
		mInflater = caller.getActivity().getLayoutInflater();
		
		HomeItem_PlaceListList itemPlaceListList = (HomeItem_PlaceListList) item;
		
		ViewPager pager = (ViewPager) view.findViewById(R.id.pager);
		PlaceListListAdapter2 adapter = new PlaceListListAdapter2(itemPlaceListList.placelists,
				pager, caller);
		
		pager.setAdapter(adapter);
	}
	
	private class PlaceListListAdapter2 extends PagerAdapter {
		private ArrayList<ViewGroup> mViewList = new ArrayList<ViewGroup>();
		private List<PlaceList> mPlaceListList;
		private HomeListener mListener;
		
		public PlaceListListAdapter2(List<PlaceList> placeListList, ViewGroup container,
				HomeListener listener) {
			mPlaceListList = placeListList;
			mListener = listener;
			
			for (int i = 0; i < placeListList.size(); i++) {
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
			
			final PlaceList mItem = mPlaceListList.get(position);
			txtName.setText(mItem.title);
			txtNumber.setText(mItem.numOfShop);
			txtUnit.setText(mItem.content);
			
//			CyUtils.setHoverEffect(layoutImage, false);
			layoutImage.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mListener.onPlaceListClick(mItem.idPlacelist, mItem);
				}
			});
			
			CyImageLoader.instance().loadImage(mItem.cover, new CyImageLoader.Listener() {
				@Override
				public void loadFinish(int from, Bitmap image, String url, CyAsyncTask task) {
					layoutImage.setBackgroundDrawable(new BitmapDrawable(image));
				}
			}, HomeAdapter.mListSize, view.getContext());
			
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
	
//	private class PlaceListListAdapter extends FragmentStatePagerAdapter {
//		
//		private List<PlaceList> mItemList;
//		
//		public PlaceListListAdapter(List<PlaceList> placeListList, FragmentManager manager) {
//			super(manager);
//			mItemList = placeListList;
//		}
//
//		@Override
//		public Fragment getItem(int pos) {
//			return new PlaceListItemFragment(mItemList.get(pos));
//		}
//
//		@Override
//		public int getCount() {
//			return mItemList.size();
//		}
//		
//	}
//	
//	@SuppressLint("ValidFragment")
//	private class PlaceListItemFragment extends Fragment {
//		
//		private PlaceList mItem; 
//		
//		public PlaceListItemFragment(PlaceList item) {
//			mItem = item;
//		}
//		
//		@Override
//		public View onCreateView(LayoutInflater inflater, ViewGroup container,
//				Bundle savedInstanceState) {
//			View view = inflater.inflate(R.layout.home_block_2_item, container, false);
//			
//			TextView txtName = (TextView) view.findViewById(R.id.txtName);
//			TextView txtNumber = (TextView) view.findViewById(R.id.txtNumber);
//			TextView txtUnit = (TextView) view.findViewById(R.id.txtUnit);
//			final View layoutImage = view.findViewById(R.id.layoutImage);
//			
//			txtName.setText(mItem.title);
//			txtNumber.setText(mItem.numOfShop);
//			txtUnit.setText("ĐỊA ĐIỂM");
//			CyImageLoader.instance().loadImage(mItem.cover, new CyImageLoader.Listener() {
//				@Override
//				public void loadFinish(int from, Bitmap image, String url) {
//					layoutImage.setBackgroundDrawable(new BitmapDrawable(image));
//				}
//			}, new Point(), getActivity());
//			
//			return view;
//		}
//	}

}
