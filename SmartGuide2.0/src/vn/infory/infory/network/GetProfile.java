package vn.infory.infory.network;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;

import vn.infory.infory.data.Profile;
import android.content.Context;

import com.cycrix.jsonparser.JsonParser;

public class GetProfile extends CyAsyncTask {
	
	private String accessToken;

	public GetProfile(Context c) {
		super(c);
	}
	
	public GetProfile(Context c, String accessToken) {
		super(c);
		
		this.accessToken = accessToken;
	}

	@Override
	protected Object doInBackground(Object... arg0) {

		try {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			
			String json = NetworkManager.post(APILinkMaker.mProfile, 
					new UrlEncodedFormEntity(pairs, "utf-8"), true, accessToken);
			
			Profile profile = new Profile();
			JsonParser.parseObject(profile, json);
			
			return profile;
		} catch (Exception e) {
			mEx = e;
		}
	
		return super.doInBackground(arg0);
	}
}
