package vn.smartguide;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import vn.smartguide.DetailPromo1Fragment.PromotionStr;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
/**
 * Created by ChauSang on 6/24/13.
 */
public class ShopDetailFragment extends Fragment {
	
	// GUI elements
    private List<Fragment> mDetailFragmentList;
    private DetailPromoFragment mPromoFragment;
    private DetailPromo1Fragment mPromo1Fragment;
    private DetailPromo2Fragment mPromo2Fragment;
    private DetailNoPromoFragment mNoPromoFragment;
    private Fragment mActiveFragment;
    
    private ImageView mLogoImageView;
    private ImageView mCoverImageView;
    private Button mBtnLike;
    private Button mBtnDislike;
    private ProgressBar mPrgLike;
    private TextView mTxtShopName;
    
    private Drawable mResLike, mResLikeHover, mResDislike, mResDislikeHover;
    
    // Data
    private Listener mListener = new Listener();
    private Shop mShop;
    
    ///////////////////////////////////////////////////////////////////////////
    // Override methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.shop_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set Viewpager adapter
        FragmentManager manager = getFragmentManager();
        
        mPromo1Fragment 	= new DetailPromo1Fragment();
        mPromo2Fragment 	= new DetailPromo2Fragment();
        mNoPromoFragment 	= new DetailNoPromoFragment();
        mDetailFragmentList = new ArrayList<Fragment>();
        mDetailFragmentList.add(manager.findFragmentById(R.id.shopInfoFrag));
        mDetailFragmentList.add(manager.findFragmentById(R.id.shopMenuFrag));
        mDetailFragmentList.add(manager.findFragmentById(R.id.shopPhotoFrag));
        mDetailFragmentList.add(manager.findFragmentById(R.id.shopCommentFrag));
        mDetailFragmentList.add(manager.findFragmentById(R.id.shopShowMapFrag));
        
        mPromoFragment = mPromo1Fragment;
        
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        
        for (Fragment f : mDetailFragmentList) {
        	transaction.hide(f);
        }
        
        transaction.add(R.id.layoutDetailPager, mPromo1Fragment).hide(mPromo1Fragment);
        transaction.add(R.id.layoutDetailPager, mPromo2Fragment).hide(mPromo2Fragment);
        transaction.add(R.id.layoutDetailPager, mNoPromoFragment).hide(mNoPromoFragment);
        transaction.commit();
        
        // Get GUI elements
        mLogoImageView = (ImageView) getView().findViewById(R.id.imgLogo);
        mCoverImageView = (ImageView) getView().findViewById(R.id.imgCover);
        
        mBtnLike = (Button) getView().findViewById(R.id.btnLike);
        mBtnDislike = (Button) getView().findViewById(R.id.btnDislike);
        mPrgLike = (ProgressBar) getView().findViewById(R.id.prgLikeDis);
        mTxtShopName = (TextView) getView().findViewById(R.id.txtShopName);
        
        Resources res = getResources();
        mResLike = getDrawableBitmap(res, R.drawable.icon_like);
        mResLikeHover = getDrawableBitmap(res, R.drawable.icon_like_hover);
        mResDislike = getDrawableBitmap(res, R.drawable.icon_dislike);
        mResDislikeHover = getDrawableBitmap(res, R.drawable.icon_dislike_hover);
        
        // Set like/dislike event
        mBtnLike.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if (mShop.mLikeStatus != 1)
					new LikeDisTask(true).execute();
			}
		});
        
        mBtnDislike.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mShop.mLikeStatus != 2)
					new LikeDisTask(false).execute();
			}
		});
        
        // Set up menu fragment
        DetailMenuFragment menu = (DetailMenuFragment) getFragmentManager().findFragmentById(R.id.detailMenuFragment);
        menu.setListener(new DetailMenuFragment.Listener() {
        	@Override
        	public void onButtonClick(int buttonIndex) {
        		onInfoButtonClick(buttonIndex);
        	}
        });
        
        // Set up Detal Promotion 1 Fragment
        mPromo1Fragment.setListener(new DetailPromo1Fragment.Listener() {
        	@Override
        	public void onRewardClick(PromotionStr reward) {
        		mListener.onReward1Click(reward);
        	}
        });
        
        // Set up Detal Promotion 2 Fragment
        mPromo2Fragment.setListener(new DetailPromo2Fragment.Listener() {
        	@Override
        	public void onReward2Click(PromotionTypeTwo promotion) {
        		mListener.onReward2Click(promotion);
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
//    
//    public DetailPromo1Fragment getPromoFragment(){
//    	return mPromo1Fragment;
//    }
//    
    
    public void setSGP(int sgp) {
    	mPromo1Fragment.setSGP(sgp);
    }
    
    public void onInfoButtonClick(int index) {
    	
    	Fragment f = null;
    	
    	if (index == -1) {
    		f = mPromoFragment;
//    		((DetailPromoFragment)f).runAnimation();
    	} else {
    		f = mDetailFragmentList.get(index);
    	}
    	
    	if (f == mActiveFragment)
    		return;
    	
    	getFragmentManager().beginTransaction().hide(mActiveFragment).show(f).commit();
		mActiveFragment = f;
    }
    
    public void setData(Shop s, boolean parseAll) {
    	
    	mShop = s;
    	DetailMenuFragment menu = (DetailMenuFragment) getFragmentManager().findFragmentById(R.id.detailMenuFragment);
    	
    	// Prepare data
    	new GetShopDetail(parseAll).execute();
    	
    	// Set promotion fragment either 1 or 2
    	if (parseAll) {
    		mPromoFragment = mPromo1Fragment;
    	} else {
    		if (s.mPromotionStatus) {
    			if (s.mPromotion.getType() == 1) {
    				mPromoFragment = mPromo1Fragment;
    			} else if (s.mPromotion.getType() == 2) {
    				mPromoFragment = mPromo2Fragment;
    			}
    		} else {
    			mPromoFragment = mNoPromoFragment;
    		}
    	}
    	
    	if (mActiveFragment != mPromoFragment) {
			if (mActiveFragment != null)
				getFragmentManager().beginTransaction().hide(mActiveFragment).show(mPromoFragment).commit();
			else
				getFragmentManager().beginTransaction().show(mPromoFragment).commit();
		}
    	
		mActiveFragment = mPromoFragment;

		//    	menu.updateLikeDis(s.mNumOfLike, s.mNumOfDislike, s.mLikeStatus);

		if (mPromoFragment == mNoPromoFragment) {
			if (!menu.mShowInfoBar)
				menu.toggleShopInfo(false, false);
			menu.switchToButton(0, false, true);
		} else if (menu.mShowInfoBar) {
			menu.switchToButton(0, false, false);
			menu.toggleShopInfo(false, true);
		}
    	
    	// Set image
		if (!parseAll) {
	    	GlobalVariable.cyImageLoader.showImage(mShop.mLogo, mLogoImageView);
	    	
	    	if (mShop.mCover.compareTo("null") != 0)
	    		GlobalVariable.cyImageLoader.showImage(mShop.mCover, mCoverImageView);
		} else {
			mLogoImageView.setImageBitmap(null);
			mCoverImageView.setImageBitmap(null);
		}
    	
		mTxtShopName.setText(parseAll ? "" : s.mName);
		updateLikeStatus(parseAll ? null : s);
		mPromoFragment.setData(parseAll ? null : s);
		((DetailShopInfoFragment) mDetailFragmentList.get(0)).setData(parseAll ? null : s);
    	((DetailShopMenuFragment) mDetailFragmentList.get(1)).setData(null);
    	((DetailShopPhotoFragment) mDetailFragmentList.get(2)).setData(null);
    	((DetailCommentFragment) mDetailFragmentList.get(3)).setData(null);
    	((DetailShowMapFragment) mDetailFragmentList.get(4)).setData(null);
    	
//    	if (s.mPromotionStatus == true && s.mPromotion.getType() == 1)
//    		mPromo1Fragment.runAnimation();
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Private methods
    ///////////////////////////////////////////////////////////////////////////
    
    private void updateLikeStatus(Shop s) {
    	
    	if (s != null) {
	    	mBtnLike.setText("" + s.mLike + " ");
	    	mBtnLike.setCompoundDrawables(null, null, s.mLikeStatus == 1 ? mResLikeHover : mResLike, null);
	    	mBtnDislike.setText("" + s.mDislike + " ");
	    	mBtnDislike.setCompoundDrawables(null, null, s.mLikeStatus == 2 ? mResDislikeHover : mResDislike, null);
    	} else {
    		mBtnLike.setText("0 ");
	    	mBtnLike.setCompoundDrawables(null, null, mResLike, null);
	    	mBtnDislike.setText("0 ");
	    	mBtnDislike.setCompoundDrawables(null, null, mResDislike, null);
    	}
    }
    
    private Drawable getDrawableBitmap(Resources res, int resId) {
    	Drawable drawable = res.getDrawable(resId);
    	drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
    	return drawable; 
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Async task
    ///////////////////////////////////////////////////////////////////////////
    
    private class GetShopDetail extends AsyncTask<Void, Void, Boolean> {
    	
    	private Exception mEx;
    	private boolean mParseAll;
    	
    	public GetShopDetail(boolean parseAll) {
    		mParseAll = parseAll;
    	}

		@Override
		protected Boolean doInBackground(Void... params) {
			
			try {
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("user_id", GlobalVariable.userID));
				pairs.add(new BasicNameValuePair("shop_id", Integer.toString(mShop.mID)));

				pairs.add(new BasicNameValuePair("user_lat", Float.toString(GlobalVariable.mLat)));
				pairs.add(new BasicNameValuePair("user_lng", Float.toString(GlobalVariable.mLng)));
				pairs.add(new BasicNameValuePair("version", "1"));
			
				String json = NetworkManger.post(APILinkMaker.mGetShopUser(), pairs);
				JSONObject jRoot = new JSONObject(json);
				if (mParseAll)
					Shop.getShop(jRoot, mShop);
				parseJsonShopDetail(jRoot);				
			} catch (Exception e) {
				mEx = e;
			}

			return true;
		}

		protected void onPostExecute(Boolean k) {
			
			if (mEx == null) {
				if (mParseAll) {
					mTxtShopName.setText(mShop.mName);
					updateLikeStatus(mShop);
					mPromoFragment.setData(mShop);
					((DetailShopInfoFragment) mDetailFragmentList.get(0)).setData(mShop);
					GlobalVariable.cyImageLoader.showImage(mShop.mLogo, mLogoImageView);
			    	
			    	if (mShop.mCover.compareTo("null") != 0)
			    		GlobalVariable.cyImageLoader.showImage(mShop.mCover, mCoverImageView);
			    	
			    	((MainActivity) getActivity()).updateHeader();
				}
				((DetailShopMenuFragment) mDetailFragmentList.get(1)).setData(mShop);
		    	((DetailShopPhotoFragment) mDetailFragmentList.get(2)).setData(mShop);
		    	((DetailCommentFragment) mDetailFragmentList.get(3)).setData(mShop);
		    	((DetailShowMapFragment) mDetailFragmentList.get(4)).setData(mShop);
		    	
		    	GlobalVariable.mCurrentShop = mShop;
			} else {
				GlobalVariable.showToast("Không thể lấy thông tin chi tiết của shop", getActivity());
			}
		}
		
		protected void onPreExecute() { }
	}
    
    private class LikeDisTask extends CyAsyncTask {
    	
    	private boolean mIsLike;
    	private Exception mEx;
    	
    	public LikeDisTask(boolean like) {
    		mIsLike = like;
    		setVisibleView(mPrgLike);
    		setDisableView(mBtnLike, mBtnDislike);
    	}
    	
		@Override
		protected Object doInBackground(Void... params) {
			try {
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("shop_id", Integer.toString(GlobalVariable.mCurrentShop.mID)));
				pairs.add(new BasicNameValuePair("user_id", GlobalVariable.userID));
				pairs.add(new BasicNameValuePair("type", mIsLike ? "1" : "2"));
				String mJson = NetworkManger.post(APILinkMaker.mPushLikeAction(), pairs);
				JSONObject jObject = new JSONObject(mJson);
				return jObject;
			} catch (Exception e) {
				mEx = e;
			}
			return null;
		}
    	
		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			
			try {
				if (mEx != null)
					throw mEx;
				
				JSONObject jObject = (JSONObject) result;
				mShop.mLike = jObject.getInt("like");
				mShop.mDislike = jObject.getInt("dislike");
				mShop.mLikeStatus = jObject.getInt("like_status");
				updateLikeStatus(mShop);
				
			} catch (Exception e) {
				GlobalVariable.showToast("Không thể thực hiện", getActivity());
			}
		}
    }
    
    public void parseJsonShopDetail(JSONObject jRoot) throws JSONException {
    	
    	// Parse promotion 2
    	if (jRoot.getInt("promotion_status") == 1) {
    		JSONObject jPro = jRoot.getJSONObject("promotion_detail");
    		if (jPro.getInt("promotion_type") == 2)
    			mShop.mPromotion = new PromotionTypeTwo().parse(jPro);
    	}
    	
    	// Parse Item
    	mShop.mItemCollections.clear();
    	mShop.mGroupItemList.clear();
    	JSONArray jCateItemArr = jRoot.optJSONArray("shop_items");
    	if (jCateItemArr != null)
    		for (int i = 0; i < jCateItemArr.length(); i++) {
    			try {
	    			JSONObject jCate = jCateItemArr.optJSONObject(i);
	    			String cateName = jCate.getString("cat_name");
	    			JSONArray jItemArr = jCate.getJSONArray("items");
	    			mShop.mGroupItemList.add(cateName);
	    			mShop.mItemCollections.put(cateName, new ArrayList<Item>());
	    			for (int j = 0; j < jItemArr.length(); j++) {
	    				JSONObject jItem = jItemArr.getJSONObject(j);
	    				Item item = new Item(jItem.getString("name"), jItem.getString("price"), null, null);
	    				mShop.mItemCollections.get(mShop.mGroupItemList.get(mShop.mGroupItemList.size() - 1)).add(item);
	    			}
    			} catch (Exception e) {}
    		}
    	
    	// Parse comment
    	mShop.mCommentList.clear();
    	JSONArray jCommentArr = jRoot.optJSONArray("shop_comments");
    	if (jCommentArr != null)
	    	for (int i = 0; i < jCommentArr.length(); i++) {
	    		try {
	    		JSONObject jComment = jCommentArr.getJSONObject(i);
	    		
	    		mShop.mCommentList.add(0, new Comment(jComment.getString("user"), jComment.getString("comment"),
	    				jComment.getString("avatar"), jComment.getString("time")));
	    		} catch (JSONException e) { }
	    	}
    	
    	// Parse user image
    	mShop.mUserImageList.clear();
    	JSONArray jUserImageArr = jRoot.optJSONArray("user_gallery");
    	if (jUserImageArr != null)
	    	for (int i = 0; i < jUserImageArr.length(); i++) {
	    		try {
		    		JSONObject jUserImage = jUserImageArr.getJSONObject(i);
		
		    		mShop.mUserImageList.add(new ImageStr(
		    				jUserImage.getString("image"), jUserImage.getString("description")));
	    		} catch (JSONException e) { }
	    	}
    	
    	// Parse shop image
    	mShop.mShopImageList.clear();
    	JSONArray jShopImage = jRoot.optJSONArray("shop_gallery");
    	if (jShopImage != null)
	    	for (int i = 0; i < jShopImage.length(); i++) {
	    		try {
	    			mShop.mShopImageList.add(new ImageStr(jShopImage.getString(i), null));
	    		} catch (JSONException e) { }
	    	}
    }
    
    public void releaseMemory() {
    	try {
	    	mLogoImageView.setImageBitmap(null);
	    	mCoverImageView.setImageBitmap(null);
//	    	((DetailShopPhotoFragment) mDetailFragmentList.get(2)).releaseMemory();
	    	((DetailCommentFragment) mDetailFragmentList.get(3)).releaseMemory();
	    	((DetailShowMapFragment) mDetailFragmentList.get(4)).releaseMemory();
    	} catch (Exception ex) {
    		
    	}
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Listener
    ///////////////////////////////////////////////////////////////////////////
    
    public static class Listener {
    	public void onReward1Click(PromotionStr reward) { }
    	public void onReward2Click(PromotionTypeTwo promotion) { }
    }
}