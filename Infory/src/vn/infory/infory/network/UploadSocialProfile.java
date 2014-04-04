package vn.infory.infory.network;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.content.Context;

public class UploadSocialProfile extends CyAsyncTask {
	
	private String profile, accessToken;
	private int socialType;
	private String tokenID;
	
	public UploadSocialProfile(Context c, String profile, 
			String accessToken, int socialType) {
		super(c);
		
		this.profile	= profile;
		this.accessToken= accessToken;
		this.socialType	= socialType;
	}

	public UploadSocialProfile(Context c, String tokenID, String profile, 
			String accessToken, int socialType) {
		super(c);
		
		this.profile	= profile;
		this.accessToken= accessToken;
		this.socialType	= socialType;
		
		this.tokenID 	= tokenID;
	}
	
	@Override
	protected Object doInBackground(Object... arg0) {

		try {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			
			pairs.add(new BasicNameValuePair("profile", profile));
			pairs.add(new BasicNameValuePair("accessToken", accessToken));
			pairs.add(new BasicNameValuePair("socialType", "" + socialType));
			
			String response = NetworkManager.post(APILinkMaker.mUploadSocialProfile,
					new UrlEncodedFormEntity(pairs, "utf-8"), true, tokenID);
			JSONObject jResponse = new JSONObject(response);
			
			return jResponse;
		} catch (Exception e) {
			mEx = e;
		}
	
		return super.doInBackground(arg0);
	}
}