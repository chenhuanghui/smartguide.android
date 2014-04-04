package vn.infory.infory.data.home;

import java.util.ArrayList;

import com.cycrix.jsonparser.JsonArray;
import com.cycrix.jsonparser.JsonDouble;
import com.cycrix.jsonparser.JsonObject;
import com.cycrix.jsonparser.JsonString;

@JsonObject
public class HomeItem_ImageList extends HomeItem {
	@JsonArray		public ArrayList<String> images = new ArrayList<String>();
	@JsonDouble(optional = true)	public double imageWidth;
	@JsonDouble(optional = true)	public double imageHeight;
}
