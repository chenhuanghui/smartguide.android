package vn.smartguide;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {

	public Context context;
	public ImageAdapter(Context c){
		context = c;
	}
	public int getCount() {
		return GlobalVariable.mAvatarList.size();
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return 0;
	}

	// create a new ImageView for each item referenced by the Adapter
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = ((Activity) context).getLayoutInflater();
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.avatar_item, parent, false);
		}
		
		final ImageView avatar = (ImageView)convertView.findViewById(R.id.avatar);
		avatar.setTag(GlobalVariable.mAvatarList.get(position));

		try{
			GlobalVariable.cyImageLoader.loadImage(GlobalVariable.mAvatarList.get(position), new CyImageLoader.Listener() {
				//					GlobalVariable.cyImageLoader.loadImage(
				//							CyImageLoader.DUMMY_PATH[position % CyImageLoader.DUMMY_PATH.length],
				//							new CyImageLoader.Listener() {

				@Override
				public void startLoad(int from) {
					switch (from) {
					case CyImageLoader.FROM_DISK:
					case CyImageLoader.FROM_NETWORK:
						avatar.setImageResource(R.drawable.ava_loading);
						break;
					}
				}

				@Override
				public void loadFinish(int from, Bitmap image, String url) {
					switch (from) {
					case CyImageLoader.FROM_MEMORY:
						avatar.setImageBitmap(image);
						break;

					case CyImageLoader.FROM_DISK:
					case CyImageLoader.FROM_NETWORK:;
//					notifyDataSetChanged();
					if (((String) avatar.getTag()).equals(url))
						avatar.setImageBitmap(image);
					//								}
					break;
					}
				}

			}, new Point(128, 128), context);
		}catch(Exception ex){
			ex.printStackTrace();
		}

		return convertView;
	}
}