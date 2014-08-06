package vn.infory.infory.store;

import vn.infory.infory.CyUtils;
import vn.infory.infory.R;
import vn.infory.infory.R.id;
import vn.infory.infory.R.layout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.ViewById;

public class StoreItemFragment extends Fragment {
	
	// GUI elements
	@ViewById(id = R.id.btnBuy) 	private ImageButton mBtnBuy;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.store_item_fragment, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		try {
			AndroidAnnotationParser.parse(this, view);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		CyUtils.setHoverEffect(mBtnBuy);
	}

}
