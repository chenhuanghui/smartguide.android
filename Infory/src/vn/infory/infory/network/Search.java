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

public class Search extends CyAsyncTask {

	// Data
	private String mKeyword;
	private int mPage;
	private int mSort;
	private float mLat;
	private float mLng;
	private int mCityId;

	public Search(Context c, String keyword, int page, int sort) {
		super(c);
		Settings s = vn.infory.infory.data.Settings.instance();
		int cityId = Integer.parseInt(s.cityId);
		init(keyword, page, sort, s.lat, s.lng, cityId);
	}

	public Search(Context c, String keyword, int page, int sort, float lat,
			float lng, int cityId) {
		super(c);
		init(keyword, page, sort, lat, lng, cityId);
	}

	private void init(String keyword, int page, int sort, float lat, float lng, int cityId) {
		mKeyword = keyword;
		mPage = page;
		mSort = sort;
		mLat = lat;
		mLng = lng;
		mCityId = cityId;
	}

	@Override
	public void setPage(int page) {
		mPage = page;
	}

	public void setSort(int sort) {
		mSort = sort;
	}

	@Override
	public Search clone() {
		return new Search(mContext, mKeyword, mPage, mSort, mLat, mLng, mCityId);
	}

	@Override
	protected Object doInBackground(Object... arg0) {

		try {
			boolean hasLatLng = (mLat != -1) && (mLng != -1);
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();

			pairs.add(new BasicNameValuePair("keyWords", mKeyword));
			pairs.add(new BasicNameValuePair("userLat", Float.toString(mLat)));
			pairs.add(new BasicNameValuePair("userLng", Float.toString(mLng)));
			pairs.add(new BasicNameValuePair("sort", Integer.toString(mSort)));
			pairs.add(new BasicNameValuePair("page", Integer.toString(mPage)));
			pairs.add(new BasicNameValuePair("idCity", Integer.toString(mCityId)));
			String json = NetworkManager.post(APILinkMaker.mSearch, pairs);
			if (json.equalsIgnoreCase("null"))
				json = "[]";
			ArrayList<Shop> shopList = new ArrayList<Shop>();
			JsonParser.parseArray(shopList, Shop.class, 
					new JSONArray(json), JsonArray.FAIL_BEHAVIOR_THROW);
			
			for (Shop shop : shopList)
				shop.hasDistance = hasLatLng;

			return shopList;
		} catch (Exception e) {
			mEx = e;
		}

		return super.doInBackground(arg0);
	}
}
