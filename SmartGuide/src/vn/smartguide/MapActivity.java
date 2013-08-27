package vn.smartguide;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.MarkerOptionsCreator;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.View;

public class MapActivity extends FragmentActivity {
	
	private GoogleMap map;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
		try {
			MapsInitializer.initialize(getApplicationContext());
		} catch (GooglePlayServicesNotAvailableException e) {
		
			e.printStackTrace();
		}
		
		final double lat = getIntent().getFloatExtra("lat", 10.759302f);
		final double lng = getIntent().getFloatExtra("lng", 106.689069f);
		
		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		map.setMyLocationEnabled(true);
		map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));
		map.addMarker(new MarkerOptions().position(new LatLng(lat, lng)));
		
		// Get direction
		map.setMyLocationEnabled(true);
		map.setOnMyLocationChangeListener(new OnMyLocationChangeListener() {

			@Override
			public void onMyLocationChange(Location loc) {
			
				if (loc != null)
					RequestDirection(lat, lng, loc.getLatitude(), loc.getLongitude(), map);
			}
		});
	}
	
	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);  // Add this method.
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);  // Add this method.
	}
	
	private void RequestDirection(double lat, double lng, double myLat, double myLon, final GoogleMap googleMap) {
		
		googleMap.setOnMyLocationChangeListener(null);
		Handler handler = new Handler() {

			@Override
			public void handleMessage(Message message) {
				switch (message.what) {
				case HttpConnection.DID_START: {
					break;
				}
				case HttpConnection.DID_SUCCEED: {
					String response = (String) message.obj;
					try {
						JSONObject rootJson = new JSONObject(response);
						HandleDirectionResponse(rootJson, googleMap);
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				}
				case HttpConnection.DID_ERROR: {
					Exception e = (Exception) message.obj;
					e.printStackTrace();
					break;
				}
				}
			}
		};
		MainActivity.GetDirection(myLat, myLon, lat, lng, handler);
	}
	
	private void HandleDirectionResponse(JSONObject rootJson, GoogleMap googleMap) {
		
		// Decode polyline string
		String encoded_points = null;
		try {
			encoded_points = rootJson.getJSONArray("routes").getJSONObject(0)
					.getJSONObject("overview_polyline").getString("points");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		PolylineOptions mapOption = MainActivity.decodePoints(encoded_points);

		// Store the polyline
		Polyline polyline = googleMap.addPolyline(mapOption.color(0xff8080FF));
		
		LatLngBounds.Builder builder = LatLngBounds.builder();
		for (LatLng latlng : polyline.getPoints()) {
			builder.include(latlng);
		}
		
		googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 24));
	}
}
