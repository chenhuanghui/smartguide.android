package vn.infory.infory.store;

import java.util.ArrayList;

import vn.infory.infory.CyUtils;
import vn.infory.infory.R;
import vn.infory.infory.R.id;
import vn.infory.infory.R.layout;
import vn.infory.infory.R.string;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;

public class StoreMenuFragment extends Fragment implements OnItemClickListener {

	// Data
	private Listener mListener = new Listener();
	private int mMode = 0; 	// 	0: newest
							//	1: top sellers

	// GUI elements
	@ViewById(id = R.id.gridMenu)		private GridView mGrid;
	@ViewById(id = R.id.btnNewest)		private Button mBtnNewest;
	@ViewById(id = R.id.btnTopSellers)	private Button mBtnTopSellers;

	private StoreMenuAdapter mAdapter;

	///////////////////////////////////////////////////////////////////////////
	// Override methods
	///////////////////////////////////////////////////////////////////////////

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.store_menu, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		try {
			AndroidAnnotationParser.parse(this, view);
		} catch (Exception e) {
			getActivity().finish();
		}

		mAdapter = new StoreMenuAdapter();
		mGrid.setAdapter(mAdapter);
		mGrid.setOnItemClickListener(this);
		
		CyUtils.setHoverEffect(mBtnNewest, false);
		CyUtils.setHoverEffect(mBtnTopSellers, false);
	}
	
	@Override
	public void onResume() {
		super.onResume();

		updateButtonAppearance();
	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		mListener.onSeeMoreClick(0);
	}
	
	@Click(id = R.id.btnNewest)
	private void onNewestClick(View v) {
		switchMode(0);
	}

	@Click(id = R.id.btnTopSellers)
	private void onTopSellersClick(View v) {
		switchMode(1);
	}

	///////////////////////////////////////////////////////////////////////////
	// Public methods
	///////////////////////////////////////////////////////////////////////////

	public void setListener(Listener listener) {
		if (listener == null)
			listener = new Listener();
		mListener = listener;
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Private methods
	///////////////////////////////////////////////////////////////////////////
	
	private void switchMode(int mode) {
		if (mMode == mode)
			return;

		mMode = mode;

		updateButtonAppearance();

		// Change listview
		mGrid.startLayoutAnimation();
	}
	
	private void updateButtonAppearance() {
		// Change button appearance
		Button btnDisable = mMode == 1 ? mBtnNewest : mBtnTopSellers;
		Button btnEnable = mMode == 0 ? mBtnNewest : mBtnTopSellers;

		btnDisable.setTextColor(0xFF9B9B9B);
		btnEnable.setTextColor(0xFF373634);

		Resources res = getResources();
		// Make disable span
		String str = res.getString(mMode == 0 ? 
				R.string.store_shop_newest : R.string.store_shop_top_sellers);
		btnDisable.setText(str);

		// Make enable span
		str = res.getString(mMode == 1 ? 
				R.string.store_shop_newest : R.string.store_shop_top_sellers);
		SpannableString span = new SpannableString(str);
		span.setSpan(new UnderlineSpan(), 0, str.length(), 0);
		btnEnable.setText(span);
	}

	///////////////////////////////////////////////////////////////////////////
	// Adapter
	///////////////////////////////////////////////////////////////////////////

	public class StoreMenuAdapter extends ArrayAdapter<String> {

		public StoreMenuAdapter() {
			super(getActivity(), R.layout.store_menu_item, R.id.txtName, new ArrayList<String>());

			for (int i = 0; i < 9; i++)
				add("Cà phê Ameriacano");
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View root = super.getView(position, convertView, parent);

			if (convertView == null) {
				CyUtils.setHoverEffect(root.findViewById(R.id.imgBuy));
				CyUtils.setHoverEffect(root.findViewById(R.id.imgMore), false);
				
				root.findViewById(R.id.imgMore).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						mListener.onSeeMoreClick(0);
					}
				});
			}
			
			convertView = root;

			return convertView;
		}
	} 

	///////////////////////////////////////////////////////////////////////////
	// Listener
	///////////////////////////////////////////////////////////////////////////

	public static class Listener {
		public void onSeeMoreClick(int shopId) {}
		public void onBuyClick(int shopId) {}
	}
}