package vn.infory.infory.scancode;

import java.util.ArrayList;
import java.util.List;

import vn.infory.infory.CyUtils;
import vn.infory.infory.FontsCollection;
import vn.infory.infory.R;
import vn.infory.infory.data.ScanResponse;
import vn.infory.infory.data.Shop;
import vn.infory.infory.network.CyAsyncTask;
import vn.infory.infory.network.NetworkManager;
import vn.infory.infory.network.ScanCode;
import vn.infory.infory.shopdetail.ShopDetailActivity;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

public class ScanCodeFragment extends Fragment {
	
	// Data
	private QRCodeReader mQRCodeReader = new QRCodeReader();
	private boolean isCanScan = true;
	private List<CyAsyncTask> mTaskList = new ArrayList<CyAsyncTask>();
	
	private Camera mCamera;
    private CameraPreview mPreview;
    
	// GUI
    @ViewById(id = R.id.layoutCameraHolder)	private FrameLayout mLayoutCamHolder;
	@ViewById(id = R.id.btnFlash)			private Button mBtnFlash;
	@ViewById(id = R.id.layoutLoading)		private View mLayoutLoading;
	@ViewById(id = R.id.layoutLoadingAnimation)private View mLayoutLoadingAnimation;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mPreview = new CameraPreview(getActivity());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		return inflater.inflate(R.layout.scan_code_fragment, container, false);
	}
	
	private PreviewCallback mPreviewCallback = new PreviewCallback() {
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			if (!isCanScan)
				return;
			
			Camera.Parameters parameters = camera.getParameters();
			int w = parameters.getPreviewSize().width;
			int h = parameters.getPreviewSize().height;
			
			YUVLuminanceSource source = new YUVLuminanceSource(data, w, h, 0, 0, w, h);
			
			BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
			Result rs = null;

			try {
				rs = mQRCodeReader.decode(bitmap);
			} catch (Exception e) {
				return;
			}

			String mQRCode = rs.getText();
			isCanScan = false;
			
			// Call scan code api
			ScanCode scanCodeTask = new ScanCode(getActivity(), mQRCode) {
				@Override
				protected void onCompleted(Object result2) throws Exception {
					mTaskList.remove(this);
					
					ScanResponse response = (ScanResponse) result2;
					processScanResponse(response);
				}
				
				@Override
				protected void onFail(Exception e) {
					mTaskList.remove(this);
					
					ScanResponse response = new ScanResponse();
					response.status = -1;
					response.message = "Mất kết nối!";
					processScanResponse(response);
				}
			};
			mTaskList.add(scanCodeTask);
			scanCodeTask.setVisibleView(mLayoutLoading);
			scanCodeTask.executeOnExecutor(NetworkManager.THREAD_POOL);
			AnimationDrawable frameAnimation = (AnimationDrawable) 
					mLayoutLoadingAnimation.getBackground();
			frameAnimation.start();
		}
	};
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		try {
			AndroidAnnotationParser.parse(this, view);
		} catch (Exception e) {
			e.printStackTrace();
			getActivity().finish();
			return;
		}
		
		if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			CyUtils.showError("Thiết bị không hỗ trợ camera", null, getActivity());
			return;
		}
		
		mLayoutCamHolder.addView(mPreview);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mCamera = getCameraInstance();
		mPreview.setCamera(mCamera);
		mPreview.setPreviewCallback(mPreviewCallback);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
//		mCamera.lock();
		mCamera.stopPreview();
		mCamera.setPreviewCallback(null);
		mCamera.release();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		for (CyAsyncTask task : mTaskList)
			task.cancel(true);
	}
	
	public Camera getCameraInstance() {
		Camera c = null;

		try {
			try {
				c = Camera.open(CameraInfo.CAMERA_FACING_BACK);
			} catch (Exception e) {
				c = Camera.open(CameraInfo.CAMERA_FACING_FRONT);
			}
		} catch (Exception e) {
		}

		return c;
	}
	
	@Click(id = R.id.btnClose)
	private void onCloseClick(View v) {
		getActivity().finish();
	}
	
	private boolean mFlash = false;
	@Click(id = R.id.btnFlash)
	private void onFlashClick(View v) {
		
		if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
			CyUtils.showError("Thiết bị không hỗ trợ đèn flash", null, getActivity());
			return;
		}
		
		Camera cam = mCamera;
		if (cam == null)
			return;
		
		Parameters p = cam.getParameters();
		cam.cancelAutoFocus();
		if (!mFlash) {
			p.setFlashMode(Parameters.FLASH_MODE_TORCH);
			mBtnFlash.setText("On ");
		} else {
			p.setFlashMode(Parameters.FLASH_MODE_OFF);
			mBtnFlash.setText("Off ");
		}
		mFlash = !mFlash;
		cam.setParameters(p);
	}
	
	private void processScanResponse(final ScanResponse response) {
		
		final Dialog dlg = new Dialog(getActivity(),
				android.R.style.Theme_Translucent_NoTitleBar);
		View v = getActivity().getLayoutInflater().inflate(R.layout.scan_dlg, null);
		
		View layoutFlag = v.findViewById(R.id.layoutFlag);
//		TextView txtShop = (TextView) v.findViewById(R.id.txtShop);
		TextView txtShopName = (TextView) v.findViewById(R.id.txtShopName);
		View btnClose = v.findViewById(R.id.btnClose);
		TextView btnContinue = (TextView) v.findViewById(R.id.btnContinue);
		FrameLayout layoutContentHolder = (FrameLayout) v.findViewById(R.id.layoutContentHolder);
		
		btnClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				getActivity().finish();
			}
		});
		
		if (response.idShop == 0) {
			btnContinue.setText("Quét tiếp");
			btnContinue.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dlg.dismiss();
					isCanScan = true;
				}
			});
		} else { 
			btnContinue.setText("Đến cửa hàng");
			btnContinue.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Shop s = new Shop();
					s.idShop = response.idShop;
					ShopDetailActivity.newInstance(getActivity(), s);
					getActivity().finish();
				}
			});
		}
		
		
		switch (response.status) {
		case -1:
		case 0: {
			layoutFlag.setBackgroundResource(R.drawable.background_greyflag_arlet);
//			txtShop.setVisibility(View.GONE);
			txtShopName.setVisibility(View.GONE);
			
			View childView = getActivity().getLayoutInflater()
					.inflate(R.layout.scan_error, layoutContentHolder, true);
			ImageView imgIcon = (ImageView) childView.findViewById(R.id.imgIcon);
			TextView txtMessage = (TextView) childView.findViewById(R.id.txtMess);
			
			if (response.status == 0)
				imgIcon.setImageResource(R.drawable.icon_wrong_arlet);
			else
				imgIcon.setImageResource(R.drawable.icon_disconnect_arlet);
			txtMessage.setText(response.message);
			break;
		}
		
		case 1: {
			layoutFlag.setBackgroundResource(R.drawable.background_flag_arlet);
			txtShopName.setText(response.shopName);
			
			View childView = getActivity().getLayoutInflater()
					.inflate(R.layout.scan_error, layoutContentHolder, true);
			ImageView imgIcon = (ImageView) childView.findViewById(R.id.imgIcon);
			TextView txtMessage = (TextView) childView.findViewById(R.id.txtMess);
			
			imgIcon.setImageResource(R.drawable.icon_condition_arlet);
			txtMessage.setText(response.message);
			break;
		}
		
		case 2: {
			layoutFlag.setBackgroundResource(R.drawable.background_flag_arlet);
			txtShopName.setText(response.shopName);
			
			View childView = getActivity().getLayoutInflater()
					.inflate(R.layout.scan_sgp_point, layoutContentHolder, true);
			
			TextView txtMessage = (TextView) childView.findViewById(R.id.txtMess);
			TextView txtSGP = (TextView) childView.findViewById(R.id.txtSGP);
			
			txtMessage.setText(response.message);
			txtSGP.setText(response.sgp);
			break;
		}
		
		case 3: {
			layoutFlag.setBackgroundResource(R.drawable.background_flag_arlet);
			txtShopName.setText(response.shopName);
			
			View childView = getActivity().getLayoutInflater()
					.inflate(R.layout.scan_sgp_reward, layoutContentHolder, true);
			
			TextView txtMessage = (TextView) childView.findViewById(R.id.txtMess);
			TextView txtType = (TextView) childView.findViewById(R.id.txtType);
			TextView txtGiftName = (TextView) childView.findViewById(R.id.txtGiftName);
			TextView txtSGP = (TextView) childView.findViewById(R.id.txtSGP);
			
			txtMessage.setText(response.message);
			txtType.setText(response.type);
			txtGiftName.setText(response.giftName);
			txtSGP.setText(response.sgp);
			break;
		}
		
		case 4: {
			layoutFlag.setBackgroundResource(R.drawable.background_flag_arlet);
			txtShopName.setText(response.shopName);
			
			View childView = getActivity().getLayoutInflater()
					.inflate(R.layout.scan_voucher, layoutContentHolder, true);
			
			TextView txtMessage = (TextView) childView.findViewById(R.id.txtMess);
			TextView txtType = (TextView) childView.findViewById(R.id.txtType);
			TextView txtVoucherName = (TextView) childView.findViewById(R.id.txtVoucherName);
			
			txtMessage.setText(response.message);
			txtType.setText(response.type);
			txtVoucherName.setText(response.voucherName);
			break;
		}
		
		}
		
		FontsCollection.setFont(v);
		dlg.setContentView(v);
		dlg.show();
	}
}
