package vn.infory.infory.network;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import vn.infory.infory.CyLogger;
import vn.infory.infory.data.Settings;
import vn.infory.infory.data.home.HomeItem;
import vn.infory.infory.data.home.HomeItem_BranchPromoInfo;
import vn.infory.infory.data.home.HomeItem_Header;
import vn.infory.infory.data.home.HomeItem_ImageList;
import vn.infory.infory.data.home.HomeItem_PlaceListList;
import vn.infory.infory.data.home.HomeItem_ShopItem;
import vn.infory.infory.data.home.HomeItem_ShopList;
import vn.infory.infory.data.home.HomeItem_StoreItem;
import vn.infory.infory.data.home.HomeItem_StoreList;
import android.content.Context;

import com.cycrix.jsonparser.JsonParser;

public class GetHome extends CyAsyncTask {

	// Data
	private int mPage;
	
	CyLogger mLog = new CyLogger("CycrixDebug", true);

	public GetHome(Context c, int page) {
		super(c);

		mPage = page;
	}

	@Override
	public void setPage(int page) {
		mPage = page;
	}

	@Override
	public GetHome clone() {
		return new GetHome(mContext, mPage);
	}

	@Override
	protected Object doInBackground(Object... arg0) {

		try {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();

			Settings s = Settings.instance();
			if (s.lat != -1 || s.lng != -1){
				pairs.add(new BasicNameValuePair("userLat", Float.toString(s.lat)));
				pairs.add(new BasicNameValuePair("userLng", Float.toString(s.lng)));
			}
			pairs.add(new BasicNameValuePair("page", Integer.toString(mPage)));

			String json = NetworkManager.post(APILinkMaker.mHome, pairs);
//			String json = readWholeFile(mContext, R.raw.home);

			if (json.equalsIgnoreCase("null"))
				json = "[]";

			JSONArray jArr = new JSONArray(json);
			ArrayList<HomeItem> homeItemList = new ArrayList<HomeItem>();

			for (int i = 0; i < jArr.length(); i++) {
				JSONObject jHomeItem = jArr.getJSONObject(i);
				int type = jHomeItem.optInt("type", 0);
				HomeItem homeItem = null;
				switch (type) {
				case 1:
					homeItem = new HomeItem_BranchPromoInfo();	// v
					break;
				case 2:
					homeItem = new HomeItem_ImageList();		// v
					break;
				case 3:
					homeItem = new HomeItem_PlaceListList();	// v
					break;
				case 4:
					homeItem = new HomeItem_ShopList();			// v
					break;
				case 5:
					homeItem = new HomeItem_StoreList();
					break;
				case 6:
					homeItem = new HomeItem_ShopItem();			// v
					break;
				case 7:
					homeItem = new HomeItem_StoreItem();		// v
					break;
				case 8:
					homeItem = new HomeItem_ShopItem();
					break;
				case 9:
					if(jHomeItem.has("title"))
						homeItem = new HomeItem_Header();
					else
						homeItem = new HomeItem_ImageList();
//					homeItem = new HomeItem_Header();
					break;
				}

				if (homeItem != null) {
					JsonParser.parseObject(homeItem, jHomeItem);
					if(type == 9)
					{
						if(jHomeItem.has("title"))
						{						
							homeItem.type = 9;
						}
						else
						{
							homeItem.type = 2;
						}
					}											
					mLog.d("deviceInfo: " + homeItem.toString() + " Type: "+homeItem.type);
					homeItemList.add(homeItem);
				}
			}

			return homeItemList;
		} catch (Exception e) {
			mEx = e;
		}

		return super.doInBackground(arg0);
	}
}
