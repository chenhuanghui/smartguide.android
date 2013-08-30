package vn.smartguide;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.google.analytics.tracking.android.EasyTracker;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class TakePictureActivity extends Activity {

	private Intent cameraIntent;
	private Uri outputFileUri;
	private static Uri oldFileUri;
	public final int CAMERA_REQUEST_CODE = 11111;
	private ImageView imageView;
	private Button mSendBtn;
	private EditText mDescription;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_take_picture);

		imageView = (ImageView) findViewById(R.id.imageView1);
		mDescription = (EditText) findViewById(R.id.editText1);

		mSendBtn = (Button)findViewById(R.id.btnSend);
		mSendBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				uploadImage();
			}
		});

		String filename = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()) + ".jpg";
		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + filename;
		File file = new File(path);
		outputFileUri = Uri.fromFile(file);

		cameraIntent  = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
		startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
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
		if (resultCode != RESULT_OK)
			return;

		switch(requestCode){
		case CAMERA_REQUEST_CODE:
			deleteOldPhoto();
			setPhoto();
			break;
		}
	}

	public static void deleteOldPhoto(){
		if (oldFileUri == null)
			return;

		File file = new File(oldFileUri.getPath());
		file.delete();
	}

	public void uploadImage(){
		String URL = APILinkMaker.mUploadImage() + "?access_token=" + GlobalVariable.tokenID + GlobalVariable.footerURL;
		HttpPost post = new HttpPost(URL);

		FileBody bin = new FileBody(new File(outputFileUri.getPath()), "image/jpeg");
		try{
			MultipartEntity reqEntity = new MultipartEntity();
			reqEntity.addPart("shop_id", new StringBody(Integer.toString(GlobalVariable.mCurrentShop.mID)));
			reqEntity.addPart("user_id", new StringBody(GlobalVariable.userID));
			reqEntity.addPart("description", new StringBody(mDescription.getText().toString()));
			reqEntity.addPart("photo", bin);
			post.setEntity(reqEntity);
			
			HttpResponse response = NetworkManger.httpclient.execute(post);
            HttpEntity resEntity = response.getEntity();
            String output = EntityUtils.toString(resEntity);
		}catch(Exception ex){
		}
	}
	
	@Override
	public void finish() {
		deleteOldPhoto();
		super.finish();
	}
}
