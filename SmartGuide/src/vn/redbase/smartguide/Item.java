package vn.redbase.smartguide;

/**
 * Created by ChauSang on 7/23/13.
 */
public class Item {
	public String mName;
	public String mPrice;
	public String mDescription;
	public String mURLImage;

	public Item(String name, String price, String description, String URLImage){
		mName = name;
		mPrice = price;
		mDescription = description;
		mURLImage = URLImage;
	}
}
