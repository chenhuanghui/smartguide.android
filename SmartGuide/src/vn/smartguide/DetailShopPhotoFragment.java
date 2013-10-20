package vn.smartguide;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import vn.smartguide.CyImageLoader.Listener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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

//	public static DetailShopPhotoFragment thiz;
	private static final int MEDIA_TYPE_IMAGE = 1;
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 4;
	
	// Data
	private Shop mShop;
	private Uri fileUri;

	// GUI elements
	public PhotoListAdapter		mUserAdapter;
	public HorizontalListView	mUserList;

	public PhotoListAdapter		mShopAdapter;
	public HorizontalListView	mShopList;
	 
	///////////////////////////////////////////////////////////////////////////
	// Override methods
	///////////////////////////////////////////////////////////////////////////

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

//		thiz = this;
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
		
		((Button) getView().findViewById(R.id.btnTakePhoto))
		.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				getActivity().startActivity(new Intent(getActivity(), TakePictureActivity.class));
			}
		});
	}
	
	@Override
	public void onDestroy() {
		
		super.onDestroy();
//		thiz = null;
	}
	
	@Override
		public void onActivityResult(int requestCode, int resultCode, Intent data) {
			
			if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
				if (resultCode == Activity.RESULT_OK) {
//					// Load image from file
//					String imagePath = fileUri.getPath();
//					
//					// calculate optimize scales
//					int scale = 1;
//					Options opt = new Options();
//					opt.inJustDecodeBounds = true;
//					Bitmap bitmap = BitmapFactory.decodeFile(imagePath, opt);
//					
//					while (opt.outWidth * opt.outHeight > PHOTO_SIZE_LIMIT) {
//						scale = scale << 1;
//						opt.outWidth = opt.outWidth >> 1;
//						opt.outHeight = opt.outHeight >> 1;
//					}
//					
//					// Decode photo with scale
//					opt = new Options();
//					opt.inSampleSize = scale;
//					bitmap = BitmapFactory.decodeFile(imagePath, opt);
					
					
				}
			} else
				super.onActivityResult(requestCode, resultCode, data);
		}

	///////////////////////////////////////////////////////////////////////////
	// Public methods
	///////////////////////////////////////////////////////////////////////////

	public void setData(Shop s) {

		if (s != null) {
			mShop = s;
			mShopAdapter.setData(s.mShopImageList);
			mUserAdapter.setData(s.mUserImageList);
			mShopAdapter.notifyDataSetChanged();
			mUserAdapter.notifyDataSetChanged();
		} else {
			mShopAdapter.setData(null);
			mUserAdapter.setData(null);
			mShopAdapter.notifyDataSetChanged();
			mUserAdapter.notifyDataSetChanged();
		}
	}
//	
//	public void releaseMemory() {
//		mShopAdapter.setData(new ArrayList<ImageStr>());
//		mUserAdapter.setData(new ArrayList<ImageStr>());
//	}
	
	private void showDialog(boolean isUser) {
		
		if (isUser)
			PhotoActivity.newInstance(getActivity(), mShop.mUserImageList, isUser, mShop.mID);
		else
			PhotoActivity.newInstance(getActivity(), mShop.mShopImageList, isUser, mShop.mID);
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Adapter
	///////////////////////////////////////////////////////////////////////////
	
	public class PhotoListAdapter extends BaseAdapter {

		public LayoutInflater 	inflater 	= getActivity().getLayoutInflater();
		public List<ImageStr> 	mItemList 	= new ArrayList<ImageStr>();
		public boolean 			mEndList 	= false;
		public boolean 			mLoadingMore= false;
		public int				mPageLoaded = 1;

		public void setData(List<ImageStr> imageList) {

			if (imageList != null) {
				mItemList.clear();
				mItemList.addAll(imageList);
				for (ImageStr item : mItemList) {
					item.loadFail = false;
				}
				notifyDataSetChanged();
			} else {
				mItemList.clear();
				notifyDataSetChanged();
			}
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
			final ImageView img = (ImageView) convertView.findViewById(R.id.image);

			// Load image
			final ImageStr imageItem = mItemList.get(position);
			img.setTag(imageItem.url);
			
			if (imageItem.loadFail) {
				img.setImageResource(R.drawable.ava_loading);
			} else {
				GlobalVariable.cyImageLoader.loadImage(imageItem.url, new Listener() {

					@Override
					public void loadFinish(int from, Bitmap image, String url) {
						switch (from) {
						case CyImageLoader.FROM_NETWORK:
						case CyImageLoader.FROM_DISK:
							if (((String) img.getTag()).equals(url))
								img.setImageBitmap(image);
							break;

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
				}, new Point(144, 144), getActivity());
			}
			
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
	
	public void addNewPhoto(String url, String description){
		mShop.mUserImageList.add(0, new ImageStr(url, description));
		mUserAdapter.setData(mShop.mUserImageList);
		mUserAdapter.notifyDataSetChanged();
	}
}