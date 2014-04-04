package vn.infory.infory.network;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;

import vn.infory.infory.data.Settings;
import vn.infory.infory.data.Shop;
import vn.infory.infory.data.UserGallery;
import android.content.Context;

import com.cycrix.jsonparser.JsonArray.FailBehavior;
import com.cycrix.jsonparser.JsonParser;

public class GetUserGallery extends CyAsyncTask {

	// Data
	private int mPage;
	private String mId;

	public GetUserGallery(Context c, String shopId, int page) {
		super(c);
		
		mPage = page;
		mId = shopId;
	}
	
	@Override
	public void setPage(int page) {
		mPage = page;
	}
	
	@Override
	public GetUserGallery clone() {
		return new GetUserGallery(mContext, mId, mPage);
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
			
			String json = NetworkManager.post(APILinkMaker.mUserGallery, pairs);
//			String json = readWholeFile(mContext, R.raw.place_list);
			
			if (json.equalsIgnoreCase("null"))
				json = "[]";
			ArrayList<UserGallery> gallery = new ArrayList<UserGallery>();
			JsonParser.parseArray(gallery, UserGallery.class, new JSONArray(json), FailBehavior.Throw);
			
			return gallery;
		} catch (Exception e) {
			mEx = e;
		}
	
		return super.doInBackground(arg0);
	}
}