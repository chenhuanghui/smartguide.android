package vn.smartguide;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import com.google.analytics.tracking.android.EasyTracker;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher.ViewFactory;

public class ReviewActivity extends Activity {

	EditText mReviewText;
	TextSwitcher mTextSwitcher;

	TextView mName;
	Button mReviewBtn;
	Activity mActivity;
	List<Review> mReviews;
	ChangeReview mChangeReview = null;
	boolean mFirstTimeClick = true;
	boolean mIsReviewing = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Test
		setContentView(R.layout.activity_review);

		mTextSwitcher = (TextSwitcher)findViewById(R.id.reviewSwitcher);
		mTextSwitcher.setInAnimation(getBaseContext(), R.anim.anim_in);
		mTextSwitcher.setOutAnimation(getBaseContext(), R.anim.anim_out);

		mTextSwitcher.setFactory(new ViewFactory() {

			public View makeView() {
				TextView myText = new TextView(ReviewActivity.this);
				myText.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
				myText.setTextSize(15);
				return myText;
			}
		});

		mName = (TextView)findViewById(R.id.textView1);

		mReviewText = (EditText)findViewById(R.id.editText1);
		mReviewText.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				mIsReviewing = hasFocus;
			}
		});
		
		mReviewBtn = (Button)findViewById(R.id.button1);
		mActivity = this;

//		mReviewText.setOnFocusChangeListener(new OnFocusChangeListener() {
//			@Override
//			public void onFocusChange(View v, boolean hasFocus) {
//				mReviewBtn.post(new Runnable() {
//					@Override
//					public void run() {
//						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//						imm.showSoftInput(mReviewText, InputMethodManager.SHOW_IMPLICIT);
//					}
//				});
//			}
//		});

		mReviewBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mFirstTimeClick) {

					if(GlobalVariable.nameFace == "" || GlobalVariable.nameFace.compareTo("null") == 0)
						mName.setText("Anomynous User");
					else
						mName.setText(GlobalVariable.nameFace);

					stopReview();
					mReviewText.setVisibility(View.VISIBLE);
					mTextSwitcher.setVisibility(View.INVISIBLE);
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.showSoftInput(mReviewText, InputMethodManager.SHOW_IMPLICIT);
					mReviewText.setCursorVisible(true);
					mReviewText.requestFocus();
					mFirstTimeClick = false;
					mReviewBtn.setText("Gửi");
					return;
				}

				AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);

				final String review = mReviewText.getText().toString();

				if (review.length() == 0){
					builder.setTitle("Thông báo");
					builder.setMessage("Vui lòng nhập nhận xét của bạn");
					builder.setCancelable(true);

					builder.setPositiveButton("OK", null);

				}else{
					builder.setMessage("Cám ơn bạn vì lời nhận xét. Nhấn OK để trở lại chương trình");
					builder.setCancelable(true);

					builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(mReviewText.getWindowToken(), 0);
							GlobalVariable.isNeedPostReview = true;
							GlobalVariable.reviewString = review;
							mReviewBtn.setText("Đánh giá");
							finish();	
						}
					});
				}
				builder.show();
			}
		});

		new GetFeedback().execute();
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

	public class GetFeedback extends AsyncTask<Void, Void, Boolean> {
		String mJson = "";

		@Override
		protected Boolean doInBackground(Void... params) {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();

			mJson = NetworkManger.post(APILinkMaker.mGetFeedback(), pairs);
			try {
			} catch (Exception e) {}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean k){
			try{
				mReviews = Review.getList(new JSONObject(mJson).getJSONArray("content"));
			}catch(Exception ex){

			}
			
			if (mIsReviewing == false)
				startReview();
		}

		@Override
		protected void onPreExecute(){}
	}

	int index = 0;
	class ChangeReview extends TimerTask {

		@Override
		public void run() {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					try {
						if (mReviews.size() == 0)
							return;

						index = (index + 1) % mReviews.size();
						mTextSwitcher.setText('"' + mReviews.get(index).mFeedback + '"');
						String name = mReviews.get(index).mName;
						if (name.compareTo("") == 0 || name.length() == 0 || name.compareTo(" ") == 0)
							mName.setText("Anomynous User");
						else
							mName.setText(name);
					} catch(Exception ex) {

					}
				}
			});
		}
	};

	void stopReview(){
		if (mChangeReview == null)
			return;
		mChangeReview.cancel();
	}

	void startReview(){
		mChangeReview = new ChangeReview();
		new Timer().schedule(mChangeReview, 0, 2500);
	}
}
