package vn.infory.infory.network;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import vn.infory.infory.data.Settings;
import android.content.Context;

public class LikeComment extends CyAsyncTask {

	// Data
	private int mIdComment;
	private int mIsAgree;

	public LikeComment(Context c, int id, int isAgree) {
		super(c);
		
		mIdComment	= id;
		mIsAgree	= isAgree;
	}
	
	@Override
	protected Object doInBackground(Object... arg0) {

		try { 
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			
			Settings s = Settings.instance();
			pairs.add(new BasicNameValuePair("idComment", "" + mIdComment));
			pairs.add(new BasicNameValuePair("userLat", Float.toString(s.lat)));
			pairs.add(new BasicNameValuePair("userLng", Float.toString(s.lng)));
			pairs.add(new BasicNameValuePair("isAgree", "" + mIsAgree));
			
			String json = NetworkManager.post(APILinkMaker.mLikeComment, pairs);
//			String json = readWholeFile(mContext, R.raw.scan_1);
			
			JSONObject jRespose = new JSONObject(json);
			
//			Thread.sleep(3000);
			
			return jRespose;
		} catch (Exception e) {
			mEx = e;
		}
	
		return super.doInBackground(arg0);
	}
}
