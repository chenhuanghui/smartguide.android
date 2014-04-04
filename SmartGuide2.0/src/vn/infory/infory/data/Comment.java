package vn.infory.infory.data;

import com.cycrix.jsonparser.JsonInt;
import com.cycrix.jsonparser.JsonObject;
import com.cycrix.jsonparser.JsonString;

@JsonObject
public class Comment {
	@JsonInt	public int idComment;
	@JsonString	public String username;
	@JsonString	public String comment;
	@JsonString	public String avatar;
	@JsonString	public String time;
	
	@JsonString	public String numOfAgree;
	@JsonInt	public int agreeStatus;
}
