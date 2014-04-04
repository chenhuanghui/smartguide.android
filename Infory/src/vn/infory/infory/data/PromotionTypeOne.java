package vn.infory.infory.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.Duration;

import com.cycrix.jsonparser.JsonArray;
import com.cycrix.jsonparser.JsonInt;
import com.cycrix.jsonparser.JsonObject;
import com.cycrix.jsonparser.JsonString;


/**
 * Created by ChauSang on 7/23/13.
 */

@JsonObject
public class PromotionTypeOne extends Promotion {
	@JsonString	public String text;
	@JsonString	public String money;
	@JsonInt	public int hasSGP;
	@JsonString	public String sgp;
//	@JsonString	public String sp;
//	@JsonString	public String p;
	@JsonArray	public List<Voucher> voucherList = new ArrayList<Voucher>();
	
	@JsonObject
	public static class Voucher {
		@JsonString	public String type;
		@JsonString	public String name;
		@JsonString	public String sgp;
		@JsonInt	public int isAfford;
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	@Override
	public int getType() {
		return 1;
	}
}
