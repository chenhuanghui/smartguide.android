package vn.infory.infory.data.home;

import org.json.JSONObject;

import vn.infory.infory.data.Shop;

import com.cycrix.jsonparser.JsonCustom;
import com.cycrix.jsonparser.JsonDouble;
import com.cycrix.jsonparser.JsonInt;
import com.cycrix.jsonparser.JsonObject;
import com.cycrix.jsonparser.JsonString;

@JsonObject
public class HomeItem_ShopItem extends HomeItem {
		
	@JsonInt					public int idShop;
	@JsonString					public String shopName;
	@JsonString					public String content;
	
	@JsonString(ignore = true)	public String cover;
	@JsonString(ignore = true)	public String numOfView;
	@JsonString(ignore = true)	public String logo;
	@JsonString(ignore = true)	public String date;
	@JsonString(ignore = true)	public String title;
	@JsonDouble(optional = true)public double coverWidth;
	@JsonDouble(optional = true)public double coverHeight;
	
	@JsonCustom(methodName = "parseGotoString")
	public String goto_;
	public void parseGotoString(JSONObject root) {
		goto_ = root.optString("goto");
	}
	
	public Shop makeShop() {
		Shop s = new Shop();
		
		s.idShop	= idShop;
		s.shopName	= shopName;
		s.numOfView = numOfView;
		s.logo		= logo;
//		s.data		= data;
//		s.title		= title;
		
		return s;
	}
}
