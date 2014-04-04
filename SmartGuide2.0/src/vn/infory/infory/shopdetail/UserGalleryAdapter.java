package vn.infory.infory.shopdetail;

import it.sephiroth.android.library.widget.AbsHListView;
import it.sephiroth.android.library.widget.AdapterView;
import it.sephiroth.android.library.widget.AdapterView.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

import vn.infory.infory.CyImageLoader;
import vn.infory.infory.LazyLoadAdapter;
import vn.infory.infory.R;
import vn.infory.infory.data.PhotoGallery;
import vn.infory.infory.data.UserGallery;
import vn.infory.infory.network.CyAsyncTask;
import vn.infory.infory.network.GetUserGallery;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class UserGalleryAdapter extends LazyLoadAdapter 
implements AbsHListView.OnScrollListener, OnItemClickListener {

	private ShopDetailActivity mAct;
	private List<CyAsyncTask> mTaskList;

	public UserGalleryAdapter(ShopDetailActivity act, ArrayList<UserGallery> itemList, String shopId,
			List<CyAsyncTask> taskList) {
		super(act, new GetUserGallery(act, shopId, 0),
				R.layout.shop_detail_user_gallery_loading, 2, itemList);
		//			mIsMore = false;
		mAct = act;
		mTaskList = taskList;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View view = super.getView(position, convertView, parent);
		int h = mAct.mLstGallery.getHeight();
		if (convertView == null) {
			if (view == null)
				convertView = mInflater.inflate(R.layout.shop_detail_user_image_item, parent, false);
			else
				convertView = view;
			LayoutParams param = convertView.getLayoutParams();

			param.width = h;
			param.height = h;
			convertView.setLayoutParams(param);
		}

		if (position >= mItemList.size())
			return convertView;

		UserGallery gal = (UserGallery) getItem(position);

		final ImageView img = (ImageView) convertView;
		
		if (position == 0 ||
				(position == mItemList.size() - 1 && !mIsMore)) {
			img.setScaleType(ScaleType.CENTER);
			img.setImageResource(R.drawable.icon_picture_photo);
		} else {
			img.setTag(gal.thumbnail);
			img.setScaleType(ScaleType.CENTER_CROP);
			CyAsyncTask task = CyImageLoader.instance().loadImage(gal.thumbnail, new CyImageLoader.Listener() {
				@Override
				public void startLoad(int from) {
					switch (from) {
					case CyImageLoader.FROM_DISK:
					case CyImageLoader.FROM_NETWORK:
						img.setImageBitmap(null);
						break;
					}
				}

				@Override
				public void loadFinish(int from, Bitmap image, String url, CyAsyncTask task) {
					mTaskList.remove(task);
					if (((String) img.getTag()).equals(url))
						img.setImageBitmap(image);
				}

				@Override
				public void loadFail(Exception e, CyAsyncTask task) {
					mTaskList.remove(task);
				}
			}, new Point(h, h), mAct);

			if (task != null)
				mTaskList.add(task);
		}

		return convertView;
	}

	@Override
	public void onCompleted(Object result) {
		super.onCompleted(result);
		if (!mIsMore) {
			mItemList.add(new UserGallery());
			notifyDataSetChanged();
		}
	}

	@Override
	public int getItemViewType(int position) {

		return 0;
	}

	@Override
	public Object getItem(int pos) {
		return mItemList.get(pos);
	}

	private int preScrollState = AbsHListView.OnScrollListener.SCROLL_STATE_IDLE;
	@Override
	public void onScroll(AbsHListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		super.onScroll(null, firstVisibleItem, visibleItemCount, totalItemCount);

		if (view.getChildCount() == 0)
			return;

		View firstView = view.getChildAt(0);
		if ((firstVisibleItem != 0 || firstView.getRight() <= 1) 
				^ (mAct.mShowUserGalleryLeft)) {
			ObjectAnimator animator = null;
			if (mAct.mShowUserGalleryLeft)
				// hide
				animator = ObjectAnimator.ofFloat(mAct.mImgUserGalleryLeft, "alpha", 1, 0);
			else
				// show
				animator = ObjectAnimator.ofFloat(mAct.mImgUserGalleryLeft, "alpha", 0, 1);
			animator.start();
			mAct.mShowUserGalleryLeft = !mAct.mShowUserGalleryLeft;
		}

		if ((firstVisibleItem + visibleItemCount != mItemList.size() ||
				(firstVisibleItem == mItemList.size() - 4 && firstView.getLeft() >= -1)) 
				^ (mAct.mShowUserGalleryRight)) {
			ObjectAnimator animator = null;
			if (mAct.mShowUserGalleryRight)
				// hide
				animator = ObjectAnimator.ofFloat(mAct.mImgUserGalleryRight, "alpha", 1, 0);
			else
				// show
				animator = ObjectAnimator.ofFloat(mAct.mImgUserGalleryRight, "alpha", 0, 1);
			animator.start();
			mAct.mShowUserGalleryRight = !mAct.mShowUserGalleryRight;
		}
	}

	@Override
	public void onScrollStateChanged(final AbsHListView view, int scrollState) {
		if (scrollState != AbsHListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL && 
				preScrollState == AbsHListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {

			View firstView = view.getChildAt(0);
			if (firstView == null)
				return;

			final int posToScrolll;
			if (-firstView.getLeft() < firstView.getWidth() / 2)
				posToScrolll = view.getFirstVisiblePosition();
			else
				posToScrolll = view.getFirstVisiblePosition() + 1;

			new Handler().post(new Runnable() {
				@Override
				public void run() {
					view.smoothScrollToPositionFromLeft(posToScrolll, 0);
				}
			});
		}

		preScrollState = scrollState;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (position == 0 ||
				(position == mItemList.size() - 1 && !mIsMore)) 
			return;
		
		List<PhotoGallery> itemList = new ArrayList<PhotoGallery>();
		for (Object item : mItemList)
			if (((PhotoGallery) item).getImage() != null)
				itemList.add((PhotoGallery) item);
		
		GalleryActivity.newInstance(mAct, mLoader.clone(), itemList, position - 1);
	}
}
