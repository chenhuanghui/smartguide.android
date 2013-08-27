package vn.smartguide;

import java.util.TimerTask;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

public class DetailShopInfoFragment extends Fragment {
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.detail_shopinfo, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        
        // Instance of ImageAdapter Class
    }
    
    public void setData(Shop s) {
    	
    	TextView txtDescription = (TextView) getView().findViewById(R.id.txtDes);
    	TextView txtAddress = (TextView) getView().findViewById(R.id.txtAddress);
    	TextView txtPhone = (TextView) getView().findViewById(R.id.txtPhone);
    	TextView txtWeb = (TextView) getView().findViewById(R.id.txtWeb);
    	
    	txtDescription.setText(s.mContent);
    	txtAddress.setText(s.mAddress);
    	txtPhone.setText(s.mTel);
    	txtWeb.setText("");
    }
}
