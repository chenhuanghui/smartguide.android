package vn.infory.infory.home;

import vn.infory.infory.CyImageLoader;
import vn.infory.infory.R;
import vn.infory.infory.data.home.HomeItem;
import vn.infory.infory.data.home.HomeItem_BranchPromoInfo;
import vn.infory.infory.network.CyAsyncTask;
import android.graphics.Bitmap;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class HomeItemUpdater_BranchPromoInfo extends HomeItemUpdater {
	@Override
	public void update(View view, HomeItem item, final HomeFragment caller) {
		
		final HomeItem_BranchPromoInfo itemInfo = (HomeItem_BranchPromoInfo) item;
		final ImageView imgLogo = (ImageView) view.findViewById(R.id.imgLogo);
		TextView txtContent = (TextView) view.findViewById(R.id.txtContent);
		txtContent.setText(itemInfo.content);

//		CyImageLoader.instance().showImage(itemInfo.logo, imgLogo, HomeAdapter.mLogoSize);
		imgLogo.setTag(itemInfo.logo);
		CyImageLoader.instance().loadImage(itemInfo.logo, new CyImageLoader.Listener() {
			@Override
			public void startLoad(int from) {
				switch (from) {
				case CyImageLoader.FROM_DISK:
				case CyImageLoader.FROM_NETWORK:
					imgLogo.setImageBitmap(null);
					break;
				}
			}
			
			@Override
			public void loadFinish(int from, Bitmap image, String url, CyAsyncTask task) {
				if (imgLogo.getTag().equals(url))
					imgLogo.setImageBitmap(image);
			}
		}, HomeAdapter.mLogoSize, caller.getActivity());
		
		view.findViewById(R.id.layoutClick).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				caller.onBranchPromoInfoClick(itemInfo.shopList);
			}
		});
	}
}
