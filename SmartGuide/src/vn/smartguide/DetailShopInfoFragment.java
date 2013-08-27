package vn.smartguide;

import java.util.TimerTask;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class DetailShopInfoFragment extends Fragment {
	ImageView mCallBtn;
	TextView txtDescription;
	TextView txtAddress;
	TextView txtPhone;
	TextView txtWeb;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.detail_shopinfo, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        txtDescription = (TextView) getView().findViewById(R.id.txtDes);
    	txtAddress = (TextView) getView().findViewById(R.id.txtAddress);
    	txtPhone = (TextView) getView().findViewById(R.id.txtPhone);
    	txtWeb = (TextView) getView().findViewById(R.id.txtWeb);
    	mCallBtn = (ImageView)getView().findViewById(R.id.callBtn);
        // Instance of ImageAdapter Class
    }
    
    public void setData(final Shop s) {
    	
    	txtDescription.setText(s.mContent);
    	txtAddress.setText(s.mAddress);
    	txtPhone.setText(s.mTel);
    	txtWeb.setText("");
    	
    	if (s.mTel == "" || s.mTel.compareTo("null") == 0){
    		mCallBtn.setVisibility(View.INVISIBLE);
    		return;
    	}
    	
    	mCallBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent call = new Intent(Intent.ACTION_CALL);
				call.setData(Uri.parse("tel:" + s.mTel));
		        getActivity().startActivity(call);				
			}
		});
    }
}
