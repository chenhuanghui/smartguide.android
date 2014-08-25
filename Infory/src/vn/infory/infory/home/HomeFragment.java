package vn.infory.infory.home;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import vn.infory.infory.CyUtils;
import vn.infory.infory.FontsCollection;
import vn.infory.infory.MainActivity;
import vn.infory.infory.PlaceListListActivity;
import vn.infory.infory.R;
import vn.infory.infory.data.PlaceList;
import vn.infory.infory.data.Shop;
import vn.infory.infory.data.home.HomeItem_ShopItem;
import vn.infory.infory.data.home.PromoItem;
import vn.infory.infory.network.CyAsyncTask;
import vn.infory.infory.network.NetworkManager;
import vn.infory.infory.network.CyAsyncTask.Listener2;
import vn.infory.infory.network.GetCounterMessage;
import vn.infory.infory.shopdetail.ShopDetailActivity;
import vn.infory.infory.shoplist.ShopListActivity;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.TextView;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;

public class HomeFragment extends Fragment implements HomeListener, Listener2 {

	public static final int iType_unread = 0;
	public static final int iType_read = 1;
	public static final int iType_total = 2;
	public static final int iType_all = 3;
	// Data
	private Listener mListener = new Listener();
	private OnScrollListener mScrollListener = new OnScrollListener() {
		public void onScroll(AbsListView a, int b, int c, int d) {}
		public void onScrollStateChanged(AbsListView arg0, int arg1) {}
	};

	private List<CyAsyncTask> mTaskList = new ArrayList<CyAsyncTask>();
	private HomeAdapter mAdapter;

	// GUI elements
	@ViewById(id = R.id.edtSearch)			private View mEdtSearch;
	@ViewById(id = R.id.lstMain)			private ListView mLayoutMain;
	@ViewById(id = R.id.layoutLoading)		private View mLayoutLoading;
	@ViewById(id = R.id.btnSideMenu)		private View mBtnSideMenu;
	@ViewById(id = R.id.imageNotification)	private View imageNotification;
	@ViewById(id = R.id.txtCounter)			private TextView txtCounter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.home_fragment, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		try {
			AndroidAnnotationParser.parse(this, view);
		} catch (Exception e) {
			getActivity().finish();
		}
	}

	public void onFinishInit() {
		FontsCollection.setFont(getView());
		// Get count unread message
		CyAsyncTask mLoader = new GetCounterMessage(getActivity(), iType_unread);
		mLoader.setListener(this);
		mLoader.executeOnExecutor(NetworkManager.THREAD_POOL);
		
		mAdapter = new HomeAdapter(getActivity(), this);
		mLayoutMain.setAdapter(mAdapter);
		mLayoutMain.setOnScrollListener(mAdapter);
		mAdapter.setOnScrollListener(mScrollListener);
		FontsCollection.setFont(getView());
		
		CyUtils.setHoverEffect(imageNotification, false);
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		Editor e = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
		e.putString("last_activity", getClass().getSimpleName());		
		e.putString("use_immediately_activity", "0");
		e.commit();
		
		mLayoutLoading.setVisibility(View.GONE);
		
		super.onResume();
	}

	@Override
	public void onDestroy() {
		Editor e = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
		e.putString("last_activity", getClass().getSimpleName());
		e.commit();
		
		for (CyAsyncTask task : mTaskList)
			task.cancel(true);

		super.onDestroy();
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		Editor e = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
		e.putString("last_activity", getClass().getSimpleName());
		e.commit();
		
		super.onPause();
	}

	@Click(id = R.id.edtSearch)
	private void onSearchClick(View v) {
		PlaceListListActivity.newInstance(getActivity());
	}

	@Click(id = R.id.btnSideMenu)
	private void onSideMenuClick(View v) {
		mListener.onSideMenuClick();
	}
	
	@Click(id = R.id.imageNotification)
	private void onNotificationClick(View v) {
		// TODO do something in here
	}

	public void setListener(Listener listener, OnScrollListener scrollListener) {
		if (listener == null)
			listener = new Listener();
		
		mListener = listener;
		mScrollListener = scrollListener;
//		mAdapter.setOnScrollListener(scrollListener);
	}

	///////////////////////////////////////////////////////////////////////////
	// Private method
	///////////////////////////////////////////////////////////////////////////

	///////////////////////////////////////////////////////////////////////////
	// Home listener implemetation
	///////////////////////////////////////////////////////////////////////////

	@Override
	public void onBranchPromoInfoClick(final String shopId) {
		ShopListActivity.newInstance(getActivity(), shopId, new ArrayList<Shop>());
	}

	@Override
	public void onImageClick(List<String> urlList, int index) {
		
	}

	@Override
	public void onPlaceListClick(int placeListId, PlaceList placeList) {
		ShopListActivity.newInstance(getActivity(), placeList, new ArrayList<Shop>());
	}

	@Override
	public void onShopItemClick(int shopId, HomeItem_ShopItem shopItem) {
		ShopDetailActivity.newInstance(getActivity(), shopItem.makeShop());
	}

	@Override
	public void onStoreItemClick(String storeId) {

	}

	///////////////////////////////////////////////////////////////////////////
	// Main Pager Adapter
	///////////////////////////////////////////////////////////////////////////

	public static class Listener implements MainActivity.onSideMenuClickListener {
		public void onSideMenuClick() {}
	}

	@Override
	public void onShopItemClick(int shopId, PromoItem shopItem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCompleted(Object result) {
		// TODO Auto-generated method stub
//		json response: {"number":0,"string":"0"}

		try {
			String unreadMessage = new JSONObject((String) result).getString("string");
			Log.e(getTag(), "unreadMessage: " + unreadMessage);
			if(unreadMessage.compareTo("0") != 0) {
				txtCounter.setVisibility(View.VISIBLE);
				txtCounter.setText(unreadMessage);
			}
		} catch (JSONException e) {
			Log.e(getTag(), e.toString());
		}
		
	}

	@Override
	public void onFail(Exception e) {
		txtCounter.setVisibility(View.GONE);
		// TODO Auto-generated method stub
		
	}
}