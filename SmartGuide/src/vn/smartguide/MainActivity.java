package vn.smartguide;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
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
import com.facebook.FacebookRequestError.Category;
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
import vn.smartguide.DetailPromo1Fragment.PromotionStr;
import vn.smartguide.UserFragment.GiftItem;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.crypto.Mac;

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
	private RelativeLayout	reviewBtn;
	private RelativeLayout	updateBtn;
	private RelativeLayout	gpsBtn;
	private RelativeLayout	mTutorialBtn;
	private RelativeLayout	mBtnIntro;
	private ImageButton		mExpandMenuBtn;
	private boolean			mIsMenuExpand = false;
	private ImageButton		mUpdateInforBtn;
	private Button			mRenameBtn;
	private LinearLayout	mExpandMenuLO;
	private String			avatarURL;
	private String			name;
	private Dialog			dialog;
	private boolean			mIsNeedChangeAvatar = false;
	// Viewpager
	private FragmentManager mFragmentManager;
	private List<Fragment> mFragmentList;
	private SViewPager mViewPager;

	// Toogle camera and content
	private boolean mShowContent 				= true;
	private boolean mShowCamera 				= false;
//	private boolean mShowUser 					= false;
//	private boolean mIsShowFilter				= false;
//	private boolean mShowMenu 					= false;
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
	private String mCurrentNavi = "DANH MỤC";

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

		if (GlobalVariable.getActivateCodeFromDB() == false){
			startActivityForResult(new Intent(this, WellcomeActivity.class), WelcomeRequestCode);
			isFirstTime = true;
		}
		else
			startActivityForResult(new Intent(this, FlashScreenActivity.class), FlashScreenRequestCode);
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
					GlobalVariable.cyImageLoader.showImage(GlobalVariable.avatarFace, avatar);
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
	
	private void turnToPage(int index) {
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
				setNaviText("NHIỀU DANH MỤC");
			else{
				int shopType = Integer.valueOf(GlobalVariable.mFilterString);
				switch(shopType){
				case 1:
					setNaviText("ẨM THỰC");
					break;
				case 2:
					setNaviText("CAFE & BAR");
					break;
				case 3:
					setNaviText("LÀM ĐẸP");
					break;
				case 4:
					setNaviText("GIẢI TRÍ");
					break;
				case 5:
					setNaviText("THỜI TRANG");
					break;
				case 6:
					setNaviText("DU LỊCH");
					break;
				case 7:
					setNaviText("SẢN PHẨM");
					break;					
				case 8:
					setNaviText("GIÁO DỤC");
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
			Toast.makeText(this, "Nhấn back lần nữa để thoát chương trình", Toast.LENGTH_SHORT).show();
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
					setNaviText("NHIỀU DANH MỤC");
				else{
					int shopType = Integer.valueOf(GlobalVariable.mFilterString);
					switch(shopType){
					case 1:
						setNaviText("ẨM THỰC");
						break;
					case 2:
						setNaviText("CAFE & BAR");
						break;
					case 3:
						setNaviText("LÀM ĐẸP");
						break;
					case 4:
						setNaviText("GIẢI TRÍ");
						break;
					case 5:
						setNaviText("THỜI TRANG");
						break;
					case 6:
						setNaviText("DU LỊCH");
						break;
					case 7:
						setNaviText("SẢN PHẨM");
						break;					
					case 8:
						setNaviText("GIÁO DỤC");
						break;
					}
				}
				break;

			case 0:
				enableFilterMap();
				mMapButton.setClickable(false);
				mMapButton.setImageResource(R.drawable.menu_map_lock);
				setNaviText("DANH MỤC");
				mShopListFragment.releaseMemory();
				break;
			}	
		}catch(Exception ex){
			setNaviText("DANH MỤC");
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

		boolean isMapAlive = !mShowContent && !mShowCamera && !mUserFragment.isShow() && !mFiterFragment.isShow();

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
				message = "Bạn cần bật GPS hoặc wireless location trước khi scan code!!";
				//else
				//					if (!isGPSOn)
				//						message = "Bạn cần bật GPS trước khi scan code!!";
				//					else
				//						message = "Bạn cần bật wireless location trước khi scan code!!";

				alertDialog.setMessage(message);

				alertDialog.setPositiveButton("Thiết lập", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int which) {
						startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
					}
				});

				alertDialog.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
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
				QRCodeTextView.setText("Tích điểm - Cửa hàng sẽ cung cấp thẻ cho bạn");
				break;
			case 2:
			case 3:
				QRCodeTextView.setText("Nhận quà - Cửa hàng sẽ cung cấp thẻ cho bạn");
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
				Toast.makeText(mActivity, "Vui lòng chờ lấy tọa độ GPS", Toast.LENGTH_LONG).show();
			}
		}
		else{
			QRCodeTextView.setText("CHẠM VÀO ĐỂ NHẬN ĐIỂM");
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

		// Tạo database
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
					final Fragment scroll = getSupportFragmentManager().findFragmentById(R.id.adsFragment);
					final FragmentTransaction tr = getSupportFragmentManager().beginTransaction();
					//tr.setCustomAnimations(R.anim.slide_up, R.anim.slide_down);
					tr.hide(scroll);
					tr.commit();
				}
				else{
					final Fragment scroll = getSupportFragmentManager().findFragmentById(R.id.adsFragment);
					final FragmentTransaction tr = getSupportFragmentManager().beginTransaction();
					tr.show(scroll);
					tr.commit();
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
					goToShopList();
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

				goToShopList();
			}
		});
		
		// Set up shop list fragment
		mShopListFragment.setListener(new vn.smartguide.ShopListFragment.Listener() {
			@Override
			public void onShopClick(Shop s) {
//				ShopDetailActivity.newInstance(MainActivity.this, s);
				GlobalVariable.mCurrentShop = s;
				mShopDetailFragment.setData(s);
				goToShopDetail();
			}
		});
		
		// Set up Detail shop fragment
		mShopDetailFragment.setListener(new vn.smartguide.ShopDetailFragment.Listener() {
			@Override
			public void onReward1Click(PromotionStr reward) {
				getAwardTypeOne(reward.id);
			}
			
			@Override
			public void onReward2Click(PromotionTypeTwo promotion) {
				getAwardTypeTwo(promotion.mID);
			}
		});
		
		// Set up filter fragment
		mFiterFragment.setListener(new vn.smartguide.FilterFragment.Listener() {
			@Override
			public void onDone() {
//				mShopListFragment.setForeground();
				mShopListFragment.filter();
				goToShopList();
			}
		});

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
					alertDialog.setMessage("GPS chưa được bật. Bạn có muốn thay đổi thiết lập");

					alertDialog.setPositiveButton("Thiết lập", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int which) {
							Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							startActivity(intent);
						}
					});

					alertDialog.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
					alertDialog.show();
				}
			}
		});

		final ImageView avatarFace = (ImageView)menu.getMenu().findViewById(R.id.userAvatarSetting);
		final ImageButton avatar = (ImageButton)menu.getMenu().findViewById(R.id.imageView1);
		avatar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mIsMenuExpand == true){
					Context context = MainActivity.this;
					AlertDialog.Builder builder = new AlertDialog.Builder(context); 
					
					LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);   
					View layout = inflater.inflate(R.layout.avatar_dialog, (ViewGroup) findViewById(R.id.layout_root));   
					GridView gridview = (GridView) layout.findViewById(R.id.avatar_list);   
					gridview.setAdapter(new ImageAdapter(getBaseContext()));   
					gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {   
						public void onItemClick(AdapterView<?> parent, View v, int position, long id) {   
							dialog.dismiss();
							avatarURL = GlobalVariable.mAvatarList.get(position);
							GlobalVariable.cyImageLoader.showImage(GlobalVariable.mAvatarList.get(position), avatarFace);					}   
					});

					    
					builder.setView(layout);     
					dialog = builder.create();
					dialog.show(); 
				}
			}
		});
		
		mExpandMenuBtn = (ImageButton)menu.getMenu().findViewById(R.id.expandMenuBtn);
		mExpandMenuBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (mIsMenuExpand == false){
					RotateAnimation anim = new RotateAnimation(0f, 45f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
					anim.setInterpolator(new LinearInterpolator());
					anim.setDuration(200);
					mExpandMenuBtn.startAnimation(anim);
					mExpandMenuLO.setVisibility(View.VISIBLE);
					
					TextView name = (TextView)menu.getMenu().findViewById(R.id.textView);
					name.setMaxLines(1);
					name.setText("Thay đổi hình đại diện");
					
				}else{
					RotateAnimation anim = new RotateAnimation(45f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
					anim.setInterpolator(new LinearInterpolator());
					anim.setDuration(200);
					mExpandMenuBtn.startAnimation(anim);
					mExpandMenuLO.setVisibility(View.GONE);
					
					TextView name = (TextView)menu.getMenu().findViewById(R.id.textView);
					name.setMaxLines(1);

					if (mIsNeedChangeAvatar == false){
						name.setText(GlobalVariable.nameFace);
						GlobalVariable.cyImageLoader.showImage(GlobalVariable.avatarFace, avatarFace);
					}
					else
						name.setText("Anomynous User");
				}
				
				mIsMenuExpand = !mIsMenuExpand;
			}
		});
		
			
		mUpdateInforBtn = (ImageButton)menu.getMenu().findViewById(R.id.updateInfoBtn);
		mRenameBtn = (Button)menu.getMenu().findViewById(R.id.renameBtn);
		mExpandMenuLO = (LinearLayout)menu.getMenu().findViewById(R.id.expandLayout);;
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
				menu.toggle();
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
				if (mUserFragment.isShow())
					onBackPressed();
				else
					goToUserCollection();
				createDestroyMap();
			}
		});

		// Set filter button
		mFilterBtn = ((ImageButton) findViewById(R.id.btnToggleFilter));
		mFilterBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mFiterFragment.isShow())
					onBackPressed();
				else
					goToFilter();
				createDestroyMap();
			}
		});

		// Set search button
		((ImageButton) findViewById(R.id.btnSearch)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				toggleSearch();
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
	private void toggleSearch() {

		EditText edtSearch = (EditText) findViewById(R.id.edtSearch);
		ImageButton btnSearch = (ImageButton) findViewById(R.id.btnSearch);
		ImageButton btnToggleMenu = (ImageButton) findViewById(R.id.btnToggleMenu);
		ImageButton btnToggleMap = (ImageButton) findViewById(R.id.btnToggleMap);

		// Set search onscreen keyboard event
		edtSearch.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				performSearch();
				return true;
			}
		});

		int width = (int) (btnToggleMap.getX() - btnToggleMenu.getX() - btnToggleMenu.getWidth() + btnToggleMap.getWidth());
//		ObjectAnimator animator = null;
//		ObjectAnimator animator2 = null;
		mShowSearch = !mShowSearch;

//		if (mShowSearch) {
//			// Show search box
//			animator = ObjectAnimator.ofInt(edtSearch, "width", 0, width);
//			animator.addListener(new AnimatorListener() {
//				public int searchWidth;
//				public AnimatorListener init(int w) {
//					searchWidth = w;
//					return this;
//				}
//				public void onAnimationRepeat(Animator animation) { }
//				public void onAnimationStart(Animator animation) {
//					showSearchBox();
//				}
//				public void onAnimationEnd(Animator animation) { }
//				public void onAnimationCancel(Animator animation) { }
//			}.init(btnSearch.getWidth()));
//
//			animator2 = ObjectAnimator.ofFloat(btnSearch, "translationX", 
//					0, btnToggleMap.getX() - btnToggleMenu.getX() - btnToggleMenu.getWidth());
//		} else {
//			// Hide search box
//			animator = ObjectAnimator.ofInt(edtSearch, "width", width, 0);
//			animator.addListener(new AnimatorListener() {
//				public int searchWidth;
//				public AnimatorListener init(int w) {
//					searchWidth = w;
//					return this;
//				}
//				public void onAnimationRepeat(Animator animation) { }
//				public void onAnimationStart(Animator animation) { }
//				public void onAnimationEnd(Animator animation) {
//					hideSearchBox();
//				}
//				public void onAnimationCancel(Animator animation) {
//					hideSearchBox();
//				}
//			}.init(btnSearch.getWidth()));
//
//			animator2 = ObjectAnimator.ofFloat(btnSearch, "translationX", 
//					btnToggleMap.getX() - btnToggleMenu.getX() - btnToggleMenu.getWidth(), 0);
//			String search = edtSearch.getText().toString();
//			edtSearch.setText("");
//			performSearch(search);
//		}
		
		if (mShowSearch) {
			// Show search box
			showSearchBox();
			edtSearch.setWidth(width);
			btnSearch.setTranslationX(btnToggleMap.getX() - btnToggleMenu.getX() - btnToggleMenu.getWidth());
		} else {
			// Hide search box
			edtSearch.setWidth(0);
			btnSearch.setTranslationX(0);
			hideSearchBox();
		}

//		TimeInterpolator acce = new AccelerateDecelerateInterpolator();
//		animator.setInterpolator(acce);
//		animator2.setInterpolator(acce);
//		animator.start();
//		animator2.start();
	}

	private void performSearch() {
		EditText edtSearch = (EditText) findViewById(R.id.edtSearch);
		String search = edtSearch.getText().toString();
		edtSearch.setText("");
		goToPage(1);
		setNaviText(search);
		getShopListFragment().search(search);
		if (mShowSearch)
			toggleSearch();
	}

	public void hideSearchBox() {

		EditText edtSearch = (EditText) findViewById(R.id.edtSearch);
		edtSearch.setText("");
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
		ab.setTitle("Chọn thành phố:");
		ab.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (mChoiceLocation == -1)
					return;

				GlobalVariable.mCityID = GlobalVariable.mCityIDes.get(mChoiceLocation);
				mLocationTV.setText(GlobalVariable.mCityNames.get(mChoiceLocation));
				menu.toggle();
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
						GlobalVariable.cyImageLoader.showImage(GlobalVariable.avatarFace, avatar);
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
			GlobalVariable.cyImageLoader.showImage(GlobalVariable.avatarFace, avatar);
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
		dlgAlert.setMessage("Bật kết nối mạng và thử lại");
		dlgAlert.setTitle("Lỗi kết nối mạng");
		dlgAlert.setPositiveButton("OK",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				finish(); 
			}
		});

		dlgAlert.setCancelable(false);
		dlgAlert.create().show();
	}

//	public void toogleUser(){
//
//		if (mShowUser){
//			if (GlobalVariable.avatarFace.compareTo("null") != 0)
//				setNaviText(GlobalVariable.nameFace);
//			else
//				setNaviText("User");
//
//			mFilterBtn.setImageResource(R.drawable.menu_filter_lock);
//			mMapButton.setImageResource(R.drawable.menu_map_lock);
//			mFilterBtn.setClickable(false);
//			mMapButton.setClickable(false);
//		}
//		else{
//			setNaviText(mPreviousNavi);
//
//			mFilterBtn.setImageResource(R.drawable.menu_filter_lock);
//			mMapButton.setImageResource(R.drawable.menu_map_lock);
//
//			if (!GlobalVariable.mIsLaunching){
//				mFilterBtn.setClickable(true);
//				mMapButton.setClickable(true);
//				mFilterBtn.setImageResource(R.drawable.menu_filter);
//				mMapButton.setImageResource(R.drawable.map_btn);
//			}
//		}
//
//		mUserFragment.toggle();
//	}

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
	
	///////////////////////////////////////////////////////////////////////////
	// View flow
	///////////////////////////////////////////////////////////////////////////
	
	private enum EnumView {
		ShopCategory,
		ShopList,
		ShopDetail,
		UserCollection,
		Filter
	}
	
	private EnumView mActiveView = EnumView.ShopCategory;
//	private boolean mShopMap = false;
	private String mTitleOnShopList = "";
	private EnumView mPreFilter;
	private EnumView mPreShopDetail;
	private EnumView mPreUserCollection;
	
	private static final boolean[][] HEADER_BUTTON_STATE = new boolean[][] {
		{true, true, true},
		{true, true, true},
		{false, true, false},
		{false, true, false},
		{true, false, false}};
	
	private void updateHeader() {
		ImageButton btnFilter = (ImageButton) findViewById(R.id.btnToggleFilter);
		ImageButton btnUser = (ImageButton) findViewById(R.id.btnUser);
		ImageButton btnMap = (ImageButton) findViewById(R.id.btnToggleMap);
		
		boolean[] state = HEADER_BUTTON_STATE[mActiveView.ordinal()];
		
		// Set enable/disable
		btnFilter.setImageResource(state[0] ? R.drawable.menu_filter : R.drawable.menu_filter_lock);
		btnFilter.setEnabled(state[0]);
		btnUser.setImageResource(state[1] ? R.drawable.menu_user : R.drawable.menu_user_lock);
		btnUser.setEnabled(state[1]);
		
		if (mShowContent)
			btnMap.setImageResource(state[2] ? R.drawable.menu_map : R.drawable.menu_map_lock);
		else
			btnMap.setImageResource(state[2] ? R.drawable.menu_list : R.drawable.menu_list_lock);

		btnMap.setEnabled(state[2]);
		
		// Set Title		
		switch (mActiveView) {
		case ShopCategory:
			mNaviText.setText("DANH MỤC");
			break;
		case ShopList:
			mNaviText.setText(mShopListFragment.getTitle());
			break;
		case ShopDetail:
			mNaviText.setText(GlobalVariable.mCurrentShop.mName);
			break;
		case UserCollection:
			if (GlobalVariable.avatarFace.compareTo("null") != 0)
				mNaviText.setText(GlobalVariable.nameFace);
			else
				mNaviText.setText("User");
			break;
		case Filter:
			mNaviText.setText("BỘ LỌC");
			break;
		}
	}
	
	private void goToFilter() {
		if (mActiveView == EnumView.ShopCategory || mActiveView == EnumView.ShopList) {
			mPreFilter = mActiveView;
			mActiveView = EnumView.Filter;
			updateHeader();
			mFiterFragment.toggle();
		}
	}
	
	private void goToUserCollection() {
		switch (mActiveView) {
		case ShopCategory:
		case ShopList:
			mPreUserCollection = mActiveView;
			mActiveView = EnumView.UserCollection;
			updateHeader();
			mUserFragment.toggle();
			break;
		case ShopDetail:
			if (mPreShopDetail == EnumView.ShopList) {
				mPreUserCollection = mActiveView;
				mActiveView = EnumView.UserCollection;
				updateHeader();
				mUserFragment.toggle();
			} else if (mPreShopDetail == EnumView.UserCollection) {
				mActiveView = EnumView.UserCollection;
				updateHeader();
				turnToPage(mPreUserCollection.ordinal());
				mUserFragment.toggle();
			}
			break;
		}
	}
	
	private void goToShopList() {
		
		if (!mShowContent)
			toggleShowContent();
		
		switch (mActiveView) {
		case ShopCategory:
			turnToPage(1);
			mActiveView = EnumView.ShopList;
			break;
		case Filter:
			if (mPreFilter == EnumView.ShopCategory) {
				mFiterFragment.toggle();
				mActiveView = EnumView.ShopList;
				turnToPage(1);
			} else if (mPreFilter == EnumView.ShopList) {
				mFiterFragment.toggle();
				mActiveView = EnumView.ShopList;
			}
			break;
		case ShopDetail:
			mActiveView = EnumView.ShopList;
			turnToPage(1);
			break;
		case UserCollection:
			if (mPreUserCollection == EnumView.ShopList) {
				mActiveView = EnumView.ShopList;
				mUserFragment.toggle();
			}
			break;
		}
		
		updateHeader();
	}
	
	private void goToShopCategory() {
		if (!mShowContent)
			toggleShowContent();
		
		switch (mActiveView) {
		case ShopList:
			mActiveView = EnumView.ShopCategory;
			turnToPage(0);
			break;
		case Filter:
			if (mPreFilter == EnumView.ShopCategory) {
				mActiveView = EnumView.ShopCategory;
				mFiterFragment.toggle();
			}
			break;
		case UserCollection:
			if (mPreUserCollection == EnumView.ShopCategory) {
				mActiveView = EnumView.ShopCategory;
				mUserFragment.toggle();
			}
			break;
		}
		
		updateHeader();
	}
	
	private void goToShopDetail() {
		if (!mShowContent)
			toggleShowContent();
		
		switch (mActiveView) {
		case ShopList:
			mPreShopDetail = mActiveView;
			mActiveView = EnumView.ShopDetail;
			turnToPage(2);
			break;
		case UserCollection:
			if (mPreUserCollection == EnumView.ShopDetail) {
				mActiveView = EnumView.ShopDetail;
				mUserFragment.toggle();
			} else {
				mPreShopDetail = mActiveView;
				mActiveView = EnumView.ShopDetail;
				mUserFragment.toggle();
				turnToPage(2);
			}
			break;
		}
	}

	@Override
	public void onBackPressed() {
		
		if (menu.isMenuShowing()) {
			menu.toggle();
			return;
		}

		if (mShowCamera) {
			toggleCamera();
			return;
		}
		
		if (mShowSearch) {
			toggleSearch();
			return;
		}
		
		if (mActiveView == EnumView.Filter) {
			if (mPreFilter == EnumView.ShopCategory)
				goToShopCategory();
			else if (mPreFilter == EnumView.ShopList)
				goToShopList();
			return;
		}
		
		if (mActiveView == EnumView.UserCollection) {
			switch (mPreUserCollection) {
			case ShopCategory:
				goToShopCategory();
				break;
			case ShopList:
				goToShopList();
				break;
			case ShopDetail:
				goToShopDetail();
				break;
			}
			return;
		}
		
		if (mActiveView == EnumView.ShopDetail) {
			if (mPreShopDetail == EnumView.ShopList)
				goToShopList();
			else if (mPreShopDetail == EnumView.UserCollection)
				goToUserCollection();
			return;
		}

		if (mActiveView == EnumView.ShopList) {
			goToShopCategory();
			return;
		}
		
		if (mActiveView == EnumView.ShopCategory) {
			if (doubleBackToExitPressedOnce) {
				super.onBackPressed();
				return;
			}

			doubleBackToExitPressedOnce = true;
			Toast.makeText(this, "Nhấn back lần nữa để thoát chương trình", Toast.LENGTH_SHORT).show();
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					doubleBackToExitPressedOnce = false;   
				}
			}, 2000);
		}
		return;
	}
	
	///////////////////////////////////////////////////////////////////////////

	@Override
	public void getAwardTypeOne(int award_id) {
		mAwardId = award_id;
		mScanningCode = 2;
		toggleCamera();
	}

	@Override
	public void userToDetail() {
		mIsNeedToggleUser = true;
		mUserFragment.toggle();
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

		GlobalVariable.cyImageLoader.showImage(GlobalVariable.mURL, view);
		
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
					mSGPText.setText(JSResult.getString("money") + " VNĐ");
					mContentText.setText("Bạn nhận được phiếu giảm giá");

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
		protected void onPostExecute(Boolean k) {
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
		protected void onPostExecute(Boolean k) {
			int sgp = 0;
			mProgressBar.setVisibility(View.INVISIBLE);
			try {
				int status = JSResult.getInt("status");
				switch(status) {
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
					mContentText.setText("Chúc mừng bạn nhận được");

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

//					if (mShopDetailFragment.getPromoFragment() != null && isNeedUpdateSGP)
					if (isNeedUpdateSGP)
						mShopDetailFragment.setSGP(total_sgp);
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
			} catch(Exception ex) {
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
					mContentText.setText("Chúc mừng bạn nhận được");
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
//					if (mShopDetailFragment.getPromoFragment() != null && isNeedUpdateSGP)
					if (isNeedUpdateSGP)
						mShopDetailFragment.setSGP(total_sgp);
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
		builder.setMessage("Bạn muốn đóng ứng dụng");
		builder.setCancelable(true);

		builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				stopAds();
				finish();
			}
		});

		builder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
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
			mAdsFragment.startDownImage();
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

