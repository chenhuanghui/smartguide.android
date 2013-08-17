package vn.smartguide;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;


import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

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

	ImageView mLogo = null;
	ImageView mSlogan = null;
	ImageView mSmartGuide = null;

	int mHeight = 0;
	int mWidth = 0;
	int marginTop = 40;

	private ObjectAnimator mSloganSlideUp = null;
	private ObjectAnimator mLogoFadeIn = null;
	private ObjectAnimator mLogoSlideUp = null;
	private ObjectAnimator mSmartGuideSlideUp = null;

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
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.flash_screen, menu);
		return true;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus){
		if (isShow)
			return;
		isShow = true;

		new InitInformation().execute();

		mLogo = (ImageView)findViewById(R.id.logoF);
		mSlogan = (ImageView)findViewById(R.id.sloganF);
		mSmartGuide = (ImageView)findViewById(R.id.smartguideF);

		Display display = getWindowManager().getDefaultDisplay(); 
		mWidth = display.getWidth();
		mHeight = display.getHeight();

		int halfWidth = mWidth / 2;

		mLogo.setX(halfWidth - mLogo.getWidth() / 2);
		mSlogan.setX(halfWidth - mSlogan.getWidth() / 2);
		mSmartGuide.setX(halfWidth - mSmartGuide.getWidth() / 2);

		mLogoFadeIn = ObjectAnimator.ofFloat(mLogo, "alpha", 0.0f, 1.0f);
		mLogoFadeIn.setInterpolator(new AccelerateDecelerateInterpolator());

		mLogoSlideUp = ObjectAnimator.ofFloat(mLogo, "translationY", mHeight, mHeight / 2 - 100 - 95);
		mLogoSlideUp.setInterpolator(new AccelerateDecelerateInterpolator());
		mLogoSlideUp.addListener(new AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {
				mLogo.setVisibility(View.VISIBLE);
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						mSmartGuide.setVisibility(View.VISIBLE);
						mSmartGuideSlideUp.start();
					}
				}, 500);
			}

			@Override
			public void onAnimationRepeat(Animator animation) {

			}

			@Override
			public void onAnimationEnd(Animator animation) {

			}

			@Override
			public void onAnimationCancel(Animator animation) {

			}
		});

		List<ObjectAnimator> ListLogoAnimators = new ArrayList<ObjectAnimator>();
		ListLogoAnimators.add(mLogoFadeIn);
		ListLogoAnimators.add(mLogoSlideUp);

		ObjectAnimator[] objectAnimators = ListLogoAnimators.toArray(new ObjectAnimator[ListLogoAnimators.size()]);
		AnimatorSet animSetXY = new AnimatorSet();
		animSetXY.playTogether(objectAnimators);
		animSetXY.setDuration(1000);//1sec
		animSetXY.start();

		mSmartGuideSlideUp = ObjectAnimator.ofFloat(mSmartGuide, "translationY", mHeight, mHeight / 2 + 10);
		mSmartGuideSlideUp.setDuration(1000);
		mSmartGuideSlideUp.setInterpolator(new AccelerateDecelerateInterpolator());
		mSmartGuideSlideUp.addListener(new AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						mSlogan.setVisibility(View.VISIBLE);
						mSloganSlideUp.start();
					}
				}, 500);
			}

			@Override
			public void onAnimationRepeat(Animator animation) {}

			@Override
			public void onAnimationEnd(Animator animation) {}

			@Override
			public void onAnimationCancel(Animator animation) {}
		});
		//mSmartGuideSlideUp.start();

		mSloganSlideUp = ObjectAnimator.ofFloat(mSlogan, "translationY", mHeight, mHeight / 2 + 65);
		mSloganSlideUp.setDuration(1000);
		mSloganSlideUp.setInterpolator(new AccelerateDecelerateInterpolator());
		mSloganSlideUp.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator animation) {

			}

			@Override
			public void onAnimationRepeat(Animator animation) {

			}

			@Override
			public void onAnimationEnd(Animator animation) {
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						if (isFinish){

							if (getParent() == null) 
								setResult(Activity.RESULT_OK, resultData);
							else
								getParent().setResult(Activity.RESULT_OK, resultData);

							finish();
							//overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
							overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
						}

						isFinish = true;
					}
				}, 200);
			}

			@Override
			public void onAnimationCancel(Animator animation) {

			}
		});
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
			.imageScaleType(ImageScaleType.EXACTLY)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.build();

			ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
			.threadPoolSize(5)
			.discCacheFileNameGenerator(new Md5FileNameGenerator())
			.tasksProcessingOrder(QueueProcessingType.FIFO)
			.memoryCache(new WeakMemoryCache())
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
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

	}
}

