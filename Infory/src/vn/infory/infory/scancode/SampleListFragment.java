package vn.infory.infory.scancode;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import vn.infory.infory.R;
import vn.infory.infory.data.Shop;
import vn.infory.infory.network.CyAsyncTask;
import vn.infory.infory.network.GetShopDetail2;
import vn.infory.infory.network.NetworkManager;
import vn.infory.infory.shopdetail.ShopDetailActivity;
import vn.infory.infory.shoplist.ShopListActivity;

import android.app.Activity;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ScrollView;


public class SampleListFragment extends ScrollTabHolderFragment implements OnScrollListener {

	private static final String ARG_POSITION = "position";
	public static final String ARG_OBJECT = "object";   

	private ListView mListView;
	private ArrayList<String> mListItems;
	public ScanCodeResult2Activity CustomListView = null;
	
	private static List<CyAsyncTask> mTaskList = new ArrayList<CyAsyncTask>();

	private int mPosition;
	private Activity mAct;
	private int mLayoutHeight;
	private static Object mScanCodeRelated;
	
	private ScanCodeRelatedListViewAdapter adapter;
	
    private static ArrayList<ListModelRelatedShops> arrListModelRelatedShops = new ArrayList<ListModelRelatedShops>();
    private static ArrayList<ListModelRelatedPromotions> arrListModelRelatedPromotions = new ArrayList<ListModelRelatedPromotions>();
    private static ArrayList<ListModelRelatedPlacelists> arrListModelRelatedPlacelists = new ArrayList<ListModelRelatedPlacelists>();

	public static Fragment newInstance(Activity act, Object scanCodeRelated, int position, int layoutHeight) {
		SampleListFragment f = new SampleListFragment();
		
		f.mAct = act;
		f.mLayoutHeight = layoutHeight;
		f.mScanCodeRelated = scanCodeRelated;		
		
		Bundle b = new Bundle();
		b.putInt(ARG_POSITION, position);
		f.setArguments(b);
		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPosition = getArguments().getInt(ARG_POSITION);
		
		setListData();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.scan_code_fragment_list, null);

		mListView = (ListView) v.findViewById(R.id.listView);		
		
		View placeHolderView = inflater.inflate(R.layout.scan_code_view_header_placeholder, mListView, false);
		FrameLayout fr = (FrameLayout)placeHolderView.findViewById(R.id.frViewHeader);
		
		fr.setPadding(0, mLayoutHeight, 0, 0);
		mListView.addHeaderView(placeHolderView);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Bundle args = getArguments();
		
		mListView.setOnScrollListener(this);
		
		Resources res = getResources();    
		CustomListView = (ScanCodeResult2Activity) getActivity();  
        final ScanCodeRelatedListViewAdapter adapter_related;
        if(args.getInt(ARG_OBJECT) == 0)
        {
        	adapter_related = new ScanCodeRelatedListViewAdapter(CustomListView, arrListModelRelatedShops,res,0);		
        }
        else if(args.getInt(ARG_OBJECT) == 1)
        {
        	adapter_related = new ScanCodeRelatedListViewAdapter(CustomListView, arrListModelRelatedPromotions,res,1);            	
        }
        else
        {
        	adapter_related = new ScanCodeRelatedListViewAdapter(CustomListView, arrListModelRelatedPlacelists,res,2);
        }
        
//		mListView.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.scan_code_list_item, android.R.id.text1, mListItems));
        mListView.setAdapter(adapter_related);
        
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub				
				View mLayoutLoading = (View) mAct.findViewById(R.id.scanDLGLayoutLoading);
				View mLayoutLoadingAnimation = (View) mAct.findViewById(R.id.scanDLGLayoutLoadingAnimation);
				mLayoutLoading.setVisibility(View.VISIBLE);
	    		AnimationDrawable frameAnimation = (AnimationDrawable) 
						mLayoutLoadingAnimation.getBackground();
				frameAnimation.start();
				
//				adapter = (ScanCodeRelatedListViewAdapter)parent.getAdapter();
				int type = adapter_related.getType();
				if(type == 0) //Shop
				{
					ListModelRelatedShops item = (ListModelRelatedShops) adapter_related.getItem(position-1);
					
					GetShopDetail2 task = new GetShopDetail2(getActivity(), item.getId()) {
						@Override
						protected void onCompleted(Object result2) {
							mTaskList.remove(this);
							
							JSONObject jShop = (JSONObject) result2;
							
							Shop shop = new Shop();
							shop.idShop	= jShop.optInt("idShop");
							shop.shopName	= jShop.optString("shopName");
							shop.numOfView = jShop.optString("numOfView");
							shop.logo		= jShop.optString("logo");
							
							ShopDetailActivity.newInstance(getActivity(), shop);
						}

						@Override
						protected void onFail(Exception e) {
							mTaskList.remove(this);
							
							ShopDetailActivity.newInstanceNoReload(getActivity(), new Shop());
						}
					};
					task.setTaskList(mTaskList);
					task.executeOnExecutor(NetworkManager.THREAD_POOL);
				}
				else if(type == 1) //Shop list
				{
					ListModelRelatedPromotions item = (ListModelRelatedPromotions) adapter_related.getItem(position-1);
					try {							
						
						//Convert JSONArray thành string "[1,2,3,4]"
						String ids = item.getShop_ids().toString();		
						//Cắt bỏ dấu [ và ]
						ids = ids.substring(1, ids.length()-1);
						
						ShopListActivity.newInstance(getActivity(), ids, new ArrayList<Shop>(),0);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						ShopListActivity.newInstance(getActivity(), "a", new ArrayList<Shop>(),0);
					} 
				}
				else
				{
					try {
						ListModelRelatedPlacelists item = (ListModelRelatedPlacelists) adapter_related.getItem(position-1);
						String id_placelist = Integer.toString(item.getId());
						ShopListActivity.newInstanceWithPlacelistId(getActivity(), id_placelist, new ArrayList<Shop>());
					} catch (Exception e) {
						// TODO: handle exception
						ShopListActivity.newInstanceWithPlacelistId(getActivity(), "a", new ArrayList<Shop>());
					}
				}						
			}
		});
	}

	@Override
	public void adjustScroll(int scrollHeight) {
		if (scrollHeight == 0 && mListView.getFirstVisiblePosition() >= 1) {
			return;
		}

		mListView.setSelectionFromTop(1, scrollHeight);

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (mScrollTabHolder != null)
			mScrollTabHolder.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount, mPosition);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// nothing
	}

	public static void setListData()
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
		                     
							related_shop_model.setId(related_shop_obj.optInt("idShop"));
							related_shop_model.setName(related_shop_obj.optString("shopName"));			                
			                related_shop_model.setLogo(related_shop_obj.optString("logo"));
			                related_shop_model.setAddress(related_shop_obj.optString("address"));
			                related_shop_model.setDescription(related_shop_obj.optString("description"));
			                    
			                arrListModelRelatedShops.add( related_shop_model );
						}
					}
					
					if(jItem.has("relatedPromotions")){
						JSONArray jArrRelatedPromotions = jItem.getJSONArray("relatedPromotions");
						for(int j = 0; j < jArrRelatedPromotions.length(); j++){
							JSONObject related_promotion_obj = jArrRelatedPromotions.getJSONObject(j);
							final ListModelRelatedPromotions related_promotion_model = new ListModelRelatedPromotions();
		                     
							related_promotion_model.setShop_ids(related_promotion_obj.optJSONArray("idShops"));
							related_promotion_model.setName(related_promotion_obj.optString("promotionName"));
							related_promotion_model.setLogo(related_promotion_obj.optString("logo"));
							related_promotion_model.setTime(related_promotion_obj.optString("time"));
							related_promotion_model.setDescription(related_promotion_obj.optString("description"));
							
			                    
							arrListModelRelatedPromotions.add( related_promotion_model );
						}
					}
					
					if(jItem.has("relatedPlacelists"))
					{
						JSONArray jArrRelatedPlacelists = jItem.getJSONArray("relatedPlacelists");
						for(int j = 0; j < jArrRelatedPlacelists.length(); j++){
							JSONObject related_placelists_obj = jArrRelatedPlacelists.getJSONObject(j);
							final ListModelRelatedPlacelists related_placelists_model = new ListModelRelatedPlacelists();
		                     
							related_placelists_model.setId(related_placelists_obj.optInt("placelistId"));
							related_placelists_model.setName(related_placelists_obj.optString("placelistName"));
							related_placelists_model.setAuthorName(related_placelists_obj.optString("authorName"));
							related_placelists_model.setAuthorAvatar(related_placelists_obj.optString("authorAvatar"));
							related_placelists_model.setDescription(related_placelists_obj.optString("description"));
			                    
			                arrListModelRelatedPlacelists.add( related_placelists_model );
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
}
