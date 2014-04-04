package vn.infory.infory.data;

import com.cycrix.jsonparser.JsonObject;
import com.cycrix.jsonparser.JsonString;

@JsonObject
public abstract class DetailInfoItem {
	@JsonString	public String content;
}
