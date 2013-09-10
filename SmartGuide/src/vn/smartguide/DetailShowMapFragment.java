package vn.smartguide;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class DetailShowMapFragment extends Fragment {

	private Shop mShop;

	@Override
	public void onAttach(Activity activity) {

		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.detail_showmap, container, false);

		ImageButton btn = (ImageButton) v.findViewById(R.id.imgDetailMap);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				Intent i = new Intent(getActivity(), MapActivity.class);
				i.putExtra("lat", mShop.mLat);
				i.putExtra("lng", mShop.mLng);
				getActivity().startActivity(i);
			}
		});

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
	}

	public void setData(Shop s) {

		mShop = s;

		// Construct URL

		View mapLayout = getView().findViewById(R.id.layoutDetailMap);
		mapLayout.addOnLayoutChangeListener(new OnLayoutChangeListener() {

			@Override
			public void onLayoutChange(View v, int left, int top, int right,
					int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {

				int w = (right - left) / 1;
				int h = (bottom - top) / 1;

				String url = "http://maps.googleapis.com/maps/api/staticmap" +
						"?zoom=14&size=%dx%d&markers=%f,%f&sensor=false&scale=1&visual_refresh=true";
				url = String.format(url, w, h, mShop.mLat, mShop.mLng);

				new HttpConnection(new Handler() {
					@Override
					public void handleMessage(Message msg) {

						switch (msg.what) {
						case HttpConnection.DID_SUCCEED:
							ImageButton img = (ImageButton) getView().findViewById(R.id.imgDetailMap);
							img.setImageBitmap((Bitmap) msg.obj);
							break;
						}
					}
				}).bitmap(url);
			}
		});
	}

	@Override
	public void onResume() {

		super.onResume();
	}

	@Override
	public void onPause() {

		super.onPause();
	}
}
