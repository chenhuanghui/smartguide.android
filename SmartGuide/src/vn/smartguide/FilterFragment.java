package vn.smartguide;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;


/**
 * Created by cycrixlaptop on 7/26/13.
 */
public class FilterFragment extends Fragment {
	
	private MainAcitivyListener mMainAcitivyListener = null;
	
	private final int TEXT_CHECK_COLOR = 0xFFFFFFFF;
	private final int TEXT_UNCHECK_COLOR = 0xFF9FA6AC;
	private final int[] mTextViewIDArr = new int[] {
			R.id.txtAllFields,
			R.id.txtAllFieldsVN,
			R.id.txtSort,
			R.id.txtSortVN,
	};
	
	private final int[] mRadioIDArr = new int[] {
			R.id.radioGetAward,
			R.id.radioMostPoint,
			R.id.radioMostLike,
			R.id.radioMostView,
			R.id.radioDistance,
	};
	
	private final FilterItem[] mItemList = new FilterItem[] {
			new FilterItem(R.drawable.icon12, R.drawable.icon12_gray, R.drawable.iconpin_food, "ĂN", "food"),
			new FilterItem(R.drawable.icon13, R.drawable.icon13_gray, R.drawable.iconpin_drink, "UỐNG", "drink"),
			new FilterItem(R.drawable.icon14, R.drawable.icon14_gray, R.drawable.iconpin_healness, "SỨC KHỎE", "health&fitness"),
			new FilterItem(R.drawable.icon15, R.drawable.icon15_gray, R.drawable.iconpin_entertaiment, "GIẢI TRÍ", "entertainment"),
			new FilterItem(R.drawable.icon16, R.drawable.icon16_gray, R.drawable.iconpin_fashion, "THỜI TRANG", "fashion"),
			new FilterItem(R.drawable.icon17, R.drawable.icon17_gray, R.drawable.iconpin_travel, "DU LỊCH", "travel"),
			new FilterItem(R.drawable.icon18, R.drawable.icon18_gray, R.drawable.iconpin_shopping, "MUA SẮM", "production"),
			new FilterItem(R.drawable.icon19, R.drawable.icon19_gray, R.drawable.iconpin_education, "GIÁO DỤC", "education"),
	};

	private FilterAdapter mFilterAdapter;
	public boolean mShowContent = false;

	private ImageButton mDoneBtn = null;
	
	private RadioButton mRadioGetAward = null;
	private RadioButton mRadioMostPoint = null;
	private RadioButton mRadioMostLike = null;
	private RadioButton mRadioMostView = null;
	private RadioButton mRadioDistance = null;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mMainAcitivyListener = (MainAcitivyListener) activity;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		return inflater.inflate(R.layout.filter_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		mRadioGetAward = (RadioButton)getView().findViewById(R.id.radioGetAward);
		mRadioMostPoint = (RadioButton)getView().findViewById(R.id.radioMostPoint);
		mRadioMostLike = (RadioButton)getView().findViewById(R.id.radioMostLike);
		mRadioMostView = (RadioButton)getView().findViewById(R.id.radioMostView);
		mRadioDistance = (RadioButton)getView().findViewById(R.id.radioDistance);
		
		mDoneBtn = (ImageButton)getView().findViewById(R.id.imageButton);
		mDoneBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				((ShopListFragment)(mMainAcitivyListener).getShopListFragment()).setForeground();
				mMainAcitivyListener.goToPage(1);
				
				toggle();
				
				GlobalVariable.mFilterString = "";
				GlobalVariable.mSortByString = "";
				
				if (mRadioGetAward.isChecked())
					GlobalVariable.mSortByString = "4";
				
				if (mRadioMostPoint.isChecked())
					GlobalVariable.mSortByString = "3";
				
				if (mRadioMostLike.isChecked())
					GlobalVariable.mSortByString = "2";
				
				if (mRadioMostView.isChecked())
					GlobalVariable.mSortByString = "1";
				
				if (mRadioDistance.isChecked())
					GlobalVariable.mSortByString = "0";
				
				FilterAdapter adapter = (FilterAdapter)((GridView) getView().findViewById(R.id.gridCate)).getAdapter();
				if (adapter.mItemList.get(0).status == true)
					GlobalVariable.mFilterString = "1";
				
				for(int i = 1; i < adapter.mItemList.size(); i++){
					if (adapter.mItemList.get(i).status == true)
						GlobalVariable.mFilterString += ", " + Integer.toString(i + 1);
				}
					
				new FindShopList().execute();
			}
		});
		
		// Set font
		Typeface typeFace = Typeface.createFromAsset(getActivity().getAssets(), "fonts/utmhelvetints.ttf");
		for (int i : mTextViewIDArr) {
			((TextView) getView().findViewById(i)).setTypeface(typeFace);
		}

		for (int i : mRadioIDArr) {
			((RadioButton) getView().findViewById(i)).setTypeface(typeFace);
		}

		// Set filter adapter
		mFilterAdapter = new FilterAdapter();
		GridView grid = (GridView) getView().findViewById(R.id.gridCate);
		grid.setAdapter(mFilterAdapter);
		grid.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				mFilterAdapter.onCheckChange(position);
			}
		});

		// Set button event
		((Button) getView().findViewById(R.id.btnSelectAll)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mFilterAdapter.selectAll(true);
			}
		});

		((Button) getView().findViewById(R.id.btnDeselectAll)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mFilterAdapter.selectAll(false);
			}
		});

		// Invisible
		View layout = getView().findViewById(R.id.layoutContentFrame2);
		layout.setVisibility(View.GONE);
	}

	public void toggle() {
		mShowContent = !mShowContent;
		ObjectAnimator animator = null;
		int height = getActivity().findViewById(R.id.linearLayout).getHeight();
		View layout = getView().findViewById(R.id.layoutContentFrame2);
		layout.setVisibility(View.VISIBLE);
		if (mShowContent)
			animator = ObjectAnimator.ofFloat(layout, "translationY", -height, 0);
		else
			animator = ObjectAnimator.ofFloat(layout, "translationY", 0, -height);

		animator.setInterpolator(new AccelerateDecelerateInterpolator());
		animator.start();
	}

	private class FilterItem {

		int RID;
		int RIDg;
		int symbol;
		String nameVN;
		String name;
		boolean status;

		public FilterItem(int RID, int RIDg, int symbol, String nameVN, String name) {
			this.RID    = RID;
			this.RIDg   = RIDg;
			this.symbol = symbol;
			this.nameVN = nameVN;
			this.name   = name;
			this.status = true;
		}
	}

	public class FilterAdapter extends BaseAdapter {

		private final int ITEM_NUM = FilterFragment.this.mItemList.length;
		private LayoutInflater inflater;
		public List<FilterItem> mItemList;

		public FilterAdapter() {
			inflater = FilterFragment.this.getActivity().getLayoutInflater();
			mItemList = new ArrayList<FilterItem>();

			for (FilterItem item : FilterFragment.this.mItemList)
				mItemList.add(item);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				convertView = inflater.inflate(R.layout.filter_item, null);
			}

			// Update view
			updateItemView(convertView, position);

			return convertView;
		}

		private void updateItemView(View convertView, int pos) {

			FilterItem item = mItemList.get(pos);

			// Set big icon
			ImageView imgBigIcon = (ImageView) convertView.findViewById(R.id.imgBigIcon);
			if (item.status)
				imgBigIcon.setImageResource(mItemList.get(pos).RID);
			else
				imgBigIcon.setImageResource(mItemList.get(pos).RIDg);

			// Small icon
			((ImageView) convertView.findViewById(R.id.imgPin))
			.setImageResource(mItemList.get(pos).symbol);

			// Set text color
			TextView txtCateNameVN = (TextView) convertView.findViewById(R.id.txtCateNameVN);
			TextView txtCateName = (TextView) convertView.findViewById(R.id.txtCateName);

			txtCateNameVN.setText(mItemList.get(pos).nameVN);
			txtCateName.setText(mItemList.get(pos).name);

			if (item.status) {
				txtCateNameVN.setTextColor(TEXT_CHECK_COLOR);
				txtCateName.setTextColor(TEXT_CHECK_COLOR);
			} else {
				txtCateNameVN.setTextColor(TEXT_UNCHECK_COLOR);
				txtCateName.setTextColor(TEXT_UNCHECK_COLOR);
			}
		}

		public void onCheckChange(int pos) {

			mItemList.get(pos).status = !mItemList.get(pos).status;
			notifyDataSetChanged();
		}

		public void selectAll(boolean isSelect) {

			for (FilterItem item : mItemList)
				item.status = isSelect;
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return ITEM_NUM;
		}

		@Override
		public Object getItem(int i) {
			return null;
		}

		@Override
		public long getItemId(int i) {
			return i;
		}
	}
	
	public class FindShopList extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("group_list", GlobalVariable.mFilterString));
			pairs.add(new BasicNameValuePair("city_id", GlobalVariable.mCityID));
			pairs.add(new BasicNameValuePair("user_id", GlobalVariable.userID));
			pairs.add(new BasicNameValuePair("user_lat", Float.toString(GlobalVariable.mLat)));
			pairs.add(new BasicNameValuePair("user_lng", Float.toString(GlobalVariable.mLng)));
			pairs.add(new BasicNameValuePair("page", "0"));
			pairs.add(new BasicNameValuePair("sort_by", GlobalVariable.mSortByString));

			String json = NetworkManger.post(APILinkMaker.ShopListInCategory(), pairs);
			((ShopListFragment)(mMainAcitivyListener).getShopListFragment()).mHaveAnimation = true;
			(mMainAcitivyListener).getShopListFragment().update(json);
			return true;
		}

		@Override
		protected void onPostExecute(Boolean k){
			List<ObjectAnimator> arrayListObjectAnimators = new ArrayList<ObjectAnimator>();
			
		}

		@Override
		protected void onPreExecute(){
			
		}
	}
}
