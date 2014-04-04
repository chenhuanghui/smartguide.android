package vn.infory.infory.data;

import com.cycrix.jsonparser.JsonObject;
import com.cycrix.jsonparser.JsonString;

@JsonObject
public class ImageStr {
	@JsonString public String image;
	@JsonString public String description;
	@JsonString public String thumbnail;
	
	public boolean loadFail;
	
	@Override
	public String toString() {
		return image;
	}
}