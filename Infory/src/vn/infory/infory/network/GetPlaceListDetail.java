package vn.infory.infory.network;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import vn.infory.infory.data.PlaceList;
import vn.infory.infory.data.Settings;
import vn.infory.infory.data.Shop;
import android.content.Context;

import com.cycrix.jsonparser.JsonArray;
import com.cycrix.jsonparser.JsonParser;

public class GetPlaceListDetail extends CyAsyncTask {

	// Data
	private int mId;
	private int mSort;

	public GetPlaceListDetail(Context c, int id, int sort) {
		super(c);
		
		mId = id;
		mSort = sort;
	}
	
	@Override
	protected Object doInBackground(Object... arg0) {

		try {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			
			Settings s = Settings.instance();
			boolean hasLatLng = (s.lat != -1) && (s.lng != -1);
			
			pairs.add(new BasicNameValuePair("idPlacelist", Integer.toString(mId)));
			pairs.add(new BasicNameValuePair("userLat", Float.toString(s.lat)));
			pairs.add(new BasicNameValuePair("userLng", Float.toString(s.lng)));
			pairs.add(new BasicNameValuePair("sort", Integer.toString(mSort)));
			
			String json = NetworkManager.post(APILinkMaker.mPlaceListDetail, pairs);	
//			String json = readWholeFile(mContext, R.raw.place_list);
			
			JSONObject jRoot = new JSONObject(json);
			PlaceList placeList = new PlaceList();
			JsonParser.parseObject(placeList, jRoot);
			
			ArrayList<Shop> shopList = new ArrayList<Shop>();
			if (!jRoot.getString("shops").equalsIgnoreCase("null"))
				JsonParser.parseArray(shopList, Shop.class, jRoot.getJSONArray("shops"), JsonArray.FAIL_BEHAVIOR_THROW);
			
			for (Shop shop : shopList)
				shop.hasDistance = hasLatLng;
			
			return new Object[] {placeList, shopList};
		} catch (Exception e) {
			mEx = e;
		}
	
		return super.doInBackground(arg0);
	}
}