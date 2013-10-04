package vn.smartguide;

import java.util.List;

/**
 * Created by ChauSang on 7/23/13.
 */
public class PromotionTypeOne extends Promotion {
	public int mCost; // min
	public int mPperSGP;
	public int mSGP;
	public int mSP;
	public int mMinScore;
	
	public List<Requirement> mRequirement;
	
	public PromotionTypeOne(int cost, int sgp, int sp, int min_score,
			String duration, List<Requirement> requirement){
		mCost = cost;
		mType = 1;
		mSGP = sgp;
		mSP = sp;
		mDuration = duration;
		mRequirement = requirement;
		mMinScore = min_score;
	}

	@Override
	public int getType(){
		return mType;
	}
}
