package vn.infory.infory.shoplist;

import java.util.ArrayList;
import java.util.List;

import vn.infory.infory.CyImageLoader;
import vn.infory.infory.CyUtils;
import vn.infory.infory.FontsCollection;
import vn.infory.infory.LayoutError;
import vn.infory.infory.LazyLoadAdapter;
import vn.infory.infory.PlaceListListActivity;
import vn.infory.infory.R;
import vn.infory.infory.data.PlaceList;
import vn.infory.infory.data.Shop;
import vn.infory.infory.network.CyAsyncTask;
import vn.infory.infory.network.GetPlaceList;
import vn.infory.infory.network.GetShopList;
import vn.infory.infory.network.Search;
import vn.infory.infory.shopdetail.ShopDetailActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;

public class ShopListActivity extends FragmentActivity {

	private static String sKeyword;
	private static PlaceList sPlacelist;
	private static String sShopIds;
	private static ArrayList<Shop> sShoplist;
	private static boolean sFromPlaceList;

	// GUI element
	@ViewById(id = R.id.lst) 				private SGShopListLayout mLayoutLst;
	@ViewById(id = R.id.layoutMapHolder)	private View mMapHolder;

	private int[] 	mSortByIconIdArr = new int[] {
			R.drawable.icon_distance,
			R.drawable.icon_view,
			R.drawable.icon_love,
			R.drawable.icon_bestmatch
	};

	// Data
//	private Listener mListener = new Listener();
	private AbsShopListAdapter mAdapter;
	private MapModule mMapModule;

	private int mSortBy = 3;	// 0: Khoảng cách
	// 1: Lượt xem
	// 2: Lượt love
	// 3: Mặc định

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shop_list);

		// Get GUI elements
		try {
			AndroidAnnotationParser.parse(this, findViewById(android.R.id.content));
		} catch (Exception e) {
			finish();
			return;
		}

		FontsCollection.setFont(findViewById(android.R.id.content));

		mMapModule = (MapModule) getSupportFragmentManager().findFragmentById(R.id.map);

		// Set up ListView
		mLayoutLst.setMapHolder(mMapHolder);
		mLayoutLst.addOnLayoutChangeListener(new OnLayoutChangeListener() {
			@Override
			public void onLayoutChange(View v, int left, int top, int right, int bottom, 
					int oldLeft, int oldTop, int oldRight, int oldBottom) {
				mLayoutLst.removeOnLayoutChangeListener(this);
				int headerHeight = calcViewHeight(R.layout.shop_list_header);
				int itemHeight = calcViewHeight(R.layout.shop_list_item);

				int firstHeaderHeight = bottom - top - headerHeight - itemHeight/2;
				mLayoutLst.setHeaderHeight(firstHeaderHeight, headerHeight);
			}
		});

		// Set adapter
		if (sKeyword != null)
			setSearchData(sKeyword, sShoplist);
		else if (sPlacelist != null)
			setPlaceListData(sPlacelist, sShoplist);
		else if (sShopIds != null)
			setShopListData(sShopIds, sShoplist);

		sKeyword 	= null;
		sPlacelist 	= null;
		sShopIds	= null;
		sShoplist 	= null;
	}

	@Click(id = R.id.btnBack)
	private void onSideMenuClick(View v) {
		finish();
	}

	@Click(id = R.id.txtSearch)
	private void onSearchClick(View v) {
		if (sFromPlaceList)
			finish();
		else
			PlaceListListActivity.newInstance(this);
	}

	///////////////////////////////////////////////////////////////////////////
	// Public methods
	///////////////////////////////////////////////////////////////////////////

	public static void newInstance(Activity act, String keyword, ArrayList<Shop> shopList) {

		sKeyword = keyword;
		sShoplist = shopList;

		newInstance(act);
	}

	public static void newInstance(Activity act, PlaceList placelist, ArrayList<Shop> shopList) {

		sPlacelist = placelist;
		sShoplist = shopList;

		newInstance(act);
	}

	public static void newInstance(Activity act, String shopIds, 
			ArrayList<Shop> shopList, int dummyShopList) {

		sShopIds = shopIds;
		sShoplist = shopList;

		newInstance(act);
	}

	private static void newInstance(Activity act) {
		sFromPlaceList = act instanceof PlaceListListActivity;

		Intent intent = new Intent(act, ShopListActivity.class);
		act.startActivity(intent);
		act.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

	private void setSearchData(String keyword, ArrayList<Shop> shopList) {
		ArrayList<Shop> paidShopList = new ArrayList<Shop>();
		paidShopList.add(new Shop());
		paidShopList.addAll(shopList);
		AbsShopListAdapter adapter = new ShopListAdapter(
				keyword, mLayoutLst.getListView(), paidShopList, mSortBy);
		adapter.setPage(shopList.size() / LazyLoadAdapter.ITEM_PER_PAGE);
		if (shopList.size() % LazyLoadAdapter.ITEM_PER_PAGE != 0)
			adapter.mIsMore = false;
		mLayoutLst.getListView().setAdapter(adapter);
		mLayoutLst.addOnScrollListener(adapter);
		mAdapter = adapter;
		mMapModule.onResetData(shopList);
	}

	private void setShopListData(String shopIds, ArrayList<Shop> shopList) {
		ArrayList<Shop> paidShopList = new ArrayList<Shop>();
		paidShopList.add(new Shop());
		paidShopList.addAll(shopList);
		AbsShopListAdapter adapter = new ShopListAdapter(
				shopIds, mLayoutLst.getListView(), paidShopList, mSortBy, 0);
		adapter.setPage(shopList.size() / LazyLoadAdapter.ITEM_PER_PAGE);
		if (shopList.size() % LazyLoadAdapter.ITEM_PER_PAGE != 0)
			adapter.mIsMore = false;
		mLayoutLst.getListView().setAdapter(adapter);
		mLayoutLst.addOnScrollListener(adapter);
		mAdapter = adapter;
		mMapModule.onResetData(shopList);
	}

	private void setPlaceListData(PlaceList placeList, ArrayList<Shop> shopList) {
		ArrayList<Shop> paidShopList = new ArrayList<Shop>();
		paidShopList.add(new Shop());
		paidShopList.add(new Shop());
		paidShopList.addAll(shopList);
		PlaceListAdapter adapter = new PlaceListAdapter(
				placeList, mLayoutLst.getListView(), paidShopList, mSortBy);
		adapter.setPage(shopList.size() / LazyLoadAdapter.ITEM_PER_PAGE);
		if (shopList.size() % LazyLoadAdapter.ITEM_PER_PAGE != 0)
			adapter.mIsMore = false;
		mLayoutLst.getListView().setAdapter(adapter);
		mLayoutLst.addOnScrollListener(adapter);
		mAdapter = adapter;
		mMapModule.onResetData(shopList);
	}

	///////////////////////////////////////////////////////////////////////////
	// Private methods
	///////////////////////////////////////////////////////////////////////////

	private void setHeaderItemEvent(View viewRoot) {
		final TextView txtSortBy = (TextView) viewRoot.findViewById(R.id.txtSortBy);
		View btnMap = viewRoot.findViewById(R.id.layoutBtnMap);

		txtSortBy.setText(getResources().getStringArray(R.array.sortby)[mSortBy]);
		txtSortBy.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(ShopListActivity.this);
				builder.setItems(R.array.sortby, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mSortBy = which;
						txtSortBy.setText(getResources().getStringArray(R.array.sortby)[mSortBy]);
						txtSortBy.setCompoundDrawablesWithIntrinsicBounds(mSortByIconIdArr[mSortBy], 0, 0, 0);
						mAdapter.setSort(mSortBy);
						mMapModule.onResetData(new ArrayList<Shop>());
					}
				});
				builder.setNegativeButton(R.string.Cancel, null);
				builder.setTitle(R.string.sortby_title);
				builder.create().show();
			}
		});

		btnMap.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mLayoutLst.toggle(false);
			}
		});
	}

	///////////////////////////////////////////////////////////////////////////
	// Adapter
	///////////////////////////////////////////////////////////////////////////

	private static class ViewItemHolder {
		@ViewById(id = R.id.txtName)		public TextView mTxtName;
		@ViewById(id = R.id.txtDistance)	public TextView mTxtDistance;
		@ViewById(id = R.id.txtAddress)		public TextView mAddress;
		@ViewById(id = R.id.txtDsc)			public TextView mDescription;
		//		@ViewById(id = R.id.imgPromoType)	public ImageView mImgPromoType;
		@ViewById(id = R.id.btnLove)		public ImageButton mBtnLove;
		@ViewById(id = R.id.btnAdd)			public ImageButton mBtnAdd;
		@ViewById(id = R.id.swipeView)		public SwipeView mSwipeView;
		@ViewById(id = R.id.txtNumOfView)	public TextView mTxtNumOfView;
		@ViewById(id = R.id.txtNumOfLove)	public TextView mTxtNumOfLove;
		@ViewById(id = R.id.txtNumOfComment)public TextView mTxtNumOfComment;
		@ViewById(id = R.id.imgType)		public ImageView mImgType;
	}

	public abstract class AbsShopListAdapter extends LazyLoadAdapter {

		protected List<SwipeView> mItemPool = new ArrayList<SwipeView>();

		public AbsShopListAdapter(CyAsyncTask loader, ListView lst, 
				int contentTypeCount, ArrayList itemList) {
			super(ShopListActivity.this, loader, R.layout.shop_list_loading, 
					contentTypeCount, itemList);
		}

		public void closeAll() {
			for (SwipeView v : mItemPool)
				v.toggle(false);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = super.getView(position, convertView, parent);
			if (v != null) {
				View loading = v.findViewById(R.id.layoutLoading);
				if (loading != null) {
					AnimationDrawable frameAnimation = 
							(AnimationDrawable) loading.getBackground();
					frameAnimation.start();
				}
			}
			return v;
		}

		abstract public void setSort(int sort);

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			super.onScrollStateChanged(view, scrollState);

			if (scrollState != SCROLL_STATE_IDLE)
				closeAll();
		}

		@Override
		public void onCompleted(Object result) {
			super.onCompleted(result);

			mMapModule.onAddData((List<Shop>) result);
		}
	}

	private static final int[] SHOP_TYPE_ICON_ID = new int[] {
		R.drawable.icon_food,
		R.drawable.icon_food,
		R.drawable.icon_drink,
		R.drawable.icon_healness,
		R.drawable.icon_entertaiment,
		R.drawable.icon_fashion,
		R.drawable.icon_travel,
		R.drawable.icon_shopping,
		R.drawable.icon_education,
	};
	private void setShopItemData(View convertView, Shop shop, int position) {
		ViewItemHolder holder = (ViewItemHolder) convertView.getTag();
		holder.mSwipeView.setTag(position);
		holder.mTxtName.setText(shop.shopName);
		holder.mAddress.setText(shop.address);
		holder.mDescription.setText(shop.description);

		holder.mTxtDistance.setVisibility(shop.hasDistance ? View.VISIBLE : View.GONE);
		if (shop.hasDistance)
			holder.mTxtDistance.setText("Cách bạn " + shop.distance);

		holder.mTxtNumOfView.setText("" + shop.numOfView + " đã xem");
		holder.mTxtNumOfLove.setText(" " + shop.numOfLove + " lượt thích");
		holder.mTxtNumOfComment.setText(" " + shop.numOfComment + " bình luận");
		holder.mImgType.setImageResource(SHOP_TYPE_ICON_ID[shop.shopType]);
	}

	public class ShopListAdapter extends AbsShopListAdapter {

		public ShopListAdapter(String keyword, ListView lst, ArrayList itemList, int sort) {
			super(new Search(ShopListActivity.this, keyword, 0, sort), lst, 2, itemList);
		}

		public ShopListAdapter(String shopId, ListView lst, ArrayList itemList, int sort, 
				int dummyidShopList) {
			super(new GetShopList(ShopListActivity.this, shopId, 0, sort){

				@Override
				protected void onFail(Exception e) {
					// TODO Auto-generated method stub
					super.onFail(e);
//					LayoutError.newInstance(ShopListActivity.this);
					
					AlertDialog.Builder builder = new Builder(mAct);
					builder.setCancelable(false);
					builder.setMessage("Không có dữ liệu!");
					builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							mAct.finish();
						}
					});
					builder.create().show();
				}				
			}, lst, 2, itemList);
		}

		@Override
		public void setSort(int sort) {
			mLoader.cancel(true);
			mLoader = mLoader.clone();
			mLoader.setListener(this);

			if (mLoader instanceof Search)
				((Search) mLoader).setSort(sort);
			else if (mLoader instanceof Search)
				((GetShopList) mLoader).setSort(sort);

			mPageNum = 0;
			mItemList.clear();
			mItemList.add(new Shop());
			mLoading = false;
			mIsMore = true;
			notifyDataSetChanged();
		}

		@Override
		public int getItemViewType(int position) {
			int r = super.getItemViewType(position);
			if (r != -1)
				return r;
			else {
				if (position == 0)
					return 0;
				else
					return 1;
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = super.getView(position, convertView, parent);

			if (position >= mItemList.size())
				return convertView;

			if (convertView == null) {
				switch (position) {
				case 0:
					convertView = mInflater.inflate(R.layout.shop_list_header, parent, false);
					setHeaderItemEvent(convertView);
					break;
				default:
					convertView = mInflater.inflate(R.layout.shop_list_item, parent, false);
					ViewItemHolder holder = new ViewItemHolder();
					convertView.setTag(holder);
					try {
						AndroidAnnotationParser.parse(holder, convertView);
					} catch (Exception e) {
						finish();
					}
					mItemPool.add(holder.mSwipeView);
					holder.mSwipeView.setListener(new SwipeView.Listener() {
						@Override
						public void onTap(SwipeView view) {
							int pos = (Integer) view.getTag();
//							mListener.onShopClick(pos - 1, (Shop) getItem(pos));
							ShopDetailActivity.newInstance(ShopListActivity.this, (Shop) getItem(pos));
						}
					});
				}

				FontsCollection.setFont(convertView);
			}

			switch (position) {
			case 0:
				break;

			default: {
				setShopItemData(convertView, (Shop) getItem(position), position);
			}
			}

			return convertView;
		}
	}

	public class PlaceListAdapter extends AbsShopListAdapter {

		private PlaceList mPlaceList;

		public PlaceListAdapter(PlaceList placeList, ListView lst, ArrayList<Shop> itemList, 
				int sort) {
			super(new GetPlaceList(ShopListActivity.this, 0, placeList.idPlacelist, sort), 
					lst, 3, itemList);
			mPlaceList = placeList;
		}

		@Override
		public void setSort(int sort) {
			mLoader.cancel(true);
			mLoader = mLoader.clone();
			mLoader.setListener(this);
			((GetPlaceList) mLoader).setSort(sort);

			mPageNum = 0;
			mItemList.clear();
			mItemList.add(new Shop());
			mItemList.add(new Shop());
			mLoading = false;
			mIsMore = true;
			notifyDataSetChanged();
		}

		@Override
		public int getItemViewType(int position) {
			int r = super.getItemViewType(position);
			if (r != -1)
				return r;
			else {
				switch (position) {
				case 0:
					return 0;
				case 1:
					return 1;
				default:
					return 2;
				}
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = super.getView(position, convertView, parent);

			if (position >= mItemList.size())
				return convertView;

			if (convertView == null) {
				switch (position) {
				case 0:
					convertView = mInflater.inflate(R.layout.shop_list_header, parent, false);
					setHeaderItemEvent(convertView);
					break;
				case 1:
					convertView = mInflater.inflate(R.layout.shop_list_placelist_header, parent, false);
					break;
				default:
					convertView = mInflater.inflate(R.layout.shop_list_item, parent, false);
					ViewItemHolder holder = new ViewItemHolder();
					convertView.setTag(holder);
					try {
						AndroidAnnotationParser.parse(holder, convertView);
					} catch (Exception e) {
						finish();
					}
					mItemPool.add(holder.mSwipeView);
					holder.mSwipeView.setListener(new SwipeView.Listener() {
						@Override
						public void onTap(SwipeView view) {
							int pos = (Integer) view.getTag();
//							mListener.onShopClick(pos - 2, (Shop) getItem(pos));
							ShopDetailActivity.newInstance(ShopListActivity.this, (Shop) getItem(pos));
						}
					});
				}

				FontsCollection.setFont(convertView);
			}

			switch (position) {
			case 0:
				break;

			case 1: {
				TextView txtName = (TextView) convertView.findViewById(R.id.txtName);
				TextView txtDsc = (TextView) convertView.findViewById(R.id.txtDescription);
				TextView txtAuthor = (TextView) convertView.findViewById(R.id.txtAuthor);
				TextView txtViews = (TextView) convertView.findViewById(R.id.txtViews);
				ImageView imgAva = (ImageView) convertView.findViewById(R.id.imgAva);

				txtName.setText(mPlaceList.title);
				txtDsc.setText(mPlaceList.description);
				SpannableString span = new SpannableString("by " + mPlaceList.authorName);
				span.setSpan(new ForegroundColorSpan(0xFFABABAB), 0, 2, 0);
				span.setSpan(new StyleSpan(Typeface.ITALIC), 0, 2, 0);
				txtAuthor.setText(span);
				txtViews.setText(CyUtils.group3digit(mPlaceList.numOfView) + " lượt xem");
				CyImageLoader.instance().showImage(mPlaceList.authorAvatar, imgAva);
				break;
			}

			default: {
				setShopItemData(convertView, (Shop) getItem(position), position);
			}
			}

			return convertView;
		}
	}

//	public void setListener(Listener listener) {
//		if (listener == null)
//			listener = new Listener();
//		mListener = listener;
//	}

	private int calcViewHeight(int rid) {
		View v = getLayoutInflater().inflate(rid, (ViewGroup) findViewById(android.R.id.content), false);
		int measureWidth = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		int measureHeight = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		v.measure(measureWidth, measureHeight);
		return v.getMeasuredHeight();
	}

	///////////////////////////////////////////////////////////////////////////
	// Listener
	///////////////////////////////////////////////////////////////////////////

	public static class Listener {
		public void onShopClick(int position, Shop s) {}
	}

	///////////////////////////////////////////////////////////////////////////
	// Debug stuff
	///////////////////////////////////////////////////////////////////////////
	private static final boolean isDebug = true;
	private static final String TAG = "CycrixDebug";
	private static final String HEADER = "ShopListFragment";
	private static void debugLog(String message) {
		if (CyUtils.isDebug && isDebug) Log.d(TAG, HEADER + " " + message);
	}
}