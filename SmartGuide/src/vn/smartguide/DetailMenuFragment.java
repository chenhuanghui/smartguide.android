package vn.smartguide;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.entity.mime.MinimalField;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;

/**
 * Created by cycrixlaptop on 7/29/13.
 */
public class DetailMenuFragment extends Fragment {

    private  MainAcitivyListener mMainAcitivyListener;
    private boolean mShowInfoBar = false;
    private Button mLikeButton = null;
    private Button mDisLikeButton = null;
    
    private final int[] mXindexArr = new int[] {
    		R.id.btnInfo,
    		R.id.btnShopMenu,
    		R.id.btnCamera,
    		R.id.btnShopPhoto,
    		R.id.btnShopComment,
    		R.id.btnShowMap,
    };
    
    private final int[] mYindexArr = new int[] {
    		R.id.layoutHalfUpperMenu,
    		R.id.layoutInfoShopChild
    };
    
    int mXindex = 0;
    int mYindex = 0;

    private Drawable icon_like;
    private Drawable icon_dislike;
    private Drawable icon_like_hover;
    private Drawable icon_dislike_hover;
    
    int mNumLike = 0;
    int mNumDislike = 0;
    int mLikeStatus = 0;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.detail_menu, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        
        Resources mResources = getActivity().getResources();
        icon_like = mResources.getDrawable(R.drawable.icon_like);
        icon_dislike = mResources.getDrawable(R.drawable.icon_dislike);
        icon_like_hover = mResources.getDrawable(R.drawable.icon_like_hover);
        icon_dislike_hover = mResources.getDrawable(R.drawable.icon_dislike_hover);
        
        View.OnClickListener switchEvent = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleShopInfo();
            }
        };
        
        ((ImageButton) getView().findViewById(R.id.btnShop3)).setOnClickListener(switchEvent);
        ((Button) getView().findViewById(R.id.btnShop1)).setOnClickListener(switchEvent);
        ((Button) getView().findViewById(R.id.btnShop2)).setOnClickListener(switchEvent);
        
        OnClickListener clickEvent = new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if (mYindex == 0)
					return;
				
				// Find id
				int id = -1;
				int index = 0;
				for (int i = 0; i < mXindexArr.length; i++) 
					if (mXindexArr[i] == v.getId()) {
						id = mXindexArr[i];
						index = i;
						break;
					}
				if (id == -1)
					return;
				
				mXindex = index;
				mYindex = 1;
				animateCursor();
				
				mMainAcitivyListener.getDetailFragment().onInfoButtonClick(index);
			}
		};
		
		for (int i = 0; i < mXindexArr.length; i++)
			((ImageButton) getView().findViewById(mXindexArr[i])).setOnClickListener(clickEvent);
		 mLikeButton = (Button) getView().findViewById(R.id.btnLike);
		 mLikeButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (GlobalVariable.mCurrentShop.mLikeStatus == 1){
					updateLikeStatus(mNumLike - 1, mNumDislike, 0);
					new ActionUnlike().execute();
					return;
				}
				
				mActionOfLike = 1;
				switch(mLikeStatus){
				case 0:
					updateLikeStatus(mNumLike + 1, mNumDislike, 1);
					break;
				case 2:
					updateLikeStatus(mNumLike + 1, mNumDislike - 1, 1);
					break;
				}
				
				new ActionLike().execute();
			}
		});
		 
		mDisLikeButton = (Button) getView().findViewById(R.id.btnDislike);
		mDisLikeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (GlobalVariable.mCurrentShop.mLikeStatus == 2){
					updateLikeStatus(mNumLike, mNumDislike - 1, 0);
					new ActionUnlike().execute();
					return;
				}
				
				mActionOfLike = 2;
				switch(mLikeStatus){
				case 0:
					updateLikeStatus(mNumLike, mNumDislike + 1, 2);
					break;
				case 1:
					updateLikeStatus(mNumLike - 1, mNumDislike + 1, 2);
					break;
				}
				new ActionLike().execute();
			}
		});
    }

    
    void updateLikeStatus(int like, int disl, int likestatus){
    	GlobalVariable.mCurrentShop.mNumOfLike = like;
    	GlobalVariable.mCurrentShop.mNumOfDislike = disl;
    	GlobalVariable.mCurrentShop.mLikeStatus = likestatus;
    	updateLikeDis(like, disl, likestatus);
    }
    
    int mActionOfLike = 0;
    
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mMainAcitivyListener = (MainAcitivyListener) activity;
    }

    private boolean mFirstTime = true;
    public void attach2DetailView() {
        try {
            ShopDetailFragment detailFragment = mMainAcitivyListener.getDetailFragment();
            RelativeLayout layoutRoot = (RelativeLayout) getView().findViewById(R.id.layoutRootDetailMenu);
            LinearLayout layoutUpper = (LinearLayout) getView().findViewById(R.id.layoutHalfUpperMenu);
            FrameLayout avaHolder = null;

            avaHolder = (FrameLayout) detailFragment.getView().findViewById(R.id.avaHolder);

            View cover = detailFragment.getView().findViewById(R.id.imgCover);

            // Set position for menu
            float x = cover.getWidth() - layoutRoot.getWidth() - 10;
            float y = avaHolder.getY() + avaHolder.getHeight() - layoutUpper.getHeight() - 4;

            layoutRoot.setTranslationX(x);
            layoutRoot.setTranslationY(y);
            
            // Set position for cursor
            View imgCursor = getView().findViewById(R.id.imgCursor);
            calculatePos(mXindex, mYindex);
            
            imgCursor.setTranslationX(mOutX);
            imgCursor.setTranslationY(mOutY);
            
            if (mFirstTime) {
	            Button btnShop1 = (Button) getView().findViewById(R.id.btnShop1);
	            Button btnShop2 = (Button) getView().findViewById(R.id.btnShop2);
	            btnShop2.setWidth(btnShop1.getWidth());
	            mFirstTime = false;
            }
            
        } catch (Exception e) {
            //e.printStackTrace();
            return;
        }
    }

    public void toggleShopInfo() {

        LinearLayout layoutUpper = (LinearLayout) getView().findViewById(R.id.layoutHalfUpperMenu);
        LinearLayout layoutInfoShop = (LinearLayout) getView().findViewById(R.id.layoutInfoShop);
        
        ImageButton btnShop = (ImageButton) getView().findViewById(R.id.btnShop3);

        mShowInfoBar = !mShowInfoBar;

        ObjectAnimator animator = null;
        int height = layoutUpper.getHeight();
        if (mShowInfoBar) {
        	
            animator = ObjectAnimator.ofFloat(layoutInfoShop, "translationY", 0, height);
            mYindex = 1;
            animator.addListener(new AnimatorListener() {
				public void onAnimationStart(Animator animation) {}
				public void onAnimationRepeat(Animator animation) {}
				public void onAnimationEnd(Animator animation) {
					ImageButton btnShop = (ImageButton) getView().findViewById(R.id.btnShop3);
					btnShop.setImageResource(R.drawable.icon_shop);
				}
				public void onAnimationCancel(Animator animation) {
					onAnimationEnd(animation);
				}
			});
            mMainAcitivyListener.getDetailFragment().onInfoButtonClick(mXindex);
        } else {
        	mMainAcitivyListener.getDetailFragment().onInfoButtonClick(-1);
            animator = ObjectAnimator.ofFloat(layoutInfoShop, "translationY", height, 0);
            mYindex = 0;
            animator.addListener(new AnimatorListener() {
				public void onAnimationStart(Animator animation) {}
				public void onAnimationRepeat(Animator animation) {}
				public void onAnimationEnd(Animator animation) {
					ImageButton btnShop = (ImageButton) getView().findViewById(R.id.btnShop3);
					btnShop.setImageResource(R.drawable.icon_info);
				}
				public void onAnimationCancel(Animator animation) {
					onAnimationEnd(animation);
				}
			});
        }

        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animateCursor();
        animator.start();
        animateSwitch();
    }
    
    public void updateLikeDis(int like, int dislike, int likestatus){
    	
    	mNumLike = like;
    	mNumDislike = dislike;
    	mLikeStatus = likestatus;
    	
    	switch(likestatus){
    	case 0:
    		mLikeButton.setCompoundDrawablesWithIntrinsicBounds(null, null, icon_like, null);
    		mDisLikeButton.setCompoundDrawablesWithIntrinsicBounds(null, null, icon_dislike, null);
    		break;
    	case 1:
    		mLikeButton.setCompoundDrawablesWithIntrinsicBounds(null, null, icon_like_hover, null);
    		mDisLikeButton.setCompoundDrawablesWithIntrinsicBounds(null, null, icon_dislike, null);
    		break;
    	case 2:
    		mLikeButton.setCompoundDrawablesWithIntrinsicBounds(null, null, icon_like, null);
    		mDisLikeButton.setCompoundDrawablesWithIntrinsicBounds(null, null, icon_dislike_hover, null);
    		break;
    	}
    	
    	mLikeButton.setText(Integer.toString(like) + " ");
    	mDisLikeButton.setText(Integer.toString(dislike) + " ");
    }
    
    private int mOutX, mOutY;
    private void calculatePos(int xindex, int yindex) {
    	
    	mOutY = 0;
    	for (int i = 0; i <= yindex; i++) {
    		View layoutUpper = getView().findViewById(mYindexArr[yindex]);
    		mOutY += layoutUpper.getHeight() + i * 2;
    	}
    	
    	View imgCursor = getView().findViewById(R.id.imgCursor);
    	if (yindex == 0) {
    		View layoutShopSwitch = getView().findViewById(R.id.layoutShopSwitch);
    		mOutX = (int) (layoutShopSwitch.getX() + (layoutShopSwitch.getWidth() - imgCursor.getWidth()) / 2);
    	} else {
    		View btn = getView().findViewById(mXindexArr[xindex]);
	    	mOutX = (int) (btn.getX() + (btn.getWidth() - imgCursor.getWidth()) / 2);
    	}
    }
    
    private void animateCursor() {
    	
    	// Calculate pos
		calculatePos(mXindex, mYindex);
		
		// Start animation
		View imgCursor = getView().findViewById(R.id.imgCursor);
		ObjectAnimator animatorX = ObjectAnimator.ofFloat(imgCursor, "translationX", mOutX);
		ObjectAnimator animatorY = ObjectAnimator.ofFloat(imgCursor, "translationY", mOutY);
		TimeInterpolator acce = new AccelerateDecelerateInterpolator();
		animatorX.setInterpolator(acce);
		animatorY.setInterpolator(acce);
		animatorX.start();
		animatorY.start();
    }
    
    private void animateSwitch() {
    	View layoutshopswView1 = getView().findViewById(R.id.layoutShopSwitch1);
    	View layoutshopswView2 = getView().findViewById(R.id.layoutShopSwitch2);
    	View btnShop = getView().findViewById(R.id.btnShop3);
    	
    	float length = layoutshopswView1.getWidth() - btnShop.getWidth();
    	
    	ObjectAnimator animator1 = null;
    	ObjectAnimator animator2 = null;
    	if (mShowInfoBar) {
    		animator1 = ObjectAnimator.ofFloat(layoutshopswView1, "translationX", 0, -length);
    		animator2 = ObjectAnimator.ofFloat(layoutshopswView2, "translationX", 
    				layoutshopswView1.getWidth(), btnShop.getWidth());
    	} else {
    		animator1 = ObjectAnimator.ofFloat(layoutshopswView1, "translationX", -length, 0);
    		animator2 = ObjectAnimator.ofFloat(layoutshopswView2, "translationX", 
    				btnShop.getWidth(), layoutshopswView1.getWidth());	
    	}
    	TimeInterpolator acce = new AccelerateDecelerateInterpolator();
    	animator1.setInterpolator(acce);
    	animator2.setInterpolator(acce);
    	animator1.start();
    	animator2.start();
    }
    
    public class ActionLike extends AsyncTask<Void, Void, Boolean> {
    	String mJson = "";
		@Override
		protected Boolean doInBackground(Void... params) {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("shop_id", Integer.toString(GlobalVariable.mCurrentShop.mID)));
			pairs.add(new BasicNameValuePair("user_id", GlobalVariable.userID));
			pairs.add(new BasicNameValuePair("type", Integer.toString(mActionOfLike)));
			mJson = NetworkManger.post(APILinkMaker.mPushLikeAction(), pairs);
			if (mJson == "")
				return false;
			return true;
		}

		@Override
		protected void onPostExecute(Boolean k){}

		@Override
		protected void onPreExecute(){}
	}
    
    public class ActionUnlike extends AsyncTask<Void, Void, Boolean> {
    	String mJson = "";
		@Override
		protected Boolean doInBackground(Void... params) {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("shop_id", Integer.toString(GlobalVariable.mCurrentShop.mID)));
			pairs.add(new BasicNameValuePair("user_id", GlobalVariable.userID));
			mJson = NetworkManger.post(APILinkMaker.mPushUnlikeAction(), pairs);
			if (mJson == "")
				return false;
			return true;
		}

		@Override
		protected void onPostExecute(Boolean k){}

		@Override
		protected void onPreExecute(){}
	}
}