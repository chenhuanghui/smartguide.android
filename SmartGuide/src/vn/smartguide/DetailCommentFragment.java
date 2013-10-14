package vn.smartguide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import vn.smartguide.CyImageLoader.Listener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

/**
 * 
 * Comment fragment
 * Get, show and post comment
 *
 */
@SuppressLint("ValidFragment")	
public class DetailCommentFragment extends Fragment {
	private CommentListAdapter 		mAdapter;
	private Shop 					mShop;
	private ListView				mLst;
	private boolean					mLoading;
	private int 					mPage = 0;
	private boolean					mPageEnd;

	private MyDialogFragment		dialogFragment;
	private ImageView 				mAvatar;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		return inflater.inflate(R.layout.detail_comment, container, false);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);

		// Set btnComment click event 
		TextView txtComment = (TextView) getView().findViewById(R.id.btnComment);
		txtComment.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				showDialog();
			}
		});

		// set comment listview adapter
		mLst = (ListView) getView().findViewById(R.id.lstComment);

		mAdapter = new CommentListAdapter();
		mLst.setAdapter(mAdapter);
		mAvatar = (ImageView)getView().findViewById(R.id.avatar);
	}

	/**
	 * Show the popup dialog fragment
	 */
	void showDialog() {
		dialogFragment = new MyDialogFragment();
		dialogFragment.show(getFragmentManager(), "dialog");
	}

	public void setData(Shop s) {
		
		if (s != null) {
			mShop = s;
	
			mPage = 1;
			mPageEnd = false;
			mLoading = false; 
			mAdapter.mItemList.clear();
			mAdapter.mItemList.addAll(s.mCommentList);
			mAdapter.notifyDataSetChanged();
			mLst.setSelectionFromTop(mAdapter.getCount() - 1, 0);
			if (GlobalVariable.avatarFace != "" || GlobalVariable.avatarFace.compareTo("null") != 0)
				GlobalVariable.cyImageLoader.showImage(GlobalVariable.avatarFace, mAvatar);
		} else {
			mPage = 1;
			mPageEnd = false;
			mLoading = false; 
			mAdapter.mItemList.clear();
			mAdapter.notifyDataSetChanged();
		}
	}

	public void releaseMemory(){
		try{
			mAdapter.mItemList.clear();
			mAdapter.mItemList.addAll(new ArrayList<Comment>());
			mAdapter.notifyDataSetChanged();
			mAvatar.setImageBitmap(null);
		}catch(Exception ex){

		}
	}
	/**
	 * 
	 * List adapter for comment lists in Comment fragment and popup dialog fragment
	 *
	 */
	public class CommentListAdapter extends BaseAdapter
	{
		private LayoutInflater inflater;
		public List<Comment> mItemList = new ArrayList<Comment>(); 

		public CommentListAdapter() {
			inflater = DetailCommentFragment.this.getActivity().getLayoutInflater();
		}

		@Override
		public int getCount() {
			return mItemList.size();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				convertView = inflater.inflate(R.layout.comment_item, null);
			}

			// Update item view
			TextView txtName = (TextView) convertView.findViewById(R.id.txtName);
			TextView txtTime = (TextView) convertView.findViewById(R.id.txtTime);
			TextView txtComment = (TextView) convertView.findViewById(R.id.txtComment);
			final ImageView imgAva = (ImageView) convertView.findViewById(R.id.imgAva);			

			Comment item = mItemList.get(position);
			txtName.setText(item.mUser);
			txtTime.setText(item.mTime);
			txtComment.setText(item.mComment);
			imgAva.setTag(item.mAvaUrl);
			GlobalVariable.cyImageLoader.loadImage(item.mAvaUrl, new Listener() {
				@Override
				public void startLoad(int from) {
					switch (from) {
						case CyImageLoader.FROM_DISK:
						case CyImageLoader.FROM_NETWORK:
							imgAva.setImageResource(R.drawable.ava_loading);
							break;
					}
				}
				
				@Override
				public void loadFinish(int from, Bitmap image, String url) {
					switch (from) {
					case CyImageLoader.FROM_MEMORY:
						imgAva.setImageBitmap(image);
						break;
						
					case CyImageLoader.FROM_DISK:
					case CyImageLoader.FROM_NETWORK:
						if (((String) imgAva.getTag()).equals(url))
							imgAva.setImageBitmap(image);
						break;
					}
				}
			}, new Point(), getActivity());

			return convertView;
		}

		@Override
		public Object getItem(int pos) {
			return pos;
		}

		@Override
		public long getItemId(int pos) {
			return pos;
		}
	}

	/**
	 * 
	 * Comment dialog
	 * Popup when user touch the comment text box
	 * 
	 */
	public class MyDialogFragment extends DialogFragment {

		public ListView lstComment;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Pick a style based on the num.
			int style = DialogFragment.STYLE_NO_FRAME; 
			int theme = R.style.MyCustomTheme;

			setStyle(style, theme);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View v = inflater.inflate(R.layout.comment_dialog, container, false);
			lstComment = (ListView) v.findViewById(R.id.lstComment);
			lstComment.setAdapter(DetailCommentFragment.this.mAdapter);

			EditText edtComment = (EditText) v.findViewById(R.id.edtComment);
			ImageView avatar = (ImageView)v.findViewById(R.id.avatar);
			if (GlobalVariable.avatarFace != "" || GlobalVariable.avatarFace.compareTo("null") != 0)
				GlobalVariable.cyImageLoader.showImage(GlobalVariable.avatarFace, avatar);
			
			edtComment.setOnEditorActionListener(new OnEditorActionListener() {

				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

					if (actionId != EditorInfo.IME_ACTION_SEND)
						return false;

					EditText edtComment = (EditText) getView().findViewById(R.id.edtComment); 
					String content = edtComment.getText().toString();
					edtComment.setText("");

					new PostComment(content).execute();

					MyDialogFragment.this.dismiss();

					return true;
				}
			});

			return v;
		}

		@Override
		public void onDestroy() {
			// TODO Auto-generated method stub
			dialogFragment = null;
			super.onDestroy();
		}
	}

	/**
	 * Get more comment
	 */
	public void loadMore() {

		if (mLoading || mPageEnd)
			return;

		mLoading = true;

		new GetComment().execute();
	}

	/**
	 * 	Post comment to server
	 */
	public void postComment(String content) {

		// valid comment
		content = content.trim();
		if (content.length() == 0)
			return;

		// post comment to server
		new PostComment(content).execute();
	}

	/**
	 * Parse json object containing comment get from server and append to a given list
	 */
	public List<Comment> parseJsonComment(JSONArray jCommentArr, List<Comment> commentList) throws JSONException {

		if (commentList == null)
			commentList = new ArrayList<Comment>();

		for (int i = 0; i < jCommentArr.length(); i++) {
			JSONObject jComment = jCommentArr.getJSONObject(i);

			commentList.add(0, new Comment(
					jComment.getString("user"),
					jComment.getString("comment"),
					jComment.getString("avatar"),
					jComment.getString("time")));
		}

		return commentList;
	}

	public void addComment(Comment newComment) {

		// add to comment list
		mAdapter.mItemList.add(newComment);
		mAdapter.notifyDataSetChanged();

		// notify list view
		mLst.smoothScrollToPosition(mLst.getCount());

		if (dialogFragment != null)
			dialogFragment.lstComment.smoothScrollToPosition(mLst.getCount() - 1);
	}

	/**
	 * AsyncTask to get more comment
	 */
	public class GetComment extends AsyncTask<Void, Void, Boolean> {

		private int beforeSize;

		@Override
		protected Boolean doInBackground(Void... params) {

			try {
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("shop_id", Integer.toString(mShop.mID)));
				pairs.add(new BasicNameValuePair("page", Integer.toString(mPage)));

				String json = NetworkManger.post(APILinkMaker.mGetCommentGet(), pairs);

				JSONArray jCommentArr = new JSONArray(json);
				beforeSize = mAdapter.mItemList.size();

				parseJsonComment(jCommentArr, mAdapter.mItemList);

				if (beforeSize == mAdapter.mItemList.size()) {
					mPageEnd = true;
					return false;
				}
				else
					mPage++;

			} catch (JSONException e) {
				e.printStackTrace();
				mPageEnd = true;
				return false;
			}

			return true;
		}

		protected void onPostExecute(Boolean k) {

			mAdapter.notifyDataSetChanged();
			mLoading = false;
			int i = mAdapter.mItemList.size() - beforeSize;
			mLst.setSelectionFromTop(i, 0);
			if (dialogFragment != null) {
				dialogFragment.lstComment.setSelectionFromTop(i, 0);
			}
		}
		protected void onPreExecute(){ }
	}

	/**
	 * AsyncTask to post comment
	 */
	public class PostComment extends AsyncTask<Void, Void, Boolean> {

		private String mContent;

		public PostComment(String content) {
			mContent = content;
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			try {
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("user_id", GlobalVariable.userID));
				pairs.add(new BasicNameValuePair("shop_id", Integer.toString(mShop.mID)));
				pairs.add(new BasicNameValuePair("content", mContent));

				String json = NetworkManger.post(APILinkMaker.mGetCommentPost(), pairs).trim();

				return true;

			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

		protected void onPostExecute(Boolean k) { 
			mLoading = false;

			if (k) {
				String timeStamp = new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime());
				String username = "Anomynous User";
				String url = "";

				if (GlobalVariable.avatarFace.compareTo("null") != 0){
					username = GlobalVariable.nameFace;
					url = GlobalVariable.avatarFace;
				}

				addComment(new Comment(username, mContent, url, timeStamp));
			}
		}

		protected void onPreExecute() { }
	}
}