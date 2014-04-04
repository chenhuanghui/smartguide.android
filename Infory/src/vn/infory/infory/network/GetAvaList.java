package vn.infory.infory.network;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;

import vn.infory.infory.R;
import vn.infory.infory.data.Settings;
import vn.infory.infory.data.Shop;
import android.content.Context;

import com.cycrix.jsonparser.JsonParser;
import com.cycrix.jsonparser.JsonArray.FailBehavior;

public class GetAvaList extends CyAsyncTask {

	public GetAvaList(Context c) {
		super(c);
	}
	
	@Override
	protected Object doInBackground(Object... arg0) {

		try {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			
			String json = NetworkManager.post(APILinkMaker.mGetAvatarList, pairs);
//			String json = readWholeFile(mContext, R.raw.place_list);
			
			if (json.equalsIgnoreCase("null"))
				json = "[]";
			ArrayList<String> avaList = new ArrayList<String>();
			JsonParser.parseArray(avaList, String.class, new JSONArray(json), FailBehavior.Throw);
			
			return avaList;
		} catch (Exception e) {
			mEx = e;
		}
	
		return super.doInBackground(arg0);
	}
}