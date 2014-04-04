package vn.infory.infory.shoplist;

import java.util.ArrayList;
import java.util.List;

import vn.infory.infory.CyUtils;
import vn.infory.infory.FontsCollection;
import vn.infory.infory.R;
import vn.infory.infory.data.Settings;
import vn.infory.infory.data.Shop;
import vn.infory.infory.network.CyAsyncTask;
import vn.infory.infory.network.GetDirection;
import vn.infory.infory.network.NetworkManager;
import vn.infory.infory.shopdetail.ShopDetailActivity;
import android.location.Location;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;


public class MapModule extends SupportMapFragment 
implements OnMarkerClickListener, OnInfoWindowClickListener, OnMyLocationChangeListener {

	public static final int[] iconIdArr = new int[] {
		R.drawable.iconpin_food,
		R.drawable.iconpin_food,
		R.drawable.iconpin_drink,
		R.drawable.iconpin_healness,
		R.drawable.iconpin_entertaiment,
		R.drawable.iconpin_fashion,
		R.drawable.iconpin_travel,
		R.drawable.iconpin_shopping,
		R.drawable.iconpin_education };

	private List<Shop> mShopList = new ArrayList<Shop>();
	private InfoWindowAdapter mPopupAdapter = new PopupAdapter();
	private List<CyAsyncTask> mTaskList = new ArrayList<CyAsyncTask>();

	@Override
	public void onDestroy() {
		super.onDestroy();

		for (CyAsyncTask task : mTaskList)
			task.cancel(true);
	}

	public void onResetData(List<Shop> shopList) {

		mShopList.clear();

		GoogleMap map = getMap();
		if (map == null)
			return;

		map.clear();
		onAddData(shopList);
	}

	public void onAddData(List<Shop> shopList) {

		mShopList.addAll(shopList);

		GoogleMap map = getMap();
		if (map == null)
			return;

		map.setInfoWindowAdapter(mPopupAdapter);
		map.setOnMarkerClickListener(this);
		map.setOnInfoWindowClickListener(this);
		map.setOnMyLocationChangeListener(this);
		map.setMyLocationEnabled(true);

		LatLngBounds.Builder builder = new LatLngBounds.Builder();



		for (int i = 0; i < shopList.size(); i++) {

			Shop s = shopList.get(i);
			LatLng ll = new LatLng(s.shopLat, s.shopLng);
			map.addMarker(new MarkerOptions().position(ll)
					.icon(BitmapDescriptorFactory.fromResource(iconIdArr[s.shopType]))
					.snippet("" + (mShopList.size() - shopList.size() + i)));

			builder.include(ll);
		}

		if (shopList.size() != 0)
			map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 20));
	}

	@Override
	public boolean onMarkerClick(Marker marker) {

		final int index = Integer.parseInt(marker.getSnippet());
		final Shop shop = mShopList.get(index);

		for (int i = 0; i < mTaskList.size(); i++)
			if (mTaskList.get(i) instanceof GetDirection) {
				mTaskList.get(i).cancel(true);
				mTaskList.remove(i);
				break;
			}

		if (shop.polyline != null) {
			ShowDirection(index);
			return false;
		}

		Settings s = Settings.instance();
		if (s.lat == -1 || s.lng == -1)
			return false;

		GetDirection task = new GetDirection(getActivity(), s.lat, s.lng, shop.shopLat, shop.shopLng) {
			@Override
			protected void onCompleted(Object result1) throws Exception {
				mTaskList.remove(this);

				GoogleMap map = getMap();
				PolylineOptions result = (PolylineOptions) result1;

				shop.polyline = map.addPolyline(result.color(0xffff3b3b).width(3));
				ShowDirection(index);
			}

			@Override
			protected void onFail(Exception e) {
				mTaskList.remove(this);

				CyUtils.showError("Tìm đường thất bại", e, getActivity());
			}
		};
		mTaskList.add(task);
		task.executeOnExecutor(NetworkManager.THREAD_POOL);
		return false;
	}

	private void ShowDirection(int index) {

		GoogleMap googleMap = getMap();
		if (googleMap == null)
			return;

		for (int i = 0; i < mShopList.size(); i++) {
			Shop shop = mShopList.get(i);
			if (shop.polyline != null)
				shop.polyline.setVisible(i == index);
		}

		// Zoom in to direction
		//		if (mShopList.get(index).polyline == null)
		//			return;
		//
		//		LatLngBounds.Builder builder = LatLngBounds.builder();
		//		for (LatLng latlng : mShopList.get(index).polyline.getPoints())
		//			builder.include(latlng);
		//
		//		googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 24));
	}

	private class PopupAdapter implements InfoWindowAdapter {
		@Override
		public View getInfoContents(Marker arg0) {
			return null;
		}

		@Override
		public View getInfoWindow(Marker marker) {

			View view = getActivity().getLayoutInflater().inflate(R.layout.shop_list_map_item, null);
			FontsCollection.setFont(view);

			Shop s = mShopList.get(Integer.parseInt(marker.getSnippet()));

			TextView txtShopName = (TextView) view.findViewById(R.id.txtShopName);
			TextView txtShopType = (TextView) view.findViewById(R.id.txtShopType);

			txtShopName.setText(s.shopName);
			txtShopType.setText(s.shopTypeDisplay);

			return view;
		}	
	}

	@Override
	public void onInfoWindowClick(Marker marker) {
		final int index = Integer.parseInt(marker.getSnippet());
		final Shop shop = mShopList.get(index);

		ShopDetailActivity.newInstance(getActivity(), shop);
	}

	@Override
	public void onMyLocationChange(Location location) {
		Settings s = Settings.instance();
		s.lat = (float) location.getLatitude();
		s.lng = (float) location.getLongitude();
	}
}
