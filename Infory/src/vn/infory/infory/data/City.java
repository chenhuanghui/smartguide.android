package vn.infory.infory.data;

import com.cycrix.jsonparser.JsonInt;
import com.cycrix.jsonparser.JsonObject;
import com.cycrix.jsonparser.JsonString;

@JsonObject
public class City {
	@JsonString						public String cityId;
	@JsonString						public String cityName;
}
