package vn.infory.infory.data;

import java.util.ArrayList;
import java.util.List;

import com.cycrix.jsonparser.JsonArray;
import com.cycrix.jsonparser.JsonInt;
import com.cycrix.jsonparser.JsonObject;
import com.cycrix.jsonparser.JsonString;


@JsonObject
public class ItemCategory {
	@JsonString	public String cat_name;
	@JsonArray	public List<Item> items = new ArrayList<Item>();
}
