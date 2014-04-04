package vn.infory.infory.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.cycrix.jsonparser.JsonArray;
import com.cycrix.jsonparser.JsonCustom;
import com.cycrix.jsonparser.JsonDouble;
import com.cycrix.jsonparser.JsonInt;
import com.cycrix.jsonparser.JsonObject;
import com.cycrix.jsonparser.JsonParser;
import com.cycrix.jsonparser.JsonString;
import com.google.android.gms.maps.model.Polyline;


/**
 * Created by ChauSang on 7/23/13.
 */
@JsonObject
public class Shop {
	
	@JsonInt 					public int idShop;		//
	@JsonString 				public String shopName;	//
	@JsonString 				public String logo;		//
	@JsonString 				public String address;	//
	@JsonDouble 				public double shopLat = -1;	//
	@JsonDouble 				public double shopLng = -1;	//
	@JsonString(ignore = true) 	public String distance;	//
	@JsonInt 					public int shopType;	//
	@JsonString 				public String description;	//
	
	@JsonString 				public String numOfComment;	//
	@JsonString 				public String numOfView;	//
	@JsonString					public String numOfLove;	//
	@JsonInt 					public int loveStatus;		//

	@JsonInt					public int promotionType;	//
	@JsonCustom(methodName = "parsePromotion")
								public Promotion promotionDetail;
	@JsonString(optional = true)public String shopTypeDisplay;
	
	@JsonObject(ignore = true)	public ShopGallery shopGalleryFirst;	//
	
	@JsonString(ignore = true)	public String city;
	@JsonString(ignore = true)	public String tel;
	@JsonString(ignore = true)	public String displayTel;
	@JsonArray(ignore = true)	public List<ShopGallery> shopGallery = new ArrayList<ShopGallery>();	//
	@JsonArray(ignore = true)	public List<UserGallery> userGallery = new ArrayList<UserGallery>();	//
	@JsonArray(ignore = true)	public List<Comment> comments = new ArrayList<Comment>();	//
	
	@JsonObject(ignore = true)	public News promotionNews;
	
	public void parsePromotion(JSONObject jObj) throws Exception {
		
		int promoType = jObj.getInt("promotionType");
		if (promoType == 1 || promoType == 2) {
			JSONObject jPromotion = jObj.optJSONObject("promotionDetail");
			if (jPromotion == null)
				return;
			
			switch (promoType) {
			case 1:
				promotionDetail = new PromotionTypeOne();
				break;
			case 2:
				promotionDetail = new PromotionTypeTwo();
				break;
			}
			JsonParser.parseObject(promotionDetail, jPromotion);
		}
	}
	
	public void normalize() {
		if (shopGallery != null && shopGallery.size() > 0)
			shopGalleryFirst = shopGallery.get(0);
		else if (shopGalleryFirst != null)
			shopGallery.add(shopGalleryFirst);
	}
	
	public boolean hasDistance;
	public Polyline polyline;
}