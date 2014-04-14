package vn.infory.infory.shopdetail;

import java.util.ArrayList;

import vn.infory.infory.CyImageLoader;
import vn.infory.infory.LazyLoadAdapter;
import vn.infory.infory.R;
import vn.infory.infory.data.PhotoGallery;
import vn.infory.infory.network.CyAsyncTask;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

public class GalleryAdapter extends LazyLoadAdapter {
	
	private Point imgSize;

	public GalleryAdapter(Activity act, CyAsyncTask loader, ArrayList itemList) {
		super(act, loader, R.layout.shop_detail_gallery_loading, 1, itemList);
		
		int w = act.getResources().getDisplayMetrics().widthPixels / 3;
		imgSize = new Point(w, w);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		convertView = super.getView(position, convertView, parent);

		if (position >= mItemList.size())
			return convertView;

		if (convertView == null)
			convertView = mInflater.inflate(R.layout.shop_detail_gallery_item, parent, false);
		
		String path = ((PhotoGallery) getItem(position)).getThumb();
		final ImageView img = (ImageView) convertView.findViewById(R.id.img);
		img.setTag(path);
		CyImageLoader.instance().loadImage(path, new CyImageLoader.Listener() {
			@Override
			public void startLoad(int from) {
				switch (from) {
				case CyImageLoader.FROM_NETWORK:
				case CyImageLoader.FROM_DISK:
					img.setImageBitmap(null);
					break;
				}
			}
			
			@Override
			public void loadFinish(int from, Bitmap image, String url, CyAsyncTask task) {
				if (img.getTag().equals(url))
					img.setImageBitmap(image);
			}
		}, imgSize, mAct);
		
		img.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				GalleryFullActivity.newInstance(mAct, mItemList, mLoader.clone(), position);
			}
		});
		
		return convertView;
	}
}
