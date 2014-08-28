package vn.infory.infory.network;

import java.security.KeyStore;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import vn.infory.infory.data.Settings;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public final class NetworkManager {

	private static final String TAG = "Infory NetworkManager";
	
	public static ThreadPoolExecutor THREAD_POOL;
	
	public static final String mHostName 	= "https://api.infory.vn";
//	public static final String mHostName 	= "http://dev2.smartguide.vn";
//	public static final String mHostName 	= "http://125.253.122.44/";
//	public static final String mHostName 	= "http://dev.infory.vn";

	public static final String mApiDomain 	= "/api/";
	public static final String serverOAuth 	= mHostName + "/oauth/v2/token";

	//	public static String getTokenURL = serverOAuth + grantType + "&client_id=" + clientID 
	//			+ "&client_secret=" + serectID; 

	public static String footerURL = "";

	public static HttpParams params;
	public static ClientConnectionManager ccm;
	public static HttpClient httpclient;

	public static void init() throws Exception {
		KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		trustStore.load(null, null);

		SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
		sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

		params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		registry.register(new Scheme("https", sf, 443));

		ccm = new ThreadSafeClientConnManager(params, registry);
		httpclient = new DefaultHttpClient(ccm, params);
		HttpParams params = httpclient.getParams();
		HttpConnectionParams.setSoTimeout(params, 30000);
		HttpConnectionParams.setConnectionTimeout(params, 30000);

		THREAD_POOL = new ThreadPoolExecutor(10, 10, 60, TimeUnit.SECONDS, 
				new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {

			@Override
			public Thread newThread(Runnable task) {
				Thread thread = new Thread(task);
				thread.setPriority(Thread.MIN_PRIORITY);
				return thread;
			}
		});
	}
	
	public static String post(String URL, List<NameValuePair> pairs) throws Exception {
		Log.e(TAG, "params sent: " + pairs.toString());
		return post(URL, pairs, true);
	}

	public static String post(String URL, List<NameValuePair> pairs, 
			boolean isNeedOauth) throws Exception {
		Log.e(TAG, "params sent: " + pairs.toString());
		return post(URL, new UrlEncodedFormEntity(pairs, "utf-8"), isNeedOauth, null);
	}
	
	public static String post(String URL, HttpEntity entity, 
			boolean isNeedOauth, String accessToken) throws Exception {
		String result = "";
		String fullURL = null;
		if (isNeedOauth) {
			if (accessToken != null)
				fullURL = URL + "?access_token=" + accessToken + footerURL;
			else
				fullURL = URL + "?access_token=" + Settings.instance().getAccessToken() + footerURL;
		} else {
			fullURL = URL;
		}

		Log.e(TAG, "fullURL: " + fullURL);
		HttpPost httppost = new HttpPost(fullURL);
		httppost.setEntity(entity);
		HttpResponse response = httpclient.execute(httppost);
		HttpEntity responseEntity = response.getEntity();
		result = EntityUtils.toString(responseEntity);

		if (!isNeedOauth)
			return result;

		Exception refreshTokenException = null;
		try {
			JSONObject error = new JSONObject(result);
			if (error.getString("error_description").equals("The access token provided has expired.") &&
					error.getString("error").compareTo("invalid_grant") == 0 &&
					accessToken == null) {
				try {
					getRefreshIDViaOAuth2();
					return post(URL, entity, isNeedOauth, null);
				} catch (Exception ex) {
					refreshTokenException = ex;
				}
			}
		} catch (Exception ex) { }

		if (refreshTokenException != null)
			throw refreshTokenException;
		Log.e(TAG, "post return result: " + result);
		return result;
	}

	public static String get(String URL, boolean isNeedOauth) throws Exception {

		String result = "";
		String fullURL = "";
		if (isNeedOauth)
			fullURL = URL + "?access_token=" + Settings.instance().getAccessToken() + footerURL;
		else
			fullURL = URL;

		HttpGet httpGet = new HttpGet(fullURL);
		HttpResponse response = httpclient.execute(httpGet);
		HttpEntity entity = response.getEntity();
		result = EntityUtils.toString(entity);

		if (!isNeedOauth)
			return result;

		Exception refreshTokenException = null;
		try {
			JSONObject error = new JSONObject(result);
			if (error.getString("error_description").compareTo("The access token provided has expired.") == 0 &&
					error.getString("error").compareTo("invalid_grant") == 0) {
				try {
					getRefreshIDViaOAuth2();
					return get(URL, isNeedOauth);
				} catch (Exception ex) {
					refreshTokenException = ex;
				}
			}
		} catch (Exception ex) { }

		if (refreshTokenException != null)
			throw refreshTokenException;

		return result;
	}

	public static boolean isOnline(Activity context) {
		ConnectivityManager cm =
				(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}

	public static void getRefreshIDViaOAuth2() throws Exception {

		HttpGet httpGet = new HttpGet(serverOAuth + "?grant_type=refresh_token&client_id=" + 
				getClientID() + "&client_secret=" + getSecretID() + "&refresh_token=" + 
				Settings.instance().getRefreshToken());
		JSONObject key;

		DefaultHttpClient httpClient = new DefaultHttpClient(ccm, params);
		HttpResponse httpResponse = httpClient.execute(httpGet);
		HttpEntity httpEntity = httpResponse.getEntity();
		key = new JSONObject(EntityUtils.toString(httpEntity));

		Settings.instance().setAccessToken(
				key.getString("access_token"), key.getString("refresh_token"));
		Settings.instance().save();
	}

	public static void getTokenIDViaOAuth2(Context ct) throws Exception {

		HttpGet httpGet = new HttpGet(serverOAuth + getGrantType() + "&client_id=" + getClientID()
				+ "&client_secret=" + getSecretID() + footerURL);
		HttpResponse httpResponse = NetworkManager.httpclient.execute(httpGet);
		HttpEntity httpEntity = httpResponse.getEntity();
		String json = EntityUtils.toString(httpEntity);

		//		String json = CyAsyncTask.readWholeFile(ct, R.raw.get_access_token).toString();

		JSONObject key = new JSONObject(json);

		Settings.instance().setAccessToken(
				key.getString("access_token"), key.getString("refresh_token"));
		Settings.instance().save();
	}

	private static String getClientID() {
		// TODO: Apply some encryptions here
		return "1_orazuv2dl3k8ossssg8804o4kwksw8kwcskkk404w40gwcwws";
	}

	private static String getSecretID() {
		// TODO: Apply some encryptions here
		return "4xvgf3r9dxs8k8g8o8k0gss0s0wc8so4g4wg40c8s44kgcwsks";
	}

	private static String getGrantType() {
		// TODO: Apply some encryptions here
		return "?grant_type=http://dev.smartguide.com/app_dev.php/grants/bingo";
	}

	private static String getTokenURL() {
		return serverOAuth + getGrantType() + "&client_id=" + getClientID() + "&client_secret=" + getSecretID();
	}
}