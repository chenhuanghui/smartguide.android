package vn.infory.infory.shopdetail;

import java.util.List;

import vn.infory.infory.CyImageLoader;
import vn.infory.infory.R;
import vn.infory.infory.network.CyAsyncTask;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

@SuppressLint("ValidFragment")
public class ShopGalleryFragment extends Fragment {
	
	private String url;
	private List<CyAsyncTask> mTaskList;
	
	public ShopGalleryFragment() {}
	
	public ShopGalleryFragment(String url, List<CyAsyncTask> taskList) {
		this.url = url;
		this.mTaskList = taskList;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.shop_detail_gallery, container, false);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		final ImageView img = (ImageView) view.findViewById(R.id.img);
		CyAsyncTask task = CyImageLoader.instance().loadImage(url, new CyImageLoader.Listener() {
			@Override
			public void loadFinish(int from, Bitmap image, String url, CyAsyncTask task) {
				mTaskList.remove(task);
				
				switch (from) {
				case CyImageLoader.FROM_MEMORY:
				case CyImageLoader.FROM_DISK:
					img.setImageBitmap(image);
					break;
					
				case CyImageLoader.FROM_NETWORK:
					TransitionDrawable trans = new TransitionDrawable(new Drawable[] {
							new ColorDrawable(0),
							new BitmapDrawable(image)
					});
					
					img.setBackgroundDrawable(trans);
					trans.startTransition(300);
					break;
				}
			}
			
			@Override
			public void loadFail(Exception e, CyAsyncTask task) {
				mTaskList.remove(task);
//				mLoadedCover = false;
//				CyUtils.showError("Không thể tải ảnh cửa hàng", e, ShopDetailActivity.this);
			}
		}, ShopDetailActivity.mCoverSize, getActivity());
		if (task != null)
			mTaskList.add(task);
	}
}
