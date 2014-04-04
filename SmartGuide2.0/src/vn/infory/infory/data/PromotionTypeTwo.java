package vn.infory.infory.data;

import java.util.ArrayList;
import java.util.List;

import com.cycrix.jsonparser.JsonArray;
import com.cycrix.jsonparser.JsonBool;
import com.cycrix.jsonparser.JsonInt;
import com.cycrix.jsonparser.JsonObject;
import com.cycrix.jsonparser.JsonString;


/**
 * Created by ChauSang on 7/23/13.
 */

@JsonObject
public class PromotionTypeTwo extends Promotion {
	
	@JsonString	public String text;
	@JsonString	public String note;
	@JsonArray	public List<Voucher> voucherList = new ArrayList<Voucher>();
	
	@JsonObject
	public static class Voucher {
		@JsonString	public String type;
		@JsonString	public String name;
		@JsonString	public String condition;
		@JsonString	public String highlightKeywords;
		@JsonInt	public int isAfford;
		
		@Override
		public String toString() {
			return name;
		}
	}

	@Override
	public int getType() {
		return 2;
	}
}
