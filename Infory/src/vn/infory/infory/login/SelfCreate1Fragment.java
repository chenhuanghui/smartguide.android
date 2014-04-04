package vn.infory.infory.login;

import vn.infory.infory.CyImageLoader;
import vn.infory.infory.CyUtils;
import vn.infory.infory.FontsCollection;
import vn.infory.infory.R;
import vn.infory.infory.network.CyAsyncTask;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;

public class SelfCreate1Fragment extends Fragment {
	
	// Data
	private Listener mListener;
	private boolean mShowUsernameNoti = true;
	private String mAvaUrl;
	private String mAvaFilePath;
	
	// GUI elements
	@ViewById(id = R.id.btnNext)		private ImageButton mBtnNext;
	@ViewById(id = R.id.imgAva)			private ImageView mImgAva;
	@ViewById(id = R.id.edtUserName)	public  EditText mEdtUsername;
	@ViewById(id = R.id.txtAvaNoti)		private TextView mTxtAvaNoti;
	@ViewById(id = R.id.txtUserNameNoti)private TextView mTxtUsernameNoti;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.login_self_create_1, container, false);
	}
	
	public void setListener(Listener listener) {
		mListener = listener;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		try {
			AndroidAnnotationParser.parse(this, view);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		FontsCollection.setFont(view);
		CyUtils.setHoverEffect(mBtnNext, false);
		
		mEdtUsername.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus && mShowUsernameNoti)
					toggleUsernameNoti(false);
			}
		});
		
		mEdtUsername.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mShowUsernameNoti)
					toggleUsernameNoti(false);
			}
		});
		
		mEdtUsername.addTextChangedListener(new TextWatcher() {
			
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				if (mShowUsernameNoti)
					toggleUsernameNoti(false);
			}
		});
	}
	
	@Click(id = R.id.imgAva)
	private void onAvaClick(View v) {
		AvaDialogActivity.newInstance(getActivity(), new AvaDialogActivity.Listener() {
			@Override
			public void onAvaSelect(String url) {
				CyImageLoader.instance().loadImage(url, new CyImageLoader.Listener() {
					@Override
					public void loadFinish(int from, Bitmap image, String url, CyAsyncTask task) {
						mImgAva.setBackgroundDrawable(new BitmapDrawable(image));
					}
				}, new Point(), getActivity());
				mAvaUrl = url;
				mAvaFilePath = null;
				toggleAvaNoti(false);
			}
			
			@Override
			public void onAvaSelectFile(String path, Bitmap ava) {
				mImgAva.setBackgroundDrawable(new BitmapDrawable(ava));
				mAvaFilePath = path;
				mAvaUrl = null;
				toggleAvaNoti(false);
			}
		});
	}
	
	@Click(id = R.id.btnNext)
	private void onNextClick(View v) {
		if (checkValidInfo())
			mListener.onNextClick();
	}

	public boolean checkValidInfo() {
		
		boolean canGoNext = true;
		if (mAvaUrl == null && mAvaFilePath == null) {
			toggleAvaNoti(true);
			canGoNext = false;
		}
		
		if (mEdtUsername.getText().toString().trim().length() == 0) {
			toggleUsernameNoti(true);
			canGoNext = false;
		}
		
		return canGoNext;
	}
	
	public Object[] getResult() {
		
		Object[] result = new Object[3];
		result[0] = mEdtUsername.getText().toString().trim();
		result[1] = mAvaUrl;
		result[2] = mAvaFilePath;
		return result;
	}
	
	private void toggleAvaNoti(boolean show) {
		float startAlpha = show ? 0 : 1;
		AlphaAnimation animation = new AlphaAnimation(startAlpha, 1 - startAlpha);
		animation.setFillAfter(true);
		animation.setDuration(300);
		mTxtAvaNoti.startAnimation(animation);
	}
	
	private void toggleUsernameNoti(boolean show) {
		float startAlpha = show ? 0 : 1;
		AlphaAnimation animation = new AlphaAnimation(startAlpha, 1 - startAlpha);
		animation.setDuration(300);
		animation.setFillAfter(true);
		mTxtUsernameNoti.startAnimation(animation);
		mShowUsernameNoti = show;
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Listener
	///////////////////////////////////////////////////////////////////////////
	
	public static class Listener {
		public void onNextClick() {}
	}
}
