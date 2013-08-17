package vn.smartguide;

import java.util.List;



import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class PhotoActivity extends FragmentActivity{

	private DetailShopPhotoFragment mParentFragment;
	private PhotoPagerAdapter mAdapter;
	private boolean mIsUser;
	private List<ImageStr> mImageList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photo_dialog);

		mParentFragment = DetailShopPhotoFragment.thiz;
		mIsUser = getIntent().getBooleanExtra("isUser", true);

		if (mIsUser)
			mImageList = mParentFragment.mUserURLList;
		else
			mImageList = mParentFragment.mShopURLList;

		ViewPager pager = (ViewPager) findViewById(R.id.pagerPhotoFull);
		try{
		mAdapter = new PhotoPagerAdapter(getSupportFragmentManager());
		}catch(Exception ex){
			ex.getMessage();
		}
		pager.setAdapter(mAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.photo, menu);
		return true;
	}

	public void loadMore() {

		mParentFragment.loadMoreUser(this);
	}

	public void loadImage() {

		mParentFragment.loadImage(mImageList, mIsUser);
	}

	public void refresh() {
		mAdapter.notifyDataSetChanged();
	}

	public class PhotoPagerAdapter extends FragmentStatePagerAdapter {

		private PhotoFullFragment[] fragArr = new PhotoFullFragment[getCount()];

		public PhotoPagerAdapter(FragmentManager fm) {
			super(fm);
			for (int i = 0; i < fragArr.length; i++)
				fragArr[i] = new PhotoFullFragment(mImageList.get(i));
		}

		@Override
		public int getCount() {
			return mImageList.size();
		}

		@Override
		public Fragment getItem(int position) {

			if (mIsUser && (getCount() - position >= 5)) {
				loadMore();
			}
			
			PhotoFullFragment f = fragArr[position % fragArr.length];
			f.mImageItem = mImageList.get(position);
			f.refresh(f.getView());
			return f;
		}

		@Override
		public void notifyDataSetChanged() {

			super.notifyDataSetChanged();
			for (int i = 0; i < fragArr.length; i++)
				fragArr[i].refresh(fragArr[i].getView());
		}
	}

	@SuppressLint("ValidFragment")
	public class PhotoFullFragment extends Fragment {

		ImageStr mImageItem;

		public PhotoFullFragment(ImageStr imageItem) {
			mImageItem = imageItem;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			View v = inflater.inflate(R.layout.photo_full_item, container, false);

			refresh(v);

			return v;
		}

		public void refresh(View v) {
			
			if (v == null)
				return;

			ImageView img = (ImageView) v.findViewById(R.id.imgFullPhoto);
			ProgressBar prgWait = (ProgressBar) v.findViewById(R.id.prgWait);
			TextView txtTitle = (TextView) v.findViewById(R.id.txtTit);
			TextView txtDsc = (TextView) v.findViewById(R.id.txtDesc);

			if (mImageItem.bm != null) {
				img.setImageBitmap(mImageItem.bm);
				prgWait.setVisibility(View.INVISIBLE);
			} else if (!mImageItem.loading) {
				loadImage();
				prgWait.setVisibility(View.VISIBLE);
			}

			if (!mIsUser) {
				txtTitle.setVisibility(View.INVISIBLE);
				txtDsc.setVisibility(View.INVISIBLE);
			} else {
				txtDsc.setText(mImageItem.description);
			}
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {

			super.onActivityCreated(savedInstanceState);
		}
	}
}