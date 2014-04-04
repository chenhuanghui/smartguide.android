package vn.infory.infory.store;

import java.util.ArrayList;

import vn.infory.infory.CyUtils;
import vn.infory.infory.R;
import vn.infory.infory.R.id;
import vn.infory.infory.R.layout;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.ViewById;

public class StorePaymentFragment extends Fragment {
	
	// Data
	
	// GUI
	@ViewById(id = R.id.lstItem) 	private ListView mLst;
	@ViewById(id = R.id.btnBuy)		private ImageButton mBtnBuy;
	
	private StorePaymentAdapter mAdapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.store_payment, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		try {
			AndroidAnnotationParser.parse(this, view);
		} catch (Exception e) {
			getActivity().finish();
		}
		
		mAdapter = new StorePaymentAdapter();
		mLst.setAdapter(mAdapter);
		
		CyUtils.setHoverEffect(mBtnBuy);
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Adapter
	///////////////////////////////////////////////////////////////////////////
	
	public class StorePaymentAdapter extends ArrayAdapter<String> {

		public StorePaymentAdapter() {
			super(getActivity(), R.layout.store_payment_item, R.id.txtName, new ArrayList<String>());
			
			for (int i = 0; i < 10; i++)
				add("Cà phê Ameriacano");
		}
	}
}