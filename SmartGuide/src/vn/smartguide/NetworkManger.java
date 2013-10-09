package vn.smartguide;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
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
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

public final class NetworkManger {
	
	public static HttpParams params;
	public static ClientConnectionManager ccm;
	public static HttpClient httpclient;
	
	public static void init() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, KeyManagementException, UnrecoverableKeyException{
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
	}
	
	public static String post(String URL, List<NameValuePair> pairs){
		String result = "";
		try {
			String fullURL = URL + "?access_token=" + GlobalVariable.tokenID + GlobalVariable.footerURL;

			HttpPost httppost = new HttpPost(fullURL);
			httppost.setEntity(new UrlEncodedFormEntity(pairs, "utf-8"));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			result = EntityUtils.toString(entity);

			try{
				JSONObject error = new JSONObject(result);
				
				if (error.getString("error_description").compareTo("The access token provided has expired.") == 0 &&
						error.getString("error").compareTo("invalid_grant") == 0){
					GlobalVariable.smartGuideDB.updateToken(GlobalVariable.getRefreshIDViaOAuth2());
					return post(URL, pairs);
				}
			}catch(Exception ex){
			}

		} catch (Exception e) {
			return result;
		}
		return result;
	}
	
	public static String post_throw(String URL, List<NameValuePair> pairs) throws Exception {
		String result = "";
		String fullURL = URL + "?access_token=" + GlobalVariable.tokenID + GlobalVariable.footerURL;

		HttpPost httppost = new HttpPost(fullURL);
		httppost.setEntity(new UrlEncodedFormEntity(pairs, "utf-8"));
		HttpResponse response = httpclient.execute(httppost);
		HttpEntity entity = response.getEntity();
		result = EntityUtils.toString(entity);

		try {
			JSONObject error = new JSONObject(result);

			if (error.getString("error_description").compareTo("The access token provided has expired.") == 0 &&
					error.getString("error").compareTo("invalid_grant") == 0){
				GlobalVariable.smartGuideDB.updateToken(GlobalVariable.getRefreshIDViaOAuth2());
				return post_throw(URL, pairs);
			}
		} catch (JSONException ex) {

		}
		return result;
	}

	
	public static String get(String URL, boolean isNeedOauth){
		String result = "";
		
		try {
			String fullURL = "";
			if (isNeedOauth)
				fullURL = URL + "?access_token=" + GlobalVariable.tokenID + GlobalVariable.footerURL;
			else
				fullURL = URL;
			
			HttpGet httpGet = new HttpGet(fullURL);
			HttpResponse response = httpclient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			result = EntityUtils.toString(entity);

			try{
				JSONObject error = new JSONObject(result);
				if (error.getString("error_description").compareTo("The access token provided has expired.") == 0 &&
						error.getString("error").compareTo("invalid_grant") == 0) {
					GlobalVariable.smartGuideDB.updateToken(GlobalVariable.getRefreshIDViaOAuth2());
					return get(URL, isNeedOauth);
				}
			}catch(Exception ex) {
				ex.getMessage();
			}

		} catch (Exception e) {
			return result;
		}
		return result;
	}
	
	public static String get_throw(String URL, boolean isNeedOauth) throws ClientProtocolException, IOException {
	
		String result = "";
		String fullURL = "";
		if (isNeedOauth)
			fullURL = URL + "?access_token=" + GlobalVariable.tokenID + GlobalVariable.footerURL;
		else
			fullURL = URL;

		HttpGet httpGet = new HttpGet(fullURL);
		HttpResponse response = httpclient.execute(httpGet);
		HttpEntity entity = response.getEntity();
		result = EntityUtils.toString(entity);

		try {
			JSONObject error = new JSONObject(result);
			if (error.getString("error_description").compareTo("The access token provided has expired.") == 0 &&
					error.getString("error").compareTo("invalid_grant") == 0) {
				GlobalVariable.smartGuideDB.updateToken(GlobalVariable.getRefreshIDViaOAuth2());
				return get(URL, isNeedOauth);
			}
		} catch (JSONException ex) { }
		return result;
	}
	
	public static String postWithoutTail(String URL, List<NameValuePair> pairs){
		String result = "";
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(URL);
			httppost.setEntity(new UrlEncodedFormEntity(pairs));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			result = EntityUtils.toString(entity);
			
		} catch (Exception e) {
			return result;
		}
		return result;
	}
}
