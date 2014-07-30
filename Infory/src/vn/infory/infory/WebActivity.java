package vn.infory.infory;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebActivity extends Activity {
	
	private static String sUrl;
	
	private String mUrl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mUrl = sUrl;
		/*Uri uri = Uri.parse(mUrl);
		mUrl = mUrl.toString();*/
		sUrl = null;
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web);
		
		WebView web = (WebView) findViewById(R.id.web);
		
		
		WebView vistaWeb = web;
		web.setWebViewClient(new WebViewClient() {
	        @Override
	        public boolean shouldOverrideUrlLoading(WebView view, String url) {
	        	super.shouldOverrideUrlLoading(view, url);
	        	
	            view.loadUrl(url);
	            return false;
	        }
	    });
		vistaWeb.getSettings().setJavaScriptEnabled(true);
		vistaWeb.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
		web.loadUrl(mUrl);
	}
	
	public static void newInstance(Activity act, String url) {
		sUrl = url;
		
		Intent intent = new Intent(act, WebActivity.class);
		act.startActivity(intent);
		act.overridePendingTransition(R.anim.slide_in_down_detail, R.anim.alpha_out);
	}
	
	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.alpha_in, R.anim.slide_out_up_detail);
	}
}
