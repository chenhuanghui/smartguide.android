package vn.infory.infory.notification;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.nhaarman.listviewanimations.itemmanipulation.ExpandableListItemAdapter;

public class MessageBySenderAdapter extends ExpandableListItemAdapter<Integer> {

	public MessageBySenderAdapter(Context context, int layoutResId,
			int titleParentResId, int contentParentResId, List<Integer> items) {
		super(context, layoutResId, titleParentResId, contentParentResId, items);
		// TODO Auto-generated constructor stub
	}

	@Override
	public View getTitleView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public View getContentView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		return null;
	}

}
