package vn.smartguide;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Test
		setContentView(R.layout.activity_review);
		mName = (TextView)findViewById(R.id.textView1);
		mName.setText(GlobalVariable.nameFace);
		mReviewText = (EditText)findViewById(R.id.editText1);
		mReviewBtn = (Button)findViewById(R.id.button1);
		
		mReviewText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
            	mReviewText.post(new Runnable() {
                    @Override
                    public void run() {
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(mReviewText, InputMethodManager.SHOW_IMPLICIT);
                    }
                });
            }
        });
		mReviewText.requestFocus();
		mReviewBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
}
