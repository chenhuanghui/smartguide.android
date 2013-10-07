package vn.smartguide;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import vn.smartguide.CyImageLoader.Listener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;

public class PhotoActivity extends FragmentActivity{

//	private DetailShopPhotoFragment mParentFragment;
	private static List<ImageStr> 	sImageList;
	private static boolean 			sIsUser;
	private static int 				sShopID;
	
	private PhotoPagerAdapter 	mAdapter;
	private boolean 			mIsUser;
	private List<ImageStr> 		mImageList;
	private int 				mShopID;
	
	public static void newInstance(Activity act, List<ImageStr> imageList, boolean isUser, int shopID) {
		
		sImageList = imageList;
		sIsUser = isUser;
		sShopID = shopID;
		
		Intent intent = new Intent(act, PhotoActivity.class);
		act.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photo_dialog);

		mImageList = sImageList;
		mIsUser = sIsUser;
		mShopID = sShopID;

		sImageList = null;
		
		ViewPager pager = (ViewPager) findViewById(R.id.pagerPhotoFull);
		mAdapter = new PhotoPagerAdapter(getSupportFragmentManager());
		mAdapter.setData(mImageList);
		pager.setAdapter(mAdapter);
	}

	public class PhotoPagerAdapter extends FragmentStatePagerAdapter {

		private List<PhotoFullFragment> fragArr = new ArrayList<PhotoFullFragment>();
		private List<ImageStr> mItemList = new ArrayList<ImageStr>();
		private boolean mIsLoadingMore;
		private boolean mEnd;
		private int mPageLoaded = 1;

		public PhotoPagerAdapter(FragmentManager fm) {
			super(fm);
		}
		
		public void setData(List<ImageStr> imageList) {
			
			mItemList.clear();
			mItemList.addAll(imageList);
			fragArr.clear();
			for (ImageStr item : mItemList) {
				item.loadFail = false;
				fragArr.add(new PhotoFullFragment(item));
			}
			notifyDataSetChanged();
		}
		
		public void loadMore() {
			
			if (!mIsUser || mIsLoadingMore || mEnd)
				return;
			
			mIsLoadingMore = true;
			new GetImage(mPageLoaded + 1).execute();
		}

		@Override
		public int getCount() {
			return fragArr.size();
		}

		@Override
		public Fragment getItem(int position) {

			if (getCount() - position <= 5)
				loadMore();
			
			PhotoFullFragment f = fragArr.get(position);
			f.mImageItem = mItemList.get(position);
			return f;
		}

		@Override
		public void notifyDataSetChanged() {

			super.notifyDataSetChanged();
			for (int i = 0; i < fragArr.size(); i++)
				fragArr.get(i).refresh();
		}
		
		///////////////////////////////////////////////////////////////////////////
		// Network Asynctask
		///////////////////////////////////////////////////////////////////////////
		
		private class GetImage extends AsyncTask<Void, Void, Boolean> {

			private int mPage;
			private JSONArray jImgArr;
			private Exception mEx;

			public GetImage(int page) {
				
				mPage = page;
			}
			
			protected void onPreExecute() { }

			@Override
			protected Boolean doInBackground(Void... params) {

				try {
					List<NameValuePair> pairs = new ArrayList<NameValuePair>();
					pairs.add(new BasicNameValuePair("shop_id", Integer.toString(mShopID)));
					pairs.add(new BasicNameValuePair("page", Integer.toString(mPage)));
	
					String json = null;
					if (mIsUser)
						json = NetworkManger.post(APILinkMaker.mGetUserImage(), pairs);
					else
						json = NetworkManger.post(APILinkMaker.mGetShopImage(), pairs);
					//					String json = NetworkManger.post(APILinkMaker.mGetUserImage(), pairs);
	
					jImgArr = new JSONArray(json);
				} catch (Exception e) {
					mEx = e;
				}

				return true;
			}

			protected void onPostExecute(Boolean k) {
				
				try {
					if (mEx == null)
						throw mEx;
					
					int beforeSize = mItemList.size();
					DetailShopPhotoFragment.parseJsonImage(jImgArr, mItemList);
					if (beforeSize == mItemList.size())
						mEnd = true;
					else {
						mPageLoaded++;
//						cycrixDebug("Loaded " + mItemList.size() + " item, " + mPageLoaded + " pages");
						for (int i = beforeSize; i < mItemList.size(); i++) {
							fragArr.add(new PhotoFullFragment(mItemList.get(i)));
							cycrixDebug(mItemList.get(i).url);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				notifyDataSetChanged();
				mIsLoadingMore = false;
			}
		}
	}

	@SuppressLint("ValidFragment")
	public class PhotoFullFragment extends Fragment {

		private ImageStr mImageItem;

		public PhotoFullFragment(ImageStr imageItem) {
			mImageItem = imageItem;
		}
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			
			super.onCreate(savedInstanceState);
			View v = getView();
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			View v = inflater.inflate(R.layout.photo_full_item, container, false);
			return v;
		}
		
		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			
			refresh(view);
			super.onViewCreated(view, savedInstanceState);
		}
		
		@Override
		public void onDestroy() {

			super.onDestroy();
			View v = getView();
		}
		
		public void refresh() {
			
			refresh(getView());
		}

		public void refresh(View v) {
			
			if (v == null)
				return;

			ImageView img = (ImageView) v.findViewById(R.id.imgFullPhoto);
			ProgressBar prgWait = (ProgressBar) v.findViewById(R.id.prgWait);
			TextView txtTitle = (TextView) v.findViewById(R.id.txtTit);
			TextView txtDsc = (TextView) v.findViewById(R.id.txtDesc);

			Point size = new Point();
			getWindowManager().getDefaultDisplay().getSize(size);
			
//			Bitmap bm = mImageItem.bm.get();
			
			if (mImageItem.loadFail)
				img.setImageResource(R.drawable.ava_loading);
			else
				GlobalVariable.cyImageLoader.loadImage(mImageItem.url, new Listener() {

					private PhotoFullFragment frag;

					public Listener init(PhotoFullFragment frag) {

						this.frag = frag;
						return this;
					}

					@Override
					public void loadFinish(int from, Bitmap image, String url) {

						View rootView = frag.getView();

						if (rootView == null)
							return;

						switch (from) {
						case CyImageLoader.FROM_NETWORK:
						case CyImageLoader.FROM_DISK:
						case CyImageLoader.FROM_MEMORY: {
							ImageView img = (ImageView) rootView.findViewById(R.id.imgFullPhoto);
							img.setImageBitmap(image);
							break;
						}
						}
					}

					@Override
					public void loadFail(Exception e) {

						mImageItem.loadFail = true;
						View rootView = frag.getView();

						if (rootView == null)
							return;

						ImageView img = (ImageView) rootView.findViewById(R.id.imgFullPhoto);
						img.setImageResource(R.drawable.ava_loading);
					}

				}.init(this), size, PhotoActivity.this);

			if (!mIsUser) {
				txtTitle.setVisibility(View.INVISIBLE);
				txtDsc.setVisibility(View.INVISIBLE);
			} else {
				txtDsc.setText(mImageItem.description);
			}
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {

			super.onActivityCreated(savedInstanceState);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);  // Add this method.
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);  // Add this method.
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Debug stuff
	///////////////////////////////////////////////////////////////////////////
	
	private static boolean isDebug = true;
	private static final String TAG = "CycrixDebug";
	private static final String HEADER = "PhotoActivity";
	private static void cycrixDebug(String message) {
		
		if (isDebug) Log.d(TAG, HEADER + ": " + message);
	}
}