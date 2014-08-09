package vn.infory.infory.notification;

import java.util.Random;

import vn.infory.infory.MainActivity;
import vn.infory.infory.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings.Global;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class NotificationGotoActivity extends Activity
{
	
	
	public static boolean	isAlreadyForeGround		= false;

	private Intent			IntentGoto;

	private Context			mContext;
	private Activity		mActivity;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		mContext = this;
		mActivity = this;
		
		Bundle extras = getIntent().getExtras();
		if (extras != null)
		{
			String messageId = extras.getString(NotificationUtil.messageId, "");
			String senderId = extras.getString(NotificationUtil.senderId, "");
			
			
			if(messageId.compareTo("") != 0 && senderId.compareTo("") != 0)
			{
				
			}
			else
			{
				if(messageId.compareTo("") == 0 && senderId.compareTo("") != 0)
				{
					
				}
				else
				{
					if(messageId.compareTo("") == 0 && senderId.compareTo("") == 0)
					{
						
					}
				}
			}
		}
	}

	

	private Intent getGotoIntent()
	{
		// goto home landing
		Intent intent = new Intent(NotificationGotoActivity.this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		return intent;
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		System.gc();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
//		IntentGoto = getGotoIntent();
//		
//		if (isAlreadyForeGround)
//		{
//			// only goto right screen
//			System.out.println("App is On Foreground");
//			finish();
//			
//			if (Tools.getProfileId(mActivity).compareTo(Global.NON_PROFILE_ID) == 0)
//			{
//				Intent intent = new Intent(mContext, RegisterAndLoginLandingActivity.class);
//				mContext.startActivity(intent);
//			}
//			else
//			{
//				startActivity(IntentGoto);
//			}
//		}
//		else
//		{
//			// load welcome screen before goto right screen
//			setContentView(R.layout.mainactivity);
//			LinearLayout layout = (LinearLayout) findViewById(R.id.main_activity_layout);
//			ImageView main_activity_image = (ImageView) findViewById(R.id.main_activity_image);
//			System.out.println("App is On BackGround");
//			Random randomGenerator = new Random();
//			int randomInt = randomGenerator.nextInt(100);
//			randomInt = randomInt % 1;
//			switch (randomInt)
//			{
//			case 0:
//				main_activity_image.setBackgroundResource(R.drawable.splashscreens_2);
//				break;
//			default:
//				main_activity_image.setBackgroundResource(R.drawable.splashscreens_2);
//				break;
//			}
//			
//			if (Tools.getProfileId(mActivity).compareTo(Global.NON_PROFILE_ID) == 0)
//			{
//				Intent intent = new Intent(mContext, RegisterAndLoginLandingActivity.class);
//				intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
//				mContext.startActivity(intent);
//			}
//			else
//			{
//				getUserInfo(Tools.getProfileId(mContext));
//			}
//		}
	}
}
