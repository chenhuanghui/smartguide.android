package vn.smartguide;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar.LayoutParams;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
	List<Drawable> images = new ArrayList<Drawable>();
	
	ImageSwitcher mAdsSwitcher = null;
	int index = 1;
	ChangeImage mChangeImage = null;

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
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (images.size() == 0)
						return;
					
					index = (index + 1) % images.size();
					mAdsSwitcher.setImageDrawable(images.get(index));
				}
			});
		}
	};
	
	public void startDownImage(){
		new DownloadImage().execute();
	}
	
	public class DownloadImage extends AsyncTask<Void, Void, Boolean> {

		String mJson = "";
		List<String> mURL = new ArrayList<String>();
		
		@Override
		protected Boolean doInBackground(Void... params) {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			mJson = NetworkManger.post(APILinkMaker.mGetAds(), pairs);
			return true;
		}

		@Override
		protected void onPostExecute(Boolean k){
			if (k == true){
				try {
					JSONArray imageArray = new JSONArray(mJson);
					for(int i = 0; i < imageArray.length(); i++){
						JSONObject image = imageArray.getJSONObject(i);
						String url = image.getString("image_url");
						
						new HttpConnection(new Handler() {
			        		@Override
			        		public void handleMessage(Message message) {
			        			switch (message.what) {
			        			case HttpConnection.DID_START: {
			        				break;
			        			}
			        			case HttpConnection.DID_SUCCEED: {
			        				try{
			        					Bitmap response = (Bitmap) message.obj;
			        					images.add(new BitmapDrawable(getActivity().getResources(), response));
			        					if (images.size() == 1){
			        						stopAds();
			        						startAds();
			        					}
			        				}catch(Exception ex){
			        				}
			        				break;
			        			}
			        			case HttpConnection.DID_ERROR: {
			        				Exception e = (Exception) message.obj;
			        				e.printStackTrace();
			        				break;
			        			}
			        			}
			        		}
			        		
			        	}).bitmap(url);
						
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
