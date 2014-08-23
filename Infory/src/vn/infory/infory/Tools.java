package vn.infory.infory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Tools {
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
		} else {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static AlertDialog.Builder AlertNetWorkDialog(final Context context,
			final Activity activity) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(context.getResources().getString(R.string.error))
				.setMessage(
						context.getResources().getString(
								R.string.error_message_no_internet_connection))
				.setCancelable(false)
				.setPositiveButton(
						context.getResources().getString(
								R.string.dialog_setting),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
								activity.startActivity(intent);
							}
						})
				.setNegativeButton(
						context.getResources()
								.getString(R.string.dialog_cancel),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								if (!isNetworkAvailable(context))
									activity.onBackPressed();
							}
						}).create();
		return builder;
	}
}
