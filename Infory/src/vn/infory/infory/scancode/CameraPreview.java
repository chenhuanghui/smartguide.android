/*
 * Barebones implementation of displaying camera preview.
 * 
 * Created by lisah0 on 2012-02-24
 */
package vn.infory.infory.scancode;

import java.io.IOException;

import vn.infory.infory.CyUtils;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PreviewCallback;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/** A basic Camera preview class */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

	private SurfaceHolder mHolder;
	private Camera mCamera;
	private PreviewCallback previewCallback;
	private AutoFocusCallback autoFocusCallback = new FocusCallback();
	
	private class FocusCallback implements AutoFocusCallback, Runnable {
		
		private Handler mHandler = new Handler();
		
		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			mHandler.postDelayed(this, 1000);
		}

		@Override
		public void run() {
			try {
				mCamera.autoFocus(this);
			} catch (Exception e) { }
		}
	}

	public CameraPreview(Context context, Camera cam) {
		super(context);
		mCamera = cam;
		init();
	}

	public CameraPreview(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CameraPreview(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		mHolder = getHolder();
		mHolder.addCallback(this);
	}
//	
//	public Camera getCamera() {
//		return mCamera;
//	}
	
	public void setPreviewCallback(PreviewCallback callback) {
		previewCallback = callback;
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, now tell the camera where to draw the preview.
		try {
			mCamera.setPreviewDisplay(holder);
			mCamera.startPreview();
		} catch (IOException e) {
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// Camera preview released in activity
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		/*
		 * If your preview can change or rotate, take care of those events here.
		 * Make sure to stop the preview before resizing or reformatting it.
		 */
		if (mHolder.getSurface() == null) {
			// preview surface does not exist
			return;
		}

		// stop preview before making changes
		try {
			mCamera.stopPreview();
		} catch (Exception e) {
			// ignore: tried to stop a non-existent preview
		}

		try {
			// Hard code camera surface rotation 90 degs to match Activity view in portrait
			mCamera.setDisplayOrientation(90);

			mCamera.setPreviewDisplay(mHolder);
			mCamera.setPreviewCallback(previewCallback);
			mCamera.startPreview();
			mCamera.autoFocus(autoFocusCallback);
		} catch (Exception e) {
		}
	}
}
