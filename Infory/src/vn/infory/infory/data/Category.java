package vn.infory.infory.data;

import com.cycrix.jsonparser.JsonInt;
import com.cycrix.jsonparser.JsonObject;
import com.cycrix.jsonparser.JsonString;

@JsonObject
public class Category {
	@JsonInt 	public int id;
	@JsonString	public String name;
	@JsonInt	public int count;
}
