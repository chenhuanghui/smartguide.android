package vn.infory.infory;

import vn.infory.infory.data.Settings;
import vn.infory.infory.data.Shop;
import vn.infory.infory.network.GetDirection;
import vn.infory.infory.network.NetworkManager;
import vn.infory.infory.shoplist.MapModule;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapActivity extends FragmentActivity implements InfoWindowAdapter, OnMyLocationChangeListener {

	private static Shop sShop;

	private SupportMapFragment mMapModule;
	private Shop mShop;
	private GetDirection mDirectionTask;
	private boolean mDestroyed = false;
//	private boolean mHasGetLocation = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		mShop = sShop;

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);

		mMapModule = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

		onGetMap();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (mDirectionTask != null)
			mDirectionTask.cancel(true);
		mDestroyed = true;
	}

	private void onGetMap() {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (mDestroyed)
					return;
				
				GoogleMap map = mMapModule.getMap();
				if (map == null)
					onGetMap();
				else
					setupMap(map);
			}
		}, 500);
	}

	private void setupMap(GoogleMap map) {
		map.setInfoWindowAdapter(this);
		map.setMyLocationEnabled(true);
		map.setOnMyLocationChangeListener(this);

		LatLng ll = new LatLng(mShop.shopLat, mShop.shopLng);
		map.addMarker(new MarkerOptions().position(ll)
				.icon(BitmapDescriptorFactory.fromResource(MapModule.iconIdArr[mShop.shopType])))
				.showInfoWindow();
		map.animateCamera(CameraUpdateFactory.newCameraPosition(
				new CameraPosition(ll, 100, 0, 0)));
		getDirection();
	}

	public static void newInstance(Activity act, Shop s) {
		sShop = s;
		Intent intent = new Intent(act, MapActivity.class);
		act.startActivity(intent);
		act.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

	@Override
	public View getInfoContents(Marker arg0) {
		return null;
	}

	@Override
	public View getInfoWindow(Marker marker) {

		View view = getLayoutInflater().inflate(R.layout.shop_list_map_item, null);
		FontsCollection.setFont(view);

		Shop s = mShop;

		TextView txtShopName = (TextView) view.findViewById(R.id.txtShopName);
		TextView txtShopType = (TextView) view.findViewById(R.id.txtShopType);

		txtShopName.setText(s.shopName);
		txtShopType.setText(s.shopTypeDisplay);

		return view;
	}
	
	public void getDirection() {
		
		if (mDirectionTask != null)
			mDirectionTask.cancel(true);
		
		Settings s = Settings.instance();
//		if (s.lat == -1 || s.lng == -1 || mHasGetLocation)
		if (s.lat == -1 || s.lng == -1)
			return;
		
		GetDirection task = new GetDirection(MapActivity.this, s.lat, s.lng, 
				mShop.shopLat, mShop.shopLng) {
			@Override
			protected void onCompleted(Object result1) throws Exception {
				mDirectionTask = null;
				
				GoogleMap map = mMapModule.getMap();
				PolylineOptions result = (PolylineOptions) result1;
				
				Polyline polyline = map.addPolyline(result.color(Color.rgb(14, 62, 252)).width(3));
				// Zoom in to direction
		
				LatLngBounds.Builder builder = LatLngBounds.builder();
				for (LatLng latlng : polyline.getPoints())
					builder.include(latlng);
		
				map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
			}
			
			@Override
			protected void onFail(Exception e) {
				mDirectionTask = null;
				
				CyUtils.showError("Tìm đường thất bại", e, MapActivity.this);
			}
		};
		mDirectionTask = task;
//		mHasGetLocation = true;
		task.executeOnExecutor(NetworkManager.THREAD_POOL);
	}

	@Override
	public void onMyLocationChange(Location location) {
		Settings s = Settings.instance();
		s.lat = (float) location.getLatitude();
		s.lng = (float) location.getLongitude();
		getDirection();
	}
}