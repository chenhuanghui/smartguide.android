package vn.smartguide;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class Review {
	String mFeedback;
	String mName;
	
	public Review(String feedback, String name){
		mFeedback = feedback;
		mName = name;
	}
	
	public static List<Review> getList(JSONArray array){
		List<Review> result = new ArrayList<Review>();
		try{
			for(int i = 0; i < array.length(); i++){
				JSONObject object = array.getJSONObject(i);
				String feedback = object.getString("feedback");
				String name = object.getString("username");
				result.add(new Review(feedback, name));
			}
		}catch(Exception ex){
			return result;
		}
		return result;
	}
}
