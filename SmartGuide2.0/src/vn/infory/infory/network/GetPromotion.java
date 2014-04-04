package vn.infory.infory.network;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;

import vn.infory.infory.data.Settings;
import vn.infory.infory.data.home.PromoItem;
import android.content.Context;

import com.cycrix.jsonparser.JsonArray.FailBehavior;
import com.cycrix.jsonparser.JsonParser;

public class GetPromotion extends CyAsyncTask {

	// Data
	private int mPage;

	public GetPromotion(Context c, int page) {
		super(c);
		
		mPage = page;
	}
	
	@Override
	public void setPage(int page) {
		mPage = page;
	}
	
	@Override
	public GetPromotion clone() {
		return new GetPromotion(mContext, mPage);
	}

	@Override
	protected Object doInBackground(Object... arg0) {

		try {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			
			Settings s = Settings.instance();
			pairs.add(new BasicNameValuePair("userLat", Float.toString(s.lat)));
			pairs.add(new BasicNameValuePair("userLng", Float.toString(s.lng)));
			pairs.add(new BasicNameValuePair("page", Integer.toString(mPage)));
			
			String json = NetworkManager.post(APILinkMaker.mPromotion, pairs);
//			String json = readWholeFile(mContext, R.raw.home);
			
			if (json.equalsIgnoreCase("null"))
				json = "[]";
			
			JSONArray jArr = new JSONArray(json);
			ArrayList<PromoItem> promoItemList = new ArrayList<PromoItem>();
			JsonParser.parseArray(promoItemList, PromoItem.class, jArr, FailBehavior.Throw);
			
			return promoItemList;
		} catch (Exception e) {
			mEx = e;
		}
	
		return super.doInBackground(arg0);
	}
}
