package vn.infory.infory.data;

import com.cycrix.jsonparser.JsonObject;
import com.cycrix.jsonparser.JsonString;

@JsonObject
public class ShopGallery extends PhotoGallery {
	@JsonString	public String cover;
	@JsonString	public String image;
	
	@Override
	public String getThumb() {
		return cover;
	}
	
	@Override
	public String getImage() {
		return image;
	}
}
