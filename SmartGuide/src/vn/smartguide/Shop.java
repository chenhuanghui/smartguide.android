package vn.smartguide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ChauSang on 7/23/13.
 */
public class Shop {
	public int mID;
	public String mName;
	public String mLogo;
	public String mAddress;
	public String mCover;
	public float mLat;
	public float mLng;
	public float mDistance;
	
	public int mNumOfVisit;
	public int mNumOfLike;
	public int mNumOfDislike;
	public int mNumGetPromotion;
	public int mNumGetReward;
	public int mGroupShop;
	public int mLikeStatus;
	public int mLike;
	public int mDislike;
	
	public String mContent;
	public String mWeb;
	public Boolean mPromotionStatus = true;
	public Promotion mPromotion;
	public String mTel;
	public String mUpdateAt;
	public String mCatName;
	
	public Map<String, List<Item>> mItemCollections = new LinkedHashMap<String, List<Item>>();
    public List<String> mGroupItemList = new ArrayList<String>();
	public List<ImageStr> mUserImageList = new ArrayList<ImageStr>();
	public List<ImageStr> mShopImageList = new ArrayList<ImageStr>();
	public List<Comment> mCommentList = new ArrayList<Comment>();
	
	public Polyline polyline;

	public Shop() {
		
	}
	
	public static List<Shop> getListForUse(JSONArray shopArray) {
		try{
			return getListForUseThrow(shopArray);
		} catch (Exception ex) {
			return new ArrayList<Shop>();
		}
	}
	
	public static List<Shop> getListForUseThrow(JSONArray shopArry) throws JSONException {
		List<Shop> listShop = new ArrayList<Shop>();
		for(int i = 0; i < shopArry.length();i++){
			JSONObject object = (JSONObject)shopArry.get(i);
			Shop mShop = getShop(object, new Shop());
			listShop.add(mShop);
		}
		return listShop;
	}
	
	public static Shop getShop(JSONObject object, Shop mShop) throws JSONException {

		mShop.mID = object.getInt("id");
		mShop.mName = object.getString("name");
		mShop.mLat = (float)object.getDouble("shop_lat");
		mShop.mLng = (float)object.getDouble("shop_lng");
		mShop.mDistance = (float)object.getDouble("distance");
		mShop.mNumOfLike = object.optInt("num_of_like", 0);
		mShop.mNumOfVisit = object.optInt("num_of_visit", 0);
		mShop.mNumGetReward = object.optInt("num_get_reward", 0);
		mShop.mNumGetPromotion = object.optInt("num_get_promotion", 0);
		mShop.mGroupShop = object.optInt("group_shop", 0);
		mShop.mLogo = object.getString("logo");
		mShop.mContent = object.getString("description");
		mShop.mPromotionStatus = object.getInt("promotion_status") == 1;

		mShop.mTel = object.optString("tel", "");
		mShop.mWeb = object.optString("website", "");
		
		mShop.mAddress = object.getString("address");
		mShop.mLikeStatus = object.getInt("like_status");
		mShop.mLike = object.getInt("like");
		mShop.mDislike = object.getInt("dislike");

		mShop.mCover = object.optString("cover", "null");

		mShop.mUpdateAt = object.optString("updated_at", null);

		if (mShop.mPromotionStatus == true){
			JSONObject promotion = object.getJSONObject("promotion_detail");
			int type = promotion.getInt("promotion_type");

			switch (type){
			case -1:
				int sp = promotion.getInt("sp");
				mShop.mPromotion = new PromotionTypeZero(sp);	
				break;
			case 1:
				String cost = "";
				try{
					cost = promotion.getString("str_cost");
				}catch(Exception ex){
					cost = Integer.toString(promotion.getInt("cost") / 1000) + "K";
				}
				int sgp = promotion.getInt("sgp");
				int min_score = promotion.optInt("min_score", 0);
				int pPerSGP = promotion.getInt("P");
				sp = promotion.getInt("sp");
				String duration = promotion.getString("duration");
				List<Requirement> requirements = new ArrayList<Requirement>();
				JSONArray jrequires = promotion.getJSONArray("array_required");
				for(int j = 0; j < jrequires.length(); j++) {
					JSONObject jo = jrequires.getJSONObject(j);
					requirements.add(new Requirement(
							jo.getInt("id"),
							jo.getInt("required"),
							jo.getString("content")));
				}

				mShop.mPromotion = new PromotionTypeOne(cost, sgp, sp, min_score, duration, requirements);
				((PromotionTypeOne) mShop.mPromotion).mPperSGP = pPerSGP;
				break;
			case 2:
				PromotionTypeTwo pro2 = new PromotionTypeTwo();
				pro2.parse(promotion);
				mShop.mPromotion = pro2;
				break;
			}
		}
		
		return mShop;
	}
}
