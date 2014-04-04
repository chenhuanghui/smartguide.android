package vn.infory.infory.shopdetail;

import java.util.ArrayList;
import java.util.List;

import vn.infory.infory.CyImageLoader;
import vn.infory.infory.LazyLoadAdapter;
import vn.infory.infory.R;
import vn.infory.infory.data.Comment;
import vn.infory.infory.network.CyAsyncTask;
import vn.infory.infory.network.GetComment;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.ViewById;

public class CommentAdapter extends LazyLoadAdapter {

	private ShopDetailActivity mAct;
	private List<CyAsyncTask> mTaskList;

	public CommentAdapter(ShopDetailActivity act, ArrayList<Comment> itemList, String shopId,
			List<CyAsyncTask> taskList) {
		super(act, new GetComment(act, shopId, 0, 0), 
				R.layout.shop_detail_comment_loading, 1, itemList);
		
		mAct = act;
		mTaskList = taskList;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = super.getView(position, convertView, parent);
		
		if (position >= mItemList.size()) {
			View v = convertView.findViewById(R.id.layoutLoading);
			((AnimationDrawable) v.getBackground()).start();
			return convertView;
		}

		CommentHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.shop_detail_comment_item, parent, false);
			holder = new CommentHolder();
			try {
				AndroidAnnotationParser.parse(holder, convertView);
			} catch (Exception e) {
				return convertView;
			}
			convertView.setTag(holder);
		} else {
			holder = (CommentHolder) convertView.getTag();
		}
		
		Comment item = null;
		try {
			item = (Comment) getItem(position);
		} catch (Exception e) {
			e.printStackTrace();
		}
			
		holder.mTxtName.setText(item.username);
		holder.mTxtContent.setText(item.comment);
		holder.mTxtTime.setText(item.time);
		holder.mTxtAgreeNum.setText(item.numOfAgree);
		final ImageView imgAva = holder.mImgAva;
		imgAva.setTag(item.avatar);

		CyAsyncTask task = CyImageLoader.instance().loadImage(item.avatar, new CyImageLoader.Listener() {
			@Override
			public void startLoad(int from) {
				switch (from) {
				case CyImageLoader.FROM_DISK:
				case CyImageLoader.FROM_NETWORK:
					imgAva.setImageBitmap(null);
					break;
				}
			}

			@Override
			public void loadFinish(int from, Bitmap image, String url, CyAsyncTask task) {
				mTaskList.remove(task);
				if (imgAva.getTag().equals(url))
					imgAva.setImageBitmap(image);
			}
			
			@Override
			public void loadFail(Exception e, CyAsyncTask task) {
				mTaskList.remove(task);
			}
		}, new Point(), mAct);
		
		if (task != null)
			mTaskList.add(task);

		return convertView;
	}
	
	private static class CommentHolder {
		@ViewById(id = R.id.txtAgreeNum)	public TextView mTxtAgreeNum;
		@ViewById(id = R.id.txtName)		public TextView mTxtName;
		@ViewById(id = R.id.txtContent)		public TextView mTxtContent;
		@ViewById(id = R.id.imgAva)			public ImageView mImgAva;
		@ViewById(id = R.id.txtTime)		public TextView mTxtTime;
	}
}