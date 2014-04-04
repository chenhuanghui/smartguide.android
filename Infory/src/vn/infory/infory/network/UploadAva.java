package vn.infory.infory.network;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONObject;

import vn.infory.infory.data.Profile;
import vn.infory.infory.data.Settings;
import android.content.Context;

import com.cycrix.jsonparser.JsonParser;

public class UploadAva extends CyAsyncTask {
	
	private String avaFilePath;
	private String accessToken;

	public UploadAva(Context c, String avaFilePath) {
		super(c);
		
		this.avaFilePath = avaFilePath;
	}
	
	public UploadAva(Context c, String accessToken, String avaFilePath) {
		super(c);
		
		this.avaFilePath = avaFilePath;
		
		this.accessToken = accessToken;
	}

	@Override
	protected Object doInBackground(Object... arg0) {

		try {
			Settings s = Settings.instance();
			MultipartEntity reqEntity = new MultipartEntity();
			FileBody bin = new FileBody(new File(avaFilePath), "image/jpeg");
			reqEntity.addPart("avatar", bin);
			reqEntity.addPart("userLat", new StringBody("" + s.lat, Charset.defaultCharset()));
			reqEntity.addPart("userLng", new StringBody("" + s.lng, Charset.defaultCharset()));
			
			String json = NetworkManager.post(APILinkMaker.mUploadAva, reqEntity, true, accessToken);
			JSONObject jResponse = new JSONObject(json);
			
			return jResponse;
		} catch (Exception e) {
			mEx = e;
		}
	
		return super.doInBackground(arg0);
	}
}
