package vn.smartguide;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class Review {
	int mID;
	int mUserID;
	String mFeedback;
	String mFirstName;
	String mLastName;
	
	public Review(int id, int userid, String feedback, String firstName, String lastName){
		mID = id;
		mUserID = userid;
		mFeedback = feedback;
		mFirstName = firstName;
		mLastName = lastName;
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
				String firstname = object.getString("first_name");
				String lastname = object.getString("last_name");
				result.add(new Review(id, userid, feedback, firstname, lastname));
			}
		}catch(Exception ex){
			return result;
		}
		return result;
	}
}
