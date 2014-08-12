package vn.infory.infory.network;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;

import vn.infory.infory.data.City;
import vn.infory.infory.data.PlaceList;
import vn.infory.infory.data.Settings;

import com.cycrix.jsonparser.JsonArray;
import com.cycrix.jsonparser.JsonParser;

import vn.infory.infory.CyLogger;
import vn.infory.infory.R;
import android.content.Context;

public class GetCityList extends CyAsyncTask{
	
	CyLogger mLog = new CyLogger("CycrixDebug", true);

	public GetCityList(Context c) {
		super(c);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Object doInBackground(Object... arg0) {

		try {	
			String json = readWholeFile(mContext, R.raw.city_list);
			if (json.equalsIgnoreCase("null"))
				json = "[]";
			ArrayList<City> cityList = new ArrayList<City>();
			JsonParser.parseArray(cityList, City.class, new JSONArray(json), JsonArray.FAIL_BEHAVIOR_THROW);
			
			return cityList;
		} catch (Exception e) {
			mEx = e;
		}
	
		return super.doInBackground(arg0);
	}
}
