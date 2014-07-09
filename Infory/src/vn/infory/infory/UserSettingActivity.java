package vn.infory.infory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONObject;

import vn.infory.infory.FlashActivity.Listener;
import vn.infory.infory.login.InforyLoginActivity;
import vn.infory.infory.data.Profile;
import vn.infory.infory.data.Settings;
import vn.infory.infory.login.AvaDialogActivity;
import vn.infory.infory.network.CyAsyncTask;
import vn.infory.infory.network.GetProfile;
import vn.infory.infory.network.NetworkManager;
import vn.infory.infory.network.UpdateProfile;
import vn.infory.infory.network.UploadAva;
import vn.infory.infory.network.UploadSocialProfile;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;
import com.cycrix.jsonparser.JsonParser;
import com.facebook.LoginActivity;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionLoginBehavior;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.PlusClient.OnAccessRevokedListener;
import com.google.android.gms.plus.model.people.Person;

public class UserSettingActivity extends Activity implements 
ConnectionCallbacks, OnConnectionFailedListener{
	
	public static final int REQUEST_CODE_RESOLVE_ERR = 9000;
	
	private static Listener sListener;
	private Listener mListener;

	// GUI
	@ViewById(id = R.id.imgAva)				private ImageView mImgAva;
	@ViewById(id = R.id.edtUserName)		private EditText mEdtUserName;
	@ViewById(id = R.id.txtDob)				private TextView mTxtDob;
	@ViewById(id = R.id.txtSex)				private TextView mTxtSex;
	@ViewById(id = R.id.layoutLoading)		private View mLayoutLoading;
	@ViewById(id = R.id.layoutLoadingAni)	private View mLayoutLoadingAni;
	@ViewById(id = R.id.fbBtn) 				private LoginButton mFacebookButton;
	@ViewById(id = R.id.btnFacebook)		private View mBtnFacebook;
	@ViewById(id = R.id.btnGooglePlus)		private View mBtnGooglePlus;

	// Data
	private String mUserName;
	private String mAvaUrl;
	private String mAvaPath;
	private String mDob;
	private int mSex;
	private List<CyAsyncTask> mTaskList = new ArrayList<CyAsyncTask>();

	// Facebook
	private UiLifecycleHelper mUiHelper;

	// Google Plus
	private PlusClient mPlusClient;
	private ConnectionResult mConnectionResult;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mListener = sListener;
		sListener = null;
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_setting);

		try {
			AndroidAnnotationParser.parse(this, findViewById(android.R.id.content));
		} catch (Exception e) {
			e.printStackTrace();
			finish();
			return;
		}

		FontsCollection.setFont(findViewById(android.R.id.content));

		Settings s = Settings.instance();
		mUserName	= s.name;
		mAvaUrl		= s.avatar;
		mDob		= s.dob;
		mSex		= s.gender;

		CyImageLoader.instance().loadImage(mAvaUrl, new CyImageLoader.Listener() {
			@Override
			public void loadFinish(int from, Bitmap image, String url, CyAsyncTask task) {
				mImgAva.setImageBitmap(SGSideMenu.getCroppedBitmap(image));
			}
		}, new Point(), this);

		mImgAva.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AvaDialogActivity.newInstance(UserSettingActivity.this,
						new AvaDialogActivity.Listener() {
					@Override
					public void onAvaSelect(String url) {
						mAvaUrl = url;
						mAvaPath = null;
						CyImageLoader.instance().loadImage(url, 
								new CyImageLoader.Listener() {
							@Override
							public void loadFinish(int from, Bitmap image, String url, CyAsyncTask task) {
								mImgAva.setImageBitmap(SGSideMenu.getCroppedBitmap(image));
							}
						}, new Point(), UserSettingActivity.this);
					}

					@Override
					public void onAvaSelectFile(String path, Bitmap bitmap) {
						mAvaPath = path;
						mAvaUrl = null;
						mImgAva.setImageBitmap(SGSideMenu.getCroppedBitmap(bitmap));
					}
				});
			}
		});

		mEdtUserName.setText(s.name);
		mTxtDob.setText(mDob);
		mTxtSex.setText(Settings.getSex(mSex));
		
		if (Settings.instance().socialType != 0) {
			mBtnFacebook.setVisibility(View.INVISIBLE);
			mBtnGooglePlus.setVisibility(View.INVISIBLE);
		}

		// Setup facebook
		mUiHelper = new UiLifecycleHelper(this, new Session.StatusCallback() {
			@Override
			public void call(Session session, SessionState state, Exception exception) {
				callback.call(session, state, exception);
			}
		});
		mUiHelper.onCreate(savedInstanceState);

		mFacebookButton.setReadPermissions(Arrays.asList("user_birthday"));
		mFacebookButton.setLoginBehavior(SessionLoginBehavior.SUPPRESS_SSO);

		// Setup Google+
		mPlusClient = new PlusClient.Builder(this, this, this)
		//        .setActions("http://schemas.google.com/AddActivity")
		//				.setScopes(Scopes.PLUS_LOGIN)  // recommended login scope for social features
		.setScopes(Scopes.PLUS_PROFILE)       // alternative basic login scope
		.build();
		
		AnimationDrawable frameAnimation = (AnimationDrawable) 
				mLayoutLoadingAni.getBackground();
		frameAnimation.start();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mUiHelper.onDestroy();

		for (CyAsyncTask task : mTaskList)
			task.cancel(true);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == REQUEST_CODE_RESOLVE_ERR && resultCode == RESULT_OK) {
			mPlusClient.connect();
		} else {
			mUiHelper.onActivityResult(requestCode, resultCode, intent);
		}
	}

	public static void newInstance(Activity act) {
		Intent intent = new Intent(act, UserSettingActivity.class);
		act.startActivity(intent);
	}

	@Click(id = R.id.btnBack)
	private void onBackClick(View v) {
		finish();
	}
	
	@Click(id = R.id.txtPolicy)
	private void onPolicyClick(View v) {
		WebActivity.newInstance(this, "http://infory.vn/dieu-khoan-nguoi-dung.html");
	}

	@Override
	public void finish() {
		super.finish();
	}

	@Click(id = R.id.btnSexEdit)
	private void onSexEdit(View v) {
		AlertDialog.Builder builder = new Builder(this);
		builder.setItems(new String[] {"Nữ", "Nam"}, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mSex = which;
				mTxtSex.setText(Settings.getSex(mSex));
			}
		});
		builder.setNegativeButton("Cancel", null);
		builder.show();
	}

	DatePickerDialog dlg;
	@Click(id = R.id.btnDobEdit)
	private void onDobEdit(View v) {

		final SimpleDateFormat dateFormat= new SimpleDateFormat(CyUtils.DOB_FORMAT, Locale.US);
		Date dob = null;
		try {
			dob = dateFormat.parse(mDob);
		} catch (ParseException e) {
			try {
				dob = dateFormat.parse("01/01/1990");
			} catch (ParseException e1) {}
		}

		int d = dob.getDate();
		int m = dob.getMonth();
		int y = dob.getYear(); 

		dlg = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				mDob = dateFormat.format(new Date(year-1900, monthOfYear, dayOfMonth));
				mTxtDob.setText(mDob);
			}
		}, y+1900, m, d) {
			@Override
			public void onDateChanged(DatePicker view, int year, int month, int day) {
				dlg.setTitle(makeVNDate(day, month, year-1900));
			}
		};
		dlg.getDatePicker().setMaxDate(new Date().getTime());
		dlg.setTitle(makeVNDate(d, m, y));
		dlg.show();
	}

	private String makeVNDate(int day, int month, int year) {
		return new StringBuilder()
		.append("ngày ").append(day)
		.append(" tháng ").append(month+1)
		.append(" năm ").append(year+1900)
		.toString();
	}

	@Click(id = R.id.btnDone)
	private void onDoneClick(View v) {
		// Check difference
		Settings s = Settings.instance();
		mUserName = mEdtUserName.getText().toString().trim();
		if (!s.avatar.equals(mAvaUrl)
				|| mAvaPath != null
				|| !mUserName.equals(s.name)
				|| !mDob.equals(s.dob)
				|| mSex != s.gender) {

			// Update

			// If chose ava from device, upload ava first
			if (mAvaPath != null) {
				uploadAva();
			} else {
				updataProfile();
			}
		} else {
			finish();
		}
	}
	
	@Click(id = R.id.btnFacebook)
	private void onFacebookClick(View v) {
		callback = callback2;
		mFacebookButton.performClick();
	}
	
	@Click(id = R.id.btnGooglePlus)
	private void onGooglePlusClick(View v) {
		if (mConnectionResult == null) {
			//            mConnectionProgressDialog.show();
		} else {
			try {
				mConnectionResult.startResolutionForResult(this, REQUEST_CODE_RESOLVE_ERR);
			} catch (SendIntentException e) {
				// Try connecting again.
				mConnectionResult = null;
				mPlusClient.connect();
			}
		}
	}
	
	@Click(id = R.id.btnLogout)
	private void onLogoutClick(View v) {
		Settings.instance().logout();
		
		getProfileFromServer();
		InforyLoginActivity.newInstance(this, mListener);
		overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top);
	}

	private void uploadAva() {
		UploadAva uploadAvaTask = new UploadAva(this, mAvaPath) {
			@Override
			protected void onCompleted(Object result) {
				mTaskList.remove(this);

				updataProfile();
			}

			@Override
			protected void onFail(Exception e) {
				mTaskList.remove(this);

				CyUtils.showError("Tải lên ảnh đại diện thất bại", mEx, UserSettingActivity.this);
			}
		};
		mTaskList.add(uploadAvaTask);
		uploadAvaTask.setVisibleView(mLayoutLoading);
		uploadAvaTask.executeOnExecutor(NetworkManager.THREAD_POOL);
	}

	private void updataProfile() {
		final SimpleDateFormat dateFormat= new SimpleDateFormat(CyUtils.DOB_FORMAT, Locale.US);
		int day = 0;
		int month = 0;
		int year = 0;
		 
		try {
			Date dob = dateFormat.parse(mDob);
			day = dob.getDate();
			month = dob.getMonth() + 1;
			year = dob.getYear() + 1900;
		} catch (ParseException e1) {}

		UpdateProfile updateProfileTask = new UpdateProfile(UserSettingActivity.this, 
				mUserName, mAvaUrl, day, month, year, mSex, Settings.instance().socialType) {
			@Override
			protected void onCompleted(Object result2) throws Exception {
				mTaskList.remove(this);

				JSONObject result = (JSONObject) result2;

				if (result.getInt("status") == 1) {
					getProfileFromServer();
				} else {
					CyUtils.showError(result.getString("message"), null, UserSettingActivity.this);
				}
			}

			@Override
			protected void onFail(Exception e) {
				mTaskList.remove(this);

				CyUtils.showError("Cập nhật thông tin thất bại", mEx, UserSettingActivity.this);
			}
		};

		mTaskList.add(updateProfileTask);
		updateProfileTask.setVisibleView(mLayoutLoading);

		updateProfileTask.executeOnExecutor(NetworkManager.THREAD_POOL);
	}

	private void getProfileFromServer() {
		GetProfile getProfileTask = new GetProfile(this) {
			@Override
			protected void onCompleted(Object result2) {
				mTaskList.remove(this);

				Profile profile = (Profile) result2;
				saveSetting(profile);
				finish();
			}

			@Override
			protected void onFail(Exception e) {
				mTaskList.remove(this);

				CyUtils.showError("Lấy thông tin thất bại", mEx, UserSettingActivity.this);
			}
		};
		mTaskList.add(getProfileTask);
		getProfileTask.setVisibleView(mLayoutLoading);
		getProfileTask.executeOnExecutor(NetworkManager.THREAD_POOL);
	}

	private void saveSetting(Profile profile) {
		Settings s = Settings.instance();

		s.phoneNumber= profile.phone;
		s.avatar 	= profile.avatar;
		s.name 		= profile.name;
		s.userID	= "" + profile.idUser;
		s.gender	= profile.gender;
		s.cover		= profile.cover;
		s.dob		= profile.dob;
		s.socialType= profile.socialType;

		s.save();
		s.notifyDataChange();
	}

	///////////////////////////////////////////////////////////////////////////
	// Facebook stuff
	///////////////////////////////////////////////////////////////////////////

	// Facebook callback
		Session.StatusCallback callback2 = new Session.StatusCallback() {
			public void call(Session session, SessionState state, Exception exception) {
				if (state == SessionState.CLOSED_LOGIN_FAILED) {
					showLoginFbFailDlg();
					mLayoutLoading.setVisibility(View.INVISIBLE);
				} else if (state.isOpened()) {
					makeMeRequest(session);
				}
			}
		};
		
		Session.StatusCallback callback1 = new Session.StatusCallback() {
			public void call(Session session, SessionState state, Exception exception) {
				if (state.isOpened())
					session.closeAndClearTokenInformation();
			}
		};
		
		Session.StatusCallback callback = callback1;

	private void showLoginFbFailDlg() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Đăng nhập facebook thất bại, " +
				"vui lòng đăng nhập lại hoặc tạo tài khoản mới");
		builder.setPositiveButton("OK", null);
		builder.create().show();
	}

	private void makeMeRequest(final Session session) {
		Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {

			public void onCompleted(GraphUser user, Response response) {
				mLayoutLoading.setVisibility(View.INVISIBLE);
				if (session == Session.getActiveSession()) {
					if (user != null) {
						// Update info to server
						String json = response.getGraphObject().getInnerJSONObject().toString();
						uploadSocialProfile(json, session.getAccessToken(), 1);
					}
				}

				if (response.getError() != null) {
					showLoginFbFailDlg();
				}
			}
		});

		Bundle bundle = new Bundle();
		bundle.putString("fields", "name,address,birthday,email,gender,id,middle_name,first_name,last_name,work,picture");
		request.setParameters(bundle);

		mLayoutLoading.setVisibility(View.VISIBLE);
		request.executeAsync();
	}

	private void uploadSocialProfile(String profile, String accessToken, int type) {
		UploadSocialProfile uploadTask = new UploadSocialProfile(
				UserSettingActivity.this, profile, accessToken, type) {
			@Override
			protected void onCompleted(Object result2) throws Exception {
				mTaskList.remove(this);

				JSONObject result = (JSONObject) result2;

				if (result.getInt("status") == 1) {
					Profile p = new Profile();
					JsonParser.parseObject(p, result.getJSONObject("profile"));
					saveSetting(p);
					finish();
				} else {
					CyUtils.showError(result.getString("message"), null, UserSettingActivity.this);
				}
			}

			@Override
			protected void onFail(Exception e) {
				mTaskList.remove(this);

				CyUtils.showError("Cập nhật thông tin thất bại!", mEx, UserSettingActivity.this);
			}
		};
		mTaskList.add(uploadTask);
		uploadTask.setVisibleView(mLayoutLoading);
		uploadTask.executeOnExecutor(NetworkManager.THREAD_POOL);
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Google+ stuff
	///////////////////////////////////////////////////////////////////////////
	
	@Override
	public void onConnected(Bundle arg0) {
//		CyUtils.showToast("Google+ connected", this);
		registerCallback();

		mPlusClient.clearDefaultAccount();
		mPlusClient.revokeAccessAndDisconnect(new OnAccessRevokedListener() {

			@Override
			public void onAccessRevoked(ConnectionResult arg0) {
//				CyUtils.showToast("Google+ Access revoked", LoginActivity.this);

				mPlusClient.connect();
			}
		});
	}

	@Override
	public void onDisconnected() {
//		CyUtils.showToast("Google+ disconnected", this);
	}
	
	private void registerCallback() {
		mPlusClient.unregisterConnectionCallbacks(this);
		mPlusClient.registerConnectionCallbacks(new ConnectionCallbacks() {

			@Override
			public void onDisconnected() {

			}

			@Override
			public void onConnected(Bundle arg0) {
				StringBuilder builder = new StringBuilder();
				Person currentPerson = mPlusClient.getCurrentPerson();
				if (currentPerson == null)
					return;
				
				String profile = currentPerson.toString();
				uploadSocialProfile(profile, "", 2);
			}
		});
	}
	
	@Override
	public void onConnectionFailed(ConnectionResult result) {
//		CyUtils.showToast("Google+ connect failed", this);
		mConnectionResult = result;
		registerCallback();
	}

	@Override
	protected void onStart() {
		super.onStart();
		mPlusClient.connect();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		mPlusClient.disconnect();
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
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mUiHelper.onSaveInstanceState(outState);
	}
}