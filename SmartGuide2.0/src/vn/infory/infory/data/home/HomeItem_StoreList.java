package vn.infory.infory.data.home;

import java.util.ArrayList;

import com.cycrix.jsonparser.JsonArray;
import com.cycrix.jsonparser.JsonObject;

@JsonObject
public class HomeItem_StoreList extends HomeItem {
	@JsonArray(ignore = true)	public ArrayList<String> images = new ArrayList<String>();
	@JsonArray					
	public ArrayList<HomeItem_StoreItem> stores = new ArrayList<HomeItem_StoreItem>();
}
