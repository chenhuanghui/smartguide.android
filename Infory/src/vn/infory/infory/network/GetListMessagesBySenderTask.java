package vn.infory.infory.network;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import vn.infory.infory.data.Settings;
import android.content.Context;
import android.util.Log;

public class GetListMessagesBySenderTask extends CyAsyncTask {

	public String TAG = "Infory GetListMessagesBySenderTask";
	// Data
	private int idSender, mPage;

    private onGetListMessagesBySenderTaskListener mListener;
    
	public interface onGetListMessagesBySenderTaskListener {
        public void onPreGetListMessagesBySender();

        public void onGetListMessagesBySenderSuccess(String response);

        public void onGetListMessagesBySenderFailure();
    }
	
	public GetListMessagesBySenderTask(Context c, int idSender, int page) {
		super(c);
		this.idSender = idSender;
		mPage = page;
	}

	@Override
	public void setPage(int page) {
		mPage = page;
	}

    public void setGetListMessagesBySenderTaskListener(onGetListMessagesBySenderTaskListener listener) {
        mListener = listener;
    }
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (mListener != null) {
            mListener.onPreGetListMessagesBySender();
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
			pairs.add(new BasicNameValuePair("idSender", Integer.toString(idSender)));
			pairs.add(new BasicNameValuePair("page", Integer.toString(mPage)));

			String json = NetworkManager.post(APILinkMaker.mGetMessagesBySender, pairs);

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
				mListener.onGetListMessagesBySenderSuccess(returnString);
			else
				mListener.onGetListMessagesBySenderFailure();
        }
	}
	

	
}
