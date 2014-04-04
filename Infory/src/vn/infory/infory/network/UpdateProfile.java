package vn.infory.infory.network;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import vn.infory.infory.CyUtils;
import android.content.Context;

public class UpdateProfile extends CyAsyncTask {
	
	private String username, avaUrl;
	private int day, month, year, sex, socialType;
	private String accessToken;

	public UpdateProfile(Context c, String username, String avaUrl,
			int day, int month, int year, int sex, int socialType) {
		super(c);
		
		this.username 	= username;
		this.avaUrl		= avaUrl;
		this.day		= day;
		this.month		= month;
		this.year		= year;
		this.sex		= sex;
		this.socialType	= socialType;
	}
	
	public UpdateProfile(Context c, String accessToken, String username, String avaUrl,
			int day, int month, int year, int sex, int socialType) {
		super(c);
		
		this.username 	= username;
		this.avaUrl		= avaUrl;
		this.day		= day;
		this.month		= month;
		this.year		= year;
		this.sex		= sex;
		this.socialType	= socialType;
		
		this.accessToken= accessToken;
	}
	
	@Override
	protected Object doInBackground(Object... arg0) {

		try {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			
			pairs.add(new BasicNameValuePair("name", username));
			if (avaUrl != null)
				pairs.add(new BasicNameValuePair("avatar", avaUrl));
			pairs.add(new BasicNameValuePair("gender", "" + sex));
			pairs.add(new BasicNameValuePair("socialType", "" + socialType));
			String dobStr;
			if (day == 0 && month == 0 && year == 0)
				dobStr = "";
			else
				dobStr = new SimpleDateFormat(CyUtils.DOB_FORMAT, Locale.US)
				.format(new Date(year - 1900, month - 1, day));
			pairs.add(new BasicNameValuePair("dob", dobStr));
			
			String respose = NetworkManager.post(APILinkMaker.mUpdateProfile, 
					new UrlEncodedFormEntity(pairs, "utf-8"), true, accessToken);
			
			JSONObject jResponse = new JSONObject(respose);
			
			return jResponse;
		} catch (Exception e) {
			mEx = e;
		}
	
		return super.doInBackground(arg0);
	}
}