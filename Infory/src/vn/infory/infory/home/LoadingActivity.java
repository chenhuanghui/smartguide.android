package vn.infory.infory.home;

import java.util.ArrayList;
import java.util.List;

import vn.infory.infory.R;
import vn.infory.infory.FlashActivity.Listener;
import vn.infory.infory.data.PlaceList;
import vn.infory.infory.data.Shop;
import vn.infory.infory.data.home.HomeItem_Header;
import vn.infory.infory.login.InforyLoginActivity;
import vn.infory.infory.login.UseImmediatelyActivity;
import vn.infory.infory.network.CyAsyncTask;
import vn.infory.infory.network.GetPlaceListDetail;
import vn.infory.infory.network.NetworkManager;
import vn.infory.infory.shoplist.ShopListActivity;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

public class LoadingActivity extends Activity{
	
	private static int mPlacelist_id;
	private List<CyAsyncTask> mTaskList = new ArrayList<CyAsyncTask>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_loading);
		
		final FrameLayout mLayoutLoadingAni = (FrameLayout) findViewById(R.id.layoutLoadingAni1);
				
		AnimationDrawable frameAnimation = (AnimationDrawable) 
				mLayoutLoadingAni.getBackground();
		frameAnimation.start();
		
		final Activity mAct = this;
		try {
			
			GetPlaceListDetail place_list_task = new GetPlaceListDetail(mAct, mPlacelist_id, 0){

				@Override
				protected void onCompleted(Object result) throws Exception {
					// TODO Auto-generated method stub
					mTaskList.remove(this);
					
					Object[] placelist = (Object[]) result;
					ShopListActivity.newInstance(mAct, (PlaceList)placelist[0], new ArrayList<Shop>());
					finish();
				}

				@Override
				protected void onFail(
						Exception e) {
					mTaskList.remove(this);
				}														
			};	
			
			mTaskList.add(place_list_task);
			place_list_task.executeOnExecutor(NetworkManager.THREAD_POOL);
		} catch (Exception e) {
			// TODO: handle exception
		}	
	}

	public static void newInstance(Activity act, int placelist_id) {
		mPlacelist_id = placelist_id;
		
		Intent intent = new Intent(act, LoadingActivity.class);
		act.startActivity(intent);
		
		act.overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom);
	}
}
