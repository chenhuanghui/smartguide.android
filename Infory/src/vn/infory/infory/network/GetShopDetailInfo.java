package vn.infory.infory.network;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;

import vn.infory.infory.R;
import vn.infory.infory.data.DetailInfoBlock;
import vn.infory.infory.data.Settings;
import vn.infory.infory.data.Shop;
import android.content.Context;

import com.cycrix.jsonparser.JsonArray;
import com.cycrix.jsonparser.JsonParser;

public class GetShopDetailInfo extends CyAsyncTask {

	// Data
	private int mId;

	public GetShopDetailInfo(Context c, int id) {
		super(c);
		
		mId = id;
	}

	@Override
	protected Object doInBackground(Object... arg0) {
		

		try {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			
			Settings s = Settings.instance();
			pairs.add(new BasicNameValuePair("idShop", Integer.toString(mId)));
			pairs.add(new BasicNameValuePair("userLat", Float.toString(s.lat)));
			pairs.add(new BasicNameValuePair("userLng", Float.toString(s.lng)));
			
			String json = NetworkManager.post(APILinkMaker.mShopDetailInfo, pairs);
			
			JSONArray jArr = new JSONArray(json);
			List<DetailInfoBlock> detailBlockList = new ArrayList<DetailInfoBlock>();
			JsonParser.parseArray(detailBlockList, DetailInfoBlock.class, jArr, JsonArray.FAIL_BEHAVIOR_THROW);
			
			super.doInBackground(arg0);
			return detailBlockList;
		} catch (Exception e) {
			mEx = e;
		}
	
		return super.doInBackground(arg0);
	}
}