package vn.infory.infory.network;

import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;

import vn.infory.infory.CyLogger;
import vn.infory.infory.R;
import vn.infory.infory.data.Settings;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class CheckEmergence extends CyAsyncTask {

	CyLogger mLog = new CyLogger("CycrixDebug", true);

	public CheckEmergence(Context c) {
		super(c);
	}

	@Override
	protected Object doInBackground(Object... arg0) {

		try {
			String deviceInfo = getDeviceName();
			String url = APILinkMaker.mEmergency + "?access_token=" + Settings.instance().getAccessToken() 
					+ "&version=android"+ Build.VERSION.RELEASE + "_" 
					+ mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName
					+ "&deviceInfo=" + URLEncoder.encode(deviceInfo);
			
			mLog.d("deviceInfo: " + deviceInfo);
			String json = NetworkManager.get(url, false);
			//			String json = readWholeFile(mContext, R.raw.notification);
			JSONObject jRoot = new JSONObject(json);

			return jRoot;
		} catch (Exception e) {
			mEx = e;
		}

		return super.doInBackground(arg0);
	}

	public String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			return capitalize(model);
		} else {
			return capitalize(manufacturer) + " " + model;
		}
	}


	private String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	} 

	@Override
	protected void onCompleted(Object result2) throws Exception {
		super.onCompleted(result2);

		try {
			final DialogInterface.OnClickListener onFailEvent = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					onFail(mEx);
				}
			};

			JSONObject result = (JSONObject) result2;

			switch(result.getInt("type")) {
			case -1: {
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setPositiveButton("Thoát", onFailEvent);
				builder.setIcon(R.drawable.logo);
				builder.setMessage("Không thể kết nối máy chủ, xin kiểm tra lại kết nối mạng.");
				builder.setCancelable(false);
				builder.create().show();
			}
			break;

			case 0: 
				//				onCompleted();
				onSuccess();
				break;

			case 1: {
				final String link =  result.getString("link");
				String content = result.getString("content");
				String message = "<a href=\"" + link + "\">" + content + "</a>";

				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setPositiveButton("Yes", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
						mContext.startActivity(myIntent);
						onSuccess();
					}
				});
				builder.setNegativeButton("No", onFailEvent);
				builder.setIcon(R.drawable.logo);
				builder.setMessage(Html.fromHtml(message));
				builder.setCancelable(false);
				Dialog d = builder.create();
				d.show();

				// Make the textview clickable. Must be called after show()   
				((TextView)d.findViewById(android.R.id.message))
				.setMovementMethod(LinkMovementMethod.getInstance());
			}
			break;
			case 2: {
				JSONArray jNotifyArr = result.getJSONArray("notifyList");
				StringBuilder builder = new StringBuilder();
				for (int i = 0; i < jNotifyArr.length(); i++)
					builder.append(jNotifyArr.getJSONObject(i).getString("content")).append("\n");

				AlertDialog g = new AlertDialog.Builder(mContext)
				.setPositiveButton("OK", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						onSuccess();
					}
				})
				.setIcon(R.drawable.logo)
				.setMessage(builder.toString())
				.setCancelable(false)
				.create();
				g.show();
			}
			break;
			case 3: {
				AlertDialog f = new AlertDialog.Builder(mContext)
				.setPositiveButton("OK", onFailEvent)
				.setIcon(R.drawable.logo)
				.setMessage(result.getString("content"))
				.setCancelable(false)
				.create();
				f.show();
				// Make the textview clickable. Must be called after show()   
				((TextView)f.findViewById(android.R.id.message))
				.setMovementMethod(LinkMovementMethod.getInstance());
			}
			break;
			}
		} catch(Exception ex) {
			onFail(ex);
		}
	}

	@Override
	protected void onFail(final Exception e) {
		super.onFail(e);

		AlertDialog.Builder builder = new Builder(mContext);
		builder.setMessage("Không thể kết nối với máy chủ!");
		builder.setPositiveButton("OK", new OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				onFinalFail(e);
			}
		});
		builder.create().show();
	}

	protected void onSuccess() {}
	protected void onFinalFail(Exception e) {}
}