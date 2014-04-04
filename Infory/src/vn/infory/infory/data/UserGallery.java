package vn.infory.infory.data;

import com.cycrix.jsonparser.JsonObject;
import com.cycrix.jsonparser.JsonString;

@JsonObject
public class UserGallery extends PhotoGallery {
	@JsonString	public String username;
	@JsonString	public String thumbnail;
	@JsonString	public String image;
	@JsonString	public String description;
	@JsonString	public String time;
	
	@Override
	public String getThumb() {
		return thumbnail;
	}
	
	@Override
	public String getImage() {
		return image;
	}
}
