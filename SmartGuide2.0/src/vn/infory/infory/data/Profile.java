package vn.infory.infory.data;

import com.cycrix.jsonparser.JsonInt;
import com.cycrix.jsonparser.JsonObject;
import com.cycrix.jsonparser.JsonString;

@JsonObject
public class Profile {
	@JsonInt	public int idUser;
	@JsonString	public String name;
	@JsonInt	public int gender;
	@JsonString	public String cover;
	@JsonString	public String avatar;
	@JsonString	public String phone;
	@JsonString	public String dob;
	@JsonInt	public int socialType;
}
