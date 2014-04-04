package vn.infory.infory.data.home;

import vn.infory.infory.data.Shop;

import com.cycrix.jsonparser.JsonDouble;
import com.cycrix.jsonparser.JsonInt;
import com.cycrix.jsonparser.JsonObject;
import com.cycrix.jsonparser.JsonString;

@JsonObject
public class PromoItem {
	@JsonString	public String logo;
	@JsonString	public String brandName;
	@JsonString	public String title;
	@JsonString	public String description;
	@JsonString	public String date;
	@JsonString	public String cover;
	@JsonDouble	public double coverHeight;
	@JsonDouble	public double coverWidth;
	@JsonString	public String goTo;
	@JsonInt	public int type;
	
	@JsonString(optional = true)	public String idShops;
	@JsonInt(optional = true)		public int idShop;
	@JsonInt(optional = true)		public int idStore;
	@JsonInt(optional = true)		public int idItem;
	
	public Shop makeShop() {
		Shop s = new Shop();
		
		s.idShop	= idShop;
		s.logo		= logo;
		s.shopName	= brandName;
		
		
		return s;
	}
}
