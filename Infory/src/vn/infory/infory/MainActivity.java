package vn.infory.infory;

import java.util.Locale;

import vn.infory.infory.data.Settings;
import vn.infory.infory.home.HomeFragment;
import vn.infory.infory.home.PromotionFragment;
import vn.infory.infory.network.NetworkManager;
import vn.infory.infory.scancode.ScanCodeActivity;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;

public class MainActivity extends FragmentActivity {

//	@ViewById(id = R.id.pager)				private ViewPager mPager;
	@ViewById(id = R.id.layoutScanCode)		private View mLayoutScanCode;
	@ViewById(id = R.id.imgScanCode)		private View mImgScanCode;
	@ViewById(id = R.id.imgScanCodeSmall)	private View mImgScanCodeSmall;

	private SGSideMenu mMenu;
	private HomeFragment mFragHome;
	private PromotionFragment mFragPromo;
//	private StoreFragment mFragStore;
	private Fragment mFragActive;
	
	private Settings.DataChangeListener mSettingListener = 
			new Settings.DataChangeListener() {
		@Override
		public void onUserDataChange(Settings s) {
			mMenu.update();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		Locale locale = new Locale("vi", "VN");
	    Locale.setDefault(locale);
	    Configuration config = new Configuration();
	    config.locale = locale;
	    getBaseContext().getResources().updateConfiguration(config,
	            getBaseContext().getResources().getDisplayMetrics()); 
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		try {
			Settings.init(this);
			NetworkManager.init();
			CyImageLoader.initInstance(this);
			FontsCollection.init(this);
			AndroidAnnotationParser.parse(this, findViewById(android.R.id.content));
		} catch (Exception e) {
			finish();
		}
		
		mAnimationHeight = CyUtils.dpToPx(24, this);
		
		// Invoke flash screen
		FlashActivity.newInstance(this, new FlashActivity.Listener() {
//			@Override
//			public void onFirstTime() {
//				UseImmediatelyActivity.newInstance(MainActivity.this);
//			}
			
			@Override
			public void onSuccess() {
				mFragHome.onFinishInit();
				mFragPromo.onFinishInit();
			}
			
			@Override
			public void onFail(Exception e) {
				finish();
			}
		});
		
		// Set up side menu
		mMenu = new SGSideMenu(this);
		mMenu.setListener(new SGSideMenu.Listener() {
			@Override
			public void onExploreClick() {
				mMenu.toggle();
				mFragHome.onFinishInit();
				if (mFragActive == mFragHome)
					return;
				getSupportFragmentManager().beginTransaction().hide(mFragActive).show(mFragHome).commit();
				mFragActive = mFragHome;
			}
			
			@Override
			public void onPromotionClick() {
				mMenu.toggle();
				mFragPromo.onFinishInit();
				if (mFragActive == mFragPromo)
					return;
				getSupportFragmentManager().beginTransaction().hide(mFragActive).show(mFragPromo).commit();
				mFragActive = mFragPromo;
			}
			
			@Override
			public void onStoreClick() {
				AlertDialog.Builder builder = new Builder(MainActivity.this);
				builder.setTitle("Thông báo");
				builder.setMessage("Sẽ xuất hiện trong phiên bản kế tiếp!");
				builder.setPositiveButton("OK", null);
				builder.create().show();
			}
			
			@Override
			public void onTutorialClick() {
				TutorialActivity.newInstance(MainActivity.this);
			}
		});
		Settings.instance().addListener(mSettingListener);
		
		FragmentManager manager = getSupportFragmentManager();
		mFragHome = (HomeFragment) manager.findFragmentById(R.id.fragExplore);
		mFragPromo = (PromotionFragment) manager.findFragmentById(R.id.fragPromo);
//		mFragStore = (StoreFragment) manager.findFragmentById(R.id.fragStore);
		getSupportFragmentManager().beginTransaction().hide(mFragPromo).commit();
		mFragActive = mFragHome;
		
		mFragHome.setListener(new HomeFragment.Listener() {
			@Override
			public void onSideMenuClick() {
				mMenu.toggle();
			}
		}, new OnScrollListener() {
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				processScroll(view, firstVisibleItem, 
						visibleItemCount, totalItemCount);
			}
			public void onScrollStateChanged(AbsListView view, int scrollState) {}
		});
		
		mFragPromo.setListener(new HomeFragment.Listener() {
			@Override
			public void onSideMenuClick() {
				mMenu.toggle();
			}
		}, new OnScrollListener() {
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				processScroll(view, firstVisibleItem, 
						visibleItemCount, totalItemCount);
			}
			public void onScrollStateChanged(AbsListView view, int scrollState) {}
		});
		
//		mFragStore.setListener(new Listener() {
//			@Override
//			public void onSideMenuClick() {
//				mMenu.toggle();
//			}
//		});
		
		
	}
	
	@Override
		protected void onDestroy() {
			super.onDestroy();
			
			Settings.instance().removeListener(mSettingListener);
		}
	
	@Click(id = R.id.layoutScanCode)
	private void onScanCodeClick(View v) {
		Settings.checkLogin(this, new Runnable() {
			@Override
			public void run() {
				ScanCodeActivity.newInstance(MainActivity.this);		
			}
		}, true);
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Scan code button animation
	///////////////////////////////////////////////////////////////////////////
	
	private AnimatorSet mScanCodeAnimation = new AnimatorSet();
	private boolean mScanCodeLayoutSmall = false;
	private int mAnimationHeight;
	private void processScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		
		if (totalItemCount > 0 && firstVisibleItem == 0 &&
				view.getChildAt(0).getTop() > 0 || totalItemCount == 0) {
			
			if (mScanCodeLayoutSmall) {
				mScanCodeAnimation.cancel();
				mImgScanCodeSmall.setVisibility(View.VISIBLE);
				Animator animator = ObjectAnimator.ofFloat(
						mLayoutScanCode, "translationY", mAnimationHeight, 0);
				Animator animator2 = ObjectAnimator.ofFloat(
						mImgScanCode, "translationY", mAnimationHeight*2, 0);
				Animator animator3 = ObjectAnimator.ofFloat(
						mImgScanCodeSmall, "translationY", 0, mAnimationHeight*2);
				mScanCodeAnimation.playTogether(animator, animator2, animator3);
				mScanCodeAnimation.start();
				mScanCodeLayoutSmall = false;
			}
		} else {
			if (!mScanCodeLayoutSmall) {
				mScanCodeAnimation.cancel();
				mImgScanCodeSmall.setVisibility(View.VISIBLE);
				Animator animator = ObjectAnimator.ofFloat(
						mLayoutScanCode, "translationY", 0, mAnimationHeight);
				Animator animator2 = ObjectAnimator.ofFloat(
						mImgScanCode, "translationY", 0, mAnimationHeight*2);
				Animator animator3 = ObjectAnimator.ofFloat(
						mImgScanCodeSmall, "translationY", mAnimationHeight*2, 0);
				mScanCodeAnimation.playTogether(animator, animator2, animator3);
				mScanCodeAnimation.start();
				mScanCodeLayoutSmall = true;
			}
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Placelist list callback
	///////////////////////////////////////////////////////////////////////////
	
	public interface onSideMenuClickListener {
		public void onSideMenuClick();
	}
}
