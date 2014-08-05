package vn.infory.infory.login;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import vn.infory.infory.CyUtils;
import vn.infory.infory.FontsCollection;
import vn.infory.infory.R;
import vn.infory.infory.data.Settings;
import vn.infory.infory.login.InforyLoginActivity.BackListener;
import vn.infory.infory.network.CheckActiveCode;
import vn.infory.infory.network.CyAsyncTask;
import vn.infory.infory.network.GetActiveCode;
import vn.infory.infory.network.NetworkManager;
import vn.infory.infory.network.UpdateDeviceInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.telephony.PhoneNumberUtils;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;

public class TelephoneFragment extends Fragment implements BackListener {

	// GUI elements
	@ViewById(id = R.id.btnBack)				private ImageButton mBtnBack;
	@ViewById(id = R.id.btnSend)				private Button mBtnSend;
	@ViewById(id = R.id.edtTelNum)				private EditText mEdtTelephone;
	@ViewById(id = R.id.txtMesTop)				private TextView mTxtMesTop;
	@ViewById(id = R.id.txtMesBot)				private TextView mTxtMesBot;
	@ViewById(id = R.id.txt84)					private TextView mTxt84; 

	private String mPhoneNumber, mActiveCode;
	private JSONObject mjProfile;
	private Timer mTimer;
	private List<CyAsyncTask> mTaskList = new ArrayList<CyAsyncTask>();
	private Listener mListener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.login_telephone, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		try {
			AndroidAnnotationParser.parse(this, view);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		FontsCollection.setFont(view);
		CyUtils.setHoverEffect(mBtnSend, false);

		mEdtTelephone.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEND) {
					if (mBtnSend.isEnabled())
						mBtnSend.performClick();
					return true;
				}
				return false;
			}
		});
	}
	
	public void setListener(Listener listener) {
		mListener = listener;
	}
	
	@Override
	public void onBackPress() {
		if(mTimer != null)
			mTimer.cancel();
		onBackClick(null);
	}

	@Override
	public void onResume() {
		super.onResume();
	}
	
	public void reset() {
		mBtnSend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onSendClick(v);
			}
		});
		
		mTxt84.setVisibility(View.VISIBLE);
		mEdtTelephone.setText("");
		mPhoneNumber = "";
		mActiveCode = "";
		mTxtMesTop.setText("Nhập số điện thoại của bạn.");
		mTxtMesBot.setText("");
		InputMethodManager inputMgr = (InputMethodManager)
				getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMgr.showSoftInput(mEdtTelephone, InputMethodManager.SHOW_IMPLICIT);
		
	}
	
	public void hideSoftKeyboard() {
		InputMethodManager inputMgr = (InputMethodManager)
				getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMgr.hideSoftInputFromWindow(mEdtTelephone.getWindowToken(), 0);
	}

	///////////////////////////////////////////////////////////////////////////
	// Private methods
	///////////////////////////////////////////////////////////////////////////
	
	@Click(id = R.id.btnBack)
	private void onBackClick(View v) {
		if(mTimer != null)
			mTimer.cancel();
		mListener.onBackPress();
	}

	@Click(id = R.id.btnSend)
	private void onSendClick(View v) {
		// Validate phone number
		mPhoneNumber = mEdtTelephone.getText().toString();

		Dialog dialog = null;
		if (mPhoneNumber.trim().length() == 0) {
			// Empty string
			dialog = new AlertDialog.Builder(getActivity()).setPositiveButton(R.string.OK, null)
					.setMessage("Xin vui lòng nhập số điện thoại!").create();
			dialog.show();
			((TextView) dialog.findViewById(android.R.id.message)).setGravity(Gravity.CENTER);
		} else if (validatePhoneNumber(mPhoneNumber)) {
			// Valid
			mPhoneNumber = formatPhone(mPhoneNumber);
//			if (mPhoneNumber.charAt(0) == '+')
//				mPhoneNumber = mPhoneNumber.substring(1);

			// Builder dialog to ensure user
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage("(+84)" + mPhoneNumber.substring(2) +"\nMã xác thực Infory sẽ được " +
					"gửi đến số điện thoại trên qua tin nhắn. Chọn Đồng ý để tiếp tục hoặc hủy để" +
					" thay đổi số điện thoại");
			builder.setCancelable(true);
			builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {

					// Send request to server to retrieve activate code via SMS
					requestActivateCode();
				}
			});
			builder.setNegativeButton(R.string.Cancel, null);
			dialog = builder.create();
			dialog.show();
			((TextView) dialog.findViewById(android.R.id.message)).setGravity(Gravity.CENTER);
		} else {
			// Invalid
			dialog = new AlertDialog.Builder(getActivity()).setPositiveButton(R.string.OK, null)
					.setMessage("Số điện thoại không hợp lệ!").create();
			dialog.show();
			((TextView) dialog.findViewById(android.R.id.message)).setGravity(Gravity.CENTER);
		}
	}

	private void requestActivateCode() {
		mTxtMesBot.setText("Đang gởi yêu cầu lấy mã xác thực...");
		CyAsyncTask task = new GetActiveCode(getActivity(), mPhoneNumber) {
			@Override
			protected void onCompleted(Object result) {
				mTaskList.remove(this);

				if (result == null) {
					// Request success, start count down, wait for SMS
					startCountdown();
					mTxt84.setVisibility(View.GONE);
					mEdtTelephone.setText("");
					mTxtMesTop.setText("Chờ và nhập mã xác thực...");
					mBtnSend.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mTimer.cancel();
							confirmActivateCode();
						}
					});
				} else {
					// Request rejected
					mTxtMesBot.setText((String) result);
				}
			}

			@Override
			protected void onFail(Exception e) {
				mTaskList.remove(this);
				
				// Request fail
				mTxtMesBot.setText("Gởi yêu cầu lấy mã xác thực thất bại!");
			}
		};
		task.setDisableView(mBtnSend);
		mTaskList.add(task);
		task.executeOnExecutor(NetworkManager.THREAD_POOL);
	}

	private void confirmActivateCode() {
		mActiveCode = mEdtTelephone.getText().toString();

		if (mActiveCode.trim().length() == 0) {
			Dialog dialog = new AlertDialog.Builder(getActivity())
			.setPositiveButton(R.string.OK, null)
			.setMessage("Xin vui lòng nhập mã xác thực!").create();
			dialog.show();
			((TextView) dialog.findViewById(android.R.id.message)).setGravity(Gravity.CENTER);
			return;
		}

		mTxtMesBot.setText("Đang xác thực...");
		CyAsyncTask task = new CheckActiveCode(getActivity(), mPhoneNumber, mActiveCode) {

			@Override
			protected void onSuccessFirstTime(JSONObject result) {
				mTaskList.remove(this);
				
				mjProfile = result;
				saveProfile(result);
				
				//Update Device Info
				CyAsyncTask taskUpdateDeviceInfo = new UpdateDeviceInfo(getActivity(), 1);
				mTaskList.add(taskUpdateDeviceInfo);
				taskUpdateDeviceInfo.executeOnExecutor(NetworkManager.THREAD_POOL);
				
				Editor e = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
				
				e.putString("accessToken", mjProfile.optString("accessToken"));
				e.putString("refreshToken", mjProfile.optString("refreshToken"));
				e.putString("activeCode", mActiveCode);
				e.commit();
				
				/*android.support.v4.app.FragmentManager fm = getFragmentManager();
				FragmentTransaction ft = fm.beginTransaction();
				
				ft.add(R.id.fragLoginRegisterType, new RegisterTypeFragment());
				ft.commit();*/
//				RegisterTypeFragment fragment = (RegisterTypeFragment) getFragmentManager().findFragmentByTag("android:switcher:"+R.id.fragLoginRegisterType+":0");
//				View v = fragment.getView();
				
//				txt.setText("CCCC");
//				fragment.onFinishLogin();
				
				mListener.onSuccess();
			}

			@Override
			protected void onSuccess(JSONObject result) {
				mTaskList.remove(this);				
				mjProfile = result;				
				saveProfile(result);
				
				mListener.onLoginSuccess(result);
			}

			@Override
			protected void onReject(JSONObject result) {
				mTaskList.remove(this);
				
				mTxtMesBot.setText(result.optString("message"));
			}

			@Override
			protected void onFail(Exception e) {
				mTaskList.remove(this);
				Log.e("YOUR_APP_LOG_TAG", "I got an error", e);
				mTxtMesBot.setText("Xác thực thất bại");
				CyUtils.showError("Xác thực thất bại", e, getActivity());
			}
		};
		task.setDisableView(mBtnSend);
		mTaskList.add(task);
		task.executeOnExecutor(NetworkManager.THREAD_POOL);
	}
	
	public JSONObject getProfile() {
		return mjProfile;
	}
	
	public String getActiveCode() {
		return mActiveCode;
	}
	
	private void saveProfile(JSONObject result) {
		Settings s = Settings.instance();
		JSONObject jProfile = result.optJSONObject("userProfile");
		
		s.setAccessToken(result.optString("accessToken"), result.optString("refreshToken"));
		s.activateID= mActiveCode;
		
		s.phoneNumber= jProfile.optString("phone");
		
		s.avatar 	= jProfile.optString("avatar");
		s.name 		= jProfile.optString("name");
		s.userID	= jProfile.optString("idUser");
		s.gender	= jProfile.optInt("gender");
		s.cover		= jProfile.optString("cover");
		s.dob		= jProfile.optString("dob");
		s.socialType= jProfile.optInt("socialType", 0);

		s.save();
		s.notifyDataChange();
	}

	private void startCountdown() {
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {
			int i = 30;

			@Override
			public void run() {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (i == -1) {							
//							mTxtCountDown.setText("Bạn chưa nhận được mã xác nhận?");
//							mBtnResendCode.setVisibility(View.VISIBLE);
							mTimer.cancel();
						} else {
							String head = "Vui lòng chờ tổng đài gởi mã xác thực trong ";
							String tail = " giây.";
							SpannableString span = new SpannableString(head + i + tail);
							span.setSpan(new StyleSpan(Typeface.BOLD), 
									head.length(), head.length() + ("" + i).length(), 0);
							span.setSpan(new ForegroundColorSpan(0xFFC95436), 
									head.length(), head.length() + ("" + i).length(), 0);
							mTxtMesBot.setText(span);
							i--;
						}
					}
				});

			}
		}, 0, 1000);
	}

	// Logic
	private String formatPhone(String phone) {
		phone = phone.trim();
		if (phone.trim().length() == 0)
			return "";

		if (phone.charAt(0) == '+')
			phone = phone.substring(1);

		if (phone.startsWith("84"))
			phone = phone.substring(2);
		
		if (phone.charAt(0) == '0')
			phone = phone.substring(1);

		return "84" + phone;
	}

	private boolean validatePhoneNumber(String phone) {
		
		// 8 - 14
		if (phone.length() < 8 || phone.length() > 14)
			return false;
		
		if (phone.charAt(0) == '+')
			phone = phone.substring(1);
		
		if (phone.startsWith("84"))
			phone = phone.substring(2);
		
		if (phone.charAt(0) == '0')
			phone = phone.substring(1);
		
		if(phone.charAt(0) != '1' && phone.charAt(0) != '9')
			return false;
		
		if (phone.charAt(0) == '9' && phone.length() != 9)
			return false;
		
		if (phone.charAt(0) == '1' && phone.length() != 10)
			return false;
		
		for (int i = 0; i < phone.length(); i++) {
			if (phone.charAt(i) < '0' || phone.charAt(i) > '9')
				return false;
		}
		
		return true;
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Listener
	///////////////////////////////////////////////////////////////////////////

	public interface Listener {
		public void onBackPress();
		public void onLoginSuccess(JSONObject result);
		public void onSuccess();
	}

	
}