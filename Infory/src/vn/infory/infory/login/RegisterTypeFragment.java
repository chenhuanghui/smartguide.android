package vn.infory.infory.login;

import java.util.Arrays;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;
import com.facebook.SessionLoginBehavior;
import com.facebook.widget.LoginButton;

import vn.infory.infory.CyUtils;
import vn.infory.infory.FontsCollection;
import vn.infory.infory.R;
import vn.infory.infory.login.LoginActivity.BackListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class RegisterTypeFragment extends Fragment implements BackListener {
	
	// Data
	private Listener mListener;
	
	// GUI
	@ViewById(id = R.id.btnFacebook)	private ImageButton mBtnFacebook;
	@ViewById(id = R.id.btnGooglePlus)	private ImageButton mBtnGooglePlus;
	@ViewById(id = R.id.fbBtn) 			private LoginButton mFacebookButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.login_register_type, container, false);
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
	
	@Click(id = R.id.btnBack)
	private void onBackClick(View v) {
		mListener.onBackClick();
	}
	
	@Click(id = R.id.txtRegisterNew)
	private void onRegisterNewClick(View v) {
		mListener.onRegisterClick();
	}
	
	@Click(id = R.id.btnFacebook)
	public void onFacebookClick(View v) {
		mListener.onFacebookClick();
		mFacebookButton.performClick();
	}
	
	@Click(id = R.id.btnGooglePlus)
	private void onGooglePlusClick(View v) {
		mListener.onGooglePlusClick();
	}

	@Override
	public void onBackPress() {
		onBackClick(null);
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Listener
	///////////////////////////////////////////////////////////////////////////
	
	public interface Listener {
		public void onBackClick();
		public void onRegisterClick();
		public void onGooglePlusClick();
		public void onFacebookClick();
	}

}
