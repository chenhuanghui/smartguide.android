package vn.smartguide;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class Review {
	int mID;
	int mUserID;
	String mFeedback;
	
	public Review(int id, int userid, String feedback){
		mID = id;
		mUserID = userid;
		mFeedback = feedback;
	}
	
	public static List<Review> getList(String json){
		List<Review> result = new ArrayList<Review>();
		try{
			JSONArray array = new JSONArray(json);
			for(int i = 0; i < array.length(); i++){
				JSONObject object = array.getJSONObject(i);
				int id = object.getInt("id");
				int userid = object.getInt("user_id");
				String feedback = object.getString("feedback");
				result.add(new Review(id, userid, feedback));
			}
		}catch(Exception ex){
			return result;
		}
		return result;
	}
}
