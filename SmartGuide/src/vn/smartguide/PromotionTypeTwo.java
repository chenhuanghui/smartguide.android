package vn.smartguide;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ChauSang on 7/23/13.
 */
public class PromotionTypeTwo extends Promotion {
	
	public String mMoney;
	public List<Voucher> mVoucherList = new ArrayList<Voucher>();
	
	public static class Voucher {
		public String description;
		public int P;
		public String numberVoucher;
		
		@Override
		public String toString() {
			return description;
		}
	}
	
	public PromotionTypeTwo() {
		mType = 2;
	}
	
	public PromotionTypeTwo(String money){
		mType = 2;
		mMoney = money;
	}

	@Override
	public int getType(){
		return mType;
	}
	
	public PromotionTypeTwo parse(JSONObject jPromotion) throws JSONException {
		try{
			mMoney = jPromotion.getString("str_money");
		}catch(Exception ex){
			mMoney = jPromotion.getString("money");
		}
		mDuration = jPromotion.getString("duration");
		JSONArray jVoucherArr = jPromotion.getJSONArray("list_voucher");
		for (int i = 0; i < jVoucherArr.length(); i++) {
			JSONObject jVoucher = jVoucherArr.getJSONObject(i);
			Voucher v = new Voucher();
			v.description = jVoucher.getString("description");
			v.P = jVoucher.getInt("P");
			v.numberVoucher = jVoucher.getString("numberVoucher"); 
			mVoucherList.add(v);
		}
		return this;
	}
}
