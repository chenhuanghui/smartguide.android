package vn.infory.infory.home;

import java.util.ArrayList;
import java.util.List;

import vn.infory.infory.CyUtils;
import vn.infory.infory.FontsCollection;
import vn.infory.infory.LayoutError;
import vn.infory.infory.PlaceListListActivity;
import vn.infory.infory.R;
import vn.infory.infory.data.PlaceList;
import vn.infory.infory.data.Shop;
import vn.infory.infory.data.home.HomeItem_ShopItem;
import vn.infory.infory.data.home.PromoItem;
import vn.infory.infory.home.HomeFragment.Listener;
import vn.infory.infory.network.CyAsyncTask;
import vn.infory.infory.network.GetShopList;
import vn.infory.infory.network.NetworkManager;
import vn.infory.infory.shopdetail.ShopDetailActivity;
import vn.infory.infory.shoplist.ShopListActivity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;

public class PromotionFragment extends Fragment implements HomeListener {

	// Data
	private Listener mListener = new Listener();
	private OnScrollListener mScrollListener = new OnScrollListener() {
		public void onScroll(AbsListView a, int b, int c, int d) {}
		public void onScrollStateChanged(AbsListView arg0, int arg1) {}
	};
	private List<CyAsyncTask> mTaskList = new ArrayList<CyAsyncTask>();
	private PromotionAdapter mAdapter;

	// GUI
	@ViewById(id = R.id.lstMain)			private ListView mLayoutMain;
	@ViewById(id = R.id.layoutLoading)		private View mLayoutLoading;
	@ViewById(id = R.id.HomeFragmentLayoutLoadingAni)		private View mLayoutLoadingAni;

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

	@Override
	public void onDestroy() {
		for (CyAsyncTask task : mTaskList)
			task.cancel(true);

		super.onDestroy();
	}

	public void onFinishInit() {
		mAdapter = new PromotionAdapter(getActivity(), this, new ArrayList<PromoItem>());
		mLayoutMain.setAdapter(mAdapter);
		mLayoutMain.setOnScrollListener(mAdapter);
		mAdapter.setOnScrollListener(mScrollListener);
		FontsCollection.setFont(getView());
	}

	@Click(id = R.id.edtSearch)
	private void onSearchClick(View v) {
		PlaceListListActivity.newInstance(getActivity());
	}

	@Click(id = R.id.btnSideMenu)
	private void onSideMenuClick(View v) {
		mListener.onSideMenuClick();
	}

	public void setListener(Listener listener, OnScrollListener scrollListener) {
		if (listener == null)
			listener = new Listener();

		mListener = listener;
		mScrollListener = scrollListener;
//		mAdapter.setOnScrollListener(scrollListener);
	}

	@Override
	public void onBranchPromoInfoClick(final String shopId) {
		GetShopList getShopListTask = new GetShopList(getActivity(), 
				shopId, 0, 0) {

			@Override
			protected void onCompleted(Object result2) {
				mTaskList.remove(this);
				
				ArrayList<Shop> result = (ArrayList<Shop>) result2;
				ShopListActivity.newInstance(getActivity(), shopId, new ArrayList<Shop>(), 0);
			}

			@Override
			protected void onFail(Exception e) {
				mTaskList.remove(this);
				
				CyUtils.showError("Không thể lấy danh sách chuỗi cửa hàng", mEx, getActivity());
//				LayoutError.newInstance(getActivity());
				
				AlertDialog.Builder builder = new Builder(getActivity());
				builder.setCancelable(false);
				builder.setMessage("Không có dữ liệu!");
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						getActivity().finish();
					}
				});
				builder.create().show();
			}
		};
		mTaskList.add(getShopListTask);
		getShopListTask.setVisibleView(mLayoutLoading);
		getShopListTask.executeOnExecutor(NetworkManager.THREAD_POOL);
				
		AnimationDrawable frameAnimation = (AnimationDrawable) 
				mLayoutLoadingAni.getBackground();
		frameAnimation.start();
	}

	@Override
	public void onShopItemClick(int shopId, PromoItem shopItem) {
		ShopDetailActivity.newInstance(getActivity(), shopItem.makeShop());
	}

	@Override
	public void onImageClick(List<String> urlList, int index) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onPlaceListClick(int placeListId, PlaceList placeList) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onShopItemClick(int shopId, HomeItem_ShopItem shopItem) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onStoreItemClick(String storeId) {
		// TODO Auto-generated method stub
	}

	
	
}
