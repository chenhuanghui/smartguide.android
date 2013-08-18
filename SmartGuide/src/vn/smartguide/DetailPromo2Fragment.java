package vn.smartguide;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class DetailPromo2Fragment extends DetailPromoFragment {
	
	private MainAcitivyListener mMainAcitivyListener;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.promotion_type_2, container, false);
        
        return root;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {

		super.onViewCreated(view, savedInstanceState);
		 mMainAcitivyListener = (MainAcitivyListener) getActivity();
	}
	
	public void setData(Shop s) {
		final PromotionTypeTwo promotion = (PromotionTypeTwo)s.mPromotion;
		Button rewardBtn = (Button)getView().findViewById(R.id.imageButton1);
		rewardBtn.setText(Integer.toString(promotion.mMoney / 1000) + ",000 vnđ");
		rewardBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mMainAcitivyListener.getAwardTypeTwo(promotion.mID);
			}
		});
	}
	
}
