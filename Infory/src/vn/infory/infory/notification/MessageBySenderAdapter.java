package vn.infory.infory.notification;

import java.util.List;

import vn.infory.infory.CyImageLoader;
import vn.infory.infory.FontsCollection;
import vn.infory.infory.R;
import vn.infory.infory.data.MessageBySender.messages;
import vn.infory.infory.home.HomeAdapter;
import vn.infory.infory.network.CyAsyncTask;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fortysevendeg.swipelistview.SwipeListView;

public class MessageBySenderAdapter extends ArrayAdapter<messages> {
	private static final String TAG = "Infory MessageBySenderAdapter: ";
	
    private Context mContext;
    private SwipeListView swipeListView;
    private List<messages> messages;
	private LayoutInflater mInflater;
    private int selectedIndex = -1;
    
	public MessageBySenderAdapter(Context mContext, int activityExpandablelistitemCard, List<messages> messages, SwipeListView swipeListView) {
        super(mContext, activityExpandablelistitemCard, messages);
        this.mContext = mContext;
        this.messages = messages;
        this.swipeListView = swipeListView;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

    public void setSelectedIndex(int index) {
        selectedIndex = index;
    }

	@Override
	public int getCount() {
		return messages.size();
	}

	@Override
	public messages getItem(int position) {
		return messages.get(position);
	}

	@Override
	public long getItemId(int position) {
		return messages.get(position).getIdMessage();
	}
	
	private class ViewHolder {
		LinearLayout front, back;
		RelativeLayout linearText;
		LinearLayout linearLogoAndTime;
		TextView txtContent, txtDateTime, txtTitle, txtDelete;
		ImageView imgLogo, imgLogoBackground, imgImage, imgDotBlue;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final messages item = messages.get(position);
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.activity_expandablelistitem_card, parent, false);
			holder = new ViewHolder();
			holder.txtContent = (TextView) convertView.findViewById(R.id.txtContent);
			holder.txtDateTime = (TextView) convertView.findViewById(R.id.txtDateTime);
			holder.txtTitle = (TextView) convertView.findViewById(R.id.txtTitle);
			holder.txtDelete = (TextView) convertView.findViewById(R.id.txtDelete);
			holder.imgLogo = (ImageView) convertView.findViewById(R.id.imgLogo);
			holder.imgLogoBackground = (ImageView) convertView.findViewById(R.id.imgLogoBackground);
			holder.imgImage = (ImageView) convertView.findViewById(R.id.imgImage);
			holder.imgDotBlue = (ImageView) convertView.findViewById(R.id.imgDotBlue);
			holder.linearLogoAndTime = (LinearLayout) convertView.findViewById(R.id.linearLogoAndTime);
			holder.linearText = (RelativeLayout) convertView.findViewById(R.id.linearText);
			holder.back = (LinearLayout) convertView.findViewById(R.id.back);
			holder.front = (LinearLayout) convertView.findViewById(R.id.front);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		initGUI(holder, item, position);
		initEvents(holder, item, position);
		FontsCollection.setFont(convertView);
		return convertView;
	}

	private void initGUI(final ViewHolder holder,
			final vn.infory.infory.data.MessageBySender.messages item, int position) {
		if(item.getStatus() == 0) {
			// message chua doc
//			holder.linearText.setBackgroundResource(R.drawable.leftroundedinput);
			holder.imgLogoBackground.setBackgroundResource(R.drawable.circle_border_white);
			holder.imgDotBlue.setVisibility(View.VISIBLE);
		} else if(item.getStatus() == 1){
			// da doc message
//			holder.linearText.setBackgroundResource(R.drawable.leftrounded_gray);
			holder.imgLogoBackground.setBackgroundResource(R.drawable.circle_border_gray);
			holder.imgDotBlue.setVisibility(View.GONE);
		}
		
		holder.txtDateTime.setText(item.getTime());
		holder.txtTitle.setText(item.getTitle());
		holder.txtContent.setText(item.getContent());
		
		holder.imgLogo.setTag(item.getLogo());
		CyImageLoader.instance().loadImage(item.getLogo(), new CyImageLoader.Listener() {
			@Override
			public void startLoad(int from) {
				switch (from) {
				case CyImageLoader.FROM_DISK:
				case CyImageLoader.FROM_NETWORK:
					holder.imgLogo.setImageBitmap(null);
					break;
				}
			}
			
			@Override
			public void loadFinish(int from, Bitmap image, String url, CyAsyncTask task) {
				if (holder.imgLogo.getTag().equals(url)) {
					holder.imgLogo.setImageBitmap(image);
				}
			}
		}, HomeAdapter.mLogoSize, mContext);

		holder.imgImage.setTag(item.getImage());
		CyImageLoader.instance().loadImage(item.getImage(), new CyImageLoader.Listener() {
			@Override
			public void startLoad(int from) {
				switch (from) {
				case CyImageLoader.FROM_DISK:
				case CyImageLoader.FROM_NETWORK:
					holder.imgImage.setImageBitmap(null);
					break;
				}
			}
			
			@Override
			public void loadFinish(int from, Bitmap image, String url, CyAsyncTask task) {
				if (holder.imgImage.getTag().equals(url)) {
					int imageWidth = image.getWidth();
					int imageHeight = image.getHeight();

					int newWidth = ListMessagesBySenderActivity.screenWidth - mContext.getResources().getDimensionPixelSize(R.dimen.padding_20dip) * 2;
					float scaleFactor = (float)newWidth/(float)imageWidth;
					int newHeight = (int)(imageHeight * scaleFactor);
					image = Bitmap.createScaledBitmap(image, newWidth, newHeight, true);
					holder.imgImage.setImageBitmap(image);
				}
			}
		}, HomeAdapter.mImageSize, mContext);

        if (selectedIndex == position) {
        	expand(holder, item);
			swipeListView.setSelection(selectedIndex);
        } else {
        	collapse(holder, item);
        }
	}

	private void initEvents(final ViewHolder holder,
			final vn.infory.infory.data.MessageBySender.messages item, final int position) {
		holder.linearLogoAndTime.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
                setSelectedIndex(position);
                notifyDataSetChanged();
			}
		});
	}
	
	private void expand(ViewHolder holder, vn.infory.infory.data.MessageBySender.messages item) {
		if(holder.txtContent.getVisibility() != View.VISIBLE) {
			holder.txtDateTime.setTextColor(mContext.getResources().getColor(R.color.black));
			holder.txtTitle.setTextColor(mContext.getResources().getColor(R.color.black));
			holder.txtContent.setTextColor(mContext.getResources().getColor(R.color.black));
			Utils.expand(holder.imgImage);
//			holder.imgImage.setVisibility(View.VISIBLE);
			holder.txtContent.setVisibility(View.VISIBLE);
		}
	}
	
	private void collapse(ViewHolder holder, vn.infory.infory.data.MessageBySender.messages item) {
		if(holder.txtContent.getVisibility() != View.GONE) {
			if(item.getStatus() == 0) {
				holder.txtDateTime.setTextColor(mContext.getResources().getColor(R.color.black));
				holder.txtTitle.setTextColor(mContext.getResources().getColor(R.color.black));
				holder.txtContent.setTextColor(mContext.getResources().getColor(R.color.black));
			} else {
				holder.txtDateTime.setTextColor(mContext.getResources().getColor(R.color.text_color_read_message));
				holder.txtTitle.setTextColor(mContext.getResources().getColor(R.color.text_color_read_message));
				holder.txtContent.setTextColor(mContext.getResources().getColor(R.color.text_color_read_message));
			}
			Utils.collapse(holder.imgImage);
//			holder.imgImage.setVisibility(View.GONE);
			holder.txtContent.setVisibility(View.GONE);
		}
	}

}
