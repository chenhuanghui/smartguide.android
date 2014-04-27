package com.cycrix.jsonparser;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonParser {

	public static void parseObject(Object obj, String json) throws Exception {
		JSONObject jObj = new JSONObject(json);
		parseObject(obj, jObj);
	}

	public static void parseObject(Object obj, JSONObject jObj) throws Exception {
		// Check object annotation
		Class cl = obj.getClass();
		if (cl.getAnnotation(JsonObject.class) == null)
			throw new JSONException("This object is not a JsonObject");

		// Scan all fields
		for (Field field : cl.getFields()) {

			// Check field annotation
			JsonBool boolAnno;
			JsonInt intAnno;
			JsonDouble doublelAnno;
			JsonString stringAnno;
			JsonObject objectAnno;
			JsonArray arrayAnno;
			JsonCustom customAnno;
			if ((boolAnno = field.getAnnotation(JsonBool.class)) != null) {
				// Parse annotation
				boolean opt = boolAnno.optional();
				boolean def = boolAnno.defaultValue();
				boolean val;
				// parse json
				if (opt)
					val = jObj.optBoolean(field.getName(), def);
				else
					val = jObj.getBoolean(field.getName());
				// write to object
				field.setBoolean(obj, val);
			} else if ((intAnno = field.getAnnotation(JsonInt.class)) != null) {
				// Parse annotation
				boolean opt = intAnno.optional();
				boolean ignore = intAnno.ignore();
				int def = intAnno.defaultValue();
				int val;
				// parse json
				if (opt)
					val = jObj.optInt(field.getName(), def);
				else
					try {
						val = jObj.getInt(field.getName());
					} catch (JSONException e) {
						if (ignore)
							continue;
						else
							throw e;
					}
				// write to object
				field.setInt(obj, val);
			} else if ((doublelAnno = field.getAnnotation(JsonDouble.class)) != null) {
				// Parse annotation
				boolean opt = doublelAnno.optional();
				double def = doublelAnno.defaultValue();
				double val;
				// parse json
				if (opt)
					val = jObj.optDouble(field.getName(), def);
				else
					val = jObj.getDouble(field.getName());
				// write to object
				field.setDouble(obj, val);
			} else if ((stringAnno = field.getAnnotation(JsonString.class)) != null) {
				// Parse annotation
				boolean opt = stringAnno.optional();
				boolean ignore = stringAnno.ignore();
				String def = stringAnno.defaultValue();
				String val;
				// parse json
				if (opt)
					val = jObj.optString(field.getName(), def);
				else {
					try {
//						if (jObj.isNull(field.getName()))
//							val = "";
//						else
						val = jObj.getString(field.getName());
						if (val.equals("null"))
							val = "";
					} catch (JSONException e) {
						if (ignore)
							continue;
						else
							throw e;
					}
				}
				// write to object
				field.set(obj, val);
			} else if ((objectAnno = field.getAnnotation(JsonObject.class)) != null) {
				// Parse annotation
				boolean opt = objectAnno.optional();
				boolean ignore = objectAnno.ignore();
				// Parse json
				JSONObject jSubObj = null;
				if (opt)
					jSubObj = jObj.optJSONObject(field.getName());
				else {
					try {
						jSubObj = jObj.getJSONObject(field.getName());
					} catch (JSONException e) {
						if (ignore)
							continue;
						else
							throw e;
					}
				}
				
				// write to object
				if (jSubObj == null) {
					field.set(obj, null);
				} else {
					Object o = field.getType().newInstance();
					try {
						parseObject(o, jSubObj);
					} catch (JSONException e) {
						if (ignore)
							continue;
						else
							throw e;
					}
					field.set(obj, o);
				}
			} else if ((arrayAnno = field.getAnnotation(JsonArray.class)) != null) {
				// Parse annotation
				Object fieldObj = field.get(obj);
				boolean isListClass = fieldObj instanceof List;
				Type genericType = field.getGenericType(); 
				Class genericClass = null;
				if (isListClass) {
					if (genericType instanceof ParameterizedType) {
						ParameterizedType aType = (ParameterizedType) genericType;
						Type[] fieldArgTypes = aType.getActualTypeArguments();
						genericClass = (Class) fieldArgTypes[0];
					}
				}

				if (!isListClass || genericClass == null)
					throw new JSONException("Field " + field.getName() + " is not a List<T>");

				if (!genericClass.isAnnotationPresent(JsonObject.class) &&
						genericClass != Boolean.class &&
						genericClass != Integer.class &&
						genericClass != Double.class &&
						genericClass != String.class)
					throw new JSONException("Required @JsonObject annotation or Boolean, Integer, Double for "
							+ genericClass.getName());

				boolean opt = arrayAnno.optional();
				boolean ignore = arrayAnno.ignore(); 
				int onFail = arrayAnno.onFail();
				// Parse json
				JSONArray jSubArr = null;
				if (opt)
					jSubArr = jObj.optJSONArray(field.getName());
				else {
					try {
						jSubArr = jObj.getJSONArray(field.getName());
					} catch (JSONException e) {
						if (ignore)
							continue;
						else
							throw e;
					}
				}

				if (jSubArr != null) {
					List<?> list = (List<?>) field.get(obj);
					parseArray(list, genericClass, jSubArr, onFail);
				}
			} else if ((customAnno = field.getAnnotation(JsonCustom.class)) != null) {
				// Parse annotation
				try {
					String methodName = customAnno.methodName();
					Method method = cl.getMethod(methodName, JSONObject.class);
					method.invoke(obj, jObj);
				} catch (InvocationTargetException e) {
					throw (Exception) e.getTargetException();
				}
			}
		}
	}

	public static <T> void parseArray(List<T> list, Class<? extends T> type, JSONArray jArr, int onFail) throws Exception {
		list.clear();

		for (int i = 0; i < jArr.length(); i++) {
			
			String shopId = null;
			String shopName = null;
			
			try {
				if (type == Boolean.class)
					list.add((T) new Boolean(jArr.getBoolean(i)));
				else if (type == Integer.class)
					list.add((T) new Integer(jArr.getInt(i)));
				else if (type == Double.class)
					list.add((T) new Double(jArr.getDouble(i)));
				else if (type == String.class)
					list.add((T) jArr.getString(i));
				else {
					//					list.add((T) jArr.get
					Object o = type.newInstance();
					JSONObject jItem = jArr.getJSONObject(i);
					if (jItem.has("id") && jItem.has("name")) {
						shopId = jItem.optString("id");
						shopName = jItem.optString("name");
					}
					parseObject(o, jItem);
					list.add((T) o);
				}
			} catch (JSONException e) {
				e.printStackTrace();
				switch (onFail) {
				case JsonArray.FAIL_BEHAVIOR_THROW:
					throw new JSONException(e.getMessage() + " ShopName=" + shopName + " ShopId=" + shopId);
				case JsonArray.FAIL_BEHAVIOR_BREAK:
					return;
				case JsonArray.FAIL_BEHAVIOR_PASS:
					break;
				}
			}
		}
	}
}