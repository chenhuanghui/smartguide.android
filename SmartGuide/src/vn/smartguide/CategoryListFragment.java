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

	private Activity mActivity;
	private MainAcitivyListener mMainAcitivyListener;

	private ImageView mLoadingCircle;
	private ImageView mLoadingMiddle;
	private ImageView mLoadingBackground;
	private RelativeLayout mLoadingOptical;

	private ObjectAnimator mRotateAnimation;
	private ObjectAnimator mFadeOutCircle;
	private ObjectAnimator mFadeOutMiddle;
	private ObjectAnimator mFadeInCircle;
	private ObjectAnimator mFadeInMiddle;

	private boolean mIsCanGoNextPage = false;

	GridView gridView = null; 

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = activity;
		mMainAcitivyListener = (MainAcitivyListener) mActivity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);
		View root = inflater.inflate(R.layout.category_list, container, false);
		return root;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		gridView = (GridView) getView().findViewById(R.id.grid_view);
		mLoadingCircle = (ImageView) getView().findViewById(R.id.loadingCircle);
		mLoadingMiddle = (ImageView) getView().findViewById(R.id.loadingMidle);
		mLoadingBackground = (ImageView) getView().findViewById(R.id.loadingBackground);
		mLoadingOptical = (RelativeLayout) getView().findViewById(R.id.bgloading);

		mFadeOutCircle = ObjectAnimator.ofFloat(mLoadingCircle, "alpha", 1.0f, 0.0f);
		mFadeOutCircle.setDuration(1000);
		mFadeOutCircle.setInterpolator(new LinearInterpolator());

		mFadeOutMiddle = ObjectAnimator.ofFloat(mLoadingMiddle, "alpha", 1.0f, 0.0f);
		mFadeOutMiddle.setDuration(1000);
		mFadeOutMiddle.setInterpolator(new LinearInterpolator());
		mFadeOutCircle.addListener(new AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {}

			@Override
			public void onAnimationRepeat(Animator animation) {}

			@Override
			public void onAnimationEnd(Animator animation) {
				mRotateAnimation.cancel();
				if (mIsCanGoNextPage){
					(mMainAcitivyListener).goNextPage();
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							(mMainAcitivyListener).startAds();
							//GlobalVariable.imageLoader.resume();
						}
					}, GlobalVariable.timeToResumeImageDownloader);
				}
			}

			@Override
			public void onAnimationCancel(Animator animation) {}
		});

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
	}

	void updateCategoryList() {
		gridView.setAdapter(new ImageAdapter(mActivity.getBaseContext(), mActivity));
		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				if (position == 0){
					GlobalVariable.mFilterString = "1,2,3,4,5,6,7,8";
					if (GlobalVariable.json10FirstShop.length() != 0 && GlobalVariable.mSortByString.compareTo("0") == 0){
						(mMainAcitivyListener).getShopListFragment().update(GlobalVariable.json10FirstShop);
						
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								(mMainAcitivyListener).goNextPage();
							}
						}, 500);
						return;
					}
				}
				else
					GlobalVariable.mFilterString = Integer.toString(position);

				new FindShopList().execute();
			}
		});
		((ImageAdapter) gridView.getAdapter()).notifyDataSetChanged();
		mMainAcitivyListener.goToPage(0);
	}

	public boolean mIsRunning = false;
	public void autoUpdate(){
		if (mIsRunning == true)
			return;
		mIsRunning = true;
		new UpdateNumber().execute();
	}

	public class FindShopList extends AsyncTask<Void, Void, Boolean> {

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

			String json = NetworkManger.post(APILinkMaker.ShopListInCategory(), pairs);
			mIsCanGoNextPage = true;
			(mMainAcitivyListener).getShopListFragment().update(json);
			return true;
		}

		@Override
		protected void onPostExecute(Boolean k){
			List<ObjectAnimator> arrayListObjectAnimators = new ArrayList<ObjectAnimator>();

			arrayListObjectAnimators.add(mFadeOutCircle);
			arrayListObjectAnimators.add(mFadeOutMiddle);

			ObjectAnimator[] objectAnimators = arrayListObjectAnimators.toArray(new ObjectAnimator[arrayListObjectAnimators.size()]);
			AnimatorSet animSetXY = new AnimatorSet();
			animSetXY.playTogether(objectAnimators);
			animSetXY.addListener(new AnimatorListener() {
				@Override
				public void onAnimationStart(Animator animation) {}

				@Override
				public void onAnimationRepeat(Animator animation) {}

				@Override
				public void onAnimationEnd(Animator animation) {
					mLoadingBackground.setVisibility(View.INVISIBLE);
					mLoadingOptical.setVisibility(View.INVISIBLE);
				}

				@Override
				public void onAnimationCancel(Animator animation) {}
			});

			animSetXY.start();
		}

		@Override
		protected void onPreExecute(){
			mIsCanGoNextPage = false;
			(mMainAcitivyListener).stopAds();

			mLoadingCircle.setVisibility(View.VISIBLE);
			mLoadingMiddle.setVisibility(View.VISIBLE);
			mLoadingBackground.setVisibility(View.VISIBLE);
			mLoadingOptical.setVisibility(View.VISIBLE);

			List<ObjectAnimator> arrayListObjectAnimators = new ArrayList<ObjectAnimator>();

			arrayListObjectAnimators.add(mFadeInMiddle);
			arrayListObjectAnimators.add(mFadeInCircle);
			arrayListObjectAnimators.add(mRotateAnimation);

			ObjectAnimator[] objectAnimators = arrayListObjectAnimators.toArray(new ObjectAnimator[arrayListObjectAnimators.size()]);
			AnimatorSet animSetXY = new AnimatorSet();
			animSetXY.playTogether(objectAnimators);
			animSetXY.start();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

	}

	List<View> mCategory = null;

	public class UpdateNumber extends AsyncTask<Void, Void, Boolean> {

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
			List<ObjectAnimator> arrayListObjectAnimators = new ArrayList<ObjectAnimator>();

			arrayListObjectAnimators.add(mFadeOutCircle);
			arrayListObjectAnimators.add(mFadeOutMiddle);

			ObjectAnimator[] objectAnimators = arrayListObjectAnimators.toArray(new ObjectAnimator[arrayListObjectAnimators.size()]);
			AnimatorSet animSetXY = new AnimatorSet();
			animSetXY.playTogether(objectAnimators);
			animSetXY.addListener(new AnimatorListener() {

				@Override
				public void onAnimationStart(Animator animation) {}

				@Override
				public void onAnimationRepeat(Animator animation) {}

				@Override
				public void onAnimationEnd(Animator animation) {
					mLoadingBackground.setVisibility(View.INVISIBLE);
					mLoadingOptical.setVisibility(View.INVISIBLE);
					updateCategoryList();
					mIsRunning = false;
				}

				@Override
				public void onAnimationCancel(Animator animation) {}
			});

			animSetXY.start();
		}

		@Override
		protected void onPreExecute(){
			mLoadingCircle.setVisibility(View.VISIBLE);
			mLoadingMiddle.setVisibility(View.VISIBLE);
			mLoadingBackground.setVisibility(View.VISIBLE);
			mLoadingOptical.setVisibility(View.VISIBLE);

			mIsCanGoNextPage = false;

			List<ObjectAnimator> arrayListObjectAnimators = new ArrayList<ObjectAnimator>();

			arrayListObjectAnimators.add(mFadeInMiddle);
			arrayListObjectAnimators.add(mFadeInCircle);
			arrayListObjectAnimators.add(mRotateAnimation);

			ObjectAnimator[] objectAnimators = arrayListObjectAnimators.toArray(new ObjectAnimator[arrayListObjectAnimators.size()]);
			AnimatorSet animSetXY = new AnimatorSet();
			animSetXY.playTogether(objectAnimators);
			animSetXY.start();
		}
	}

	public class ImageAdapter extends BaseAdapter
	{
		Context MyContext;
		Activity mActivity;
		public ImageAdapter(Context _MyContext, Activity activity)
		{
			MyContext = _MyContext;
			mActivity = activity;
			mCategory = new ArrayList<View>();
			for(int i = 0; i < 9; i++)
				mCategory.add(null);
		}

		@Override
		public int getCount()
		{
			return 9;
		}

		public int getTotalShop(){
			int sum = 0;
			for(int i = 0; i < GlobalVariable.mCateogries.size(); i++)
				sum += GlobalVariable.mCateogries.get(i).mNum;
			return sum;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if (mCategory.get(position) != null)
				return mCategory.get(position);

			Category category = null;
			if (position != 0 && GlobalVariable.mCateogries != null) 
				category = GlobalVariable.mCateogries.get(position - 1);

			View MyView = convertView;

			LayoutInflater li = mActivity.getLayoutInflater();
			MyView = li.inflate(R.layout.category_item, null);

			TextView tv = (TextView)MyView.findViewById(R.id.grid_item_text);
			tv.setTypeface(Typeface.DEFAULT_BOLD);

			ImageView iv = (ImageView)MyView.findViewById(R.id.grid_item_image);

			TextView num = (TextView)MyView.findViewById(R.id.amountShop);
			ImageView pic = (ImageView)MyView.findViewById(R.id.amountPic);

			int numShop = 0;

			if (GlobalVariable.mCateogries != null){
				if (category != null){
					numShop = category.mNum;
					if (numShop == 0){
						pic.setVisibility(View.INVISIBLE);
						num.setVisibility(View.INVISIBLE);
					}
					else if (numShop>= 99)
						num.setText("99+");
					else num.setText(Integer.toString(numShop));
				}else{
					numShop = getTotalShop();
					if (numShop == 0)
						pic.setVisibility(View.INVISIBLE);
					else if (numShop>= 99)
						num.setText("99+");
					else num.setText(Integer.toString(numShop));
				}
			}else{
				pic.setVisibility(View.INVISIBLE);
				num.setVisibility(View.INVISIBLE);
			}

			switch (position)
			{
			case 0:
				iv.setImageResource(R.drawable.icon1);
				tv.setText("TẤT CẢ");
				break;
			case 1:
				iv.setImageResource(R.drawable.icon12);
				tv.setText("ẨM THỰC");
				break;
			case 2:
				iv.setImageResource(R.drawable.icon13);
				tv.setText("CAFE & BAR");
				break;
			case 3:
				iv.setImageResource(R.drawable.icon14);
				tv.setText("LÀM ĐẸP");
				break;
			case 4:
				iv.setImageResource(R.drawable.icon15);
				tv.setText("GIẢI TRÍ");
				break;
			case 5:
				iv.setImageResource(R.drawable.icon16);
				tv.setText("THỜI TRANG");
				break;
			case 6:
				iv.setImageResource(R.drawable.icon17);
				tv.setText("DU LỊCH");
				break;
			case 7:
				iv.setImageResource(R.drawable.icon18);
				tv.setText("SẢN PHẨM");
				break;
			case 8:
				iv.setImageResource(R.drawable.icon19);
				tv.setText("GIÁO DỤC");
				break;
			}

			mCategory.set(position, MyView);
			return MyView;
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}
	}

	void firstTimeUpdate(){
		new FirstTimeUpdate().execute();
	}
	public class FirstTimeUpdate extends AsyncTask<Void, Void, Boolean> {
		String mJson = "";
		@Override
		protected Boolean doInBackground(Void... params) {
			mJson = NetworkManger.get(APILinkMaker.mGetCityList(), true);
			try{
				JSONArray cityList = new JSONArray(mJson);
				for(int i = 0; i < cityList.length(); i++){
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

				try {
					JSONObject object = new JSONObject(json);
					GlobalVariable.mCateogries = Category.getListCategory(object.getJSONArray("content"));
					
				} catch (JSONException e) {
					return false;
				}

			}catch(Exception ex){
				ex.getMessage();
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean k){
			List<ObjectAnimator> arrayListObjectAnimators = new ArrayList<ObjectAnimator>();

			arrayListObjectAnimators.add(mFadeOutCircle);
			arrayListObjectAnimators.add(mFadeOutMiddle);

			ObjectAnimator[] objectAnimators = arrayListObjectAnimators.toArray(new ObjectAnimator[arrayListObjectAnimators.size()]);
			AnimatorSet animSetXY = new AnimatorSet();
			animSetXY.playTogether(objectAnimators);
			animSetXY.addListener(new AnimatorListener() {

				@Override
				public void onAnimationStart(Animator animation) {}

				@Override
				public void onAnimationRepeat(Animator animation) {}

				@Override
				public void onAnimationEnd(Animator animation) {
					mMainAcitivyListener.setLocation(GlobalVariable.mCityNames.get(GlobalVariable.mCityIDes.indexOf(GlobalVariable.mCityID)));
					updateCategoryList();
					mLoadingBackground.setVisibility(View.INVISIBLE);
					mLoadingOptical.setVisibility(View.INVISIBLE);
					mIsRunning = false;
				}

				@Override
				public void onAnimationCancel(Animator animation) {}
			});

			animSetXY.start();
		}

		@Override
		protected void onPreExecute(){
			mLoadingCircle.setVisibility(View.VISIBLE);
			mLoadingMiddle.setVisibility(View.VISIBLE);
			mLoadingBackground.setVisibility(View.VISIBLE);
			mLoadingOptical.setVisibility(View.VISIBLE);

			mIsCanGoNextPage = false;

			List<ObjectAnimator> arrayListObjectAnimators = new ArrayList<ObjectAnimator>();

			arrayListObjectAnimators.add(mFadeInMiddle);
			arrayListObjectAnimators.add(mFadeInCircle);
			arrayListObjectAnimators.add(mRotateAnimation);

			ObjectAnimator[] objectAnimators = arrayListObjectAnimators.toArray(new ObjectAnimator[arrayListObjectAnimators.size()]);
			AnimatorSet animSetXY = new AnimatorSet();
			animSetXY.playTogether(objectAnimators);
			animSetXY.start();
		}
	}
}