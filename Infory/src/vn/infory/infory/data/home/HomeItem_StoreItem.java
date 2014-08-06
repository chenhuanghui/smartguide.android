package vn.infory.infory.data.home;

import org.json.JSONObject;

import vn.infory.infory.data.Store;

import com.cycrix.jsonparser.JsonCustom;
import com.cycrix.jsonparser.JsonDouble;
import com.cycrix.jsonparser.JsonObject;
import com.cycrix.jsonparser.JsonString;

@JsonObject
public class HomeItem_StoreItem extends HomeItem {
	@JsonString					public String content;
	@JsonString					public String cover;
	@JsonObject					public Store storeInfo;
	
	@JsonString(optional = true)public String numOfPurchase;
	
	@JsonString(optional = true)public String date;
	@JsonString(optional = true)public String title;
	@JsonDouble(optional = true)public double coverWidth;
	@JsonDouble(optional = true)public double coverHeight;
	
	@JsonCustom(methodName = "parseGotoString")
	public String goto_;
	public void parseGotoString(JSONObject root) {
		goto_ = root.optString("goto");
	}
}
