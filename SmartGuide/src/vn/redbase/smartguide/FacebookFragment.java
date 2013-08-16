package vn.redbase.smartguide;

import com.facebook.*;
import com.facebook.model.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


import com.nostra13.universalimageloader.core.ImageLoader;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.Animator.AnimatorListener;
import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher.ViewFactory;
import com.facebook.FacebookException;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.facebook.widget.LoginButton.OnErrorListener;
public class FacebookFragment extends Fragment {
	
	ImageView mLogo = null;
	ImageView mSlogan = null;
	ImageView mSmartGuide = null;
	TextView mLoginFacebookText = null;
	ImageButton mLogin = null;
	ImageButton mSkip = null;
	
	int mHeight = 0;
	int mWidth = 0;
	
	int mConer = -100;
	
	private MainAcitivyListener mMainAcitivyListener = null;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mMainAcitivyListener = (MainAcitivyListener) activity;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		return inflater.inflate(R.layout.facebook_screen, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		mLogo = (ImageView)getView().findViewById(R.id.logoWF);
		mSlogan = (ImageView)getView().findViewById(R.id.sloganWF);
		mSmartGuide = (ImageView)getView().findViewById(R.id.smartguideWF);
		mLoginFacebookText = (TextView)getView().findViewById(R.id.loginFacebookText);
		mLogin = (ImageButton)getView().findViewById(R.id.viaFaceButton);
		
		
		  
		mLogin.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// start Facebook Login
				try{
					Session.openActiveSession(getActivity(), true, new Session.StatusCallback() {

						// callback when session changes state
						@Override
						public void call(Session session, SessionState state, Exception exception) {
							if (session.isOpened()) {

								// make request to the /me API
								Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {

									// callback after Graph API response with user object
									@Override
									public void onCompleted(GraphUser user, Response response) {
										HashMap<String, String> token =  new  HashMap<String, String>();
										
										token.put("activateID", user.getId());
										GlobalVariable.smartGuideDB.insertFacebook(token);
										mMainAcitivyListener.exit();
									}
								});
							}
						}
					});
				}catch(Exception exx){
					exx.getMessage();
				}
			}
		});
		
		mSkip = (ImageButton)getView().findViewById(R.id.skipFaceButton);
		mSkip.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mMainAcitivyListener.exit();
			}
		});
	}
	
	public void setLayout(){
		Display display = getActivity().getWindowManager().getDefaultDisplay(); 
		mWidth = display.getWidth();
		mHeight = display.getHeight();
		
		int halfWidth = mWidth / 2;
		mLogo.setX(halfWidth - mLogo.getWidth() / 2);
		mLogo.setY(mHeight / 2 - 100 - 95 + mConer);
		
		mSlogan.setX(halfWidth - mSlogan.getWidth() / 2);
		mSlogan.setY(mHeight / 2 + 65 + mConer);
		
		mSmartGuide.setX(halfWidth - mSmartGuide.getWidth() / 2);
		mSmartGuide.setY(mHeight / 2 + 10 + mConer);
		
		mLoginFacebookText.setX(halfWidth - mLoginFacebookText.getWidth() / 2);
		mLoginFacebookText.setY(mHeight / 2 + 180 + mConer);
		
		mLogin.setX(halfWidth - (mLogin.getWidth() + mSkip.getWidth() + 10) / 2);
		mLogin.setY(mHeight / 2 + 215 + mConer);
		
		mSkip.setX( mLogin.getWidth() + 10 + mLogin.getX());
		mSkip.setY(mHeight / 2 + 215 + mConer);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}
}
