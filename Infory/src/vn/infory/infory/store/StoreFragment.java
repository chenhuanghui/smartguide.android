package vn.infory.infory.store;

import vn.infory.infory.CyUtils;
import vn.infory.infory.MainActivity;
import vn.infory.infory.R;
import vn.infory.infory.MainActivity.onSideMenuClickListener;
import vn.infory.infory.R.id;
import vn.infory.infory.R.layout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;

public class StoreFragment extends Fragment {
	
	// Data
	private Listener mListener = new Listener();
	private int mPrePaymentPage = 0;
	
	// GUI element
	@ViewById(id = R.id.pagerStore) 	private ViewPager mPager;
	@ViewById(id = R.id.imgAnimation)	private ImageView mImgAnimation;
	@ViewById(id = R.id.fliperLeftTitle)private ViewFlipper mFlipper;
	@ViewById(id = R.id.txtBack)		private TextView mTxtBack;
	@ViewById(id = R.id.btnSideMenu)	private ImageButton mBtnSideMenu;
	@ViewById(id = R.id.btnPayment)		private ImageButton mBtnPayment;
	private StorePager mAdapter;
	
	private StoreShopFragment 		mFragShop;
	private StoreMenuFragment		mFragMenu;
	private StoreItemFragment		mFragItem;
	private StorePaymentFragment	mFragPayment;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.store_fragment, container, true);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		try {
			AndroidAnnotationParser.parse(this, view);
		} catch (Exception e) {
			getActivity().finish();
		}
		
		mFragShop = new StoreShopFragment();
		mFragShop.setListener(new StoreShopFragment.Listener() {
			@Override
			public void onShopClick(int shopId) {
				mPager.setCurrentItem(1, true);
			}
		});
		
		mFragMenu = new StoreMenuFragment();
		mFragMenu.setListener(new StoreMenuFragment.Listener() {
			@Override
			public void onSeeMoreClick(int shopId) {
				mPager.setCurrentItem(2, true);
			}
		});
		
		mFragItem = new StoreItemFragment();
		mFragPayment = new StorePaymentFragment();
		
		mAdapter = new StorePager(getChildFragmentManager());
//		mPager.setOffscreenPageLimit(2);
		mPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int page) {
				if ((page == 0) ^ (mFlipper.getDisplayedChild() == 0))
					mFlipper.setDisplayedChild(page == 0 ? 0 : 1);
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				mImgAnimation.setTranslationX(
						-(mImgAnimation.getWidth() - getView().getWidth()) 
						/ 3 * (position + positionOffset));
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		mPager.setAdapter(mAdapter);
	}
	
	@Click(id = R.id.btnSideMenu)
	private void onSideMenuClick(View v) {
		mListener.onSideMenuClick();
	}
	
	@Click(id = R.id.txtBack)
	private void onBackClick(View v) {
		if (mPager.getCurrentItem() == 3)
			mPager.setCurrentItem(mPrePaymentPage, false);
		else
			mPager.setCurrentItem(mPager.getCurrentItem() - 1, true);
	}
	
	@Click(id = R.id.btnPayment)
	private void onPaymentClick(View v) {
		mPrePaymentPage = mPager.getCurrentItem();
		mPager.setCurrentItem(3, false);
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Public methods
	///////////////////////////////////////////////////////////////////////////
	
	public void setListener(Listener listener) {
		if (listener == null)
			listener = new Listener();
		mListener = listener;
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Store pager adapter
	///////////////////////////////////////////////////////////////////////////
	
	public class StorePager extends FragmentPagerAdapter {

		public StorePager(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int pos) {
			switch (pos) {
			case 0:
				return mFragShop;
			case 1:
				return mFragMenu;
			case 2:
				return mFragItem;
			case 3:
				return mFragPayment;
			default:
				return null;	
			}
		}

		@Override
		public int getCount() {
			return 4;
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	// Listener
	///////////////////////////////////////////////////////////////////////////
	
	public static class Listener implements MainActivity.onSideMenuClickListener {
		public void onSideMenuClick() {}
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Debug stuff
	///////////////////////////////////////////////////////////////////////////
	private static final boolean isDebug = true;
	private static final String TAG = "CycrixDebug";
	private static final String HEADER = "StoreFragment";
	private static void debugLog(String message) {
		if (CyUtils.isDebug && isDebug) Log.d(TAG, HEADER + " " + message);
	}
}
