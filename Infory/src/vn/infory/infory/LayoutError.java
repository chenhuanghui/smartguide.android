package vn.infory.infory;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;

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
import android.view.View;
import android.widget.Toast;

public class LayoutError extends Activity{

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
	}
	
	@Click(id = R.id.layoutErrorBtnRetry)
	private void onRetryClick(View v) {
		Settings.checkLogin(this, new Runnable() {
			@Override
			public void run() {
				ScanCodeActivity.newInstance(LayoutError.this);
				finish();
			}
		}, true);
	}

	public static void newInstance(Activity act) {		
		Intent intent = new Intent(act, LayoutError.class);
		act.startActivity(intent);
		act.finish();
	}
}
