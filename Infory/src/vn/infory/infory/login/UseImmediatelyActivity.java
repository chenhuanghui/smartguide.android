package vn.infory.infory.login;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;

import vn.infory.infory.CyUtils;
import vn.infory.infory.R;
import vn.infory.infory.WebActivity;
import vn.infory.infory.FlashActivity.Listener;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

public class UseImmediatelyActivity extends FragmentActivity {
	
	private static Listener sListener;
	
	private Listener mListener;

	// GUI elements
	@ViewById(id = R.id.btnLogin)	private ImageButton mBtnLogin;
	@ViewById(id = R.id.btnUse)		private ImageButton mBtnUse;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		mListener = new Listener() {
			private Listener inerListener;
			public Listener init(Listener listener) {
				inerListener = listener;
				return this;
			}
			
			@Override
			public void onSuccess() {	
				finish();
				inerListener.onSuccess();
			}
			
			@Override
			public void onFail(Exception e) {
				inerListener.onFail(e);
			}
		}.init(sListener);
		sListener = null;
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_use_immediately);

		try {
			AndroidAnnotationParser.parse(this, findViewById(android.R.id.content));
		} catch (Exception e) {	
			e.printStackTrace();
			finish();
			return;
		}
		
		CyUtils.setHoverEffect(mBtnLogin, false);
		CyUtils.setHoverEffect(mBtnUse, false);
	}

	public static void newInstance(Activity act, Listener listener) {
		sListener = listener;
		
		Intent intent = new Intent(act, UseImmediatelyActivity.class);
		act.startActivity(intent);
		act.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}

	@Override
	public void finish() {
		super.finish();
	}
	
	@Override
	public void onBackPressed() {
	}
	
	@Click(id = R.id.btnLogin)
	private void onLoginClick(View v) {
		InforyLoginActivity.newInstance(this, mListener);
		overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top);
	}
	
	@Click(id = R.id.btnUse)
	private void onUseClick(View v) {	
		Editor e = PreferenceManager.getDefaultSharedPreferences(this).edit();
		e.putString("use_immediately_activity", "1");
		e.commit();
		
		finish();
		mListener.onSuccess();
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}
	
	@Click(id = R.id.txtPolicy)
	private void onPolicyClick(View v) {
		WebActivity.newInstance(this, "http://infory.vn/dieu-khoan-nguoi-dung.html");
	}
}
