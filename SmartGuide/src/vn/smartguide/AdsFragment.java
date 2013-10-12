package vn.smartguide;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher.ViewFactory;

public class AdsFragment extends Fragment {
	
	private List<Drawable> images = new ArrayList<Drawable>();
	private ImageSwitcher mAdsSwitcher;
	private int index = 1;
	private ChangeImage mChangeImage;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		return inflater.inflate(R.layout.ads_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mAdsSwitcher = (ImageSwitcher) getView().findViewById(R.id.ads_gallery);
		mAdsSwitcher.setFactory(new ViewFactory() {
			@SuppressWarnings("deprecation")
			public View makeView() {
				ImageView imageView = new ImageView(getActivity().getApplicationContext());
				imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
				imageView.setLayoutParams(new ImageSwitcher.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
				return imageView;
			}
		});

		((ImageButton)getView().findViewById(R.id.imageButton)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (images.size() == 0)
					return;

				stopAds();

				if (index == 0)
					index = images.size();

				index = (index - 1) % images.size();
				mAdsSwitcher.setImageDrawable(images.get(index));
				startAds();
			}
		});

		((ImageButton)getView().findViewById(R.id.imageButton2)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (images.size() == 0)
					return;

				stopAds();

				index = (index + 1) % images.size();

				mAdsSwitcher.setInAnimation(AnimationUtils.loadAnimation(getActivity(),
						android.R.anim.slide_in_left));
				mAdsSwitcher.setOutAnimation(AnimationUtils.loadAnimation(getActivity(),
						android.R.anim.slide_out_right));

				mAdsSwitcher.setImageDrawable(images.get(index));

				mAdsSwitcher.setInAnimation(AnimationUtils.loadAnimation(getActivity(),
						R.anim.slide_in_right));
				mAdsSwitcher.setOutAnimation(AnimationUtils.loadAnimation(getActivity(),
						R.anim.slide_out_left));

				startAds();
			}
		});

		mAdsSwitcher.setInAnimation(AnimationUtils.loadAnimation(getActivity(),
				R.anim.slide_in_right));
		mAdsSwitcher.setOutAnimation(AnimationUtils.loadAnimation(getActivity(),
				R.anim.slide_out_left));

		startAds();
	}


	void stopAds(){
		if (mChangeImage == null)
			return;
		mChangeImage.cancel();
	}

	void startAds(){
		mChangeImage = new ChangeImage();
		new Timer().schedule(mChangeImage, 2000, GlobalVariable.timeChangeAds);
	}

	class ChangeImage extends TimerTask {
		@Override
		public void run() {
			try{
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						try{
							if (images.size() == 0)
								return;

							index = (index + 1) % images.size();
							mAdsSwitcher.setImageDrawable(images.get(index));
						}catch(Exception ex){
							return;
						}
					}
				});
			}catch(Exception ex){
				
			}
		}
	};

	public void startDownImage(){
		new DownloadImage().execute();
	}

	private class DownloadImage extends AsyncTask<Void, Void, Boolean> {

		String mJson = "";
		
		@Override
		protected Boolean doInBackground(Void... params) {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			mJson = NetworkManger.post(APILinkMaker.mGetAds(), pairs);
			return true;
		}

		@SuppressLint("HandlerLeak")
		@Override
		protected void onPostExecute(Boolean k){
			if (k == true){
				try {
					JSONArray imageArray = new JSONArray(mJson);
					for(int i = 0; i < imageArray.length(); i++){
						JSONObject image = imageArray.getJSONObject(i);
						String url = image.getString("image_url");

						GlobalVariable.cyImageLoader.loadImage(url, new CyImageLoader.Listener() {
							@Override
							public void startLoad(int from) {
								switch (from) {
								case CyImageLoader.FROM_DISK:
								case CyImageLoader.FROM_NETWORK:
								}
							}

							@Override
							public void loadFinish(int from, Bitmap image, String url) {
								switch (from) {
								case CyImageLoader.FROM_MEMORY:
									break;

								case CyImageLoader.FROM_DISK:
								case CyImageLoader.FROM_NETWORK:
									images.add(new BitmapDrawable(getActivity().getResources(), image));
									if (images.size() == 1){
										stopAds();
										startAds();
									}
									break;
								}
							}

						}, new Point(0, 0), getActivity().getBaseContext());
					}
				} catch (JSONException e) {
				}
			}
		}

		@Override
		protected void onPreExecute(){

		}
	}
}
