package vn.smartguide;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends FragmentActivity implements MainAcitivyListener, OnTouchListener  {

	private final int WelcomeRequestCode 		= 44444;
	private final int FlashScreenRequestCode 	= 55555;
	private final int ReviewRequestCode			= 66666;
	// Load qrcode lib
	static {
		System.loadLibrary("iconv");
	}

	// QRcode 
	private Camera mCamera;
	private CameraPreview mPreview;
	private Handler autoFocusHandler;
	private FrameLayout preview;
	private View mOpticalFrame;
	private ImageScanner scanner;
	private boolean previewing;
	private String mQRCode = "";
	private int mScanningCode = 1;
	private int mAwardId = -1;
	private boolean isNeedUpdateSGP = false;
	private boolean isCanScan = true;

	// QR Code layout
	private LinearLayout mMirror;
	private RelativeLayout mMirrorFront;
	private TextView mShopNameText;
	private TextView mContentText;
	private TextView mSGPText;
	private ImageButton mCloseBtn;
	private Activity mActivity;
	private TextView mTotalSGP;

	// Slide menu
	SlidingMenu menu;
	private RelativeLayout reviewBtn;
	private boolean isNeedReview = false;

	// Viewpager
	private FragmentManager mFragmentManager;
	private List<Fragment> mFragmentList;
	private SViewPager mViewPager;

	// Toogle camera and content
	private boolean mShowContent 				= true;
	private boolean mShowCamera 				= false;
	private boolean mShowUser 					= false;
	private boolean mIsShowFilter				= false;
	private boolean mIsNeedGotoDetail 			= false;

	// Fragment
	private AdsFragment mAdsFragment = null;
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

	//
	private ImageButton mFilterBtn;
	private ImageButton mMapButton;
	private LatLngBounds mBound;
	private boolean mIsMapAlive = false;
	private boolean mNeedUpdateMap = false;

	//
	static final int MIN_DISTANCE = 100;
	private float downX, downY, upX, upY;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		mUiHelper = new UiLifecycleHelper(this, callback);
		mUiHelper.onCreate(savedInstanceState);

		init();

		if (GlobalVariable.getActivateCodeFromDB() == false)
			startActivityForResult(new Intent(this, WellcomeActivity.class), WelcomeRequestCode);
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

		case GlobalVariable.CAMERA_REQUEST_CODE:
			getDetailFragment().onActivityResult(requestCode, resultCode, data);
			break;
		}
	}

	@Override
	public void goToPage(int index) {
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
			setNaviText(GlobalVariable.mCurrentShop.mName);
			break;
		}
		mViewPager.setCurrentItem(pageWillGo, true);
	}

	@Override
	public void goPreviousPage() {
		int current_index = mViewPager.getCurrentItem();
		if (current_index == 0){
			stopAds();
			finish();
			return;
		}

		int pageWillGo = current_index - 1;
		try{
			switch (pageWillGo){
			case 1:
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
				setNaviText("DANH MỤC");
				break;
			}	
		}catch(Exception ex){
			setNaviText("DANH MỤC");
		}
		mViewPager.setCurrentItem(current_index - 1, true);
	}

	public void toggleShowContent() {
		mShowContent = !mShowContent;
		ObjectAnimator animator = null;
		int height = findViewById(R.id.layoutContentHolder).getHeight();
		View layout = findViewById(R.id.layoutContentFrame);
		if (mShowContent)
			animator = ObjectAnimator.ofFloat(layout, "translationY", -height, 0);
		else{
			animator = ObjectAnimator.ofFloat(layout, "translationY", 0, -height);
			updateMap();
		}

		animator.setInterpolator(new AccelerateDecelerateInterpolator());
		animator.start();
	}

	public void updateMap() {
		mNeedUpdateMap = true;
	}

	public void updateMapAsync() {
		GoogleMap map = mMapFragment.getMap();
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

				txtShopNae.setText(s.mName);
				txtAddress.setText(s.mAddress);

				return view;
			}
		});

		map.clear();

		if (getShopListFragment().mShopList == null)
			return;

		LatLngBounds.Builder builder = new LatLngBounds.Builder();

		for (int i = 0; i < getShopListFragment().mShopList.size() && i < 30; i++) {

			Shop s = getShopListFragment().mShopList.get(i);
			LatLng ll = new LatLng(s.mLat, s.mLng);
			map.addMarker(new MarkerOptions().position(ll)
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

				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						try {
							if (mBound == null || mMapFragment.getMap() == null)
								return;
							mMapFragment.getMap().animateCamera(CameraUpdateFactory.newLatLngBounds(mBound, 0));
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
		//		mShowContent;
		//		mShowCamera;
		//		mShowUser;
		//		mIsShowFilter;

	}

	public void initToggleCamera(){
		layoutTC = findViewById(R.id.relativeLayout2);
		heightTC = layoutTC.getHeight();
		btnQRTToogle = findViewById(R.id.btnQRToggle);
		textViewGetScore = findViewById(R.id.textViewGetScore);
	}

	public void toggleCamera() {
		RelativeLayout layoutQR = (RelativeLayout) findViewById(R.id.layoutQR);
		if (layoutQR.getVisibility() == View.GONE) {
			layoutQR.setVisibility(View.VISIBLE);
			isCanScan = true;
		} else {
			mMirror.setVisibility(View.INVISIBLE);
			mMirrorFront.setVisibility(View.INVISIBLE);
		}

		mShowCamera = !mShowCamera;

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
		preview = (FrameLayout)findViewById(R.id.cameraPreview);
		autoFocusHandler = new Handler();
		scanner = new ImageScanner();
		scanner.setConfig(0, Config.X_DENSITY, 3);
		scanner.setConfig(0, Config.Y_DENSITY, 3);
		mCamera = getCameraInstance();
		mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB);
		preview.addView(mPreview);
	}

	Camera.PreviewCallback previewCb = new Camera.PreviewCallback() {
		public void onPreviewFrame(byte[] data, Camera camera) {
			if (!isCanScan)
				return;

			Camera.Parameters parameters = camera.getParameters();
			Camera.Size size = parameters.getPreviewSize();

			Image barcode = new Image(size.width, size.height, "Y800");
			barcode.setData(data);

			int result = scanner.scanImage(barcode);

			if (result != 0) {
				mCamera.setPreviewCallback(null);
				SymbolSet syms = scanner.getResults();
				for (Symbol sym : syms) {
					mQRCode = sym.getData();
					switch (mScanningCode) {
					case 1:
						isCanScan = false;
						new GetSGPPoint().execute();
						break;
					case 2:
						isCanScan = false;
						new GetAwardType1().execute();
						break;
					case 3:
						isCanScan = false;
						new GetAwardType2().execute();
					default:
						break;
					};
				}
			}
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

		((RelativeLayout)findViewById(R.id.rootOfroot)).setOnTouchListener(this);
		((RelativeLayout)findViewById(R.id.layoutQR)).setOnTouchListener(this);

		mActivity = this;

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

		// Update GPS
		GlobalVariable.updateLocation(this);

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
					//tr.hide(scroll);
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

		stopAds();

		// Setting layout
		mOpticalFrame = findViewById(R.id.opticalMainFrame);
		mLocationBtn = (RelativeLayout)menu.getMenu().findViewById(R.id.location_layout);
		mLocationTV = (TextView) menu.getMenu().findViewById(R.id.textView7);
		mNaviText = (TextView) findViewById(R.id.txtNavi);
		mAvatarFaceBtn = (ImageButton)menu.getMenu().findViewById(R.id.imageView1);
		mTotalSGP = (TextView)menu.getMenu().findViewById(R.id.SGPScoreSetting);
		reviewBtn = (RelativeLayout)menu.getMenu().findViewById(R.id.reviewSmartGuide);

		reviewBtn.setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View v) {
				if (GlobalVariable.avatarFace.compareTo("null") == 0){
					isNeedReview = true;
					loginFaceToReview();
				}else{
					startActivityForResult(new Intent(mActivity, ReviewActivity.class), FlashScreenRequestCode);
				}
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
				toggleShowContent();
				createDestroyMap();
			}
		});

		// Set user button
		((ImageButton)findViewById(R.id.btnUser)).setOnClickListener(new View.OnClickListener() {
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
				if (mIsShowFilter)
					setNaviText("BỘ LỌC");
				else
					setNaviText(mPreviousNavi);

				createDestroyMap();
			}
		});

		initToggleCamera();
	}

	void updateLocation(){
		final String items[] = GlobalVariable.mCityNames.toArray(new String[GlobalVariable.mCityNames.size()]);

		AlertDialog.Builder ab=new AlertDialog.Builder(MainActivity.this);
		ab.setTitle("Chọn thành phố:");
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
		// Update SGP at Setting view
		new GetUserCollection().execute();

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

	public void toggleSideMenu() {
		menu.toggle();
	}

	public void toogleUser(){
		mShowUser = !mShowUser;

		if (mShowUser){
			mFilterBtn.setImageResource(R.drawable.menu_filter_lock);
			mMapButton.setImageResource(R.drawable.menu_map_lock);
			mFilterBtn.setClickable(false);
			mMapButton.setClickable(false);

		}
		else{
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
					mSGPText.setText(JSResult.getString("money") + " VN�?");
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
		protected void onPreExecute(){}
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
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("user_id", GlobalVariable.userID));
			pairs.add(new BasicNameValuePair("code", mQRCode));


			String json = NetworkManger.post(APILinkMaker.mGetSGP(), pairs);
			try {
				JSResult = new JSONObject(json);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
		protected void onPreExecute(){}
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
				mTotalSGP.setText(score+ " point");
				mUserFragment.updateScore(score);
				mTotalSGP.setText(score + " point");

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
		protected void onPreExecute(){}
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
				mTotalSGP.setText(Integer.toString((new JSONObject(mJson)).getInt("score")) + " point");
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

	public void onRightToLeftSwipe(){

	}

	public void onLeftToRightSwipe(){

	}

	public void onTopToBottomSwipe(){
		if (mShowCamera)
			toggleCamera();
	}

	public void onBottomToTopSwipe(){
		if (!mShowCamera)
			toggleCamera();
	}

	public void loginFaceToReview(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setTitle("Thông báo");
		builder.setMessage("Bạn cần đăng nhập facebook để đánh giá SmartGuide");
		builder.setCancelable(true);
		
		builder.setPositiveButton("Đăng nhập", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});
		
		builder.setNegativeButton("Thoát", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		
		builder.show();
	}
}

