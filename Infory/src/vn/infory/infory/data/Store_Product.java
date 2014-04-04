package vn.infory.infory.data;

import com.cycrix.jsonparser.JsonDouble;
import com.cycrix.jsonparser.JsonInt;
import com.cycrix.jsonparser.JsonObject;
import com.cycrix.jsonparser.JsonString;

@JsonObject
public class Store_Product {
	@JsonInt	public int idItem;
	@JsonString	public String price;
	@JsonDouble	public double p;
	@JsonString	public String money;
	@JsonString	public String description;
	@JsonString	public String image;
}
