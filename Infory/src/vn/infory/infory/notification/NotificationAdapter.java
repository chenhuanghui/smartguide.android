package vn.infory.infory.notification;

import java.util.List;

import vn.infory.infory.FontsCollection;
import vn.infory.infory.R;
import vn.infory.infory.data.MessageInfo;
import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class NotificationAdapter extends BaseAdapter {
	private static final String log_tag = "Infory NotificationAdapter: ";

    private List<MessageInfo> lstMessages;
	
	private LayoutInflater mInflater;
	private Context mContext;
	
	public NotificationAdapter(Context mContext, List<MessageInfo> lstMessages) {
		this.lstMessages = lstMessages;
		this.mContext = mContext;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return this.lstMessages.size();
	}

	@Override
	public Object getItem(int position) {
		return this.lstMessages.get(position);
	}

	@Override
	public long getItemId(int position) {
		return this.lstMessages.get(position).hashCode();
	}

	private class ViewHolder {
		LinearLayout front, back;
		FrameLayout linearUnreadlayout;
		View viewLeft;
		RelativeLayout linearText;
		TextView txtContent, txtDateTime, txtUnreadCount, txtDelete;
		ImageView imgDotBlue, imgArrow;
	}
    
	@Override
	public View getView(final int position, View convertView, final ViewGroup parent) {
		final MessageInfo item = lstMessages.get(position);
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.notification_item_layout, parent, false);
			holder = new ViewHolder();
			holder.viewLeft = (View) convertView.findViewById(R.id.viewLeft);
			holder.txtContent = (TextView) convertView.findViewById(R.id.txtContent);
			holder.txtDateTime = (TextView) convertView.findViewById(R.id.txtDateTime);
			holder.txtUnreadCount = (TextView) convertView.findViewById(R.id.txtUnreadCount);
			holder.txtDelete = (TextView) convertView.findViewById(R.id.txtDelete);
			holder.imgDotBlue = (ImageView) convertView.findViewById(R.id.imgDotBlue);
			holder.imgArrow = (ImageView) convertView.findViewById(R.id.imgArrow);
			holder.linearText = (RelativeLayout) convertView.findViewById(R.id.linearText);
			holder.back = (LinearLayout) convertView.findViewById(R.id.back);
			holder.front = (LinearLayout) convertView.findViewById(R.id.front);
			holder.linearUnreadlayout = (FrameLayout) convertView.findViewById(R.id.linearUnreadlayout);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		initGUI(holder, item, position);
		initEvents(holder, item, position);
		FontsCollection.setFont(convertView);
		return convertView;
	}

	private void initGUI(final ViewHolder holder, MessageInfo item, int position){

		Log.d(log_tag, String.format("Item in %s has status %s", position, item.getStatus()));
		if(item.getStatus() != -1) {
			if(item.getStatus() == 0) {
				// con message chua doc
				holder.txtContent.setTextColor(mContext.getResources().getColor(R.color.black));
				holder.linearText.setBackgroundResource(R.drawable.leftroundedinput);
				holder.viewLeft.setBackgroundColor(mContext.getResources().getColor(R.color.background_view_unread_message));
				holder.imgDotBlue.setVisibility(View.VISIBLE);
				holder.txtUnreadCount.setVisibility(View.VISIBLE);
				holder.txtUnreadCount.setText(item.getCount().getString()[0] + " tin chưa đọc");
			} else if(item.getStatus() == 1){
				// da doc tat ca message
				holder.txtContent.setTextColor(mContext.getResources().getColor(R.color.text_color_read_message));
				holder.linearText.setBackgroundResource(R.drawable.leftrounded_gray);
				holder.viewLeft.setBackgroundColor(mContext.getResources().getColor(R.color.background_view_read_message));
				holder.imgDotBlue.setVisibility(View.GONE);
				holder.txtUnreadCount.setVisibility(View.GONE);
			}

			String sender = item.getSender();
			if(item.getSender().compareTo(NotificationActivity.stringEmptyData) == 0){
				holder.txtDateTime.setVisibility(View.GONE);
				holder.txtUnreadCount.setVisibility(View.GONE);
				holder.imgDotBlue.setVisibility(View.GONE);
				holder.imgArrow.setVisibility(View.GONE);
				holder.txtContent.setText(sender);
				holder.viewLeft.setVisibility(View.GONE);
			} else {
				String content = item.getNewestMessage().getContent();
				String tempContent = sender + " " + content;
				SpannableString contentSpan = new SpannableString(tempContent);
				StyleSpan bss = new StyleSpan(Typeface.BOLD); 
				contentSpan.setSpan(
						bss,
						0,
						sender.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				holder.txtContent.setText(contentSpan);
				holder.txtDateTime.setText(item.getNewestMessage().getTime());
			}
			holder.front.setVisibility(View.VISIBLE);
			holder.back.setVisibility(View.VISIBLE);
			holder.linearUnreadlayout.setVisibility(View.GONE);
		} else {
			holder.front.setVisibility(View.GONE);
			holder.back.setVisibility(View.GONE);
			holder.linearUnreadlayout.setVisibility(View.VISIBLE);
		}
	}

	private void initEvents(final ViewHolder holder, final MessageInfo item, final int position) {
//		holder.front.setOnTouchListener(new View.OnTouchListener() {
//			
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				if(item.getStatus() == -1)
//					return holder.front.dispatchTouchEvent(event);
//				
//				return false;
//			}
//		});
	}

}
