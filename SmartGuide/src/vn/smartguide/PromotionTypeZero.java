package vn.smartguide;

/**
 * Created by ChauSang on 7/23/13.
 */
public class PromotionTypeZero extends Promotion {
	public int mSP;

	public PromotionTypeZero(int sp){
		mType = 0;
		mSP = sp;
	}

	@Override
	public int getType(){
		return mType;
	}
}
