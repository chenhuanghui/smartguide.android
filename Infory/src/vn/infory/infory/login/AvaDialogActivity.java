package vn.infory.infory.login;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import vn.infory.infory.CyImageLoader;
import vn.infory.infory.CyUtils;
import vn.infory.infory.R;
import vn.infory.infory.network.CyAsyncTask;
import vn.infory.infory.network.GetAvaList;
import vn.infory.infory.network.NetworkManager;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;

public class AvaDialogActivity extends FragmentActivity {

	public static final int SELECT_PICTURE = 201;

	// Data
	private static Listener sListener;
	private Listener mListener;
	private static List<String> sAvaList = new ArrayList<String>();
	private List<CyAsyncTask> mTaskList = new ArrayList<CyAsyncTask>();
	private String mAvaFile;
	private Bitmap mAvaBitmap;

	// GUI elements
	@ViewById(id = R.id.pagerAva)		private ViewPager mPagerAva;
	@ViewById(id = R.id.btnFromDevice)	private ImageButton mBtnFromDevice;
	@ViewById(id = R.id.btnConfirm)		private ImageButton mBtnConfirm;
	private AvaAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ava_dialog);

		mListener = sListener;
		sListener = null;

		try {
			AndroidAnnotationParser.parse(this, findViewById(android.R.id.content));
		} catch (Exception e) {
			e.printStackTrace();
			finish();
			return;
		}
		
		CyUtils.setHoverEffect(mBtnFromDevice, false);
		CyUtils.setHoverEffect(mBtnConfirm, false);

		mPagerAva.setClipChildren(false);
		mAdapter = new AvaAdapter(sAvaList.size());
		mPagerAva.setAdapter(mAdapter);
		mPagerAva.setOffscreenPageLimit(2);

		// Get avatar list
		if (sAvaList.size() == 0) {
			GetAvaList task = new GetAvaList(this) {
				@Override
				protected void onCompleted(Object result) {
					sAvaList.addAll((List<String>) result);
					mAdapter.ensureSize(sAvaList.size());
					mAdapter.notifyDataSetChanged();
				}

				@Override
				protected void onFail(Exception e) {
					CyUtils.showError("Lấy danh sách ảnh đại diện thất bại!", e, AvaDialogActivity.this);
				}
			};
			mTaskList.add(task);
			task.executeOnExecutor(NetworkManager.THREAD_POOL);
		}
	}

	public static void newInstance(Activity act, Listener listener) {
		sListener = listener;
		Intent intent = new Intent(act, AvaDialogActivity.class);
		act.startActivity(intent);
	}

	@Override
	protected void onDestroy() {	
		super.onDestroy();

		for (CyAsyncTask task : mTaskList)
			task.cancel(true);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {

		case SELECT_PICTURE:
			if (resultCode == RESULT_OK) {
				Uri selectedImageUri = data.getData();
				String strPath = getPath(selectedImageUri);
				try {
					setImage(strPath);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			break;
		}
	}

	private void setImage(String imgPath) throws Exception {
		BitmapFactory.Options bmOpt = new BitmapFactory.Options();
		bmOpt.inJustDecodeBounds = true;

		BitmapFactory.decodeFile(imgPath, bmOpt);

		ExifInterface exif = null;
		exif = new ExifInterface(imgPath);

		int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

		int photoW = bmOpt.outWidth;
		int photoH = bmOpt.outHeight;
		
		if (photoH < 0 || photoW < 0)
			throw new IOException("Cannot decode image file");

		int targetW = 180;
		int targetH = 180;

		int scale = 1;
			
		scale = Math.min(photoW / targetW, photoH / targetH);

		bmOpt.inJustDecodeBounds = false;
		bmOpt.inSampleSize = scale;
		bmOpt.inPurgeable = true;

		Bitmap bitmap = BitmapFactory.decodeFile(imgPath, bmOpt);
		
		if (bitmap == null)
			throw new IOException("Cannot decode image file");
		
		bitmap = rotateBitmap(bitmap, orientation);
		if (bitmap == null)
			throw new IOException("Cannot rotate bitmap");
		
//		imageView.setImageBitmap(bitmap);

		//			int newWidth = 640;
		//			int newHeight = 480;
		//
		String filename = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
			.format(Calendar.getInstance().getTime()) + ".jpeg";
		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + filename;
		File file = new File(path);
		
		FileOutputStream out = new FileOutputStream(file);
		bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
		out.close();
		
		mAdapter.setFileAva(bitmap);
		
		mAvaFile = path;
		mAvaBitmap = bitmap;
	}
	
	private static Bitmap rotateBitmap(Bitmap source, int orientation) {
		Matrix matrix = new Matrix();
		switch (orientation) {
		case ExifInterface.ORIENTATION_NORMAL:
			return source;
		case ExifInterface.ORIENTATION_ROTATE_90:
			matrix.postRotate(90);
			break;
		case ExifInterface.ORIENTATION_ROTATE_180:
			matrix.postRotate(180);
			break;
		case ExifInterface.ORIENTATION_ROTATE_270:
			matrix.postRotate(270);
			break;
		}
		return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
//		return source;
	}

	private String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };

		Cursor cursor = managedQuery(uri, projection, null, null, null);
		if(cursor!=null) {
			//HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
			//THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		}
		else return null;
	}

	@Click(id = R.id.btnBack)
	private void onBackClick(View v) {
		finish();
	}

	@Click(id = R.id.btnFromDevice)
	private void onFromDeviceClick(View v) {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_PICK);
		startActivityForResult(intent, SELECT_PICTURE);
	}
	
	@Click(id = R.id.btnConfirm)
	private void onConfirmClick(View v) {
		if (mAvaFile == null)
			mListener.onAvaSelect(sAvaList.get(mPagerAva.getCurrentItem()));
		else {
			if (mPagerAva.getCurrentItem() == 0) 
				mListener.onAvaSelectFile(mAvaFile, mAvaBitmap);
			else
				mListener.onAvaSelect(sAvaList.get(mPagerAva.getCurrentItem() - 1));
		}
		
		finish();
	}

	@Override
	public void onBackPressed() {
		onBackClick(null);
	}

	private class AvaAdapter extends PagerAdapter {
		
		private ArrayList<ViewGroup> mViewList = new ArrayList<ViewGroup>();
		private Bitmap mFileAvaBitmap;
		
		public AvaAdapter(int size) {
			for (int i = 0; i < size; i++) {
				mViewList.add((ViewGroup)
						getLayoutInflater().inflate(R.layout.ava_dialog_item, mPagerAva, false));
			}
		}
		
		public void ensureSize(int size) {
			while (mViewList.size() < size)
				mViewList.add((ViewGroup)
						getLayoutInflater().inflate(R.layout.ava_dialog_item, mPagerAva, false));
			
			notifyDataSetChanged();
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			ViewGroup view = mViewList.get(position);
			
			ImageView imgAva = (ImageView) view.findViewById(R.id.imgAva);
			if (position == 0 && mFileAvaBitmap != null)
				imgAva.setImageBitmap(mFileAvaBitmap);
			else {
				if (mFileAvaBitmap == null)
					CyImageLoader.instance().showImage(sAvaList.get(position), imgAva);
				else
					CyImageLoader.instance().showImage(sAvaList.get(position - 1), imgAva);
			}
			container.addView(view);
			return view;
		}
		
		public void setFileAva(Bitmap ava) {
			if (mFileAvaBitmap == null) {
				ViewGroup v = (ViewGroup)
						getLayoutInflater().inflate(R.layout.ava_dialog_item, mPagerAva, false);
				mViewList.add(0, v);
				mFileAvaBitmap = ava;
				
				notifyDataSetChanged();
			} else {
				View view = mViewList.get(0);
				ImageView imgAva = (ImageView) view.findViewById(R.id.imgAva);
				imgAva.setImageBitmap(ava);
				mFileAvaBitmap = ava;
			}
			mPagerAva.setCurrentItem(0, false);
		}

		@Override
		public int getCount() {
			return mViewList.size();
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View)object);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return (view == object);
		}
		
		@Override
		public int getItemPosition(Object object) {
			int index = mViewList.indexOf(object);
			if (index < 0)
				return PagerAdapter.POSITION_NONE;
			else
				return index;
		}
	}

	///////////////////////////////////////////////////////////////////////////
	// Listener
	///////////////////////////////////////////////////////////////////////////

	public static class Listener {
		public void onAvaSelect(String url) {}
		public void onAvaSelectFile(String path, Bitmap bitmap) {}
	}
}
