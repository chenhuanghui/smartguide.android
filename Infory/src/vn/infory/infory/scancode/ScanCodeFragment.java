package vn.infory.infory.scancode;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import vn.infory.infory.CyImageLoader;
import vn.infory.infory.CyUtils;
import vn.infory.infory.FlashActivity;
import vn.infory.infory.FontsCollection;
import vn.infory.infory.LayoutError;
import vn.infory.infory.R;
import vn.infory.infory.WebActivity;
import vn.infory.infory.data.PlaceList;
import vn.infory.infory.data.ScanResponse;
import vn.infory.infory.data.Shop;
import vn.infory.infory.network.CyAsyncTask;
import vn.infory.infory.network.GetPlaceListDetail;
import vn.infory.infory.network.GetShopDetail2;
import vn.infory.infory.network.NetworkManager;
import vn.infory.infory.network.ScanCode;
import vn.infory.infory.network.ScanCodeRelated;
import vn.infory.infory.shopdetail.ShopDetailActivity;
import vn.infory.infory.shoplist.ShopListActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

public class ScanCodeFragment extends Fragment {
	
	// Data
//	private QRCodeReader mQRCodeReader = new QRCodeReader();
	private boolean isCanScan = true;
	private Object objScanCode;
	private List<CyAsyncTask> mTaskList = new ArrayList<CyAsyncTask>();
	
	private Camera mCamera;
    private CameraPreview mPreview;
    private ImageScanner mScanner;
    private Handler mAutoFocusHandler;
    private boolean mPreviewing = true;
    
	// GUI
//    @ViewById(id = R.id.llScanDLG2)			private LinearLayout mLLScanDLG2;
    @ViewById(id = R.id.layoutCameraHolder)	private FrameLayout mLayoutCamHolder;
	@ViewById(id = R.id.btnFlash)			private Button mBtnFlash;
	@ViewById(id = R.id.layoutLoading)		private View mLayoutLoading;
	@ViewById(id = R.id.layoutLoadingAnimation)private View mLayoutLoadingAnimation;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mAutoFocusHandler = new Handler();

        // Create and configure the ImageScanner;
        setupScanner();

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
	        Camera.Size size = parameters.getPreviewSize();
			
			/*YUVLuminanceSource source = new YUVLuminanceSource(data, w, h, 0, 0, w, h);
			
			BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
			Result rs = null;

			try {
				rs = mQRCodeReader.decode(bitmap);
			} catch (Exception e) {
				return;
			}*/
			
			Image barcode = new Image(size.width, size.height, "Y800");
	        barcode.setData(data);

	        int result = mScanner.scanImage(barcode);	  
	        if (result != 0) {
	        	 mCamera.cancelAutoFocus();
	             mCamera.setPreviewCallback(null);
	             mCamera.stopPreview();
	             mPreviewing = false;

	            SymbolSet syms = mScanner.getResults();
	            for (Symbol sym : syms) {	                
	                try {
	    				Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
	    				 // Vibrate for 500 milliseconds
	    				v.vibrate(300);
	    			} catch (Exception e) {
	    				// TODO: handle exception
	    			}
	                
	                final String code = sym.getData();
	                
	                if(sym.getType() == 13) //Barcode
	                {
	                	WebActivity.newInstance(getActivity(), "http://shp.li/barcode/ean_13/"+code+"/?t=v&partner=scan");
						getActivity().finish();
	                }
	                else
	                {
	        			mLayoutLoading.setVisibility(View.VISIBLE);
	            		AnimationDrawable frameAnimation = (AnimationDrawable) 
	        					mLayoutLoadingAnimation.getBackground();
	        			frameAnimation.start();
	        			
	        			isCanScan = false;	
	        			
	        			String prefix = "http://page.infory.vn/";	
	        						
	        			if(code.toLowerCase().startsWith("http://") ||
	        					code.toLowerCase().startsWith("https://") ||
	        					code.toLowerCase().startsWith("www."))
	        			{
	        				if(code.toLowerCase().contains("?infory=true") || 
	        						code.toLowerCase().contains("&infory=true"))
	        				{				
	        					try {
	        						ScanCode scanCodeTask = new ScanCode(getActivity(), code) {
	        							@Override
	        							protected void onCompleted(final Object result2) throws Exception {
	        								mTaskList.remove(this);
	        								
	        								objScanCode = result2;
	        								
	        								ScanCodeResult2Activity.newInstance(getActivity(), result2, code);
	        							}
	        							
	        							@Override
	        							protected void onFail(Exception e) {
	        								mTaskList.remove(this);
	        								
//	        								LayoutError.newInstance(getActivity());								
	        								showAlertDialog();
	        							}
	        						};		

	        						mTaskList.add(scanCodeTask);
	        			    		scanCodeTask.executeOnExecutor(NetworkManager.THREAD_POOL);	
	        					} catch (Exception e) {
	        						// TODO: handle exception
//	        						LayoutError.newInstance(getActivity());
	        						showAlertDialog();
	        					}					
	        				}	
	        				else if(code.toLowerCase().startsWith(prefix))
	        				{
	        					if(code.toLowerCase().startsWith(prefix + "shop/"))
	        					{					
	        						try
	        						{
	        							int shop_id = Integer.parseInt(code.substring(code.lastIndexOf("shop/")+5));
	        							
	        							GetShopDetail2 task = new GetShopDetail2(getActivity(), shop_id) {
	        								@Override
	        								protected void onCompleted(Object result2) {
	        									mTaskList.remove(this);
	        									
	        									JSONObject jShop = (JSONObject) result2;
	        									
	        									Shop shop = new Shop();
	        									shop.idShop	= jShop.optInt("idShop");
	        									shop.shopName	= jShop.optString("shopName");
	        									shop.numOfView = jShop.optString("numOfView");
	        									shop.logo		= jShop.optString("logo");
	        									
	        									ShopDetailActivity.newInstance(getActivity(), shop);
	        									getActivity().finish();
	        								}

	        								@Override
	        								protected void onFail(Exception e) {
	        									mTaskList.remove(this);
	        									
	        									ShopDetailActivity.newInstanceNoReload(getActivity(), new Shop());
	        									getActivity().finish();
	        								}
	        							};
	        							task.setTaskList(mTaskList);
	        							task.executeOnExecutor(NetworkManager.THREAD_POOL);							
	        						}
	        						catch(Exception e)
	        						{
	        							ShopDetailActivity.newInstanceNoReload(getActivity(), new Shop());
	        							getActivity().finish();
	        						}						
	        					}
	        					else if(code.startsWith(prefix + "shops?idShops="))
	        					{
	        						String id_shops = code.substring(code.lastIndexOf("shops?idShops=")+14);
	        						ShopListActivity.newInstance(getActivity(), id_shops, new ArrayList<Shop>(),0);
	        						getActivity().finish();
	        					}
	        					else if(code.toLowerCase().startsWith(prefix + "placelist/"))
	        					{
	        						try {
	        							String id_placelist = code.substring(code.lastIndexOf("placelist/")+10);
	        							ShopListActivity.newInstanceWithPlacelistId(getActivity(), id_placelist, new ArrayList<Shop>());
	        							getActivity().finish();
	        						} catch (Exception e) {
	        							// TODO: handle exception
	        							showAlertDialog();
	        						}						
	        					}	
	        					else if(code.toLowerCase().startsWith(prefix + "qrcode/"))
	        					{
	        						try {
	        							final String qrcode = code.substring(code.lastIndexOf("qrcode/")+7);
	        							
	        							// Call scan code api
	        							ScanCode scanCodeTask = new ScanCode(getActivity(), qrcode) {
	        								@Override
	        								protected void onCompleted(final Object result2) throws Exception {
	        									mTaskList.remove(this);
	        									
	        									objScanCode = result2;
	        									ScanCodeResult2Activity.newInstance(getActivity(), result2, qrcode);
	        								}
	        								
	        								@Override
	        								protected void onFail(Exception e) {
	        									mTaskList.remove(this);
	        									
//	        									LayoutError.newInstance(getActivity());
	        									showAlertDialog();
	        								}
	        							};		

	        							mTaskList.add(scanCodeTask);
	        				    		scanCodeTask.executeOnExecutor(NetworkManager.THREAD_POOL);	
	        						} catch (Exception e) {
	        							// TODO: handle exception
//	        							LayoutError.newInstance(getActivity());
	        							showAlertDialog();
	        						}
	        					}
	        					else
	        					{
	        						try {
	        							// Call scan code api
	        							ScanCode scanCodeTask = new ScanCode(getActivity(), code) {
	        								@Override
	        								protected void onCompleted(final Object result2) throws Exception {
	        									mTaskList.remove(this);
	        									
	        									objScanCode = result2;
	        									
	        									ScanCodeResult2Activity.newInstance(getActivity(), result2, code);
	        								}
	        								
	        								@Override
	        								protected void onFail(Exception e) {
	        									mTaskList.remove(this);
	        									
//	        									LayoutError.newInstance(getActivity());
	        									showAlertDialog();
	        								}
	        							};		

	        							mTaskList.add(scanCodeTask);
	        				    		scanCodeTask.executeOnExecutor(NetworkManager.THREAD_POOL);
	        						} catch (Exception e) {
	        							// TODO: handle exception
//	        							LayoutError.newInstance(getActivity());
	        							showAlertDialog();
	        						}
	        					}
	        				}
	        				else
	        				{
	        					try {
	        						String newQRCode = code;
	        						if(code.toLowerCase().startsWith("www.")) {
//	        							newQRCode = mQRCode.replace("www.", "http://");
	        							newQRCode = "http://" + code;
	        						}
	        						WebActivity.newInstance(getActivity(), newQRCode);
	        						getActivity().finish();
	        					} catch (Exception e) {
	        						// TODO: handle exception
//	        						LayoutError.newInstance(getActivity());
	        						showAlertDialog();
	        					}
	        				}				
	        			}
	        			else
	        			{
	        				try {
	        					// Call scan code api
	        					ScanCode scanCodeTask = new ScanCode(getActivity(), code) {
	        						@Override
	        						protected void onCompleted(final Object result2) throws Exception {
	        							mTaskList.remove(this);
	        							
	        							objScanCode = result2;
	        							
	        							ScanCodeResult2Activity.newInstance(getActivity(), result2, code);
	        						}
	        						
	        						@Override
	        						protected void onFail(Exception e) {
	        							mTaskList.remove(this);
	        							
//	        							LayoutError.newInstance(getActivity());
	        							showAlertDialog();
	        						}
	        					};		

	        					mTaskList.add(scanCodeTask);
	        		    		scanCodeTask.executeOnExecutor(NetworkManager.THREAD_POOL);
	        				} catch (Exception e) {
	        					// TODO: handle exception
//	        					LayoutError.newInstance(getActivity());
	        					showAlertDialog();
	        				}
	        			}
	                }
	            }
	        }
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
		
		if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA) && 
				!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
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
	
	public void showAlertDialog() {
		AlertDialog.Builder builder = new Builder(getActivity());
		builder.setCancelable(false);
		builder.setMessage("Không có dữ liệu!");
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				getActivity().finish();
			}
		});
		builder.create().show();
	}
	
	public void setupScanner() {
        mScanner = new ImageScanner();
        mScanner.setConfig(0, Config.X_DENSITY, 1);
        mScanner.setConfig(0, Config.Y_DENSITY, 1);

        int[] symbols = getActivity().getIntent().getIntArrayExtra("SCAN_MODES");
        if (symbols != null) {
            mScanner.setConfig(Symbol.NONE, Config.ENABLE, 0);
            for (int symbol : symbols) {
                mScanner.setConfig(symbol, Config.ENABLE, 1);
            }
        }
    }
	
	private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if(mCamera != null && mPreviewing) {
                mCamera.autoFocus(autoFocusCB);
            }
        }
    };

    // Mimic continuous auto-focusing
    Camera.AutoFocusCallback autoFocusCB = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            mAutoFocusHandler.postDelayed(doAutoFocus, 1000);
        }
    };
}
