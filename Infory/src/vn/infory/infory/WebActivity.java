package vn.infory.infory;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

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
		
		web.getSettings().setJavaScriptEnabled(true);
		web.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
		web.getSettings().setUseWideViewPort(true); 
		Toast.makeText(getApplicationContext(), "mUrl: "+mUrl, Toast.LENGTH_LONG).show();
		web.setWebViewClient(new WebViewClient() {
	        @Override
	        public boolean shouldOverrideUrlLoading(WebView view, String url) {		
	        	Toast.makeText(getApplicationContext(), "Override: "+url, Toast.LENGTH_LONG).show();
	            view.loadUrl(url);
	            return false;
	        }
	    });
		
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
