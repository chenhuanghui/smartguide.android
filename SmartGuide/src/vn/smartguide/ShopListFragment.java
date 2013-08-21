package vn.smartguide;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.Animator.AnimatorListener;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.method.CharacterPickerDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
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



import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ChauSang on 6/24/13.
 */
public class ShopListFragment extends Fragment {

	private MainAcitivyListener mMainAcitivyListener = null;
	private GridView gridView = null;
	public List<Shop> mShopList = null;
	private ShopListAdapter mAdapter = null;
	
	String mJson = "";
	
	private ImageView mLoadingCircle = null;
	private ImageView mLoadingMiddle = null;
	private ImageView mLoadingBackground = null;
	private RelativeLayout mLoadingOptical = null;
	
	private ObjectAnimator mRotateAnimation = null;
	private ObjectAnimator mFadeOutCircle = null;
	private ObjectAnimator mFadeOutMiddle = null;
	private ObjectAnimator mFadeInCircle = null;
	private ObjectAnimator mFadeInMiddle = null;
	
	public boolean mHaveAnimation = false;
	
	public void updateSGP(int id, int sgp){
		if (mShopList == null || mShopList.size() == 0)
			return;
		
		for(int i = 0; i < mShopList.size(); i++){
			if (mShopList.get(i).mID == id){
				View view = mAdapter.mViewList.get(i);
				LinearLayout mShopScoreNowMin = (LinearLayout)view.findViewById(R.id.shop_score_now_min);
				TextView mShopScoreNowTV = (TextView) mShopScoreNowMin.findViewById(R.id.shop_score_now);
				mShopScoreNowTV.setText(Integer.toString(sgp));
				((PromotionTypeOne)mShopList.get(i).mPromotion).mSGP = sgp;
				return;
			}
		}
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mMainAcitivyListener = (MainAcitivyListener) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);
		View root = inflater.inflate(R.layout.shop_list, container, false);
		return root;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		gridView = (GridView) getView().findViewById(R.id.grid_view_shop_list);
		
		mLoadingCircle = (ImageView) getView().findViewById(R.id.loadingCircleS);
		mLoadingMiddle = (ImageView) getView().findViewById(R.id.loadingMidleS);
		mLoadingBackground = (ImageView) getView().findViewById(R.id.loadingBackgroundS);
		mLoadingOptical = (RelativeLayout) getView().findViewById(R.id.foregroundLoading);
		
		//mForeGround = (RelativeLayout) getView().findViewById(R.id.foreground);
		
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
		
		mShopList = new ArrayList<Shop>();
		mAdapter = new ShopListAdapter(getActivity().getBaseContext(), getActivity());
		gridView.setAdapter(mAdapter);
		gridView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (((firstVisibleItem >= indexPage * GlobalVariable.itemPerPage + GlobalVariable.needLoadMore) ||  visibleItemCount >= GlobalVariable.needLoadMore) && isMore == true){
					isMore = false;
					new FetchMoreShopListTask().execute();
				}
			}
		});
		
		mFadeOutCircle = ObjectAnimator.ofFloat(mLoadingCircle, "alpha", 1.0f, 0.0f);
		mFadeOutCircle.setDuration(1000);
		mFadeOutCircle.setInterpolator(new AccelerateDecelerateInterpolator());
		
		mFadeOutMiddle = ObjectAnimator.ofFloat(mLoadingMiddle, "alpha", 1.0f, 0.0f);
		mFadeOutMiddle.setDuration(1000);
		mFadeOutMiddle.setInterpolator(new AccelerateDecelerateInterpolator());
		mFadeOutCircle.addListener(new AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {}
			
			@Override
			public void onAnimationRepeat(Animator animation) {}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				mRotateAnimation.cancel();
				mHaveAnimation = false;
				updateShopList();
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {}
		});
		
	}

	public void update(String json){
		mJson = json;
		new UpdateTask().execute();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

	}

	public int indexPage = 0;
	public boolean isMore = false;
	
	public void setForeground(){
		mLoadingOptical.setVisibility(View.VISIBLE);
	}
	
	public class UpdateTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				if (mJson == "")
					return false;
				mShopList = Shop.getListForUse(new JSONArray(mJson));
			} catch (JSONException e) {
				mShopList = null;
				return false;
			}

			return true;
		}

		@Override
		protected void onPostExecute(Boolean k){
			if (k == true){
				getActivity().runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (mHaveAnimation){
							//mForeGround.setBackgroundColor(Color.parseColor("#00000000"));
							List<ObjectAnimator> arrayListObjectAnimators = new ArrayList<ObjectAnimator>();
							arrayListObjectAnimators.add(mFadeOutCircle);
							arrayListObjectAnimators.add(mFadeOutMiddle);
							
							ObjectAnimator[] objectAnimators = arrayListObjectAnimators.toArray(new ObjectAnimator[arrayListObjectAnimators.size()]);
							AnimatorSet animSetXY = new AnimatorSet();
							animSetXY.playTogether(objectAnimators);
							animSetXY.addListener(new AnimatorListener() {
								
								@Override
								public void onAnimationStart(Animator animation) {
									// TODO Auto-generated method stub
									
								}
								
								@Override
								public void onAnimationRepeat(Animator animation) {
									// TODO Auto-generated method stub
									
								}
								
								@Override
								public void onAnimationEnd(Animator animation) {
									// TODO Auto-generated method stub
									mLoadingOptical.setVisibility(View.INVISIBLE);
									mLoadingBackground.setVisibility(View.INVISIBLE);
								}
								
								@Override
								public void onAnimationCancel(Animator animation) {
									// TODO Auto-generated method stub
									
								}
							});
							animSetXY.start();
						}else{
							updateShopList();
						}
					}
				});
			}
		}

		@Override
		protected void onPreExecute(){
			if (mHaveAnimation){
				getActivity().runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						//mForeGround.setBackgroundColor(Color.parseColor("#282e3a"));
						mLoadingCircle.setVisibility(View.VISIBLE);
						mLoadingMiddle.setVisibility(View.VISIBLE);
						mLoadingBackground.setVisibility(View.VISIBLE);
						
						List<ObjectAnimator> arrayListObjectAnimators = new ArrayList<ObjectAnimator>();
						
						arrayListObjectAnimators.add(mFadeInMiddle);
						arrayListObjectAnimators.add(mFadeInCircle);
						arrayListObjectAnimators.add(mRotateAnimation);
						
						ObjectAnimator[] objectAnimators = arrayListObjectAnimators.toArray(new ObjectAnimator[arrayListObjectAnimators.size()]);
						AnimatorSet animSetXY = new AnimatorSet();
						animSetXY.playTogether(objectAnimators);
						animSetXY.start();
					}
				});
			}
		}
	}
	
	public void updateShopList(){
		if (mShopList != null){
			if (gridView != null)
				gridView.getAdapter();
			
			mAdapter = new ShopListAdapter(getActivity().getBaseContext(), getActivity());
			gridView.setAdapter(mAdapter);
			mAdapter.notifyDataSetChanged();
		}
	}
	
	public class FetchMoreShopListTask extends AsyncTask<Void, Void, Boolean> {
		private String json = null;

		@Override
		protected Boolean doInBackground(Void... params) {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("group_list", GlobalVariable.mFilterString));
			pairs.add(new BasicNameValuePair("city_id", GlobalVariable.mCityID));
			pairs.add(new BasicNameValuePair("user_id", GlobalVariable.userID));
			pairs.add(new BasicNameValuePair("user_lat", Float.toString(GlobalVariable.mLat)));
			pairs.add(new BasicNameValuePair("user_lng", Float.toString(GlobalVariable.mLng)));
			pairs.add(new BasicNameValuePair("page", Integer.toString(indexPage + 1)));
			pairs.add(new BasicNameValuePair("sort_by", GlobalVariable.mSortByString));
			try {
				json = NetworkManger.post(APILinkMaker.ShopListInCategory(), pairs);
			} catch (Exception e) {
				return false;
			}

			return true;
		}

		@Override
		protected void onPostExecute(Boolean k){

			if (k == true){
				try {
					List<Shop> shopList = Shop.getListForUse(new JSONArray(json));
					if (shopList.size() != 0){

						if (shopList == null || shopList.size() == 0)
							isMore = false;
						else{
							for(int i = 0; i < shopList.size(); i++){
								mShopList.add(shopList.get(i));
								mAdapter.mViewList.add(null);
							}
							
							if (shopList.size() % GlobalVariable.itemPerPage == 0)
								isMore = true;
							else
								isMore = false;
	
							indexPage++;
							
							mAdapter.notifyDataSetChanged();
						}
						
						GlobalVariable.imageLoader.resume();	
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		@Override
		protected void onPreExecute(){
		}
	}
	
	public class ShopListAdapter extends BaseAdapter
	{
		Context MyContext;
		Activity mActivity;
		List<View> mViewList;

		public ShopListAdapter(Context _MyContext, Activity activity)
		{
			MyContext = _MyContext;
			mActivity = activity;
			mViewList = new ArrayList<View>(mShopList.size());
			for(int i = 0; i < mShopList.size(); i++){
				mViewList.add(null);
			}

			if (mShopList.size() == 0)
				isMore = false;
			else if (mShopList.size() % 10 == 0)
				isMore = true;
			
			indexPage = 0;
		}

		@Override
		public int getCount()
		{
			return mShopList.size();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			final int index = position;
			
			if (mViewList.get(position) != null)
				return mViewList.get(position);

			View MyView = convertView;

			try{
				final Shop mShop = mShopList.get(position);
				LayoutInflater li = mActivity.getLayoutInflater();
				if (mShop.mPromotionStatus == true){
					switch(mShop.mPromotion.getType()){
					case 1:
						MyView = li.inflate(R.layout.shop_list_item, null);
						break;
					case 2:
						MyView = li.inflate(R.layout.shop_list_item_type_2, null);
						break;
					}
				}

				final TextView mDistantTV = (TextView)MyView.findViewById(R.id.shop_distance);
				if (mShop.mDistance == - 1)
					mDistantTV.setText("... KM");
				else
					mDistantTV.setText(Float.toString(mShop.mDistance) + " KM");
				//
				final LinearLayout mShopScoreNowMin = (LinearLayout)MyView.findViewById(R.id.shop_score_now_min);
				TextView mShopScoreNowTV = (TextView) mShopScoreNowMin.findViewById(R.id.shop_score_now);
				TextView mShopScoreMinTV = (TextView) mShopScoreNowMin.findViewById(R.id.shop_score_min);

				try{
					if (mShop.mPromotionStatus == true){
						switch(mShop.mPromotion.getType()){
						case 0:
							break;
						case 1:
							PromotionTypeOne promotionTypeOne = (PromotionTypeOne)mShop.mPromotion;
							mShopScoreNowTV.setText(Integer.toString(promotionTypeOne.mSGP));
							break;
						case 2:
							PromotionTypeTwo promotionTypeTwo = (PromotionTypeTwo)mShop.mPromotion;
							mShopScoreMinTV.setText(Integer.toString(promotionTypeTwo.mMoney / 1000) + " K");
							break;
						}
					}}catch(Exception ex){
						ex.getMessage();
					}

				final LinearLayout mShopTypeScore = (LinearLayout)MyView.findViewById(R.id.shop_type_score);
				TextView mShopTotalScore = (TextView)mShopTypeScore.findViewById(R.id.shop_score);
				ImageView mShopType = (ImageView)mShopTypeScore.findViewById(R.id.shop_type_image);

				final ImageView mShopCover = (ImageView)MyView.findViewById(R.id.shop_cover);
				GlobalVariable.imageLoader.displayImage(mShop.mLogo, mShopCover);
//				new Handler().postDelayed(new Runnable() {
//					
//					@Override
//					public void run() {
//						new HttpConnection(new Handler() {
//			        		@Override
//			        		public void handleMessage(Message message) {
//			        			
//			        			switch (message.what) {
//			        			case HttpConnection.DID_START: {
//			        				break;
//			        			}
//			        			case HttpConnection.DID_SUCCEED: {
//			        				Bitmap response = (Bitmap) message.obj;
//			        				mShopCover.setBackgroundDrawable((new BitmapDrawable(getActivity().getResources(), response)));
//			        				break;
//			        			}
//			        			case HttpConnection.DID_ERROR: {
//			        				Exception e = (Exception) message.obj;
//			        				e.printStackTrace();
//			        				break;
//			        			}
//			        			}
//			        		}
//			        		
//			        	}).bitmap(mShop.mLogo);
//					}
//				}, 2000);
				
				final LinearLayout mShopNameContent = (LinearLayout)MyView.findViewById(R.id.shop_name_content);
				TextView mShopName= (TextView) mShopNameContent.findViewById(R.id.shop_name_real);
				mShopName.setText(mShop.mName);

				TextView mShopContent = (TextView) mShopNameContent.findViewById(R.id.shop_content_real);
				mShopContent.setText(mShop.mAddress);
				final LinearLayout shop_cover_layout_tran = (LinearLayout)MyView.findViewById(R.id.shop_cover_layout_tran);


				RelativeLayout touch_layout = (RelativeLayout)MyView.findViewById(R.id.root_layout_item_list);
				touch_layout.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						mDistantTV.setBackgroundResource(R.drawable.shop_distance_red);
						mShopScoreNowMin.setBackgroundResource(R.drawable.shop_score_red);
						mShopTypeScore.setBackgroundResource(R.drawable.shop_type_red);
						shop_cover_layout_tran.setBackgroundResource(R.drawable.shop_avatar_red_tran);
						mShopNameContent.setBackgroundResource(R.drawable.shop_content_red);
						
						Shop s = mShopList.get(index);
						GlobalVariable.mCurrentShop = s;
						mMainAcitivyListener.getDetailFragment().setData(GlobalVariable.mCurrentShop );
						
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								mMainAcitivyListener.goNextPage();
							}
						}, 500);
					}
				});
				
				setImageType((ImageView)MyView.findViewById(R.id.shop_type_icon), mShop.mGroupShop);
				mViewList.set(position, MyView);
			}catch(Exception ex){
				ex.getMessage();
			}
			return MyView;
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}
	}
	
	public void setImageType(ImageView image, int type){
		switch(type){
		case 1:
			image.setBackgroundResource(R.drawable.iconpin_food);
			break;
		case 2:
			image.setBackgroundResource(R.drawable.iconpin_drink);
			break;
		case 3:
			image.setBackgroundResource(R.drawable.iconpin_healness);
			break;
		case 4:
			image.setBackgroundResource(R.drawable.iconpin_entertaiment);
			break;
		case 5:
			image.setBackgroundResource(R.drawable.iconpin_fashion);
			break;
		case 6:
			image.setBackgroundResource(R.drawable.iconpin_travel);
			break;
		case 7:
			image.setBackgroundResource(R.drawable.iconpin_shopping);
			break;
		case 8:
			image.setBackgroundResource(R.drawable.iconpin_education);
			break;
			
		}
	}
}