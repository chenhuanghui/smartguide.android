package vn.smartguide;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.telephony.PhoneNumberUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.FacebookException;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.facebook.widget.LoginButton.OnErrorListener;
import com.google.analytics.tracking.android.EasyTracker;

public class WellcomeActivity extends FragmentActivity{

	UiLifecycleHelper 	mUiHelper;
	boolean isConfirm = false;

	ImageView mLogo;
	ImageView mSlogan;
	ImageView mSmartGuide;

	ImageButton mLogin;
	ImageButton mSkip;

	EditText mNumberField = null;
	ImageButton mSendButton = null;
	TextView mStatusText = null;
	TextView mHeadText = null;
	TextView mTailText = null;
	
	TextView mTimeText;
	
	String phoneNumber = "";
	String confirmCode = "";

	Button mResendCode;
	
	int mHeight = 0;
	int mWidth = 0;
	int padding = 5;
	int marginTop = 40;

	String userID = "";
	LoginButton authButton = null;

	private ObjectAnimator mNumberFieldSlideUp;
	private ObjectAnimator mSendButtonSlideUp;
	private ObjectAnimator mFacebookBtnFadeIn;
	private ObjectAnimator mStatusTextFlash;

	private Intent resultData;

	TextView m84TV;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wellcome);

		Session.StatusCallback callback = new Session.StatusCallback() {

			public void call(Session session, SessionState state, Exception exception) {
				if (state.isOpened()) {
					mStatusText.setText("Vui lÃƒÂ²ng chÃ¡Â»ï¿½ cÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t thÃƒÂ´ng tin...");
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

		// Set up animation		
		mFacebookBtnFadeIn = ObjectAnimator.ofFloat(mLogin, "alpha", 0.0f, 1.0f);
		mFacebookBtnFadeIn.setInterpolator(new AccelerateDecelerateInterpolator());
		//		mSkipBtnFadeIn = ObjectAnimator.ofFloat(mSkip, "alpha", 0.0f, 1.0f);
		//		mSkipBtnFadeIn.setInterpolator(new AccelerateDecelerateInterpolator());

		mStatusTextFlash = ObjectAnimator.ofFloat(mStatusText, "alpha", 0.3f, 1.0f);
		mStatusTextFlash.setInterpolator(new LinearInterpolator());
		mStatusTextFlash.setDuration(1000);
		mStatusTextFlash.setRepeatCount(Animation.INFINITE);
		mStatusTextFlash.setRepeatMode(Animation.REVERSE);
		mStatusTextFlash.start();

		mSendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isConfirm == false){
					phoneNumber = formatPhone(mNumberField.getText().toString());
					if (PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber) && validatePhoneNumber(phoneNumber)){
						if (phoneNumber.charAt(0) == '+'){
							String subphone = phoneNumber.substring(1);
							phoneNumber = subphone;
						}
						
						confirmPhone();
					}else{
						mStatusText.setText("SÃ¡Â»â€˜ Ã„â€˜iÃ¡Â»â€¡n thoÃ¡ÂºÂ¡i khÃƒÂ´ng hÃ¡Â»Â£p lÃ¡Â»â€¡...");
						mNumberField.setText("");
					}
				}else{
					if (timer != null)
						timer.cancel();
					
					mTailText.setVisibility(View.INVISIBLE);
					mHeadText.setVisibility(View.INVISIBLE);
					mTimeText.setVisibility(View.INVISIBLE);
					mResendCode.setVisibility(View.INVISIBLE);
					
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
				authButton.performClick();
			}
		});

		m84TV = (TextView) findViewById(R.id.m84TV);
		mTimeText = (TextView) findViewById(R.id.timeLeftTV);
		mHeadText = (TextView) findViewById(R.id.headTV);
		mTailText = (TextView) findViewById(R.id.tailTV);
		mResendCode = (Button) findViewById(R.id.resendCodeBtn);
		mResendCode.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mNumberField.getWindowToken(), 0);
				
				startCounting();
				
				mResendCode.setVisibility(View.INVISIBLE);
				m84TV.setVisibility(View.INVISIBLE);
				isConfirm = true;
				mStatusText.setText("ChÃ¡Â»ï¿½ vÃƒÂ  nhÃ¡ÂºÂ­p mÃƒÂ£ xÃƒÂ¡c nhÃ¡ÂºÂ­n...");
				mNumberField.setText("");
				
				new GetActivateCode().execute();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
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
						GlobalVariable.faceAccessToken = session.getAccessToken();
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
		@Override
		protected Boolean doInBackground(Void... params) {
			NetworkManger.get(GlobalVariable.urlGetActivateCode + phoneNumber, false);
			return true;
		}

		protected void onPostExecute(Boolean k) { }
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

					mStatusText.setText("Ã„ï¿½Ã„Æ’ng nhÃ¡ÂºÂ­p facebook");

					mNumberFieldSlideUp = ObjectAnimator.ofFloat(mNumberField, "alpha", 1.0f, 0.0f);
					mNumberFieldSlideUp.setInterpolator(new AccelerateDecelerateInterpolator());				

					mSendButtonSlideUp = ObjectAnimator.ofFloat(mSendButton, "alpha", 1.0f, 0.0f);
					mSendButtonSlideUp.setInterpolator(new AccelerateDecelerateInterpolator());

					ObjectAnimator[] objectAnimators = new ObjectAnimator[] {mNumberFieldSlideUp, mSendButtonSlideUp};
					AnimatorSet animSetXY = new AnimatorSet();
					animSetXY.playTogether(objectAnimators);
					animSetXY.setDuration(1200);//1sec
					animSetXY.addListener(new AnimatorListener() {

						public void onAnimationStart(Animator arg0) { }
						public void onAnimationRepeat(Animator arg0) { }
						public void onAnimationCancel(Animator arg0) { }

						@Override
						public void onAnimationEnd(Animator arg0) {

							mNumberField.setVisibility(View.INVISIBLE);
							mSendButton.setVisibility(View.INVISIBLE);

							mLogin.setVisibility(View.VISIBLE);
							//							mSkip.setVisibility(View.VISIBLE);

							//							ObjectAnimator[] objectAnimators = new ObjectAnimator[] {mFacebookBtnFadeIn, mSkipBtnFadeIn};
							ObjectAnimator[] objectAnimators = new ObjectAnimator[] {mFacebookBtnFadeIn};
							AnimatorSet animSetXY = new AnimatorSet();
							animSetXY.playTogether(objectAnimators);
							animSetXY.setDuration(1200);//1sec
							animSetXY.start();
						}
					});
					animSetXY.start();
				}
				else{
					mStatusText.setText("MÃƒÂ£ xÃƒÂ¡c nhÃ¡ÂºÂ­n khÃƒÂ´ng hÃ¡Â»Â£p lÃ¡Â»â€¡");
					mNumberField.setText("");
				}

			} catch (JSONException e) {
				mStatusText.setText("MÃƒÂ£ xÃƒÂ¡c nhÃ¡ÂºÂ­n khÃƒÂ´ng hÃ¡Â»Â£p lÃ¡Â»â€¡");
				mNumberField.setText("");
			}
		}
		protected void onPreExecute(){ }
	}

	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);
	}

	public void confirmPhone(){

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setMessage(phoneNumber +"\nMÃƒÂ£ kÃƒÂ­ch hoÃ¡ÂºÂ¡t SmartGuide sÃ¡ÂºÂ½ Ã„â€˜Ã†Â°Ã¡Â»Â£c gÃ¡Â»Â­i Ã„â€˜Ã¡ÂºÂ¿n sÃ¡Â»â€˜ Ã„â€˜iÃ¡Â»â€¡n thoÃ¡ÂºÂ¡i trÃƒÂªn" +
				" qua tin nhÃ¡ÂºÂ¯n. ChÃ¡Â»ï¿½n Ã„ï¿½Ã¡Â»â€œng ÃƒÂ½ Ã„â€˜Ã¡Â»Æ’ tiÃ¡ÂºÂ¿p tÃ¡Â»Â¥c hoÃ¡ÂºÂ·c hÃ¡Â»Â§y Ã„â€˜Ã¡Â»Æ’ thay Ã„â€˜Ã¡Â»â€¢i sÃ¡Â»â€˜ Ã„â€˜iÃ¡Â»â€¡n thoÃ¡ÂºÂ¡i");
		builder.setCancelable(true);

		builder.setNegativeButton("Ã„ï¿½Ã¡Â»â€œng ÃƒÂ½", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mNumberField.getWindowToken(), 0);
				
				startCounting();
				
				m84TV.setVisibility(View.INVISIBLE);
				isConfirm = true;
				mStatusText.setText("ChÃ¡Â»ï¿½ vÃƒÂ  nhÃ¡ÂºÂ­p mÃƒÂ£ xÃƒÂ¡c nhÃ¡ÂºÂ­n...");
				mNumberField.setText("");
				new GetActivateCode().execute();
			}
		});

		builder.setPositiveButton("HÃ¡Â»Â§y", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				mStatusText.setText("NhÃ¡ÂºÂ­p sÃ¡Â»â€˜ Ã„â€˜iÃ¡Â»â€¡n thoÃ¡ÂºÂ¡i...");
				mNumberField.setText("");
			}
		});

		AlertDialog dialog = builder.show();
		TextView messageView = (TextView)dialog.findViewById(android.R.id.message);
		messageView.setGravity(Gravity.CENTER);
	}

	public String formatPhone(String phone){
		String result = "";

		if (phone.charAt(0) == '+')
			phone = phone.substring(1);

		if (phone.charAt(0) != '0')
			return "84" + phone;

		try{
			String first3c = phone.substring(0, 2);
			if (first3c.compareTo("84") == 0)
				return phone;
			else{
				return "84" + phone.substring(1);
			}
		}catch(Exception ex){
			return result;
		}
	}

	public boolean validatePhoneNumber(String phone){
		if (phone.length() != 11 && phone.length() != 12)
			return false;
		return true;
	}

	Timer timer;
	
	public void startCounting(){
		mTailText.setVisibility(View.VISIBLE);
		mHeadText.setVisibility(View.VISIBLE);
		mHeadText.setText("Bạn sẽ nhận được mã kích hoạt sau ");
		mTimeText.setVisibility(View.VISIBLE);
		
		timer = new Timer();
		timer.schedule(new TimerTask() {
			int i = 30;
			
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (i == - 1){
							mHeadText.setVisibility(View.VISIBLE);
							mHeadText.setText("Bạn chưa nhận được mã xác nhận?");
							mTailText.setVisibility(View.INVISIBLE);
							mTimeText.setVisibility(View.INVISIBLE);
							mResendCode.setVisibility(View.VISIBLE);
							i = 30;
							timer.cancel();
							return;
						}
							
						mTimeText.setText(Integer.toString(i--));
						
					}
				});
				
			}
		}, 0, 1000);
	}
}