package vn.infory.infory.network;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;

import vn.infory.infory.data.Settings;
import vn.infory.infory.data.ShopGallery;
import android.content.Context;

import com.cycrix.jsonparser.JsonArray;
import com.cycrix.jsonparser.JsonParser;

public class GetShopGallery extends CyAsyncTask {

	// Data
	private int mPage;
	private String mId;

	public GetShopGallery(Context c, String shopId, int page) {
		super(c);
		
		mPage = page;
		mId = shopId;
	}
	
	@Override
	public void setPage(int page) {
		mPage = page;
	}
	
	@Override
	public GetShopGallery clone() {
		return new GetShopGallery(mContext, mId, mPage);
	}

	@Override
	protected Object doInBackground(Object... arg0) {

		try {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			
			Settings s = Settings.instance();
			
			pairs.add(new BasicNameValuePair("idShop", mId));
			pairs.add(new BasicNameValuePair("userLat", Float.toString(s.lat)));
			pairs.add(new BasicNameValuePair("userLng", Float.toString(s.lng)));
			pairs.add(new BasicNameValuePair("page", Integer.toString(mPage)));
			
			String json = NetworkManager.post(APILinkMaker.mShopGallery, pairs);
//			String json = readWholeFile(mContext, R.raw.place_list);
			
			if (json.equalsIgnoreCase("null"))
				json = "[]";
			ArrayList<ShopGallery> gallery = new ArrayList<ShopGallery>();
			JsonParser.parseArray(gallery, ShopGallery.class, new JSONArray(json), JsonArray.FAIL_BEHAVIOR_THROW);
			
			return gallery;
		} catch (Exception e) {
			mEx = e;
		}
	
		return super.doInBackground(arg0);
	}
}