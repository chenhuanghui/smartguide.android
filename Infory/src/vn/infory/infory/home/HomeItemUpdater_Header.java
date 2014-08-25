package vn.infory.infory.home;

import java.util.ArrayList;
import java.util.List;

import vn.infory.infory.R;
import vn.infory.infory.data.PlaceList;
import vn.infory.infory.data.Shop;
import vn.infory.infory.data.home.HomeItem;
import vn.infory.infory.data.home.HomeItem_Header;
import vn.infory.infory.network.CyAsyncTask;
import vn.infory.infory.network.GetPlaceListDetail;
import vn.infory.infory.network.NetworkManager;
import vn.infory.infory.shoplist.ShopListActivity;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class HomeItemUpdater_Header extends HomeItemUpdater{

	private LayoutInflater mInflater;
	private List<CyAsyncTask> mTaskList = new ArrayList<CyAsyncTask>();
	
	@Override
	public void update(View view, HomeItem item, final HomeFragment caller) {
		// TODO Auto-generated method stub
		mInflater = caller.getActivity().getLayoutInflater();
		
		final HomeItem_Header itemHeader = (HomeItem_Header) item;
		
		TextView txtHeaderContent = (TextView) view.findViewById(R.id.txtHeaderContent);
		RelativeLayout relativeLayoutHeader = (RelativeLayout) view.findViewById(R.id.relativeLayoutHeader);
		
		final View mLayoutLoading = (View) caller.getActivity().findViewById(R.id.layoutLoading);
		final View mLayoutLoadingAni = (View) caller.getActivity().findViewById(R.id.HomeFragmentLayoutLoadingAni);
		
		txtHeaderContent.setText(itemHeader.title);	
		
		relativeLayoutHeader.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				mLayoutLoading.setVisibility(View.VISIBLE);
	    		AnimationDrawable frameAnimation = (AnimationDrawable) 
	    				mLayoutLoadingAni.getBackground();
				frameAnimation.start();
				
				if(itemHeader.idPlacelist != 0)
				{		    		
					ShopListActivity.newInstanceWithPlacelistId(caller.getActivity(), itemHeader.idPlacelist+"", new ArrayList<Shop>());
//					LoadingActivity.newInstance(caller.getActivity(), itemHeader.idPlacelist);			
				}
				else if(itemHeader.idShops != "")
				{		    		
					String id_shops = itemHeader.idShops;
					ShopListActivity.newInstance(caller.getActivity(), id_shops, new ArrayList<Shop>(),0);
				}
			}
		});
		
	}

}
