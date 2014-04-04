package vn.infory.infory.network;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;

import vn.infory.infory.data.Settings;
import vn.infory.infory.data.Shop;
import android.content.Context;

import com.cycrix.jsonparser.JsonArray.FailBehavior;
import com.cycrix.jsonparser.JsonParser;

public class GetShopList extends CyAsyncTask {

	// Data
	private int mPage;
	private String mId;
	private int mSort;

	public GetShopList(Context c, String shopId, int page, int sort) {
		super(c);
		
		mPage = page;
		mId = shopId;
		mSort = sort;
	}
	
	@Override
	public void setPage(int page) {
		mPage = page;
	}
	
	public void setSort(int sort) {
		mSort = sort;
	}
	
	@Override
	public GetShopList clone() {
		return new GetShopList(mContext, mId, mPage, mSort);
	}

	@Override
	protected Object doInBackground(Object... arg0) {

		try {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			
			Settings s = Settings.instance();
			boolean hasLatLng = (s.lat != -1) && (s.lng != -1);
			
			pairs.add(new BasicNameValuePair("idShop", mId));
			pairs.add(new BasicNameValuePair("userLat", Float.toString(s.lat)));
			pairs.add(new BasicNameValuePair("userLng", Float.toString(s.lng)));
			pairs.add(new BasicNameValuePair("page", Integer.toString(mPage)));
			pairs.add(new BasicNameValuePair("sort", Integer.toString(mSort)));
			
			String json = NetworkManager.post(APILinkMaker.mShopList, pairs);
//			String json = readWholeFile(mContext, R.raw.place_list);
			
			if (json.equalsIgnoreCase("null"))
				json = "[]";
			ArrayList<Shop> shopList = new ArrayList<Shop>();
			JsonParser.parseArray(shopList, Shop.class, new JSONArray(json), FailBehavior.Throw);
			
			for (Shop shop : shopList)
				shop.hasDistance = hasLatLng;
			
			return shopList;
		} catch (Exception e) {
			mEx = e;
		}
	
		return super.doInBackground(arg0);
	}
}