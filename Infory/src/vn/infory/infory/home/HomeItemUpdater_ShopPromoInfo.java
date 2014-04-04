package vn.infory.infory.home;

import vn.infory.infory.CyImageLoader;
import vn.infory.infory.R;
import vn.infory.infory.data.home.HomeItem;
import vn.infory.infory.data.home.HomeItem_ShopItem;
import vn.infory.infory.network.CyAsyncTask;
import android.graphics.Bitmap;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class HomeItemUpdater_ShopPromoInfo extends HomeItemUpdater {
	@Override
	public void update(View view, HomeItem item, final HomeFragment caller) {
		
		final HomeItem_ShopItem itemShop = (HomeItem_ShopItem) item;
		final ImageView imgLogo = (ImageView) view.findViewById(R.id.imgLogo);
		TextView txtContent = (TextView) view.findViewById(R.id.txtContent);

		imgLogo.setTag(itemShop.logo);
		CyImageLoader.instance().loadImage(itemShop.logo, new CyImageLoader.Listener() {
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
		
		txtContent.setText(itemShop.content);
		
		view.findViewById(R.id.layoutClick).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				caller.onShopItemClick(itemShop.idShop, itemShop);
			}
		});
	}
}
