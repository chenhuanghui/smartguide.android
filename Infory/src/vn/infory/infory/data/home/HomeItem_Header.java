package vn.infory.infory.data.home;

import java.util.ArrayList;

import android.R.string;

import com.cycrix.jsonparser.JsonArray;
import com.cycrix.jsonparser.JsonDouble;
import com.cycrix.jsonparser.JsonInt;
import com.cycrix.jsonparser.JsonObject;
import com.cycrix.jsonparser.JsonString;

@JsonObject
public class HomeItem_Header extends HomeItem {
	@JsonInt(optional = true)		public int idPlacelist;
	@JsonString(optional = true)	public String idShops;
	@JsonString (optional = true)	public String title;
	/*@JsonString						public String headerImage;	
	@JsonArray		public ArrayList<String> images = new ArrayList<String>();
	@JsonDouble(optional = true)	public double imageWidth;
	@JsonDouble(optional = true)	public double imageHeight;*/
}
