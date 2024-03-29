package vn.infory.infory.data;

import com.cycrix.jsonparser.JsonDouble;
import com.cycrix.jsonparser.JsonObject;
import com.cycrix.jsonparser.JsonString;

@JsonObject
public class News {
	@JsonString public String duration;
	@JsonString public String image;
	@JsonDouble	public double imageWidth;
	@JsonDouble	public double imageHeight;
	@JsonString public String title;
	@JsonString public String content;
}
