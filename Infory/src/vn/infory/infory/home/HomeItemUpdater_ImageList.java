package vn.infory.infory.home;

import java.util.ArrayList;
import java.util.List;

import vn.infory.infory.CyImageLoader;
import vn.infory.infory.R;
import vn.infory.infory.data.home.HomeItem;
import vn.infory.infory.data.home.HomeItem_ImageList;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.viewpagerindicator.CirclePageIndicator;

public class HomeItemUpdater_ImageList extends HomeItemUpdater {
	
	private LayoutInflater mInflater;
	
	@Override
	public void update(View view, HomeItem item, HomeFragment caller) {
		
		mInflater = caller.getActivity().getLayoutInflater();

		HomeItem_ImageList itemImageList = (HomeItem_ImageList) item;

		ViewPager pagerImage = (ViewPager) view.findViewById(R.id.pagerImage);
		FrameLayout ratioLayout = (FrameLayout) view.findViewById(R.id.layoutRatio);
		
		if (itemImageList.imageWidth == 0 ||
			itemImageList.imageHeight == 0) {
			itemImageList.imageWidth = 1;
			itemImageList.imageHeight = 1;
		}
		
		ratioLayout.setContentDescription("ratio:" + 
		(float) itemImageList.imageWidth / itemImageList.imageHeight);

		ImageAdapter2 adapter = new ImageAdapter2(itemImageList.images, pagerImage, caller);
		pagerImage.setAdapter(adapter);
	}
	
	private class ImageAdapter2 extends PagerAdapter {

		private ArrayList<ViewGroup> mViewList = new ArrayList<ViewGroup>();
		private List<String> mUrlList;
		private HomeListener mListener;
		
		public ImageAdapter2(List<String> urlList, ViewGroup container, HomeListener listener) {
			mUrlList = urlList;
			mListener = listener;
			
			for (int i = 0; i < urlList.size(); i++) {
				mViewList.add((ViewGroup)
						mInflater.inflate(R.layout.home_image_item, container, false));
			}
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, final int position) {
			ViewGroup view = mViewList.get(position);
			
			ImageView img = (ImageView) view.findViewById(R.id.imgImage);
			// 364 huynh tan phat
			CyImageLoader.instance().showImage(mUrlList.get(position), img, HomeAdapter.mImageSize);
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					mListener.onImageClick(mUrlList, position);
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
	
//	private class ImageAdapter extends FragmentPagerAdapter {
//		
//		private List<String> mUrlList;
//		
//		public ImageAdapter(FragmentManager fm, List<String> urlList) {
//			super(fm);
//			mUrlList = urlList;
//		}
//
//		@Override
//		public Fragment getItem(int pos) {
//			return new ImageFragmentItem(mUrlList.get(pos));
//		}
//
//		@Override
//		public int getCount() {
//			return mUrlList.size();
//		}
//	}
//	
//	@SuppressLint("ValidFragment")
//	private static class ImageFragmentItem extends Fragment {
//		
//		private String mUrl;
//		
//		public ImageFragmentItem(String imageUrl) {
//			mUrl = imageUrl;
//		}
//		
//		@Override
//		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//			View view = inflater.inflate(R.layout.home_image_item, container, false);
//			ImageView img = (ImageView) view.findViewById(R.id.imgImage);
//			CyImageLoader.instance().showImage(mUrl, img);
//			return view;
//		}
//	}
}

