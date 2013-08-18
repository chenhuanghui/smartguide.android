package vn.smartguide;

import android.animation.ObjectAnimator;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;

/**
 * Created by cycrixlaptop on 7/26/13.
 */
public class ContentFragment extends Fragment {

	private boolean mShowContent = true;
	private View layout;
	private LinearLayout contentLayout;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		return inflater.inflate(R.layout.filter_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		layout = getActivity().findViewById(R.id.layoutContentFrame);
		contentLayout = (LinearLayout)getActivity().findViewById(R.id.layoutContentHolder);
	}

	public void toggleShowContent() {

		mShowContent = !mShowContent;
		ObjectAnimator animator = null;
		int height = contentLayout.getHeight();
		
		if (mShowContent)
			animator = ObjectAnimator.ofFloat(layout, "translationY", -height, 0);
		else
			animator = ObjectAnimator.ofFloat(layout, "translationY", 0, -height);

		animator.setInterpolator(new AccelerateDecelerateInterpolator());
		animator.start();
	}
}
