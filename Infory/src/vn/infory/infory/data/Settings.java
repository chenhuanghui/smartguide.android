package vn.infory.infory.data;

import java.util.ArrayList;

import vn.infory.infory.FlashActivity.Listener;
import vn.infory.infory.login.InforyLoginActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class Settings {

	public static final String SHARED_PREFERENCE_NAME = "SETTINGS";

	private static Settings sInstance;
	private static Context mContext;

	private ArrayList<DataChangeListener> mListenerList = new ArrayList<DataChangeListener>();

	private String tokenID;
	private String refreshID;

	public String activateID;
	public String userID;
	public String name;
	public String phoneNumber;
	public String avatar;

	public String fb_id;
	public String fb_access_token;
	public String email;
	public int gender;
	public String dob;
	public String job;

	public String cover;
	public int socialType;
	public boolean firstTime;

	//City
	public String cityId = "1";
	public String cityName = "Hồ Chí Minh";

//	public float lat = 10.759765f;
//	public float lng = 106.692842f;
	public static float lat = -1;
	public static float lng = -1;
	public String P;
	
	public static void init(Context context) {
		if (sInstance == null) {
			mContext = context;
			sInstance = new Settings();
			sInstance.load();
		}
	}

	public static void release() {
		mContext = null;
		sInstance = null;
	}

	public static Settings instance() {	
		return sInstance;
	}

	public void notifyDataChange() {
		for (DataChangeListener listener : mListenerList)
			listener.onUserDataChange(this);
	}

	public void addListener(DataChangeListener listener) {
		mListenerList.add(listener);
	}
	
	public void removeListener(DataChangeListener listener) {
		mListenerList.remove(listener);
	}

	public void save() {
		SharedPreferences.Editor editor = 
				mContext.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE).edit();

		editor.putString("tokenID", getAccessToken());
		editor.putString("refreshID", getRefreshToken());
		editor.putString("activateID", activateID);
		editor.putString("userID", userID);
		editor.putString("phoneNumber", phoneNumber);
		editor.putString("avatar", avatar);
		editor.putString("name", name);

		editor.putString("fb_id", fb_id);
		editor.putString("fb_access_token", fb_access_token);
		editor.putString("email", email);
		editor.putInt("gender", gender);
		editor.putString("dob", dob);
		editor.putString("job", job);

		editor.putString("cityId", cityId);

		editor.putString("cover", cover);
		editor.putInt("socialType", socialType);
		editor.putBoolean("firstTime", firstTime);

		editor.commit();
	}

	public String getSex() {
		return getSex(gender);
	}
	
	public static String getSex(int sex) {
		switch (sex) {
		case 0: return "Nữ";
		case 1: return "Nam";
		case -1: return "Không xác định";
		default: return "";
		}
	}

	public void load() {
		SharedPreferences preference = 
				mContext.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);

		setAccessToken(preference.getString("tokenID", "abc"), preference.getString("refreshID", ""));
		activateID 		= preference.getString("activateID", "");
		userID 			= preference.getString("userID", "");
		phoneNumber 	= preference.getString("phoneNumber", "");
		avatar 			= preference.getString("avatar", "");
		name			= preference.getString("name", "");

		fb_id 			= preference.getString("fb_id", "");
		fb_access_token = preference.getString("fb_access_token", "");
		email 			= preference.getString("email", "");
		gender 			= preference.getInt("gender", -1);
		dob 			= preference.getString("dob", "");
		job 			= preference.getString("job", "");

		cityId 			= preference.getString("cityId", "1");

		cover 			= preference.getString("cover", "");
		socialType		= preference.getInt("socialType", 0);
		firstTime		= preference.getBoolean("firstTime", true);
	}
	
	public void logout() {
		setAccessToken("abc", "");
		activateID 		= "";
		userID 			= "";
		phoneNumber 	= "";
		avatar 			= "";
		name			= "";

		fb_id 			= "";
		fb_access_token = "";
		email 			= "";
		gender 			= -1;
		dob 			= "";
		job 			= "";

		cityId 			= "1";

		cover 			= "";
		socialType		= 0;
		firstTime		= true;
		
		save();
	}
	
	public static void checkLogin(final Activity act, 
			final Runnable task, final boolean runAfterLogin) {
		if (sInstance.getAccessToken().equals("abc")) {
			AlertDialog.Builder builder = new Builder(act);
			builder.setMessage("Bạn cần phải đăng nhập để sử dụng chức năng này!");
			builder.setPositiveButton("Đăng nhập", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Listener listener;
					if (runAfterLogin) {
						listener = new Listener() {
							
							@Override
							public void onSuccess() {
								task.run();
							}
						};
					} else {
						listener = new Listener();
					}

					InforyLoginActivity.newInstance(act, listener);
				}
			});
			builder.setNegativeButton("Hủy", null);
			builder.show();
		} else {
			task.run();
		}
	}

	public String getAccessToken() {
		// TODO: Should apply some encryptions here
		return tokenID;
	}

	public String getRefreshToken() {
		// TODO: Should apply some encryptions here
		return refreshID;
	}

	public void setAccessToken(String token, String refresh) {
		// TODO: Should apply some encryptions here
		tokenID = token;
		refreshID = refresh;
	}

	public interface DataChangeListener {
		public void onUserDataChange(Settings s);
	}
	
	public static void getLocation(Context context){
		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		LocationListener locationListener = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				lat = (float)location.getLatitude();
				lng = (float)location.getLongitude();
			}

			@Override
			public void onProviderDisabled(String provider) {

			}

			@Override
			public void onProviderEnabled(String provider) {
				
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				// TODO Auto-generated method stub
				
			}  
		};

		// Register the listener with the Location Manager to receive location updates
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5 * 60 * 1000, 0, locationListener);
		//locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
	}

}