package vn.smartguide;

import java.lang.ref.WeakReference;

import android.graphics.Bitmap;

public class ImageStr {
	
	public String url;
	public String description;
	public boolean loadFail;
	
	public ImageStr(String u, String d) {
		url = u;
		description = d;
	}
	
	@Override
	public String toString() {
		return url;
	}
}
