package vn.infory.infory.network;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.cycrix.jsonparser.JsonParser;

import vn.infory.infory.CyLogger;
import vn.infory.infory.data.Profile;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;

public class UpdateDeviceInfo extends CyAsyncTask {

	protected Exception mEx;
	private Integer mIsLogin;
	private Context mContext;
	CyLogger mLog = new CyLogger("CycrixDebug", true);
	public UpdateDeviceInfo(Context c, Integer isLogin) {
		super(c);
		// TODO Auto-generated constructor stub
		//Type:
		//	0: logout
		//	1: login
		mIsLogin = isLogin;
		mContext = c;
	}
	
	@Override
	protected Object doInBackground(Object... arg0) {

		try {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();

			String deviceInfo = getDeviceName();
			String imei = getIMEI(mContext);
			String mac = getMAC(mContext);
			
			pairs.add(new BasicNameValuePair("deviceId", imei));
			pairs.add(new BasicNameValuePair("deviceInfo", deviceInfo));
			pairs.add(new BasicNameValuePair("macAddress", mac));
			pairs.add(new BasicNameValuePair("os", "android"));
			pairs.add(new BasicNameValuePair("osVersion", Build.VERSION.RELEASE));
			pairs.add(new BasicNameValuePair("appVersion", mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName));
			pairs.add(new BasicNameValuePair("pushToken", null));
			pairs.add(new BasicNameValuePair("isLogin", mIsLogin.toString()));
			
//			mLog.d("deviceId: " + imei + " deviceInfo: " + deviceInfo + " mac: "+mac+" pushToken: " + null+" isLogin: "+mIsLogin.toString());
						
			NetworkManager.post(APILinkMaker.mUpdateDeviceInfo, pairs, false);
			return null;
		} catch (Exception e) {
			mLog.d("error: " + e);
		}

		return super.doInBackground(arg0);
	}
	
	public String getIMEI(Context c){
		TelephonyManager tm = (TelephonyManager) c.getSystemService(c.TELEPHONY_SERVICE);
		return tm.getDeviceId();
	}
	
	public String getMAC(Context c){
		WifiManager m_wm = (WifiManager)c.getSystemService(c.WIFI_SERVICE); 
		return m_wm.getConnectionInfo().getMacAddress();
	}
	
	public String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			return capitalize(model);
		} else {
			return capitalize(manufacturer) + " " + model;
		}
	}


	private String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	} 
}
