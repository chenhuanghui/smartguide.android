package vn.infory.infory;

import org.json.JSONException;

import vn.infory.infory.notification.ListMessagesBySenderActivity;
import vn.infory.infory.notification.NotificationActivity;
import vn.infory.infory.notification.NotificationGotoActivity;
import vn.infory.infory.notification.NotificationUtil;
import vn.infory.infory.notification.ServerUtilities;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

public class GCMIntentService extends GCMBaseIntentService
{
	public static final String	GCM_SENDERID_LIVE	= "790158294934";
	private static final String	TAG					= "GCMIntentService";

	private Context				mycontext;
	boolean						way					= false;

	public GCMIntentService()
	{
		super(GCMIntentService.GCM_SENDERID_LIVE);
	}

	@Override
	protected void onError(Context context, String errorId)
	{
		Log.e(TAG, "Error");
	}

	@Override
	protected void onMessage(Context context, Intent intent)
	{
		Log.e(TAG, "Received message from GCM");
		// Custom init for app
		mycontext = context;
		handleMessage(context, intent);
	}

	@Override
	protected void onRegistered(Context context, String registrationId)
	{
		Log.e(TAG, "Device registered: regId = " + registrationId);
		ServerUtilities.register(context, registrationId);
	}

	@Override
	protected void onUnregistered(Context context, String registrationId)
	{
		Log.i(TAG, "Device unregistered");
		if (GCMRegistrar.isRegisteredOnServer(context))
		{
			ServerUtilities.unregister(context, registrationId);
		}
		else
		{
			// This callback results from the call to unregister made on
			// ServerUtilities when the registration to the server failed.
			Log.i(TAG, "Ignoring unregister callback");
		}
	}

	private void handleMessage(Context context, Intent intent)
	{
		Bundle bundle = intent.getExtras();

		final String content = intent.getStringExtra(NotificationUtil.content);
		final String unreadCount = intent.getStringExtra(NotificationUtil.unreadCount);
		final String messageId = intent.getStringExtra(NotificationUtil.messageId);
		final String senderId = intent.getStringExtra(NotificationUtil.senderId);


		for (String key : bundle.keySet())
		{
			Object value = bundle.get(key);
			Log.d(TAG, String.format("%s - %s - (%s)", key, value.toString(), value.getClass().getName()));
		}
		
		
		updateMyActivity(context, unreadCount);
		
		try
		{
			Intent gotoNotificationIntent = gotoNotification(content, messageId, senderId);
			int requestID = (int) System.currentTimeMillis();

			PendingIntent pendingIntent = PendingIntent.getActivity(context, requestID, gotoNotificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			// setup new notification
			Notification notification = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.sic_launcher).setContentTitle(NotificationUtil.MESSAGE).setContentText(content).setLargeIcon(((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap()).setContentIntent(pendingIntent).setAutoCancel(true).setDefaults(Notification.DEFAULT_SOUND).setTicker("Infory: " + content).setWhen(System.currentTimeMillis()).getNotification();

			// get notificationManager
			NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

			notificationManager.notify(requestID, notification);
			
		}
		catch (Exception e)
		{
			Log.i("GCMIntentService", "exception json: " + e.getMessage());
		}

	}

	static void updateMyActivity(Context context, String unreadCount)
	{
		Intent intent = new Intent("aaaaaaaaaaaaaaaaaaaa");
		intent.putExtra("unreadCount", unreadCount);
		context.sendBroadcast(intent);
	}

	private Intent gotoNotification(String content, String messageId, String senderId)
	{
		Intent i = null;

		int idMessage = Integer.valueOf(messageId);
		int idSender = Integer.valueOf(senderId);
//		intent = new Intent(mycontext, NotificationGotoActivity.class);
//		intent.putExtra(NotificationUtil.messageId, messageId);
//		intent.putExtra(NotificationUtil.senderId, senderId);
		if(idMessage > 0 && idSender > 0)
		{
			i = new Intent(this, ListMessagesBySenderActivity.class);
			i.putExtra(NotificationUtil.messageId, idMessage);
			i.putExtra(NotificationUtil.senderId, idSender);
		}
		else
		{
			if(idMessage == 0 && idSender > 0)
			{
				i = new Intent(this, ListMessagesBySenderActivity.class);
				i.putExtra(NotificationUtil.senderId, idSender);
			}
			else
			{
				if(idMessage == 0 && idSender == 0)
				{
					//Đi đến view message list
					i = new Intent(this, NotificationActivity.class);
				}
			}
		}

		return i;
	}
}
