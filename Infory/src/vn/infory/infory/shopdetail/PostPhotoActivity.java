package vn.infory.infory.shopdetail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.json.JSONObject;

import vn.infory.infory.CyLogger;
import vn.infory.infory.CyUtils;
import vn.infory.infory.FontsCollection;
import vn.infory.infory.R;
import vn.infory.infory.R.id;
import vn.infory.infory.R.layout;
import vn.infory.infory.network.CyAsyncTask;
import vn.infory.infory.network.NetworkManager;
import vn.infory.infory.network.PostPhotoBinary;
import vn.infory.infory.network.PostPhotoDsc;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;

public class PostPhotoActivity extends Activity {
	
	public final int CAMERA_REQUEST_CODE = 11111;
	
	private static Listener sListener;
	private static int sShopId;
	private static Activity sAct;
	
	private Listener mListener;
	private Uri outputFileUri;
	private CyLogger mLog = new CyLogger("PostPhotoActivity", true);
	private int mIdUserGallery;
	private String mDsc;
	private Activity mAct;
	private boolean mDestroyed = false;
	private Bitmap mBitmap;
	
	// GUI
	@ViewById(id = R.id.btnSend) 		private View mBtnSend;
	@ViewById(id = R.id.edtDescription)	private	EditText mEdtDsc;
	@ViewById(id = R.id.layoutPreview)	private View mLayoutPreview;
	@ViewById(id = R.id.imgPreview)		private ImageView mImgPreview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mListener 	= sListener;
		sListener 	= null;
		mAct 		= sAct;
		sAct		= null;
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post_photo);
		
		try {
			AndroidAnnotationParser.parse(this, findViewById(android.R.id.content));
		} catch (Exception e) {
			e.printStackTrace();
			finish();
			return;
		}
		
		FontsCollection.setFont(findViewById(android.R.id.content));
		CyUtils.setHoverEffect(mBtnSend, false);
		
		// Start capture image activity
		String filename = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()) + ".jpg";
		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + filename;
		File file = new File(path);
		outputFileUri = Uri.fromFile(file);
		
		Intent cameraIntent  = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
		startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		mDestroyed = true;
	}
	
	public static void newInstance(Activity act, int shopId, Listener listener) {
		sListener 	= listener;
		sShopId 	= shopId;
		sAct		= act;
		
		Intent intent = new Intent(act, PostPhotoActivity.class);
		act.startActivity(intent);
	}
	
	private void showError(Exception e) {
		if (mDestroyed)
			CyUtils.showError("Tải ảnh lên thất bại!", e, mAct);
		else
			CyUtils.showError("Tải ảnh lên thất bại!", e, this);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (resultCode != RESULT_OK) {
			finish();
			return;
		}

		switch(requestCode) {
		case CAMERA_REQUEST_CODE:
			setPhoto();
			break;
		}
	}
	
	@Click(id = R.id.btnSend)
	private void onSendClick(View v) {
		mDsc = mEdtDsc.getText().toString().trim();
		
		postDscIfPossible();
		finish();
		mListener.onFinish(mBitmap);
	}
	
	@SuppressLint("SimpleDateFormat")
	private void setPhoto()
	{
		String imgPath = outputFileUri.getPath();
		BitmapFactory.Options bmOpt = new BitmapFactory.Options();
		bmOpt.inJustDecodeBounds = true;

		BitmapFactory.decodeFile(imgPath, bmOpt);
		
		ExifInterface exif = null;
		try {
			exif = new ExifInterface(imgPath);
		} catch (IOException e1) {
			return;
		}
		
		int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

		int photoW = bmOpt.outWidth;
		int photoH = bmOpt.outHeight;

		int targetW = 1920;
		int targetH = 1280;

		int scale = 1;
		if ((targetW > 0) || (targetH > 0))
			scale = Math.max(Math.min(photoW / targetW, photoH / targetH), 1);

		bmOpt.inJustDecodeBounds = false;
		bmOpt.inSampleSize = scale;
		bmOpt.inPurgeable = true;

		Bitmap bitmap = BitmapFactory.decodeFile(imgPath, bmOpt);
		bitmap = RotateBitmap(bitmap, orientation);
		mImgPreview.setImageBitmap(bitmap);
		mLayoutPreview.setBackgroundDrawable(new BitmapDrawable(bitmap));

//		int newWidth = 640;
//		int newHeight = 480;
//
		String filename = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()) + ".jpeg";
		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + filename;
		File file = new File(path);

		try {
			FileOutputStream out = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
//			out.flush();
//			out.close();
			outputFileUri = Uri.fromFile(file);
//			oldFileUri = Uri.fromFile(file);;
		} catch (Exception e) {
			
		}
		
		CyAsyncTask task = new PostPhotoBinary(getApplicationContext(), path, sShopId) {
			@Override
			protected void onCompleted(Object result2) throws Exception {
				try {
					JSONObject result = (JSONObject) result2;
					
					if (result.getInt("status") == 1) {
						mIdUserGallery = result.getInt("idUserGallery");
						
						postDscIfPossible();
					} else {
						onFail(null);
					}
				} catch (Exception e) {
					onFail(e);
				}
			}
			
			@Override
			protected void onFail(Exception e) {
				showError(e);
			}
		};
		task.executeOnExecutor(NetworkManager.THREAD_POOL);
		
		mBitmap = bitmap;
	}
	
	private void postDscIfPossible() {
		if (mIdUserGallery == 0 || mDsc == null)
			return;
		
		CyAsyncTask task = new PostPhotoDsc(getApplicationContext(), mIdUserGallery, mDsc) {
			@Override
			protected void onCompleted(Object result2) throws Exception {
				try {
					JSONObject result = (JSONObject) result2;
					String message = result.optString("message");
					
					if (message.length() != 0) {
						Activity act;
						if (mDestroyed)
							act = mAct;
						else
							act = PostPhotoActivity.this;
						AlertDialog.Builder builder = 
								new AlertDialog.Builder(act);
						builder.setMessage(message);
						builder.create().show();
					}
				} catch (Exception e) {
					onFail(e);
				}
			}
			
			@Override
			protected void onFail(Exception e) {
				showError(e);
			}
		};
		task.executeOnExecutor(NetworkManager.THREAD_POOL);
	}

	private static Bitmap RotateBitmap(Bitmap source, int orientation) {
		Matrix matrix = new Matrix();
		
		switch (orientation) {
		
		case ExifInterface.ORIENTATION_ROTATE_90:
			matrix.postRotate(90);
			break;
		case ExifInterface.ORIENTATION_ROTATE_180:
			matrix.postRotate(180);
			break;
		case ExifInterface.ORIENTATION_ROTATE_270:
			matrix.postRotate(270);
			break;
		default:
			return source;
		}
		return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
//		return source;
	}
	
	public static class Listener {
		public void onFinish(Bitmap newBitmap) {}
	}
}
