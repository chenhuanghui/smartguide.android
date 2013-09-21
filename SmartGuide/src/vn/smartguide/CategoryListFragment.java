package vn.smartguide;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CategoryListFragment extends Fragment {

	// GUI elements
	private ImageView mLoadingCircle;
	private ImageView mLoadingMiddle;
	private ImageView mLoadingBackground;
	private RelativeLayout mLoadingOptical;

	private ObjectAnimator mRotateAnimation;
	private ObjectAnimator mFadeOutCircle;
	private ObjectAnimator mFadeOutMiddle;
	private ObjectAnimator mFadeInCircle;
	private ObjectAnimator mFadeInMiddle;
	
	private AnimatorSet mBeginLoadAniSet;
	private AnimatorSet mEndLoadAniSet;

	private GridView gridView;
	private ImageAdapter mAdapter;

	// Data
	private Listener mListener = new Listener();

	///////////////////////////////////////////////////////////////////////////
	// Override methods
	///////////////////////////////////////////////////////////////////////////
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);
		View root = inflater.inflate(R.layout.category_list, container, false);
		return root;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Get GUI element
		gridView = (GridView) getView().findViewById(R.id.grid_view);
		mLoadingCircle = (ImageView) getView().findViewById(R.id.loadingCircle);
		mLoadingMiddle = (ImageView) getView().findViewById(R.id.loadingMidle);
		mLoadingBackground = (ImageView) getView().findViewById(R.id.loadingBackground);
		mLoadingOptical = (RelativeLayout) getView().findViewById(R.id.bgloading);

		// Set up animation
		mFadeOutCircle = ObjectAnimator.ofFloat(mLoadingCircle, "alpha", 1.0f, 0.0f);
		mFadeOutCircle.setDuration(1000);
		mFadeOutCircle.setInterpolator(new LinearInterpolator());

		mFadeOutMiddle = ObjectAnimator.ofFloat(mLoadingMiddle, "alpha", 1.0f, 0.0f);
		mFadeOutMiddle.setDuration(1000);
		mFadeOutMiddle.setInterpolator(new LinearInterpolator());

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
		
		mBeginLoadAniSet = new AnimatorSet();
		mBeginLoadAniSet.playTogether(mFadeInMiddle, mFadeInCircle, mRotateAnimation);
		mBeginLoadAniSet.addListener(new AnimatorListener() {
			
			public void onAnimationStart(Animator animation) {
				mLoadingCircle.setVisibility(View.VISIBLE);
				mLoadingMiddle.setVisibility(View.VISIBLE);
				mLoadingBackground.setVisibility(View.VISIBLE);
				mLoadingOptical.setVisibility(View.VISIBLE);
			}
			
			public void onAnimationRepeat(Animator animation) {	}
			public void onAnimationEnd(Animator animation) { }	
			public void onAnimationCancel(Animator animation) { }
		});
		
		mEndLoadAniSet = new AnimatorSet();
		mEndLoadAniSet.playTogether(mFadeOutCircle, mFadeOutMiddle);
		mEndLoadAniSet.addListener(new AnimatorListener() {
			
			public void onAnimationStart(Animator animation) { }
			public void onAnimationRepeat(Animator animation) { }
			public void onAnimationEnd(Animator animation) {
				mLoadingBackground.setVisibility(View.INVISIBLE);
				mLoadingOptical.setVisibility(View.INVISIBLE);
				mRotateAnimation.cancel();
			}
			
			public void onAnimationCancel(Animator animation) {
				onAnimationEnd(animation);
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
	
	public void updateCategoryList() {
		
		if (mAdapter == null) {
			mAdapter = new ImageAdapter();
			gridView.setAdapter(mAdapter);
			gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
					if (position == 0){
						GlobalVariable.mFilterString = "1,2,3,4,5,6,7,8";
						
						if (mListener.onCategoryClick(position))
							return;
					}
					else
						GlobalVariable.mFilterString = Integer.toString(position);

					new FindShopList().execute();
				}
			});
		} else
			mAdapter.notifyDataSetChanged();
	}

	public boolean mIsRunning = false;
	public void autoUpdate() {
		if (mIsRunning == true)
			return;
		mIsRunning = true;
		new UpdateNumber().execute();
	}

	///////////////////////////////////////////////////////////////////////////
	// Network AsyncTask
	///////////////////////////////////////////////////////////////////////////
	
	public class FindShopList extends AsyncTask<Void, Void, Boolean> {
		
		private String json; 

		@Override
		protected void onPreExecute() {
			mListener.onStartLoadShopList();
			mBeginLoadAniSet.start();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("group_list", GlobalVariable.mFilterString));
			pairs.add(new BasicNameValuePair("city_id", GlobalVariable.mCityID));
			pairs.add(new BasicNameValuePair("user_id", GlobalVariable.userID));
			pairs.add(new BasicNameValuePair("user_lat", Float.toString(GlobalVariable.mLat)));
			pairs.add(new BasicNameValuePair("user_lng", Float.toString(GlobalVariable.mLng)));
			pairs.add(new BasicNameValuePair("page", "0"));
			pairs.add(new BasicNameValuePair("sort_by", GlobalVariable.mSortByString));

			json = NetworkManger.post(APILinkMaker.ShopListInCategory(), pairs);
//			(mMainAcitivyListener).getShopListFragment().update(json);	/////////
			return true;
		}

		@Override
		protected void onPostExecute(Boolean k) {
			mListener.onFinishLoadShopList(json, true, null);
			mEndLoadAniSet.addListener(new AnimatorListener() {
				
				public void onAnimationStart(Animator animation) {}
				public void onAnimationRepeat(Animator animation) {}			
				public void onAnimationEnd(Animator animation) {
					mEndLoadAniSet.removeListener(this);
					mListener.onFinishAnimation();
				}
				
				public void onAnimationCancel(Animator animation) {
					onAnimationEnd(animation);
				}
			});
			mEndLoadAniSet.start();
		}
	}

	public class UpdateNumber extends AsyncTask<Void, Void, Boolean> {
		
		@Override
		protected void onPreExecute() {
			mBeginLoadAniSet.start();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("city", GlobalVariable.mCityID));
			pairs.add(new BasicNameValuePair("env", Integer.toString(GlobalVariable.mMode)));

			String json = NetworkManger.post(APILinkMaker.mGroupByCity(), pairs);

			try {
				JSONObject object = new JSONObject(json);
				GlobalVariable.mCateogries = Category.getListCategory(object.getJSONArray("content"));
			} catch (JSONException e) {
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean k){

			mEndLoadAniSet.addListener(new AnimatorListener() {
				
				public void onAnimationStart(Animator animation) {}
				public void onAnimationRepeat(Animator animation) {}			
				public void onAnimationEnd(Animator animation) {
					mEndLoadAniSet.removeListener(this);
					updateCategoryList();
					mIsRunning = false;
				}
				
				public void onAnimationCancel(Animator animation) {
					onAnimationEnd(animation);
				}
			});
			mEndLoadAniSet.start();
		}
	}
	
	public void firstTimeUpdate(){
		new FirstTimeUpdate().execute();
	}
	
	public class FirstTimeUpdate extends AsyncTask<Void, Void, Boolean> {
		
		private String mJson = "";
		
		@Override
		protected void onPreExecute() {
			mBeginLoadAniSet.start();
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			mJson = NetworkManger.get(APILinkMaker.mGetCityList(), true);
			try{
				JSONArray cityList = new JSONArray(mJson);
				for (int i = 0; i < cityList.length(); i++) {
					JSONObject city = cityList.getJSONObject(i);

					HashMap<String, String> token =  new  HashMap<String, String>();
					token.put("cityID", Integer.toString(city.getInt("id")));
					token.put("name", city.getString("name"));
					token.put("googlename", city.getString("google_name"));
					GlobalVariable.smartGuideDB.insertCity(token);
				}

				GlobalVariable.getCityList();

				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("city", GlobalVariable.mCityID));
				pairs.add(new BasicNameValuePair("env", Integer.toString(GlobalVariable.mMode)));

				String json = NetworkManger.post(APILinkMaker.mGroupByCity(), pairs);

				JSONObject object = new JSONObject(json);
				GlobalVariable.mCateogries = Category.getListCategory(object.getJSONArray("content"));

			} catch(Exception e) {
				e.printStackTrace();
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean k) {
			mEndLoadAniSet.addListener(new AnimatorListener() {

				public void onAnimationStart(Animator animation) {}
				public void onAnimationRepeat(Animator animation) {}
				public void onAnimationEnd(Animator animation) {
					mIsRunning = false;
					mEndLoadAniSet.removeListener(this);
				}
				public void onAnimationCancel(Animator animation) {
					onAnimationEnd(animation);
				}
			});
			mListener.onFinishFirstTimeUpdate();
			updateCategoryList();
			mEndLoadAniSet.start();
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Adapter
	///////////////////////////////////////////////////////////////////////////

	public class ImageAdapter extends BaseAdapter
	{
		private LayoutInflater li;
		private final int[] ICON_ID = new int[] { R.drawable.icon1, R.drawable.icon12, 
				R.drawable.icon13, R.drawable.icon14, R.drawable.icon15,R.drawable.icon16, 
				R.drawable.icon17, R.drawable.icon18, R.drawable.icon19 };
		private final String[] LABEL = new String[] { "TẤT CẢ", "ẨM THỰC", "CAFE & BAR", "LÀM ĐẸP",
				"GIẢI TRÍ", "THỜI TRANG", "DU LỊCH", "SẢN PHẨM", "GIÁO DỤC" };
		
		public ImageAdapter() {
			li = getActivity().getLayoutInflater();
		}

		@Override
		public int getCount() {
			return 9;
		}

		public int getTotalShop() {
			int sum = 0;			
			for (Category cate : GlobalVariable.mCateogries)
				sum += cate.mNum;
			return sum;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if (convertView == null)
				convertView = li.inflate(R.layout.category_item, null);

			Category category = null;
			if (position != 0 && GlobalVariable.mCateogries != null) 
				category = GlobalVariable.mCateogries.get(position - 1);

			View MyView = convertView;

			TextView tv = (TextView) MyView.findViewById(R.id.grid_item_text);
			tv.setTypeface(Typeface.DEFAULT_BOLD);
			ImageView iv = (ImageView) MyView.findViewById(R.id.grid_item_image);
			TextView num = (TextView) MyView.findViewById(R.id.amountShop);
			ImageView pic = (ImageView) MyView.findViewById(R.id.amountPic);

			int numShop = 0;

			if (GlobalVariable.mCateogries != null) {
				if (category != null)
					// Single category
					numShop = category.mNum;
				else
					// All categories
					numShop = getTotalShop();
				
				if (numShop == 0) {
					pic.setVisibility(View.INVISIBLE);
					num.setVisibility(View.INVISIBLE);
				} else if (numShop > 99)
					num.setText("99+");
				else num.setText(Integer.toString(numShop));
				
			} else {
				pic.setVisibility(View.INVISIBLE);
				num.setVisibility(View.INVISIBLE);
			}

			iv.setImageResource(ICON_ID[position]);
			tv.setText(LABEL[position]);

			return MyView;
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Listener
	///////////////////////////////////////////////////////////////////////////
	public static class Listener {
		/**
		 * 
		 * @param position
		 * @param json
		 * @return true: first 10 shops read
		 * false: otherwise
		 */
		public void onFinishFirstTimeUpdate() {}
		public boolean onCategoryClick(int position) { return false; }
		public void onStartLoadShopList() {}
		public void onFinishLoadShopList(String json, boolean success, Exception e) {}
		public void onFinishAnimation() {}
	}
}