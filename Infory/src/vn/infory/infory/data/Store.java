package vn.infory.infory.data;

import java.util.ArrayList;

import com.cycrix.jsonparser.JsonArray;
import com.cycrix.jsonparser.JsonInt;
import com.cycrix.jsonparser.JsonObject;
import com.cycrix.jsonparser.JsonString;

@JsonObject
public class Store {
	@JsonInt	public int idStore;
	@JsonString	public String logo;
	@JsonString	public String storeName;
	@JsonString	public String storeType;
	@JsonString	public String description;
	@JsonString	public String condition;
	@JsonString	public String highlightKeywords;
	@JsonString	public String total;
	@JsonArray	public ArrayList<Store_Product> latestItems = new ArrayList<Store_Product>();
	@JsonArray	public ArrayList<Store_Product> topSellerItems = new ArrayList<Store_Product>();
}
