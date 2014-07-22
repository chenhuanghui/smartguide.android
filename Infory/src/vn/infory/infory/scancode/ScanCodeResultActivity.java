package vn.infory.infory.scancode;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import vn.infory.infory.CyImageLoader;
import vn.infory.infory.FontsCollection;
import vn.infory.infory.R;
import vn.infory.infory.network.CyAsyncTask;
import vn.infory.infory.network.NetworkManager;
import vn.infory.infory.network.ScanCode;
import vn.infory.infory.network.ScanCodeRelated;

import com.cycrix.androidannotation.ViewById;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class ScanCodeResultActivity extends FragmentActivity{
		
	private List<CyAsyncTask> mTaskList = new ArrayList<CyAsyncTask>();
	
	private static Object mScanCodeResult;
	public static Object mScanCodeRelated;
	public static String mQRCode;
	
	ScanCodeRelatedPagerAdapter mScanCodeRelatedPagerAdapter;	
	ViewPager mViewPager;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scan_dlg_2);
        
		JSONArray jArr = (JSONArray) mScanCodeResult;
		
		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearLayoutScanDLG2);
		for (int i = 0; i < jArr.length(); i++) 
		{						
			try {
				if(jArr.getJSONObject(i) instanceof JSONObject)
				{
					JSONObject jItem = jArr.getJSONObject(i);							
					
					if(jItem.has("header"))
					{
						TextView txtHeader = new TextView(getApplicationContext());
						txtHeader.setText(jItem.optString("header"));
						txtHeader.setTextSize(20);
						txtHeader.setTextColor(Color.BLACK);
						txtHeader.setGravity(Gravity.CENTER);
						txtHeader.setPadding(50, 0, 50, 0);
						linearLayout.addView(txtHeader);
					}
					
					if(jItem.has("bigText"))
					{
						TextView txtBigText = new TextView(getApplicationContext());
						txtBigText.setText(jItem.optString("bigText"));

//								txtBigText.setTextAppearance(getActivity(), R.style.BigText);
						
						txtBigText.setTextSize(16);
						txtBigText.setTextColor(Color.BLACK);
						txtBigText.setPadding(50, 20, 50, 0);
						linearLayout.addView(txtBigText);
					}
					
					if(jItem.has("image"))
					{
						if(jItem.optJSONObject("image") instanceof JSONObject)
						{									
							JSONObject jImage = jItem.optJSONObject("image");
							final ImageView imgView = new ImageView(getApplicationContext());
							
							//Set image width and height
							imgView.setLayoutParams(new LinearLayout.LayoutParams(jImage.optInt("width"),jImage.optInt("height")));
							
							CyImageLoader.instance().loadImage(jImage.optString("url"), new CyImageLoader.Listener(){
								@Override
								public void loadFinish(int from,
										Bitmap image, String url,
										CyAsyncTask task) {
									// TODO Auto-generated method stub
									imgView.setImageBitmap(image);
								}
								
							}, new Point(), getApplicationContext());
							imgView.setPadding(0, 20, 0, 0);
							linearLayout.addView(imgView);
						}
					}

					if(jItem.has("smallText"))
					{
						TextView txtSmallText = new TextView(getApplicationContext());
						txtSmallText.setText(jItem.optString("smallText"));
						txtSmallText.setTextSize(14);
						txtSmallText.setTextColor(Color.GRAY);
						txtSmallText.setPadding(50, 20, 50, 0);
						linearLayout.addView(txtSmallText);
					}
					
					
					
					if(jItem.has("buttons"))
					{
						if(jItem.optJSONArray("buttons") instanceof JSONArray)
						{
							JSONArray jArrButton = jItem.optJSONArray("buttons");
							if(jArrButton.length() > 0)
							{
								for (int j = 0; j < jArrButton.length(); j++) 
								{
									JSONObject jItemButton = jArrButton.getJSONObject(j);
									
									Button btn = new Button(getApplicationContext());
									btn.setText(jItemButton.optString("actionTitle"));
																				
									switch (jItemButton.optInt("color")) 
									{
										case 1: //grey
											btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_viewer_buttongrey));
											break;

										default:
											btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_viewer_button));
											break;
									}
									
									LayoutParams params = new LayoutParams(
									        LayoutParams.WRAP_CONTENT,      
									        LayoutParams.WRAP_CONTENT
									);
									params.setMargins(0, 20, 0, 50);
									btn.setLayoutParams(params);
									
									btn.setPadding(50, 20, 50, 20);
									btn.setGravity(Gravity.CENTER);
									
									linearLayout.addView(btn);
								}
							}
						}
						else if (jItem.optJSONObject("buttons") instanceof JSONObject) 
						{
							
						}
					}
				}
				else if (jArr.getJSONArray(i) instanceof JSONArray) 
				{
					JSONArray jChildArr = jArr.getJSONArray(i);
					if(jChildArr.length() > 0)
					{
						
					}
				}
			} catch (NotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		
		mScanCodeRelatedPagerAdapter = new ScanCodeRelatedPagerAdapter(getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.testpager1);
        mViewPager.setAdapter(mScanCodeRelatedPagerAdapter);
		
        
        /*mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mViewPager.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });*/
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {        
        return super.onOptionsItemSelected(item);
    }
	
	/**
     * A {@link android.support.v4.app.FragmentStatePagerAdapter} that returns a fragment
     * representing an object in the collection.
     */
    public static class ScanCodeRelatedPagerAdapter extends FragmentStatePagerAdapter {

        public ScanCodeRelatedPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new ScanCodeRelatedFragment();
            Bundle args = new Bundle();
            args.putInt(ScanCodeRelatedFragment.ARG_OBJECT, i); // Our object is just an integer :-P
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            // For this contrived example, we have a 100-object collection.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
        	String title = "";
        	switch (position) {
			case 0:
				title = "Địa điểm";
				break;

			case 1:
				title = "Ưu đãi";
				break;
				
			case 2:
				title = "Danh sách";
				break;
			}
            return title;
        }
    }
    
    /**
     * A dummy fragment representing a section of the app, but that simply displays dummy text.
     */
    public static class ScanCodeRelatedFragment extends Fragment {

        public static final String ARG_OBJECT = "object";
        
        ListView list;
    	ScanCodeRelatedListViewAdapter adapter;
    	public ScanCodeResultActivity CustomListView = null;
    	public ArrayList<ListModelRelatedShops> arrListModelRelatedShops = new ArrayList<ListModelRelatedShops>();
    	private List<CyAsyncTask> mTaskList = new ArrayList<CyAsyncTask>();
    	
    	private Integer current_position;
    	
    	public void setListData()
        {
    		JSONArray jArr = (JSONArray) mScanCodeRelated;
			for (int i = 0; i < jArr.length(); i++) 
			{						
				try {
					if(jArr.getJSONObject(i) instanceof JSONObject)
					{
						JSONObject jItem = jArr.getJSONObject(i);							
						
						if(jItem.has("relatedShops"))
						{
							JSONArray jArrRelatedShops = jItem.getJSONArray("relatedShops");
							for(int j = 0; j < jArrRelatedShops.length(); j++){
								JSONObject related_shop_obj = jArrRelatedShops.getJSONObject(j);
								final ListModelRelatedShops related_shop_model = new ListModelRelatedShops();
			                     
				                /******* Firstly take data in model object ******/
				                related_shop_model.setName(related_shop_obj.optString("shopName"));
				                related_shop_model.setDescription(related_shop_obj.optString("description"));
				                    
				                /******** Take Model Object in ArrayList **********/
				                arrListModelRelatedShops.add( related_shop_model );
							}
						}	
					}
				} catch (NotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			}
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.scan_code_related_shop_fragment, container, false);
            Bundle args = getArguments();
            
            Resources res =getResources();            
            CustomListView = (ScanCodeResultActivity) getActivity(); 
            list = ( ListView )rootView.findViewById( R.id.lstRelated );  // List defined in XML ( See Below )
            
            setListData();       
            
            switch (args.getInt(ARG_OBJECT)) {
			case 0: //Related shops  
				adapter=new ScanCodeRelatedListViewAdapter( CustomListView, arrListModelRelatedShops,res );
                list.setAdapter( adapter );
				break;

			case 1: //Related promotions  
				
				break;
				
			case 2: //Related placelists  
				
				break;
			}
            
            return rootView;
        }
    }
	
	public static void newInstance(Activity act, Object objScanCode, Object objScanCodeRelated, String code) {
		mScanCodeResult = objScanCode;
		mScanCodeRelated = objScanCodeRelated;
		mQRCode = code;
		
		Intent intent = new Intent(act, ScanCodeResultActivity.class);
		act.startActivity(intent);
		act.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}
	
	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

	///////////////////////////////////////////////////////////////////////////
	// Listener
	///////////////////////////////////////////////////////////////////////////
		
	public interface BackListener {
		public void onBackPress();
	}
}
