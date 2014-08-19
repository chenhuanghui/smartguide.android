package vn.infory.infory.scancode;

import java.util.ArrayList;

import vn.infory.infory.R;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;


public class SampleListFragment extends ScrollTabHolderFragment implements OnScrollListener {

	private static final String ARG_POSITION = "position";

	private ListView mListView;
	private ArrayList<String> mListItems;

	private int mPosition;
	private Activity mAct;

	public static Fragment newInstance(Activity act, int position) {
		SampleListFragment f = new SampleListFragment();
		f.mAct = act;
		Bundle b = new Bundle();
		b.putInt(ARG_POSITION, position);
		f.setArguments(b);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPosition = getArguments().getInt(ARG_POSITION);

		mListItems = new ArrayList<String>();

		for (int i = 1; i <= 100; i++) {
			mListItems.add(i + ". item - currnet page: " + (mPosition + 1));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.scan_code_fragment_list, null);

		mListView = (ListView) v.findViewById(R.id.listView);

		LinearLayout ll = (LinearLayout)mAct.findViewById(R.id.linearLayoutScanDLG2);
		
		
		
		View placeHolderView = inflater.inflate(R.layout.scan_code_view_header_placeholder, mListView, false);
		FrameLayout fr = (FrameLayout)placeHolderView.findViewById(R.id.frViewHeader);
		Log.i("Height", ll.getHeight()+"");
		fr.setPadding(0, ll.getHeight(), 0, 0);
		mListView.addHeaderView(placeHolderView);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mListView.setOnScrollListener(this);
		mListView.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.scan_code_list_item, android.R.id.text1, mListItems));
	}

	@Override
	public void adjustScroll(int scrollHeight) {
		if (scrollHeight == 0 && mListView.getFirstVisiblePosition() >= 1) {
			return;
		}

		mListView.setSelectionFromTop(1, scrollHeight);

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (mScrollTabHolder != null)
			mScrollTabHolder.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount, mPosition);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// nothing
	}

}
