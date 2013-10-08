package vn.smartguide;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.Animator.AnimatorListener;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by ChauSang on 6/24/13.
 */
public class ShopListFragment extends Fragment {

	private static final int[] ICON_ID = new int[] { R.drawable.iconpin_food, R.drawable.iconpin_drink,
		R.drawable.iconpin_healness, R.drawable.iconpin_entertaiment, R.drawable.iconpin_fashion,
		R.drawable.iconpin_travel, R.drawable.iconpin_shopping, R.drawable.iconpin_education};
	
	// GUI elements
	private GridView gridView;
	public List<Shop> mShopList;
	private ShopListAdapter mAdapter;	

	private ImageView mLoadingCircle;
	private ImageView mLoadingMiddle;
	private ImageView mLoadingBackground;
	private RelativeLayout mLoadingOptical;

	private ObjectAnimator mRotateAnimation;
	private ObjectAnimator mFadeOutCircle;
	private ObjectAnimator mFadeOutMiddle;
	private ObjectAnimator mFadeInCircle;
	private ObjectAnimator mFadeInMiddle;
	
	private AnimatorSet	mAniSetStart;
	private AnimatorSet mAniSetFinish;

	// Data
//	private String mJson = "";
//	public boolean mHaveAnimation 	= false;
	private Listener mListener 	= new Listener();
	private boolean isMore 		= false;
	private boolean isSearch 	= false;

	private String mSearchString = "";
	private int indexPage = 0;

	///////////////////////////////////////////////////////////////////////////
	// Override methods
	///////////////////////////////////////////////////////////////////////////
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.shop_list, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Get GUI elements
		gridView = (GridView) getView().findViewById(R.id.grid_view_shop_list);

		mLoadingCircle = (ImageView) getView().findViewById(R.id.loadingCircleS);
		mLoadingMiddle = (ImageView) getView().findViewById(R.id.loadingMidleS);
		mLoadingBackground = (ImageView) getView().findViewById(R.id.loadingBackgroundS);
		mLoadingOptical = (RelativeLayout) getView().findViewById(R.id.foregroundLoading);

		// Set up animation
		mRotateAnimation = ObjectAnimator.ofFloat(mLoadingCircle, "rotation", 0, 360).setDuration(1100);
		mRotateAnimation.setInterpolator(new LinearInterpolator());
		mRotateAnimation.setRepeatCount(ObjectAnimator.INFINITE);
		mRotateAnimation.setRepeatMode(ObjectAnimator.INFINITE);

		mFadeInCircle = ObjectAnimator.ofFloat(mLoadingCircle, "alpha", 0.0f, 1.0f);
		mFadeInCircle.setDuration(200);
		mFadeInCircle.setInterpolator(new LinearInterpolator());

		mFadeInMiddle = ObjectAnimator.ofFloat(mLoadingMiddle, "alpha", 1.0f, 1.0f);
		mFadeInMiddle.setDuration(200);
		mFadeInMiddle.setInterpolator(new LinearInterpolator());
		
		mFadeOutCircle = ObjectAnimator.ofFloat(mLoadingCircle, "alpha", 1.0f, 0.0f);
		mFadeOutCircle.setDuration(1000);
		mFadeOutCircle.setInterpolator(new AccelerateDecelerateInterpolator());

		mFadeOutMiddle = ObjectAnimator.ofFloat(mLoadingMiddle, "alpha", 1.0f, 0.0f);
		mFadeOutMiddle.setDuration(1000);
		mFadeOutMiddle.setInterpolator(new AccelerateDecelerateInterpolator());
		
		mAniSetStart = new AnimatorSet();
		mAniSetStart.playTogether(mFadeInMiddle, mFadeInCircle, mRotateAnimation);
		mAniSetStart.addListener(new AnimatorListener() {
			public void onAnimationStart(Animator animation) { 
				mLoadingOptical.setVisibility(View.VISIBLE);
			}
			public void onAnimationRepeat(Animator animation) { }
			public void onAnimationEnd(Animator animation) { }
			public void onAnimationCancel(Animator animation) { }
		});
		
		mAniSetFinish = new AnimatorSet();
		mAniSetFinish.playTogether(mFadeOutCircle, mFadeOutMiddle);
		mAniSetFinish.addListener(new AnimatorListener() {
			public void onAnimationStart(Animator animation) { }
			public void onAnimationRepeat(Animator animation) { }
			public void onAnimationEnd(Animator animation) { 
				mLoadingOptical.setVisibility(View.INVISIBLE);
			}
			public void onAnimationCancel(Animator animation) { 
				onAnimationEnd(animation);
			}
		});

		// Set up list view
		mShopList = new ArrayList<Shop>();
		mAdapter = new ShopListAdapter();
		gridView.setAdapter(mAdapter);
		gridView.setOnItemClickListener(mAdapter);
		gridView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, 
					int visibleItemCount, int totalItemCount) {
				if (((firstVisibleItem >= indexPage * GlobalVariable.itemPerPage + GlobalVariable.needLoadMore) ||
						visibleItemCount >= GlobalVariable.needLoadMore) && isMore == true) {
					
					isMore = false;
					new FetchMoreShopListTask().execute();
				}
			}
		});
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Public methods
	///////////////////////////////////////////////////////////////////////////
	
	public void setListener(Listener listener) {
		if (listener == null)
			listener = new Listener();
		mListener = listener;
	}
	
	public String getTitle() {
		String title = null;
		if (isSearch) {
			title = mSearchString;
		} else {
			int lengthOfFilterString = GlobalVariable.mFilterString.length();
			if (lengthOfFilterString >= 2)
				title = "NHIỀU DANH MỤC";
			else {
				int shopType = Integer.valueOf(GlobalVariable.mFilterString);
				String[] cateName = new String[] {"", "ẨM THỰC", "CAFE & BAR", "LÀM ĐẸP", "GIẢI TRÍ", 
						"THỜI TRANG", "DU LỊCH", "SẢN PHẨM", "GIÁO DỤC"};
				title = cateName[shopType];
			}
		}
		return title;
	}
	
	public void updateSGP(int id, int sgp) {
		if (mShopList == null || mShopList.size() == 0)
			return;

		for(int i = 0; i < mShopList.size(); i++) {
			if (mShopList.get(i).mID == id) {
				((PromotionTypeOne)mShopList.get(i).mPromotion).mSGP = sgp;
				mAdapter.notifyDataSetChanged();
				return;
			}
		}
	}

	public void search(String search) {
		indexPage = 0;
		isSearch = true;

		// get search result
		mSearchString = search;
		new SearchShopListTask(search).execute();
	}
	
	public void filter() {
		indexPage = 0;
		isSearch = false;
		isMore = true;
		
		new FindShopList().execute();
	}

	public void update(String json) {
		try {
			update_throw(json);
		} catch (Exception e) {
			GlobalVariable.showToast("Không lấy được danh sách cửa hàng", getActivity());
		}
	}
	
	public void update_throw(String json) throws Exception {
		indexPage = 0;
		isSearch = false;
		isMore = true;
		
		if (json.length() == 0)
			return;
		mAdapter.clear();
		mAdapter.addAll(Shop.getListForUseThrow(new JSONArray(json)));
	} 

	public void setForeground() {
		mLoadingOptical.setVisibility(View.VISIBLE);
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Adapter
	///////////////////////////////////////////////////////////////////////////
	
	private class ShopListAdapter extends ArrayAdapter<Shop> implements OnItemClickListener
	{
		private HashSet<Integer> mHighLightItem = new HashSet<Integer>();

		public ShopListAdapter() {
			super(getActivity(), R.layout.shop_list_item, R.id.shop_name_real, mShopList);
			
			if (mShopList.size() == 0)
				isMore = false;
			else if (mShopList.size() % 10 == 0)
				isMore = true;

			indexPage = 0;
		}
		
		@Override
		public void clear() {
			mHighLightItem.clear();
			super.clear();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {			
			View MyView = super.getView(position, convertView, parent);

			Shop mShop = mShopList.get(position);

			// Get GUI elements

			// Distance text
			TextView mDistantTV = (TextView) MyView.findViewById(R.id.shop_distance);

			// Score layout
			LinearLayout mShopScoreNowMin = (LinearLayout) MyView.findViewById(R.id.shop_score_now_min);
			TextView mShopScoreNowTV = (TextView) mShopScoreNowMin.findViewById(R.id.shop_score_now);
			TextView mShopScoreMinTV = (TextView) mShopScoreNowMin.findViewById(R.id.shop_score_min);

			// Promotion type layout
			LinearLayout mShopTypeScore = (LinearLayout) MyView.findViewById(R.id.shop_type_score);
			ImageView imgShopType = (ImageView) mShopTypeScore.findViewById(R.id.shop_type_image);
			TextView txtShopScore = (TextView) mShopTypeScore.findViewById(R.id.shop_score);

			// Shop logo
			final ImageView mShopCover = (ImageView) MyView.findViewById(R.id.shop_cover);
			LinearLayout shop_cover_layout_tran = (LinearLayout) MyView.findViewById(R.id.shop_cover_layout_tran);

			// Shop content
			LinearLayout mShopNameContent = (LinearLayout)MyView.findViewById(R.id.shop_name_content);
			TextView mShopName = (TextView) mShopNameContent.findViewById(R.id.shop_name_real);
			TextView mShopContent = (TextView) mShopNameContent.findViewById(R.id.shop_content_real);
			ImageView mShopTypeIcon = (ImageView) MyView.findViewById(R.id.shop_type_icon);

			// Set promotion visibility
			int promoVisibility = mShop.mPromotionStatus ? View.VISIBLE : View.INVISIBLE;
			imgShopType.setVisibility(promoVisibility);
			txtShopScore.setVisibility(promoVisibility);

			// Set highlight
			if (mHighLightItem.contains(position)) {
				mDistantTV.setBackgroundResource(R.drawable.shop_distance_red);
				mShopScoreNowMin.setBackgroundResource(R.drawable.shop_score_red);
				mShopTypeScore.setBackgroundResource(R.drawable.shop_type_red);
				shop_cover_layout_tran.setBackgroundResource(R.drawable.shop_avatar_red_tran);
				mShopNameContent.setBackgroundResource(R.drawable.shop_content_red);
			} else {
				mDistantTV.setBackgroundResource(R.drawable.shop_distance_green);
				mShopScoreNowMin.setBackgroundResource(R.drawable.shop_score_green);
				mShopTypeScore.setBackgroundResource(R.drawable.shop_type_green);
				shop_cover_layout_tran.setBackgroundResource(R.drawable.shop_avatar_green_tran);
				mShopNameContent.setBackgroundResource(R.drawable.shop_content_green);
			}
			
			// Set distance
			if (mShop.mDistance == - 1)
				mDistantTV.setText("... KM");
			else
				mDistantTV.setText(Float.toString(mShop.mDistance) + " KM");			

			// Set promotion type
			if (mShop.mPromotionStatus) {					
				switch(mShop.mPromotion.getType()) {
				case 1:
					PromotionTypeOne promotionTypeOne = (PromotionTypeOne) mShop.mPromotion;
					mShopScoreNowTV.setText(Integer.toString(promotionTypeOne.mSGP));
					mShopScoreMinTV.setText("/" + promotionTypeOne.mMinScore);
					
					imgShopType.setBackgroundResource(R.drawable.point);
					txtShopScore.setText("điểm");
					break;

				case 2:
					PromotionTypeTwo promotionTypeTwo = (PromotionTypeTwo) mShop.mPromotion;
					mShopScoreNowTV.setText(promotionTypeTwo.mMoney);
					mShopScoreMinTV.setText("");
					
					imgShopType.setBackgroundResource(R.drawable.money);
					txtShopScore.setText("vnd");
					break;
				}
			}
			
			// Set logo
			final int index = position; 
			GlobalVariable.cyImageLoader.loadImage(mShop.mLogo, new CyImageLoader.Listener() {
//			GlobalVariable.cyImageLoader.loadImage(
//					CyImageLoader.DUMMY_PATH[position % CyImageLoader.DUMMY_PATH.length],
//					new CyImageLoader.Listener() {
						
				@Override
				public void startLoad(int from) {
					switch (from) {
						case CyImageLoader.FROM_DISK:
						case CyImageLoader.FROM_NETWORK:
							mShopCover.setImageResource(R.drawable.ava_loading);
							break;
					}
				}
				
				@Override
				public void loadFinish(int from, Bitmap image, String url) {
					switch (from) {
					case CyImageLoader.FROM_MEMORY:
						mShopCover.setImageBitmap(image);
						break;
						
					case CyImageLoader.FROM_DISK:
					case CyImageLoader.FROM_NETWORK:
//						if (gridView.getFirstVisiblePosition() >= index &&
//							index <= gridView.getLastVisiblePosition()) {
//							mShopCover.setImageBitmap(image);
							notifyDataSetChanged();
//						}
						break;
					}
				}
				
			}, new Point(128, 128), getActivity());
			
//			GlobalVariable.imageLoader.displayImage(mShop.mLogo, mShopCover);

			// Set content
			mShopName.setText(mShop.mName);
			mShopContent.setText(mShop.mAddress);
			mShopTypeIcon.setBackgroundResource(ICON_ID[mShop.mGroupShop-1]);

			return MyView;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			mHighLightItem.add(position);
			Shop s = mShopList.get(position);
			GlobalVariable.mCurrentShop = s;
			notifyDataSetChanged();
			mListener.onShopClick(s);
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Network async task
	///////////////////////////////////////////////////////////////////////////

	public void updateShopList() {
			mAdapter.notifyDataSetChanged();
	}

	private class FetchMoreShopListTask extends AsyncTask<Void, Void, List<Shop>> {
		
		private Exception mEx;

		@Override
		protected List<Shop> doInBackground(Void... params) {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("group_list", GlobalVariable.mFilterString));
			pairs.add(new BasicNameValuePair("city_id", GlobalVariable.mCityID));
			pairs.add(new BasicNameValuePair("user_id", GlobalVariable.userID));
			pairs.add(new BasicNameValuePair("user_lat", Float.toString(GlobalVariable.mLat)));
			pairs.add(new BasicNameValuePair("user_lng", Float.toString(GlobalVariable.mLng)));
			pairs.add(new BasicNameValuePair("page", Integer.toString(++indexPage)));
			pairs.add(new BasicNameValuePair("sort_by", GlobalVariable.mSortByString));
			pairs.add(new BasicNameValuePair("shop_name", mSearchString));
			pairs.add(new BasicNameValuePair("version", "1"));

			try {
				String json = null;
				if (isSearch)
					json = NetworkManger.post(APILinkMaker.mSearch(), pairs);
				else
					json = NetworkManger.post(APILinkMaker.ShopListInCategory(), pairs);
				return Shop.getListForUse(new JSONArray(json));
			} catch (Exception e) {
				mEx = e;
				return null;
			}
		}

		@Override
		protected void onPostExecute(List<Shop> shopList) {

			if (mEx == null) {
					if (shopList == null || shopList.size() == 0)
						isMore = false;
					else {
						/////////////////////////////////
						for (Shop s : shopList)
							mAdapter.add(s);

						if (shopList.size() % GlobalVariable.itemPerPage == 0)
							isMore = true;
						else
							isMore = false;

						mAdapter.notifyDataSetChanged();
					}
			} else {
				
			}
		}
	}

	public class SearchShopListTask extends AsyncTask<Void, Void, Boolean> {
		
		private List<Shop> mShopList;
		private String mName;
		private Exception mEx;

		public SearchShopListTask(String name) {
			mName = name;
		}
		
		@Override
		protected void onPreExecute() {
			mAniSetStart.start();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("shop_name", mName));
			pairs.add(new BasicNameValuePair("user_id", GlobalVariable.userID));
			pairs.add(new BasicNameValuePair("user_lat", Float.toString(GlobalVariable.mLat)));
			pairs.add(new BasicNameValuePair("user_lng", Float.toString(GlobalVariable.mLng)));
			pairs.add(new BasicNameValuePair("page", Integer.toString(indexPage)));
			try {
				String json = NetworkManger.post(APILinkMaker.mSearch(), pairs);
				mShopList = Shop.getListForUseThrow(new JSONArray(json));
			} catch (Exception e) {
				mEx = e;
			}

			return true;
		}

		@Override
		protected void onPostExecute(Boolean k) {

			if (mEx == null) {
				mAniSetFinish.addListener(new AnimatorListener() {
					
					public void onAnimationStart(Animator animation) { }
					public void onAnimationRepeat(Animator animation) { }
					public void onAnimationEnd(Animator animation) {
						mAniSetFinish.removeListener(this);
						if (mShopList != null) {
//							mAdapter = new ShopListAdapter(getActivity().getBaseContext(), getActivity());
//							gridView.setAdapter(mAdapter);
							mAdapter.clear();
							mAdapter.addAll(mShopList);
							((MainActivity) getActivity()).updateMapAsync();
							((MainActivity) getActivity()).jumpToBound();
						}
					}
					public void onAnimationCancel(Animator animation) { 
						onAnimationEnd(animation);
					}
				});
				mAniSetFinish.start();
			} else {
				GlobalVariable.showToast("Tìm kiếm thất bại", getActivity());
				mAniSetFinish.start();
			}
		}
	}
		
	private class FindShopList extends AsyncTask<Void, Void, Boolean> {
		
		private Exception mEx;
		private String json;
		
		@Override
		protected void onPreExecute() {
			mAniSetStart.start();
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			
			try {
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("group_list", GlobalVariable.mFilterString));
				pairs.add(new BasicNameValuePair("city_id", GlobalVariable.mCityID));
				pairs.add(new BasicNameValuePair("user_id", GlobalVariable.userID));
				pairs.add(new BasicNameValuePair("user_lat", Float.toString(GlobalVariable.mLat)));
				pairs.add(new BasicNameValuePair("user_lng", Float.toString(GlobalVariable.mLng)));
				pairs.add(new BasicNameValuePair("page", "0"));
				pairs.add(new BasicNameValuePair("sort_by", GlobalVariable.mSortByString));
				pairs.add(new BasicNameValuePair("version", "1"));
	
				json = NetworkManger.post(APILinkMaker.ShopListInCategory(), pairs);
			} catch (Exception e) {
				mEx = e;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean k) {
			
			try {
				if (mEx != null)
					throw mEx;
				
				update_throw(json);
			} catch (Exception e) {
				GlobalVariable.showToast("Không lấy được danh sách cửa hàng", getActivity());
			}
			
			mAniSetFinish.start();
		}
	}

	public void releaseMemory() {
//		try{
//			mShopList = new ArrayList<Shop>();
//			updateShopList();
//		}catch(Exception ex){
//			
//		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Listener
	///////////////////////////////////////////////////////////////////////////
	
	public static class Listener {
		public void onShopClick(Shop s) {} 
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Debug stuff
	///////////////////////////////////////////////////////////////////////////
	
	private static final boolean isDebug = true;
	private static final String TAG = "CycrixDebug";
	private static final String HEADER = "ShopListFragment";
	private static void debugLog(String message) {
		if (isDebug) Log.d(TAG, HEADER + " " + message);
	}	
}