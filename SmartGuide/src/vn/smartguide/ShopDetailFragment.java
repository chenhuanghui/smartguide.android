package vn.smartguide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.Animator.AnimatorListener;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
/**
 * Created by ChauSang on 6/24/13.
 */
public class ShopDetailFragment extends Fragment {
	
	private final int NUM_PAGES = 2;

    private Activity mActivity = null;
    private MainAcitivyListener mMainAcitivyListener = null;
    private List<Fragment> mDetailFragmentList;
    private DetailPromoFragment mPromoFragment;
    private DetailPromo1Fragment mPromo1Fragment;
    private DetailPromo2Fragment mPromo2Fragment;
    private Fragment mActiveFragment;
    private Shop mShop;
    
    private ImageView mLogoImageView;
    private ImageView mCoverImageView;
    
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        mMainAcitivyListener = (MainAcitivyListener) mActivity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.shop_detail, container, false);
        
        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set Viewpager adapter
        mPromo1Fragment = new DetailPromo1Fragment();
        mPromo2Fragment = new DetailPromo2Fragment();
        mDetailFragmentList = new ArrayList<Fragment>();
        mDetailFragmentList.add(new DetailShopInfoFragment());
        mDetailFragmentList.add(new DetailShopMenuFragment());
        mDetailFragmentList.add(new DetailShopTakePhoto());
        mDetailFragmentList.add(new DetailShopPhotoFragment());
        mDetailFragmentList.add(new DetailCommentFragment());
        mDetailFragmentList.add(new DetailShowMapFragment());
        mDetailFragmentList.add(new DetailPromo2Fragment());
        
        mPromoFragment = mPromo1Fragment;
        
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        
        for (Fragment f : mDetailFragmentList) {
        	transaction.add(R.id.layoutDetailPager, f).hide(f);
        }
        transaction.add(R.id.layoutDetailPager, mPromo1Fragment).hide(mPromo1Fragment);
        transaction.add(R.id.layoutDetailPager, mPromo2Fragment).hide(mPromo2Fragment);
        transaction.commit();
        
        mLogoImageView = (ImageView) getView().findViewById(R.id.imgLogo);
        mCoverImageView = (ImageView) getView().findViewById(R.id.imgCover);
    }

    @Override
    public void onResume() {
    	super.onResume();

    	try {
    		getView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
    			@Override
    			public void onGlobalLayout() {
    				((DetailMenuFragment) getFragmentManager().findFragmentById(R.id.detailMenuFragment))
    				.attach2DetailView();
    			}
    		});
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }
    
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
    	
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mDetailFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
    
    public DetailPromo1Fragment getPromoFragment(){
    	return mPromo1Fragment;
    }
    
    public void onInfoButtonClick(int index) {
    	
    	Fragment f = null;
    	
    	if (index == -1) {
    		f = mPromoFragment;
//    		((DetailPromoFragment)f).runAnimation();
    	} else {
    		f = mDetailFragmentList.get(index);
    	}
    	
    	DetailShowMapFragment mapFragment = (DetailShowMapFragment) mDetailFragmentList.get(5);
    	if (mActiveFragment == mapFragment) {
    		mapFragment.destroyMap();
    	} else if (f == mapFragment) {
    		mapFragment.createMap();
    	}
    	
    	if (f == mActiveFragment)
    		return;
    	
    	getFragmentManager().beginTransaction().hide(mActiveFragment).show(f).commit();
		mActiveFragment = f;
		
		switch(index){
		case 2:
			Intent intent = new Intent(getActivity(), TakePictureActivity.class);
	    	getActivity().startActivity(intent);
			break;
		}
    }
    
    private int mCoverHeight;
    private boolean mShowCover = true;
    public void toggleCover(boolean isShow) {
    	
    	View cover = getView().findViewById(R.id.layoutCover);
		ObjectAnimator animator = null;
		
		mShowCover = isShow;
		if (!mShowCover) {
			mCoverHeight = cover.getHeight();
			animator = ObjectAnimator.ofFloat(cover, "translationY", 0, -mCoverHeight);
		} else {
			mCoverHeight = cover.getHeight();
			animator = ObjectAnimator.ofFloat(cover, "translationY", -mCoverHeight, 0);
		}
		
		TimeInterpolator acce = new AccelerateDecelerateInterpolator();
		animator.setInterpolator(acce);
		animator.start();
    }
    
    public void setData(Shop s) {
    	mShop = s;
    	
    	// Prepare data
    	getShopDetail();
    	if (s.mPromotionStatus) {
    		if (s.mPromotion.getType() == 1) {
    			mPromoFragment = mPromo1Fragment;
    		} else if (s.mPromotion.getType() == 2) {
    			mPromoFragment = mPromo2Fragment;
    		}
    	}
    	
    	if (mActiveFragment != mPromoFragment) {
    		if (mActiveFragment != null)
    		getFragmentManager().beginTransaction().hide(mActiveFragment).show(mPromoFragment).commit();
    		else
    			getFragmentManager().beginTransaction().show(mPromoFragment).commit();
    	}
    	
    	mActiveFragment = mPromoFragment;
    	
    	// Set data
    	GlobalVariable.imageLoader.displayImage(mShop.mLogo, mLogoImageView, GlobalVariable.displayImageOptions);
    	
    	if (mShop.mCover.compareTo("null") != 0)
    		GlobalVariable.imageLoader.displayImage(mShop.mCover, mCoverImageView, GlobalVariable.displayImageOptions);
    	
    	DetailMenuFragment menu = (DetailMenuFragment) getFragmentManager().findFragmentById(R.id.detailMenuFragment);
    	menu.updateLikeDis(s.mNumOfLike, s.mNumOfDislike, s.mLikeStatus);
    	
    	if (menu.mYindex == 1) {
    		menu.toggleShopInfo();
    	}
    	
    	mPromoFragment.setData(s);
    	((DetailShopInfoFragment) mDetailFragmentList.get(0)).setData(s);
    	
    	if (s.mPromotionStatus == true && s.mPromotion.getType() == 1)
    		mPromo1Fragment.runAnimation();
    }
    
    public void getShopDetail() {
    	new GetShopDetail().execute();
    }
    
    public class GetShopDetail extends AsyncTask<Void, Void, Boolean> {    	

		@Override
		protected Boolean doInBackground(Void... params) {
			
			try {
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("user_id", GlobalVariable.userID));
				pairs.add(new BasicNameValuePair("shop_id", Integer.toString(mShop.mID)));

				// Fuck you
				pairs.add(new BasicNameValuePair("user_lat", Float.toString(GlobalVariable.mLat)));
				pairs.add(new BasicNameValuePair("user_lng", Float.toString(GlobalVariable.mLng)));
			
				String json = NetworkManger.post(APILinkMaker.mGetShopUser(), pairs);
				JSONObject jRoot = new JSONObject(json);
				parseJsonShopDetail(jRoot);
				
				
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return true;
		}

		protected void onPostExecute(Boolean k) { 
			
			((DetailShopMenuFragment) mDetailFragmentList.get(1)).setData(mShop);
	    	((DetailShopPhotoFragment) mDetailFragmentList.get(3)).setData(mShop);
	    	((DetailCommentFragment) mDetailFragmentList.get(4)).setData(mShop);
	    	((DetailShowMapFragment) mDetailFragmentList.get(5)).setData(mShop);
		}
		protected void onPreExecute(){ }
	}
    
    public void parseJsonShopDetail(JSONObject jRoot) throws JSONException {
    	// Parse Item
    	mShop.mItemList.clear();
    	JSONArray jCateItemArr = jRoot.getJSONArray("shop_items"); 
    	
    	for (int i = 0; i < jCateItemArr.length(); i++) {
    		JSONArray jItemArr = jCateItemArr.getJSONObject(i).getJSONArray("items");
    		
    		for (int j = 0; j < jItemArr.length(); j++) {
    			JSONObject jItem = jItemArr.getJSONObject(j);
    			
    			mShop.mItemList.add(new Item(jItem.getString("name"), jItem.getString("price"), null, null));
    		}
    	}
    	
    	// Parse comment
    	mShop.mCommentList.clear();
    	JSONArray jCommentArr = jRoot.getJSONArray("shop_comments");
    	for (int i = 0; i < jCommentArr.length(); i++) {
    		JSONObject jComment = jCommentArr.getJSONObject(i);
    		
    		mShop.mCommentList.add(0, new Comment(jComment.getString("user"), jComment.getString("comment"),
    				jComment.getString("avatar"), jComment.getString("time")));
    	}
    	
    	// Parse user image
    	mShop.mUserImageList.clear();
    	JSONArray jUserImageArr = jRoot.getJSONArray("user_gallery");
    	for (int i = 0; i < jUserImageArr.length(); i++) {
    		JSONObject jUserImage = jUserImageArr.getJSONObject(i);

    		mShop.mUserImageList.add(new ImageStr(
    				jUserImage.getString("image"), jUserImage.getString("description")));
    	}
    	
    	// Parse shop image
    	mShop.mShopImageList.clear();
    	JSONArray jShopImage = jRoot.getJSONArray("shop_gallery");
    	for (int i = 0; i < jUserImageArr.length(); i++) {
    		mShop.mShopImageList.add(new ImageStr(jShopImage.getString(i), null));
    	}
    }
}