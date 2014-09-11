package vn.infory.infory.notification;

import java.util.List;

import vn.infory.infory.CyImageLoader;
import vn.infory.infory.FontsCollection;
import vn.infory.infory.R;
import vn.infory.infory.data.Message.buttons;
import vn.infory.infory.data.MessageBySender.messages;
import vn.infory.infory.home.HomeAdapter;
import vn.infory.infory.mywidget.ListViewNotScroll;
import vn.infory.infory.network.CyAsyncTask;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nhaarman.listviewanimations.itemmanipulation.expandablelistitem.ExpandableListItemAdapter;

public class MessageBySenderAdapter extends ExpandableListItemAdapter<messages> {
	private static final String TAG = "Infory MessageBySenderAdapter: ";
	
    private Context mContext;
//	private ProgressDialog dialog;
    private List<messages> messages;
//	private LayoutInflater mInflater;
    private int selectedIndex = 0;
    
	public MessageBySenderAdapter(Context mContext, List<messages> messages) {
//        super(mContext, activityExpandablelistitemCard, messages);
		super(mContext, 
				R.layout.expandable_list_item_card_layout,
				R.id.activity_expandablelistitem_card_title,
				R.id.activity_expandablelistitem_card_content);
        this.mContext = mContext;
        this.messages = messages;
//		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

//		dialog = new ProgressDialog(mContext);
//		dialog.setMessage("Vui lòng chờ đợi!");
//		dialog.setCancelable(false);
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
	
//	private class ViewHolder {
//		RelativeLayout linearText;
//		LinearLayout linearLogoAndTime;
//		TextView txtContent, txtDateTime, txtTitle;
//		ImageView imgLogo, imgLogoBackground, imgImage, imgDotBlue, imgVideoThumb;
//		ListView listViewButtons;
//		FrameLayout frameVideoThumb, loadProgressBarVideo, loadProgressBar;
//		Button btnPlay;
//	}
//
//	@Override
//	public View getView(int position, View convertView, ViewGroup parent) {
//		final messages item = messages.get(position);
//		ViewHolder holder;
//		if (convertView == null) {
//			convertView = mInflater.inflate(R.layout.activity_expandablelistitem_card, parent, false);
//			holder = new ViewHolder();
//			holder.txtContent = (TextView) convertView.findViewById(R.id.txtContent);
//			holder.txtDateTime = (TextView) convertView.findViewById(R.id.txtDateTime);
//			holder.txtTitle = (TextView) convertView.findViewById(R.id.txtTitle);
//			holder.imgLogo = (ImageView) convertView.findViewById(R.id.imgLogo);
//			holder.imgLogoBackground = (ImageView) convertView.findViewById(R.id.imgLogoBackground);
//			holder.imgImage = (ImageView) convertView.findViewById(R.id.imgImage);
//			holder.imgDotBlue = (ImageView) convertView.findViewById(R.id.imgDotBlue);
//			holder.linearLogoAndTime = (LinearLayout) convertView.findViewById(R.id.linearLogoAndTime);
//			holder.linearText = (RelativeLayout) convertView.findViewById(R.id.linearText);
//			holder.listViewButtons = (ListView) convertView.findViewById(R.id.listViewButtons);
//			holder.imgVideoThumb = (ImageView) convertView.findViewById(R.id.imgVideoThumb);
//			holder.btnPlay = (Button) convertView.findViewById(R.id.btnPlay);
//			holder.frameVideoThumb = (FrameLayout) convertView.findViewById(R.id.frameVideoThumb);
//			holder.loadProgressBar = (FrameLayout) convertView.findViewById(R.id.loadProgressBar);
//			holder.loadProgressBarVideo = (FrameLayout) convertView.findViewById(R.id.loadProgressBarVideo);
//			convertView.setTag(holder);
//		} else {
//			holder = (ViewHolder) convertView.getTag();
//		}
//		((AnimationDrawable) holder.loadProgressBar.getBackground()).start();
//		((AnimationDrawable) holder.loadProgressBarVideo.getBackground()).start();
//		initGUI(holder, item, position);
//		initEvents(holder, item, position);
//		FontsCollection.setFont(convertView);
//		return convertView;
//	}
//
//	private void initGUI(final ViewHolder holder,
//			final vn.infory.infory.data.MessageBySender.messages item, int position) {
//
//		int newWidth = ListMessagesBySenderActivity.screenWidth - mContext.getResources().getDimensionPixelSize(R.dimen.padding_20dip) * 2;
//		
//		if(item.getStatus() == 0) {
//			// message chua doc
//			holder.linearText.setBackgroundResource(R.drawable.leftroundedinput);
//			holder.imgLogoBackground.setBackgroundResource(R.drawable.circle_border_white);
//			holder.imgDotBlue.setVisibility(View.VISIBLE);
//		} else if(item.getStatus() == 1){
//			// da doc message
//			holder.linearText.setBackgroundResource(R.drawable.leftrounded_gray);
//			holder.imgLogoBackground.setBackgroundResource(R.drawable.circle_border_gray);
//			holder.imgDotBlue.setVisibility(View.GONE);
//		}
//		
//		holder.txtDateTime.setText(item.getTime());
//		holder.txtTitle.setText(item.getTitle());
//		holder.txtContent.setText(item.getContent());
//		
//		holder.imgLogo.setTag(item.getLogo());
//		CyImageLoader.instance().loadImage(item.getLogo(), new CyImageLoader.Listener() {
//			@Override
//			public void startLoad(int from) {
//				switch (from) {
//				case CyImageLoader.FROM_DISK:
//				case CyImageLoader.FROM_NETWORK:
//					holder.imgLogo.setImageBitmap(null);
//					break;
//				}
//			}
//			
//			@Override
//			public void loadFinish(int from, Bitmap image, String url, CyAsyncTask task) {
//				if (holder.imgLogo.getTag().equals(url)) {
//					holder.imgLogo.setImageBitmap(image);
//				}
//			}
//		}, HomeAdapter.mLogoSize, mContext);
//
//		if((item.getImage() != null && item.getImage().length() > 0)) {
//			holder.imgImage.setTag(item.getImage());
//			
//			int imageWidth = item.getImageWidth();
//			int imageHeight = item.getImageHeight();
//			float scaleFactor = (float)newWidth/(float)imageWidth;
//			int newHeight = (int)(imageHeight * scaleFactor);
//			
//			FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) holder.imgImage.getLayoutParams();
//			params.width = newWidth;
//			params.height = newHeight;
//			holder.imgImage.setLayoutParams(params);
//			
//			CyImageLoader.instance().loadImage(item.getImage(), new CyImageLoader.Listener() {
//				@Override
//				public void startLoad(int from) {
//					holder.loadProgressBar.setVisibility(View.VISIBLE);
//					switch (from) {
//					case CyImageLoader.FROM_DISK:
//					case CyImageLoader.FROM_NETWORK:
//						holder.imgImage.setImageBitmap(null);
//						break;
//					}
//				}
//				
//				@Override
//				public void loadFinish(int from, Bitmap image, String url, CyAsyncTask task) {
//					holder.loadProgressBar.setVisibility(View.GONE);
//					if (holder.imgImage.getTag().equals(url)) {
//						holder.imgImage.setImageBitmap(image);
//					}
//				}
//			}, HomeAdapter.mImageSize, mContext);
//		} else {
//			holder.loadProgressBar.setVisibility(View.GONE);
//		}
//		
//		if(item.getVideoThumbnail() != null && item.getVideoThumbnail().length() > 0) {
//			holder.imgVideoThumb.setTag(item.getVideoThumbnail());
//			
//			int videoWidth = item.getVideoWidth();
//			int videoHeight = item.getVideoHeight();
//			float scaleFactor = (float)newWidth/(float)videoWidth;
//			int newHeight = (int)(videoHeight * scaleFactor);
//			
//			FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) holder.imgVideoThumb.getLayoutParams();
//			params.width = newWidth;
//			params.height = newHeight;
//			holder.imgVideoThumb.setLayoutParams(params);
//			
//			CyImageLoader.instance().loadImage(item.getVideoThumbnail(), new CyImageLoader.Listener() {
//				@Override
//				public void startLoad(int from) {
//					holder.loadProgressBarVideo.setVisibility(View.VISIBLE);
//					switch (from) {
//					case CyImageLoader.FROM_DISK:
//					case CyImageLoader.FROM_NETWORK:
//						holder.imgVideoThumb.setImageBitmap(null);
//						break;
//					}
//				}
//				
//				@Override
//				public void loadFinish(int from, Bitmap image, String url, CyAsyncTask task) {
//					holder.loadProgressBarVideo.setVisibility(View.GONE);
//					if (holder.imgVideoThumb.getTag().equals(url)) {
////						int imageWidth = image.getWidth();
////						int imageHeight = image.getHeight();
////
////						int newWidth = ListMessagesBySenderActivity.screenWidth - mContext.getResources().getDimensionPixelSize(R.dimen.padding_20dip) * 2;
////						float scaleFactor = (float)newWidth/(float)imageWidth;
////						int newHeight = (int)(imageHeight * scaleFactor);
////						image = Bitmap.createScaledBitmap(image, newWidth, newHeight, true);
//						holder.imgVideoThumb.setImageBitmap(image);
//					}
//				}
//			}, HomeAdapter.mImageSize, mContext);
//		} else {
//			holder.loadProgressBarVideo.setVisibility(View.GONE);
//		}
//		
//		// set adapter list button
//		List<buttons> lstButtons = item.getButtons();
//		if(lstButtons != null && lstButtons.size() > 0) {
//			ListButtonAdapter adapter = new ListButtonAdapter(mContext, lstButtons);
//			holder.listViewButtons.setAdapter(adapter);
//		}
//		
//        if (selectedIndex == position) {
//        	expand(holder, item);
//			swipeListView.setSelection(selectedIndex);
//        } else {
//        	collapse(holder, item);
//        }
//	}
//
//	private void initEvents(final ViewHolder holder,
//			final vn.infory.infory.data.MessageBySender.messages item, final int position) {
//		holder.linearLogoAndTime.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//                setSelectedIndex(position);
//                notifyDataSetChanged();
//			}
//		});
//		
//		holder.btnPlay.setOnClickListener(new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				try {
//					Log.e(TAG, item.getVideo());
//					Uri video = Uri.parse(item.getVideo());
//					Intent tostart = new Intent(Intent.ACTION_VIEW);
//					tostart.setDataAndType(video, "video/*");
//					mContext.startActivity(tostart);
//				} catch (Exception e) {
//					Log.e(TAG, e.getMessage());
//				}
//			}
//		});
//	}
//	
//	private void expand(final ViewHolder holder, final vn.infory.infory.data.MessageBySender.messages item) {
//		if(item.getStatus() == 0) {
//			// chua doc message, call service mark read message trong day
//			MarkReadMessageTask task = new MarkReadMessageTask(mContext, item.getIdMessage(), 0);
//			task.setMarkReadMessageTaskListener(new onMarkReadMessageTaskListener() {
//				
//				@Override
//				public void onPreMarkReadMessage() {
////					if(dialog != null && !dialog.isShowing())
////						dialog.show();
//				}
//				
//				@Override
//				public void onMarkReadMessageSuccess(String response) {
////					if(dialog != null && dialog.isShowing())
////						dialog.cancel();
//					item.setStatus(1);
//					notifyDataSetChanged();
//					initUIWhenExpand(holder, item);
//				}
//				
//				@Override
//				public void onMarkReadMessageFailure() {
////					if(dialog != null && dialog.isShowing())
////						dialog.cancel();
//					item.setStatus(1);
//					notifyDataSetChanged();
//				}
//			});
//			task.execute();
//		} else {
//			initUIWhenExpand(holder, item);
//		}
//	}
//	
//	private void collapse(ViewHolder holder, vn.infory.infory.data.MessageBySender.messages item) {
//		if(holder.txtContent.getVisibility() != View.GONE) {
//			if(item.getStatus() == 0) {
//				holder.txtDateTime.setTextColor(mContext.getResources().getColor(R.color.black));
//				holder.txtTitle.setTextColor(mContext.getResources().getColor(R.color.black));
//				holder.txtContent.setTextColor(mContext.getResources().getColor(R.color.black));
//			} else {
//				holder.txtDateTime.setTextColor(mContext.getResources().getColor(R.color.text_color_read_message));
//				holder.txtTitle.setTextColor(mContext.getResources().getColor(R.color.text_color_read_message));
//				holder.txtContent.setTextColor(mContext.getResources().getColor(R.color.text_color_read_message));
//			}
////			if(isAnimation)
////				Utils.collapse(holder.imgImage);
////			else
//				holder.imgImage.setVisibility(View.GONE);
//			
//			holder.frameVideoThumb.setVisibility(View.GONE);
//			holder.txtContent.setVisibility(View.GONE);
//			holder.listViewButtons.setVisibility(View.GONE);
//		}
//	}
//
//	private void initUIWhenExpand(ViewHolder holder, vn.infory.infory.data.MessageBySender.messages item) {
//		holder.linearText.setBackgroundResource(R.drawable.leftroundedinput);
//		holder.imgLogoBackground.setBackgroundResource(R.drawable.circle_border_white);
//		if(holder.txtContent.getVisibility() != View.VISIBLE) {
//			holder.txtDateTime.setTextColor(mContext.getResources().getColor(R.color.black));
//			holder.txtTitle.setTextColor(mContext.getResources().getColor(R.color.black));
//			holder.txtContent.setTextColor(mContext.getResources().getColor(R.color.black));
//
//			if(item.getImage() != null && item.getImage().length() > 0) {
////				if(isAnimation)
////					Utils.expand(holder.imgImage);
////				else
//					holder.imgImage.setVisibility(View.VISIBLE);
//			}
//			
//			if(item.getVideo() != null && item.getVideo().length() > 0) {
//				holder.frameVideoThumb.setVisibility(View.VISIBLE);
//			}
//			holder.txtContent.setVisibility(View.VISIBLE);
//
//			List<buttons> lstButtons = item.getButtons();
//			if(lstButtons != null && lstButtons.size() > 0) {
//				holder.listViewButtons.setVisibility(View.VISIBLE);
//			} else {
//				holder.listViewButtons.setVisibility(View.GONE);
//			}
//		}
//	}

	@Override
	@NonNull
	public View getTitleView(int position, View convertView, ViewGroup parent) {
		messages item = messages.get(position);
        convertView = LayoutInflater.from(mContext).inflate(R.layout.expandable_list_item_card_title, parent, false);
        RelativeLayout relaHeaderTitle = (RelativeLayout) convertView.findViewById(R.id.relaHeaderTitleHead);
        ImageView imgLogo = (ImageView) convertView.findViewById(R.id.imgLogo);
		ImageView imgLogoBackground = (ImageView) convertView.findViewById(R.id.imgLogoBackground);
		ImageView imgDotBlue = (ImageView) convertView.findViewById(R.id.imgDotBlue);
		TextView txtDateTime = (TextView) convertView.findViewById(R.id.txtDateTime);
		TextView txtTitle = (TextView) convertView.findViewById(R.id.txtTitleHead);

		FontsCollection.setFont(convertView);
		initGUITitle(item, position, imgLogo, imgLogoBackground, imgDotBlue, txtDateTime, txtTitle, relaHeaderTitle);
        return convertView;
	}

	private void initGUITitle(messages item, int position, final ImageView imgLogo,
			ImageView imgLogoBackground, ImageView imgDotBlue,
			TextView txtDateTime, TextView txtTitle, RelativeLayout relaHeaderTitle) {
		Log.i(TAG, "initGUITitle in position: " + position);
		if (item.getStatus() == 0) {
			// message chua doc
			relaHeaderTitle.setBackgroundResource(R.drawable.leftroundedinput);
			imgLogoBackground.setBackgroundResource(R.drawable.circle_border_white);
			imgDotBlue.setVisibility(View.VISIBLE);
		} else if (item.getStatus() == 1) {
			// da doc message
			relaHeaderTitle.setBackgroundResource(R.drawable.leftrounded_gray);
			imgLogoBackground.setBackgroundResource(R.drawable.circle_border_gray);
			imgDotBlue.setVisibility(View.GONE);
		}
		txtDateTime.setText(item.getTime());
		if(item.getTitle() != null && item.getTitle().length() > 0) {
			txtTitle.setText(item.getTitle());
			txtTitle.setVisibility(View.VISIBLE);
		} else
			txtTitle.setVisibility(View.GONE);
		
		imgLogo.setTag(item.getLogo());
		CyImageLoader.instance().loadImage(item.getLogo(), new CyImageLoader.Listener() {
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
				if (imgLogo.getTag().equals(url)) {
					imgLogo.setImageBitmap(image);
				}
			}
		}, HomeAdapter.mLogoSize, mContext);
		
		if (selectedIndex == position) {
			txtTitle.setVisibility(View.GONE);
	    	relaHeaderTitle.setBackgroundResource(R.drawable.leftroundedinput);
			imgLogoBackground.setBackgroundResource(R.drawable.circle_border_white);
			txtDateTime.setTextColor(mContext.getResources().getColor(R.color.black));
		} else {
			txtTitle.setVisibility(View.VISIBLE);
			if(item.getStatus() == 0) {
				txtDateTime.setTextColor(mContext.getResources().getColor(R.color.black));
				txtTitle.setTextColor(mContext.getResources().getColor(R.color.black));
			} else {
				txtDateTime.setTextColor(mContext.getResources().getColor(R.color.text_color_read_message));
				txtTitle.setTextColor(mContext.getResources().getColor(R.color.text_color_read_message));
			}
		}
	}

	@Override
	@NonNull
	public View getContentView(int position, View convertView, ViewGroup parent) {
		messages item = messages.get(position);
        convertView = LayoutInflater.from(mContext).inflate(R.layout.expandable_list_item_card_content, parent, false);
        RelativeLayout relaHeaderTitle = (RelativeLayout) convertView.findViewById(R.id.relaHeaderTitle);
        ImageView imgImage = (ImageView) convertView.findViewById(R.id.imgImage);
        ListViewNotScroll listViewButtons = (ListViewNotScroll) convertView.findViewById(R.id.listViewButtons);
        ImageView imgVideoThumb = (ImageView) convertView.findViewById(R.id.imgVideoThumb);
        Button btnPlay = (Button) convertView.findViewById(R.id.btnPlay);
        FrameLayout frameVideoThumb = (FrameLayout) convertView.findViewById(R.id.frameVideoThumb);
        FrameLayout loadProgressBar = (FrameLayout) convertView.findViewById(R.id.loadProgressBar);
        FrameLayout loadProgressBarVideo = (FrameLayout) convertView.findViewById(R.id.loadProgressBarVideo);
		TextView txtTitle = (TextView) convertView.findViewById(R.id.txtTitle);
		TextView txtContent = (TextView) convertView.findViewById(R.id.txtContent);

		FontsCollection.setFont(convertView);
		initGUIContent(item, position, imgImage, imgVideoThumb, btnPlay, frameVideoThumb, loadProgressBar, loadProgressBarVideo, txtTitle, txtContent, listViewButtons, relaHeaderTitle);
		initEventContent(item, position, btnPlay);
        return convertView;
	}

	private void initEventContent(final messages item, int position, Button btnPlay) {
		btnPlay.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					Log.e(TAG, item.getVideo());
					Uri video = Uri.parse(item.getVideo());
					Intent tostart = new Intent(Intent.ACTION_VIEW);
					tostart.setDataAndType(video, "video/*");
					mContext.startActivity(tostart);
				} catch (Exception e) {
					AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
					builder.setMessage(mContext.getString(R.string.error_no_video_player));
					builder.setPositiveButton(mContext.getString(R.string.google_play), new OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							mContext.startActivity(new Intent(Intent.ACTION_VIEW,
									Uri.parse("market://search?q=video%20player&c=apps")));
						}
					});
					builder.setNegativeButton(mContext.getString(R.string.Cancel), new OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
					builder.create().show();
				}
			}
		});
	}

	private void initGUIContent(messages item, int position,
			final ImageView imgImage, final ImageView imgVideoThumb,
			final Button btnPlay, FrameLayout frameVideoThumb,
			final FrameLayout loadProgressBar,
			final FrameLayout loadProgressBarVideo, TextView txtTitle,
			TextView txtContent, ListViewNotScroll listViewButtons,
			RelativeLayout relaHeaderTitle) {
	Log.i(TAG, "initGUIContent in position: " + position);
		if (item.getStatus() == 0) {
			// message chua doc
			relaHeaderTitle.setBackgroundResource(R.drawable.leftroundedinput);
		} else if (item.getStatus() == 1) {
			// da doc message
			relaHeaderTitle.setBackgroundResource(R.drawable.leftrounded_gray);
		}
		if(item.getTitle() != null && item.getTitle().length() > 0) {
			txtTitle.setText(item.getTitle());
			txtTitle.setVisibility(View.VISIBLE);
		} else
			txtTitle.setVisibility(View.GONE);

		if(item.getContent() != null && item.getContent().length() > 0) {
			txtContent.setText(item.getContent());
			txtContent.setVisibility(View.VISIBLE);
		} else
			txtContent.setVisibility(View.GONE);
		
		int newWidth = ListMessagesBySenderActivity.screenWidth - mContext.getResources().getDimensionPixelSize(R.dimen.padding_20dip) * 2;
		if ((item.getImage() != null && item.getImage().length() > 0)) {
			((AnimationDrawable) loadProgressBar.getBackground()).start();
			imgImage.setTag(item.getImage());

			int imageWidth = item.getImageWidth();
			int imageHeight = item.getImageHeight();
			float scaleFactor = (float) newWidth / (float) imageWidth;
			int newHeight = (int) (imageHeight * scaleFactor);

			FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) imgImage.getLayoutParams();
			params.width = newWidth;
			params.height = newHeight;
			imgImage.setLayoutParams(params);
			CyImageLoader.instance().loadImage(item.getImage(),
					new CyImageLoader.Listener() {
						@Override
						public void startLoad(int from) {
							loadProgressBar.setVisibility(View.VISIBLE);
							switch (from) {
							case CyImageLoader.FROM_DISK:
							case CyImageLoader.FROM_NETWORK:
								imgImage.setImageBitmap(null);
								break;
							}
						}

						@Override
						public void loadFinish(int from, Bitmap image, String url, CyAsyncTask task) {
							loadProgressBar.setVisibility(View.GONE);
							if (imgImage.getTag().equals(url)) {
								imgImage.setImageBitmap(image);
							}
						}
					}, HomeAdapter.mImageSize, mContext);
		} else {
			loadProgressBar.setVisibility(View.GONE);
			imgImage.setVisibility(View.GONE);
		}

		if (item.getVideoThumbnail() != null && item.getVideoThumbnail().length() > 0) {
			((AnimationDrawable) loadProgressBarVideo.getBackground()).start();
			imgVideoThumb.setTag(item.getVideoThumbnail());

			int videoWidth = item.getVideoWidth();
			int videoHeight = item.getVideoHeight();
			float scaleFactor = (float) newWidth / (float) videoWidth;
			int newHeight = (int) (videoHeight * scaleFactor);

			FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) imgVideoThumb.getLayoutParams();
			params.width = newWidth;
			params.height = newHeight;
			imgVideoThumb.setLayoutParams(params);

			CyImageLoader.instance().loadImage(item.getVideoThumbnail(),
					new CyImageLoader.Listener() {
						@Override
						public void startLoad(int from) {
							loadProgressBarVideo.setVisibility(View.VISIBLE);
							switch (from) {
							case CyImageLoader.FROM_DISK:
							case CyImageLoader.FROM_NETWORK:
								imgVideoThumb.setImageBitmap(null);
								btnPlay.setVisibility(View.INVISIBLE);
								break;
							}
						}

						@Override
						public void loadFinish(int from, Bitmap image, String url, CyAsyncTask task) {
							loadProgressBarVideo.setVisibility(View.GONE);
							if (imgVideoThumb.getTag().equals(url)) {
								imgVideoThumb.setImageBitmap(image);
								btnPlay.setVisibility(View.VISIBLE);
							}
						}
					}, HomeAdapter.mImageSize, mContext);
		} else {
			frameVideoThumb.setVisibility(View.GONE);
		}

		// set adapter list button
		List<buttons> lstButtons = item.getButtons();
		if (lstButtons != null && lstButtons.size() > 0) {
			ListButtonAdapter adapter = new ListButtonAdapter(mContext, lstButtons);
			listViewButtons.setAdapter(adapter);
			listViewButtons.setVisibility(View.VISIBLE);
		} else
			listViewButtons.setVisibility(View.GONE);
		
		if (selectedIndex == position) {
			txtTitle.setVisibility(View.VISIBLE);
	    	relaHeaderTitle.setBackgroundResource(R.drawable.leftroundedinput);
			txtContent.setTextColor(mContext.getResources().getColor(R.color.black));
			txtTitle.setTextColor(mContext.getResources().getColor(R.color.black));
		} else {
			txtTitle.setVisibility(View.GONE);
			if(item.getStatus() == 0) {
				txtContent.setTextColor(mContext.getResources().getColor(R.color.black));
			} else {
				txtContent.setTextColor(mContext.getResources().getColor(R.color.text_color_read_message));
			}
		}
	}
}
