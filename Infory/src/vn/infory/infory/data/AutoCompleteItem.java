package vn.infory.infory.data;

import java.util.ArrayList;

import com.cycrix.jsonparser.JsonArray;
import com.cycrix.jsonparser.JsonBool;
import com.cycrix.jsonparser.JsonInt;
import com.cycrix.jsonparser.JsonObject;
import com.cycrix.jsonparser.JsonString;

@JsonObject
public class AutoCompleteItem {
	
	@JsonString	public String _type;
	@JsonObject	public Fields fields;
	@JsonObject	public Highlight highlight;
	
	public int RIDtype;
	
	@JsonObject
	public static class Fields {
		@JsonInt	public int id;
		
		// Shop
		@JsonString(optional = true)	public String shop_name;
		@JsonBool(optional = true)		public boolean hasPromotion;
		
		// Place list
		@JsonString(optional = true)	public String name;
	}
	
	@JsonObject
	public static class Highlight {
		// Shop
		@JsonArray(optional = true)	public ArrayList<String> shop_name_auto_complete = new ArrayList<String>();
		
		// Place list
		@JsonArray(optional = true)	public ArrayList<String> name_auto_complete = new ArrayList<String>();
	}
}

