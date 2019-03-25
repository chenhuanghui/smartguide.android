package vn.smartguide;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

public class Locationer {
	Activity mActivity = null;
	
	public Locationer(Activity activity){
		mActivity = activity;
		String provider = Settings.Secure.getString(mActivity.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

	    if(!provider.contains("gps")){ //if gps is disabled
	        final Intent poke = new Intent();
	        poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider"); 
	        poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
	        poke.setData(Uri.parse("3")); 
	        mActivity.sendBroadcast(poke);
	    }
	    
		LocationManager locationManager = (LocationManager)mActivity.getSystemService(Context.LOCATION_SERVICE);
		LocationListener gpsListener = new GPSListener();  
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, gpsListener);
	}
	
	private class GPSListener implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			GlobalVariable.mLat = (float) location.getLatitude();
			GlobalVariable.mLng = (float) location.getLongitude();
		}

		@Override
		public void onProviderDisabled(String provider) {
			String s = "";
		}

		@Override
		public void onProviderEnabled(String provider) {}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			String s = "";
		}
	}
}
