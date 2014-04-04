package vn.infory.infory.data;

import com.cycrix.jsonparser.JsonInt;
import com.cycrix.jsonparser.JsonObject;
import com.cycrix.jsonparser.JsonString;

@JsonObject
public class ScanResponse {
	@JsonInt	public int status;
	@JsonString	public String message;
	
	@JsonInt(optional = true)		public int idShop;
	@JsonString(optional = true)	public String shopName;
	@JsonString(optional = true)	public String sgp;
	@JsonString(optional = true)	public String totalSGP;
	@JsonString(optional = true)	public String type;
	@JsonString(optional = true)	public String giftName;
	@JsonString(optional = true)	public String voucherName;
}
