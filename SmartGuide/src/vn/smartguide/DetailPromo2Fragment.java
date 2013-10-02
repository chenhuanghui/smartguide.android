package vn.smartguide;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class DetailPromo2Fragment extends DetailPromoFragment {
	
	// Data
	private Listener mListener = new Listener();
	
	///////////////////////////////////////////////////////////////////////////
	// Override methods
	///////////////////////////////////////////////////////////////////////////
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.promotion_type_2, container, false);
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Public methods
	///////////////////////////////////////////////////////////////////////////
	
	public void setListener(Listener listener) {
		if (listener == null)
			listener = new Listener();
		mListener = listener;
	}
	
	public void setData(Shop s) {
		final Shop mShop = s;
		Button rewardBtn = (Button)getView().findViewById(R.id.imageButton1);
		rewardBtn.setText(Integer.toString(((PromotionTypeTwo) s.mPromotion).mMoney / 1000) + ",000 vnđ");
		rewardBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PromotionTypeTwo promotion = (PromotionTypeTwo) mShop.mPromotion;
//				mMainAcitivyListener.getAwardTypeTwo(promotion.mID);
				mListener.onReward2Click(promotion);
			}
		});
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Listener
	///////////////////////////////////////////////////////////////////////////
	
	public static class Listener {
		public void onReward2Click(PromotionTypeTwo promotion) { }
	}
}