package vn.infory.infory.data;

import com.cycrix.jsonparser.JsonObject;
import com.cycrix.jsonparser.JsonString;

/**
 * Created by ChauSang on 7/23/13.
 */

@JsonObject
public abstract class Promotion {
	
	@JsonString	public String duration;

	public abstract int getType();
}
