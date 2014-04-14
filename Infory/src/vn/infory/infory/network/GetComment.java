package vn.infory.infory.network;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;

import vn.infory.infory.R;
import vn.infory.infory.data.Comment;
import vn.infory.infory.data.Settings;
import vn.infory.infory.data.ShopGallery;
import android.content.Context;

import com.cycrix.jsonparser.JsonArray.FailBehavior;
import com.cycrix.jsonparser.JsonParser;

public class GetComment extends CyAsyncTask {

	// Data
	private int mPage;
	private String mId;
	private int mSort;

	public GetComment(Context c, String shopId, int page, int sort) {
		super(c);
		
		mPage = page;
		mId = shopId;
		mSort = sort;
	}
	
	@Override
	public void setPage(int page) {
		mPage = page;
	}
	
	public void setSort(int sort) {
		mSort = sort;
	}
	
	@Override
	public GetComment clone() {
		return new GetComment(mContext, mId, mPage, mSort);
	}

	@Override
	protected Object doInBackground(Object... arg0) {

		try {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			
			Settings s = Settings.instance();
			
			pairs.add(new BasicNameValuePair("idShop", mId));
			pairs.add(new BasicNameValuePair("userLat", Float.toString(s.lat)));
			pairs.add(new BasicNameValuePair("userLng", Float.toString(s.lng)));
			pairs.add(new BasicNameValuePair("page", Integer.toString(mPage)));
			pairs.add(new BasicNameValuePair("sort", Integer.toString(mSort)));
			
			String json = NetworkManager.post(APILinkMaker.mComment, pairs);
//			String json = readWholeFile(mContext, R.raw.comment);
			
			if (json.equalsIgnoreCase("null"))
				json = "[]";
			ArrayList<Comment> commentList = new ArrayList<Comment>();
			JsonParser.parseArray(commentList, Comment.class, new JSONArray(json), FailBehavior.Throw);
			
			return commentList;
		} catch (Exception e) {
			mEx = e;
		}
	
		return super.doInBackground(arg0);
	}
}