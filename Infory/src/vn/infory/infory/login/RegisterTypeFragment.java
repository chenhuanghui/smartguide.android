package vn.infory.infory.login;

import java.util.Arrays;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;
import com.facebook.SessionLoginBehavior;
import com.facebook.widget.LoginButton;

import vn.infory.infory.CyImageLoader;
import vn.infory.infory.CyUtils;
import vn.infory.infory.FontsCollection;
import vn.infory.infory.R;
import vn.infory.infory.data.Settings;
import vn.infory.infory.login.InforyLoginActivity.BackListener;
import vn.infory.infory.network.CyAsyncTask;
import vn.infory.infory.SGSideMenu;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView.BufferType;
import android.widget.Toast;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;

public class RegisterTypeFragment extends Fragment implements BackListener {
	
	// Data
	private Listener mListener;
	
	private Activity mCt;
	
	public Settings s = Settings.instance();
	
	// GUI
	@ViewById(id = R.id.btnContinue)			private TextView mBtnContinue;
	@ViewById(id = R.id.imgAva)					private ImageView mImgAva;
	@ViewById(id = R.id.frameAva)				private FrameLayout mFrameAva;
	@ViewById(id = R.id.txtOr)					private TextView mTxtOr;
	@ViewById(id = R.id.btnFacebook)			private ImageButton mBtnFacebook;
	@ViewById(id = R.id.btnGooglePlus)			private ImageButton mBtnGooglePlus;
	@ViewById(id = R.id.fbBtn) 					private LoginButton mFacebookButton;

	
	//Chuyển frame layout trong fragment
	//http://stackoverflow.com/questions/5953502/how-do-i-change-the-view-inside-a-fragment
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View myInflatedView = inflater.inflate(R.layout.login_register_type, container, false);
		
		return myInflatedView;
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
		CyUtils.setHoverEffect(mBtnFacebook, false);
		CyUtils.setHoverEffect(mBtnGooglePlus, false);
		
		mFacebookButton.setReadPermissions(Arrays.asList("user_birthday"));
		mFacebookButton.setLoginBehavior(SessionLoginBehavior.SUPPRESS_SSO);
	}
	
	public void setListener(Listener listener) {
		mListener = listener;
	}
	
	/*@Click(id = R.id.btnBack)
	private void onBackClick(View v) {
		mListener.onBackClick();
	}*/
	
	/*@Click(id = R.id.txtRegisterNew)
	private void onRegisterNewClick(View v) {
		mListener.onRegisterClick();
	}*/
	
	@Click(id = R.id.btnFacebook)
	public void onFacebookClick(View v) {
		mListener.onFacebookClick();
		mFacebookButton.performClick();
	}
	
	@Click(id = R.id.btnGooglePlus)
	private void onGooglePlusClick(View v) {
		mListener.onGooglePlusClick();
	}
	
	@Click(id = R.id.btnContinue)
	private void onButtonContinueClick(View v) {
		// TODO Auto-generated method stub
		mListener.onButtonContinueClick();
	}
	
	@Click(id = R.id.frameAva)
	private void onFrameAvatarContinueClick(View v) {
		// TODO Auto-generated method stub
		mListener.onButtonContinueClick();
	}

	@Override
	public void onBackPress() {
//		onBackClick(null);
	}
	
	
	
	public void onFinishLogin()
	{		
		mCt = new Activity();
				
		if(s.name.equals(""))
		{				
			mBtnContinue.setVisibility(View.GONE);
			mImgAva.setVisibility(View.GONE);
			mFrameAva.setVisibility(View.GONE);
			mTxtOr.setVisibility(View.GONE);
		}
		else
		{			
			FontsCollection.setFontForTextView(mTxtOr, "sfufuturabook");
			mBtnContinue.setVisibility(View.VISIBLE);
			mImgAva.setVisibility(View.VISIBLE);
			mFrameAva.setVisibility(View.VISIBLE);
			mTxtOr.setVisibility(View.VISIBLE);
			
			Spannable WordToSpan = new SpannableString("Sử dụng "); 
			WordToSpan.setSpan(new ForegroundColorSpan(Color.WHITE), 0, WordToSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			
			mBtnContinue.setText(WordToSpan);
			
			WordToSpan = new SpannableString(s.name); 
			WordToSpan.setSpan(new ForegroundColorSpan(Color.CYAN), 0, WordToSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			WordToSpan.setSpan(new StyleSpan(Typeface.BOLD), 0, WordToSpan.length(), 0);
			mBtnContinue.append(WordToSpan);
			
			WordToSpan = new SpannableString(" để tiếp tục "); 
			WordToSpan.setSpan(new ForegroundColorSpan(Color.WHITE), 0, WordToSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			mBtnContinue.append(WordToSpan);
			
			FontsCollection.setFontForTextView(mBtnContinue, "sfufuturabook");
			
			CyImageLoader.instance().loadImage(s.avatar, new CyImageLoader.Listener() {
				@Override
				public void loadFinish(int from, Bitmap image, String url, CyAsyncTask task) {
					mImgAva.setImageBitmap(SGSideMenu.getCroppedBitmap(image));
				}
			}, new Point(), mCt);
		}
	}
	
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		
		String last_activity = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("last_activity", "");
//		Toast.makeText(getActivity().getApplicationContext(), "Register type fragment: " +  last_activity, Toast.LENGTH_LONG).show();
		if(last_activity.equals("HomeFragment"))
		{
			Settings s = Settings.instance();
			mCt = new Activity();
					
			if(s.name.equals(""))
			{	
				mBtnContinue.setVisibility(View.GONE);
				mImgAva.setVisibility(View.GONE);
				mFrameAva.setVisibility(View.GONE);
				mTxtOr.setVisibility(View.GONE);
			}
			else
			{			
				mBtnContinue.setVisibility(View.VISIBLE);
				mImgAva.setVisibility(View.VISIBLE);
				mFrameAva.setVisibility(View.VISIBLE);
				mTxtOr.setVisibility(View.VISIBLE);
				
				Spannable WordToSpan = new SpannableString("Sử dụng "); 
				WordToSpan.setSpan(new ForegroundColorSpan(Color.WHITE), 0, WordToSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				
				mBtnContinue.setText(WordToSpan);
				
				WordToSpan = new SpannableString(s.name); 
				WordToSpan.setSpan(new ForegroundColorSpan(Color.CYAN), 0, WordToSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				WordToSpan.setSpan(new StyleSpan(Typeface.BOLD), 0, WordToSpan.length(), 0);
				mBtnContinue.append(WordToSpan);
				
				WordToSpan = new SpannableString(" để tiếp tục "); 
				WordToSpan.setSpan(new ForegroundColorSpan(Color.WHITE), 0, WordToSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				mBtnContinue.append(WordToSpan);
				
				FontsCollection.setFontForTextView(mBtnContinue, "sfufuturabook");
				
				
				
				CyImageLoader.instance().loadImage(s.avatar, new CyImageLoader.Listener() {
					@Override
					public void loadFinish(int from, Bitmap image, String url, CyAsyncTask task) {
						mImgAva.setImageBitmap(SGSideMenu.getCroppedBitmap(image));
					}
				}, new Point(), mCt);
			}
		}
				
		Editor e = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
		e.putString("last_activity", getClass().getSimpleName());
		e.commit();
		
		super.onResume();
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Editor e = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
		e.putString("last_activity", getClass().getSimpleName());
		e.commit();
		
		super.onDestroy();
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		Editor e = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
		e.putString("last_activity", getClass().getSimpleName());
		e.commit();
		
		super.onPause();
	}
	
	
	
	public void startRegisterTypeFragmentActivity() 
	{
		// TODO Auto-generated method stub
		Intent i = new Intent(getActivity(),RegisterTypeFragment.class);
		startActivity(i);
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// Listener
	///////////////////////////////////////////////////////////////////////////	

	public interface Listener {
		public void onBackClick();
		public void onRegisterClick();
		public void onGooglePlusClick();
		public void onFacebookClick();
		public void onButtonContinueClick();
	}

}
