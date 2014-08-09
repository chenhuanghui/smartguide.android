package vn.infory.infory.network;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import vn.infory.infory.R;
import vn.infory.infory.data.Settings;
import vn.infory.infory.data.Shop;
import android.content.Context;

import com.cycrix.jsonparser.JsonArray;
import com.cycrix.jsonparser.JsonParser;

public class RegisterRegistrationID extends CyAsyncTask
{
	private String	registrationId;

	public RegisterRegistrationID(Context c, String registrationId)
	{
		super(c);
		this.registrationId = registrationId;
	}

	@Override
	protected Object doInBackground(Object... arg0)
	{

		try
		{
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("registrationId", registrationId));

			String json = NetworkManager.post(APILinkMaker.mRegisterIDGCM, pairs);

			JSONObject jRespose = new JSONObject(json);
			
			return jRespose;
		}
		catch (Exception e)
		{
			mEx = e;
		}

		return super.doInBackground(arg0);
	}
}