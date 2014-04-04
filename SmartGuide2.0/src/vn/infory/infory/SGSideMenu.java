package vn.infory.infory;

import vn.infory.infory.data.Settings;
import vn.infory.infory.network.CyAsyncTask;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class SGSideMenu {
	
	// Data
	private Activity mCt;
	private SlidingMenu mMenu;
	private Listener mListener = new Listener();
	
	// GUI elements
	@ViewById(id = R.id.txtUserName)	private TextView mTxtUserName;
	@ViewById(id = R.id.imgAva)			private ImageView mImgAva;
	
	@ViewById(id = R.id.btnExplore) 	private Button mBtnExplorer;
	@ViewById(id = R.id.btnPromotion) 	private Button mBtnPromotion;
	@ViewById(id = R.id.btnStore) 		private Button mBtnStore;
	@ViewById(id = R.id.btnTutorial)	private Button mBtnTutorial;
	@ViewById(id = R.id.btnUpdate)		private Button mBtnUpdate;
	
	
	public SGSideMenu(Activity context) {
		mCt = context;
		
		mMenu = new SlidingMenu(context);
		mMenu.setMode(SlidingMenu.LEFT);
		mMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
		mMenu.setShadowWidth(8);
		mMenu.setBehindOffset(64);
		mMenu.setFadeDegree(0.35f);
		mMenu.attachToActivity(context, SlidingMenu.SLIDING_CONTENT);
		mMenu.setMenu(R.layout.side_menu);
		
		try {
			AndroidAnnotationParser.parse(this, mMenu.getMenu());
		} catch (Exception e) { 
			mCt.finish();
		}
	}
	
	public void setListener(Listener listener) {
		if (listener == null)
			listener = new Listener();
		mListener = listener;
	}
	
	public void toggle() {
		mMenu.toggle();	
	}
	
	public void update() {
		Settings s = Settings.instance();
		
		mTxtUserName.setText(s.name);
		CyImageLoader.instance().loadImage(s.avatar, new CyImageLoader.Listener() {
			@Override
			public void startLoad(int from) {
				switch (from) {
				case CyImageLoader.FROM_DISK:
				case CyImageLoader.FROM_NETWORK:
					mImgAva.setImageBitmap(null);
					break;
				}
			}
			
			@Override
			public void loadFinish(int from, Bitmap image, String url, CyAsyncTask task) {
				Bitmap ava = getCroppedBitmap(image);
				mImgAva.setImageBitmap(ava);
			}
		}, new Point(), mCt);
	}
	
	@Click(id = R.id.btnExplore)
	private void onExploreClick(View v) {
		mListener.onExploreClick();
	}
	
	@Click(id = R.id.btnPromotion)
	private void onPromoClick(View v) {
		mListener.onPromotionClick();
	}
	
	@Click(id = R.id.btnStore)
	private void onStoreClick(View v) {
		mListener.onStoreClick();
	}
	
	@Click(id = R.id.btnTutorial)
	private void onTutorilClick(View v) {
		mListener.onTutorialClick();
	}
	
	@Click(id = R.id.btnSettings)
	private void onSettingsClick(View v) {
		Settings.checkLogin(mCt, new Runnable() {
			@Override
			public void run() {
				UserSettingActivity.newInstance(mCt);
			}
		}, true);
	}
	
	public static Bitmap getCroppedBitmap(Bitmap bitmap) {
		int edge = Math.min(bitmap.getWidth(), bitmap.getHeight());
	    Bitmap output = Bitmap.createBitmap(edge, edge, Config.ARGB_8888);
	    Canvas canvas = new Canvas(output);

	    final int color = 0xff424242;
	    final Paint paint = new Paint();

	    paint.setAntiAlias(true);
	    canvas.drawARGB(0, 0, 0, 0);
	    paint.setColor(color);
	    // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
	    canvas.drawCircle(edge / 2, edge / 2, edge / 2, paint);
	    paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
	    Rect dstRect = new Rect(0, 0, edge, edge);
	    Rect srcRect = new Rect(
	    		(bitmap.getWidth() - edge) / 2,
	    		(bitmap.getHeight() - edge) / 2,
	    		(bitmap.getWidth() - edge) / 2 + edge, 
	    		(bitmap.getHeight() - edge) / 2 + edge);
	    canvas.drawBitmap(bitmap, srcRect, dstRect, paint);
	    //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
	    //return _bmp;
	    return output;
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Listener
	///////////////////////////////////////////////////////////////////////////
	
	public static class Listener {
		public void onExploreClick() {}
		public void onPromotionClick() {}
		public void onStoreClick() {}
		public void onTutorialClick() {}
		public void onUpdateClick() {}
		public void onAvaClick() {}
	}
}