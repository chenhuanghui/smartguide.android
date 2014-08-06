package vn.infory.infory.notification;

import java.util.ArrayList;
import java.util.List;

import vn.infory.infory.R;
import vn.infory.infory.data.MessageInfo;
import vn.infory.infory.mywidget.MyPTRAndSwipeListView;
import vn.infory.infory.mywidget.MyPTRAndSwipeListView.OnActionPullToRefreshAndLoadMoreListView;
import vn.infory.infory.network.GetNotificationTask;
import vn.infory.infory.network.GetNotificationTask.onGetNotificationsTaskListener;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.fortysevendeg.swipelistview.SwipeListView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshSwipsListView;

public class NotificationActivity extends FragmentActivity {
    private static final String TAG = "Infory NotificationActivity";

    public static final int GET_UNREAD_MESSAGES = 0;
    public static final int GET_READ_MESSAGES = 1;
    public static final int GET_ALL_MESSAGES = 2;
    OnActionPullToRefreshAndLoadMoreListView onActionPullToRefreshAndLoadMore = new OnActionPullToRefreshAndLoadMoreListView() {

        @Override
        public void onRefreshListView(
                PullToRefreshSwipsListView mPullRefreshListView,
                ProgressBar proNotifications, boolean isShowProgressBar) {
            loadDataForFirstTime(mPullRefreshListView, proNotifications, isShowProgressBar);
        }

        @Override
        public void onLoadMoreListView(boolean mIsLoadingMore, ProgressBar mProgressBarLoadMore) {
            loadMoreListMessagesTask task = new loadMoreListMessagesTask(mIsLoadingMore, mProgressBarLoadMore);
            task.execute();
        }

        @Override
        public void onClickBackViewListView(final int position) {
        	int positionClick = position - swipeListView.getHeaderViewsCount();
        }

        @Override
        public void onClickFrontViewListView(int position) {
//            DoctorInfo info = lstDoctors.get(position - swipeListView.getHeaderViewsCount());
//            Intent intent = new Intent(mContext, DoctorProfileActivity.class);
//            intent.putExtra(com.medpat.ringmd.Constants.BUNDLE_STRING_DOCTOR_INFO, new Gson().toJson(info));
//            startActivity(intent);
        }

        @Override
        public void onOpenedItem(int position) {
        }

        @Override
        public void onClosedItem(int position) {

        }

    };
    private MyPTRAndSwipeListView myPullToRefreshSwipeListView;
    private SwipeListView swipeListView;
	private ProgressDialog dialog;

    private Context mContext;

	private int screenWidth;
    public static String stringEmptyData = "Không có dữ liệu";
    private List<MessageInfo> lstMessages;
	private NotificationAdapter viewAdapter;
    
    private int type = GET_ALL_MESSAGES;
    private int page = 0;
    private int per_page = 10;
    private boolean isLoadMore = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_activity_layout);

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		screenWidth = metrics.widthPixels;
		
        mContext = this;

        lstMessages = new ArrayList<MessageInfo>();
        
        init();
        setGUI();
        initEvents();
    }

    private void init() {
        myPullToRefreshSwipeListView = (MyPTRAndSwipeListView) findViewById(R.id.myPullToRefreshSwipeListView);
        swipeListView = myPullToRefreshSwipeListView.getListView();

		dialog = new ProgressDialog(mContext);
		dialog.setMessage("Vui lòng chờ đợi!");
		dialog.setCancelable(false);
    }

    private void setGUI() {
        myPullToRefreshSwipeListView.setOnActionPullToRefreshAndLoadMoreListView(onActionPullToRefreshAndLoadMore);
        // reload data
        myPullToRefreshSwipeListView.activePullToRefeshAndLoadMoreListView();
    }

    private void initEvents() {
        
    }

    private void loadDataForFirstTime(
            final PullToRefreshSwipsListView mPullRefreshListView,
            final ProgressBar proNotifications, final boolean isShowProgressBar) {
    	page = 0;
    	GetNotificationTask task = new GetNotificationTask(mContext, type, page);
    	task.setGetNotificationsTaskListener(new onGetNotificationsTaskListener() {
			
			@Override
			public void onPreGetNotifications() {
				if (isShowProgressBar) {
					proNotifications.setVisibility(View.VISIBLE);
				} else {
					proNotifications.setVisibility(View.GONE);
				}
			}
			
			@Override
			public void onGetNotificationsSuccess(String response) {
				proNotifications.setVisibility(View.GONE);
				if (lstMessages != null && lstMessages.size() > 0) {
					lstMessages.clear();
					lstMessages = new ArrayList<MessageInfo>();
				}
				if (response != null && response.compareTo("") != 0) {
					loadMessagesData(response);
					if (lstMessages != null && lstMessages.size() > 0) {
						checkLogicLoadMore(lstMessages);
						initAdapterListView();
					} else {
						initEmptyData();
					}
					mPullRefreshListView.onRefreshComplete();
				}
			}
			
			@Override
			public void onGetNotificationsFailure() {
				proNotifications.setVisibility(View.GONE);
				// show error in here
				if (lstMessages != null && lstMessages.size() > 0) {
					lstMessages.clear();
					lstMessages = new ArrayList<MessageInfo>();
				}
				initEmptyData();
				initAdapterListView();
				mPullRefreshListView.onRefreshComplete();
			}
		});
    	task.execute();
    }

    protected void initAdapterListView() {
    	if(lstMessages.size() == 1 && 
    			lstMessages.get(0).getSender().compareTo(stringEmptyData) == 0){
			swipeListView.setSwipeMode(SwipeListView.SWIPE_MODE_NONE);
		}else{
			float width_deletehide_btn = getResources().getDimensionPixelOffset(R.dimen.width_delete);
			swipeListView.setSwipeMode(SwipeListView.SWIPE_MODE_LEFT);
			swipeListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			swipeListView.setOffsetLeft(screenWidth - width_deletehide_btn);
		}
		viewAdapter = new NotificationAdapter(mContext, lstMessages);
		swipeListView.setAdapter(viewAdapter);
		viewAdapter.notifyDataSetChanged();
    }

    private boolean loadMessagesData(String response) {
        boolean checkLoad = false;
        try {
            lstMessages = new Gson().fromJson(response, new TypeToken<List<MessageInfo>>() {
            }.getType());
            addSpecialMessage();
        } catch (Exception ex) {
            Log.e(TAG + "loadMessagesData: ", ex.toString());
            checkLoad = false;
            initEmptyData();
        }
        return checkLoad;
    }

    private void checkLogicLoadMore(List<MessageInfo> lstMessages) {
    	Log.d(TAG, "checkLogicLoadMore: size is " + lstMessages.size());
    	if(lstMessages.size() < 10)
			isLoadMore = false;
		else {
            isLoadMore = true;
            page++;
		}
    	Log.d(TAG, "isLoadMore: " + isLoadMore);
    }
    
    private void addSpecialMessage() {
//    	MessageInfo messageSpecial = new MessageInfo();
//        messageSpecial.setStatus(-1);
//        for(int i = 0; i < lstMessages.size(); i++) {
//        	MessageInfo message1 = lstMessages.get(i);
//        	if(message1.getStatus() == -1) {
//        		return;
//        	}
//        }
//        for(int i = 0; i < lstMessages.size(); i++) {
//        	MessageInfo message1 = lstMessages.get(i);
//        	if(i < lstMessages.size() - 1) {
//        		MessageInfo message2 = lstMessages.get(i + 1);
//	        	if(message1.getStatus() != message2.getStatus()) {
//	        		Log.d(TAG, "Index will have special message: " + (i + 1));
//	        		lstMessages.add(i + 1, messageSpecial);
//	        		break;
//	        	}
//        	}
//        }
    }
    
    private void initEmptyData() {
    	MessageInfo messageInfo = new MessageInfo();
    	messageInfo.setSender(stringEmptyData);
    	lstMessages.add(messageInfo);
    }

    @Override
    protected void onResume() {
    	super.onResume();
    }

    private class loadMoreListMessagesTask extends AsyncTask<Void, Void, Void> {

        private boolean mIsLoadingMore;
        private ProgressBar mProgressBarLoadMore;

        public loadMoreListMessagesTask(boolean mIsLoadingMore, ProgressBar mProgressBarLoadMore) {
            this.mIsLoadingMore = mIsLoadingMore;
            this.mProgressBarLoadMore = mProgressBarLoadMore;
        }

        private List<MessageInfo> loadDataLoadMore(String response) {
            List<MessageInfo> lstMessages = new ArrayList<MessageInfo>();
            try {
                lstMessages = new Gson().fromJson(response, new TypeToken<List<MessageInfo>>() {
                }.getType());
            } catch (Exception ex) {
                Log.e(TAG + "loadDataLoadMore: ", ex.toString());
            }
            return lstMessages;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.v(TAG + "loadmore: ", "page + isLoadMore: " + page + " + " + isLoadMore);
            if (isLoadMore) {
            	GetNotificationTask task = new GetNotificationTask(mContext, type, page);
                task.setGetNotificationsTaskListener(new onGetNotificationsTaskListener() {

                    @Override
                    public void onPreGetNotifications() {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onGetNotificationsSuccess(String response) {
                        if (response != null && response.compareTo("") != 0) {
                            List<MessageInfo> lstMessagesLoadMore = loadDataLoadMore(response);
                            if (lstMessagesLoadMore != null && lstMessagesLoadMore.size() > 0) {
        						checkLogicLoadMore(lstMessagesLoadMore);
                                for (int i = 0; i < lstMessagesLoadMore.size(); i++) {
                                    lstMessages.add(lstMessagesLoadMore.get(i));
                                }
                                addSpecialMessage();
                                viewAdapter.notifyDataSetChanged();
                            }
                        }
                    }

                    @Override
                    public void onGetNotificationsFailure() {
                        // TODO Auto-generated method stub

                    }
                });
                task.execute();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Call onLoadMoreComplete when the LoadMore task, has finished
            onLoadMoreComplete();
        }

        public void onLoadMoreComplete() {
            mIsLoadingMore = false;
            mProgressBarLoadMore.setVisibility(View.GONE);
            myPullToRefreshSwipeListView.setMyIsLoadingMore(mIsLoadingMore);
        }
    }
}
