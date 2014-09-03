package vn.infory.infory.notification;

import java.util.ArrayList;
import java.util.List;

import vn.infory.infory.FontsCollection;
import vn.infory.infory.R;
import vn.infory.infory.Tools;
import vn.infory.infory.data.MessageBySender;
import vn.infory.infory.data.MessageBySender.messages;
import vn.infory.infory.mywidget.LoadMoreSwipeListView;
import vn.infory.infory.mywidget.LoadMoreSwipeListView.OnLoadMoreListener;
import vn.infory.infory.network.DeleteMessageTask;
import vn.infory.infory.network.DeleteMessageTask.onDeleteMessageTaskListener;
import vn.infory.infory.network.GetListMessagesBySenderTask;
import vn.infory.infory.network.GetListMessagesBySenderTask.onGetListMessagesBySenderTaskListener;
import vn.infory.infory.network.MarkReadMessageTask;
import vn.infory.infory.network.MarkReadMessageTask.onMarkReadMessageTaskListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fortysevendeg.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.swipelistview.SwipeListView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nhaarman.listviewanimations.appearance.simple.AlphaInAnimationAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.expandablelistitem.ExpandableListItemAdapter.ExpandCollapseListener;

public class ListMessagesBySenderActivity extends FragmentActivity {
	private static final String TAG = "Infory ListMessagesBySenderActivity";

    private static final int INITIAL_DELAY_MILLIS = 250;
    
	private LinearLayout linearContentView;
	private ImageButton btnBack;
	private TextView txtHeader;
	private LoadMoreSwipeListView swipeListView;
//	private ProgressDialog dialog;
	private FrameLayout proNotifications;
	
	// footer view
	private FrameLayout mFooterView;
	// private TextView mLabLoadMore;
	private FrameLayout mProgressBarLoadMore;

	private Context mContext;
	private Activity mActivity;

	public static int screenWidth;
	public static String stringEmptyData = "Không có dữ liệu";

	private int page = 0;
	private int per_page = 10;
	private boolean isLoadMore = false;
	private boolean isNeedReloaded;
//	private boolean mIsLoadingMore = false;
	
	// private MessageInfo messageInfo;
	private int senderID;
	private int messageId = -1;
	private MessageBySender messageBySender;
	private MessageBySenderAdapter adapter;
	private List<messages> lstMessages;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_messags_by_sender_activity_layout);

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		screenWidth = metrics.widthPixels;

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			// messageInfo = new
			// Gson().fromJson(bundle.getString("message_info"),
			// MessageInfo.class);
			senderID = bundle.getInt(NotificationUtil.senderId);
			messageId = bundle.getInt(NotificationUtil.messageId);
		}
		mContext = this;
		mActivity = this;
		lstMessages = new ArrayList<messages>();

		init();
		setGUI();
		initEvents();
	}

	private void init() {
		linearContentView = (LinearLayout) findViewById(R.id.linearContentView);
		btnBack = (ImageButton) findViewById(R.id.btnBack);
		txtHeader = (TextView) findViewById(R.id.txtHeader);
		swipeListView = (LoadMoreSwipeListView) findViewById(R.id.listMessages);

		proNotifications = (FrameLayout) findViewById(R.id.progressBar);
		FrameLayout loadProgressBar = (FrameLayout) findViewById(R.id.loadProgressBar);
		((AnimationDrawable) loadProgressBar.getBackground()).start();

//		LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//
//		// footer
//		mFooterView = (FrameLayout) mInflater.inflate(R.layout.load_more_footer_layout, null, false);
//		mProgressBarLoadMore = (FrameLayout) mFooterView.findViewById(R.id.load_more_progressBar);
//		((AnimationDrawable) mProgressBarLoadMore.getBackground()).start();
//		dialog = new ProgressDialog(mContext);
//		dialog.setMessage("Vui lòng chờ đợi!");
//		dialog.setCancelable(false);

		FontsCollection.setFont(linearContentView);
	}

	private void setGUI() {
		if (Tools.isNetworkAvailable(mContext)) {
			GetListMessagesBySenderTask task = new GetListMessagesBySenderTask(
					mContext, senderID, page);
			task.setGetListMessagesBySenderTaskListener(new onGetListMessagesBySenderTaskListener() {

				@Override
				public void onPreGetListMessagesBySender() {
					proNotifications.setVisibility(View.VISIBLE);
				}

				@Override
				public void onGetListMessagesBySenderSuccess(String response) {
					proNotifications.setVisibility(View.GONE);
					if (response != null && response.compareTo("") != 0) {
						loadMessagesData(response);
						if (messageBySender != null) {
							checkLogicLoadMore(messageBySender);
							initAdapterListView();
						} else {
							initEmptyData();
						}
					}
				}

				@Override
				public void onGetListMessagesBySenderFailure() {
					proNotifications.setVisibility(View.GONE);
					// show error in here
					initEmptyData();
					initAdapterListView();
				}
			});
			task.execute();

		} else {
			isNeedReloaded = true;
			AlertDialog.Builder builder = Tools.AlertNetWorkDialog(mContext, mActivity);
			builder.show();
		}
	}

	private void checkLogicLoadMore(MessageBySender messageBySender) {
		Log.d(TAG, "checkLogicLoadMore: size is "
				+ messageBySender.getMessages().size());
		if (messageBySender.getMessages().size() < 10)
			isLoadMore = false;
		else {
			isLoadMore = true;
			page++;
		}
		Log.d(TAG, "isLoadMore: " + isLoadMore);
	}
    
	private void updateView(int index, boolean isExpanded){
	    View v = swipeListView.getChildAt(index - swipeListView.getFirstVisiblePosition());

	    if(v == null)
	       return;

        RelativeLayout relaHeaderTitleHead = (RelativeLayout) v.findViewById(R.id.relaHeaderTitleHead);
        RelativeLayout relaHeaderTitle = (RelativeLayout) v.findViewById(R.id.relaHeaderTitle);
		ImageView imgLogoBackground = (ImageView) v.findViewById(R.id.imgLogoBackground);
		TextView txtDateTime = (TextView) v.findViewById(R.id.txtDateTime);
    	TextView txtTitleHead = (TextView) v.findViewById(R.id.txtTitleHead);
	    TextView txtTitleContent = (TextView) v.findViewById(R.id.txtTitle);
		TextView txtContent = (TextView) v.findViewById(R.id.txtContent);
	    if(isExpanded) {
	    	txtTitleHead.setVisibility(View.GONE);
	    	txtTitleContent.setVisibility(View.VISIBLE);
	    	relaHeaderTitle.setBackgroundResource(R.drawable.leftroundedinput);
	    	relaHeaderTitleHead.setBackgroundResource(R.drawable.leftroundedinput);
			imgLogoBackground.setBackgroundResource(R.drawable.circle_border_white);
			txtDateTime.setTextColor(mContext.getResources().getColor(R.color.black));
			txtTitleContent.setTextColor(mContext.getResources().getColor(R.color.black));
			txtContent.setTextColor(mContext.getResources().getColor(R.color.black));
	    } else {
	    	txtTitleHead.setVisibility(View.VISIBLE);
	    	txtTitleContent.setVisibility(View.GONE);
	    	if(lstMessages.get(index).getStatus() == 0) {
				txtDateTime.setTextColor(mContext.getResources().getColor(R.color.black));
				txtTitleHead.setTextColor(mContext.getResources().getColor(R.color.black));
				relaHeaderTitleHead.setBackgroundResource(R.drawable.leftroundedinput);
				imgLogoBackground.setBackgroundResource(R.drawable.circle_border_white);
			} else {
				txtDateTime.setTextColor(mContext.getResources().getColor(R.color.text_color_read_message));
				txtTitleHead.setTextColor(mContext.getResources().getColor(R.color.text_color_read_message));
				relaHeaderTitleHead.setBackgroundResource(R.drawable.leftrounded_gray);
				imgLogoBackground.setBackgroundResource(R.drawable.circle_border_gray);
			}
	    }
	}
	
	protected void initAdapterListView() {
		if (messageBySender.getSender().compareTo(stringEmptyData) == 0) {
			swipeListView.setSwipeMode(SwipeListView.SWIPE_MODE_NONE);
		} else {
			float width_deletehide_btn = getResources().getDimensionPixelOffset(R.dimen.width_delete);
			swipeListView.setSwipeMode(SwipeListView.SWIPE_MODE_LEFT);
			swipeListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			swipeListView.setOffsetLeft(screenWidth - width_deletehide_btn);
		}
		adapter = new MessageBySenderAdapter(mContext, lstMessages);
		adapter.setExpandCollapseListener(new ExpandCollapseListener() {

			@Override
			public void onItemExpanded(int position) {
				Log.e(TAG, "onItemExpanded: " + position);
				updateView(position, true);
				adapter.setSelectedIndex(position);
			}

			@Override
			public void onItemCollapsed(int position) {
				Log.e(TAG, "onItemCollapsed: " + position);
				updateView(position, false);
				adapter.setSelectedIndex(position);
			}
		});
		AlphaInAnimationAdapter alphaInAnimationAdapter = new AlphaInAnimationAdapter(adapter);
        alphaInAnimationAdapter.setAbsListView(swipeListView);

        assert alphaInAnimationAdapter.getViewAnimator() != null;
        alphaInAnimationAdapter.getViewAnimator().setInitialDelayMillis(INITIAL_DELAY_MILLIS);
        swipeListView.setAdapter(alphaInAnimationAdapter);
        adapter.setLimit(1);
		swipeListView.setAdapter(adapter);
		int index = 0;
		if (messageId > 0) {
			for (int i = 0; i < messageBySender.getMessages().size(); i++) {
				messages mess = messageBySender.getMessages().get(i);
				if (mess.getIdMessage() == messageId) {
					index = i;
					break;
				}
			}
		}
		adapter.expand(index);
		updateView(index, true);
	}

	private void initEmptyData() {
		MessageBySender messageInfo = new MessageBySender();
		messageInfo.setSender(stringEmptyData);
	}

	protected boolean loadMessagesData(String response) {
		boolean checkLoad = false;
		try {
			messageBySender = new Gson().fromJson(response, new TypeToken<MessageBySender>() {}.getType());
			txtHeader.setText(messageBySender.getSender());
			lstMessages = messageBySender.getMessages();
			Log.d(TAG, "messageBySender.getMessages().size(): " + messageBySender.getMessages().size());
		} catch (Exception ex) {
			Log.e(TAG + "loadMessagesData: ", ex.toString());
			checkLoad = false;
		}
		return checkLoad;
	}

	private void initEvents() {
		btnBack.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		// set a listener to be invoked when the list reaches the end
		swipeListView.setOnLoadMoreListener(new OnLoadMoreListener() {
			public void onLoadMore(FrameLayout mProgressBarLoadMore) {
				// Do the work to load more items at the end of list here
				if (Tools.isNetworkAvailable(mContext)) {
					new LoadDataTask(mProgressBarLoadMore).execute();
				} else {
					isNeedReloaded = true;
					AlertDialog.Builder builder = Tools.AlertNetWorkDialog(mContext, mActivity);
					builder.show();
				}
			}
		});
				
		swipeListView.setSwipeListViewListener(new BaseSwipeListViewListener() {
			@Override
			public void onOpened(int position, boolean toRight) {

			}

			@Override
			public void onClosed(int position, boolean fromRight) {

			}

			@Override
			public void onListChanged() {
			}

			@Override
			public void onMove(int position, float x) {
			}

			@Override
			public void onStartOpen(int position, int action, boolean right) {
				Log.d("swipe", String.format("onStartOpen %d - action %d",
						position, action));
			}

			@Override
			public void onStartClose(int position, boolean right) {
				Log.d("swipe", String.format("onStartClose %d", position));
			}

			@Override
			public void onClickFrontView(final int position) {
				Log.d("swipe", String.format("onClickFrontView %d", position));
				if (lstMessages.get(position).getStatus() == 0) {
					// chua doc message, call service mark read message trong
					// day

					lstMessages.get(position).setStatus(1);
					updateView(position, true);
					adapter.expand(position);
					swipeListView.postDelayed(new Runnable() {
						
						@Override
						public void run() {
							swipeListView.smoothScrollToPosition(position);
						}
					}, 500);
					MarkReadMessageTask task = new MarkReadMessageTask(mContext, lstMessages.get(position).getIdMessage(), 0);
//					task.setMarkReadMessageTaskListener(new onMarkReadMessageTaskListener() {
//
//						@Override
//						public void onPreMarkReadMessage() {
//							
//						}
//
//						@Override
//						public void onMarkReadMessageSuccess(String response) {
//						}
//
//						@Override
//						public void onMarkReadMessageFailure() {
//							
//						}
//					});
					task.execute();
				} else {
					adapter.expand(position);
					swipeListView.postDelayed(new Runnable() {
						
						@Override
						public void run() {
							swipeListView.smoothScrollToPosition(position);
						}
					}, 500);
				}
//				int h1 = swipeListView.getHeight();
//				int h2 = v.getHeight();
//
//				swipeListView.smoothScrollToPositionFromTop(position, h1/2 - h2/2, INITIAL_DELAY_MILLIS);
			}

			@Override
			public void onClickBackView(final int position) {
				Log.d("swipe", String.format("onClickBackView %d", position));
				messages message = messageBySender.getMessages().get(position);
				DeleteMessageTask task = new DeleteMessageTask(mContext, message.getIdMessage(), 0);
//				task.setDeleteMessageTaskListener(new onDeleteMessageTaskListener() {
//
//					@Override
//					public void onPreDeleteMessage() {
//						if (dialog != null && !dialog.isShowing())
//							dialog.show();
//					}
//
//					@Override
//					public void onDeleteMessageSuccess(String response) {
//						if (dialog != null && dialog.isShowing())
//							dialog.cancel();
//						messageBySender.getMessages().remove(position);
//						adapter.notifyDataSetChanged();
//						swipeListView.closeOpenedItems(true);
//
//					}
//
//					@Override
//					public void onDeleteMessageFailure() {
//						if (dialog != null && dialog.isShowing())
//							dialog.cancel();
//						swipeListView.closeOpenedItems(true);
//					}
//				});
				if (Tools.isNetworkAvailable(mContext)) {
	            	task.execute();
					messageBySender.getMessages().remove(position);
					adapter.notifyDataSetChanged();
					swipeListView.closeOpenedItems(true);
					NotificationActivity.isNeedReloaded = true;
				} else {
					isNeedReloaded = true;
					AlertDialog.Builder builder = Tools.AlertNetWorkDialog(mContext, mActivity);
					builder.show();
				}
			}

			@Override
			public void onDismiss(int[] reverseSortedPositions) {

			}

			@Override
			public void onLastListItem() {
				super.onLastListItem();
				// Do the work to load more items at the end of list here
				Log.e(TAG, "onLastListItem");
//				if (!mIsLoadingMore) {
//					if (Tools.isNetworkAvailable(mContext)) {
//						new LoadDataTask().execute();
//					} else {
//						isNeedReloaded = true;
//						AlertDialog.Builder builder = Tools.AlertNetWorkDialog(mContext, mActivity);
//						builder.show();
//					}
//				} else {
//					onLoadMoreComplete();
//				}
			}
		});
	}

	private class LoadDataTask extends AsyncTask<Void, Void, Void> {
		private FrameLayout mProgressBarLoadMore;
		
		public LoadDataTask(FrameLayout mProgressBarLoadMore) {
			this.mProgressBarLoadMore = mProgressBarLoadMore;
		}

		private MessageBySender loadDataLoadMore(String response) {
			MessageBySender messageBySender = new Gson().fromJson(response,
					new TypeToken<MessageBySender>() {
					}.getType());
			return messageBySender;
		}

		@Override
		protected Void doInBackground(Void... params) {
			return null;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
//			mIsLoadingMore = true;
//			swipeListView.addFooterView(mFooterView);
			if (isLoadMore) {
				mProgressBarLoadMore.setVisibility(View.VISIBLE);
				GetListMessagesBySenderTask task = new GetListMessagesBySenderTask(mContext, senderID, page);
				task.setGetListMessagesBySenderTaskListener(new onGetListMessagesBySenderTaskListener() {

					@Override
					public void onPreGetListMessagesBySender() {
					}

					@Override
					public void onGetListMessagesBySenderSuccess(String response) {
						if (response != null && response.compareTo("") != 0) {
							MessageBySender messageBySender = loadDataLoadMore(response);
							if (messageBySender.getMessages() != null && messageBySender.getMessages().size() > 0) {
								checkLogicLoadMore(messageBySender);
								for (int i = 0; i < messageBySender.getMessages().size(); i++) {
									lstMessages.add(messageBySender.getMessages().get(i));
								}
								adapter.notifyDataSetChanged();
							} else {
								isLoadMore = false;
							}
						}
					}

					@Override
					public void onGetListMessagesBySenderFailure() {

					}
				});
				task.execute();
			} else {
				swipeListView.onLoadMoreComplete();
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			// Call onLoadMoreComplete when the LoadMore task, has finished
			swipeListView.onLoadMoreComplete();
		}
	}
	
//	private void onLoadMoreComplete() {
//		mIsLoadingMore = false;
//		mProgressBarLoadMore.setVisibility(View.GONE);
//		swipeListView.removeFooterView(mFooterView);
//	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(isNeedReloaded) {
			messageBySender = null;
			lstMessages.clear();
			setGUI();
			isNeedReloaded = false;
    	}
	}

}
