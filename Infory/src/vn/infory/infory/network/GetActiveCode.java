package vn.infory.infory.network;

import org.json.JSONObject;

import vn.infory.infory.R;

import android.content.Context;

public class GetActiveCode extends CyAsyncTask {

	// Data
	private String mMessage;
	private String mPhoneNum;

	public GetActiveCode(Context c, String phoneNum) {
		super(c);
		
		mPhoneNum = phoneNum;
	}

	@Override
	protected Object doInBackground(Object... arg0) {

		try {
			String json = NetworkManager.get(APILinkMaker.mGetActivateCode + mPhoneNum, false);
//			String json = readWholeFile(mContext, R.raw.get_active_code);
			JSONObject jRoot = new JSONObject(json);
			if (!jRoot.getBoolean("result"))
				mMessage = jRoot.getString("message");
			return mMessage;
		} catch (Exception e) {
			mEx = e;
		}
	
		return super.doInBackground(arg0);
	}
}