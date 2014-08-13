package vn.infory.infory.scancode;

import java.util.ArrayList;

import org.json.JSONArray;

public class ListModelRelatedPromotions {
	private JSONArray shop_ids;
	private String name;
	private String logo;
	private String time;
	private String description;
	public JSONArray getShop_ids() {
		return shop_ids;
	}
	public void setShop_ids(JSONArray shop_ids) {
		this.shop_ids = shop_ids;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLogo() {
		return logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
		
}
