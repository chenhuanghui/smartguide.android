package vn.smartguide;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;

import com.loopj.android.http.AsyncHttpClient;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

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
	
	// AysnHttpClient
	public static AsyncHttpClient mAsyncHttpClient = new AsyncHttpClient();

	// ImageLoader & DisplayImageOptions for asyn downloading image
	public static ImageLoader imageLoader;
	public static DisplayImageOptions displayImageOptions;

	// Animation time for next page -> ViewPager
	public static int timeTransfer = 1000;

	// Secret key and Client key to get token
	public static String clientID = "1_orazuv2dl3k8ossssg8804o4kwksw8kwcskkk404w40gwcwws";
	public static String serectID = "4xvgf3r9dxs8k8g8o8k0gss0s0wc8so4g4wg40c8s44kgcwsks";
	public static String grantType = "?grant_type=http://dev.smartguide.com/app_dev.php/grants/bingo";
	public static String serverOAuth = "https://devapi.smartguide.vn/oauth/v2/token";
	
	public static String tokenID = "NWRiNTRhMDI1MDQwOWY4ZDk4MDAzOTA5ZDQ2MTY5YmQwYzM0YTM5Y2RiZTcxZDQ3ZTEyNDU3YThiNzQ3N2UxMg";
	public static String refreshTokenID = "NzRkYmFiMTc2YTZlZGVlZTk3OTcxNGM4M2MwNjYwYmVjYTEwMWIxYTcyNzU5M2JhYzI3OTBiNjA2MDAyMDk5Nw";
	
	public static String getTokenURL = serverOAuth + grantType + "&client_id=" + clientID + "&client_secret=" + serectID; 
			
	// Activate Code
	public static String activateCode = "";
	public static String phoneNumber = "";
	public static String urlGetActivateCode = "https://devapi.smartguide.vn/user/activation?phone=";
	public static String urlChekcActivateCode = "https://devapi.smartguide.vn/user/check?phone=";
	public static String footerURL = "";
	
	// Database
	public static String databaseName ="SmartGuideDB.db";
	public static DatabaseManger smartGuideDB = null;

	// Time
	public static int delayToShopDetail = 700;
	public static int focusInterval = 500;
	public static int timeChangeAds = 10000;
	public static int timeToResumeImageDownloader = 3500;
	
	// Load more setting
	public static int needLoadMore = 5;
	public static int itemPerPage = 10;

	// Category
	public static List<Category> mCateogries = null;

	// Facebook
	public static String userIDFacebook = "";
	public static String avatarFace ="";
	public static String userID = "";
	public static String id ="";
	public static String user_id ="";
	public static String name ="";
	public static boolean gender=false;
	public static String email="";
	public static String dob="";
	public static String job="";
	public static String avatar="";
	public static boolean isNeedUpdateFacebook = false;
	public static String nameFace = "";

	// Filter String
	public static String mFilterString = "";
	public static String mSortByString = "0";
	public static String mCityID = "1";
	public static String mVersion = "0";
	public static boolean isNeedUpdateCityList = false;
	public static List<String> mCityNames = new ArrayList<String>();
	public static List<String> mCityIDes = new ArrayList<String>();
	
	public static float mLat = -1;
	public static float mLng = -1;
	
	// Camera Intent request code
	public static final int CAMERA_REQUEST_CODE = 33333;
	
	// Shop
	public static Shop mCurrentShop = null;
	
	public static boolean mIsLaunching = false;
	
	public static void createDatbase(Context applicationcontext){
		smartGuideDB = new DatabaseManger(applicationcontext);
	}

	public static void getAndSaveTokenID(){
		smartGuideDB.updateToken(getTokenIDViaOAuth2());
	}

	public static HashMap<String, String> getTokenIDViaOAuth2(){
		HttpGet httpGet = new HttpGet(getTokenURL + GlobalVariable.footerURL);
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
		if(avatarFace == "" || avatarFace.compareTo("null") == 0)
			avatarFace = token.get("avatar");
		if (nameFace == "" || nameFace.compareTo("null") == 0)
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
		for(int i = 0; i < list.size(); i++){ 
			HashMap<String, String> city = list.get(i);
			mCityNames.add(city.get("name"));
			mCityIDes.add(city.get("cityID"));
		}
	}
	
	public static void updateLocation(Activity activity){
		LocationManager mgr = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String best = mgr.getBestProvider(criteria, true);
        Location location = mgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null){
        	mLat = (float) location.getLatitude();
        	mLng = (float) location.getLongitude();
        }
        
        mLat = 10.7602819f;
        mLng = 106.6886185999993f;
	}
	
	// for launching reason
	public static int mMode = 1;
	public static String mURL = "";
}
