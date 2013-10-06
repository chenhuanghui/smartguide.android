package vn.smartguide;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

public class UserFragment extends Fragment{
//	private MainAcitivyListener mMainAcitivyListener = null;
	
	// GUI elements
	private ImageView mAvatar;
	private TextView mScoreText;
	private ListView mLstCollection;
	private CollectionAdapter mAdapter;
	private GiftAdapter mGiftAdapter;
	
	// Data
	private Listener mListener = new Listener();
	private boolean mShowContent;
	private int indexPage = 0;
	private boolean isMore = true;
	
	///////////////////////////////////////////////////////////////////////////
	// Override methods
	///////////////////////////////////////////////////////////////////////////
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.user_fragment, container, false); 		
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		((Button) (getView().findViewById(R.id.btnDoiDiemLayQua)))
		.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				((ViewSwitcher) getView().findViewById(R.id.switcherUser)).showNext();
			}
		});
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
//		mMainAcitivyListener = (MainAcitivyListener) getActivity();
		
		mLstCollection = (ListView) getView().findViewById(R.id.lstCollection);
		mAdapter = new CollectionAdapter();
		mAdapter.setData(new ArrayList<Shop>());
		mLstCollection.setAdapter(mAdapter);
		
		ListView lstGift = (ListView) getView().findViewById(R.id.lstGift);
		mGiftAdapter = new GiftAdapter();
		lstGift.setAdapter(mGiftAdapter);
		
		// invisible
		View layoutMain = getView().findViewById(R.id.userLayoutMain);
		layoutMain.setVisibility(View.GONE);
		
		mAvatar = (ImageView) getView().findViewById(R.id.avatarUserView);
		mScoreText = (TextView) getView().findViewById(R.id.txtPoint);
		
		mLstCollection.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (((firstVisibleItem >= indexPage * GlobalVariable.itemPerPage + GlobalVariable.needLoadMore) ||  visibleItemCount >= GlobalVariable.needLoadMore) && isMore == true){
					isMore = false;
					new FetchMoreShopListTask().execute();
				}	
			}
		});
		
		mLstCollection.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				GlobalVariable.mCurrentShop = mAdapter.mShops.get(arg2);
				mListener.onShopClick(GlobalVariable.mCurrentShop);
				
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
	
	public boolean updateSGP(int id, int sgp) {
		if (mAdapter == null)
			return false;
		
		List<Shop> mShopList = mAdapter.mShops;
		if (mShopList == null || mShopList.size() == 0)
			return false;
		
		for(int i = 0; i < mShopList.size(); i++){
			if (mShopList.get(i).mID == id){
				try{
				mAdapter.mSGPTexts.get(i).setText(Integer.toString(sgp));
				}catch(Exception ex){
					return false;
				}
				((PromotionTypeOne)mShopList.get(i).mPromotion).mSGP = sgp;
				return true;
			}
		}
		
		return false;
	}
		
	public void updateScore(String score){
		mScoreText.setText(score);
	}
	
	public void updateAvatar(){
		GlobalVariable.cyImageLoader.showImage(GlobalVariable.avatarFace, mAvatar);
	}
	
	public void toggle() {
		mShowContent = !mShowContent;
//		ObjectAnimator animator = null;
//		int height = getActivity().findViewById(R.id.linearLayout).getHeight();
		View layout = getView().findViewById(R.id.userLayoutMain);
//		layout.setVisibility(View.VISIBLE);
		layout.setVisibility(mShowContent ? View.VISIBLE : View.INVISIBLE);
//		if (mShowContent)
//			animator = ObjectAnimator.ofFloat(layout, "translationY", -height, 0);
//		else
//			animator = ObjectAnimator.ofFloat(layout, "translationY", 0, -height);
//
//		animator.setInterpolator(new AccelerateDecelerateInterpolator());
//		animator.start();
	}
	
	public boolean isShow() {
		return mShowContent;
	}
	
	public void update(List<Shop> shop){
		mAdapter = new CollectionAdapter();
		if (shop == null || shop.size() == 0)
			isMore = false;
		else{
			if (shop.size() % GlobalVariable.itemPerPage == 0)
				isMore = true;
			else
				isMore = false;

			indexPage++;
		}	
		
		mAdapter.setData(shop);
		mLstCollection.setAdapter(mAdapter);
		mAdapter.notifyDataSetChanged();
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Adapter
	///////////////////////////////////////////////////////////////////////////
	
	public class CollectionAdapter extends BaseAdapter {
		
		private LayoutInflater inflater;
		private List<Shop> mShops;
		private List<Drawable> mBitmaps;
		private List<TextView> mSGPTexts;
		
		public CollectionAdapter() {
			inflater = UserFragment.this.getActivity().getLayoutInflater();
		}
		
		public void setData(List<Shop> shops){
			mShops = shops;
			mBitmaps = new ArrayList<Drawable>();
			mSGPTexts = new ArrayList<TextView>();
			for(int i = 0; i < shops.size(); i++) {
				mBitmaps.add(null);
				mSGPTexts.add(null);
			}
		}
		
		public void addData(List<Shop> shops){
			for(int i = 0; i < shops.size(); i++){
				mShops.add(shops.get(i));
				mBitmaps.add(null);
				mSGPTexts.add(null);
			}
		}
		
		@Override
		public int getCount() {
			return mShops.size();
		}

		@Override
		public Object getItem(int pos) {
			return pos;
		}

		@Override
		public long getItemId(int pos) {
			return pos;
		}

		@SuppressWarnings("deprecation")
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.user_item, null);
			}
			
			final Shop sp = mShops.get(position);
			TextView shopTextView = (TextView)convertView.findViewById(R.id.textView1);
			TextView sgpTextView = (TextView)convertView.findViewById(R.id.TextView01);
			TextView spTextView = (TextView)convertView.findViewById(R.id.TextView03);
			TextView timeTextView = (TextView)convertView.findViewById(R.id.textView4);
			//TextView updateTextview = (TextView)convertView.findViewById(R.id.textView5);
			final ImageView shopAva = (ImageView)convertView.findViewById(R.id.imageView1);
			mSGPTexts.set(position, sgpTextView);
			
			shopTextView.setText(sp.mName);
			Drawable nowDrawable = mBitmaps.get(position); 
			if (nowDrawable == null){
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						new HttpConnection(new Handler() {
			        		@SuppressWarnings("deprecation")
							@Override
			        		public void handleMessage(Message message) {
			        			
			        			switch (message.what) {
			        			case HttpConnection.DID_START: {
			        				break;
			        			}
			        			case HttpConnection.DID_SUCCEED: {
			        				Bitmap response = (Bitmap) message.obj;
			        				Drawable drawable = new BitmapDrawable(getActivity().getResources(), response);
			        				mBitmaps.set(position, drawable);
			        				shopAva.setBackgroundDrawable(drawable);
			        				break;
			        			}
			        			case HttpConnection.DID_ERROR: {
			        				Exception e = (Exception) message.obj;
			        				e.printStackTrace();
			        				break;
			        			}
			        			}
			        		}
			        		
			        	}).bitmap(sp.mLogo);
					}
				}, 2000);
			}else
				shopAva.setBackgroundDrawable(nowDrawable);
			
			if (sp.mPromotionStatus == true){
				int type = sp.mPromotion.getType();
				switch(type){
				case 1:
					PromotionTypeOne promotion = (PromotionTypeOne)sp.mPromotion;
					sgpTextView.setText(Integer.toString(promotion.mSGP));
					spTextView.setText(Integer.toString(promotion.mSP));
					//updateTextview.setText(promotion.mDuration);
					break;
				case 2:
					PromotionTypeTwo promotion_2 = (PromotionTypeTwo)sp.mPromotion;
					TextView spTitle = (TextView)convertView.findViewById(R.id.TextView02);
					TextView sgpTitle = (TextView)convertView.findViewById(R.id.textView2);
					
					spTitle.setVisibility(View.INVISIBLE);
					spTextView.setVisibility(View.INVISIBLE);
					sgpTextView.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
					sgpTextView.setText(Integer.toString(promotion_2.mMoney) + "  VNĐ");
					
					sgpTitle.setVisibility(View.INVISIBLE);
					break;
				}	
			}
			
			timeTextView.setText(sp.mUpdateAt);
			return convertView;
		}
	}
	
public static class GiftItem {
		
		public int id;
		public int score;
		public String content;
		public int status;
	}
	
	public class GiftAdapter extends BaseAdapter {
		
		private LayoutInflater inflater;
		private List<GiftItem> mGiftList = new ArrayList<GiftItem>(); 
		
		public GiftAdapter() {
			
			inflater = UserFragment.this.getActivity().getLayoutInflater();
		}
		
		public void setData(List<GiftItem> giftList){
			
			mGiftList = giftList;
			notifyDataSetChanged();
		}
		
		@Override
		public int getCount() {
			return mGiftList.size();
		}

		@Override
		public Object getItem(int pos) {
			return pos;
		}

		@Override
		public long getItemId(int pos) {
			return pos;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.gift_item, null);
				
			}
			
			GiftItem gift = mGiftList.get(position);
			
			TextView txtName = (TextView) convertView.findViewById(R.id.txtGiftName);
			TextView txtPoint = (TextView) convertView.findViewById(R.id.txtPoint);
			
			txtName.setText("■ " + gift.content);
			txtPoint.setText("" + gift.score + " P");
			Button btnDoiQua = (Button) convertView.findViewById(R.id.btnDoiQua);
			btnDoiQua.setTag(position);
			if (gift.status == 0)
				btnDoiQua.setAlpha(1f);
			else
				btnDoiQua.setAlpha(0.5f);
			btnDoiQua.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					int index = (Integer) v.getTag();
					onDoiQuaClick(index);
				}
			});
			
			return convertView;
		}
		
		public void onDoiQuaClick(int pos) {
			
			new GetRewardTask(mGiftList.get(pos).id).execute();
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Netword asynctask
	///////////////////////////////////////////////////////////////////////////
	
	public class FetchMoreShopListTask extends AsyncTask<Void, Void, Boolean> {
		String JSResult = null;
		
		@Override
		protected Boolean doInBackground(Void... params) {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("user_id", GlobalVariable.userID));
			pairs.add(new BasicNameValuePair("user_lat", Float.toString(GlobalVariable.mLat)));
			pairs.add(new BasicNameValuePair("user_lng", Float.toString(GlobalVariable.mLng)));
			pairs.add(new BasicNameValuePair("page", "0"));
			
			JSResult = NetworkManger.post(APILinkMaker.mGetUserCollection(), pairs);

			return true;
		}

		@Override
		protected void onPostExecute(Boolean k){
			JSONObject obj;
			try {
				obj = new JSONObject(JSResult);
				
				List<Shop> shopList = Shop.getListForUse(obj.getJSONArray("collection"));
				if (shopList.size() != 0){

					if (shopList == null || shopList.size() == 0)
						isMore = false;
					else{
						mAdapter.addData(shopList);
						
						if (shopList.size() % GlobalVariable.itemPerPage == 0)
							isMore = true;
						else
							isMore = false;

						indexPage++;
						
						mAdapter.notifyDataSetChanged();
					}	
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void onPreExecute(){
		}
	}
	
	public void updateRewardList(List<GiftItem> giftList) {
		
		mGiftAdapter.setData(giftList);
	}
	
			
	public class GetRewardTask extends AsyncTask<Void, Void, Boolean> {
		
		private String result;
		private int rewardId;
		
		public GetRewardTask(int id) {
			
			rewardId = id;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("reward_id", "" + rewardId));
			pairs.add(new BasicNameValuePair("user_id", GlobalVariable.userID));
			
			result = NetworkManger.post(APILinkMaker.mGetReward(), pairs);

			return true;
		}

		@Override
		protected void onPostExecute(Boolean k){
			JSONObject obj;
			try {
				obj = new JSONObject(result);
				
				int sta = obj.getInt("status");
				switch (sta) {
				
				case 2: {
					// Show success dialog 
					int total_score = obj.getInt("total_score");
					updateScore(Integer.toString(total_score));
//					mMainAcitivyListener.updateTotalSGP(Integer.toString(total_score));
					
					Builder builder = new Builder(UserFragment.this.getActivity());
					builder.setMessage("Chúc mừng bạn đã nhận được\n"
							+ obj.getString("reward") 
							+ "\nChúng tôi sẽ liên lạc với bạn trong thời gian sớm nhất");
					builder.setCancelable(true);
					builder.setPositiveButton("OK", null);

					AlertDialog dialog = builder.show();
					TextView messageView = (TextView)dialog.findViewById(android.R.id.message);
					messageView.setGravity(Gravity.CENTER);
					break;
				}
				
				default: {
					// Show fail dialog 
					Builder builder = new Builder(UserFragment.this.getActivity());
					builder.setMessage(obj.getString("content"));
					builder.setCancelable(true);
					builder.setPositiveButton("OK", null);
					
					AlertDialog dialog = builder.show();
					TextView messageView = (TextView)dialog.findViewById(android.R.id.message);
					messageView.setGravity(Gravity.CENTER);
					break;
				}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void onPreExecute() { }
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Listener
	///////////////////////////////////////////////////////////////////////////
	
	public static class Listener {
		public void onShopClick(Shop s) { }
	}
}