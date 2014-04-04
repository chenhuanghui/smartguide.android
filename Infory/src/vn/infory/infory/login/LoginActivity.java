package vn.infory.infory.login;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import vn.infory.infory.CyUtils;
import vn.infory.infory.R;
import vn.infory.infory.FlashActivity.Listener;
import vn.infory.infory.data.Profile;
import vn.infory.infory.data.Settings;
import vn.infory.infory.network.CyAsyncTask;
import vn.infory.infory.network.GetProfile;
import vn.infory.infory.network.NetworkManager;
import vn.infory.infory.network.UpdateProfile;
import vn.infory.infory.network.UploadAva;
import vn.infory.infory.network.UploadSocialProfile;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.ViewById;
import com.cycrix.jsonparser.JsonParser;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
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

public class LoginActivity extends FragmentActivity implements 
ConnectionCallbacks, OnConnectionFailedListener {
	
	public static final int REQUEST_CODE_RESOLVE_ERR = 9000;
	private static Listener sListener;

	// data
	private Listener mListener = new Listener();

	private BackListener[] mBackFragArr = new BackListener[3];
	private BackListener mActiveFrag;

	private List<CyAsyncTask> mTaskList = new ArrayList<CyAsyncTask>();

	// Google Plus
	private PlusClient mPlusClient;
	private ConnectionResult mConnectionResult;

	// Facebook
	private UiLifecycleHelper mUiHelper;

	// GUI elements
	@ViewById(id = R.id.pagerMain)			private ViewPager mPager;
	@ViewById(id = R.id.layoutLoading)		private View mLayoutLoading;
	@ViewById(id = R.id.layoutLoadingAni)	private View mLayoutLoadingAni;
	@ViewById(id = R.id.fbBtn) 				private LoginButton mFacebookButton;

	private FragmentPagerAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		mListener = sListener;
		sListener = null;

		

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		try {
			AndroidAnnotationParser.parse(this, findViewById(android.R.id.content));
		} catch (Exception e) {
			e.printStackTrace();
			finish();
		}

		mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {

			@Override
			public int getCount() {
				return 3;
			}

			@Override
			public Fragment getItem(int pos) {

				switch (pos) {
				case 0: {
					TelephoneFragment frag = new TelephoneFragment();
					mBackFragArr[0] = frag;
					mActiveFrag = frag;
					frag.setListener(new TelephoneFragment.Listener() {
						
						@Override
						public void onBackPress() {
							finish();
						}

						@Override
						public void onSuccess() {
							mPager.setCurrentItem(1);
						}

						@Override
						public void onLoginSuccess(JSONObject result) {
							finish();
							mListener.onSuccess();
						}						
					});
					return frag;
				}
				case 1: {
					RegisterTypeFragment frag = new RegisterTypeFragment();
					mBackFragArr[1] = frag;
					mActiveFrag = frag;
					frag.setListener(new RegisterTypeFragment.Listener() {

						@Override
						public void onBackClick() {
							mPager.setCurrentItem(0);
						}

						@Override
						public void onRegisterClick() {
							mPager.setCurrentItem(2);
						}

						@Override
						public void onGooglePlusClick() {
							LoginActivity.this.onGooglePlusClick();
						}
						
						@Override
						public void onFacebookClick() {
							callback = callback2;
						}
					});
					return frag;
				}
				case 2: {
					RegisterFragment frag = new RegisterFragment();
					mBackFragArr[2] = frag;
					mActiveFrag = frag;
					frag.setListener(new RegisterFragment.Listener() {

						@Override
						public void onBackClick() {
							mPager.setCurrentItem(1);
						}

						@Override
						public void onRegisterClick(Object[] result1, int[] result2) {
							LoginActivity.this.onRegisterClick(result1, result2);
						}
					});
					return frag;
				}
				}
				return null;
			}

			@Override
			public CharSequence getPageTitle(int position) {
				return "page";
			}
		};
		mPager.setAdapter(mAdapter);
		mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			
			private int mPrePos = 0;
			@Override
			public void onPageSelected(int position) {
				
				switch (position) {
				case 0: 
					((TelephoneFragment) mBackFragArr[0]).reset();
					break;
				case 1: 
					if (mBackFragArr[mPrePos] instanceof TelephoneFragment)
						((TelephoneFragment) mBackFragArr[mPrePos]).hideSoftKeyboard();
					else if (mBackFragArr[mPrePos] instanceof RegisterFragment)
						((RegisterFragment) mBackFragArr[mPrePos]).hideSoftKeyboard();
					break;
				case 2: 
					((RegisterFragment) mBackFragArr[2]).showSoftKeyboard();
					break;
				}
				
				mPrePos = position;
			}
		});

		// Setup facebook
		mUiHelper = new UiLifecycleHelper(this, new Session.StatusCallback() {
			@Override
			public void call(Session session, SessionState state, Exception exception) {
				callback.call(session, state, exception);
			}
		});
		mUiHelper.onCreate(savedInstanceState);

		// Setup Google+
		mPlusClient = new PlusClient.Builder(this, this, this)
		//        .setActions("http://schemas.google.com/AddActivity")
//		.setScopes(Scopes.PLUS_LOGIN)  // recommended login scope for social features
		 .setScopes(Scopes.PLUS_PROFILE)       // alternative basic login scope
		.build();
		
		((AnimationDrawable) mLayoutLoadingAni.getBackground()).start();
	}
	
	public static void newInstance(Activity act, Listener listener) {
		sListener = listener;
		sFromUseImmediate = act instanceof UseImmediatelyActivity;
			
		Intent intent = new Intent(act, LoginActivity.class);
		act.startActivity(intent);
	}
	
	private static boolean sFromUseImmediate = false;
	@Override
	public void finish() {
		super.finish();
		if (sFromUseImmediate)
			overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == REQUEST_CODE_RESOLVE_ERR && resultCode == RESULT_OK) {
//			mFragRegister.onActivityResult();
			mPlusClient.connect();
		} else {
			mUiHelper.onActivityResult(requestCode, resultCode, intent);
		}
	}

	@Override
	public void onBackPressed() {
		mBackFragArr[mPager.getCurrentItem()].onBackPress();
	}

	///////////////////////////////////////////////////////////////////////////
	// Self register stuff
	///////////////////////////////////////////////////////////////////////////

	private void onRegisterClick(Object[] result1, int[] result2) {
		String username = (String) result1[0];
		String avaUrl	= (String) result1[1];
		final String avaFilePath = (String) result1[2];

		int day = result2[0];
		int month = result2[1];
		int year = result2[2];
		int sex = result2[3];

		UpdateProfile updateProfileTask = new UpdateProfile(LoginActivity.this, 
				getTempAccessToken(), username, avaUrl, day, month, year, sex, 0) {
			@Override
			protected void onCompleted(Object result2) throws Exception {
				mTaskList.remove(this);

				JSONObject result = (JSONObject) result2;

				if (result.getInt("status") == 1) {
					if (avaFilePath != null)
						uploadAva(avaFilePath);
					else
						getProfileFromServer();
				} else {
					CyUtils.showError(result.getString("message"), null, LoginActivity.this);
				}
			}

			@Override
			protected void onFail(Exception e) {
				mTaskList.remove(this);

				CyUtils.showError("Cập nhật thông tin thất bại", mEx, LoginActivity.this);
			}
		};

		mTaskList.add(updateProfileTask);
		updateProfileTask.setVisibleView(mLayoutLoading);
		updateProfileTask.executeOnExecutor(NetworkManager.THREAD_POOL);
	}

	private void uploadAva(String avaFilePath) {
		UploadAva uploadAvaTask = new UploadAva(this, getTempAccessToken(), avaFilePath) {
			@Override
			protected void onCompleted(Object result) {
				mTaskList.remove(this);

				getProfileFromServer();
			}

			@Override
			protected void onFail(Exception e) {
				mTaskList.remove(this);

				CyUtils.showError("Tải lên ảnh đại diện thất bại", mEx, LoginActivity.this);
			}
		};
		mTaskList.add(uploadAvaTask);
		uploadAvaTask.setVisibleView(mLayoutLoading);
		uploadAvaTask.executeOnExecutor(NetworkManager.THREAD_POOL);
	}

	private void getProfileFromServer() {
		TelephoneFragment teleFrag = (TelephoneFragment) mBackFragArr[0];
		GetProfile getProfileTask = new GetProfile(this, teleFrag.getProfile().optString("accessToken")) {
			@Override
			protected void onCompleted(Object result2) {
				mTaskList.remove(this);

				Profile profile = (Profile) result2;
				saveSetting(profile);
				finish();
				mListener.onSuccess();
			}

			@Override
			protected void onFail(Exception e) {
				mTaskList.remove(this);

				CyUtils.showError("Lấy thông tin thất bại", mEx, LoginActivity.this);
			}
		};
		mTaskList.add(getProfileTask);
		getProfileTask.setVisibleView(mLayoutLoading);
		getProfileTask.executeOnExecutor(NetworkManager.THREAD_POOL);
	}

	private void saveSetting(Profile profile) {
		Settings s = Settings.instance();
		//		JSONObject jProfile = result.optJSONObject("userProfile");
		TelephoneFragment teleFrag = (TelephoneFragment) mBackFragArr[0];
		JSONObject jProfile = teleFrag.getProfile();

		s.setAccessToken(
				jProfile.optString("accessToken"), 
				jProfile.optString("refreshToken"));
		s.activateID= teleFrag.getActiveCode();

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
	
	private String getTempAccessToken() {
		TelephoneFragment teleFrag = (TelephoneFragment) mBackFragArr[0];
		JSONObject jProfile = teleFrag.getProfile();
		return jProfile.optString("accessToken");
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

				mLayoutLoading.setVisibility(View.INVISIBLE);
			}
		});

		Bundle bundle = new Bundle();
		bundle.putString("fields", "name,address,birthday,email,gender,id,middle_name,first_name,last_name,work,picture");
		mLayoutLoading.setVisibility(View.VISIBLE);
		request.setParameters(bundle);
		request.executeAsync();
	}

	private void uploadSocialProfile(String profile, String accessToken, int type) {
		UploadSocialProfile uploadTask = new UploadSocialProfile(
				LoginActivity.this, getTempAccessToken(), profile, accessToken, type) {
			@Override
			protected void onCompleted(Object result2) throws Exception {
				mTaskList.remove(this);

				JSONObject result = (JSONObject) result2;

				if (result.getInt("status") == 1) {
					Profile p = new Profile();
					JsonParser.parseObject(p, result.getJSONObject("profile"));
					saveSetting(p);
					finish();
					mListener.onSuccess();
				} else {
					CyUtils.showError(result.getString("message"), null, LoginActivity.this);
				}
			}

			@Override
			protected void onFail(Exception e) {
				mTaskList.remove(this);

				CyUtils.showError("Cập nhật thông tin thất bại!", mEx, LoginActivity.this);
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
		CyUtils.showToast("Google+ connected", this);
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
	
	@Override
	public void onConnectionFailed(ConnectionResult result) {
//		CyUtils.showToast("Google+ connect failed", this);
		mConnectionResult = result;
		registerCallback();
	}
	
	private void registerCallback() {
		mPlusClient.unregisterConnectionCallbacks(this);
		mPlusClient.registerConnectionCallbacks(new ConnectionCallbacks() {

			@Override
			public void onDisconnected() {

			}

			@Override
			public void onConnected(Bundle arg0) {
				Person currentPerson = mPlusClient.getCurrentPerson();
				if (currentPerson == null)
					return;
				
				String profile = currentPerson.toString();
				uploadSocialProfile(profile, "", 2);
			}
		});
	}
	
	private void onGooglePlusClick() {
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
	protected void onDestroy() {
		super.onDestroy();
		mUiHelper.onDestroy();

		for (CyAsyncTask task : mTaskList)
			task.cancel(true);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mUiHelper.onSaveInstanceState(outState);
	}

	///////////////////////////////////////////////////////////////////////////
	// Listener
	///////////////////////////////////////////////////////////////////////////

//	public static class Listener {
//		public void onLoginSuccess() {};
//		public void onRegisterSuccess() {};
//	}

	public interface BackListener {
		public void onBackPress();
	}
}
