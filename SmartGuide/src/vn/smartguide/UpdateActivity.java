package vn.smartguide;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.analytics.tracking.android.EasyTracker;

public class UpdateActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update);
		
		((Button) findViewById(R.id.btnUpdate))
		.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {

				Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=vn.smartguide");
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
		});
	}
	
	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);  // Add this method.
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);  // Add this method.
	}
}
