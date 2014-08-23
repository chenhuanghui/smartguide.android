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
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.fortysevendeg.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.swipelistview.SwipeListView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ListMessagesBySenderActivity extends FragmentActivity {
	private static final String TAG = "Infory ListMessagesBySenderActivity";

	private LinearLayout linearContentView;
	private ImageButton btnBack;
	private TextView txtHeader;
	private LoadMoreSwipeListView swipeListView;
	private ProgressDialog dialog;
	private FrameLayout proNotifications;

	private Context mContext;
	private Activity mActivity;

	public static int screenWidth;
	public static String stringEmptyData = "Không có dữ liệu";

	private int page = 0;
	private int per_page = 10;
	private boolean isLoadMore = false;

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

		dialog = new ProgressDialog(mContext);
		dialog.setMessage("Vui lòng chờ đợi!");
		dialog.setCancelable(false);

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
			AlertDialog.Builder builder = Tools.AlertNetWorkDialog(
					mContext, mActivity);
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

	protected void initAdapterListView() {
		if (messageBySender.getSender().compareTo(stringEmptyData) == 0) {
			swipeListView.setSwipeMode(SwipeListView.SWIPE_MODE_NONE);
		} else {
			float width_deletehide_btn = getResources()
					.getDimensionPixelOffset(R.dimen.width_delete);
			swipeListView.setSwipeMode(SwipeListView.SWIPE_MODE_LEFT);
			swipeListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			swipeListView.setOffsetLeft(screenWidth - width_deletehide_btn);
		}
		adapter = new MessageBySenderAdapter(mContext,
				R.layout.activity_expandablelistitem_card,
				lstMessages, swipeListView);
		swipeListView.setAdapter(adapter);
		if (messageId > 0) {
			int index = 0;
			for (int i = 0; i < messageBySender.getMessages().size(); i++) {
				messages mess = messageBySender.getMessages().get(i);
				if (mess.getIdMessage() == messageId) {
					index = i;
					break;
				}
			}
			adapter.setSelectedIndex(index);
		} else {
			// expand item in position 0 by default
			adapter.setSelectedIndex(0);
		}
		adapter.notifyDataSetChanged();
	}

	private void initEmptyData() {
		MessageBySender messageInfo = new MessageBySender();
		messageInfo.setSender(stringEmptyData);
	}

	protected boolean loadMessagesData(String response) {
		boolean checkLoad = false;
		try {
			messageBySender = new Gson().fromJson(response,
					new TypeToken<MessageBySender>() {
					}.getType());
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
			public void onLoadMore() {
				// Do the work to load more items at the end of list here
				if (Tools.isNetworkAvailable(mContext)) {
					new LoadDataTask().execute();
				} else {
					AlertDialog.Builder builder = Tools.AlertNetWorkDialog(
							mContext, mActivity);
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
			public void onClickFrontView(int position) {
				Log.d("swipe", String.format("onClickFrontView %d", position));
			}

			@Override
			public void onClickBackView(final int position) {
				Log.d("swipe", String.format("onClickBackView %d", position));
				messages message = messageBySender.getMessages().get(position);
				DeleteMessageTask task = new DeleteMessageTask(mContext,
						message.getIdMessage(), 0);
				task.setDeleteMessageTaskListener(new onDeleteMessageTaskListener() {

					@Override
					public void onPreDeleteMessage() {
						if (dialog != null && !dialog.isShowing())
							dialog.show();
					}

					@Override
					public void onDeleteMessageSuccess(String response) {
						if (dialog != null && dialog.isShowing())
							dialog.cancel();
						messageBySender.getMessages().remove(position);
						adapter.notifyDataSetChanged();
						swipeListView.closeOpenedItems(true);

					}

					@Override
					public void onDeleteMessageFailure() {
						if (dialog != null && dialog.isShowing())
							dialog.cancel();
						swipeListView.closeOpenedItems(true);
					}
				});
				task.execute();
			}

			@Override
			public void onDismiss(int[] reverseSortedPositions) {

			}

		});
	}

	private class LoadDataTask extends AsyncTask<Void, Void, Void> {

		private MessageBySender loadDataLoadMore(String response) {
			MessageBySender messageBySender = new Gson().fromJson(response,
					new TypeToken<MessageBySender>() {
					}.getType());
			return messageBySender;
		}

		@Override
		protected Void doInBackground(Void... params) {
			if (isCancelled()) {
				return null;
			}
			return null;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (isLoadMore) {
				GetListMessagesBySenderTask task = new GetListMessagesBySenderTask(
						mContext, senderID, page);
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
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			// Call onLoadMoreComplete when the LoadMore task, has finished
			swipeListView.onLoadMoreComplete();
			super.onPostExecute(result);
		}

		@Override
		protected void onCancelled() {
			// Notify the loading more operation has finished
			swipeListView.onLoadMoreComplete();
		}
	}

}
