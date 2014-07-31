package vn.infory.infory.home;

import vn.infory.infory.CyImageLoader;
import vn.infory.infory.R;
import vn.infory.infory.data.home.HomeItem;
import vn.infory.infory.data.home.HomeItem_ShopItem;
import vn.infory.infory.network.CyAsyncTask;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class HomeItemUpdater_ShopItem extends HomeItemUpdater {
	private static long lastClickTime = 0;
	
	@Override
	public void update(View view, HomeItem item, final HomeFragment caller) {
		
		final HomeItem_ShopItem itemShop = (HomeItem_ShopItem) item;
		
		TextView txtName = (TextView) view.findViewById(R.id.txtName);
		TextView txtDate = (TextView) view.findViewById(R.id.txtDate);
		TextView txtTitle = (TextView) view.findViewById(R.id.txtTitle);
		TextView txtContent = (TextView) view.findViewById(R.id.txtContent);
		final ImageView imgCover = (ImageView) view.findViewById(R.id.imgCover);
		final ImageView imgLogo = (ImageView) view.findViewById(R.id.imgLogo);
		Button btnGoto = (Button) view.findViewById(R.id.btnGoto);
		
		if (itemShop.title == null || itemShop.title.length() == 0)
			txtTitle.setVisibility(View.GONE);
		else {
			txtTitle.setVisibility(View.VISIBLE);
			txtTitle.setText(itemShop.title);
		}
		
		if (itemShop.content == null || itemShop.content.length() == 0)
			txtContent.setVisibility(View.GONE);
		else {
			txtContent.setVisibility(View.VISIBLE);
			txtContent.setText(itemShop.content);
		}
		
		txtName.setText(itemShop.shopName);
		txtDate.setText(itemShop.date);
		btnGoto.setText(itemShop.goto_);
		
		txtName.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				caller.onShopItemClick(itemShop.idShop, itemShop);
			}
		});
		
		imgLogo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				caller.onShopItemClick(itemShop.idShop, itemShop);
			}
		});
		
		
		
		imgCover.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				long clickTime = System.currentTimeMillis();
		        if (clickTime - lastClickTime < 300){
		        	caller.onShopItemClick(itemShop.idShop, itemShop);
		        }
		        lastClickTime = clickTime;
			}
		});
		
		btnGoto.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				caller.onShopItemClick(itemShop.idShop, itemShop);
			}
		});
		
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
		
		if (itemShop.coverHeight == 0 || itemShop.coverWidth == 0) {
			itemShop.coverHeight = 1;
			itemShop.coverWidth = 3;
		}
		int w = caller.getResources().getDisplayMetrics().widthPixels;
		int h = (int) (w * itemShop.coverHeight / itemShop.coverWidth);
		imgCover.setContentDescription("ratio:" + (float) w / h);
		Point coverSize = new Point(w, h);
		imgCover.setTag(itemShop.cover);
		CyImageLoader.instance().loadImage(itemShop.cover, new CyImageLoader.Listener() {
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
