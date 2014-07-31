package vn.infory.infory.home;

import java.util.ArrayList;

import vn.infory.infory.CyUtils;
import vn.infory.infory.FontsCollection;
import vn.infory.infory.LazyLoadAdapter;
import vn.infory.infory.R;
import vn.infory.infory.data.home.HomeItem;
import vn.infory.infory.network.GetHome;
import android.app.Activity;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class HomeAdapter extends LazyLoadAdapter {
	
	private HomeFragment mCaller;
	private HomeItemUpdater[] mUpdaterArr = new HomeItemUpdater[10];
	
	public static Point mLogoSize;
	public static Point mListSize;
	public static Point mImageSize;
//	public static Point mCoverSize;
	
	public HomeAdapter(Activity act, HomeFragment caller) {
		super(act, new GetHome(act, 0), R.layout.shop_list_loading, 9, new ArrayList<HomeItem>());
		mCaller = caller;
		
		int logoEdge = CyUtils.dpToPx(36, caller.getActivity());
		mLogoSize = new Point(logoEdge, logoEdge);
		mListSize = calcViewSizeById(R.layout.home_block_2);
		mImageSize = calcViewSizeById(R.layout.home_block_3);
//		mCoverSize = calcViewSizeByRatio(3.6f);
		
		mUpdaterArr[1] = new HomeItemUpdater_BranchPromoInfo();
		mUpdaterArr[2] = new HomeItemUpdater_ImageList();
		mUpdaterArr[3] = new HomeItemUpdater_PlaceListList();
		mUpdaterArr[4] = new HomeItemUpdater_ShopList();
		mUpdaterArr[5] = new HomeItemUpdater_StoreList();
		mUpdaterArr[6] = new HomeItemUpdater_ShopItem();
		mUpdaterArr[7] = new HomeItemUpdater_StoreItem();
		mUpdaterArr[8] = new HomeItemUpdater_ShopPromoInfo();
		mUpdaterArr[9] = mUpdaterArr[2];
	}
	
	private Point calcViewSizeById(int rid) {
		
		View v = mCaller.getActivity().getLayoutInflater().inflate(
				rid, (ViewGroup) mCaller.getView(), false);

		int w = mCaller.getResources().getDisplayMetrics().widthPixels;
		int measureWidth = MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY);
		int measureHeight = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		v.measure(measureWidth, measureHeight);
		return new Point(w, v.getMeasuredHeight());
	}
	
	private Point calcViewSizeByRatio(float ratio) {
		
		int w = mCaller.getResources().getDisplayMetrics().widthPixels;
		return new Point(w, (int) (w / ratio));
	}
	
	@Override
	public int getItemViewType(int position) {
		
		int type = super.getItemViewType(position);
		if (type != -1) {
			return type;
		} else {
			return ((HomeItem) mItemList.get(position)).type - 1;
		}
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = super.getView(position, convertView, parent);
		
		if (position >= mItemList.size()) {
			((AnimationDrawable) convertView.findViewById(R.id.layoutLoading).getBackground()).start();
			return convertView;
		}
		
		HomeItem item = (HomeItem) getItem(position);
		
		if (convertView == null) {
			// Create new view item
			switch (item.type) {
			
			// home_block_1 : message
			case 1:
			case 8:
				convertView = mInflater.inflate(R.layout.home_block_1, parent, false);
				CyUtils.setHoverEffect(convertView.findViewById(R.id.layoutClick), false);
				break;
				
			// home_block_2 : list
			case 3:
			case 4:
			case 5: {
				convertView = mInflater.inflate(R.layout.home_block_2, parent, false);
				final ViewPager pager = (ViewPager) convertView.findViewById(R.id.pager);
				convertView.findViewById(R.id.btnBack).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						int index = Math.max(pager.getCurrentItem() - 1, 0);
						pager.setCurrentItem(index);
					}
				});
				
				convertView.findViewById(R.id.btnForward).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						int index = Math.min(pager.getCurrentItem() + 1, pager.getAdapter().getCount() - 1);
						pager.setCurrentItem(index);
					}
				});
			}
				break;
			
			// home_block_3 : image list
			case 2:
			case 9:
				convertView = mInflater.inflate(R.layout.home_block_3, parent, false);
				break;
				
			// home_block_4 : item
			case 6:
			case 7:
				convertView = mInflater.inflate(R.layout.home_block_4, parent, false);
				CyUtils.setHoverEffect(convertView.findViewById(R.id.btnGoto), false);
				break;
			}
			
//			FontsCollection.setFont(convertView);
		}
		
		HomeItemUpdater updater = mUpdaterArr[item.type];
		if (updater != null) {
			updater.update(convertView, item, mCaller);
		}
		
		return convertView;
	}
}
