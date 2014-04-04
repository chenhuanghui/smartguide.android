package vn.infory.infory.network;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.content.Context;

public class CheckActiveCode extends CyAsyncTask {

	// Data
	private String mMessage;
	private String mPhoneNum; 
	private String mActiveCode;

	public CheckActiveCode(Context c, String phoneNum, String activeCode) {
		super(c);

		mPhoneNum = phoneNum;
		mActiveCode = activeCode;
	}

	@Override
	protected Object doInBackground(Object... arg0) {

		try {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();

			pairs.add(new BasicNameValuePair("phone", mPhoneNum));
			pairs.add(new BasicNameValuePair("activeCode", mActiveCode));

			String json = NetworkManager.post(APILinkMaker.mCheckActivateCode, pairs, false);
//			String json = readWholeFile(mContext, vn.smartguide2.R.raw.check_active_code);
			JSONObject jRoot = new JSONObject(json);

//			jRoot.getJSONObject("userProfile").put("name", "");
			
			return jRoot;
		} catch (Exception e) {
			mEx = e;
		}

		return super.doInBackground(arg0);
	}

	@Override
	protected void onCompleted(Object result2) {
		
		try {
			JSONObject result = (JSONObject) result2;
			if (result.getInt("status") != 0) {
				if (result.getJSONObject("userProfile").getString("name").length() == 0) {
					// first time
					onSuccessFirstTime(result);
				} else {
					// Success
					onSuccess(result);
				}
			} else {
				// Reject
				onReject(result);
			}
		} catch (Exception e) {
			onFail(e);
		}
	}
	
	protected void onSuccessFirstTime(JSONObject result) {}
	protected void onSuccess(JSONObject result) {}
	protected void onReject(JSONObject result) {}

}