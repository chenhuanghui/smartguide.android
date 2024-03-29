package vn.infory.infory.home;

import vn.infory.infory.CyImageLoader;
import vn.infory.infory.R;
import vn.infory.infory.data.home.HomeItem;
import vn.infory.infory.data.home.HomeItem_ShopItem;
import vn.infory.infory.data.home.HomeItem_StoreItem;
import vn.infory.infory.network.CyAsyncTask;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class HomeItemUpdater_StoreItem extends HomeItemUpdater {
	@Override
	public void update(View view, HomeItem item, final HomeFragment caller) {
		
		final HomeItem_StoreItem itemStore = (HomeItem_StoreItem) item;
		
		TextView txtName = (TextView) view.findViewById(R.id.txtName);
		TextView txtDate = (TextView) view.findViewById(R.id.txtDate);
		TextView txtTitle = (TextView) view.findViewById(R.id.txtTitle);
		TextView txtContent = (TextView) view.findViewById(R.id.txtContent);
		final ImageView imgCover = (ImageView) view.findViewById(R.id.imgCover);
		final ImageView imgLogo = (ImageView) view.findViewById(R.id.imgLogo);
		Button btnGoto = (Button) view.findViewById(R.id.btnGoto);
		
		txtName.setText(itemStore.storeInfo.storeName);
		txtDate.setText(itemStore.date);
		txtTitle.setText(itemStore.title);
		txtContent.setText(itemStore.content);
		btnGoto.setText(itemStore.goto_);
		
		btnGoto.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				caller.onStoreItemClick("" + itemStore.storeInfo.idStore);
			}
		});
		
		imgLogo.setTag(itemStore.storeInfo.logo);
		CyImageLoader.instance().loadImage(itemStore.storeInfo.logo, new CyImageLoader.Listener() {
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
		
		if (itemStore.coverHeight == 0 || itemStore.coverWidth == 0) {
			itemStore.coverHeight = 1;
			itemStore.coverWidth = 3;
		}
		
		int w = caller.getResources().getDisplayMetrics().widthPixels;
		int h = (int) (w * itemStore.coverHeight / itemStore.coverWidth);
		imgCover.setContentDescription("ratio:" + (float) w / h);
		Point coverSize = new Point(w, h);
		imgCover.setTag(itemStore.cover);
		CyImageLoader.instance().loadImage(itemStore.cover, new CyImageLoader.Listener() {
			@Override
			public void startLoad(int from) {
				switch (from) {
				case CyImageLoader.FROM_DISK:
				case CyImageLoader.FROM_NETWORK:
					imgCover.setImageBitmap(null);
					break;
				}
			}
			
			@Override
			public void loadFinish(int from, Bitmap image, String url, CyAsyncTask task) {
				if (imgCover.getTag().equals(url))
					imgCover.setImageBitmap(image);
			}
		}, coverSize, caller.getActivity());
	}
}
