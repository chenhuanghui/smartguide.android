package vn.redbase.smartguide;

import android.graphics.Bitmap;

public class ImageStr {
	
	public String url;
	public String description;
	public Bitmap bm;
	public boolean loading;
	
	public ImageStr(String u, String d) {
		url = u;
		description = d;
	}
}
