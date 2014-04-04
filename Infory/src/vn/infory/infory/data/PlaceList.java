package vn.infory.infory.data;

import com.cycrix.jsonparser.JsonInt;
import com.cycrix.jsonparser.JsonObject;
import com.cycrix.jsonparser.JsonString;

@JsonObject
public class PlaceList {
	
	@JsonInt						public int idPlacelist;
	@JsonString						public String title;
	@JsonString						public String description;
	@JsonString(optional = true)	public String image;
	@JsonInt						public int numOfView;
	@JsonInt						public int loveStatus;
	
	@JsonString(optional = true)	public String authorName;
	@JsonString(optional = true)	public String authorAvatar;
	@JsonString(optional = true)	public String numOfShop;
	@JsonString(optional = true)	public String content;
	@JsonString(optional = true)	public String cover;
}
