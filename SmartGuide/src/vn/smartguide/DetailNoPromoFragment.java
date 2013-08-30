package vn.smartguide;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DetailNoPromoFragment extends DetailPromoFragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.detail_no_promo, container, false);
        
        return root;
	}
}
