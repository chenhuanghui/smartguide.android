package vn.infory.infory.data.home;

import com.cycrix.jsonparser.JsonObject;
import com.cycrix.jsonparser.JsonString;

@JsonObject
public class HomeItem_BranchPromoInfo extends HomeItem {
	@JsonString		public String logo;
	@JsonString		public String content;
	@JsonString		public String shopList;
}
