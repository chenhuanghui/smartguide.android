package vn.smartguide;

import com.google.analytics.tracking.android.EasyTracker;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Menu;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ReviewActivity extends Activity {

	EditText mReviewText;
	TextView mName;
	Button mReviewBtn;
	Activity mActivity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Test
		setContentView(R.layout.activity_review);
		mName = (TextView)findViewById(R.id.textView1);
		if(GlobalVariable.nameFace == "" || GlobalVariable.nameFace.compareTo("null") == 0)
			mName.setText("Anomynous User");
		else
			mName.setText(GlobalVariable.nameFace);

		mReviewText = (EditText)findViewById(R.id.editText1);
		mReviewBtn = (Button)findViewById(R.id.button1);
		mActivity = this;

		mReviewBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {			
				AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);

				final String review = mReviewText.getText().toString();
				
				if (review == "" || review.length() == 0){
					builder.setTitle("Thong bao");
					builder.setMessage("Vui lÃ²ng nháº­p nháº­n xÃ©t");
					builder.setCancelable(true);

					builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
								
						}
					});

				}else{
					builder.setMessage("Cáº£m Æ¡n báº¡n vÃ¬ lá»�i nháº­n xÃ©t. Báº¥m OK Ä‘á»ƒ trá»Ÿ láº¡i chÆ°Æ¡ng trÃ¬nh");
					builder.setCancelable(true);

					builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(mReviewText.getWindowToken(), 0);
							GlobalVariable.isNeedPostReview = true;
							GlobalVariable.reviewString = review;
							finish();	
						}
					});
				}
				builder.show();
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
