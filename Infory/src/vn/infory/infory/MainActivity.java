package vn.infory.infory;

import java.util.Locale;

import vn.infory.infory.FlashActivity.Listener;
import vn.infory.infory.data.Settings;
import vn.infory.infory.home.HomeFragment;
import vn.infory.infory.home.PromotionFragment;
import vn.infory.infory.login.InforyLoginActivity;
import vn.infory.infory.login.RegisterTypeFragment;
import vn.infory.infory.network.NetworkManager;
import vn.infory.infory.notification.Constants;
import vn.infory.infory.notification.ServerUtilities;
import vn.infory.infory.scancode.ScanCodeActivity;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;
import com.google.android.gcm.GCMRegistrar;

public class MainActivity extends FragmentActivity
{

	@ViewById(id = R.id.pager)
	private ViewPager					mPager;
	@ViewById(id = R.id.layoutScanCode)
	private View						mLayoutScanCode;
	@ViewById(id = R.id.imgScanCode)
	private View						mImgScanCode;
	@ViewById(id = R.id.imgScanCodeSmall)
	private View						mImgScanCodeSmall;

	private static Listener				sListener;

	private SGSideMenu					mMenu;
	private HomeFragment				mFragHome;
	private PromotionFragment			mFragPromo;
	// private StoreFragment mFragStore;
	private Fragment					mFragActive;
	private RegisterTypeFragment		mFragRegisterType;

	private Settings.DataChangeListener	mSettingListener	= new Settings.DataChangeListener() {
																@Override
																public void onUserDataChange(Settings s)
																{
																	mMenu.update();
																}
															};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{

		Locale locale = new Locale("vi", "VN");
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;
		getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		sListener = null;

		try
		{
			Settings.init(this);
			Settings.getLocation(this);
			NetworkManager.init();
			CyImageLoader.initInstance(this);
			FontsCollection.init(this);
			AndroidAnnotationParser.parse(this, findViewById(android.R.id.content));
		}
		catch (Exception e)
		{
			finish();
		}

		/*
		 * Start GCM;
		 */

		GCMRegistrar.checkDevice(this);
		GCMRegistrar.checkManifest(this);
		final String regId = "";

		final Context mContext = this;

		if (regId.compareTo("") == 0 || regId.compareTo("0") == 0)
		{
			System.out.println("Device is not have any registrationID");
			GCMRegistrar.register(this, Constants.GCM_SENDERID);
		}
		else
		{
			// device is already registered on GCM, check server.
			System.out.println("registrationID is: " + regId);
			if (GCMRegistrar.isRegisteredOnServer(this))
			{
				System.out.println("Device is already registered on Server side: " + regId);
			}
			else
			{
				// Try to register again, but not in the UI thread.
				// It's also necessary to cancel the thread
				// onDestroy(),
				// hence the use of AsyncTask instead of a raw
				// thread.
				System.out.println("Device is not registered on Server side");
				final AsyncTask<Void, Void, Void> mRegisterTask = new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params)
					{

						boolean registered = ServerUtilities.register(mContext, regId);
						if (!registered)
						{
							System.out.println("async task: not registered ");
							GCMRegistrar.unregister(mContext);
						}
						return null;
					}

					@Override
					protected void onPostExecute(Void result)
					{
						Log.e("GCMRegistrar regid", GCMRegistrar.getRegistrationId(mContext));

					}
				};
				mRegisterTask.execute(null, null, null);
			}
		}

		/*
		 * Intent intent = new Intent(this, ScanCodeRelatedActivity.class);
		 * startActivity(intent);
		 */

		String last_activity = PreferenceManager.getDefaultSharedPreferences(this).getString("last_activity", "");
		// Toast.makeText(getApplicationContext(), "Main activity: " +
		// last_activity, Toast.LENGTH_SHORT).show();
		if (last_activity.equals("RegisterTypeFragment"))
		{
			// Toast.makeText(getApplicationContext(),
			// "Main activity: chuyển qua LoginActivity",
			// Toast.LENGTH_LONG).show();
			InforyLoginActivity.newInstance(this, sListener);

			/*
			 * FragmentTransaction ft =
			 * getSupportFragmentManager().beginTransaction();
			 * RegisterTypeFragment fb = new RegisterTypeFragment();
			 * ft.replace(R.layout.activity_login, fb); ft.commit(); ViewPager
			 * mViewPager = new ViewPager(this);
			 * mViewPager.setId(R.layout.login_register_type);
			 * setContentView(mViewPager);
			 */
		}

		mAnimationHeight = CyUtils.dpToPx(24, this);

		// Invoke flash screen
		FlashActivity.newInstance(this, new FlashActivity.Listener() {
			// @Override
			// public void onFirstTime() {
			// UseImmediatelyActivity.newInstance(MainActivity.this);
			// }

			@Override
			public void onSuccess()
			{
				mFragHome.onFinishInit();
				mFragPromo.onFinishInit();
			}

			@Override
			public void onFail(Exception e)
			{
				finish();
			}
		});

		// Set up side menu
		mMenu = new SGSideMenu(this);
		mMenu.setListener(new SGSideMenu.Listener() {
			@Override
			public void onExploreClick()
			{
				mMenu.toggle();
				mFragHome.onFinishInit();
				if (mFragActive == mFragHome)
					return;
				getSupportFragmentManager().beginTransaction().hide(mFragActive).show(mFragHome).commit();
				mFragActive = mFragHome;
			}

			@Override
			public void onPromotionClick()
			{
				mMenu.toggle();
				mFragPromo.onFinishInit();
				if (mFragActive == mFragPromo)
					return;
				getSupportFragmentManager().beginTransaction().hide(mFragActive).show(mFragPromo).commit();
				mFragActive = mFragPromo;
			}

			@Override
			public void onStoreClick()
			{
				AlertDialog.Builder builder = new Builder(MainActivity.this);
				builder.setTitle("Thông báo");
				builder.setMessage("Sẽ xuất hiện trong phiên bản kế tiếp!");
				builder.setPositiveButton("OK", null);
				builder.create().show();
			}

			@Override
			public void onTutorialClick()
			{
				WebActivity.newInstance(MainActivity.this, "http://infory.vn/mobile/guide/");
			}
		});
		Settings.instance().addListener(mSettingListener);

		FragmentManager manager = getSupportFragmentManager();
		mFragHome = (HomeFragment) manager.findFragmentById(R.id.fragExplore);
		mFragPromo = (PromotionFragment) manager.findFragmentById(R.id.fragPromo);
		// mFragStore = (StoreFragment)
		// manager.findFragmentById(R.id.fragStore);
		getSupportFragmentManager().beginTransaction().hide(mFragPromo).commit();
		mFragActive = mFragHome;

		// Toast.makeText(getApplicationContext(), mScanCodeLayoutSmall+"",
		// Toast.LENGTH_SHORT).show();

		mFragHome.setListener(new HomeFragment.Listener() {
			@Override
			public void onSideMenuClick()
			{
				mMenu.toggle();
			}
		}, new OnScrollListener() {
			private int	mLastFirstVisibleItem;

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
			{
				/*
				 * processScroll(view, firstVisibleItem, visibleItemCount,
				 * totalItemCount);
				 */
				if (totalItemCount > 0 && firstVisibleItem == 0 && view.getChildAt(0).getTop() == 0 || totalItemCount == 0)
				{

					if (mScanCodeLayoutSmall)
					{
						mScanCodeAnimation.cancel();
						mImgScanCodeSmall.setVisibility(View.VISIBLE);
						Animator animator = ObjectAnimator.ofFloat(mLayoutScanCode, "translationY", mAnimationHeight, 0);
						Animator animator2 = ObjectAnimator.ofFloat(mImgScanCode, "translationY", mAnimationHeight * 2, 0);
						Animator animator3 = ObjectAnimator.ofFloat(mImgScanCodeSmall, "translationY", 0, mAnimationHeight * 2);
						mScanCodeAnimation.playTogether(animator, animator2, animator3);
						mScanCodeAnimation.start();
						mScanCodeLayoutSmall = false;
					}
				}

				if (mLastFirstVisibleItem < firstVisibleItem && !mScanCodeLayoutSmall) // Scroll
																						// up
				{
					mScanCodeAnimation.cancel();
					mImgScanCodeSmall.setVisibility(View.VISIBLE);
					Animator animator = ObjectAnimator.ofFloat(mLayoutScanCode, "translationY", 0, mAnimationHeight);
					Animator animator2 = ObjectAnimator.ofFloat(mImgScanCode, "translationY", 0, mAnimationHeight * 2);
					Animator animator3 = ObjectAnimator.ofFloat(mImgScanCodeSmall, "translationY", mAnimationHeight * 2, 0);
					mScanCodeAnimation.playTogether(animator, animator2, animator3);
					mScanCodeAnimation.start();
					mScanCodeLayoutSmall = true;
				}

				if (mLastFirstVisibleItem > firstVisibleItem && mScanCodeLayoutSmall) // Scroll
																						// down
				{
					mScanCodeAnimation.cancel();
					mImgScanCodeSmall.setVisibility(View.VISIBLE);
					Animator animator = ObjectAnimator.ofFloat(mLayoutScanCode, "translationY", mAnimationHeight, 0);
					Animator animator2 = ObjectAnimator.ofFloat(mImgScanCode, "translationY", mAnimationHeight * 2, 0);
					Animator animator3 = ObjectAnimator.ofFloat(mImgScanCodeSmall, "translationY", 0, mAnimationHeight * 2);
					mScanCodeAnimation.playTogether(animator, animator2, animator3);
					mScanCodeAnimation.start();
					mScanCodeLayoutSmall = false;
				}
				mLastFirstVisibleItem = firstVisibleItem;
			}

			public void onScrollStateChanged(AbsListView view, int scrollState)
			{
			}
		});

		mFragPromo.setListener(new HomeFragment.Listener() {
			@Override
			public void onSideMenuClick()
			{
				mMenu.toggle();
			}
		}, new OnScrollListener() {
			private int	mLastFirstVisibleItem;

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
			{
				/*
				 * processScroll(view, firstVisibleItem, visibleItemCount,
				 * totalItemCount);
				 */
				if (totalItemCount > 0 && firstVisibleItem == 0 && view.getChildAt(0).getTop() > 0 || totalItemCount == 0)
				{

					if (mScanCodeLayoutSmall)
					{
						mScanCodeAnimation.cancel();
						mImgScanCodeSmall.setVisibility(View.VISIBLE);
						Animator animator = ObjectAnimator.ofFloat(mLayoutScanCode, "translationY", mAnimationHeight, 0);
						Animator animator2 = ObjectAnimator.ofFloat(mImgScanCode, "translationY", mAnimationHeight * 2, 0);
						Animator animator3 = ObjectAnimator.ofFloat(mImgScanCodeSmall, "translationY", 0, mAnimationHeight * 2);
						mScanCodeAnimation.playTogether(animator, animator2, animator3);
						mScanCodeAnimation.start();
						mScanCodeLayoutSmall = false;
					}
				}

				if (mLastFirstVisibleItem < firstVisibleItem && !mScanCodeLayoutSmall) // Scroll
																						// up
				{
					mScanCodeAnimation.cancel();
					mImgScanCodeSmall.setVisibility(View.VISIBLE);
					Animator animator = ObjectAnimator.ofFloat(mLayoutScanCode, "translationY", 0, mAnimationHeight);
					Animator animator2 = ObjectAnimator.ofFloat(mImgScanCode, "translationY", 0, mAnimationHeight * 2);
					Animator animator3 = ObjectAnimator.ofFloat(mImgScanCodeSmall, "translationY", mAnimationHeight * 2, 0);
					mScanCodeAnimation.playTogether(animator, animator2, animator3);
					mScanCodeAnimation.start();
					mScanCodeLayoutSmall = true;
				}

				if (mLastFirstVisibleItem > firstVisibleItem && mScanCodeLayoutSmall) // Scroll
																						// down
				{
					mScanCodeAnimation.cancel();
					mImgScanCodeSmall.setVisibility(View.VISIBLE);
					Animator animator = ObjectAnimator.ofFloat(mLayoutScanCode, "translationY", mAnimationHeight, 0);
					Animator animator2 = ObjectAnimator.ofFloat(mImgScanCode, "translationY", mAnimationHeight * 2, 0);
					Animator animator3 = ObjectAnimator.ofFloat(mImgScanCodeSmall, "translationY", 0, mAnimationHeight * 2);
					mScanCodeAnimation.playTogether(animator, animator2, animator3);
					mScanCodeAnimation.start();
					mScanCodeLayoutSmall = false;
				}
				mLastFirstVisibleItem = firstVisibleItem;
			}

			public void onScrollStateChanged(AbsListView view, int scrollState)
			{
			}
		});

		// mFragStore.setListener(new Listener() {
		// @Override
		// public void onSideMenuClick() {
		// mMenu.toggle();
		// }
		// });

	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		Settings.instance().removeListener(mSettingListener);
	}

	@Click(id = R.id.layoutScanCode)
	private void onScanCodeClick(View v)
	{
		Settings.checkLogin(this, new Runnable() {
			@Override
			public void run()
			{
				ScanCodeActivity.newInstance(MainActivity.this);
			}
		}, true);
	}

	public static void newInstance(Activity act, Listener listener)
	{
		sListener = listener;
		Intent intent = new Intent(act, MainActivity.class);
		act.startActivity(intent);
	}

	// /////////////////////////////////////////////////////////////////////////
	// Scan code button animation
	// /////////////////////////////////////////////////////////////////////////

	private AnimatorSet	mScanCodeAnimation		= new AnimatorSet();
	private int			mAnimationHeight;
	private boolean		mScanCodeLayoutSmall	= false;

	private void processScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
	{

		if (totalItemCount > 0 && firstVisibleItem == 0 && view.getChildAt(0).getTop() > 0 || totalItemCount == 0)
		{

			if (mScanCodeLayoutSmall)
			{
				mScanCodeAnimation.cancel();
				mImgScanCodeSmall.setVisibility(View.VISIBLE);
				Animator animator = ObjectAnimator.ofFloat(mLayoutScanCode, "translationY", mAnimationHeight, 0);
				Animator animator2 = ObjectAnimator.ofFloat(mImgScanCode, "translationY", mAnimationHeight * 2, 0);
				Animator animator3 = ObjectAnimator.ofFloat(mImgScanCodeSmall, "translationY", 0, mAnimationHeight * 2);
				mScanCodeAnimation.playTogether(animator, animator2, animator3);
				mScanCodeAnimation.start();
				mScanCodeLayoutSmall = false;
			}
		}
		else
		{
			if (!mScanCodeLayoutSmall)
			{
				mScanCodeAnimation.cancel();
				mImgScanCodeSmall.setVisibility(View.VISIBLE);
				Animator animator = ObjectAnimator.ofFloat(mLayoutScanCode, "translationY", 0, mAnimationHeight);
				Animator animator2 = ObjectAnimator.ofFloat(mImgScanCode, "translationY", 0, mAnimationHeight * 2);
				Animator animator3 = ObjectAnimator.ofFloat(mImgScanCodeSmall, "translationY", mAnimationHeight * 2, 0);
				mScanCodeAnimation.playTogether(animator, animator2, animator3);
				mScanCodeAnimation.start();
				mScanCodeLayoutSmall = true;
			}
		}
	}

	// /////////////////////////////////////////////////////////////////////////
	// Placelist list callback
	// /////////////////////////////////////////////////////////////////////////

	public interface onSideMenuClickListener
	{
		public void onSideMenuClick();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		this.registerReceiver(mMessageReceiver, new IntentFilter("aaaaaaaaaaaaaaaaaaaa"));
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		this.unregisterReceiver(mMessageReceiver);
	}

	// This is the handler that will manager to process the broadcast intent
	private BroadcastReceiver	mMessageReceiver	= new BroadcastReceiver() {
														@Override
														public void onReceive(Context context, Intent intent)
														{
															String count = intent.getStringExtra("unreadCount");
															Log.e("unreadCount", count + "");
															mFragHome.updateCounter(count);
														}
													};
}
