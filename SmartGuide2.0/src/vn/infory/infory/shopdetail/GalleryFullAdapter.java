package vn.infory.infory.shopdetail;

import java.util.ArrayList;
import java.util.List;

import vn.infory.infory.CyImageLoader;
import vn.infory.infory.LazyPagerAdapter;
import vn.infory.infory.R;
import vn.infory.infory.data.PhotoGallery;
import vn.infory.infory.data.ShopGallery;
import vn.infory.infory.network.CyAsyncTask;
import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class GalleryFullAdapter extends LazyPagerAdapter {

	private Activity mAct;
	private boolean mIsThumb;

	public GalleryFullAdapter(FragmentActivity act, List<PhotoGallery> itemList, 
			CyAsyncTask loader, boolean isThumb) {
		super(act, loader, R.id.layoutLoading, (ArrayList) itemList);
		mAct = act;
		mIsThumb = isThumb;
	}

	@Override
	public View getView(LayoutInflater inflater, ViewGroup container, 
			final int pos, Object dataItem, boolean isLoading) {
		View v = inflater.inflate(R.layout.shop_detail_gallery, container, false);
		ImageView img = (ImageView) v.findViewById(R.id.img);
		
		if (mIsThumb)
			v.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					GalleryActivity.newInstance(mAct, mLoader.clone(), mItemList, pos);
				}
			});
		else
			img.setScaleType(ScaleType.FIT_CENTER);

		if (isLoading) {
			View loading = v.findViewById(R.id.layoutLoading);
			loading.setVisibility(View.VISIBLE);
			((AnimationDrawable) loading.getBackground()).start();
		} else {
			PhotoGallery gal = (PhotoGallery) dataItem;
			CyImageLoader.instance().showImage(mIsThumb ? gal.getThumb(): gal.getImage(),
					img, ShopDetailActivity.mCoverSize);
		}
		return v;
	}

	@Override
	public void transform(View convertView, int pos, Object dataItem) {
		View loading = convertView.findViewById(R.id.layoutLoading);
		loading.setVisibility(View.INVISIBLE);
		((AnimationDrawable) loading.getBackground()).stop();

		ShopGallery gal = (ShopGallery) dataItem;
		ImageView img = (ImageView) convertView.findViewById(R.id.img);
		CyImageLoader.instance().showImage(gal.cover, img, ShopDetailActivity.mCoverSize);
	}
}
