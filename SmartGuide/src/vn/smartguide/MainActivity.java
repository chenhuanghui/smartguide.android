package vn.smartguide;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.datamatrix.DataMatrixReader;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;
import com.google.zxing.qrcode.QRCodeReader;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnClosedListener;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnOpenedListener;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import vn.smartguide.CategoryListFragment.Listener;
import vn.smartguide.UserFragment.GiftItem;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends FragmentActivity implements MainAcitivyListener, OnTouchListener  {
	private final int WelcomeRequestCode 		= 44444;
	private final int FlashScreenRequestCode 	= 55555;
	private final int ReviewRequestCode			= 33333;
	private final int UpdateRequestCode			= 22222;
	private final int TutorialRequestCode		= 11111;

	// GCM
	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	String SENDER_ID = "523934270714";
	GoogleCloudMessaging gcm;
	String regid;

	// Load qrcode lib
	static {
		System.loadLibrary("iconv");
	}

	// QRcode 
	DataMatrixReader mDataMatrixReader = new DataMatrixReader();
	QRCodeReader mQRCodeReader = new QRCodeReader();
	private Camera mCamera;
	private CameraPreview mPreview;
	private Handler autoFocusHandler;
	private FrameLayout preview;
	private View mOpticalFrame;
	private boolean previewing;
	private String mQRCode = "";
	private int mScanningCode = 1;
	private int mAwardId = -1;
	private boolean isNeedUpdateSGP = false;
	private boolean isCanScan = true;
	private ProgressBar mProgressBar;
	private TextView QRCodeTextView;
	private ImageButton mCloseQRC;

	// QR Code layout
	private LinearLayout mMirror;
	private RelativeLayout mMirrorFront;
	private TextView mShopNameText;
	private TextView mContentText;
	private TextView mSGPText;
	private ImageButton mCloseBtn;
	private ImageView mScanCover;
	private Activity mActivity;
	//private TextView mTotalSGP;

	// Slide menu
	SlidingMenu menu;
	private RelativeLayout reviewBtn;
	private RelativeLayout updateBtn;
	private RelativeLayout gpsBtn;
	private RelativeLayout mTutorialBtn;
	private RelativeLayout mBtnIntro;

	// Viewpager
	private FragmentManager mFragmentManager;
	private List<Fragment> mFragmentList;
	private SViewPager mViewPager;

	// Toogle camera and content
	private boolean mShowContent 				= true;
	private boolean mShowCamera 				= false;
	private boolean mShowUser 					= false;
	private boolean mIsShowFilter				= false;
	private boolean mShowMenu 					= false;
	private boolean mIsNeedToggleMap			= false;
	private boolean mIsNeedToggleUser 			= false;

	// Fragment
	private AdsFragment mAdsFragment;
	private CategoryListFragment mCategoryListFragment;
	private ShopListFragment mShopListFragment;
	private ShopDetailFragment mShopDetailFragment;
	private UserFragment mUserFragment;
	private FilterFragment mFiterFragment;
	private SupportMapFragment mMapFragment;

	// Location 
	private RelativeLayout mLocationBtn = null;
	private TextView mLocationTV = null;

	// Facebook
	LoginButton authButton = null;
	private ImageButton mAvatarFaceBtn = null;
	Session.StatusCallback callback = new Session.StatusCallback() {

		public void call(Session session, SessionState state, Exception exception) {
			if (state.isOpened()) {
				makeMeRequest(session);
			} else if (state.isClosed()) {
			}
		}
	};

	// Navigation text
	private TextView mNaviText = null;
	private String mPreviousNavi = "";
	private String mCurrentNavi = "DANH Má»¤C";

	// Location choice in setting view
	private int mChoiceLocation = -1;

	//
	private View layoutTC;
	private int heightTC;
	private int cameraViewHeight;
	private View btnQRTToogle;
	private View textViewGetScore;

	// Map control
	private ImageButton mFilterBtn;
	private ImageButton mMapButton;
	private LatLngBounds mBound;
	private boolean mIsMapAlive = false;
	private boolean mNeedUpdateMap = false;

	// Swipe para
	static final int MIN_DISTANCE = 100;
	private float downX, downY, upX, upY;
	private boolean mIsCanWipe = false;

	// User button
	private ImageButton mUserButton;

	//Exit para
	private boolean doubleBackToExitPressedOnce = false;
	private boolean isFirstTime = false;
	private boolean mIsNeedGotoDetail = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);


		mUiHelper = new UiLifecycleHelper(this, callback);
		mUiHelper.onCreate(savedInstanceState);

		init();
//
//		//		startActivity(new Intent(this, GCM.class));
//		if (GlobalVariable.getActivateCodeFromDB() == false){
//			startActivityForResult(new Intent(this, WellcomeActivity.class), WelcomeRequestCode);
//			isFirstTime = true;
//		}
//		else
//			startActivityForResult(new Intent(this, FlashScreenActivity.class), FlashScreenRequestCode);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		mUiHelper.onActivityResult(requestCode, resultCode, data);

		switch (requestCode){
		case FlashScreenRequestCode:
			if (GlobalVariable.mIsLaunching){
				ImageView avatar = (ImageView)menu.getMenu().findViewById(R.id.userAvatarSetting);
				TextView name = (TextView)menu.getMenu().findViewById(R.id.textView);
				name.setMaxLines(1);

				if (GlobalVariable.avatarFace.compareTo("null") != 0){
					name.setText(GlobalVariable.nameFace);
					GlobalVariable.imageLoader.displayImage(GlobalVariable.avatarFace, avatar);
					mUserFragment.updateAvatar();
				}
				else
					name.setText("Anomynous User");

				disableAll();
				return;
			}
			if (data.getStringExtra("Database").compareTo("OK") == 0 && 
					data.getStringExtra("Connection").compareTo("OK") == 0){
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						if (isFirstTime)
							startActivityForResult(new Intent(MainActivity.this, TutorActivity.class), TutorialRequestCode);
						else
							updateInformation();
					}
				}, 500);
			}
			else
				exitWithError();

			break;

		case WelcomeRequestCode:
			if (data.getStringExtra("GOAHEAD").compareTo("OK") == 0)
				startActivityForResult(new Intent(this, FlashScreenActivity.class), FlashScreenRequestCode);
			else
				finish();
			break;

		case ReviewRequestCode:
			if(GlobalVariable.isNeedPostReview == true){
				new PostReview().execute();
			}
			break;

		case TutorialRequestCode:
			updateInformation();
			break;
		}
	}

	@Override
	public void goToPage(int index) {
		switch (index){
		case 0:
			enableFilterMap();
			mShopListFragment.releaseMemory();
			mMapButton.setClickable(false);
			mMapButton.setImageResource(R.drawable.menu_map_lock);
			break;
		case 1:
			enableFilterMap();
			mShopDetailFragment.releaseMemory();
			mMapButton.setClickable(true);
			mMapButton.setImageResource(R.drawable.map_btn);
			break;
		case 2:
			disableFilterMap();

		}
		mViewPager.setCurrentItem(index, true);
	}

	@Override
	public void goNextPage() {
		int current_index = mViewPager.getCurrentItem();
		if (current_index == mFragmentList.size() - 1)
			return;

		int pageWillGo = current_index + 1;
		switch (pageWillGo){
		case 1:
			enableFilterMap();
			int lengthOfFilterString = GlobalVariable.mFilterString.length();
			if (lengthOfFilterString >= 2)
				setNaviText("NHIá»€U DANH Má»¤C");
			else{
				int shopType = Integer.valueOf(GlobalVariable.mFilterString);
				switch(shopType){
				case 1:
					setNaviText("áº¨M THá»°C");
					break;
				case 2:
					setNaviText("CAFE & BAR");
					break;
				case 3:
					setNaviText("LÃ€M Ä�áº¸P");
					break;
				case 4:
					setNaviText("GIáº¢I TRÃ�");
					break;
				case 5:
					setNaviText("THá»œI TRANG");
					break;
				case 6:
					setNaviText("DU Lá»ŠCH");
					break;
				case 7:
					setNaviText("Sáº¢N PHáº¨M");
					break;					
				case 8:
					setNaviText("GIÃ�O Dá»¤C");
					break;
				}
			}

			break;
		case 2:
			disableFilterMap();
			setNaviText(GlobalVariable.mCurrentShop.mName);
			break;
		case 0:
			mMapButton.setClickable(false);
			mMapButton.setImageResource(R.drawable.menu_map_lock);
			break;
		}

		mViewPager.setCurrentItem(pageWillGo, true);
	}

	@Override
	public void goPreviousPage() {
		int current_index = mViewPager.getCurrentItem();

		if (current_index == 0){
			if (doubleBackToExitPressedOnce) {
				super.onBackPressed();
				return;
			}

			doubleBackToExitPressedOnce = true;
			Toast.makeText(this, "Nháº¥n back láº§n ná»¯a Ä‘á»ƒ thoÃ¡t chÆ°Æ¡ng trÃ¬nh", Toast.LENGTH_SHORT).show();
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					doubleBackToExitPressedOnce = false;   

				}
			}, 2000);
		}else
			doubleBackToExitPressedOnce = false;

		int pageWillGo = current_index - 1;
		try{
			switch (pageWillGo){
			case 1:
				mShopDetailFragment.releaseMemory();
				enableFilterMap();
				int lengthOfFilterString = GlobalVariable.mFilterString.length();
				if (lengthOfFilterString >= 2)
					setNaviText("NHIá»€U DANH Má»¤C");
				else{
					int shopType = Integer.valueOf(GlobalVariable.mFilterString);
					switch(shopType){
					case 1:
						setNaviText("áº¨M THá»°C");
						break;
					case 2:
						setNaviText("CAFE & BAR");
						break;
					case 3:
						setNaviText("LÃ€M Ä�áº¸P");
						break;
					case 4:
						setNaviText("GIáº¢I TRÃ�");
						break;
					case 5:
						setNaviText("THá»œI TRANG");
						break;
					case 6:
						setNaviText("DU Lá»ŠCH");
						break;
					case 7:
						setNaviText("Sáº¢N PHáº¨M");
						break;					
					case 8:
						setNaviText("GIÃ�O Dá»¤C");
						break;
					}
				}
				break;

			case 0:
				enableFilterMap();
				mMapButton.setClickable(false);
				mMapButton.setImageResource(R.drawable.menu_map_lock);
				setNaviText("DANH Má»¤C");
				mShopListFragment.releaseMemory();
				break;
			}	
		}catch(Exception ex){
			setNaviText("DANH Má»¤C");
		}
		mViewPager.setCurrentItem(current_index - 1, true);
	}

	public void toggleShowContent() {
		mShowContent = !mShowContent;

		if (mShowContent && mViewPager.getCurrentItem() == 2)
			disableFilterMap();
		else
			enableFilterUserMap();

		ObjectAnimator animator = null;
		int height = findViewById(R.id.layoutContentHolder).getHeight();
		View layout = findViewById(R.id.layoutContentFrame);
		if (mShowContent){
			animator = ObjectAnimator.ofFloat(layout, "translationY", -height, 0);
			mMapButton.setImageResource(R.drawable.menu_map);
		}
		else{
			mMapButton.setImageResource(R.drawable.menu_list);
			animator = ObjectAnimator.ofFloat(layout, "translationY", 0, -height);
			updateMap();
		}

		animator.setInterpolator(new AccelerateDecelerateInterpolator());
		animator.start();
	}

	///////////////////////////////////////////////////////////////////////////
	// Map stuff
	///////////////////////////////////////////////////////////////////////////

	private Shop mMapSelectedShop;

	public void updateMap() {
		mNeedUpdateMap = true;
	}

	public void updateMapAsync() {
		final GoogleMap map = mMapFragment.getMap();

		if (map == null)
			return;

		map.clear();
		map.setInfoWindowAdapter(new InfoWindowAdapter() {

			@Override
			public View getInfoContents(Marker arg0) {

				return null;
			}

			@Override
			public View getInfoWindow(Marker marker) {

				View view = MainActivity.this.getLayoutInflater().inflate(R.layout.map_shop_item, null);
				Shop s = getShopListFragment().mShopList.get(Integer.parseInt(marker.getSnippet()));

				TextView txtPoint = (TextView) view.findViewById(R.id.txtPoint);
				TextView txtMinPoint = (TextView) view.findViewById(R.id.txtMinPoint);
				TextView txtPointName = (TextView) view.findViewById(R.id.txtPointName);
				TextView txtDistance = (TextView) view.findViewById(R.id.txtDistance);
				TextView txtShopNae = (TextView) view.findViewById(R.id.txtShopName);
				TextView txtAddress = (TextView) view.findViewById(R.id.txtAddress);

				txtDistance.setText(Float.toString(s.mDistance) + " km");

				if (s.mPromotionStatus && s.mPromotion != null) {
					switch (s.mPromotion.getType()) {
					case 1: {
						PromotionTypeOne promo = (PromotionTypeOne) s.mPromotion; 
						txtPoint.setText("" + promo.mSGP);
						txtMinPoint.setText("/10");
						txtPointName.setText("POINT");
					}
					break;

					case 2: {
						PromotionTypeTwo promo = (PromotionTypeTwo) s.mPromotion;
						txtPoint.setText(promo.mMoney / 1000 + " K");
						txtMinPoint.setText("");
						txtPointName.setText("VND");
					}
					break;
					}

				} else { 
					txtPoint.setText("");
					txtMinPoint.setText("");
					txtPointName.setText("");
				}

				txtShopNae.setText(s.mName);
				txtAddress.setText(s.mAddress);

				// Get diretion
				mMapSelectedShop = s;
				if (s.polyline == null)
					RequestDirection(s, map);
				else
					ShowDirection(map);

				return view;
			}
		});

		if (getShopListFragment().mShopList == null)
			return;

		LatLngBounds.Builder builder = new LatLngBounds.Builder();

		final int[] iconIdArr = new int[] {
				R.drawable.iconpin_food,
				R.drawable.iconpin_drink,
				R.drawable.iconpin_healness,
				R.drawable.iconpin_entertaiment,
				R.drawable.iconpin_fashion,
				R.drawable.iconpin_travel,
				R.drawable.iconpin_shopping,
				R.drawable.iconpin_education};

		for (int i = 0; i < getShopListFragment().mShopList.size() && i < 30; i++) {

			Shop s = getShopListFragment().mShopList.get(i);
			LatLng ll = new LatLng(s.mLat, s.mLng);
			map.addMarker(new MarkerOptions().position(ll)
					.icon(BitmapDescriptorFactory.fromResource(iconIdArr[s.mGroupShop-1]))
					.snippet("" + i));

			builder.include(ll);
		}	

		if (getShopListFragment().mShopList.size() != 0)
			mBound = builder.build();

		map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
			@Override
			public void onInfoWindowClick(Marker marker) {
				Shop s = getShopListFragment().mShopList.get(Integer.parseInt(marker.getSnippet()));
				turnMapShop2Detail(s);
			}
		});
	}

	public void turnMapShop2Detail(Shop s) {
		if (mShowContent)
			return;

		mShowContent = !mShowContent;
		int height = findViewById(R.id.layoutContentHolder).getHeight();
		View layout = findViewById(R.id.layoutContentFrame);
		ObjectAnimator animator = ObjectAnimator.ofFloat(layout, "translationY", -height, 0);
		animator.setInterpolator(new AccelerateDecelerateInterpolator());

		GlobalVariable.mCurrentShop = s;
		getDetailFragment().setData(s);
		setNaviText(s.mName);
		mViewPager.setCurrentItem(2, false);

		animator.start();
		mIsNeedToggleMap = true;
	}

	public void createDestroyMap() {

		boolean isMapAlive = !mShowContent && !mShowCamera && !mShowUser && !mIsShowFilter;

		if (isMapAlive != mIsMapAlive) {
			mIsMapAlive = isMapAlive;
			if (mIsMapAlive) {

				getSupportFragmentManager().beginTransaction().add(R.id.layoutMap, mMapFragment).commit();
				getSupportFragmentManager().executePendingTransactions();
				if (mNeedUpdateMap)
					updateMapAsync();

				mMapFragment.getMap().setOnMyLocationChangeListener(new OnMyLocationChangeListener() {

					@Override
					public void onMyLocationChange(Location loc) {
						GlobalVariable.mLat = (float) loc.getLatitude();
						GlobalVariable.mLng = (float) loc.getLongitude();
					}
				});

				mMapFragment.getMap().setMyLocationEnabled(true);

				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						try {
							jumpToBound();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}, 1000);

				mNeedUpdateMap = false;
			} else {
				getSupportFragmentManager().beginTransaction().remove(mMapFragment).commit();
			}
		}
	}

	public void jumpToBound() {

		if (mBound == null || mMapFragment.getMap() == null)
			return;
		mMapFragment.getMap().animateCamera(CameraUpdateFactory.newLatLngBounds(mBound, 0));
	}

	private void RequestDirection(Shop nearby, final GoogleMap googleMap) {
		if (googleMap.getMyLocation() == null)
			return;

		double myLat = googleMap.getMyLocation().getLatitude();
		double myLon = googleMap.getMyLocation().getLongitude();
		double dstLat = nearby.mLat;
		double dstLon = nearby.mLng;
		Handler handler = new Handler() {

			private Shop nearby;

			public Handler init(Shop nearby) {
				this.nearby = nearby;
				return this;
			}

			@Override
			public void handleMessage(Message message) {
				switch (message.what) {
				case HttpConnection.DID_START: {
					break;
				}
				case HttpConnection.DID_SUCCEED: {
					String response = (String) message.obj;
					try {
						JSONObject rootJson = new JSONObject(response);
						HandleDirectionResponse(rootJson, nearby, googleMap);
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				}
				case HttpConnection.DID_ERROR: {
					Exception e = (Exception) message.obj;
					e.printStackTrace();
					break;
				}
				}
			}
		}.init(nearby);
		GetDirection(myLat, myLon, dstLat, dstLon, handler);
	}

	private void ShowDirection(GoogleMap googleMap) {

		if (mMapSelectedShop != null && mMapSelectedShop.polyline != null)
			mMapSelectedShop.polyline.setVisible(true);
		else
			return;

		for (Shop nearby : getShopListFragment().mShopList) {
			if (nearby.polyline != null)
				nearby.polyline.setVisible(nearby.equals(mMapSelectedShop));
		}

		LatLngBounds.Builder builder = LatLngBounds.builder();
		for (LatLng latlng : mMapSelectedShop.polyline.getPoints()) {
			builder.include(latlng);
		}

		googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 24));
	}

	private void HandleDirectionResponse(JSONObject rootJson, Shop nearby, GoogleMap googleMap) {

		//		if (nearby.polyline != null)
		//			return;

		// Decode polyline string
		String encoded_points = null;
		try {
			encoded_points = rootJson.getJSONArray("routes").getJSONObject(0)
					.getJSONObject("overview_polyline").getString("points");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		PolylineOptions mapOption = decodePoints(encoded_points);

		// Store the polyline
		nearby.polyline = googleMap.addPolyline(mapOption.color(0xff8080FF));

		// If this polyline is not being selected
		if (mMapSelectedShop != nearby) {
			// Hide it
			nearby.polyline.setVisible(false);
		} else {
			ShowDirection(googleMap);
		}
	}

	public static PolylineOptions decodePoints(String encoded_points){

		int index = 0;
		int lat = 0;
		int lng = 0;
		PolylineOptions polylineOption = new PolylineOptions();

		try {
			int shift;
			int result;

			while (index < encoded_points.length()) {
				shift = 0;
				result = 0;

				while (true) {
					int b = encoded_points.charAt(index++) - '?';
					result |= ((b & 31) << shift);
					shift += 5;
					if (b < 32)
						break;
				}
				lat += ((result & 1) != 0 ? ~(result >> 1) : result >> 1);

				shift = 0;
				result = 0;

				while (true) {
					int b = encoded_points.charAt(index++) - '?';
					result |= ((b & 31) << shift);
					shift += 5;
					if (b < 32)
						break;
				}
				lng += ((result & 1) != 0 ? ~(result >> 1) : result >> 1);
				/* Add the new Lat/Lng to the Array. */
				polylineOption = polylineOption.add(new LatLng((lat/1e5),(lng/1e5)));
			}
			return polylineOption;

		} catch(Exception e) {
			e.printStackTrace();
		}
		return polylineOption;
	}

	public static void GetDirection(double srcLat, double srcLon, double dstLat, double dstLon, Handler handler) {

		final String URLFormat = "http://maps.googleapis.com/maps/api/directions/json?origin=%f,%f&destination=%f,%f&sensor=true";

		String URL = String.format(Locale.US, URLFormat, srcLat, srcLon, dstLat, dstLon);

		new HttpConnection(handler).get(URL);
	}

	///////////////////////////////////////////////////////////////////////////

	public void initToggleCamera(){
		layoutTC = findViewById(R.id.relativeLayout2);
		heightTC = layoutTC.getHeight();
		btnQRTToogle = findViewById(R.id.btnQRToggle);
		textViewGetScore = findViewById(R.id.textViewGetScore);
	}

	public void toggleCamera() {

		if (!mShowCamera && (mScanningCode == 1 || mScanningCode == 2)){
			LocationManager locationManager = locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
			boolean isGPSOn = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			boolean isWifiOn = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if(!isGPSOn && !isWifiOn){
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(mActivity);
				String message = "";

				//if (!isGPSOn)// && !isWifiOn)
				message = "Báº¡n cáº§n báº­t GPS hoáº·c wireless location trÆ°á»›c khi scan code!!";
				//else
				//					if (!isGPSOn)
				//						message = "Báº¡n cáº§n báº­t GPS trÆ°á»›c khi scan code!!";
				//					else
				//						message = "Báº¡n cáº§n báº­t wireless location trÆ°á»›c khi scan code!!";

				alertDialog.setMessage(message);

				alertDialog.setPositiveButton("Thiáº¿t láº­p", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int which) {
						startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
					}
				});

				alertDialog.setNegativeButton("Há»§y", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
				alertDialog.show();
				return;
			}
		}

		RelativeLayout layoutQR = (RelativeLayout) findViewById(R.id.layoutQR);
		if (layoutQR.getVisibility() == View.GONE) {
			layoutQR.setVisibility(View.VISIBLE);
		} else {
			mMirror.setVisibility(View.INVISIBLE);
			mMirrorFront.setVisibility(View.INVISIBLE);
		}

		mShowCamera = !mShowCamera;

		if (mShowCamera){
			mCloseQRC.setVisibility(View.VISIBLE);

			switch(mScanningCode){
			case 1:
				QRCodeTextView.setText("TÃ­ch Ä‘iá»ƒm - Cá»­a hÃ ng sáº½ cung cáº¥p tháº» cho báº¡n");
				break;
			case 2:
			case 3:
				QRCodeTextView.setText("Nháº­n quÃ  - Cá»­a hÃ ng sáº½ cung cáº¥p tháº» cho báº¡n");
				break;
			}

			isCanScan = true;
			mIsCanWipe = true;
			//mScanCover.setVisibility(View.VISIBLE);

			if (GlobalVariable.mLat == -1){
				mMirrorFront.setVisibility(View.INVISIBLE);
				mMirror.setVisibility(View.VISIBLE);
				mShopNameText.setVisibility(View.INVISIBLE);
				mSGPText.setVisibility(View.INVISIBLE);
				mContentText.setVisibility(View.INVISIBLE);
				mProgressBar.setVisibility(View.VISIBLE);
				isCanScan = false;
				Toast.makeText(mActivity, "Vui lÃ²ng chá»� láº¥y tá»�a Ä‘á»™ GPS", Toast.LENGTH_LONG).show();
			}
		}
		else{
			QRCodeTextView.setText("CHáº M VÃ€O Ä�á»‚ NHáº¬N Ä�Iá»‚M");
			isCanScan = false;
			//mScanCover.setVisibility(View.INVISIBLE);
		}

		ObjectAnimator animatorPopup = null;
		ObjectAnimator animatorOptical= null;
		List<ObjectAnimator> arrayListObjectAnimators = new ArrayList<ObjectAnimator>();

		mOpticalFrame.setBackgroundColor(Color.rgb(40, 46, 58));
		heightTC = layoutTC.getHeight();
		cameraViewHeight = btnQRTToogle.getHeight() + textViewGetScore.getHeight();

		if (mShowCamera){
			animatorPopup = ObjectAnimator.ofFloat(layoutTC, "translationY", heightTC, 0);
			animatorOptical = ObjectAnimator.ofFloat(mOpticalFrame, "alpha", 0.0f, 1.0f);
		}
		else{
			animatorPopup = ObjectAnimator.ofFloat(layoutTC, "translationY", 0, heightTC - cameraViewHeight);
			animatorOptical = ObjectAnimator.ofFloat(mOpticalFrame, "alpha", 1.0f, 0.0f);
		}

		animatorPopup.addListener(new AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {}

			@Override
			public void onAnimationRepeat(Animator animation) {}

			@Override
			public void onAnimationEnd(Animator animation) {
				if (mShowCamera){
					InitCamera();
				}
				else{
					releaseCamera();
					mScanningCode = 1;
				}
			}

			@Override
			public void onAnimationCancel(Animator animation) {}
		});

		arrayListObjectAnimators.add(animatorPopup);
		arrayListObjectAnimators.add(animatorOptical);

		ObjectAnimator[] objectAnimators = arrayListObjectAnimators.toArray(new ObjectAnimator[arrayListObjectAnimators.size()]);
		AnimatorSet animSetXY = new AnimatorSet();
		animSetXY.playTogether(objectAnimators);
		animSetXY.setDuration(600);
		animSetXY.start();
	}

	public void InitCamera(){
		// create camera & qrcode scanner
		previewing = true;
		preview = (FrameLayout)findViewById(R.id.cameraPreview);
		autoFocusHandler = new Handler();
		mCamera = getCameraInstance();
		mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB);
		preview.addView(mPreview);
	}

	Camera.PreviewCallback previewCb = new Camera.PreviewCallback() {
		public void onPreviewFrame(byte[] data, Camera camera) {
			if (!isCanScan)
				return;
			Camera.Parameters parameters = camera.getParameters();
			int format = parameters.getPreviewFormat();
			Bitmap bmp = null;
			
			// YUV formats require more conversion
			if (format == ImageFormat.NV21 /*|| format == ImageFormat.YUY2 || format == ImageFormat.NV16*/)
			{
		    	int w = parameters.getPreviewSize().width;
		    	int h = parameters.getPreviewSize().height;

		    	YuvImage yuv_image = new YuvImage(data, format, w, h, null);

				Rect rect = new Rect(0, 0, w, h);
				ByteArrayOutputStream output_stream = new ByteArrayOutputStream();
				yuv_image.compressToJpeg(rect, 100, output_stream);

				bmp = BitmapFactory.decodeByteArray(output_stream.toByteArray(), 0, output_stream.size());
			}
			
			LuminanceSource source = new RGBLuminanceSource(bmp);
			BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
			DataMatrixReader readers = new DataMatrixReader();
			Result rs = null;
			
			try {
				rs = mDataMatrixReader.decode(bitmap);
			} catch (NotFoundException e) {
				try {
					rs = mQRCodeReader.decode(bitmap);
					
				} catch (NotFoundException e1) {
					return;
				} catch (ChecksumException e1) {
					return;
				} catch (FormatException e1) {
					return;
				}
				return;
			} catch (ChecksumException e) {
				return;
			} catch (FormatException e) {
				return;
			}

			Toast.makeText(MainActivity.this, rs.getText(), Toast.LENGTH_LONG).show();
			
			mCamera.setPreviewCallback(null);
			previewing = false;
			
			mQRCode = rs.getText();
			//mScanCover.setVisibility(View.INVISIBLE);
			switch (mScanningCode) {
			case 1:
				isCanScan = false;
				mIsCanWipe = false;
				new GetSGPPoint().execute();
				break;
			case 2:
				isCanScan = false;
				mIsCanWipe = false;
				new GetAwardType1().execute();
				break;
			case 3:
				isCanScan = false;
				mIsCanWipe = false;
				new GetAwardType2().execute();
			default:
				break;
			};
		}
	};

	private Runnable doAutoFocus = new Runnable() {
		public void run() {
			if (previewing)
				mCamera.autoFocus(autoFocusCB);
		}
	};

	Camera.AutoFocusCallback autoFocusCB = new Camera.AutoFocusCallback() {
		public void onAutoFocus(boolean success, Camera camera) {
			autoFocusHandler.postDelayed(doAutoFocus, GlobalVariable.focusInterval);
		}
	};

	@Override
	public void startAds() {
		mAdsFragment.startAds();
	}

	@Override
	public void stopAds() {
		mAdsFragment.stopAds();
	};

	@Override
	public void exit() {
		finish();
	}

	public void init(){		
		GlobalVariable.getLocation(this);

		((RelativeLayout)findViewById(R.id.rootOfroot)).setOnTouchListener(this);
		((RelativeLayout)findViewById(R.id.layoutQR)).setOnTouchListener(this);

		mActivity = this;

		// init cyImageLoader
		GlobalVariable.cyImageLoader = new CyImageLoader(this);

		// init
		try{
			NetworkManger.init();
		}catch(Exception e){
		}

		// Change policy
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);

		// Táº¡o database
		GlobalVariable.createDatbase(this);

		// Create menu
		menu = new SlidingMenu(this);
		menu.setMode(SlidingMenu.LEFT);
		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		menu.setShadowWidth(8);
		menu.setShadowDrawable(R.drawable.side_menu_shadow);
		menu.setBehindOffset(120);
		menu.setFadeDegree(0.35f);
		menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
		menu.setMenu(R.layout.side_menu_fragment);
		menu.setOnOpenedListener(new OnOpenedListener() {
			@Override
			public void onOpened() {
				mShowMenu = true;
			}
		});
		menu.setOnClosedListener(new OnClosedListener() {
			@Override
			public void onClosed() {
				mShowMenu = false;
			}
		});

		// Create fragment list
		mFragmentManager = getSupportFragmentManager();
		mFragmentList = new ArrayList<Fragment>();
		mFragmentList.add(Fragment.instantiate(this, CategoryListFragment.class.getName()));
		mFragmentList.add(Fragment.instantiate(this, ShopListFragment.class.getName()));
		mFragmentList.add(Fragment.instantiate(this, ShopDetailFragment.class.getName()));

		// Create viewpager & add fragment list into
		mViewPager = (SViewPager) findViewById(R.id.contentViewPager);
		mViewPager.setOffscreenPageLimit(5);
		mViewPager.setAdapter(new MainAdapter(mFragmentManager, mFragmentList));
		mViewPager.getAdapter().notifyDataSetChanged();
		mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());
		mViewPager.setPagingEnabled(false);
		mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int i, float v, int i2) {}

			@Override
			public void onPageSelected(int i) {
				if (i == 2){
					//					final Fragment scroll = getSupportFragmentManager().findFragmentById(R.id.adsFragment);
					//					final FragmentTransaction tr = getSupportFragmentManager().beginTransaction();
					//					//tr.setCustomAnimations(R.anim.slide_up, R.anim.slide_down);
					//					tr.hide(scroll);
					//					tr.commit();
				}
				else{
					//					final Fragment scroll = getSupportFragmentManager().findFragmentById(R.id.adsFragment);
					//					final FragmentTransaction tr = getSupportFragmentManager().beginTransaction();
					//					tr.show(scroll);
					//					tr.commit();
				}
			}

			@Override
			public void onPageScrollStateChanged(int i) {}
		});

		// Save fragment for later using
		mCategoryListFragment = ((CategoryListFragment)((MainAdapter)mViewPager.getAdapter()).getItem(0));
		mShopListFragment = ((ShopListFragment)((MainAdapter)mViewPager.getAdapter()).getItem(1));
		mShopDetailFragment = ((ShopDetailFragment)((MainAdapter)mViewPager.getAdapter()).getItem(2));
		mAdsFragment = (AdsFragment)getSupportFragmentManager().findFragmentById(R.id.adsFragment);
		mUserFragment = ((UserFragment) getSupportFragmentManager().findFragmentById(R.id.userFragment));
		mFiterFragment = ((FilterFragment) getSupportFragmentManager().findFragmentById(R.id.filterFragment));
		mMapFragment = new SupportMapFragment();

		// Set up category list fragment
		mCategoryListFragment.setListener(new Listener() {

			@Override
			public boolean onCategoryClick(int position) {
				if (GlobalVariable.json10FirstShop.length() != 0 && 
						GlobalVariable.mSortByString.compareTo("0") == 0) {
					getShopListFragment().update(GlobalVariable.json10FirstShop);
					goNextPage();
					return true;
				} else 
					return false;
			}

			@Override
			public void onFinishFirstTimeUpdate() {
				setLocation(GlobalVariable.mCityNames.get(GlobalVariable.mCityIDes.indexOf(GlobalVariable.mCityID)));
			}

			@Override
			public void onFinishLoadShopList(String json, boolean success, Exception e) {
				getShopListFragment().update(json);
			}

			@Override
			public void onFinishAnimation() {

				goNextPage();
			}
		});

		stopAds();

		// Setting layout
		mOpticalFrame = findViewById(R.id.opticalMainFrame);
		mLocationBtn = (RelativeLayout)menu.getMenu().findViewById(R.id.location_layout);
		mLocationTV = (TextView) menu.getMenu().findViewById(R.id.textView7);
		mNaviText = (TextView) findViewById(R.id.txtNavi);
		mAvatarFaceBtn = (ImageButton)menu.getMenu().findViewById(R.id.imageView1);
		//mTotalSGP = (TextView)menu.getMenu().findViewById(R.id.SGPScoreSetting);
		mTutorialBtn = (RelativeLayout)menu.getMenu().findViewById(R.id.tutorialBtn);
		mTutorialBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(mActivity, TutorActivity.class));
			}
		});

		mBtnIntro = (RelativeLayout)menu.getMenu().findViewById(R.id.btnIntro);
		mBtnIntro.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(mActivity, IntroActivity.class));
			}
		});

		gpsBtn = (RelativeLayout)menu.getMenu().findViewById(R.id.GPSButton);
		gpsBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				LocationManager locationManager = locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
				if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
					AlertDialog.Builder alertDialog = new AlertDialog.Builder(mActivity);
					alertDialog.setMessage("GPS chÆ°a Ä‘Æ°á»£c báº­t. Báº¡n cÃ³ muá»‘n thay Ä‘á»•i thiáº¿t láº­p");

					alertDialog.setPositiveButton("Thiáº¿t láº­p", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int which) {
							Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							startActivity(intent);
						}
					});

					alertDialog.setNegativeButton("Há»§y", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
					alertDialog.show();
				}
			}
		});

		reviewBtn = (RelativeLayout)menu.getMenu().findViewById(R.id.reviewSmartGuide);
		reviewBtn.setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View v) {
				GlobalVariable.isNeedPostReview = false;
				startActivityForResult(new Intent(mActivity, ReviewActivity.class), ReviewRequestCode);
			}
		});

		updateBtn = (RelativeLayout)menu.getMenu().findViewById(R.id.updateBtn);
		updateBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//startActivityForResult(new Intent(mActivity, UpdateActivity.class), UpdateRequestCode);
				Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=vn.smartguide");
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
		});

		authButton = (LoginButton)menu.getMenu().findViewById(R.id.authButtonSetting);
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

		mAvatarFaceBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (GlobalVariable.avatarFace.compareTo("null") == 0){
					authButton.performClick();
				}
			}
		});

		mLocationBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				updateLocation();
			}
		});

		mMirror = (LinearLayout) findViewById(R.id.mirror);
		mMirrorFront = (RelativeLayout) findViewById(R.id.monitor_front);
		mCloseBtn = (ImageButton)findViewById(R.id.closeButton);
		mCloseBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				isCanScan = true;
				mIsCanWipe = true;

				//				if (mIsNeedGotoDetail){
				//					new QCToDetail().execute();
				//				}
				//				else
				toggleCamera();
			}
		});

		mSGPText = (TextView)findViewById(R.id.sgpText);
		mContentText = (TextView)findViewById(R.id.errorText);
		mShopNameText = (TextView)findViewById(R.id.shopName);

		// Set menu button
		((ImageButton) findViewById(R.id.btnToggleMenu)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				toggleSideMenu();
			}
		});

		// Set qrcode button
		((ImageButton) findViewById(R.id.btnQRToggle)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				toggleCamera();
				createDestroyMap();
			}
		});

		// Set google map button
		mMapButton = ((ImageButton)findViewById(R.id.btnToggleMap));
		mMapButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mViewPager.getCurrentItem() == 0)
					return;
				toggleShowContent();
				createDestroyMap();
			}
		});

		// Set user button
		mUserButton = ((ImageButton)findViewById(R.id.btnUser));
		mUserButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				toogleUser();
				createDestroyMap();
			}
		});

		// Set filter button
		mFilterBtn = ((ImageButton) findViewById(R.id.btnToggleFilter));
		mFilterBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mFiterFragment.toggle();
				mIsShowFilter = !mIsShowFilter;
				if (mIsShowFilter){
					disableUserMap();
					setNaviText("Bá»˜ Lá»ŒC");
				}
				else{
					enableUserMap();
					setNaviText(mPreviousNavi);
				}

				createDestroyMap();
			}
		});

		// Set search button
		((ImageButton) findViewById(R.id.btnSearch)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				OnSearchButtonClick();
			}
		});

		//
		mProgressBar = (ProgressBar)findViewById(R.id.progressBar1);
		mScanCover = (ImageView)findViewById(R.id.scanCover); 
		QRCodeTextView = (TextView)findViewById(R.id.textViewGetScore);
		mCloseQRC = (ImageButton)findViewById(R.id.closeQRCode);
		mCloseQRC.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {				
				toggleCamera();
			}
		});

		initToggleCamera();
	}

	private boolean mShowSearch = false;
	void OnSearchButtonClick() {

		EditText edtSearch = (EditText) findViewById(R.id.edtSearch);
		ImageButton btnSearch = (ImageButton) findViewById(R.id.btnSearch);
		ImageButton btnToggleMenu = (ImageButton) findViewById(R.id.btnToggleMenu);
		ImageButton btnToggleFilter = (ImageButton) findViewById(R.id.btnToggleMap);

		// Set search onscreen keyboard event
		edtSearch.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				OnSearchButtonClick();
				return true;
			}
		});

		int width = (int) (btnToggleFilter.getX() - btnToggleMenu.getX() - btnToggleMenu.getWidth() + btnToggleFilter.getWidth());
		ObjectAnimator animator = null;
		ObjectAnimator animator2 = null;
		mShowSearch = !mShowSearch;

		if (mShowSearch) {
			// Show search box
			animator = ObjectAnimator.ofInt(edtSearch, "width", 0, width);
			animator.addListener(new AnimatorListener() {
				public int searchWidth;
				public AnimatorListener init(int w) {
					searchWidth = w;
					return this;
				}
				public void onAnimationRepeat(Animator animation) { }
				public void onAnimationStart(Animator animation) {
					showSearchBox();
				}
				public void onAnimationEnd(Animator animation) { }
				public void onAnimationCancel(Animator animation) { }
			}.init(btnSearch.getWidth()));

			animator2 = ObjectAnimator.ofFloat(btnSearch, "translationX", 
					0, btnToggleFilter.getX() - btnToggleMenu.getX() - btnToggleMenu.getWidth());
		} else {
			// Hide search box
			animator = ObjectAnimator.ofInt(edtSearch, "width", width, 0);
			animator.addListener(new AnimatorListener() {
				public int searchWidth;
				public AnimatorListener init(int w) {
					searchWidth = w;
					return this;
				}
				public void onAnimationRepeat(Animator animation) { }
				public void onAnimationStart(Animator animation) { }
				public void onAnimationEnd(Animator animation) {
					hideSearchBox();
				}
				public void onAnimationCancel(Animator animation) {
					hideSearchBox();
				}
			}.init(btnSearch.getWidth()));

			animator2 = ObjectAnimator.ofFloat(btnSearch, "translationX", 
					btnToggleFilter.getX() - btnToggleMenu.getX() - btnToggleMenu.getWidth(), 0);
			String search = edtSearch.getText().toString();
			edtSearch.setText("");
			performSearch(search);
		}

		TimeInterpolator acce = new AccelerateDecelerateInterpolator();
		animator.setInterpolator(acce);
		animator2.setInterpolator(acce);
		animator.start();
		animator2.start();
	}

	private void performSearch(String name) {
		goToPage(1);
		setNaviText(name);
		getShopListFragment().search(name);
	}

	public void hideSearchBox() {

		EditText edtSearch = (EditText) findViewById(R.id.edtSearch);
		edtSearch.setVisibility(View.INVISIBLE);
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(edtSearch.getWindowToken(), 0);
	}

	public void showSearchBox() {

		EditText edtSearch = (EditText) findViewById(R.id.edtSearch);
		edtSearch.setVisibility(View.VISIBLE);
		InputMethodManager imm = (InputMethodManager)getSystemService(
				Context.INPUT_METHOD_SERVICE);
		edtSearch.requestFocus();
		imm.toggleSoftInput(0, 0);
	}

	void updateLocation(){
		final String items[] = GlobalVariable.mCityNames.toArray(new String[GlobalVariable.mCityNames.size()]);

		AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this);
		ab.setTitle("Chá»�n thÃ nh phá»‘:");
		ab.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (mChoiceLocation == -1)
					return;

				GlobalVariable.mCityID = GlobalVariable.mCityIDes.get(mChoiceLocation);
				mLocationTV.setText(GlobalVariable.mCityNames.get(mChoiceLocation));
				toggleSideMenu();
				goToPage(0);
				((CategoryListFragment)mFragmentList.get(0)).autoUpdate();

				GlobalVariable.smartGuideDB.deleteUserSetting();
				HashMap<String, String> token =  new  HashMap<String, String>();
				token.put("versionID", GlobalVariable.mVersion);
				token.put("cityID", GlobalVariable.mCityID);
				GlobalVariable.smartGuideDB.insertVersion(token);
			}
		});

		ab.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});

		ab.setSingleChoiceItems(items, GlobalVariable.mCityIDes.indexOf(GlobalVariable.mCityID), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mChoiceLocation = which;
			}
		});

		ab.show();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		ConnectionManager.instance = null;
		GlobalVariable.smartGuideDB = null;
		mUiHelper.onDestroy();
	}

	@Override
	public void setNaviText(String naviText) {
		mPreviousNavi = mCurrentNavi;
		mCurrentNavi = naviText;
		mNaviText.setText(mCurrentNavi);
	}

	private UiLifecycleHelper 	mUiHelper;

	private void makeMeRequest(final Session session) {
		Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
			public void onCompleted(GraphUser user, Response response) {
				if (session == Session.getActiveSession()) {
					if (user != null) {
						GlobalVariable.id = user.getId();
						GlobalVariable.user_id = GlobalVariable.userID;
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
						GlobalVariable.getFacebookFromDB();

						ImageView avatar = (ImageView)menu.getMenu().findViewById(R.id.userAvatarSetting);
						TextView name = (TextView)menu.getMenu().findViewById(R.id.textView);
						name.setMaxLines(1);
						name.setText(GlobalVariable.nameFace);
						GlobalVariable.imageLoader.displayImage(GlobalVariable.avatarFace, avatar);
						mUserFragment.updateAvatar();

						new PushFacebookInfo().execute();
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

	void updateInformation(){

		//getAndUploadContact();		
		// Update SGP at Setting view

		if (checkPlayServices()) {
			gcm = GoogleCloudMessaging.getInstance(this);
			regid = getRegistrationId(getApplicationContext());

			if (regid.isEmpty()) {
				new RegisterGCM().execute();
			}
		} else {
		}

		new GetUserCollection().execute();
		new GetRewardList().execute();
		new FindShopList().execute();

		// Update name and avatar facebook if possible
		ImageView avatar = (ImageView)menu.getMenu().findViewById(R.id.userAvatarSetting);
		TextView name = (TextView)menu.getMenu().findViewById(R.id.textView);
		name.setMaxLines(1);

		if (GlobalVariable.avatarFace.compareTo("null") != 0){
			name.setText(GlobalVariable.nameFace);
			GlobalVariable.imageLoader.displayImage(GlobalVariable.avatarFace, avatar);
			mUserFragment.updateAvatar();
		}
		else
			name.setText("Anomynous User");

		// Update city list at first time
		// Delete old version
		GlobalVariable.smartGuideDB.deleteUserSetting();
		GlobalVariable.smartGuideDB.deleteCity();

		HashMap<String, String> token =  new  HashMap<String, String>();
		token.put("versionID", GlobalVariable.mVersion);
		token.put("cityID", GlobalVariable.mCityID); // default for first time = 1

		GlobalVariable.smartGuideDB.insertVersion(token);

		//new UpdateCityList().execute();
		mCategoryListFragment.firstTimeUpdate();
		mAdsFragment.startDownImage();

		mMapButton.setClickable(true);
		mMapButton.setImageResource(R.drawable.map_btn);
	}

	void exitWithError(){
		AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);                      
		dlgAlert.setMessage("Báº­t káº¿t ná»‘i máº¡ng vÃ  thá»­ láº¡i");
		dlgAlert.setTitle("Lá»—i káº¿t ná»‘i máº¡ng");
		dlgAlert.setPositiveButton("OK",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				finish(); 
			}
		});

		dlgAlert.setCancelable(false);
		dlgAlert.create().show();
	}

	public void toggleSideMenu() {
		mShowMenu = !mShowMenu;
		menu.toggle();
	}

	public void toogleUser(){
		mShowUser = !mShowUser;

		if (mShowUser){
			if (GlobalVariable.avatarFace.compareTo("null") != 0)
				setNaviText(GlobalVariable.nameFace);
			else
				setNaviText("User");

			mFilterBtn.setImageResource(R.drawable.menu_filter_lock);
			mMapButton.setImageResource(R.drawable.menu_map_lock);
			mFilterBtn.setClickable(false);
			mMapButton.setClickable(false);
		}
		else{
			setNaviText(mPreviousNavi);

			mFilterBtn.setImageResource(R.drawable.menu_filter_lock);
			mMapButton.setImageResource(R.drawable.menu_map_lock);

			if (!GlobalVariable.mIsLaunching){
				mFilterBtn.setClickable(true);
				mMapButton.setClickable(true);
				mFilterBtn.setImageResource(R.drawable.menu_filter);
				mMapButton.setImageResource(R.drawable.map_btn);
			}
		}

		mUserFragment.toggle();
	}

	private void releaseCamera() {
		if (mCamera != null) {
			preview.removeAllViews();
			previewing = false;
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
		}
	}

	public Camera getCameraInstance(){
		Camera c = null;
		int cameraId = -1;
		boolean errorFound = false;

		boolean hasFeatCamera = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
		if (hasFeatCamera){
			try{
				c = Camera.open(CameraInfo.CAMERA_FACING_BACK);
			}catch(Exception ex){
				c = Camera.open(0);
			}
		}else if(CameraInfo.CAMERA_FACING_FRONT>-1){
			try{
				c = Camera.open(CameraInfo.CAMERA_FACING_FRONT);
			}catch(Exception e){
				errorFound = true;
			}
		}

		if (errorFound){
			try{
				c = Camera.open(0);
			}catch(Exception ex){
				cameraId = -1;
			}
		}

		return c;
	}

	@Override
	public ShopListFragment getShopListFragment() {
		return mShopListFragment;
	}

	@Override
	public ShopDetailFragment getDetailFragment() {
		return mShopDetailFragment;
	}

	public class MainAdapter extends FragmentPagerAdapter {
		private List<Fragment> fragments;

		@Override
		public Fragment getItem(int i) {
			return this.fragments.get(i);
		}

		public MainAdapter(FragmentManager fm, List<Fragment> fragments) {
			super(fm);
			this.fragments = fragments;
		}

		@Override
		public int getCount() {
			return this.fragments.size();
		}
	}

	@Override
	public void onBackPressed() {	
		if (mShowMenu){
			toggleSideMenu();
			return;
		}

		if (mShowCamera){
			toggleCamera();
			return;
		}

		if (mShowUser){
			toogleUser();
			return;
		}

		if (mIsShowFilter){
			mIsShowFilter = !mIsShowFilter;
			mFiterFragment.toggle();
			return;
		}

		if(!mShowContent){
			toggleShowContent();
			return;
		}

		if(mIsNeedToggleUser){
			mIsNeedToggleUser = !mIsNeedToggleUser;
			if (!mShowUser){
				toogleUser();
				return;
			}
		}

		if (mIsNeedToggleMap){
			mIsNeedToggleMap = !mIsNeedToggleMap;
			if (mShowContent){
				toggleShowContent();
				return;
			}
		}

		goPreviousPage();
		return;
	}

	@Override
	public void getAwardTypeOne(int award_id) {
		mAwardId = award_id;
		mScanningCode = 2;
		toggleCamera();
	}

	@Override
	public void userToDetail() {
		mIsNeedToggleUser = true;
		toogleUser();
		setNaviText(GlobalVariable.mCurrentShop.mName);
		mViewPager.setCurrentItem(2, false);
	}

	@Override
	public void getAwardTypeTwo(int award_id) {
		mAwardId = award_id;
		mScanningCode = 3;
		toggleCamera();
	}

	public void disableAll(){
		mAdsFragment.startDownImage();

		ImageView view = (ImageView)findViewById(R.id.launchingLayout);
		view.setVisibility(View.VISIBLE);

		GlobalVariable.imageLoader.displayImage(GlobalVariable.mURL, view);
		mFilterBtn.setClickable(false);
		mMapButton.setClickable(false);
		mLocationBtn.setClickable(false);
		((ImageButton) findViewById(R.id.btnSearch)).setClickable(false);
		((ImageButton) findViewById(R.id.btnQRToggle)).setClickable(false);

		mFilterBtn.setImageResource(R.drawable.menu_filter_lock);
		mMapButton.setImageResource(R.drawable.menu_map_lock);
	}

	public class GetAwardType2 extends AsyncTask<Void, Void, Boolean> {
		JSONObject JSResult = null;
		int id = -1;
		@Override
		protected Boolean doInBackground(Void... params) {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("user_id", GlobalVariable.userID));
			pairs.add(new BasicNameValuePair("promotion_2_id", Integer.toString(mAwardId)));
			pairs.add(new BasicNameValuePair("code", mQRCode));
			pairs.add(new BasicNameValuePair("user_lat", Float.toString(GlobalVariable.mLat)));
			pairs.add(new BasicNameValuePair("user_lng", Float.toString(GlobalVariable.mLng)));

			String json = NetworkManger.post(APILinkMaker.mGetAwardType2(), pairs);
			try {
				JSResult = new JSONObject(json);
			} catch (JSONException e) {
			}

			return true;
		}

		@Override
		protected void onPostExecute(Boolean k){
			int sgp = 0;
			try{
				int status = JSResult.getInt("status");
				switch(status){
				case 0:
					mShopNameText.setVisibility(View.INVISIBLE);
					mSGPText.setVisibility(View.INVISIBLE);
					mContentText.setVisibility(View.VISIBLE);
					mContentText.setText(JSResult.getString("content"));
					break;
				case 1:
					mShopNameText.setVisibility(View.VISIBLE);
					mSGPText.setVisibility(View.VISIBLE);
					mContentText.setVisibility(View.VISIBLE);
					mShopNameText.setText(JSResult.getString("shop_name"));
					mSGPText.setText(JSResult.getString("money") + " VNÄ�");
					mContentText.setText("Báº¡n nháº­n Ä‘Æ°á»£c phiáº¿u giáº£m giÃ¡");

					try {
						String url = new JSONObject(mQRCode).getString("url");
						id = Integer.valueOf(url.substring(url.lastIndexOf('/') + 1, url.length()));
						if (GlobalVariable.mCurrentShop != null && id == GlobalVariable.mCurrentShop.mID){
							isNeedUpdateSGP = true;
						};
					} catch (JSONException e) {
					}

					if (!mUserFragment.updateSGP(id, 0))
						new GetUserCollection().execute();
					else
						new UpdateTotalSGP().execute();
					break;
				}
			}catch(Exception ex){
				return;
			}

			mMirror.setVisibility(View.VISIBLE);
			mMirrorFront.setVisibility(View.VISIBLE);
		}

		@Override
		protected void onPreExecute(){
			mCloseQRC.setVisibility(View.INVISIBLE);
		}
	}

	public class PushFacebookInfo extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			return true;
		}

		@Override
		protected void onPostExecute(Boolean k){
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("fb_id", GlobalVariable.id));
			pairs.add(new BasicNameValuePair("user_id", GlobalVariable.user_id));
			pairs.add(new BasicNameValuePair("email", GlobalVariable.email));
			pairs.add(new BasicNameValuePair("name", GlobalVariable.name));
			pairs.add(new BasicNameValuePair("gender", String.valueOf(GlobalVariable.gender)));
			pairs.add(new BasicNameValuePair("dob", GlobalVariable.dob));
			pairs.add(new BasicNameValuePair("avatar", GlobalVariable.avatar));
			pairs.add(new BasicNameValuePair("job", GlobalVariable.job));

			NetworkManger.post(APILinkMaker.mPushInforFacebook(), pairs);
		}

		@Override
		protected void onPreExecute(){}
	}

	public class GetSGPPoint extends AsyncTask<Void, Void, Boolean> {
		JSONObject JSResult = null;
		int id = -1;

		@Override
		protected Boolean doInBackground(Void... params) {
			isNeedUpdateSGP = false;
			mIsNeedGotoDetail = false;

			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("user_id", GlobalVariable.userID));
			pairs.add(new BasicNameValuePair("code", mQRCode));
			pairs.add(new BasicNameValuePair("user_lat", Float.toString(GlobalVariable.mLat)));
			pairs.add(new BasicNameValuePair("user_lng", Float.toString(GlobalVariable.mLng)));

			String json = NetworkManger.post(APILinkMaker.mGetSGP(), pairs);
			try {
				JSResult = new JSONObject(json);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean k){
			int sgp = 0;
			mProgressBar.setVisibility(View.INVISIBLE);
			try{
				int status = JSResult.getInt("status");
				switch(status){
				case 0:
					mShopNameText.setVisibility(View.INVISIBLE);
					mSGPText.setVisibility(View.INVISIBLE);
					mContentText.setVisibility(View.VISIBLE);
					mContentText.setText(JSResult.getString("content"));
					break;
				case 1:
					mShopNameText.setVisibility(View.INVISIBLE);
					mSGPText.setVisibility(View.INVISIBLE);
					mContentText.setVisibility(View.VISIBLE);
					mContentText.setText(JSResult.getString("content"));
					break;
				case 2:
					mShopNameText.setVisibility(View.INVISIBLE);
					mSGPText.setVisibility(View.INVISIBLE);
					mContentText.setVisibility(View.VISIBLE);
					mContentText.setText(JSResult.getString("content"));
					break;
				case 3:
					mShopNameText.setVisibility(View.VISIBLE);
					mSGPText.setVisibility(View.VISIBLE);
					mContentText.setVisibility(View.VISIBLE);
					mShopNameText.setText(JSResult.getString("shop_name"));
					sgp = JSResult.getInt("sgp");
					mSGPText.setText(Integer.toString(sgp) + "SGP");
					mContentText.setText("ChÃºc má»«ng báº¡n nháº­n Ä‘Æ°á»£c");

					int total_sgp = JSResult.getInt("total_sgp");

					try {
						String url = new JSONObject(mQRCode).getString("url");
						id = Integer.valueOf(url.substring(url.lastIndexOf('/') + 1, url.length()));
						if (GlobalVariable.mCurrentShop != null && id == GlobalVariable.mCurrentShop.mID){
							isNeedUpdateSGP = true;
						};
					} catch (JSONException e) {
					}

					mShopListFragment.updateSGP(id, total_sgp);

					if (mShopDetailFragment.getPromoFragment() != null && isNeedUpdateSGP)
						mShopDetailFragment.getPromoFragment().setSGP(total_sgp);
					else{
						if (GlobalVariable.mCurrentShop != null)
							((PromotionTypeOne)GlobalVariable.mCurrentShop.mPromotion).mSGP = total_sgp;
					}

					if (!mUserFragment.updateSGP(id, total_sgp))
						new GetUserCollection().execute();
					else
						new UpdateTotalSGP().execute();

					mIsNeedGotoDetail = true;
					break;
				}
			}catch(Exception ex){
				return;

			}

			mMirror.setVisibility(View.VISIBLE);
			mMirrorFront.setVisibility(View.VISIBLE);
		}

		@Override
		protected void onPreExecute(){
			mCloseQRC.setVisibility(View.INVISIBLE);
			mProgressBar.setVisibility(View.VISIBLE);
		}
	}

	public class GetUserCollection extends AsyncTask<Void, Void, Boolean> {
		String JSResult = null;

		@Override
		protected Boolean doInBackground(Void... params) {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("user_id", GlobalVariable.userID));
			pairs.add(new BasicNameValuePair("user_lat", Float.toString(GlobalVariable.mLat)));
			pairs.add(new BasicNameValuePair("user_lng", Float.toString(GlobalVariable.mLng)));
			pairs.add(new BasicNameValuePair("page", "0"));

			JSResult = NetworkManger.post(APILinkMaker.mGetUserCollection(), pairs);
			return true;
		}

		@Override
		protected void onPostExecute(Boolean k){
			try {
				JSONObject obj = new JSONObject(JSResult);
				String score = Integer.toString(obj.getInt("score"));
				mUserFragment.updateScore(score);
				//mTotalSGP.setText(score + " P");

				List<Shop> mShops = Shop.getListForUse(obj.getJSONArray("collection"));
				mUserFragment.update(mShops);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		protected void onPreExecute(){}
	}

	public class GetRewardList extends AsyncTask<Void, Void, Boolean> {
		String result = null;

		@Override
		protected Boolean doInBackground(Void... params) {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("user_id", GlobalVariable.userID));

			result = NetworkManger.post(APILinkMaker.mGetRewardList(), pairs);
			return true;
		}

		@Override
		protected void onPostExecute(Boolean k) {
			try {
				JSONArray jGiftArr = new JSONArray(result);
				List<GiftItem> giftList = new ArrayList<GiftItem>();

				// Parse json to get reward list
				for (int i = 0; i < jGiftArr.length(); i++) {
					JSONObject jGift = jGiftArr.getJSONObject(i);
					GiftItem item 	= new GiftItem();
					item.content 	= jGift.getString("content");
					item.id 		= jGift.getInt("id");
					item.score 		= jGift.getInt("score");
					item.status 	= jGift.getInt("status");
					giftList.add(item);
				}

				mUserFragment.updateRewardList(giftList);

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		protected void onPreExecute(){}
	}

	public class GetAwardType1 extends AsyncTask<Void, Void, Boolean> {
		JSONObject JSResult = null;
		int id = -1;
		@Override
		protected Boolean doInBackground(Void... params) {
			isNeedUpdateSGP = false;
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("user_id", GlobalVariable.userID));
			pairs.add(new BasicNameValuePair("award_id", Integer.toString(mAwardId)));
			pairs.add(new BasicNameValuePair("code", mQRCode));

			String json = NetworkManger.post(APILinkMaker.mGetAwardType1(), pairs);
			try {
				JSResult = new JSONObject(json);
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return true;
		}

		@Override
		protected void onPostExecute(Boolean k){
			int sgp = 0;
			mProgressBar.setVisibility(View.INVISIBLE);
			try{
				int status = JSResult.getInt("status");
				switch(status){
				case 0:
					mShopNameText.setVisibility(View.INVISIBLE);
					mSGPText.setVisibility(View.INVISIBLE);
					mContentText.setVisibility(View.VISIBLE);
					mContentText.setText(JSResult.getString("content"));
					break;
				case 1:
					mShopNameText.setVisibility(View.INVISIBLE);
					mSGPText.setVisibility(View.INVISIBLE);
					mContentText.setVisibility(View.VISIBLE);
					mContentText.setText(JSResult.getString("content"));
					break;
				case 2:
					mShopNameText.setVisibility(View.VISIBLE);
					mSGPText.setVisibility(View.VISIBLE);
					mContentText.setVisibility(View.VISIBLE);
					mShopNameText.setText(JSResult.getString("shop_name"));
					mSGPText.setText(JSResult.getString("award"));
					mContentText.setText("ChÃºc má»«ng báº¡n nháº­n Ä‘Æ°á»£c");
					int total_sgp = JSResult.getInt("total_sgp");

					try {
						String url = new JSONObject(mQRCode).getString("url");
						id = Integer.valueOf(url.substring(url.lastIndexOf('/') + 1, url.length()));
						if (GlobalVariable.mCurrentShop != null && id == GlobalVariable.mCurrentShop.mID){
							isNeedUpdateSGP = true;
						};
					} catch (JSONException e) {
					}

					mShopListFragment.updateSGP(id, total_sgp);
					if (mShopDetailFragment.getPromoFragment() != null && isNeedUpdateSGP)
						mShopDetailFragment.getPromoFragment().setSGP(total_sgp);
					else{
						if (GlobalVariable.mCurrentShop != null)
							((PromotionTypeOne)GlobalVariable.mCurrentShop.mPromotion).mSGP = total_sgp;
					}

					if (!mUserFragment.updateSGP(id, total_sgp))
						new GetUserCollection().execute();
					else
						new UpdateTotalSGP().execute();
					break;
				}
			}catch(Exception ex){
				return;
			}

			mMirror.setVisibility(View.VISIBLE);
			mMirrorFront.setVisibility(View.VISIBLE);
		}

		@Override
		protected void onPreExecute(){
			mCloseQRC.setVisibility(View.INVISIBLE);
			mProgressBar.setVisibility(View.VISIBLE);
		}
	}

	public class UpdateTotalSGP extends AsyncTask<Void, Void, Boolean> {
		String mJson;
		@Override
		protected Boolean doInBackground(Void... params) {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("user_id", GlobalVariable.userID));

			mJson = NetworkManger.post(APILinkMaker.mGetTotalSGP(), pairs);
			try {
			} catch (Exception e) {}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean k){
			try {
				mUserFragment.updateScore(Integer.toString((new JSONObject(mJson)).getInt("score")));
				new GetUserCollection().execute();
			} catch (JSONException e) {
			}
		}

		@Override
		protected void onPreExecute(){

		}
	}

	@Override
	public void setLocation(String cityName) {
		mLocationTV.setText(cityName);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN: {
			downX = event.getX();
			downY = event.getY();
			return true;
		}
		case MotionEvent.ACTION_UP: {
			upX = event.getX();
			upY = event.getY();

			float deltaX = downX - upX;
			float deltaY = downY - upY;

			if(Math.abs(deltaX) > MIN_DISTANCE){
				// left or right
				if(deltaX < 0) { this.onLeftToRightSwipe(); return true; }
				if(deltaX > 0) { this.onRightToLeftSwipe(); return true; }
			}
			else {
			}

			if(Math.abs(deltaY) > MIN_DISTANCE){
				// top or down
				if(deltaY < 0) { this.onTopToBottomSwipe(); return true; }
				if(deltaY > 0) { this.onBottomToTopSwipe(); return true; }
			}
			else {
			}

			return true;
		}
		}
		return false;
	}

	public void onRightToLeftSwipe(){}

	public void onLeftToRightSwipe(){}

	public void onTopToBottomSwipe(){
		if (mShowCamera && mIsCanWipe && GlobalVariable.mMode != 0)
			toggleCamera();
	}

	public void onBottomToTopSwipe(){
	}

	public class PostReview extends AsyncTask<Void, Void, Boolean> {
		String mJson;
		@Override
		protected Boolean doInBackground(Void... params) {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("user_id", GlobalVariable.userID));
			pairs.add(new BasicNameValuePair("feedback", GlobalVariable.reviewString));

			mJson = NetworkManger.post(APILinkMaker.mPostReview(), pairs);
			try {
			} catch (Exception e) {}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean k){}

		@Override
		protected void onPreExecute(){}
	}

	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);  // Add this method.
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);  // Add this method.
	}

	public void getAndUploadContact(){
		Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
		ContentResolver cr = getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,null, null, null, null);
		String[] projection    = new String[] {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Phone.NUMBER };
		Cursor names = getContentResolver().query(uri, projection, null, null, null);

		int indexName = names.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
		int indexNumber = names.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
		names.moveToFirst();
		JSONArray contacts = new JSONArray();

		while (names.moveToNext()) {
			JSONObject contact = new JSONObject();
			JSONArray phoneNumber = new JSONArray();
			try{
				contact.put("name", names.getString(indexName));
				phoneNumber.put(0, names.getString(indexNumber));
				contact.put("phone", phoneNumber);
				contacts.put(contact);

			}catch(Exception ex){

			}
		}
		new PostContact(contacts.toString()).execute();
	}

	public class PostContact extends AsyncTask<Void, Void, Boolean> {
		String mJson;

		public PostContact(String mjson){
			mJson = mjson;

		}

		@Override
		protected Boolean doInBackground(Void... params) {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("user_id", GlobalVariable.userID));
			pairs.add(new BasicNameValuePair("contact", mJson));

			mJson = NetworkManger.post(APILinkMaker.mPostContact(), pairs);
			try {
			} catch (Exception e) {}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean k){}

		@Override
		protected void onPreExecute(){}
	}

	public void confirmExit(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Báº¡n muá»‘n Ä‘Ã³ng á»©ng dá»¥ng");
		builder.setCancelable(true);

		builder.setPositiveButton("CÃ³", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				stopAds();
				finish();
			}
		});

		builder.setNegativeButton("KhÃ´ng", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			}
		});

		AlertDialog dialog = builder.show();
		TextView messageView = (TextView)dialog.findViewById(android.R.id.message);
		messageView.setGravity(Gravity.CENTER);
	}

	@Override
	public void updateTotalSGP(String score) {
		//		mTotalSGP.setText(score + " P");
	}

	public void enableFilterMap(){
		mFilterBtn.setClickable(true);
		mMapButton.setClickable(true);
		mFilterBtn.setImageResource(R.drawable.menu_filter);
		mMapButton.setImageResource(R.drawable.map_btn);
	}

	public void disableFilterMap(){
		mFilterBtn.setImageResource(R.drawable.menu_filter_lock);
		mMapButton.setImageResource(R.drawable.menu_map_lock);
		mFilterBtn.setClickable(false);
		mMapButton.setClickable(false);
	}

	public void enableUserMap(){
		mUserButton.setClickable(true);
		mMapButton.setClickable(true);
		mUserButton.setImageResource(R.drawable.user_btn);
		mMapButton.setImageResource(R.drawable.map_btn);
	}

	public void disableUserMap(){
		mUserButton.setClickable(false);
		mMapButton.setClickable(false);
		mUserButton.setImageResource(R.drawable.user_btn);
		mMapButton.setImageResource(R.drawable.menu_map_lock);
	}

	public void enableFilterUserMap(){
		mFilterBtn.setClickable(true);
		mMapButton.setClickable(true);
		mUserButton.setClickable(true);
		mFilterBtn.setImageResource(R.drawable.menu_filter);
		mMapButton.setImageResource(R.drawable.map_btn);
		mUserButton.setImageResource(R.drawable.user_btn);
	}

	public class QCToDetail extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {

			return true;
		}

		@Override
		protected void onPostExecute(Boolean k){
			mProgressBar.setVisibility(View.INVISIBLE);
		}

		@Override
		protected void onPreExecute(){
			mMirrorFront.setVisibility(View.INVISIBLE);
			mShopNameText.setVisibility(View.INVISIBLE);
			mSGPText.setVisibility(View.INVISIBLE);
			mContentText.setVisibility(View.INVISIBLE);
			mProgressBar.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void finishGetSGP() {
		isCanScan = true;
		mMirror.setVisibility(View.INVISIBLE);
		mProgressBar.setVisibility(View.INVISIBLE);
	}

	public class FindShopList extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			GlobalVariable.json10FirstShop = "";
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("group_list", "1,2,3,4,5,6,7,8"));
			pairs.add(new BasicNameValuePair("city_id", GlobalVariable.mCityID));
			pairs.add(new BasicNameValuePair("user_id", GlobalVariable.userID));
			pairs.add(new BasicNameValuePair("user_lat", Float.toString(GlobalVariable.mLat)));
			pairs.add(new BasicNameValuePair("user_lng", Float.toString(GlobalVariable.mLng)));
			pairs.add(new BasicNameValuePair("page", "0"));
			pairs.add(new BasicNameValuePair("sort_by", "0"));

			GlobalVariable.json10FirstShop = NetworkManger.post(APILinkMaker.ShopListInCategory(), pairs);
			return true;
		}

		@Override
		protected void onPostExecute(Boolean k){

		}

		@Override
		protected void onPreExecute(){
		}
	}


	// GCM
	private SharedPreferences getGCMPreferences(Context context) {
		return getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
	}

	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	private void storeRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getGCMPreferences(context);
		int appVersion = getAppVersion(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}

	private void sendRegistrationIdToBackend(){

	}

	public class RegisterGCM extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			String msg = "";
			try {
				if (gcm == null) {
					gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
				}
				regid = gcm.register(SENDER_ID);

				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("access_token", GlobalVariable.tokenID));
				pairs.add(new BasicNameValuePair("registration_code", regid));
				pairs.add(new BasicNameValuePair("OS", "2"));

				NetworkManger.post(APILinkMaker.mUpRegistration(), pairs);

				storeRegistrationId(getApplicationContext(), regid);
			}catch(Exception ex){
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean k){

		}

		@Override
		protected void onPreExecute(){
		}
	}

	public boolean checkPlayServices(){
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				finish();
				return false;
			}
		}
		return true;
	}

	private String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGCMPreferences(context);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.isEmpty()) {
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			return "";
		}
		return registrationId;
	}
}

