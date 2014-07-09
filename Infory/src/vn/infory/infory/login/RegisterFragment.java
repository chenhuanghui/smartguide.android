 package vn.infory.infory.login;

import vn.infory.infory.FontsCollection;
import vn.infory.infory.NonSlidePager;
import vn.infory.infory.PagerSlidingTabStrip;
import vn.infory.infory.R;
import vn.infory.infory.PagerSlidingTabStrip.OnTabClickListener;
import vn.infory.infory.login.InforyLoginActivity.BackListener;
import vn.infory.infory.network.UpdateProfile;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;

public class RegisterFragment extends Fragment implements BackListener {
	
	// Data
	private Listener mListener;
	private SelfCreate1Fragment frag1;
	private SelfCreate2Fragment frag2;

	// GUI
	@ViewById(id = R.id.pagerSelfCreate)		private NonSlidePager mPagerSelf;
	@ViewById(id = R.id.tabs)					private PagerSlidingTabStrip mStrip;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		return inflater.inflate(R.layout.login_register, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		try {
			AndroidAnnotationParser.parse(this, view);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		FontsCollection.setFont(view);

		mPagerSelf.setAdapter(new FragmentPagerAdapter(getChildFragmentManager()) {

			@Override
			public int getCount() {
				return 2;
			}

			@Override
			public Fragment getItem(int pos) {
				switch (pos) {
				case 0: {
					frag1 = new SelfCreate1Fragment();
					frag1.setListener(new SelfCreate1Fragment.Listener() {
						@Override
						public void onNextClick() {
							mPagerSelf.setCurrentItem(1);
						}
					});
					return frag1;
				}
				case 1: {
					frag2 = new SelfCreate2Fragment();
					frag2.setListener(new SelfCreate2Fragment.Listener() {
						@Override
						public void onConfirmClick() {
							Object[] result = frag1.getResult();
							int[] result2 = frag2.getResult();
							
							mListener.onRegisterClick(result, result2);
						}
					});
					return frag2;
				}
				}

				return null;
			}

			@Override
			public CharSequence getPageTitle(int pos) {
				switch (pos) {
				case 0:
					return "Bước 1";
				case 1:
					return "Bước 2";
				}

				return "";
			}
		});
		
		mStrip.setViewPager(mPagerSelf);
		mStrip.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				mPagerSelf.setDragable(position == 1);
			}
		});
		mStrip.setOnTabClickListener(new OnTabClickListener() {
			@Override
			public boolean onTabClick(int pos) {
				return pos == 1;
			}
		});
	}
	
	public void setListener(Listener listener) {
		mListener = listener;
	}
	
	public void showSoftKeyboard() {
		InputMethodManager inputMgr = (InputMethodManager)
				getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMgr.showSoftInput(frag1.mEdtUsername, InputMethodManager.SHOW_IMPLICIT);
	}
	
	public void hideSoftKeyboard() {
		InputMethodManager inputMgr = (InputMethodManager)
				getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMgr.hideSoftInputFromWindow(frag1.mEdtUsername.getWindowToken(), 0);
	}
	

	@Click(id = R.id.btnBack)
	private void onBackClick(View v) {
		if (mPagerSelf.getCurrentItem() == 1)
			mPagerSelf.setCurrentItem(0);
		else
			mListener.onBackClick();
	}

	@Override
	public void onBackPress() {
		onBackClick(null);
	}

	///////////////////////////////////////////////////////////////////////////
	// Listener
	///////////////////////////////////////////////////////////////////////////

	public interface Listener {
		public void onBackClick();
		public void onRegisterClick(Object[] result1, int[] result2);
	}
}
