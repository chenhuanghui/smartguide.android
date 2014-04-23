package vn.infory.infory.network;

import java.io.File;
import java.nio.charset.Charset;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONObject;

import vn.infory.infory.data.Settings;
import android.content.Context;

public class PostPhotoBinary extends CyAsyncTask {
	
	private String photoFilePath;
	private int shopId;

	public PostPhotoBinary(Context c, String photoFilePath, int shopId) {
		super(c);
		
		this.photoFilePath 	= photoFilePath;
		this.shopId 		= shopId;
	}

	@Override
	protected Object doInBackground(Object... arg0) {

		try {
			Settings s = Settings.instance();
			MultipartEntity reqEntity = new MultipartEntity();
			FileBody bin = new FileBody(new File(photoFilePath), "image/jpeg");
			reqEntity.addPart("image", bin);
			reqEntity.addPart("userLat", new StringBody("" + s.lat, Charset.defaultCharset()));
			reqEntity.addPart("userLng", new StringBody("" + s.lng, Charset.defaultCharset()));
			reqEntity.addPart("idShop", new StringBody("" + shopId));
			
			String json = NetworkManager.post(APILinkMaker.mPostPhotoBinary, reqEntity, true,
					s.getAccessToken());
			JSONObject jResponse = new JSONObject(json);
			
			return jResponse;
		} catch (Exception e) {
			mEx = e;
		}
	
		return super.doInBackground(arg0);
	}
}
