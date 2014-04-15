package vn.infory.infory.network;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import vn.infory.infory.data.Settings;
import android.content.Context;

public class PostComment extends CyAsyncTask {

	// Data
	private String mComment;
	private int mShopId;

	public PostComment(Context c, int shopId, String comment) {
		super(c);
		
		mShopId = shopId;
		mComment = comment;
	}
	
	@Override
	protected Object doInBackground(Object... arg0) {

		try {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			
			Settings s = Settings.instance();
			
			pairs.add(new BasicNameValuePair("idShop", "" + mShopId));
			pairs.add(new BasicNameValuePair("userLat", Float.toString(s.lat)));
			pairs.add(new BasicNameValuePair("userLng", Float.toString(s.lng)));
			pairs.add(new BasicNameValuePair("comment", mComment));
			
			String json = NetworkManager.post(APILinkMaker.mPostComment, pairs);
//			String json = readWholeFile(mContext, R.raw.comment);
			
			JSONObject jResponse = new JSONObject(json);
			
			return jResponse;
		} catch (Exception e) {
			mEx = e;
		}
	
		return super.doInBackground(arg0);
	}
}