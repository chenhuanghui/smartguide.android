package vn.smartguide;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;


import com.google.analytics.tracking.android.EasyTracker;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.Animator.AnimatorListener;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

public class FlashScreenActivity extends Activity {
	
	private boolean isShow = false;
	private boolean isFinish = false;
	Intent resultData = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flash_screen);

		resultData = new Intent();
		resultData.putExtra("Database", "OK");
		resultData.putExtra("Connection", "OK");
		
		new InitInformation().execute();
		
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				if (isFinish) {
					finish();
					overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
				}
				
				isFinish = true;
			}
		}, 2000);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.flash_screen, menu);
		return true;
	}

	public class InitInformation extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			GlobalVariable.getActivateCodeFromDB();
			GlobalVariable.getTokenFromDB();
			GlobalVariable.getVersionFromDB();

			String json = "";

			if (GlobalVariable.isNeedUpdateFacebook){
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("fb_id", GlobalVariable.id));
				pairs.add(new BasicNameValuePair("user_id", GlobalVariable.user_id));
				pairs.add(new BasicNameValuePair("email", GlobalVariable.email));
				pairs.add(new BasicNameValuePair("name", GlobalVariable.name));
				pairs.add(new BasicNameValuePair("gender", String.valueOf(GlobalVariable.gender)));
				pairs.add(new BasicNameValuePair("dob", GlobalVariable.dob));
				pairs.add(new BasicNameValuePair("avatar", GlobalVariable.avatar));
				pairs.add(new BasicNameValuePair("job", GlobalVariable.job));

				json = NetworkManger.post(APILinkMaker.mPushInforFacebook(), pairs);
			}

			GlobalVariable.getFacebookFromDB();

			GlobalVariable.displayImageOptions = new DisplayImageOptions.Builder()
			.showStubImage(R.drawable.ic_stub)
			.showImageForEmptyUri(R.drawable.ic_empty)
			.showImageOnFail(R.drawable.ic_error)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.cacheOnDisc(true)
			.cacheInMemory()
			.imageScaleType(ImageScaleType.EXACTLY)
			.displayer(new RoundedBitmapDisplayer(20))
			.build();
			
			ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
			.threadPoolSize(6)
			.threadPriority(Thread.NORM_PRIORITY-1)
			.denyCacheImageMultipleSizesInMemory()
			.discCacheSize(10*1024*1024)
			.tasksProcessingOrder(QueueProcessingType.LIFO)
			.build();


			ImageLoader.getInstance().init(config);
			GlobalVariable.imageLoader = ImageLoader.getInstance();

			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("city", GlobalVariable.mCityID));
			pairs.add(new BasicNameValuePair("env", Integer.toString(GlobalVariable.mMode)));
			
			json = NetworkManger.post(APILinkMaker.mGroupByCity(), pairs);
			
			if (json != ""){
				resultData = new Intent();
				resultData.putExtra("Database", "OK");
				resultData.putExtra("Connection", "OK");

				try{
					JSONObject object = new JSONObject(json);
					int status = object.getInt("status");
					switch (status){
					case 0:
						GlobalVariable.mIsLaunching = true;
						GlobalVariable.mURL = object.getString("url");
						break;
					case 1:
						GlobalVariable.mCateogries = Category.getListCategory(object.getJSONArray("content"));
						pairs = new ArrayList<NameValuePair>();
						pairs.add(new BasicNameValuePair("type", "1")); // lấy version của city
						
						json = NetworkManger.post(APILinkMaker.mGetVersion(), pairs);
						String version = json.substring(1, json.length() - 1);
						if (version.compareTo(GlobalVariable.mVersion) != 0){
							GlobalVariable.isNeedUpdateCityList = true;
							GlobalVariable.mVersion = version;
						}

						break;
					}
				}catch(Exception ex){

				}
			}else{
				resultData = new Intent();
				resultData.putExtra("Database", "OK");
				resultData.putExtra("Connection", "False");
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean k){
			if (k == true){

				if (getParent() == null) 
					setResult(Activity.RESULT_OK, resultData);
				else
					getParent().setResult(Activity.RESULT_OK, resultData);

				if (isFinish == true){
					finish();
					overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
				}

				isFinish = true;
			}
		}

		@Override
		protected void onPreExecute(){
			
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);
	}
}
