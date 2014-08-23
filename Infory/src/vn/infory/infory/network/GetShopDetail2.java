package vn.infory.infory.network;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import vn.infory.infory.CyLogger;
import vn.infory.infory.data.Settings;
import vn.infory.infory.data.Shop;
import android.content.Context;

import com.cycrix.jsonparser.JsonParser;

public class GetShopDetail2 extends CyAsyncTask{
			
	CyLogger mLog = new CyLogger("CycrixDebug", true);
	
	// Data
	private int mId;

	public GetShopDetail2(Context c, int id) {
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
			
			String json = NetworkManager.post(APILinkMaker.mShopDetail, pairs);
			
			JSONObject jObj = new JSONObject(json);		
//			mLog.d("JSON: " + json.toString());
			return jObj;
		} catch (Exception e) {
			mEx = e;
		}
	
		return super.doInBackground(arg0);
	}
}
