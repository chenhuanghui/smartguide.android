package vn.infory.infory.network;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;

import vn.infory.infory.data.Settings;
import android.content.Context;

public class ScanCodeRelated extends CyAsyncTask{

	//Input data
	private String mCode;
	private Integer mType;
	private Integer mPage;
	private Integer mPageSize;
	
	public ScanCodeRelated(Context c, String code, Integer type, Integer page) {
		super(c);
		// TODO Auto-generated constructor stub
		
		mCode = code;
		mType = type;
		mPage = page;
		mPageSize = 10;
	}

	@Override
	protected Object doInBackground(Object... arg0) {

		try { 
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			
			Settings s = Settings.instance();
			pairs.add(new BasicNameValuePair("userLat", Float.toString(s.lat)));
			pairs.add(new BasicNameValuePair("userLng", Float.toString(s.lng)));
			pairs.add(new BasicNameValuePair("code", mCode));
			pairs.add(new BasicNameValuePair("type", Integer.toString(mType)));
			pairs.add(new BasicNameValuePair("page", Integer.toString(mPage)));
			pairs.add(new BasicNameValuePair("pageSize", Integer.toString(mPageSize)));
			
			String json = NetworkManager.post(APILinkMaker.mRelated, pairs);
			JSONArray response = new JSONArray(json);
			
			return response;
		} catch (Exception e) {
			mEx = e;
		}
	
		return super.doInBackground(arg0);
	}
}
