package vn.redbase.smartguide;


import android.support.v4.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by cycrixlaptop on 7/24/13.
 */
public class MainFrameFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		return inflater.inflate(R.layout.main_frame_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);

		((ImageButton) getView().findViewById(R.id.btnQRToggle))
		.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				toggleQRCodeView();
			}
		});

		((ImageButton) getView().findViewById(R.id.btnToggleMap))
		.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				((ContentFragment) getFragmentManager().findFragmentById(R.id.contentFragment))
				.toggleShowContent();
			}
		});
	}

	public void toggleQRCodeView() {
		RelativeLayout layoutQR = (RelativeLayout) getView().findViewById(R.id.layoutQR);
		if (layoutQR.getVisibility() == View.GONE) {
			layoutQR.setVisibility(View.VISIBLE);
		} else {
			layoutQR.setVisibility(View.GONE);
		}
	}
}