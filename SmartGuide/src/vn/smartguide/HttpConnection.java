package vn.smartguide;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
 
/**
 * Asynchronous HTTP connections
 * 
 * @author Greg Zavitz & Joseph Roth
 */
public class HttpConnection implements Runnable {
 
    public static final int DID_START = 0;
    public static final int DID_ERROR = 1;
    public static final int DID_SUCCEED = 2;
    public static final int DID_STATUS = 3;
 
    private static final int GET = 0;
    private static final int POST = 1;
    private static final int PUT = 2;
    private static final int DELETE = 3;
    private static final int BITMAP = 4;
    private static final int GET_FILE = 5;
 
    private String url;
    private int method;
    private Handler handler;
    private String data;
    private Context ct;
 
    private HttpClient httpClient;
 
    public HttpConnection() {
    }
 
    public HttpConnection(Handler _handler) {
        handler = _handler;
    }
 
    public void create(int method, String url, String data) {
        this.method = method;
        this.url = url;
        this.data = data;
        
        if (handler != null)
        	ConnectionManager.getInstance().push(this);
    }
 
    public HttpConnection get(String url) {
        create(GET, url, null);
        return this;
    }
 
    public HttpConnection post(String url, String data) {
        create(POST, url, data);
        return this;
    }
 
    public void put(String url, String data) {
        create(PUT, url, data);
    }
 
    public void delete(String url) {
        create(DELETE, url, null);
    }
 
    public void bitmap(String url) {
        create(BITMAP, url, null);
    }
    
    public void getFile(String url, Context ct) {
    	this.ct = ct;
    	create(GET_FILE, url, null);
    }
 
    public void run() {
    	if (handler != null)
    		handler.sendMessage(Message.obtain(handler,
    				HttpConnection.DID_START));
    	httpClient = new DefaultHttpClient();
    	HttpParams params = httpClient.getParams();
    	HttpConnectionParams.setSoTimeout(params, 10000);
    	HttpConnectionParams.setConnectionTimeout(params, 10000);

        try {
            HttpResponse response = null;
            switch (method) {
            case GET:
                response = httpClient.execute(new HttpGet(url));
                break;
            case POST:
                HttpPost httpPost = new HttpPost(url);
                httpPost.setEntity(new StringEntity(data));
                httpPost.addHeader("Content-Type", "text/html; charset=UTF-8");
                response = httpClient.execute(httpPost);
                break;
            case PUT:
                HttpPut httpPut = new HttpPut(url);
                httpPut.setEntity(new StringEntity(data));
                response = httpClient.execute(httpPut);
                break;
            case DELETE:
                response = httpClient.execute(new HttpDelete(url));
                break;
            case BITMAP:
                response = httpClient.execute(new HttpGet(url));
                processBitmapEntity(response.getEntity());
                break;
            case GET_FILE:
            	response = httpClient.execute(new HttpGet(url));
                processGetFile(response.getEntity());
            	break;
            }
            if (method < BITMAP)
                processEntity(response.getEntity());
        } catch (Exception e) {
        	if (handler != null)
        		handler.sendMessage(Message.obtain(handler,
        				HttpConnection.DID_ERROR, e));
        }
        ConnectionManager.getInstance().didComplete(this);
    }
    
    public Object runSync() throws Exception {

    	httpClient = new DefaultHttpClient();
    	HttpParams params = httpClient.getParams();
    	HttpConnectionParams.setSoTimeout(params, 10000);
    	HttpConnectionParams.setConnectionTimeout(params, 10000);

    	HttpResponse response = null;
    	switch (method) {
    	case GET:
    		response = httpClient.execute(new HttpGet(url));
    		break;
    	case POST:
    		HttpPost httpPost = new HttpPost(url);
    		httpPost.setEntity(new StringEntity(data));
    		httpPost.addHeader("Content-Type", "text/html; charset=UTF-8");
    		response = httpClient.execute(httpPost);
    		break;
    	case PUT:
    		HttpPut httpPut = new HttpPut(url);
    		httpPut.setEntity(new StringEntity(data));
    		response = httpClient.execute(httpPut);
    		break;
    	case DELETE:
    		response = httpClient.execute(new HttpDelete(url));
    		break;
    	case BITMAP:
    		response = httpClient.execute(new HttpGet(url));
    		return processBitmapEntity(response.getEntity());
    	}
    	return processEntity(response.getEntity());
    }
 
    private String processEntity(HttpEntity entity) throws IllegalStateException,
            IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(entity
                .getContent()));
        String line, result = "";
        while ((line = br.readLine()) != null)
            result += line;
        
        if (handler != null) {
        	Message message = Message.obtain(handler, DID_SUCCEED, result);
        	handler.sendMessage(message);
        }
        
        return result;
    }
 
    private Bitmap processBitmapEntity(HttpEntity entity) throws IOException {
    	
        BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
        Bitmap bm = BitmapFactory.decodeStream(bufHttpEntity.getContent());
        if (handler != null) 
        	handler.sendMessage(Message.obtain(handler, DID_SUCCEED, bm));
        return bm;
    }
 
    private void processGetFile(HttpEntity entity) throws IOException {
    	
    	InputStreamReader in = new InputStreamReader(entity.getContent());
    	FileOutputStream jsonFile = ct.openFileOutput(
    			"songlist.json", Context.MODE_PRIVATE);
    	OutputStreamWriter out = new OutputStreamWriter(jsonFile);
    	
    	char[] buffer = new char[1024 * 5];
    	int justRead = 0;
    	int hasRead = 0;
    	while ((justRead = in.read(buffer)) != -1) {
    		out.write(buffer, 0, justRead);
    		hasRead += justRead;
    		handler.sendMessage(Message.obtain(handler, DID_STATUS, hasRead,
    				(int) entity.getContentLength()));
    	}
    	
    	handler.sendMessage(Message.obtain(handler, DID_SUCCEED, "songlist.json"));
    	
    	out.close();
    	jsonFile.close();
    	in.close();
    }
}