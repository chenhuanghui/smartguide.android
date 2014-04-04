package vn.infory.infory.network;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import vn.infory.infory.data.Settings;
import vn.infory.infory.data.Shop;
import android.content.Context;

import com.cycrix.jsonparser.JsonParser;

public class GetShopDetail extends CyAsyncTask {

	// Data
	private int mId;
	private Shop mShop;

	public GetShopDetail(Context c, Shop shop, int id) {
		super(c);
		
		mId = id;
		mShop = shop;
	}

	@Override
	protected Object doInBackground(Object... arg0) {

		try {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			
			Settings s = Settings.instance();
			pairs.add(new BasicNameValuePair("idShop", Integer.toString(mId)));
			pairs.add(new BasicNameValuePair("userLat", Float.toString(s.lat)));
			pairs.add(new BasicNameValuePair("userLng", Float.toString(s.lng)));
			
			String json = NetworkManager.post(APILinkMaker.mShopDetail, pairs);
//			String json = readWholeFile(mContext, R.raw.shop_detail_promo2);
			
			JsonParser.parseObject(mShop, json);
			
			if (mShop.shopGallery.size() > 0) {
				mShop.shopGalleryFirst = mShop.shopGallery.get(0);
			}
			
//			Thread.sleep(1000);
			
			super.doInBackground(arg0);
			return mShop;
		} catch (Exception e) {
			mEx = e;
		}
	
		return super.doInBackground(arg0);
	}
}