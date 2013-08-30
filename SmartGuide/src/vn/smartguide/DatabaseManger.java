package vn.smartguide;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseManger extends SQLiteOpenHelper {
	public DatabaseManger(Context applicationcontext) {
		super(applicationcontext, GlobalVariable.databaseName, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		String query;
		query = "CREATE TABLE " + "token" + " (tokenID TEXT PRIMARY KEY, refreshID TEXT)";
		database.execSQL(query);
		query = "CREATE TABLE " + "activateCode" + " (activateID TEXT PRIMARY KEY, userID TEXT, phoneNumber TEXT, avatar TEXT, nameFace Text)";
		database.execSQL(query);
		query = "CREATE TABLE " + "facebook" + " (userID TEXT PRIMARY KEY, avatar TEXT, name TEXT)";
		database.execSQL(query);
		query = "CREATE TABLE " + "city" + " (cityID TEXT PRIMARY KEY, name TEXT, googlename TEXT)";
		database.execSQL(query);
		query = "CREATE TABLE " + "userSetting" + " (versionID TEXT PRIMARY KEY, cityID TEXT)";
		database.execSQL(query);
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int version_old, int current_version) {
		String query;
		query = "DROP TABLE IF EXISTS " + "token";
		database.execSQL(query);
		query = "DROP TABLE IF EXISTS " + "activateCode";
		database.execSQL(query);
		query = "DROP TABLE IF EXISTS " + "facebook";
		database.execSQL(query);
		query = "DROP TABLE IF EXISTS " + "city";
		database.execSQL(query);
		query = "DROP TABLE IF EXISTS " + "userSetting";
		database.execSQL(query);
		onCreate(database);
	}
	
	public void insertVersion(HashMap<String, String> queryValues) {
		SQLiteDatabase database = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("versionID", queryValues.get("versionID"));
		values.put("cityID", queryValues.get("cityID"));
		
		database.insert("userSetting", null, values);
		database.close();
	}
	
	public void insertCity(HashMap<String, String> queryValues) {
		SQLiteDatabase database = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("cityID", queryValues.get("cityID"));
		values.put("name", queryValues.get("name"));
		values.put("googlename", queryValues.get("googlename"));
		
		database.insert("city", null, values);
		database.close();
	}
	
	public void insertActivateCode(HashMap<String, String> queryValues) {
		SQLiteDatabase database = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("activateID", queryValues.get("activateID"));
		values.put("userID", queryValues.get("userID"));
		values.put("phoneNumber", queryValues.get("phoneNumber"));
		values.put("avatar", queryValues.get("avatar"));
		values.put("nameFace", queryValues.get("nameFace"));
		
		GlobalVariable.footerURL = "&phone=" + queryValues.get("phoneNumber") + "&code=" + queryValues.get("activateID");
		database.insert("activateCode", null, values);
		database.close();
	}
	
	public void insertToken(HashMap<String, String> queryValues) {
		SQLiteDatabase database = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("tokenID", queryValues.get("tokenID"));
		values.put("refreshID", queryValues.get("refreshID"));
		database.insert("token", null, values);
		database.close();
	}

	public void insertFacebook(HashMap<String, String> queryValues) {
		SQLiteDatabase database = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("userID", queryValues.get("userID"));
		values.put("avatar", queryValues.get("avatar"));
		values.put("name", queryValues.get("name"));
		database.insert("facebook", null, values);
		database.close();
	}
	
	public void updateToken(HashMap<String, String> queryValues) {
		deleteToken(); 
		insertToken(queryValues);
	}

	public void deleteUserSetting() {
		SQLiteDatabase database = this.getWritableDatabase();  
		String deleteQuery = "DELETE FROM userSetting";
		database.execSQL(deleteQuery);
	}
	
	public void deleteToken() {
		SQLiteDatabase database = this.getWritableDatabase();  
		String deleteQuery = "DELETE FROM token";
		database.execSQL(deleteQuery);
	}

	public void deleteFacebook(){
		SQLiteDatabase database = this.getWritableDatabase();  
		String deleteQuery = "DELETE FROM facebook";
		database.execSQL(deleteQuery);
	}
	
	public void deleteCity(){
		SQLiteDatabase database = this.getWritableDatabase();  
		String deleteQuery = "DELETE FROM city";
		database.execSQL(deleteQuery);
	}
	
	public HashMap<String, String> getToken() {
		ArrayList<HashMap<String, String>> wordList;
		wordList = new ArrayList<HashMap<String, String>>();

		String selectQuery = "SELECT * FROM token";
		SQLiteDatabase database = this.getWritableDatabase();
		Cursor cursor = database.rawQuery(selectQuery, null);
		if (cursor.moveToFirst()) {
			do {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("tokenID", cursor.getString(0));
				map.put("refreshID", cursor.getString(1));
				wordList.add(map);
			} while (cursor.moveToNext());
		}

		if (wordList == null || wordList.size() == 0)
			return null;

		return wordList.get(0);
	}
	
	public HashMap<String, String> getActivateCode() {
		ArrayList<HashMap<String, String>> wordList;
		wordList = new ArrayList<HashMap<String, String>>();

		String selectQuery = "SELECT * FROM activateCode";
		SQLiteDatabase database = this.getWritableDatabase();
		Cursor cursor = null;
		try{
			cursor = database.rawQuery(selectQuery, null);
		}catch(Exception ex){
			ex.getMessage();
		}
		if (cursor.moveToFirst()) {
			do {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("activateID", cursor.getString(0));
				map.put("userID", cursor.getString(1));
				map.put("phoneNumber", cursor.getString(2));
				map.put("avatar", cursor.getString(3));
				map.put("nameFace", cursor.getString(4));
				wordList.add(map);
			} while (cursor.moveToNext());
		}

		if (wordList == null || wordList.size() == 0)
			return null;

		return wordList.get(0);
	}
	
	public HashMap<String, String> getFacebook() {
		ArrayList<HashMap<String, String>> wordList;
		wordList = new ArrayList<HashMap<String, String>>();

		String selectQuery = "SELECT * FROM facebook";
		SQLiteDatabase database = this.getWritableDatabase();
		Cursor cursor = database.rawQuery(selectQuery, null);
		if (cursor.moveToFirst()) {
			do {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("userID", cursor.getString(0));
				map.put("avatar", cursor.getString(1));
				map.put("name", cursor.getString(2));
				wordList.add(map);
			} while (cursor.moveToNext());
		}

		if (wordList == null || wordList.size() == 0)
			return null;

		return wordList.get(0);
	}
	
	public ArrayList<HashMap<String, String>> getCity() {
		ArrayList<HashMap<String, String>> wordList;
		wordList = new ArrayList<HashMap<String, String>>();

		String selectQuery = "SELECT * FROM city";
		SQLiteDatabase database = this.getWritableDatabase();
		Cursor cursor = database.rawQuery(selectQuery, null);
		if (cursor.moveToFirst()) {
			do {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("cityID", cursor.getString(0));
				map.put("name", cursor.getString(1));
				map.put("googlename", cursor.getString(2));
				wordList.add(map);
			} while (cursor.moveToNext());
		}

		if (wordList == null || wordList.size() == 0)
			return null;

		return wordList;
	}
	
	public HashMap<String, String> getVersion() {
		ArrayList<HashMap<String, String>> wordList;
		wordList = new ArrayList<HashMap<String, String>>();

		String selectQuery = "SELECT * FROM userSetting";
		SQLiteDatabase database = this.getWritableDatabase();
		Cursor cursor = database.rawQuery(selectQuery, null);
		if (cursor.moveToFirst()) {
			do {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("versionID", cursor.getString(0));
				map.put("cityID", cursor.getString(1));
				wordList.add(map);
			} while (cursor.moveToNext());
		}

		if (wordList == null || wordList.size() == 0)
			return null;

		return wordList.get(0);
	}
}