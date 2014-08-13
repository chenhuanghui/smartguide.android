package vn.infory.infory;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;

import vn.infory.infory.FlashActivity.Listener;
import vn.infory.infory.data.Settings;
import vn.infory.infory.login.InforyLoginActivity;
import vn.infory.infory.login.UseImmediatelyActivity;
import vn.infory.infory.network.CyAsyncTask;
import vn.infory.infory.network.NetworkManager;
import vn.infory.infory.network.UpdateDeviceInfo;
import vn.infory.infory.scancode.ScanCodeActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class LayoutError extends Activity{

	@ViewById(id = R.id.linearLayoutErrorContent)	private LinearLayout mLLErrorContent;
	@ViewById(id = R.id.txtErrorMessage)			private TextView mTxtErrorMessage;
	@ViewById(id = R.id.imgError)					private ImageView mImgError;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_error);	
		
		try {
			AndroidAnnotationParser.parse(this, findViewById(android.R.id.content));
		} catch (Exception e) {
			e.printStackTrace();
			finish();
			return;
		}
		
		
		
		mLLErrorContent.post(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mImgError.setPadding(0, (int)mLLErrorContent.getHeight()/4, 0, 0);				
			}
		});
		FontsCollection.setFontForTextView(mTxtErrorMessage, "sfufuturaheavy");
	}
	
	
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		finish();
	}



	@Click(id = R.id.layoutErrorBtnBack)
	private void onBackClick(View v) {
		finish();
	}

	public static void newInstance(Activity act) {		
		Intent intent = new Intent(act, LayoutError.class);
		act.startActivity(intent);
		act.finish();
	}
}
