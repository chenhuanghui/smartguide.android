package vn.smartguide;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import vn.smartguide.WellcomeActivity.GetDefaultAvatar;


import com.google.analytics.tracking.android.EasyTracker;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Display;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FlashScreenActivity extends Activity {
	private boolean isFinish = false;
	Intent resultData = null;
	private boolean isArrange = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flash_screen);

		resultData = new Intent();
		resultData.putExtra("Database", "OK");
		resultData.putExtra("Connection", "OK");
		(new WellcomeActivity.GetDefaultAvatar()).execute();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (isArrange)
			return;
		isArrange = true;

		Display display = getWindowManager().getDefaultDisplay();
		@SuppressWarnings("deprecation")
		int mHeight = display.getHeight() / 4;

		ImageView layout = (ImageView) findViewById(R.id.logoF);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.setMargins(0, mHeight, 0, 0);
		layout.setLayoutParams(lp);

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
		int action_type = -1;
		JSONObject key;

		@Override
		protected Boolean doInBackground(Void... params) {
			try{
				key = new JSONObject(NetworkManger.get(APILinkMaker.mCheckEmergence() + "?access_token=" + GlobalVariable.tokenID + "&version=android"+ 
				Build.VERSION.RELEASE + "_" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName, false));
				////+ getPackageManager().getPackageInfo(getPackageName(), 0).versionName
//				key = new JSONObject(
//						"{\"notify_list\":[{\"content\":\"xcscxczxczc\",\"notification_id\":1}],\"notification_type\":2}");
				action_type = key.getInt("notification_type");
			}catch(Exception ex){
				action_type = -1;
				return false;
			}

			if (action_type != 0)
				return false;

			try{
				URL myUrl = new URL(APILinkMaker.mHostName);
				URLConnection connection = myUrl.openConnection();
				connection.setConnectTimeout(3000);
				connection.connect();
			} catch (Exception e) {
				resultData = new Intent();
				resultData.putExtra("Database", "OK");
				resultData.putExtra("Connection", "False");
				return true;
			}
			
			GlobalVariable.getActivateCodeFromDB();
			GlobalVariable.getTokenFromDB();
			GlobalVariable.getVersionFromDB();
			
			(new WellcomeActivity.GetDefaultAvatar()).execute();

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
				pairs.add(new BasicNameValuePair("fb_access_token", GlobalVariable.faceAccessToken));

				json = NetworkManger.post(APILinkMaker.mPushInforFacebook(), pairs);
			}

			GlobalVariable.getFacebookFromDB();

			if (GlobalVariable.mAvatarList == null || GlobalVariable.mAvatarList.size() == 0){
				String JSResult = NetworkManger.get(APILinkMaker.mGetDefaultAvatar(), false);
				try{
					JSONArray arrayImage = new JSONArray(JSResult);
					GlobalVariable.mAvatarList = new ArrayList<String>();
					for(int i = 0; i < arrayImage.length(); i++){
						GlobalVariable.mAvatarList.add(arrayImage.getString(i));
					}
				} catch (Exception ex) {

				}
			}

			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("city", GlobalVariable.mCityID));
			pairs.add(new BasicNameValuePair("env", Integer.toString(GlobalVariable.mMode)));

			json = NetworkManger.post(APILinkMaker.mGroupByCity(), pairs);
			
			try{
				JSONObject object = new JSONObject(json);
				if (object.getString("error").compareTo("invalid_grant") == 0 || object.getString("error").compareTo("invalid_client") == 0){
					return false;
				}
			}catch(Exception ex){
				
			}
			
			if (json != "") {
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
		protected void onPostExecute(Boolean k) {
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
			} else {
				try{
					switch(action_type) {
					case -1:
						
						resultData = new Intent();
						resultData.putExtra("Database", "OK");
						resultData.putExtra("Connection", "False");
						
						if (getParent() == null) 
							setResult(Activity.RESULT_OK, resultData);
						else
							getParent().setResult(Activity.RESULT_OK, resultData);

						if (isFinish == true){
							finish();
							overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
						}
						break;
					case 1:
						String link =  key.getString("link");
						String content = key.getString("content");
						String message = "<a href=\"" + link + "\">" + content + "</a>";

						AlertDialog d = new AlertDialog.Builder(FlashScreenActivity.this)
						.setPositiveButton("OK", new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								resultData = new Intent();
								resultData.putExtra("Database", "OK");
								resultData.putExtra("Connection", "False");
								resultData.putExtra("Finish", "True");
								setResult(Activity.RESULT_OK, resultData);
								finish();
								overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
								return;
								
							}
						})
						.setIcon(R.drawable.logo)
						.setMessage(Html.fromHtml(message))
						.create();
						d.show();
						// Make the textview clickable. Must be called after show()   
						((TextView)d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
						break;
					case 2:
						JSONArray jNotifyArr = key.getJSONArray("notify_list");
						StringBuilder builder = new StringBuilder();
						for (int i = 0; i < jNotifyArr.length(); i++) {
							builder.append(jNotifyArr.getJSONObject(i).getString("content")).append("\n");
						}
						
						AlertDialog g = new AlertDialog.Builder(FlashScreenActivity.this)
						.setPositiveButton("OK", new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								resultData = new Intent();
								resultData.putExtra("Database", "OK");
								resultData.putExtra("Connection", "False");
								setResult(RESULT_CANCELED, resultData);
								finish();
								overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
								return;
							}
						})
						.setIcon(R.drawable.logo)
						.setMessage(builder.toString())
						.create();
						g.show();
						break;
					case 3:
						AlertDialog f = new AlertDialog.Builder(FlashScreenActivity.this)
						.setPositiveButton("OK", new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								resultData = new Intent();
								resultData.putExtra("Database", "OK");
								resultData.putExtra("Connection", "False");
								setResult(RESULT_CANCELED, resultData);
								finish();
								overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
								return;
								
							}
						})
						.setIcon(R.drawable.logo)
						.setMessage(key.getString("content"))
						.create();
						f.show();
						// Make the textview clickable. Must be called after show()   
						((TextView)f.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
						break;
					}
				}catch(Exception ex){

				}
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

	@Override
	public void onBackPressed() {
		return;
	}
}
