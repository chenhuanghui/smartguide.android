package vn.infory.infory.data.home;

import com.cycrix.jsonparser.JsonInt;
import com.cycrix.jsonparser.JsonObject;

@JsonObject
public class HomeItem {
	@JsonInt(ignore = true)		public int idPost;
	@JsonInt(ignore = true)		public int type;
}
