package vn.smartguide;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import vn.smartguide.CyImageLoader.Listener;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;

@SuppressLint("ValidFragment")
public class DetailShopPhotoFragment extends Fragment {

	public static DetailShopPhotoFragment thiz;
	public Shop mShop;

	public PhotoListAdapter		mUserAdapter;
	public HorizontalListView	mUserList;

	public PhotoListAdapter		mShopAdapter;
	public HorizontalListView	mShopList;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		thiz = this;
		return inflater.inflate(R.layout.detail_shopphoto, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);

		// User image
		mUserList = (HorizontalListView) getView().findViewById(R.id.pagerShopPhoto2);
		mUserAdapter = new PhotoListAdapter();
		mUserList.setAdapter(mUserAdapter);

		// Shop image
		mShopList = (HorizontalListView) getView().findViewById(R.id.pagerShopPhoto1);
		mShopAdapter = new PhotoListAdapter();
		mShopAdapter.mEndList = true;	// Don't load more shop image
		mShopList.setAdapter(mShopAdapter);

		((Button) getView().findViewById(R.id.btnPagerPhoto1))
		.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				showDialog(false);
			}
		});

		((Button) getView().findViewById(R.id.btnPagerPhoto2))
		.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				showDialog(true);
			}
		});
	}
	
	@Override
	public void onDestroy() {
		
		super.onDestroy();
		thiz = null;
	}

	private void showDialog(boolean isUser) {
		
		if (isUser)
			PhotoActivity.newInstance(getActivity(), mShop.mUserImageList, isUser, mShop.mID);
		else
			PhotoActivity.newInstance(getActivity(), mShop.mShopImageList, isUser, mShop.mID);
	}

	public void setData(Shop s) {

		mShop = s;
		mShopAdapter.setData(s.mShopImageList);
		mUserAdapter.setData(s.mUserImageList);
	}

	public class PhotoListAdapter extends BaseAdapter {

		public LayoutInflater 	inflater 	= getActivity().getLayoutInflater();
		public List<ImageStr> 	mItemList 	= new ArrayList<ImageStr>();
		public boolean 			mEndList 	= false;
		public boolean 			mLoadingMore= false;
		public int				mPageLoaded = 1;

		public void setData(List<ImageStr> imageList) {

			mItemList.clear();
			mItemList.addAll(imageList);
			for (ImageStr item : mItemList) {
				item.loadFail = false;
			}
			notifyDataSetChanged();
		}

//		public void loadMore(boolean isUser) {
//
//			if (mEndList)
//				return;
//
//			if (!mLoadingMore) {
//				mLoadingMore = true;
//				new GetImage(mPageLoaded + 1, isUser).execute();
//			}
//		} 

		@Override
		public int getCount() {
			return mItemList.size();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				convertView = inflater.inflate(R.layout.photo_item, null);
			}

			// Set tag
			convertView.setTag(position);

			// Get imageView
			ImageView img = (ImageView) convertView.findViewById(R.id.image);

			// Load image
			ImageStr imageItem = mItemList.get(position);
			
			if (imageItem.loadFail) {
				img.setImageResource(R.drawable.ava_loading);
			} else {
//				Bitmap bm = imageItem.bm.get();
//				if (bm != null) {
//					img.setImageBitmap(bm);
//				} else {
					GlobalVariable.cyImageLoader.loadImage(imageItem.url, new Listener() {

						private ImageStr imageItem;
						private ImageView img;

						public Listener init (ImageStr imageItem, ImageView img) {
							this.imageItem = imageItem;
							this.img = img;
							return this;
						}

						@Override
						public void loadFinish(int from, Bitmap image) {
							
//							imageItem.bm = new WeakReference<Bitmap>(image);
							
							switch (from) {
							case CyImageLoader.FROM_NETWORK:
								Log.d("CycrixDebug", "" + image.getWidth() + "x" + image.getHeight());
								notifyDataSetChanged();
								break;
								
							case CyImageLoader.FROM_DISK:
							case CyImageLoader.FROM_MEMORY:
								img.setImageBitmap(image);
								break;
							}
						}
						
						@Override
						public void loadFail(Exception e) {
							
							imageItem.loadFail = true;
							notifyDataSetChanged();
						}
					}.init(imageItem, img), new Point(128, 128), getActivity());
				}
//			}
			
			return convertView;
		}

		@Override
		public Object getItem(int pos) {
			return pos;
		}

		@Override
		public long getItemId(int pos) {
			return pos;
		}

//		private class GetImage extends AsyncTask<Void, Void, Boolean> {
//
//			private int mPage;
//			private boolean mIsUser;
//
//			public GetImage(int page, boolean isUser) {
//				
//				mPage = page;
//				mIsUser = isUser;
//			}
//
//			@Override
//			protected Boolean doInBackground(Void... params) {
//
//				try {
//					List<NameValuePair> pairs = new ArrayList<NameValuePair>();
//					pairs.add(new BasicNameValuePair("shop_id", Integer.toString(mShop.mID)));
//					pairs.add(new BasicNameValuePair("page", Integer.toString(mPage)));
//
//					String json = null;
//					if (mIsUser)
//						NetworkManger.post(APILinkMaker.mGetUserImage(), pairs);
//					else
//						NetworkManger.post(APILinkMaker.mGetShopImage(), pairs);
//
//					JSONArray jImgArr = new JSONArray(json);
//					int beforeSize = mItemList.size();
//
//					parseJsonImage(jImgArr, mItemList);
//
//					if (beforeSize == mItemList.size())
//						mEndList = true;
//					else
//						mPageLoaded++;
//
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}
//
//				return true;
//			}
//
//			protected void onPostExecute(Boolean k) {
//				
//				mLoadingMore = false;
//			}
//			
//			protected void onPreExecute() { }
//		}
	}
	
	public static List<ImageStr> parseJsonImage(JSONArray jImgArr, List<ImageStr> imageList) throws JSONException {
    	
    	if (imageList == null)
    		imageList = new ArrayList<ImageStr>();
    	
    	for (int i = 0; i < jImgArr.length(); i++) {
    		JSONObject jImage =  jImgArr.getJSONObject(i);
    		imageList.add(new ImageStr(
    				jImage.getString("image"),
    				jImage.getString("description")));
    	}
    	
    	return imageList;
    }
    
}