package vn.smartguide;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
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

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

@SuppressLint("ValidFragment")
public class DetailShopPhotoFragment extends Fragment {
	
	public static DetailShopPhotoFragment thiz;
	public PhotoActivity mPhotoDlg;
	private Shop mShop;
	
	public List<ImageStr> 		mUserURLList = new ArrayList<ImageStr>();
	public int 					mUserPageLoaded;
	public boolean 	 			mUserURLListEnd;
	public PhotoListAdapter		mUserAdapter;
	public HorizontalListView	mUserList;
	public boolean 				mLoadingMore;
	
	public List<ImageStr>		mShopURLList = new ArrayList<ImageStr>();
//	private int 				mShopPageLoaded;
//	private boolean 	 		mShopURLListEnd;
	private HorizontalListView	mShopList;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

		thiz = this;
        return inflater.inflate(R.layout.detail_shopphoto, container, false);
    }
	
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        
        // Instance of ImageAdapter Class
        mUserList = (HorizontalListView) getView().findViewById(R.id.pagerShopPhoto2);
        mUserAdapter = new PhotoListAdapter(mUserURLList, true);
        mUserList.setAdapter(mUserAdapter);
        mShopList = (HorizontalListView) getView().findViewById(R.id.pagerShopPhoto1);
        
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
    
    private void showDialog(boolean isUser) {
    	
    	Intent i = new Intent(getActivity(), PhotoActivity.class);
    	i.putExtra("isUser", isUser);
    	getActivity().startActivityForResult(i, 0x0503);
    }
    
    public void setData(Shop s) {
    	mShop = s;
    	
    	mUserURLList.clear();
    	mUserURLList.addAll(s.mUserImageList);
    	mUserPageLoaded = 1;
    	mUserURLListEnd = false;
    	
    	mShopURLList = mShop.mShopImageList;
    	
    	refreshPhoto(false);
    	refreshPhoto(true);
    	
//    	loadMoreUser();
//    	new GetShopImage().execute();
    }
    
    public void loadMoreUser(PhotoActivity photoActivity) {
    	
    	mPhotoDlg = photoActivity;
    	if (mUserURLListEnd)
    		return;
    	
    	
    	if (!mLoadingMore) {
    		mLoadingMore = true;
    		new GetUserImage(mUserPageLoaded).execute();
    	}
    }
    
    private void refreshPhoto(boolean isUser) {
    	
    	if (isUser) {
    		mUserList.setAdapter(new PhotoListAdapter(mUserURLList, isUser));
    	} else {
    		mShopList.setAdapter(new PhotoListAdapter(mShopURLList, isUser));
    	}
    	
    	if (mPhotoDlg != null) {
    		mPhotoDlg.refresh();
    	}
    }
    
    public void loadImage(List<ImageStr> imageList, boolean isUser) {
    	
    	for (ImageStr imageItem : imageList) {
    		if (imageItem.loading)
    			continue;
    		
    		imageItem.loading = true;
        	GlobalVariable.imageLoader.loadImage(imageItem.url, new ImageLoadingListener() {
//    		GlobalVariable.imageLoader.loadImage("http://static.mp3.zdn.vn/skins/mp3_main/images/zmp3_logo.jpg",
//    				new ImageLoadingListener() {

        		private ImageStr mImageItem;
        		private boolean mIsUser;

        		public ImageLoadingListener init(ImageStr imgItem, boolean isUser) {

        			mImageItem = imgItem;
        			mIsUser = isUser;
        			return this;
        		}

        		public void onLoadingStarted(String imageUri, View view) { }
        		public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
        			Log.d("CycrixDebug", "Failed " + imageUri);
        			mImageItem.loading = false;
        			refreshPhoto(mIsUser);
        		}
        		
        		public void onLoadingCancelled(String imageUri, View view) {
        			Log.d("CycrixDebug", "Canceled " + imageUri);
        			mImageItem.loading = false;
        			refreshPhoto(mIsUser);
        		}

        		@Override
        		public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
        			Log.d("CycrixDebug", "Completed " + imageUri);
        			mImageItem.bm = loadedImage;
        			mImageItem.loading = false;
        			refreshPhoto(mIsUser);
        		}
        	}.init(imageItem, isUser));
    	}
    }
    
    public List<ImageStr> parseJsonImage(JSONArray jImgArr, List<ImageStr> imageList) throws JSONException {
    	
    	if (imageList == null)
    		imageList = new ArrayList<ImageStr>();
    	
    	for (int i = 0; i < jImgArr.length(); i++) {
    		JSONObject jImage =  jImgArr.getJSONObject(i);
    		imageList.add(new ImageStr(jImage.getString("image"), jImage.getString("description")));
    	}
    	
    	return imageList;
    }
    
    class PhotoListAdapter extends BaseAdapter {
    	
    	private LayoutInflater inflater;
    	private List<ImageStr> mItemList;
    	private boolean mIsUser;
    	
        public PhotoListAdapter(List<ImageStr> itemList, boolean isUser) {
        	inflater = getActivity().getLayoutInflater();
        	mItemList = itemList;
        	mIsUser = isUser;
        }

        @Override
        public int getCount() {
        	return mItemList.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
        	
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.photo_item, null);
            }
            
            ImageView img = (ImageView) convertView.findViewById(R.id.image);
            
            ImageStr imageItem = mItemList.get(position);
            if (imageItem.bm != null) {
            	img.setImageBitmap(imageItem.bm);
            	
            } else if (!imageItem.loading) {
            	loadImage(mItemList, mIsUser);
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
    
    public class GetUserImage extends AsyncTask<Void, Void, Boolean> {
    	
    	private int mPage;
    	
    	public GetUserImage(int page) {
    		mPage = page;
    	}

		@Override
		protected Boolean doInBackground(Void... params) {
			
			try {
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("shop_id", Integer.toString(mShop.mID)));
				pairs.add(new BasicNameValuePair("page", Integer.toString(mPage)));
			
				String json = NetworkManger.post(APILinkMaker.mGetUserImage(), pairs);

				JSONArray jImgArr = new JSONArray(json);
				int beforeSize = mUserURLList.size();
				
				parseJsonImage(jImgArr, mUserURLList);
				
				if (beforeSize == mUserURLList.size())
					mUserURLListEnd = true;
				else
					mUserPageLoaded++;
				
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return true;
		}

		protected void onPostExecute(Boolean k) { 
			mLoadingMore = false;
			
			if (mPhotoDlg != null) {
				mPhotoDlg.refresh();
			}
		}
		protected void onPreExecute(){ }
	}
}
