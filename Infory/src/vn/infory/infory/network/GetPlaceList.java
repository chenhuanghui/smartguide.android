package vn.infory.infory.network;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;

import vn.infory.infory.data.Settings;
import vn.infory.infory.data.Shop;
import android.content.Context;

import com.cycrix.jsonparser.JsonArray;
import com.cycrix.jsonparser.JsonParser;

public class GetPlaceList extends CyAsyncTask {

	// Data
	private int mPage;
	private int mId;
	private int mSort;

	public GetPlaceList(Context c, int page, int id, int sort) {
		super(c);
		
		mPage = page;
		mId = id;
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
	public GetPlaceList clone() { 
		return new GetPlaceList(mContext, mPage, mId, mSort);
	}

	@Override
	protected Object doInBackground(Object... arg0) {

		try {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			
			Settings s = Settings.instance();
			boolean hasLatLng = (s.lat != -1) && (s.lng != -1);
			
			pairs.add(new BasicNameValuePair("idPlacelist", Integer.toString(mId)));
			pairs.add(new BasicNameValuePair("page", Integer.toString(mPage)));
			pairs.add(new BasicNameValuePair("sort", Integer.toString(mSort)));
			pairs.add(new BasicNameValuePair("userLat", Float.toString(s.lat)));
			pairs.add(new BasicNameValuePair("userLng", Float.toString(s.lng)));
			
			String json = NetworkManager.post(APILinkMaker.mPlaceList, pairs);
//			String json = readWholeFile(mContext, R.raw.place_list);
			
			if (json.equalsIgnoreCase("null"))
				json = "[]";
			ArrayList<Shop> shopList = new ArrayList<Shop>();
			JsonParser.parseArray(shopList, Shop.class, new JSONArray(json), JsonArray.FAIL_BEHAVIOR_THROW);
			
			for (Shop shop : shopList)
				shop.hasDistance = hasLatLng;
			
			return shopList;
		} catch (Exception e) {
			mEx = e;
		}
	
		return super.doInBackground(arg0);
	}
}