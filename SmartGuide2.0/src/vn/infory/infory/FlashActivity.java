package vn.infory.infory;

import java.security.MessageDigest;

import vn.infory.infory.data.Profile;
import vn.infory.infory.data.Settings;
import vn.infory.infory.login.UseImmediatelyActivity;
import vn.infory.infory.network.CheckEmergence;
import vn.infory.infory.network.GetProfile;
import vn.infory.infory.network.NetworkManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

public class FlashActivity extends Activity {
	
	private static Listener sListener;
	
	private Listener mListener;
	
	///////////////////////////////////////////////////////////////////////////
	// Override methods
	///////////////////////////////////////////////////////////////////////////

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		mListener = sListener;
		sListener = null;
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flash);
		
//		PackageInfo info;
//		try {
//		    info = getPackageManager().getPackageInfo("vn.infory.infory", PackageManager.GET_SIGNATURES);
//		    for (Signature signature : info.signatures) {
//		        MessageDigest md;
//		        md = MessageDigest.getInstance("SHA");
//		        md.update(signature.toByteArray());
//		        String something = new String(Base64.encode(md.digest(), 0));
//		        //String something = new String(Base64.encodeBytes(md.digest()));
//		        Log.d("hash key", something);
//		    }
//		} catch (Exception e1) {}
		
		// Check emergency
		new CheckEmergence(this) {
			
			protected void onFinalFail(Exception e) {
				finish();
				mListener.onFail(e);
			}
			
			protected void onSuccess() {
				getProfile();
			}
		}.execute(NetworkManager.THREAD_POOL);
	}
	
	@Override
	public void onBackPressed() {}
	
	private void getProfile() {
		new GetProfile(this) {
			protected void onCompleted(Object result2) throws Exception {
				boolean firstTime =  Settings.instance().getAccessToken().equals("abc");
				
//				firstTime = true;
				
				Profile profile = (Profile) result2;
				saveSetting(profile);
				
				if (firstTime) {
//					mListener.onFirstTime();
					UseImmediatelyActivity.newInstance(FlashActivity.this, mListener);
				} else {
					mListener.onSuccess();
				}
				
				finish();
			}
			
			protected void onFail(final Exception e) {
				AlertDialog.Builder builder = new Builder(FlashActivity.this);
				builder.setMessage("Không thể kết nối với máy chủ!");
				builder.setPositiveButton("OK", new OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						finish();
						mListener.onFail(e);
					}
				});
				builder.create().show();
			}
		}.executeOnExecutor(NetworkManager.THREAD_POOL);
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Public methods
	///////////////////////////////////////////////////////////////////////////

	public static void newInstance(Activity act, Listener listener) {
		sListener = listener;
		Intent intent = new Intent(act, FlashActivity.class);
		act.startActivity(intent);
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
		
		s.firstTime	= false;

		s.save();
		s.notifyDataChange();
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Listener
	///////////////////////////////////////////////////////////////////////////
	
	public static class Listener {
//		public void onFirstTime() {}
		public void onSuccess() {}
		public void onFail(Exception e) {}
		
//		public void onRegisterCancel() {}
	}
}