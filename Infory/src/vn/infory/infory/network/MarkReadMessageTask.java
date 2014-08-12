package vn.infory.infory.network;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import vn.infory.infory.data.Settings;
import android.content.Context;
import android.util.Log;

public class MarkReadMessageTask extends CyAsyncTask {
	public String TAG = "Infory MarkReadMessageTask";
	// Data
	private int idMessage, idSender;

    private onMarkReadMessageTaskListener mListener;
    
	public interface onMarkReadMessageTaskListener {
        public void onPreMarkReadMessage();

        public void onMarkReadMessageSuccess(String response);

        public void onMarkReadMessageFailure();
    }
	
	public MarkReadMessageTask(Context c, int idMessage, int idSender) {
		super(c);
		this.idMessage = idMessage;
		this.idSender = idSender;
	}

    public void setMarkReadMessageTaskListener(onMarkReadMessageTaskListener listener) {
        mListener = listener;
    }
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (mListener != null) {
            mListener.onPreMarkReadMessage();
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
			if(idMessage != 0)
				pairs.add(new BasicNameValuePair("idMessage", Integer.toString(idMessage)));
			if(idSender != 0)
				pairs.add(new BasicNameValuePair("idSender", Integer.toString(idSender)));

			String json = NetworkManager.post(APILinkMaker.mMarkReadMessage, pairs);

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
				mListener.onMarkReadMessageSuccess(returnString);
			else
				mListener.onMarkReadMessageFailure();
        }
	}
}
