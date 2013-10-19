package vn.smartguide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.telephony.PhoneNumberUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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

	// Data
	private boolean isConfirm = false;	
	private String phoneNumber = "";
	private String confirmCode = "";
	private String userID = "";
	private Intent resultData;

	// GUI elements
	private ImageView mLogo;
	private ImageView mSlogan;
	private ImageView mSmartGuide;
	private ImageButton mLogin;
	private ImageButton mSendButton;
	private EditText mNumberField;
	private TextView mStatusText;
	private TextView mHeadText;
	private TextView mTailText;
	private TextView mTimeText;
	private TextView m84TV;
	private Button mResendCode;

	private ObjectAnimator mNumberFieldSlideUp;
	private ObjectAnimator mSendButtonSlideUp;
	private ObjectAnimator mStatusTextFlash;

	private LoginButton authButton = null;

	private RelativeLayout signUpScreen;
	private RelativeLayout faceOrACCScreen;
	private RelativeLayout createACCScreen;
	private ImageButton viaCreateACCBtn;
	private RelativeLayout loadingFace;

	// Others
	private UiLifecycleHelper 	mUiHelper;
	private ImageButton mChangeAvatarBtn;
	private ImageView mAvatarView;
	private Button mDoneCreatACCBtn;
	private EditText mNameField;
	private String avatarURL = "";
	private String name;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wellcome);
		
		// Set up Facebook
		Session.StatusCallback callback = new Session.StatusCallback() {
			public void call(Session session, SessionState state, Exception exception) {
					
				if (state.isOpened()) {
					mStatusText.setText("Vui lòng chờ cập nhật thông tin...");
					makeMeRequest(session);
				} else if (state.isClosed()) {
				}
			}
		};

		mUiHelper = new UiLifecycleHelper(this, callback);
		mUiHelper.onCreate(savedInstanceState);

		resultData = new Intent();
		resultData.putExtra("GOAHEAD", "OK");

		// Get GUI elements
		mLogo = (ImageView)findViewById(R.id.logo);
		mSlogan = (ImageView)findViewById(R.id.slogan);
		mSmartGuide = (ImageView)findViewById(R.id.smartguide);
		mNumberField = (EditText)findViewById(R.id.numberField);
		mSendButton = (ImageButton)findViewById(R.id.sendCodeButton);
		mStatusText = (TextView)findViewById(R.id.statusTextView);
		m84TV = (TextView) findViewById(R.id.m84TV);
		mTimeText = (TextView) findViewById(R.id.timeLeftTV);
		mHeadText = (TextView) findViewById(R.id.headTV);
		mTailText = (TextView) findViewById(R.id.tailTV);
		mResendCode = (Button) findViewById(R.id.resendCodeBtn);

		mLogin = (ImageButton)findViewById(R.id.viaFaceButton);

		mStatusTextFlash = ObjectAnimator.ofFloat(mStatusText, "alpha", 0.3f, 1.0f);
		mStatusTextFlash.setInterpolator(new LinearInterpolator());
		mStatusTextFlash.setDuration(1000);
		mStatusTextFlash.setRepeatCount(Animation.INFINITE);
		mStatusTextFlash.setRepeatMode(Animation.REVERSE);
		mStatusTextFlash.start();

		// Set send button event
		mSendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isConfirm == false) {
					phoneNumber = formatPhone(mNumberField.getText().toString());
					if (PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber) && validatePhoneNumber(phoneNumber)) {
						if (phoneNumber.charAt(0) == '+'){
							String subphone = phoneNumber.substring(1);
							phoneNumber = subphone;
						}

						confirmPhone();
					} else {
						mStatusText.setText("Số điện thoại không hợp lệ...");
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
				mLogin.setClickable(false);
				viaCreateACCBtn.setClickable(false);
				loadingFace.setVisibility(View.VISIBLE);
				authButton.performClick();
			}
		});

		mResendCode.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mNumberField.getWindowToken(), 0);

				startCounting();

				mResendCode.setVisibility(View.INVISIBLE);
				m84TV.setVisibility(View.INVISIBLE);
				isConfirm = true;
				mStatusText.setText("Chờ và nhập mã xác nhận...");
				mNumberField.setText("");

				new GetActivateCode().execute();
			}
		});

		signUpScreen = (RelativeLayout)findViewById(R.id.signupScreen);
		faceOrACCScreen = (RelativeLayout)findViewById(R.id.faceOrAccountScreen);
		createACCScreen = (RelativeLayout)findViewById(R.id.createAccountScreen);
		viaCreateACCBtn = (ImageButton)findViewById(R.id.viaCreateACC);
		viaCreateACCBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				faceOrACCScreen.setVisibility(View.GONE);
				createACCScreen.setVisibility(View.VISIBLE);
			}
		});

		loadingFace = (RelativeLayout)findViewById(R.id.loadingFace);
		mChangeAvatarBtn = (ImageButton)findViewById(R.id.changeAvaBtn);
		mChangeAvatarBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {   
				Context context = WellcomeActivity.this;
				AlertDialog.Builder builder = new AlertDialog.Builder(context); 
				
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);   
				View layout = inflater.inflate(R.layout.avatar_dialog, (ViewGroup) findViewById(R.id.layout_root));   
				GridView gridview = (GridView) layout.findViewById(R.id.avatar_list);   
				gridview.setAdapter(new ImageAdapter(WellcomeActivity.this));   
				gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {   
					public void onItemClick(AdapterView<?> parent, View v, int position, long id) {   
						dialog.dismiss();
						avatarURL = GlobalVariable.mAvatarList.get(position);
						GlobalVariable.cyImageLoader.showImage(GlobalVariable.mAvatarList.get(position), mAvatarView);					}   
				});

				    
				builder.setView(layout);     
				dialog = builder.create();
				dialog.show(); 
			}
		});
		
		mAvatarView = (ImageView) findViewById(R.id.avatar);
		mDoneCreatACCBtn = (Button)findViewById(R.id.doneCreateACCBtn);
		mDoneCreatACCBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {	
				name = mNameField.getText().toString().trim();
				if (name.length() == 0 || avatarURL.length() == 0) {
					AlertDialog.Builder builder = new AlertDialog.Builder(WellcomeActivity.this);

					if (name.length() == 0)
						builder.setMessage("Bạn cần nhập tên");
					else 
						builder.setMessage("Bạn cần chọn ảnh đại diện");
						
					builder.setCancelable(true);

					builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
						}
					});

					AlertDialog dialog = builder.show();
					TextView messageView = (TextView)dialog.findViewById(android.R.id.message);
					messageView.setGravity(Gravity.CENTER);
					return;
				}
				
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mNumberField.getWindowToken(), 0);
				
				new UpdateUserInfo().execute();
			}
		});
		
		mNameField = (EditText)findViewById(R.id.nameField);
	}

	Dialog dialog;
	
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

	public void exit() {
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

						loadingFace.setVisibility(View.INVISIBLE);

						HashMap<String, String> token2 = new HashMap<String, String>();
						token2.put("activateID", confirmCode);
						token2.put("userID", GlobalVariable.userID);
						token2.put("phoneNumber", phoneNumber);
						token2.put("avatar", "http://graph.facebook.com/" + GlobalVariable.id + "/picture?type=large");
						token2.put("nameFace", GlobalVariable.name);
						GlobalVariable.smartGuideDB.insertActivateCode(token2);

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

	public void confirmPhone() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setMessage("(+84)" + phoneNumber.substring(2) +"\nMã kích hoạt SmartGuide sẽ được gửi đến số điện thoại trên" +
				" qua tin nhắn. Chọn Đồng ý để tiếp tục hoặc hủy để thay đổi số điện thoại");
		builder.setCancelable(true);

		builder.setNegativeButton("Đồng ý", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mNumberField.getWindowToken(), 0);

				startCounting();

				m84TV.setVisibility(View.INVISIBLE);
				isConfirm = true;
				mStatusText.setText("Chờ và nhập mã xác nhận...");
				mNumberField.setText("");
				new GetActivateCode().execute();
			}
		});

		builder.setPositiveButton("Hủy", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				mStatusText.setText("Nhập số điện thoại...");
				mNumberField.setText("");
			}
		});

		AlertDialog dialog = builder.show();
		TextView messageView = (TextView)dialog.findViewById(android.R.id.message);
		messageView.setGravity(Gravity.CENTER);
	}

	public String formatPhone(String phone) {

		if (phone.charAt(0) == '+')
			phone = phone.substring(1);

		if (phone.charAt(0) != '0')
			return "84" + phone;

		try {
			String first3c = phone.substring(0, 2);
			if (first3c.compareTo("84") == 0)
				return phone;
			else
				return "84" + phone.substring(1);
		} catch (Exception ex) {
			return "";
		}
	}

	public boolean validatePhoneNumber(String phone) {
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
	
	///////////////////////////////////////////////////////////////////////////
	// Network AsyncTask
	///////////////////////////////////////////////////////////////////////////
	
	private class GetActivateCode extends AsyncTask<Void, Void, Boolean> {

		private Exception mEx;

		protected void onPreExecute(){ }

		@Override
		protected Boolean doInBackground(Void... params) {
			
			try {
				String json = NetworkManger.get_throw(GlobalVariable.urlGetActivateCode + phoneNumber, false);
			} catch (Exception e) {
				mEx = e;
			}
			return true;
		}

		protected void onPostExecute(Boolean k) { 
			if (mEx == null) {
				isConfirm = true;
				mStatusText.setText("Chờ và nhập mã xác nhận...");
				mNumberField.setText("");
			} else {
				GlobalVariable.showToast("Không thể gởi mã xác nhận", WellcomeActivity.this);
				mNumberField.setText("");
			}
		}
	}
	
	private class ConfirmActivateCode extends AsyncTask<Void, Void, Boolean> {

		private JSONObject result;
		private Exception mEx;

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				// Check activate code
				String mJson = NetworkManger.get_throw(GlobalVariable.urlChekcActivateCode + phoneNumber + "&code=" + confirmCode, false);
				result = new JSONObject(mJson);
			} catch (Exception e) {
				mEx = e;
			}
			return true;
		}

		protected void onPostExecute(Boolean k) {
			try {
				if (mEx != null)
					throw mEx;

				boolean success = result.getBoolean("result");
				GlobalVariable.userID = result.getString("user_id");
				boolean connect_fb = result.getBoolean("connect_fb");
				String name = "";
				if (!result.isNull("name"))
					name = result.optString("name");

				if (success) {
					GlobalVariable.footerURL = "&phone=" + phoneNumber + "&code=" + confirmCode;
					GlobalVariable.getTokenFromDB();
					
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(mNumberField.getWindowToken(), 0);

					if (connect_fb == true || (result.optString("avatar").length() != 0 && name.length() != 0)) {

						HashMap<String, String> token2 = new HashMap<String, String>();

						token2.put("activateID", confirmCode);
						token2.put("userID", GlobalVariable.userID);
						token2.put("phoneNumber", phoneNumber);
						token2.put("avatar", result.optString("avatar"));
						token2.put("nameFace", name);

						GlobalVariable.smartGuideDB.insertActivateCode(token2);
						
						HashMap<String, String> token = new HashMap<String, String>();
						token.put("userID", "-1");
						token.put("avatar",result.optString("avatar"));
						token.put("name",name);

						GlobalVariable.smartGuideDB.insertFacebook(token);

						exit();
						return;
					}

					signUpScreen.setVisibility(View.GONE);
					faceOrACCScreen.setVisibility(View.VISIBLE);
					new GetDefaultAvatar().setDisableView(mChangeAvatarBtn).execute();
				} else {
					mStatusText.setText("Mã xác nhận không hợp lệ");
					mNumberField.setText("");
				}

			} catch (Exception e) {
				GlobalVariable.showToast("Không thể xác nhận được", WellcomeActivity.this);
				mNumberField.setText("");
			}
		}
	}
	
	public static class GetDefaultAvatar extends CyAsyncTask {
		private String JSResult = null;
		private Exception mEx;
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
//			pairs.add(new BasicNameValuePair("name", name));
			JSResult = NetworkManger.post_throw(APILinkMaker.mGetDefaultAvatar(), pairs);
			if (JSResult == "")
				return false; //for test
			} catch (Exception e) {
				mEx = e;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Object k) {
			super.onPostExecute(k);
			try{
				if (mEx != null)
					throw mEx;

				JSONArray arrayImage = new JSONArray(JSResult);
				GlobalVariable.mAvatarList = new ArrayList<String>();
				for(int i = 0; i < arrayImage.length(); i++){
					GlobalVariable.mAvatarList.add(arrayImage.getString(i));
				}
			}catch(Exception ex){

			}
		}
	}
	
	private class UpdateUserInfo extends AsyncTask<Void, Void, Boolean> {
		String JSResult = null;
		@Override
		protected Boolean doInBackground(Void... params) {
			
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("name", name));
			if (avatarURL == "" || avatarURL.length() == 0)
				avatarURL = GlobalVariable.avatarFace;
			
			pairs.add(new BasicNameValuePair("avatar", avatarURL));
			JSResult = NetworkManger.post(APILinkMaker.mUpdateUserInfo(), pairs);
			if (JSResult == "")
				return false; 
			return true;
		}

		@Override
		protected void onPostExecute(Boolean k) {
			if (k == true){
				try{
					JSONObject result = new JSONObject(JSResult);
					int code = result.getInt("code");
					if (code == 1){
						HashMap<String, String> token2 = new HashMap<String, String>();

						token2.put("activateID", confirmCode);
						token2.put("userID", GlobalVariable.userID);
						token2.put("phoneNumber", phoneNumber);
						token2.put("avatar", avatarURL);
						token2.put("nameFace", name);

						GlobalVariable.smartGuideDB.insertActivateCode(token2);
						
						HashMap<String, String> token =  new  HashMap<String, String>();
						token.put("userID", "-1");
						token.put("avatar",avatarURL);
						token.put("name",name);

						GlobalVariable.smartGuideDB.insertFacebook(token);
						
						loadingFace.setVisibility(View.GONE);
						exit();
					}
				}catch(Exception ex){

				}
			} else {
				loadingFace.setVisibility(View.GONE);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(WellcomeActivity.this);

				builder.setMessage("Có lỗi xảy ra vui lòng thử lại");
				builder.setCancelable(true);

				builder.setNegativeButton("Đồng ý", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						
					}
				});
				
				AlertDialog dialog = builder.show();
				TextView messageView = (TextView)dialog.findViewById(android.R.id.message);
				messageView.setGravity(Gravity.CENTER);
			}
		}

		@Override
		protected void onPreExecute() {
			
		}
	}
}