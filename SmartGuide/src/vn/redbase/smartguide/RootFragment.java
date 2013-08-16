package vn.redbase.smartguide;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

/**
 * Created by cycrixlaptop on 7/24/13.
 */
public class RootFragment extends Fragment {

	SlidingMenu menu;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		return inflater.inflate(R.layout.root_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);

		// Hide side menu
		//        Fragment sideMenuFragment = getFragmentManager().findFragmentById(R.id.side_menu_fragment);
		//        getFragmentManager().beginTransaction().hide(sideMenuFragment).commit();

		menu = new SlidingMenu(getActivity());
		menu.setMode(SlidingMenu.LEFT);
		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		//        menu.setShadowWidthRes(R.dimen.shadow_width);
		//        menu.setShadowDrawable(R.drawable.shadow);
		menu.setBehindOffset(120);
		menu.setFadeDegree(0.35f);
		menu.attachToActivity(getActivity(), SlidingMenu.SLIDING_CONTENT);
		menu.setMenu(R.layout.side_menu_fragment);

	}

	private boolean _showSideMenu = false;
	public void toggleSideMenu() {
		menu.toggle();
	}
}