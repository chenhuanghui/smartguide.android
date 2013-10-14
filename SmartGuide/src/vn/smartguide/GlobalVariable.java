package vn.smartguide;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ChauSang on 7/24/13.
 */

public final class GlobalVariable {
	
	// ImageLoader
	public static final int IMAGE_FILE_NAME_LENGTH  = 20;
	public static final int SAMPLE_LENGTH 			= 3;
	public static final String IMAGE_FILE_PATH 		= "imageCache/";
	
	// AysnHttpClient
	public static AsyncHttpClient mAsyncHttpClient = new AsyncHttpClient();
	
	// Cycrix ImageLoader
	public static CyImageLoader cyImageLoader;

	// Animation time for next page -> ViewPager
	public static int timeTransfer 		= 1000;

	// Secret key and Client key to get token
	public static String serverOAuth 	= APILinkMaker.mHostName + "oauth/v2/token";
	
	// Dev2
//	public static String clientID 		= "1_407qlmrvr5esg8s8wkocw8wgog84kkk40o8k00oososgcs8sc4";
//	public static String serectID 		= "1jcvy0kw4tk0o4wcgcos8s84kssw08c0w8w04c0k08gwc48cks";
//	// Dev
//	public static String clientID 		= "1_orazuv2dl3k8ossssg8804o4kwksw8kwcskkk404w40gwcwws";
//	public static String serectID 		= "4xvgf3r9dxs8k8g8o8k0gss0s0wc8so4g4wg40c8s44kgcwsks";
	// Pro 
	public static String clientID 		= "1_53obx9yqlcco80w8wkoowgccw44o0w0ook0okogwosg84wscg8";
	public static String serectID 		= "t3p0k1rvstcgwcsggo8ossgcwo8cckso88sscgcsks8w0wsk8";
	
	public static String grantType 		= "?grant_type=http://dev.smartguide.com/app_dev.php/grants/bingo";

	public static String tokenID 		= "";
	public static String refreshTokenID = "";

	public static String getTokenURL 	= serverOAuth + grantType + "&client_id=" + clientID + "&client_secret=" + serectID; 

	// Activate Code
	public static String urlGetActivateCode 	= APILinkMaker.mHostName + "user/activation?phone=";
	public static String urlChekcActivateCode 	= APILinkMaker.mHostName + "user/check?phone=";
	public static String activateCode 			= "";
	public static String phoneNumber 			= "";
	public static String footerURL 				= "";

	// Database
	public static String databaseName 			= "SmartGuideDB.db";
	public static DatabaseManger smartGuideDB 	= null;

	// Time
	public static int delayToShopDetail 			= 700;
	public static int focusInterval 				= 500;
	public static int timeChangeAds 				= 10000;
	public static int timeToResumeImageDownloader 	= 3500;

	// Load more setting
	public static int needLoadMore 	= 5;
	public static int itemPerPage 	= 10;

	// Category
	public static List<Category> mCateogries;

	// Facebook
	public static String userIDFacebook = "";
	public static String avatarFace 	= "";
	public static String userID 		= "";
	public static String id 			= "";
	public static String user_id 		= "";
	public static String name 			= "";
	public static boolean gender		= false;
	public static String email			= "";
	public static String dob			= "";
	public static String job			= "";
	public static String avatar			= "";
	public static boolean isNeedUpdateFacebook = false;
	public static String nameFace 		= "";
	public static String faceAccessToken = "";
	// Filter String
	public static String mFilterString 	= "1,2,3,4,5,6,7,8";
	public static String mSortByString 	= "0";
	public static String mCityID 		= "1";
	public static String mVersion 		= "0";
	public static boolean isNeedUpdateCityList 	= false;
	public static List<String> mCityNames 		= new ArrayList<String>();
	public static List<String> mCityIDes 		= new ArrayList<String>();

	public static float mLat = -1;
	public static float mLng = -1;

	// for launching reason
	public static int mMode 				= 1; 	// 0:LOCK 1:NORMAL
	public static String mURL 				= "";
	public static String reviewString 		= "";
	public static boolean isNeedPostReview 	= false;

	// Shop
	public static Shop mCurrentShop;

	public static boolean mIsLaunching = false;

	// GPS
	private static boolean isFirstTimeGetGPS = false;
	
	public static String json10FirstShop = "";
	public static List<String> mAvatarList;
	
	
	public Bitmap photo = null;
	
	public static void createDatbase(Context applicationcontext){
		smartGuideDB = new DatabaseManger(applicationcontext);
	}

	public static void getAndSaveTokenID(){
		smartGuideDB.updateToken(getTokenIDViaOAuth2());
	}

	public static HashMap<String, String> getTokenIDViaOAuth2(){
	
		HttpGet httpGet = new HttpGet(getTokenURL + footerURL);
		JSONObject key;

		try{
			HttpResponse httpResponse = NetworkManger.httpclient.execute(httpGet);
			HttpEntity httpEntity = httpResponse.getEntity();
			key = new JSONObject(EntityUtils.toString(httpEntity));

		}catch(Exception ex){
			return null;
		}

		try {
			tokenID = key.getString("access_token");
			refreshTokenID = key.getString("refresh_token");
		} catch (JSONException e) {
			return null;
		}

		HashMap<String, String> token =  new  HashMap<String, String>();
		token.put("tokenID", tokenID);
		token.put("refreshID", refreshTokenID);
		return token;
	}

	public static HashMap<String, String> getRefreshIDViaOAuth2(){
		
		HttpGet httpGet = new HttpGet(serverOAuth + "?grant_type=refresh_token&client_id=" + clientID + "&client_secret=" + serectID + "&refresh_token=" + refreshTokenID);
		JSONObject key;

		try{
			DefaultHttpClient httpClient = new DefaultHttpClient(NetworkManger.ccm, NetworkManger.params);
			HttpResponse httpResponse = httpClient.execute(httpGet);
			HttpEntity httpEntity = httpResponse.getEntity();
			key = new JSONObject(EntityUtils.toString(httpEntity));

		}catch(Exception ex){
			return null;
		}

		try {
			tokenID = key.getString("access_token");
			refreshTokenID = key.getString("refresh_token");
		} catch (JSONException e) {
			return null;
		}

		HashMap<String, String> token =  new  HashMap<String, String>();
		token.put("tokenID", tokenID);
		token.put("refreshID", refreshTokenID);
		return token;
	}

	public static void getTokenFromDB(){
		HashMap<String, String> token = smartGuideDB.getToken();

		if (token == null){
			getAndSaveTokenID();
		}else{
			tokenID = token.get("tokenID");
			refreshTokenID = token.get("refreshID");
		}
	}

	public static boolean getActivateCodeFromDB(){
		HashMap<String, String> token = smartGuideDB.getActivateCode();

		if (token == null){
			return false;
		}

		activateCode = token.get("activateID");
		phoneNumber = token.get("phoneNumber");
		userID = token.get("userID");
		avatarFace = token.get("avatar");
		nameFace = token.get("nameFace");
		footerURL = "&phone=" + phoneNumber + "&code=" + activateCode;
		return true;
	}

	public static boolean getFacebookFromDB(){
		HashMap<String, String> token = smartGuideDB.getFacebook();

		if (token == null){
			return false;
		}

		userIDFacebook = token.get("userID");
		avatarFace = token.get("avatar");
		nameFace = token.get("name");
		return true;
	}

	public static boolean getVersionFromDB(){
		HashMap<String, String> token = smartGuideDB.getVersion();

		if (token == null){
			return false;
		}

		mVersion = token.get("versionID");
		mCityID = token.get("cityID");
		return true;
	}

	public static void getCityList(){
		mCityNames = new ArrayList<String>();
		mCityIDes = new ArrayList<String>();

		ArrayList<HashMap<String, String>> list = smartGuideDB.getCity();
		for(int i = 0; i < list.size(); i++) {
			HashMap<String, String> city = list.get(i);
			mCityNames.add(city.get("name"));
			mCityIDes.add(city.get("cityID"));
		}
	}
	
	public static void getLocation(Context context){

		final MainAcitivyListener mMainAcitivyListener = (MainAcitivyListener) context;
		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		
		final Context mcontext = context;
		
		LocationListener locationListener = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				if (!isFirstTimeGetGPS){
					isFirstTimeGetGPS = true;
					mMainAcitivyListener.finishGetSGP();
				}
				
				GlobalVariable.mLat = (float)location.getLatitude();
				GlobalVariable.mLng = (float)location.getLongitude();
			}

			@Override
			public void onProviderDisabled(String provider) {
				
			}

			@Override
			public void onProviderEnabled(String provider) {
				Toast.makeText(mcontext, "Đang lấy tọa độ GPS", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}		  
		};

		// Register the listener with the Location Manager to receive location updates
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
	}
	
	public static String right(String s, int n) {
		return s.substring(s.length() - n);
	}
	
	public static void showToast(String message, Context ct) {
		Toast.makeText(ct, message, Toast.LENGTH_LONG).show();
	}
}
