package vn.infory.infory.home;

import it.sephiroth.android.library.widget.AbsHListView.PositionScroller;

import java.util.ArrayList;

import vn.infory.infory.CyImageLoader;
import vn.infory.infory.CyUtils;
import vn.infory.infory.FontsCollection;
import vn.infory.infory.LazyLoadAdapter;
import vn.infory.infory.R;
import vn.infory.infory.data.home.PromoItem;
import vn.infory.infory.network.CyAsyncTask;
import vn.infory.infory.network.GetPromotion;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class PromotionAdapter extends LazyLoadAdapter 
implements OnClickListener {
	
	// Data
	private Point mAvaAize, mCoverSize;
	private HomeListener mListener;

	private static long lastClickTime = 0;
	
	public PromotionAdapter(Activity act, HomeListener listener, ArrayList itemList) {
		super(act, new GetPromotion(act, 0), R.layout.shop_list_loading, 1, itemList);
		
		mListener = listener;
		int w = CyUtils.dpToPx(36, act);
		mAvaAize = new Point(w, w);
		mCoverSize = new Point(act.getResources().getDisplayMetrics().widthPixels, 0);
	}
	
	@Override
	public int getItemViewType(int position) {
		int type = super.getItemViewType(position);
		if (type != -1)
			return type;
		else
			return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		convertView = super.getView(position, convertView, parent);
		
		if (position >= mItemList.size()) {
			((AnimationDrawable) convertView.findViewById(R.id.layoutLoading).getBackground()).start();
			return convertView;
		}
		
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.home_block_4, parent, false);
			FontsCollection.setFont(convertView);
			CyUtils.setHoverEffect(convertView.findViewById(R.id.btnGoto), false);
		}
		
		PromoItem item = (PromoItem) getItem(position);
		
		TextView txtName = (TextView) convertView.findViewById(R.id.txtName);
		TextView txtDate = (TextView) convertView.findViewById(R.id.txtDate);
		TextView txtTitle = (TextView) convertView.findViewById(R.id.txtTitle);
		TextView txtContent = (TextView) convertView.findViewById(R.id.txtContent);
		final ImageView imgCover = (ImageView) convertView.findViewById(R.id.imgCover);
		final ImageView imgLogo = (ImageView) convertView.findViewById(R.id.imgLogo);
		Button btnGoto = (Button) convertView.findViewById(R.id.btnGoto);
		
		txtName.setText(item.brandName);
		txtDate.setText(item.date);
		txtTitle.setText(item.title);
		txtContent.setText(item.description);
		btnGoto.setText(item.goTo);
		btnGoto.setTag(position);
		btnGoto.setOnClickListener(this);
		
		imgCover.setContentDescription("ratio:" + 
			(float) item.coverWidth / item.coverHeight);
		convertView.requestLayout();
		
		imgLogo.setTag(item.logo);
		CyImageLoader.instance().loadImage(item.logo, new CyImageLoader.Listener() {
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
				if (((String) imgLogo.getTag()).equals(url))
					imgLogo.setImageBitmap(image);
			}
		}, mAvaAize, parent.getContext());
		
		int w = parent.getResources().getDisplayMetrics().widthPixels;
		mCoverSize.y = (int) (w * item.coverHeight / item.coverWidth);
		imgCover.setTag(item.cover);
		CyImageLoader.instance().loadImage(item.cover, new CyImageLoader.Listener() {
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
				if (((String) imgCover.getTag()).equals(url))
					imgCover.setImageBitmap(image);
			}
		}, mCoverSize, parent.getContext());
		
		imgLogo.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onItemClick(position);
			}
		});
		
		imgCover.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				long clickTime = System.currentTimeMillis();
		        if (clickTime - lastClickTime < 300){
		        	onItemClick(position);
		        }
		        lastClickTime = clickTime;
			}
		});
		
		txtName.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onItemClick(position);
			}
		});
		
		return convertView;
	}
	
	public void onItemClick(Integer position) {
		PromoItem item = (PromoItem) getItem(position);
		
		switch (item.type) {
		case 0: // branch
			mListener.onBranchPromoInfoClick(item.idShops);
			break;
		case 1: // shop
			mListener.onShopItemClick(item.idShop, item);
			break;
		}
	}

	@Override
	public void onClick(View v) {
		int position = (Integer) v.getTag();
		PromoItem item = (PromoItem) getItem(position);
		
		switch (item.type) {
		case 0: // branch
			mListener.onBranchPromoInfoClick(item.idShops);
			break;
		case 1: // shop
			mListener.onShopItemClick(item.idShop, item);
			break;
		}
	}
}
