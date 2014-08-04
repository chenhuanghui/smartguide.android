package vn.infory.infory.home;

import vn.infory.infory.R;
import vn.infory.infory.data.home.HomeItem;
import vn.infory.infory.data.home.HomeItem_Header;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class HomeItemUpdater_Header extends HomeItemUpdater{

	private LayoutInflater mInflater;
	
	@Override
	public void update(View view, HomeItem item, HomeFragment caller) {
		// TODO Auto-generated method stub
		mInflater = caller.getActivity().getLayoutInflater();
		
		HomeItem_Header itemHeader = (HomeItem_Header) item;
		
		TextView txtHeaderContent = (TextView) view.findViewById(R.id.txtHeaderContent);
		
		txtHeaderContent.setText(itemHeader.title);		
	}

}
