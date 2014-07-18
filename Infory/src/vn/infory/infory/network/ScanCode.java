package vn.infory.infory.network;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import vn.infory.infory.data.ScanResponse;
import vn.infory.infory.data.Settings;
import android.content.Context;

import com.cycrix.jsonparser.JsonParser;

public class ScanCode extends CyAsyncTask {

	// Data
	private String mCode;

	public ScanCode(Context c, String code) {
		super(c);
		
		mCode = code; 
	}
	
	@Override
	protected Object doInBackground(Object... arg0) {

		try { 
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			
			Settings s = Settings.instance();
			pairs.add(new BasicNameValuePair("userLat", Float.toString(s.lat)));
			pairs.add(new BasicNameValuePair("userLng", Float.toString(s.lng)));
			pairs.add(new BasicNameValuePair("code", mCode));
			
			String json = NetworkManager.post(APILinkMaker.mScan, pairs);
//			String json = readWholeFile(mContext, R.raw.scan_1);
			
//			ScanResponse scanResponse = new ScanResponse();
//			JSONObject scanResponse = new JSONObject(json);
			JSONArray scanResponse = new JSONArray(json);
//			JsonParser.parseObject(scanResponse, json);
			
//			Thread.sleep(3000);
			
			return scanResponse;
		} catch (Exception e) {
			mEx = e;
		}
	
		return super.doInBackground(arg0);
	}
}
