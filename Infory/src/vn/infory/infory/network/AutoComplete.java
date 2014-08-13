package vn.infory.infory.network;

import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.cycrix.jsonparser.JsonParser;

import vn.infory.infory.CyUtils;
import vn.infory.infory.data.AutoCompleteItem;
import vn.infory.infory.data.Settings;
import android.content.Context;

public class AutoComplete extends CyAsyncTask {

	private String mKeyword;
	private JSONObject mRequestBody;
	private int mCityId;

	public AutoComplete(Context c, String keyword) {
		super(c);

		mKeyword = keyword;
		
		Settings s = vn.infory.infory.data.Settings.instance();
		int cityId = Integer.parseInt(s.cityId);
		mCityId = cityId;
	}

	@Override
	protected Object doInBackground(Object... arg0) {

		try {
			if (mRequestBody == null)
				/*mRequestBody = new JSONObject(
						"{\"query\":{\"query_string\":{\"query\":\"a\",\"fields\":[\"shop_name_auto_complete\",\"name_auto_complete\"]}},\"highlight\":{\"fields\":{\"shop_name_auto_complete\":{},\"name_auto_complete\":{}}},\"from\":0,\"size\":5,\"fields\":[\"shop_name\",\"hasPromotion\",\"name\",\"id\"]}");*/
				mRequestBody = new JSONObject("{\"query\":{\"filtered\":{\"query\":{\"query_string\":{\"query\":\"infory\",\"fields\":[\"shop_name_auto_complete\",\"name_auto_complete\"]}},\"filter\":{\"term\":{\"city\":83}}}},\"highlight\":{\"fields\":{\"shop_name_auto_complete\":{},\"name_auto_complete\":{}}},\"from\":0,\"size\":5,\"fields\":[\"shop_name\",\"hasPromotion\",\"name\",\"id\"]}");

			mRequestBody.getJSONObject("query").getJSONObject("filtered").getJSONObject("query").getJSONObject("query_string")
					.put("query", CyUtils.covertToNonVietnamese(mKeyword));
			
			mRequestBody.getJSONObject("query").getJSONObject("filtered").getJSONObject("filter").getJSONObject("term")
					.put("city", mCityId);
			

			String httpDomain = APILinkMaker.mAutoComplete;
			if (httpDomain.startsWith("https"))
				httpDomain = "http" + APILinkMaker.mAutoComplete.substring(5);
			
			String json = NetworkManager.get(httpDomain
					+ "?source=" + URLEncoder.encode(mRequestBody.toString()), false);
			
			//String json = "{\"took\":214,\"timed_out\":false,\"_shards\":{\"total\":5,\"successful\":5,\"failed\":0},\"hits\":{\"total\":5,\"max_score\":3.3379505,\"hits\":[{\"_index\":\"data\",\"_type\":\"shop\",\"_id\":\"23\",\"_score\":3.3379505,\"fields\":{\"id\":[23],\"hasPromotion\":[0],\"shop_name\":[\"gloria jean's coffees bitexco\"]},\"highlight\":{\"shop_name_auto_complete\":[\"<em>gloria</em> jean's coffees bitexco\"]}},{\"_index\":\"data\",\"_type\":\"shop\",\"_id\":\"24\",\"_score\":2.511835,\"fields\":{\"id\":[24],\"hasPromotion\":[0],\"shop_name\":[\"gloria jean's coffees - galaxy nguyen du\"]},\"highlight\":{\"shop_name_auto_complete\":[\"<em>gloria</em> jean's coffees - galaxy nguyen du\"]}},{\"_index\":\"data\",\"_type\":\"shop\",\"_id\":\"25\",\"_score\":2.5058265,\"fields\":{\"id\":[25],\"hasPromotion\":[0],\"shop_name\":[\"gloria jean's coffees  - ho con rua\"]},\"highlight\":{\"shop_name_auto_complete\":[\"<em>gloria</em> jean's coffees  - ho con rua\"]}},{\"_index\":\"data\",\"_type\":\"shop\",\"_id\":\"27\",\"_score\":2.5024462,\"fields\":{\"id\":[27],\"hasPromotion\":[0],\"shop_name\":[\"gloria jean's coffees - crescent mall quan 7\"]},\"highlight\":{\"shop_name_auto_complete\":[\"<em>gloria</em> jean's coffees - crescent mall quan 7\"]}},{\"_index\":\"data\",\"_type\":\"shop\",\"_id\":\"26\",\"_score\":2.0767639,\"fields\":{\"id\":[26],\"hasPromotion\":[0],\"shop_name\":[\"gloria jean's coffees  minh khai - nam ky khoi nghia\"]},\"highlight\":{\"shop_name_auto_complete\":[\"<em>gloria</em> jean's coffees  minh khai - nam ky khoi nghia\"]}}]}}";
			JSONObject root = new JSONObject(json);
			
			ArrayList<AutoCompleteItem> items = new ArrayList<AutoCompleteItem>();
//			JsonParser.parseArray(items, AutoCompleteItem.class, 
//					root.getJSONObject("hits").getJSONArray("hits"), JsonArray.FAIL_BEHAVIOR_THROW);
			
			JSONArray hitsArray =  root.getJSONObject("hits").getJSONArray("hits");
			for(int i = 0; i < hitsArray.length(); i++){
				AutoCompleteItem item = new AutoCompleteItem();
				JSONObject itemObj = hitsArray.getJSONObject(i);
				
				JSONObject itemObjReal = new JSONObject();
				
				JSONObject fieldsObj = new JSONObject();
				fieldsObj.put("id", itemObj.getJSONObject("fields").getJSONArray("id").get(0));
				fieldsObj.put("hasPromotion", itemObj.getJSONObject("fields").getJSONArray("id").get(0));
				fieldsObj.put("shop_name", itemObj.getJSONObject("fields").getJSONArray("id").get(0));
				
				itemObjReal.put("_type", itemObj.get("_type"));
				itemObjReal.put("fields", fieldsObj);
				itemObjReal.put("highlight", itemObj.get("highlight"));
				JsonParser.parseObject(item, itemObjReal);
				items.add(item);
			}
			
			super.doInBackground(arg0);
			return items;
		} catch (Exception e) {
			mEx = e;
		}

		return super.doInBackground(arg0);
	}
}
