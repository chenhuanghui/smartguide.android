package vn.smartguide;

/**
 * Created by ChauSang on 7/23/13.
 */
public class PromotionTypeTwo extends Promotion {
	public int mMoney;
	public int mID;
	public PromotionTypeTwo(int money, int id){
		mType = 2;
		mMoney = money;
		mID = id;
	}

	@Override
	public int getType(){
		return mType;
	}
}
