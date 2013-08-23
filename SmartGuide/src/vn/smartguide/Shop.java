package vn.smartguide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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
	public int mDislike;
	
	public String mContent;
	public Boolean mPromotionStatus = true;
	public Promotion mPromotion;
	public String mTel;
	public String mUpdateAt;
	public String mCatName;
	
	public List<Item> mItemList = new ArrayList<Item>();
	public List<ImageStr> mUserImageList = new ArrayList<ImageStr>();
	public List<ImageStr> mShopImageList = new ArrayList<ImageStr>();
	public List<Comment> mCommentList = new ArrayList<Comment>();

	public Shop() {
		
	}
	
	public static List<Shop> getListForUse(JSONArray shopArry){
		List<Shop> listShop = new ArrayList<Shop>();
		try{
			for(int i = 0; i < shopArry.length();i++){
				JSONObject object = (JSONObject)shopArry.get(i);

				Shop mShop = new Shop();

				mShop.mID = object.getInt("id");
				mShop.mName = object.getString("name");
				mShop.mLat = (float)object.getDouble("shop_lat");
				mShop.mLng = (float)object.getDouble("shop_lng");
				mShop.mDistance = (float)object.getDouble("distance");
				mShop.mNumOfLike = object.getInt("num_of_like");
				mShop.mNumOfVisit = object.getInt("num_of_visit");
				mShop.mNumGetReward = object.getInt("num_get_reward");
				mShop.mNumGetPromotion = object.getInt("num_get_promotion");
				mShop.mGroupShop = object.getInt("group_shop");
				mShop.mLogo = object.getString("logo");
				mShop.mContent = object.getString("description");
				mShop.mPromotionStatus = object.getInt("promotion_status") == 1;
//				mShop.mTel = object.getString("tel");
				mShop.mAddress = object.getString("address");
				mShop.mLikeStatus = object.getInt("like_status");
				mShop.mNumOfDislike = object.getInt("dislike");
				
				try{
					mShop.mCover = object.getString("cover");
				}catch(Exception ex){
					mShop.mCover = "null";
				}
				
				try{
					mShop.mUpdateAt = object.getString("updated_at");
				}catch(Exception ex){
					
				}
				if (mShop.mPromotionStatus == true){
					JSONObject promotion = object.getJSONObject("promotion_detail");
					int type = promotion.getInt("promotion_type");

					switch (type){
					case -1:
						int sp = promotion.getInt("sp");
						mShop.mPromotion = new PromotionTypeZero(sp);	
						break;
					case 1:
						int cost = promotion.getInt("cost");
						int sgp = promotion.getInt("sgp");
						sp = promotion.getInt("sp");
						String duration = promotion.getString("duration");
						List<Requirement> requirements = new ArrayList<Requirement>();
						JSONArray jrequires = promotion.getJSONArray("array_required");
						for(int j = 0; j < jrequires.length(); j++){
							JSONObject jo = jrequires.getJSONObject(j);
							requirements.add(new Requirement(jo.getInt("id"), jo.getInt("required"), jo.getString("content")));
						}
						
						mShop.mPromotion = new PromotionTypeOne(cost, sgp, sp, duration, requirements);
						
						break;
					case 2:
						int money = promotion.getInt("money");
						int id = promotion.getInt("id");
						mShop.mPromotion = new PromotionTypeTwo(money, id);
						break;
					}
				}
				listShop.add(mShop);
			}
		} catch (Exception ex) {

		}
		return listShop;
	}

	public static List<Shop> getListShopWithFullInfor(String jsonString) throws JSONException {
		return null;
	}
}
