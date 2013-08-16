package vn.redbase.smartguide;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Category {
	public int mID;
	public String mName = "";
	public int mNum;

	public Category(int id, String name, int num){
		mID = id;
		mName = name;
		mNum = num;
	}

	public static List<Category> getListCategory(String jsonString) throws JSONException {
		List<Category> listCategories = new ArrayList<Category>();
		try{
			JSONArray shopArry = new JSONArray(jsonString);

			for(int i = 0; i < shopArry.length();i++){
				JSONObject object = (JSONObject)shopArry.get(i);
				int id = object.getInt("id");
				String name = object.getString("name");
				int count = object.getInt("count");

				Category mCategory = new Category(id, name, count);
				listCategories.add(mCategory);
			}
		}catch(Exception ex){
		}
		
		return listCategories;
	}
}
