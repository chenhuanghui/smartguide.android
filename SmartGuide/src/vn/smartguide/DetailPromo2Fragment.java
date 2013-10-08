package vn.smartguide;

import java.util.ArrayList;
import java.util.List;

import vn.smartguide.PromotionTypeTwo.Voucher;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

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
		rewardBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PromotionTypeTwo promotion = (PromotionTypeTwo) mShop.mPromotion;
//				mMainAcitivyListener.getAwardTypeTwo(promotion.mID);
				mListener.onReward2Click(promotion);
			}
		});
		
		((ImageButton) getView().findViewById(R.id.btnRewardList))
		.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), 
						android.R.style.Theme_Translucent_NoTitleBar);
			    LayoutInflater inflater = getActivity().getLayoutInflater();
			    View v = inflater.inflate(R.layout.detail_promo2_list, null);
			    ListView lst = (ListView) v.findViewById(R.id.lst);
			    if (mShop.mPromotion.mType == 2) {
				    PromotionTypeTwo pro2 = (PromotionTypeTwo) mShop.mPromotion;
				    lst.setAdapter(new VoucherAdapter(pro2.mVoucherList));
			    }
			    builder.setView(v);
//			    builder.create().show();
			    
			    Dialog dialog = new Dialog(getActivity(), android.R.style.Theme_Translucent_NoTitleBar);
//			    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

			    //you can move the dialog, so that is not centered
			     dialog.getWindow().getAttributes().y = 50; //50 should be based on density

			    dialog.setContentView(v);
			    dialog.setCancelable(true);
			    //dialog.setOnCancelListener(cancelListener);
			    dialog.show();
			}
		});
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Adapter
	///////////////////////////////////////////////////////////////////////////
	
	private class VoucherAdapter extends ArrayAdapter<Voucher> {

		public VoucherAdapter(List<Voucher> itemList) {
			super(getActivity(), R.layout.detail_promo2_list_item, R.id.txtDes, itemList);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = super.getView(position, convertView, parent);
			TextView txtP = (TextView)v.findViewById(R.id.txtP);
			
			SpannableString span = new SpannableString("Tích lũy " + getItem(position).P + " P trên một lượt quét thẻ");
			span.setSpan(new StyleSpan(Typeface.BOLD), 
					("Tích lũy ").length(),
					("Tích lũy " + getItem(position).P + " P").length(), 0);
			span.setSpan(new ForegroundColorSpan(0xFFC95436),
					("Tích lũy ").length(),
					("Tích lũy " + getItem(position).P + " P").length(), 0);
			txtP.setText(span);
			return v;
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Listener
	///////////////////////////////////////////////////////////////////////////
	
	public static class Listener {
		public void onReward2Click(PromotionTypeTwo promotion) { }
	}
}