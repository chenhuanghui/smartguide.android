package vn.infory.infory.data;

import com.cycrix.jsonparser.JsonObject;
import com.cycrix.jsonparser.JsonString;

/**
 * Created by ChauSang on 7/23/13.
 */

@JsonObject
public class Item {
	@JsonString public String name;
	@JsonString public String price;
}