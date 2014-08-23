package vn.infory.infory.notification;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import vn.infory.infory.CyUtils;
import vn.infory.infory.FontsCollection;
import vn.infory.infory.R;
import vn.infory.infory.WebActivity;
import vn.infory.infory.data.Message.buttons;
import vn.infory.infory.data.PlaceList;
import vn.infory.infory.data.Shop;
import vn.infory.infory.network.CyAsyncTask;
import vn.infory.infory.network.GetPlaceListDetail;
import vn.infory.infory.network.NetworkManager;
import vn.infory.infory.shopdetail.ShopDetailActivity;
import vn.infory.infory.shoplist.ShopListActivity;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;

public class ListButtonAdapter extends BaseAdapter {

	private static final String TAG = "Infory ListButtonAdapter: ";

	private Context mContext;
	private List<buttons> lstButtons;
	private LayoutInflater mInflater;
	private List<CyAsyncTask> mTaskList = new ArrayList<CyAsyncTask>();

	public ListButtonAdapter(Context mContext, List<buttons> lstButtons) {
		this.lstButtons = lstButtons;
		this.mContext = mContext;
		mInflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return lstButtons.size();
	}

	@Override
	public Object getItem(int position) {
		return lstButtons.get(position);
	}

	@Override
	public long getItemId(int position) {
		return lstButtons.get(position).hashCode();
	}

	private class ViewHolder {
		Button btnGoto;
		FrameLayout layoutLoading;
		FrameLayout activityPlacelistLayoutLoadingAni;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final buttons item = lstButtons.get(position);
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.button_goto_layout,
					parent, false);
			holder = new ViewHolder();
			holder.btnGoto = (Button) convertView.findViewById(R.id.btnGoto);
			holder.layoutLoading = (FrameLayout) convertView
					.findViewById(R.id.layoutLoading);
			holder.activityPlacelistLayoutLoadingAni = (FrameLayout) convertView
					.findViewById(R.id.activityPlacelistLayoutLoadingAni);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		initGUI(holder, item, position);
		initEvents(holder, item, position);
		FontsCollection.setFont(convertView);
		return convertView;
	}

	private void initGUI(ViewHolder holder, buttons item, int position) {
		holder.layoutLoading.setVisibility(View.INVISIBLE);
		holder.btnGoto.setText(item.getActionTitle());
	}

	private void initEvents(final ViewHolder holder, final buttons item,
			int position) {
		holder.btnGoto.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.e(TAG, "action type when click is: " + item.getActionType());
//				item.setActionType(0);
//				item.setMethod(1);
//				item.setUrl("www.google.com");
//				item.setParams("{\"param1\":\"abc\", \"param2\":\"123\"}");
				if (item.getActionType() == 0) {
					// call API
					Log.e(TAG, "method is: " + item.getMethod());
					if (item.getMethod() == 0) {
						// GET
						GetMethod task = new GetMethod(mContext, item.getUrl());
						task.execute();
					} else if (item.getMethod() == 1) {
						// POST
						PostMethod task = new PostMethod(mContext, item
								.getUrl(), item.getParams());
						task.execute();
					}
				} else if (item.getActionType() == 1) {
					// open shop info
					ShopDetailActivity.newInstance((Activity) mContext,
							makeShop(item.getIdShop()));
				} else if (item.getActionType() == 2) {
					// open shop list
					Log.e(TAG, "keyword value is: " + item.getKeywords());
					Log.e(TAG, "idShops value is: " + item.getIdShops());
					Log.e(TAG, "idPlacelist value is: " + item.getIdPlacelist());
					if (item.getKeywords() != null
							&& item.getKeywords().length() > 0) {
						// - *keywords*: string: search 1 từ khóa
						ShopListActivity.newInstance((Activity) mContext,
								item.getKeywords(), new ArrayList<Shop>());
					} else if (item.getIdShops() != null
							&& item.getIdShops().length() > 0) {
						// - *idShops*: string `ex: 1,2,5,7`: mở 1 list các địa
						// điểm định sẵn
						String idShops = item.getIdShops().substring(1,
								item.getIdShops().length() - 1);
						Log.d(TAG, "idShops after substring: " + idShops);
						ShopListActivity.newInstance((Activity) mContext,
								idShops, new ArrayList<Shop>(), 0);
					} else if (item.getIdPlacelist() > 0) {
						// - *idPlacelist*: int: mở 1 placelist theo id
						// If place list, then load placeList
						GetPlaceListDetail getPlacelistDetail = new GetPlaceListDetail(
								mContext, item.getIdPlacelist(), 0) {
							@Override
							protected void onCompleted(Object result2) {
								mTaskList.remove(this);

								// Open place list
								Object[] result = (Object[]) result2;
								ShopListActivity.newInstance(
										(Activity) mContext,
										(PlaceList) result[0],
										new ArrayList<Shop>());
							}

							@Override
							protected void onFail(Exception e) {
								mTaskList.remove(this);

								CyUtils.showError("Không thể lấy placelist", e,
										mContext);
							}
						};
						getPlacelistDetail.setVisibleView(holder.layoutLoading);
						mTaskList.add(getPlacelistDetail);
						getPlacelistDetail
								.executeOnExecutor(NetworkManager.THREAD_POOL);

						AnimationDrawable frameAnimation = (AnimationDrawable) holder.activityPlacelistLayoutLoadingAni
								.getBackground();
						frameAnimation.start();
					}
				} else if (item.getActionType() == 3) {
					// open webview
					WebActivity.newInstance((Activity) mContext, item.getUrl());
				}
			}
		});
	}

	public Shop makeShop(int idShop) {
		Shop s = new Shop();
		s.idShop = idShop;
		return s;
	}

	public class GetMethod extends CyAsyncTask {
		private String url;

		public GetMethod(Context c, String url) {
			super(c);
			this.url = url;
		}

		@Override
		protected Object doInBackground(Object... arg0) {

			try {
				String json = NetworkManager.get(url, false);
				Log.d(TAG, "json response: " + json);
				return json;
			} catch (Exception e) {
				mEx = e;
			}

			return super.doInBackground(arg0);
		}
	}

	public class PostMethod extends CyAsyncTask {
		public String TAG = "Infory PostMethod";
		// Data
		private String url, params;

		public PostMethod(Context c, String url, String params) {
			super(c);
			this.url = url;
			this.params = params;
		}

		@Override
		protected Object doInBackground(Object... arg0) {
			try {
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				params = params.trim().substring(1, params.length() - 1);
				String[] arrParams = params.split(",");
				if(arrParams != null && arrParams.length > 0) {
					for(int i = 0; i < arrParams.length; i++) {
						String param = arrParams[i].trim();
						String[] keyPairValues = param.split(":");
						String key = keyPairValues[0].substring(1, keyPairValues[0].length() - 1);
						pairs.add(new BasicNameValuePair(key, keyPairValues[1]));
					}
				}
				String json = NetworkManager.post(url, pairs, false);
				Log.d(TAG, "json response: " + json);
				return json;
			} catch (Exception e) {
				mEx = e;
			}
			return super.doInBackground(arg0);
		}
	}

}
