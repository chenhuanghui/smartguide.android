package vn.infory.infory;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import vn.infory.infory.data.AutoCompleteItem;
import vn.infory.infory.data.PlaceList;
import vn.infory.infory.data.Settings;
import vn.infory.infory.data.Shop;
import vn.infory.infory.data.AutoCompleteItem.Fields;
import vn.infory.infory.data.AutoCompleteItem.Highlight;
import vn.infory.infory.network.AutoComplete;
import vn.infory.infory.network.CyAsyncTask;
import vn.infory.infory.network.GetPlaceListDetail;
import vn.infory.infory.network.GetPlaceListList;
import vn.infory.infory.network.GetShopDetail;
import vn.infory.infory.network.NetworkManager;
import vn.infory.infory.network.Search;
import vn.infory.infory.shopdetail.ShopDetailActivity;
import vn.infory.infory.shoplist.ShopListActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;

public class PlaceListListActivity extends Activity {

	// Data
//	private Listener mListener = new SimpleListener();
//	private CyAsyncTask mSearchTask;
	private PlaceListAdapter mPlaceListAdapter;
	private Map<String, List<AutoCompleteItem>> mAutoCompCache = 
			new HashMap<String, List<AutoCompleteItem>>();
	private AutoComplete mCurrentAutoCompRequest;
	private List<CyAsyncTask> mTaskList = new ArrayList<CyAsyncTask>();

	// GUI
	@ViewById(id = R.id.edtSearch)		private EditText mEdtSearch;
	@ViewById(id = R.id.lst)			private ListView mLst;
	@ViewById(id = R.id.btnSelectCity)	private Button mBtnSelectCity;
	@ViewById(id = R.id.layoutLoading)	private View mLayoutLoading;
	@ViewById(id = R.id.activityPlacelistLayoutLoadingAni)	private View mLayoutLoadingAni;

	///////////////////////////////////////////////////////////////////////////
	// Override methods
	///////////////////////////////////////////////////////////////////////////

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_place_list_list);

		try {
			View rootView = findViewById(android.R.id.content);
			AndroidAnnotationParser.parse(this, rootView);
			FontsCollection.setFont(rootView);
		} catch (Exception e) {
			finish();
		}

		// Set on search event
		mEdtSearch.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

				if (actionId == EditorInfo.IME_ACTION_SEARCH) {

					// Check input keyword
					final String keyword = v.getText().toString().trim();

					// Alert if keyword length = 0
					if (keyword.length() == 0) {
						new AlertDialog.Builder(PlaceListListActivity.this)
						.setMessage("Xin nhập nội dung tìm kiếm")
						.setPositiveButton(R.string.OK, null)
						.create().show();
					}

					ShopListActivity.newInstance(PlaceListListActivity.this, keyword, 
							new ArrayList<Shop>());
					return true;
				}
				return false;
			}
		});
		
		Settings s = Settings.instance();
		mBtnSelectCity.setText(" \" " + s.cityName + " \" ");

		// Set up listview
		mPlaceListAdapter = new PlaceListAdapter();

		mLst.setAdapter(mPlaceListAdapter);
		mLst.setOnScrollListener(mPlaceListAdapter);
		mLst.setOnItemClickListener(mPlaceListAdapter);

		// Set up auto complete
		mEdtSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

				String keyword = s.toString().trim();

				if (keyword.length() == 0) {
					if (mCurrentAutoCompRequest != null)
						mCurrentAutoCompRequest.cancel(true);
					mLst.setAdapter(mPlaceListAdapter);
					mLst.setOnItemClickListener(mPlaceListAdapter);
					mLst.setOnScrollListener(mPlaceListAdapter);
				} else
					showAuoCompleteItem(s.toString().trim());
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void afterTextChanged(Editable s) {}
		});
		
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

	///////////////////////////////////////////////////////////////////////////
	// Public methods
	///////////////////////////////////////////////////////////////////////////

	public static void newInstance(Activity act) {

		Intent intent = new Intent(act, PlaceListListActivity.class);
		act.startActivity(intent);
		act.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		Settings s = Settings.instance();
		mBtnSelectCity.setText(" \" " + s.cityName + " \" ");
	}

	@Override
	public void finish() {
		for (CyAsyncTask task : mTaskList)
			task.cancel(true);

		super.finish();
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

	@Click(id = R.id.btnCancel)
	private void onCancelClick(View v) {
		finish();
	}
	
	@Click(id = R.id.btnClearSearch)
	private void onClearSearchClick(View v) {
		mEdtSearch.setText("");
	}
	
	@Click(id = R.id.btnSelectCity)
	private void onBtnSelectCityClick(View v) {
		CityList.newInstance(PlaceListListActivity.this);
	}

	///////////////////////////////////////////////////////////////////////////
	// Private methods
	///////////////////////////////////////////////////////////////////////////

	private void showAuoCompleteItem(final String keyword) {

		// Check cache		
		if (mAutoCompCache.containsKey(keyword)) {
			// if found, display it
			List<AutoCompleteItem> result = mAutoCompCache.get(keyword);
			AutoCompleteAdapter autoAdapter = new AutoCompleteAdapter(result, keyword);
			mLst.setAdapter(autoAdapter);
			mLst.setAdapter(autoAdapter);
			mLst.setOnScrollListener(null);
		} else {
			// if not found, cancel current request, make new request
			if (mCurrentAutoCompRequest != null)
				mCurrentAutoCompRequest.cancel(true);

			mCurrentAutoCompRequest = new AutoComplete(PlaceListListActivity.this, 
					keyword.toString().trim()) {
				@Override
				protected void onCompleted(Object result2) {
					mTaskList.remove(this);

					// cache result
					List<AutoCompleteItem> result = (List<AutoCompleteItem>) result2;
					mAutoCompCache.put(keyword, result);

					// display it
					AutoCompleteAdapter autoAdapter = new AutoCompleteAdapter(result, keyword);
					mLst.setAdapter(autoAdapter);
					mLst.setOnItemClickListener(autoAdapter);
					mLst.setOnScrollListener(null);
				}

				@Override
				protected void onFail(Exception e) {
					mTaskList.remove(this);

					// display empty
					AutoCompleteAdapter autoAdapter = new AutoCompleteAdapter(
							new ArrayList<AutoCompleteItem>(), keyword);
					mLst.setAdapter(autoAdapter);
					mLst.setOnItemClickListener(autoAdapter);
					mLst.setOnScrollListener(null);
				}
			};
			mTaskList.add(mCurrentAutoCompRequest);
			mCurrentAutoCompRequest.executeOnExecutor(NetworkManager.THREAD_POOL);
		}
	}

	private Spannable makeHighlight(String highlightXml, String fallback) {

		try {
			highlightXml = "<root>" + highlightXml + "</root>";

			InputSource is = new InputSource(new StringReader(highlightXml));
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
			NodeList nodeList = doc.getDocumentElement().getChildNodes();

			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < nodeList.getLength(); i++)
				builder.append(nodeList.item(i).getTextContent());

			SpannableString span = new SpannableString(builder.toString());
			StyleSpan boldStyle = new StyleSpan(Typeface.BOLD);
			int pos = 0;
			for (int i = 0; i < nodeList.getLength(); i++) {
				int len = nodeList.item(i).getTextContent().length();
				if (nodeList.item(i).getNodeName() != null
						&& nodeList.item(i).getNodeName().equalsIgnoreCase("em"))
					span.setSpan(boldStyle, pos, pos + len, 0);
				pos += len;
			}

			return span;
		} catch (Exception e) {
			return new SpannableString(fallback);
		}
	}

	///////////////////////////////////////////////////////////////////////////
	// Adapter
	///////////////////////////////////////////////////////////////////////////

	public class AutoCompleteAdapter extends BaseAdapter implements OnItemClickListener {

		private List<AutoCompleteItem> mItemList;
		private LayoutInflater mInflater;

		public AutoCompleteAdapter(List<AutoCompleteItem> itemList, String keyword) {
			mItemList = new ArrayList<AutoCompleteItem>();
			mInflater = PlaceListListActivity.this.getLayoutInflater();

			List<AutoCompleteItem> shopList = new ArrayList<AutoCompleteItem>();
			List<AutoCompleteItem> placeList = new ArrayList<AutoCompleteItem>();

			for (AutoCompleteItem item : itemList) {
				if (item._type.equalsIgnoreCase("shop")) {
					shopList.add(item);
				} else if (item._type.equalsIgnoreCase("placelist")){
					placeList.add(item);
				}
			}

			if (shopList.size() != 0) {
				setRIDType(shopList);
				AutoCompleteItem fakeItem = new AutoCompleteItem();
				fakeItem.RIDtype = 4;
				fakeItem.highlight = new Highlight();
				fakeItem.highlight.name_auto_complete.add("Cửa hàng");
				fakeItem.fields = new Fields();
				fakeItem.fields.name = "Cửa hàng";
				mItemList.add(fakeItem);
				mItemList.addAll(shopList);
			}

			if (placeList.size() != 0) {
				setRIDType(placeList);
				AutoCompleteItem fakeItem = new AutoCompleteItem();
				fakeItem.RIDtype = 4;
				fakeItem.highlight = new Highlight();
				fakeItem.highlight.name_auto_complete.add("Placelist");
				fakeItem.fields = new Fields();
				fakeItem.fields.name = "Placelist";
				mItemList.add(fakeItem);
				mItemList.addAll(placeList);

			}

			if (itemList.size() == 0) {
				AutoCompleteItem fakeItem = new AutoCompleteItem();
				fakeItem.RIDtype = 4;
				fakeItem.highlight = new Highlight();
				fakeItem.highlight.name_auto_complete.add("Tìm kiếm với \"" + keyword + "\"");
				fakeItem.fields = new Fields();
				fakeItem.fields.name = "Tìm kiếm với \"" + keyword + "\"";
				mItemList.add(fakeItem);
			}
		}

		private void setRIDType(List<AutoCompleteItem> list) {
			if (list.size() == 1)
				list.get(0).RIDtype = 3;
			else
				for (int i = 0; i < list.size(); i++) {
					AutoCompleteItem item = list.get(i);
					if (i == 0)
						item.RIDtype = 0;
					else if (i == list.size() - 1)
						item.RIDtype = 2;
					else 
						item.RIDtype = 1;
				}
		}

		@Override
		public int getCount() {
			return mItemList.size();
		}

		@Override
		public AutoCompleteItem getItem(int pos) {
			return mItemList.get(pos);
		}

		@Override
		public long getItemId(int pos) {
			return pos;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {

				switch (getItemViewType(position)) {
				case 0:
					convertView = mInflater.inflate(R.layout.autocomplete_item_header, parent, false);
					break;
				case 1:
					convertView = mInflater.inflate(R.layout.autocomplete_item_body, parent, false);
					break;
				case 2:
					convertView = mInflater.inflate(R.layout.autocomplete_item_footer, parent, false);
					break;
				case 3:
					convertView = mInflater.inflate(R.layout.autocomplete_item, parent, false);
					break;
				case 4:
					convertView = mInflater.inflate(R.layout.autocomplete_item_fake, parent, false);
					break;
				}
				
				FontsCollection.setFont(convertView);
			}

			AutoCompleteItem item = getItem(position);

			TextView txtName = (TextView) convertView.findViewById(R.id.txtName);

			if (getItemViewType(position) != 4) {
				ImageView imgIcon = (ImageView) convertView.findViewById(R.id.imgIcon);
				if (item._type.equalsIgnoreCase("shop")) {
					if (item.fields.hasPromotion)
						imgIcon.setImageResource(R.drawable.icon_promotion_search);
					else
						imgIcon.setImageResource(R.drawable.icon_keyword_search);
				} else if (item._type.equalsIgnoreCase("placelist")) {
					imgIcon.setImageResource(R.drawable.icon_playlist_search);
				}
			}

			if (item.highlight.name_auto_complete.size() != 0)
				txtName.setText(makeHighlight(
						item.highlight.name_auto_complete.get(0), item.fields.name));
			else
				txtName.setText(makeHighlight(
						item.highlight.shop_name_auto_complete.get(0), item.fields.shop_name));

			return convertView;
		}

		@Override
		public int getViewTypeCount() {
			return 5;
		}

		@Override
		public int getItemViewType(int position) {
			return mItemList.get(position).RIDtype;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			// Check Shop or place list ?

			AutoCompleteItem item = getItem(position);
			if (item._type == null)
				return;

			if (item._type.equalsIgnoreCase("shop")) {

				// If shop, then load shop
				Shop s = new Shop();
				GetShopDetail getShopDetailTask = 
						new GetShopDetail(PlaceListListActivity.this, s, item.fields.id) {
					@Override
					protected void onCompleted(Object result) {
						mTaskList.remove(this);

						// Open shop
						ShopDetailActivity.newInstanceNoReload(PlaceListListActivity.this, (Shop) result);
					}

					@Override
					protected void onFail(Exception e) {
						mTaskList.remove(this);

						CyUtils.showError("Không thể lấy chi tiết cửa hàng", e, PlaceListListActivity.this);
					}
				};
				getShopDetailTask.setVisibleView(mLayoutLoading);				
				mTaskList.add(getShopDetailTask);
				getShopDetailTask.executeOnExecutor(NetworkManager.THREAD_POOL);
				
				AnimationDrawable frameAnimation = (AnimationDrawable) 
						mLayoutLoadingAni.getBackground();
				frameAnimation.start();

			} else if (item._type.equalsIgnoreCase("placelist")) {

				// If place list, then load placeList
				GetPlaceListDetail getPlacelistDetail = 
						new GetPlaceListDetail(PlaceListListActivity.this, item.fields.id, 0) {
					@Override
					protected void onCompleted(Object result2) {
						mTaskList.remove(this);

						// Open place list
						Object[] result = (Object[]) result2;
						ShopListActivity.newInstance(PlaceListListActivity.this,
								(PlaceList) result[0], new ArrayList<Shop>());
					}

					@Override
					protected void onFail(Exception e) {
						mTaskList.remove(this);

						CyUtils.showError("Không thể lấy placelist", e, PlaceListListActivity.this);
					}
				};
				getPlacelistDetail.setVisibleView(mLayoutLoading);
				mTaskList.add(getPlacelistDetail);
				getPlacelistDetail.executeOnExecutor(NetworkManager.THREAD_POOL);
				
				AnimationDrawable frameAnimation = (AnimationDrawable) 
						mLayoutLoadingAni.getBackground();
				frameAnimation.start();
			}
		}
	}

	///////////////////////////////////////////////////////////////////////////
	// Place list Adapter
	///////////////////////////////////////////////////////////////////////////

	public class PlaceListAdapter extends LazyLoadAdapter implements OnItemClickListener {

		public PlaceListAdapter() {
			super(PlaceListListActivity.this, 
					new GetPlaceListList(PlaceListListActivity.this, 0), 
					R.layout.shop_list_loading, 
					2, new ArrayList<PlaceList>());
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

			int type = getItemViewType(position);
			if (convertView == null) {
				switch (type) {
				case 0:
					convertView = mInflater.inflate(R.layout.place_list_list_top_item, parent, false);
					break;
				case 1:
					convertView = mInflater.inflate(R.layout.place_list_list_item, parent, false);
					break;
				}
				FontsCollection.setFont(convertView);
			}

			if (position >= mItemList.size())
				return convertView;

			PlaceList item = (PlaceList) getItem(position);

			if (type == 0) {
				if (position == 2) {
					View layoutLine = convertView.findViewById(R.id.layoutLine);
					layoutLine.setVisibility(View.GONE);
				}

				TextView txtContent = (TextView) convertView.findViewById(R.id.txtContent);
				txtContent.setText(item.description);
				
				ImageView imgIcon = (ImageView) convertView.findViewById(R.id.imgIcon);
				CyImageLoader.instance().showImage(item.image, imgIcon);

			} else {
				if (mItemList.size() == 4) {
					convertView.setBackgroundResource(R.drawable.button_call);
				} else {
					if (position == 3) {
						convertView.setBackgroundResource(R.drawable.frame_detail_info);
					} else if (!mIsMore && position == mItemList.size() - 1) {
						convertView.setBackgroundResource(R.drawable.frame_detail_info_footer);
					} else {
						convertView.setBackgroundResource(R.drawable.frame_detail_info_item);
					}
				}
			}

			TextView txtName = (TextView) convertView.findViewById(R.id.txtName);
			txtName.setText(item.title);

			return convertView;
		}

		@Override
		public int getItemViewType(int position) {

			int type = super.getItemViewType(position);
			if (type < 0) {
				if (position < 3)
					return 0;
				else
					return 1;
			} else
				return type;
		}
		
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			final PlaceList placeList = (PlaceList) getItem(position);
			ShopListActivity.newInstance(mAct, placeList, new ArrayList<Shop>());
		}
	}

	///////////////////////////////////////////////////////////////////////////
	// Listener
	///////////////////////////////////////////////////////////////////////////
}