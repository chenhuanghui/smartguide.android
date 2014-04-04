package vn.infory.infory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class TutorialActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tutorial);
		
		WebView web = (WebView) findViewById(R.id.web);
		
		
		WebView vistaWeb = web;
		web.setWebViewClient(new WebViewClient() {
	        @Override
	        public boolean shouldOverrideUrlLoading(WebView view, String url) {
	            view.loadUrl(url);
	            return false;
	        }
	    });
		vistaWeb.getSettings().setJavaScriptEnabled(true);
		vistaWeb.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
		web.loadUrl("http://infory.vn/mobile/guide/");
	}
	
	public static void newInstance(Activity act) {
		Intent intent = new Intent(act, TutorialActivity.class);
		act.startActivity(intent);
		act.overridePendingTransition(R.anim.slide_in_down_detail, R.anim.alpha_out);
		
		
	}
	
	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.alpha_in, R.anim.slide_out_up_detail);
	}
}
