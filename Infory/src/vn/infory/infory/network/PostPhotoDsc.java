package vn.infory.infory.network;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import vn.infory.infory.data.Settings;
import android.content.Context;

public class PostPhotoDsc extends CyAsyncTask {

	// Data
	private String mDsc;
	private int mUserGallery;

	public PostPhotoDsc(Context c, int idUserGallery, String dsc) {
		super(c);
		
		mUserGallery = idUserGallery;
		mDsc = dsc;
	}
	
	@Override
	protected Object doInBackground(Object... arg0) {

		try {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			
			Settings s = Settings.instance();
			
			pairs.add(new BasicNameValuePair("idUserGallery", "" + mUserGallery));
			pairs.add(new BasicNameValuePair("userLat", Float.toString(s.lat)));
			pairs.add(new BasicNameValuePair("userLng", Float.toString(s.lng)));
			pairs.add(new BasicNameValuePair("description", mDsc));
			
			String json = NetworkManager.post(APILinkMaker.mPostPhotoDsc, pairs);
//			String json = readWholeFile(mContext, R.raw.comment);
			
			JSONObject jResponse = new JSONObject(json);
			
			return jResponse;
		} catch (Exception e) {
			mEx = e;
		}
	
		return super.doInBackground(arg0);
	}
}