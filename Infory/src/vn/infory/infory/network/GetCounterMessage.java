package vn.infory.infory.network;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import vn.infory.infory.data.Settings;
import android.content.Context;
import android.util.Log;

public class GetCounterMessage extends CyAsyncTask {
	public String TAG = "Infory GetCounterMessage: ";
	
	// Data
	private int mType;

	public GetCounterMessage(Context c, int type) {
		super(c);
		mType = type;
	}
	
	@Override
	protected Object doInBackground(Object... arg0) {

		try {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();

			Settings s = Settings.instance();
			if (s.lat != -1 || s.lng != -1) {
				pairs.add(new BasicNameValuePair("userLat", Float.toString(s.lat)));
				pairs.add(new BasicNameValuePair("userLng", Float.toString(s.lng)));
			}
			pairs.add(new BasicNameValuePair("type", Integer.toString(mType)));

			String json = NetworkManager.post(APILinkMaker.mGetMessageCount, pairs);
			// String json = readWholeFile(mContext, R.raw.home);
			Log.e(TAG, "json response: " + json);
			

			return json;
		} catch (Exception e) {
			mEx = e;
		}

		return super.doInBackground(arg0);
	}
}
