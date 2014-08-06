package vn.infory.infory.data;

import com.cycrix.jsonparser.JsonInt;
import com.cycrix.jsonparser.JsonObject;
import com.cycrix.jsonparser.JsonString;

@JsonObject()
public class GiftItem {
	
	@JsonInt 		public int id;
	@JsonString		public String score;
	@JsonString		public String content;
	@JsonInt		public int status;
	@JsonString		public String thumbnail;
}
