package vn.smartguide;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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

	public static List<Category> getListCategory(JSONArray shopArry) throws JSONException {
		List<Category> listCategories = new ArrayList<Category>();
		try{
			for(int i = 0; i < shopArry.length();i++){
				JSONObject object = (JSONObject)shopArry.get(i);
				int id = object.getInt("id");
				String name = object.getString("name");
				int count = object.getInt("count");

				Category mCategory = new Category(id, name, count);
				listCategories.add(mCategory);
			}
			Collections.sort(listCategories, new Comparator<Category>() {

				@Override
				public int compare(Category lhs, Category rhs) {
					return lhs.mID - rhs.mID;
				}
			});
		}catch(Exception ex){
		}
		
		return listCategories;
	}
}
