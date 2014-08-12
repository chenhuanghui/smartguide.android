package vn.infory.infory.notification;

import vn.infory.infory.FontsCollection;
import vn.infory.infory.R;
import vn.infory.infory.data.MessageBySender;
import vn.infory.infory.data.MessageInfo;
import vn.infory.infory.network.GetListMessagesBySenderTask;
import vn.infory.infory.network.GetListMessagesBySenderTask.onGetListMessagesBySenderTaskListener;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
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
    private SwipeListView swipeListView;
	private ProgressDialog dialog;

    private Context mContext;

	public static int screenWidth;
    public static String stringEmptyData = "Không có dữ liệu";

    private int page = 0;
    private int per_page = 10;
    private boolean isLoadMore = false;
    
    private MessageInfo messageInfo;
    private MessageBySender messageBySender;
    private MessageBySenderAdapter adapter;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_messags_by_sender_activity_layout);
		
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		screenWidth = metrics.widthPixels;
		
		Bundle bundle = getIntent().getExtras();
		if(bundle != null) {
			messageInfo = new Gson().fromJson(bundle.getString("message_info"), MessageInfo.class);
		}
        mContext = this;
        
        init();
        setGUI();
        initEvents();
	}

	private void init() {
    	linearContentView = (LinearLayout) findViewById(R.id.linearContentView);
    	btnBack = (ImageButton) findViewById(R.id.btnBack);
    	txtHeader = (TextView) findViewById(R.id.txtHeader);
		swipeListView = (SwipeListView) findViewById(R.id.listMessages);

		dialog = new ProgressDialog(mContext);
		dialog.setMessage("Vui lòng chờ đợi!");
		dialog.setCancelable(false);

		FontsCollection.setFont(linearContentView);
	}

	private void setGUI() {
		txtHeader.setText(messageInfo.getSender());
		GetListMessagesBySenderTask task = new GetListMessagesBySenderTask(mContext, messageInfo.getIdSender(), page);
		task.setGetListMessagesBySenderTaskListener(new onGetListMessagesBySenderTaskListener() {
			
			@Override
			public void onPreGetListMessagesBySender() {
				if(dialog != null && !dialog.isShowing())
					dialog.show();
			}
			
			@Override
			public void onGetListMessagesBySenderSuccess(String response) {
				if(dialog != null && dialog.isShowing())
					dialog.cancel();
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
				if(dialog != null && dialog.isShowing())
					dialog.cancel();
				// show error in here
				initEmptyData();
				initAdapterListView();
			}
		});
		task.execute();
	}
	
    private void checkLogicLoadMore(MessageBySender messageBySender) {
    	Log.d(TAG, "checkLogicLoadMore: size is " + messageBySender.getMessages().size());
    	if(messageBySender.getMessages().size() < 10)
			isLoadMore = false;
		else {
            isLoadMore = true;
            page++;
		}
    	Log.d(TAG, "isLoadMore: " + isLoadMore);
    }
	
	protected void initAdapterListView() {
    	if(messageBySender.getSender().compareTo(stringEmptyData) == 0){
			swipeListView.setSwipeMode(SwipeListView.SWIPE_MODE_NONE);
		}else{
			float width_deletehide_btn = getResources().getDimensionPixelOffset(R.dimen.width_delete);
			swipeListView.setSwipeMode(SwipeListView.SWIPE_MODE_LEFT);
			swipeListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			swipeListView.setOffsetLeft(screenWidth - width_deletehide_btn);
		}
		adapter = new MessageBySenderAdapter(mContext, R.layout.activity_expandablelistitem_card, messageBySender.getMessages(), swipeListView);
		swipeListView.setAdapter(adapter);
    	// expand item in position 0 by default
		adapter.setSelectedIndex(0);
		adapter.notifyDataSetChanged();
    }
	
    private void initEmptyData() {
    	MessageBySender messageInfo = new MessageBySender();
    	messageInfo.setSender(stringEmptyData);
    }
    
	protected boolean loadMessagesData(String response) {
		boolean checkLoad = false;
        try {
        	messageBySender = new Gson().fromJson(response, new TypeToken<MessageBySender>() {
            }.getType());
        	Log.e(TAG, "messageBySender.getMessages().size(): " + messageBySender.getMessages().size());
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
                Log.d("swipe", String.format("onStartOpen %d - action %d", position, action));
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
            public void onClickBackView(int position) {
                Log.d("swipe", String.format("onClickBackView %d", position));
            }

            @Override
            public void onDismiss(int[] reverseSortedPositions) {
                
            }

        });
	}
	
}
