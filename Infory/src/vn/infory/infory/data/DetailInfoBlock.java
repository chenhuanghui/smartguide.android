package vn.infory.infory.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cycrix.jsonparser.JsonArray;
import com.cycrix.jsonparser.JsonCustom;
import com.cycrix.jsonparser.JsonInt;
import com.cycrix.jsonparser.JsonObject;
import com.cycrix.jsonparser.JsonParser;
import com.cycrix.jsonparser.JsonString;

@JsonObject
public class DetailInfoBlock {
	@JsonInt	public int type;
	@JsonString	public String header;
	@JsonCustom(methodName = "parseItems")
				public List<DetailInfoItem> items = new ArrayList<DetailInfoItem>();
	
	public void parseItems(JSONObject jObj) throws Exception {
		JSONArray jItemArr = jObj.getJSONArray("items");
		switch (jObj.getInt("type")) {
		case 1:
			JsonParser.parseArray(items, DetailInfoItem1.class, jItemArr, JsonArray.FAIL_BEHAVIOR_THROW);
			break;
		case 2:
			JsonParser.parseArray(items, DetailInfoItem2.class, jItemArr, JsonArray.FAIL_BEHAVIOR_THROW);
			break;
		case 3:
			JsonParser.parseArray(items, DetailInfoItem3.class, jItemArr, JsonArray.FAIL_BEHAVIOR_THROW);
			break;
		case 4:
			JsonParser.parseArray(items, DetailInfoItem4.class, jItemArr, JsonArray.FAIL_BEHAVIOR_THROW);
			break;
		}
	}
}
