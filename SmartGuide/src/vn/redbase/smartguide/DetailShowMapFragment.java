package vn.redbase.smartguide;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class DetailShowMapFragment extends Fragment {
	
	private Shop mShop;
	private SupportMapFragment mMapFragment;
	
	@Override
    public void onAttach(Activity activity) {
    	// TODO Auto-generated method stub
    	super.onAttach(activity);
    }
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	super.onCreate(savedInstanceState);
    }

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.detail_showmap, container, false);
        
        ImageButton btn = (ImageButton) v.findViewById(R.id.imgDetailMap);
        btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				Intent i = new Intent(getActivity(), MapActivity.class);
				i.putExtra("lat", mShop.mLat);
				i.putExtra("lng", mShop.mLng);
				getActivity().startActivity(i);
			}
		});
        
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        
        mMapFragment = new SupportMapFragment();
    }
    
    public void setData(Shop s) {
    	
    	mShop = s;
    	
    	///////////////////////////////////////////////////////////////////////
    	
    	// Construct URL
    	
    	String url = "http://maps.googleapis.com/maps/api/staticmap" +
    			"?zoom=14&size=%dx%d&markers=%f,%f&sensor=false&scale=2";
    	
    	View mapLayout = getView().findViewById(R.id.layoutDetailMap);
    	mapLayout.addOnLayoutChangeListener(new OnLayoutChangeListener() {
			
			@Override
			public void onLayoutChange(View v, int left, int top, int right,
					int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
				// TODO Auto-generated method stub
				int w = (right - left) / 1;
		    	int h = (bottom - top) / 1;
		    	
		    	String url = "http://maps.googleapis.com/maps/api/staticmap" +
		    			"?zoom=14&size=%dx%d&markers=%f,%f&sensor=false&scale=1&visual_refresh=true";
		    	url = String.format(url, w, h, mShop.mLat, mShop.mLng);
		    	
		    	new HttpConnection(new Handler() {
		    		@Override
		    		public void handleMessage(Message msg) {
		    			
		    			switch (msg.what) {
		    			case HttpConnection.DID_SUCCEED:
		    				ImageButton img = (ImageButton) getView().findViewById(R.id.imgDetailMap);
		    				img.setImageBitmap((Bitmap) msg.obj);
		    				break;
		    			}
		    		}
		    	}).bitmap(url);
		    	
		    	Log.d("CycrixDebug", "Download= " + url);
			}
		});
    	
    	// Request map image
    	
    	
    	///////////////////////////////////////////////////////////////////////
    	
//    	GoogleMap map = mMapFragment.getMap();
//    	if (map != null)
//    		moveCamera();
    }
    
    public void destroyMap() {
    	
//    	getFragmentManager().beginTransaction().remove(mMapFragment).commit();
    }
    
    public void createMap() {
    	
//    	getFragmentManager().beginTransaction().add(R.id.layoutDetailMap, mMapFragment).commit();
//    	getFragmentManager().executePendingTransactions();
//    	
//    	if (mShop != null)
//    		moveCamera();
    }
    
    Marker mMarker;
    private void moveCamera() {
    	
    	GoogleMap map = mMapFragment.getMap();
    	CameraUpdate camUpdate = null;
    	LatLng latLng = new LatLng(mShop.mLat, mShop.mLng);
    	try {
    		MapsInitializer.initialize(getActivity());
    		camUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	map.animateCamera(camUpdate);
    	if (mMarker != null)
    		mMarker.remove();
    	mMarker = map.addMarker(new MarkerOptions().position(latLng));
    }
    
//    private SupportMapFragment getMapFragent() {
//    	
//    	SupportMapFragment mapFragment = null;
//    	mapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.map);
//    	if (mapFragment == null) 
//    		mapFragment = (SupportMapFragment) getFragmentManager().findFragmentByTag("map");
//
//    	return mapFragment;
//    }
    
     @Override
    public void onResume() {
    	 
    	super.onResume();
    }
     
     @Override
    public void onPause() {

    	super.onPause();
    }
}
