package vn.infory.infory.network;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import vn.infory.infory.data.Settings;
import android.content.Context;
import android.util.Log;

public class GetNotificationTask extends CyAsyncTask {
	public String TAG = "Infory GetNotificationTask";
	// Data
	private int mType, mPage;

    private onGetNotificationsTaskListener mListener;
    
	public interface onGetNotificationsTaskListener {
        public void onPreGetNotifications();

        public void onGetNotificationsSuccess(String response);

        public void onGetNotificationsFailure();
    }
	
	public GetNotificationTask(Context c, int type, int page) {
		super(c);
		mType = type;
		mPage = page;
	}

	@Override
	public void setPage(int page) {
		mPage = page;
	}

    public void setGetNotificationsTaskListener(onGetNotificationsTaskListener listener) {
        mListener = listener;
    }
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (mListener != null) {
            mListener.onPreGetNotifications();
        }
	}

	@Override
	protected Object doInBackground(Object... arg0) {

		try {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();

			Settings s = Settings.instance();
			if (s.lat != -1 || s.lng != -1){
				pairs.add(new BasicNameValuePair("userLat", Float.toString(s.lat)));
				pairs.add(new BasicNameValuePair("userLng", Float.toString(s.lng)));
			}
			pairs.add(new BasicNameValuePair("type", Integer.toString(mType)));
			pairs.add(new BasicNameValuePair("page", Integer.toString(mPage)));

			String json = NetworkManager.post(APILinkMaker.mGetNotifications, pairs);

			Log.d(TAG, "json response: " + json);
			if (json.equalsIgnoreCase("null"))
				json = "[]";

			return json;
		} catch (Exception e) {
			mEx = e;
		}

		return super.doInBackground(arg0);
	}
	
	@Override
	protected void onPostExecute(Object result) {
		super.onPostExecute(result);
		if (mListener != null) {
			String returnString = (String) result;
			if(returnString != null && returnString.length() > 0)
				mListener.onGetNotificationsSuccess(returnString);
			else
				mListener.onGetNotificationsFailure();
        }
	}
	
}
