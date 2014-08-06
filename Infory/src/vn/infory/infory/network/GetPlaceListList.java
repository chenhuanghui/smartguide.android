package vn.infory.infory.network;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;

import vn.infory.infory.R;
import vn.infory.infory.data.PlaceList;
import vn.infory.infory.data.Settings;
import vn.infory.infory.data.Shop;

import com.cycrix.jsonparser.JsonArray;
import com.cycrix.jsonparser.JsonParser;

import android.content.Context;

public class GetPlaceListList extends CyAsyncTask {

	// Data
	private int mPage;

	public GetPlaceListList(Context c, int page) {
		super(c);
		
		mPage = page;
	}
	
	@Override
	public void setPage(int page) {
		mPage = page;
	}
	
	@Override
	public GetPlaceListList clone() {
		return new GetPlaceListList(mContext, mPage);
	}

	@Override
	protected Object doInBackground(Object... arg0) {

		try {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			
			Settings s = Settings.instance();
			pairs.add(new BasicNameValuePair("userLat", Float.toString(s.lat)));
			pairs.add(new BasicNameValuePair("userLng", Float.toString(s.lng)));
			pairs.add(new BasicNameValuePair("page", Integer.toString(mPage)));
			
			String json = NetworkManager.post(APILinkMaker.mPlaceListList, pairs);
//			String json = readWholeFile(mContext, R.raw.place_list_list);
			
			if (json.equalsIgnoreCase("null"))
				json = "[]";
			ArrayList<PlaceList> placeListList = new ArrayList<PlaceList>();
			JsonParser.parseArray(placeListList, PlaceList.class, new JSONArray(json), JsonArray.FAIL_BEHAVIOR_THROW);
			
			return placeListList;
		} catch (Exception e) {
			mEx = e;
		}
	
		return super.doInBackground(arg0);
	}
}
