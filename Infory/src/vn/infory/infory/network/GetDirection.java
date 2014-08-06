package vn.infory.infory.network;

import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

public class GetDirection extends CyAsyncTask {

	// Data
	private double srcLat;
	private double srcLon;
	private double dstLat;
	private double dstLon;

	public GetDirection(Context c, double srcLat, double srcLon, double dstLat, double dstLon) {
		super(c);

		this.srcLat = srcLat;
		this.srcLon = srcLon;
		this.dstLat = dstLat;
		this.dstLon = dstLon;
	}

	@Override
	protected Object doInBackground(Object... arg0) {

		try {
			final String URLFormat = "http://maps.googleapis.com/maps/api/directions/json?origin=%f,%f&destination=%f,%f&sensor=true";

			String URL = String.format(Locale.US, URLFormat, srcLat, srcLon, dstLat, dstLon);

			String response = NetworkManager.get(URL, false);

			JSONObject rootJson = new JSONObject(response);

			return HandleDirectionResponse(rootJson);
		} catch (Exception e) {
			mEx = e;
		}

		return super.doInBackground(arg0);
	}

	private PolylineOptions HandleDirectionResponse(JSONObject rootJson) throws JSONException {

		String encoded_points = rootJson.getJSONArray("routes").getJSONObject(0)
				.getJSONObject("overview_polyline").getString("points");

		int index = 0;
		int lat = 0;
		int lng = 0;
		PolylineOptions polylineOption = new PolylineOptions();

		int shift;
		int result;

		while (index < encoded_points.length()) {
			shift = 0;
			result = 0;

			while (true) {
				int b = encoded_points.charAt(index++) - '?';
				result |= ((b & 31) << shift);
				shift += 5;
				if (b < 32)
					break;
			}
			lat += ((result & 1) != 0 ? ~(result >> 1) : result >> 1);

			shift = 0;
			result = 0;

			while (true) {
				int b = encoded_points.charAt(index++) - '?';
				result |= ((b & 31) << shift);
				shift += 5;
				if (b < 32)
					break;
			}
			lng += ((result & 1) != 0 ? ~(result >> 1) : result >> 1);
			/* Add the new Lat/Lng to the Array. */
			polylineOption = polylineOption.add(new LatLng((lat/1e5),(lng/1e5)));
		}
		return polylineOption;
	}
}
