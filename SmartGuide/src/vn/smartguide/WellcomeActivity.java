package vn.smartguide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;



import com.facebook.FacebookException;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.facebook.widget.LoginButton.OnErrorListener;
import com.loopj.android.http.AsyncHttpResponseHandler;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v4.app.FragmentActivity;
import android.telephony.PhoneNumberUtils;

public class WellcomeActivity extends FragmentActivity{

	UiLifecycleHelper 	mUiHelper;
	boolean isShow = false;
	
	ImageView mLogo = null;
	ImageView mSlogan = null;
	ImageView mSmartGuide = null;
	
	ImageButton mLogin = null;
	ImageButton mSkip = null;
	
	EditText mNumberField = null;
	ImageButton mSendButton = null;
	TextView mStatusText = null;
	String phoneNumber = "";
	String confirmCode = "";
	
	int mHeight = 0;
	int mWidth = 0;
	int padding = 5;
	int marginTop = 40;
	
	String userID = "";
	boolean isConfirm = false;
	LoginButton authButton = null;
	
	private ObjectAnimator mSloganSlideUp = null;
	private ObjectAnimator mLogoFadeIn = null;
	private ObjectAnimator mLogoSlideUp = null;
	private ObjectAnimator mSmartGuideSlideUp = null;
	private ObjectAnimator mNumberFieldSlideUp = null;
	private ObjectAnimator mSendButtonSlideUp = null;
	private ObjectAnimator mStatusTextFadeIn = null;
	private ObjectAnimator mFacebookBtnFadeIn = null;
	private ObjectAnimator mSkipBtnFadeIn = null;
	private ObjectAnimator mStatusTextFlash = null;

	private Intent resultData;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wellcome);
		
		Session.StatusCallback callback = new Session.StatusCallback() {

			public void call(Session session, SessionState state, Exception exception) {
				if (state.isOpened()) {
					mStatusText.setText("Vui lòng chờ cập nhật thông tin...");
					makeMeRequest(session);
				} else if (state.isClosed()) {
				}
			}
		};
		
		resultData = new Intent();
		resultData.putExtra("GOAHEAD", "OK");
		
		mUiHelper = new UiLifecycleHelper(this, callback);
		mUiHelper.onCreate(savedInstanceState);
		
		mLogo = (ImageView)findViewById(R.id.logo);
		mSlogan = (ImageView)findViewById(R.id.slogan);
		mSmartGuide = (ImageView)findViewById(R.id.smartguide);
		mNumberField = (EditText)findViewById(R.id.numberField);
		mSendButton = (ImageButton)findViewById(R.id.sendCodeButton);
		mStatusText = (TextView)findViewById(R.id.statusTextView);
		
		mLogin = (ImageButton)findViewById(R.id.viaFaceButton);
		mSkip = (ImageButton)findViewById(R.id.skipFaceButton);
		
		mFacebookBtnFadeIn = ObjectAnimator.ofFloat(mLogin, "alpha", 0.0f, 1.0f);
		mFacebookBtnFadeIn.setInterpolator(new AccelerateDecelerateInterpolator());
		mSkipBtnFadeIn = ObjectAnimator.ofFloat(mSkip, "alpha", 0.0f, 1.0f);
		mSkipBtnFadeIn.setInterpolator(new AccelerateDecelerateInterpolator());
		
		mStatusTextFlash = ObjectAnimator.ofFloat(mStatusText, "alpha", 0.3f, 1.0f);
		mStatusTextFlash.setInterpolator(new LinearInterpolator());
		mStatusTextFlash.setDuration(1000);
		mStatusTextFlash.setRepeatCount(Animation.INFINITE);
		mStatusTextFlash.setRepeatMode(Animation.REVERSE);
		
		mSendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isConfirm == false){
					phoneNumber = mNumberField.getText().toString();
					if (PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber) == true){
						
						mStatusText.setText("Chờ và nhập mã xác nhận...");
						mNumberField.setText("");
						isConfirm = true;
						
						if (phoneNumber.charAt(0) == '+'){
							String subphone = phoneNumber.substring(1);
							phoneNumber = subphone;
						}
						
						new GetActivateCode().execute();
					}else{
						mStatusText.setText("Số điện thoại không hợp lệ...");
						mNumberField.setText("");
					}
				}else{
					confirmCode = mNumberField.getText().toString();
					mNumberField.setText("");
					new ConfirmActivateCode().execute();
				}
			}
		});
		
		mSkip.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				exit();
			}
		});
		
		authButton = (LoginButton) findViewById(R.id.authButton);
		authButton.setOnErrorListener(new OnErrorListener() {

			@Override
			public void onError(FacebookException error) {
			}
		});
		
		authButton.setReadPermissions(Arrays.asList("basic_info","email"));
		authButton.setSessionStatusCallback(new Session.StatusCallback() {
			@Override
			public void call(Session session, SessionState state, Exception exception) {

				if (session.isOpened()) {
					Request.executeMeRequestAsync(session,
							new Request.GraphUserCallback() {
						@Override
						public void onCompleted(GraphUser user,Response response) {
							if (user != null) { 
							}
						}
					});
				}

			}
		});
		
		mLogin.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				authButton.callOnClick();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus){
		if (isShow)
			return;
		isShow = true;
		wellcomeAnimation();
	}
	
	int halfWidth = 0;
	public void wellcomeAnimation(){
		Display display = getWindowManager().getDefaultDisplay(); 
		mWidth = display.getWidth();
		mHeight = display.getHeight();
		
		halfWidth = mWidth / 2;
		
		mStatusText.setX(halfWidth - mStatusText.getWidth() / 2);
		mLogo.setX(halfWidth - mLogo.getWidth() / 2);
		mSlogan.setX(halfWidth - mSlogan.getWidth() / 2);
		mSmartGuide.setX(halfWidth - mSmartGuide.getWidth() / 2);
		mNumberField.setX(halfWidth - (padding + mNumberField.getWidth() + mSendButton.getWidth()) / 2);
		mSendButton.setX(mNumberField.getX() + padding + mNumberField.getWidth());
		
		mLogoFadeIn = ObjectAnimator.ofFloat(mLogo, "alpha", 0.0f, 1.0f);
		mLogoFadeIn.setInterpolator(new AccelerateDecelerateInterpolator());
		
		mLogoSlideUp = ObjectAnimator.ofFloat(mLogo, "translationY", mHeight, mHeight / 2 - 100 - 95);
		mLogoSlideUp.setInterpolator(new AccelerateDecelerateInterpolator());
		mLogoSlideUp.addListener(new AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						mSmartGuide.setVisibility(View.VISIBLE);
						mSmartGuideSlideUp.start();
					}
				}, 500);
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {}
			
			@Override
			public void onAnimationEnd(Animator animation) {}
			
			@Override
			public void onAnimationCancel(Animator animation) {}
		});
		
		List<ObjectAnimator> ListLogoAnimators = new ArrayList<ObjectAnimator>();
		ListLogoAnimators.add(mLogoFadeIn);
		ListLogoAnimators.add(mLogoSlideUp);
		
		ObjectAnimator[] objectAnimators = ListLogoAnimators.toArray(new ObjectAnimator[ListLogoAnimators.size()]);
		AnimatorSet animSetXY = new AnimatorSet();
		animSetXY.playTogether(objectAnimators);
		animSetXY.setDuration(1000);//1sec
		animSetXY.start();
		
		mSmartGuideSlideUp = ObjectAnimator.ofFloat(mSmartGuide, "translationY", mHeight, mHeight / 2 + 10);
		mSmartGuideSlideUp.setDuration(1000);
		mSmartGuideSlideUp.setInterpolator(new AccelerateDecelerateInterpolator());
		mSmartGuideSlideUp.addListener(new AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {
				new Handler().postDelayed(new Runnable() {
					
					@Override
					public void run() {
						mSlogan.setVisibility(View.VISIBLE);
						mSloganSlideUp.start();
					}
				}, 500);
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {}
			
			@Override
			public void onAnimationEnd(Animator animation) {}
			
			@Override
			public void onAnimationCancel(Animator animation) {}
		});
		
		//mSmartGuideSlideUp.start();
		
		mSloganSlideUp = ObjectAnimator.ofFloat(mSlogan, "translationY", mHeight, mHeight / 2 + 65);
		mSloganSlideUp.setDuration(1000);
		mSloganSlideUp.setInterpolator(new AccelerateDecelerateInterpolator());
		mSloganSlideUp.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				new Handler().postDelayed(new Runnable() {
					
					@Override
					public void run() {
						mNumberFieldSlideUp.start();
					}
				}, 600);
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
				
			}
		});
		
		mNumberFieldSlideUp = ObjectAnimator.ofFloat(mNumberField, "translationY", mHeight, mHeight / 2 - 15);
		mNumberFieldSlideUp.setDuration(1000);
		mNumberFieldSlideUp.setInterpolator(new AccelerateDecelerateInterpolator());
		mNumberFieldSlideUp.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator arg0) {
				// TODO Auto-generated method stub
				List<ObjectAnimator> arrayListObjectAnimators = new ArrayList<ObjectAnimator>();
				
				mNumberField.setVisibility(View.VISIBLE);
				mSendButton.setVisibility(View.VISIBLE);
				// TODO Auto-generated method stub
				
				mSendButtonSlideUp = ObjectAnimator.ofFloat(mSendButton, "translationY", mHeight, mHeight / 2 - 15);
				mSendButtonSlideUp.setDuration(1000);
				mSendButtonSlideUp.setInterpolator(new AccelerateDecelerateInterpolator());
				
				
				mLogoSlideUp = ObjectAnimator.ofFloat(mLogo, "translationY", mLogo.getY(), marginTop);
				mLogoSlideUp.setDuration(1000);
				mLogoSlideUp.setInterpolator(new AccelerateDecelerateInterpolator());
				
				mSmartGuideSlideUp = ObjectAnimator.ofFloat(mSmartGuide, "translationY", mSmartGuide.getY(), mLogo.getHeight() + marginTop + 17);
				mSmartGuideSlideUp.setDuration(1000);
				mSmartGuideSlideUp.setInterpolator(new AccelerateDecelerateInterpolator());
				
				mSloganSlideUp.cancel();
				mSloganSlideUp = ObjectAnimator.ofFloat(mSlogan, "translationY", mSlogan.getY(), mLogo.getHeight() + marginTop + 17 + mSmartGuide.getHeight() + 12);
				mSloganSlideUp.setDuration(1000);
				mSloganSlideUp.setInterpolator(new AccelerateDecelerateInterpolator());
			
				arrayListObjectAnimators.add(mSendButtonSlideUp);
				arrayListObjectAnimators.add(mLogoSlideUp);
				arrayListObjectAnimators.add(mSloganSlideUp);
				arrayListObjectAnimators.add(mSmartGuideSlideUp);
				
				ObjectAnimator[] objectAnimators = arrayListObjectAnimators.toArray(new ObjectAnimator[arrayListObjectAnimators.size()]);
				AnimatorSet animSetXY = new AnimatorSet();
				animSetXY.playTogether(objectAnimators);
				animSetXY.start();
			}
			
			@Override
			public void onAnimationRepeat(Animator arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator arg0) {
				// TODO Auto-generated method stub
				mStatusText.setVisibility(View.VISIBLE);
				mStatusText.setY(mHeight / 2 - 50);
				mStatusTextFadeIn = ObjectAnimator.ofFloat(mStatusText, "alpha", 0.0f, 1.0f);
				mStatusTextFadeIn.setInterpolator(new LinearInterpolator());
				mStatusTextFadeIn.setDuration(1500);
				mStatusTextFadeIn.start();
				mStatusTextFlash.start();
			}
			
			@Override
			public void onAnimationCancel(Animator arg0) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		mUiHelper.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	protected void onResume() {

		super.onResume();
		mUiHelper.onResume();
	}
	
	@Override
	public void onPause() {

		super.onPause();
		mUiHelper.onPause();
	}
	
	@Override
	protected void onDestroy() {
		
		super.onDestroy();
		mUiHelper.onDestroy();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {

		super.onSaveInstanceState(outState);
		mUiHelper.onSaveInstanceState(outState);
	}
	
	@Override
	public void onBackPressed() {
		resultData = new Intent();
		resultData.putExtra("GOAHEAD", "FAIL");
		exit();
		return;
	}
	
	public void exit(){
		if (getParent() == null) 
		    setResult(Activity.RESULT_OK, resultData);
		else
		    getParent().setResult(Activity.RESULT_OK, resultData);
		finish();
	}
	
	private void makeMeRequest(final Session session) {

		Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {

			public void onCompleted(GraphUser user, Response response) {

				if (session == Session.getActiveSession()) {
					if (user != null) {
						GlobalVariable.id = user.getId();
						GlobalVariable.user_id = userID;
						GlobalVariable.name = user.getName();
					    if(user.asMap().get("gender").toString().compareTo("male") == 0)
					    	GlobalVariable.gender = true;
					    else
					    	GlobalVariable.gender = false;
					    
					    GlobalVariable.email = user.asMap().get("email").toString();
					    GlobalVariable.dob = user.getBirthday();
		                if (GlobalVariable.dob == null)
		                	GlobalVariable.dob ="01/01/2012";
		                GlobalVariable.job = "Unknown";
		                
		                GlobalVariable.avatar="http://graph.facebook.com/" + GlobalVariable.id + "/picture?type=large";
		                
						HashMap<String, String> token =  new  HashMap<String, String>();
						token.put("userID", GlobalVariable.id);
						token.put("avatar","http://graph.facebook.com/" + GlobalVariable.id + "/picture?type=large");
						token.put("name",GlobalVariable.name);
						
						GlobalVariable.smartGuideDB.insertFacebook(token);
						GlobalVariable.isNeedUpdateFacebook = true;
						
						exit();
					}
				}
				if (response.getError() != null) {
				}
			}
		});

		Bundle bundle = new Bundle();
		bundle.putString("fields", "picture,birthday,email,gender,id,name,name_format,first_name,last_name,work");
		request.setParameters(bundle);
		request.executeAsync();
	}
	
	public class GetActivateCode extends AsyncTask<Void, Void, Boolean> {    	

		String mJson = "";
		@Override
		protected Boolean doInBackground(Void... params) {
			mJson = NetworkManger.get(GlobalVariable.urlGetActivateCode + phoneNumber, false);
			return true;
		}

		protected void onPostExecute(Boolean k) { 
			
			
		}
		protected void onPreExecute(){ }
	}
	
	public class ConfirmActivateCode extends AsyncTask<Void, Void, Boolean> {    	

		String mJson = "";
		@Override
		protected Boolean doInBackground(Void... params) {
			mJson = NetworkManger.get(GlobalVariable.urlChekcActivateCode + phoneNumber + "&code=" + confirmCode, false);
			return true;
		}

		protected void onPostExecute(Boolean k) { 
			try {
				JSONObject result = new JSONObject(mJson);
				boolean success = result.getBoolean("result");
				int user_id = result.getInt("user_id");
				String avatar = result.getString("avatar");
				
				if (success){
					HashMap<String, String> token =  new  HashMap<String, String>();
					
					token.put("activateID", confirmCode);
					token.put("userID", Integer.toString(user_id));
					token.put("phoneNumber", phoneNumber);
					token.put("avatar", avatar);
					token.put("nameFace", result.getString("name"));
					
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(mNumberField.getWindowToken(), 0);
					
					userID = Integer.toString(user_id);
					GlobalVariable.smartGuideDB.insertActivateCode(token);
					
					boolean connect_fb = result.getBoolean("connect_fb");
					if (connect_fb == true){
						exit();
						return;
					}
					
					mNumberField.setText("");
					
					mStatusText.setText("Đăng nhập facebook");
					
					mLogin.setX(halfWidth - (mLogin.getWidth() + mSkip.getWidth() + 10) / 2);
					mLogin.setY(mNumberField.getY());
					
					mSkip.setX( mLogin.getWidth() + 10 + mLogin.getX());
					mSkip.setY(mNumberField.getY());
					
					mNumberFieldSlideUp = ObjectAnimator.ofFloat(mNumberField, "alpha", 1.0f, 0.0f);
					mNumberFieldSlideUp.setInterpolator(new AccelerateDecelerateInterpolator());
					mNumberFieldSlideUp.setDuration(200); 
					mNumberFieldSlideUp.addListener(new AnimatorListener() {
						
						@Override
						public void onAnimationStart(Animator animation) {
							// TODO Auto-generated method stub
							mSendButtonSlideUp = ObjectAnimator.ofFloat(mSendButton, "alpha", 1.0f, 0.0f);
							mSendButtonSlideUp.setInterpolator(new AccelerateDecelerateInterpolator());
							mSendButtonSlideUp.setDuration(200);
							mSendButtonSlideUp.start();
						}
						
						@Override
						public void onAnimationRepeat(Animator animation) {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void onAnimationEnd(Animator animation) {
							// TODO Auto-generated method stub
							mNumberField.setVisibility(View.INVISIBLE);
							mSendButton.setVisibility(View.INVISIBLE);
							
							mLogin.setVisibility(View.VISIBLE);
							mSkip.setVisibility(View.VISIBLE);
							
							List<ObjectAnimator> ListLogoAnimators = new ArrayList<ObjectAnimator>();
							ListLogoAnimators.add(mFacebookBtnFadeIn);
							ListLogoAnimators.add(mSkipBtnFadeIn);
							
							ObjectAnimator[] objectAnimators = ListLogoAnimators.toArray(new ObjectAnimator[ListLogoAnimators.size()]);
							AnimatorSet animSetXY = new AnimatorSet();
							animSetXY.playTogether(objectAnimators);
							animSetXY.setDuration(1200);//1sec
							animSetXY.start();
						}
						
						@Override
						public void onAnimationCancel(Animator animation) {
							// TODO Auto-generated method stub
							
						}
					});
					
					mNumberFieldSlideUp.start();
				}
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				mStatusText.setText("Mã xác nhận không hợp lệ");
				mNumberField.setText("");
			}
			
		}
		protected void onPreExecute(){ }
	}
}