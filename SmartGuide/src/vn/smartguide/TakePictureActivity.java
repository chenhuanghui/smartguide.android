package vn.smartguide;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.FacebookException;
import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.facebook.widget.LoginButton.OnErrorListener;
import com.google.analytics.tracking.android.EasyTracker;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class TakePictureActivity extends Activity {
	UiLifecycleHelper 	mUiHelper;
	private Intent cameraIntent;
	private Uri outputFileUri;
	private static Uri oldFileUri;
	public final int CAMERA_REQUEST_CODE = 11111;
	private ImageView imageView;
	private Button mSendBtn;
	private EditText mDescription;
	private Button mFaceBtn;
	LoginButton authButton = null;
	private boolean mShareFace = false;
	private RelativeLayout mLoadingLO = null;
	
	@SuppressLint("SimpleDateFormat")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_take_picture);

		Session.StatusCallback callback = new Session.StatusCallback() {
			@Override
			public void call(Session session, SessionState state, Exception exception) {
				if (state.isOpened()) {
					uploadAccessToken();
				} else if (state.isClosed()) {
				}

			}
		};

		mUiHelper = new UiLifecycleHelper(this, callback);
		mUiHelper.onCreate(savedInstanceState);

		imageView = (ImageView) findViewById(R.id.imageView1);
		mDescription = (EditText) findViewById(R.id.editText1);

		mFaceBtn = (Button)findViewById(R.id.btnFacebook);
		mFaceBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mShareFace = ! mShareFace;
				if (mShareFace){
					mFaceBtn.setBackgroundResource(R.drawable.facebook_tick);
					getPermission();
				}
				else{
					mFaceBtn.setBackgroundResource(R.drawable.facebook_nonetick);
				}
			}
		});

		mSendBtn = (Button)findViewById(R.id.btnSend);
		mSendBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//getPermission();
				new uploadImage().execute();
			}
		});

		authButton = (LoginButton) findViewById(R.id.authButton);
		authButton.setOnErrorListener(new OnErrorListener() {

			@Override
			public void onError(FacebookException error) {
			}
		});

		authButton.setPublishPermissions(Arrays.asList("publish_stream"));
		authButton.setSessionStatusCallback(new Session.StatusCallback() {
			@Override
			public void call(Session session, SessionState state, Exception exception) {

				if (session.isOpened()) {
					uploadAccessToken();
				}
			}
		});

		String filename = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()) + ".jpg";
		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + filename;
		File file = new File(path);
		outputFileUri = Uri.fromFile(file);

		cameraIntent  = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
		startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);

		mLoadingLO = (RelativeLayout)findViewById(R.id.loadingFace);
	}

	public void getPermission(){
		Session session = Session.getActiveSession();

		try{
			if (session != null){
				// Check for publish permissions    
				List<String> permissions = session.getPermissions();
				if (!permissions.contains("publish_stream")) {
					Session.NewPermissionsRequest newPermissionsRequest = new Session
							.NewPermissionsRequest(this, Arrays.asList("publish_stream"));
					session.requestNewPublishPermissions(newPermissionsRequest);
					return;
				}
			}else{
				authButton.performClick();
				return;
			}
		}catch(Exception ex){
			authButton.performClick();
		}

		// For test reseaon
		//		Request.Callback callback= new Request.Callback() {
		//
		//			@Override
		//			public void onCompleted(Response response) {
		//				JSONObject graphResponse = response
		//						.getGraphObject()
		//						.getInnerJSONObject();
		//				String postId = null;
		//				try {
		//					postId = graphResponse.getString("id");
		//				} catch (JSONException e) {
		//				}
		//			}
		//		};
		//		
		//		Bundle postParams = new Bundle();
		//        postParams.putString("name", "Facebook SDK for Android");
		//        postParams.putString("caption", "Build great social apps and get more installs.");
		//        postParams.putString("description", "The Facebook SDK for Android makes it easier and faster to develop Facebook integrated Android apps.");
		//        postParams.putString("link", "https://developers.facebook.com/android");
		//        postParams.putString("picture", "https://raw.github.com/fbsamples/ios-3.x-howtos/master/Images/iossdk_logo.png");
		//        
		//		Request request = new Request(session, "me/feed", postParams, 
		//				HttpMethod.POST, callback);
		//
		//		RequestAsyncTask task = new RequestAsyncTask(request);
		//		task.execute();
	}

	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);  // Add this method.
	}

	@SuppressLint("SimpleDateFormat")
	private void setPhoto()
	{
		String imgPath = outputFileUri.getPath();
		BitmapFactory.Options bmOpt = new BitmapFactory.Options();
		bmOpt.inJustDecodeBounds = true;

		BitmapFactory.decodeFile(imgPath, bmOpt);

		int photoW = 640;
		int photoH = 480;

		int targetW = 320;
		int targetH = 240;

		int scale = 1;
		if ((targetW > 0) || (targetH > 0)){
			scale = Math.min(photoW / targetW, photoH / targetH);
		}

		bmOpt.inJustDecodeBounds = false;
		bmOpt.inSampleSize = scale;
		bmOpt.inPurgeable = true;

		Bitmap bitmap = BitmapFactory.decodeFile(imgPath, bmOpt);
		imageView.setImageBitmap(RotateBitmap(bitmap, 90));

		int newWidth = 640;
		int newHeight = 480;

		String filename = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()) + ".jpeg";
		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + filename;
		File file = new File(path);

		try {
			FileOutputStream out = new FileOutputStream(file);
			Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true).compress(Bitmap.CompressFormat.JPEG, 90, out);
			out.flush();
			out.close();
			outputFileUri = Uri.fromFile(file);
			oldFileUri = Uri.fromFile(file);
		} catch (Exception e) {

		}
	}

	public static Bitmap RotateBitmap(Bitmap source, float angle)
	{
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		mUiHelper.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
		
		if (resultCode != RESULT_OK){
			finish();
			return;
		}

		switch(requestCode){
		case CAMERA_REQUEST_CODE:
			deleteOldPhoto();
			setPhoto();
			break;
		case 64206:
			uploadAccessToken();
		}
	}

	public void uploadAccessToken(){
		Session session = Session.getActiveSession();

		if (session != null){
			// Check for publish permissions    
			List<String> permissions = session.getPermissions();
			if (permissions.contains("publish_stream")) {
				Toast.makeText(this, session.getAccessToken(), Toast.LENGTH_LONG).show();
				new UpFaceAT(session.getAccessToken()).execute();
				return;
			}
		}
	}

	public class UpFaceAT extends AsyncTask<Void, Void, Boolean> {
		String mAT = "";
		public UpFaceAT(String at){
			mAT = at;
		}
		@Override
		protected Boolean doInBackground(Void... params) {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("user_id", GlobalVariable.userID));
			pairs.add(new BasicNameValuePair("fb_access_token", mAT));
			NetworkManger.post(APILinkMaker.mUpFaceAccessToken(), pairs);
			return true;
		}

		protected void onPostExecute(Boolean k) {
			mLoadingLO.setVisibility(View.GONE);
		}
		protected void onPreExecute(){
			mLoadingLO.setVisibility(View.VISIBLE);
		}
	}
	public static void deleteOldPhoto(){
		if (oldFileUri == null)
			return;

		File file = new File(oldFileUri.getPath());
		file.delete();
	}

	public class uploadImage extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			String URL = APILinkMaker.mUploadImage() + "?access_token=" + GlobalVariable.tokenID + GlobalVariable.footerURL;
			HttpPost post = new HttpPost(URL);

			FileBody bin = new FileBody(new File(outputFileUri.getPath()), "image/jpeg");
			try{
				MultipartEntity reqEntity = new MultipartEntity();
				reqEntity.addPart("shop_id", new StringBody(Integer.toString(GlobalVariable.mCurrentShop.mID)));
				reqEntity.addPart("user_id", new StringBody(GlobalVariable.userID));
				reqEntity.addPart("description", new StringBody(mDescription.getText().toString()));
				reqEntity.addPart("photo", bin);
				
				if (mShareFace)
					reqEntity.addPart("share", new StringBody("1"));
				
				post.setEntity(reqEntity);

				HttpResponse response = NetworkManger.httpclient.execute(post);
				HttpEntity resEntity = response.getEntity();
				String output = EntityUtils.toString(resEntity);
				
			}catch(Exception ex){
				return false;
			}
			return true;
		}

		protected void onPostExecute(Boolean k) {
			mLoadingLO.setVisibility(View.GONE);
			finish();
		}
		protected void onPreExecute(){ 
			mLoadingLO.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void finish() {
		try{
		deleteOldPhoto();
		}catch(Exception ex){
			
		}
		super.finish();
	}

	@Override
	public void onResume() {
		super.onResume();
		mUiHelper.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		mUiHelper.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mUiHelper.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mUiHelper.onSaveInstanceState(outState);
	}
}
