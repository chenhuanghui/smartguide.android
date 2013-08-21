package vn.smartguide;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.google.analytics.tracking.android.EasyTracker;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.Menu;
import android.widget.ImageView;

public class TakePictureActivity extends Activity {

	private Intent cameraIntent;
	private Uri outputFileUri;
	private static Uri oldFileUri;
	public final int CAMERA_REQUEST_CODE = 11111;
	private ImageView imageView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_take_picture);
		
		imageView = (ImageView) findViewById(R.id.imageView1);
		
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
		EasyTracker.getInstance(this).activityStart(this);  // Add this method.
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
        float aspectRatio = (float) photoW / (float) photoH;
        
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

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

        String filename = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()) + ".jpg";
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

    public String getRealPathFromURI(Uri contentUri) {
        String [] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery( contentUri, proj, null, null,null);

        if (cursor == null)
            return null;

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
}
