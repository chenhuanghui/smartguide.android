package vn.infory.infory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;
import com.cycrix.jsonparser.JsonArray;
import com.cycrix.jsonparser.JsonParser;

import vn.infory.infory.PlaceListListActivity.PlaceListAdapter;
import vn.infory.infory.data.City;
import vn.infory.infory.data.PlaceList;
import vn.infory.infory.data.Settings;
import vn.infory.infory.data.Shop;
import vn.infory.infory.network.CyAsyncTask;
import vn.infory.infory.network.GetCityList;
import vn.infory.infory.network.GetPlaceListList;
import vn.infory.infory.shoplist.ShopListActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class CityList extends Activity{
	
	private CityListAdapter mCityListAdapter;
	
	@ViewById(id = R.id.edtSearch)				private EditText mEdtSearch;
	@ViewById(id = R.id.listViewCity)			private ListView mLst;
	@ViewById(id = R.id.btnSelectCity)			private Button mBtnSelectCity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.city_list);

		try {
			View rootView = findViewById(android.R.id.content);
			AndroidAnnotationParser.parse(this, rootView);
			FontsCollection.setFont(rootView);
		} catch (Exception e) {
			finish();
		}
		
		Settings s = Settings.instance();
		mBtnSelectCity.setText(" \" " + s.cityName + " \" ");
		
		mCityListAdapter = new CityListAdapter();

		mLst.setAdapter(mCityListAdapter);
		mLst.setOnScrollListener(mCityListAdapter);
		mLst.setOnItemClickListener(mCityListAdapter);
		
		mLst.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				InputMethodManager inputMgr = (InputMethodManager)
						getSystemService(Context.INPUT_METHOD_SERVICE);
				inputMgr.hideSoftInputFromWindow(mEdtSearch.getWindowToken(), 0);
				return false;
			}
		});
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		finish();
	}
	
	@Click(id = R.id.btnCancel)
	private void onCancelClick(View v) {
		finish();
	}

	public static void newInstance(Activity act) {
		Intent intent = new Intent(act, CityList.class);
		act.startActivity(intent);
		act.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}
		
	///////////////////////////////////////////////////////////////////////////
	// City list Adapter
	///////////////////////////////////////////////////////////////////////////
	public class CityListAdapter extends LazyLoadAdapter implements OnItemClickListener {

		public CityListAdapter() {
			super(CityList.this, 
					new GetCityList(CityList.this), 
					R.layout.shop_list_loading, 
					1, new ArrayList<City>());
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = super.getView(position, convertView, parent);
			
			if (position >= mItemList.size()) {
				View loading = convertView.findViewById(R.id.layoutLoading);
				if (loading != null) {
					AnimationDrawable frameAnimation = 
							(AnimationDrawable) loading.getBackground();
					frameAnimation.start();
				}
				return convertView;
			}

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.city_list_item, parent, false);
				FontsCollection.setFont(convertView);
			}

			if (position >= mItemList.size())
				return convertView;

			City item = (City) getItem(position);
			
			Settings s = Settings.instance();
			if(s.cityId.equalsIgnoreCase(item.cityId))
			{
				ImageView img = (ImageView) convertView.findViewById(R.id.imgIcon);
				img.setImageResource(R.drawable.icon_location_city_selected);
			}
			
			if (mItemList.size() == 4) {
				convertView.setBackgroundResource(R.drawable.button_call);
			} else {
				if (position == 0) {
					convertView.setBackgroundResource(R.drawable.frame_detail_info);
				} else if (!mIsMore && position == mItemList.size() - 1) {
					convertView.setBackgroundResource(R.drawable.frame_detail_info_footer);
				} else {
					convertView.setBackgroundResource(R.drawable.frame_detail_info_item);
				}
			}

			TextView txtName = (TextView) convertView.findViewById(R.id.txtName);
			txtName.setText(item.cityName);

			return convertView;
		}
		
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			City city = (City) getItem(position);			
			Settings s = Settings.instance();
			s.cityId = city.cityId;
			s.cityName = city.cityName;
			
			finish();
		}
	}
}
