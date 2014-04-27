package vn.infory.infory.network;

import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONObject;

import com.cycrix.jsonparser.JsonArray;
import com.cycrix.jsonparser.JsonParser;

import vn.infory.infory.CyUtils;
import vn.infory.infory.data.AutoCompleteItem;

import android.content.Context;

public class AutoComplete extends CyAsyncTask {

	private String mKeyword;
	private JSONObject mRequestBody;

	public AutoComplete(Context c, String keyword) {
		super(c);

		mKeyword = keyword;
	}

	@Override
	protected Object doInBackground(Object... arg0) {

		try {
			if (mRequestBody == null)
				mRequestBody = new JSONObject(
						"{\"query\":{\"query_string\":{\"query\":\"a\",\"fields\":[\"shop_name_auto_complete\",\"name_auto_complete\"]}},\"highlight\":{\"fields\":{\"shop_name_auto_complete\":{},\"name_auto_complete\":{}}},\"from\":0,\"size\":5,\"fields\":[\"shop_name\",\"hasPromotion\",\"name\",\"id\"]}");

			mRequestBody.getJSONObject("query").getJSONObject("query_string")
					.put("query", CyUtils.covertToNonVietnamese(mKeyword));

			String httpDomain = APILinkMaker.mAutoComplete;
			if (httpDomain.startsWith("https"))
				httpDomain = "http" + APILinkMaker.mAutoComplete.substring(5);
			
			String json = NetworkManager.get(httpDomain
					+ "?source=" + URLEncoder.encode(mRequestBody.toString()), false);
			
			JSONObject root = new JSONObject(json);
			
			ArrayList<AutoCompleteItem> items = new ArrayList<AutoCompleteItem>();
			JsonParser.parseArray(items, AutoCompleteItem.class, 
					root.getJSONObject("hits").getJSONArray("hits"), JsonArray.FAIL_BEHAVIOR_THROW);
			super.doInBackground(arg0);
			return items;
		} catch (Exception e) {
			mEx = e;
		}

		return super.doInBackground(arg0);
	}
}
