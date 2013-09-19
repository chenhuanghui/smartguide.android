package vn.smartguide;

import vn.smartguide.IntroActivity.IntroPagerAdapter;
import vn.smartguide.IntroActivity.IntroPagerFragment;
import vn.smartguide.PhotoActivity.PhotoFullFragment;
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

public class TutorActivity extends FragmentActivity {

	private static final int[] INTRO_RESOURCE_ARR = new int[] {
		R.drawable.tutorial1,
		R.drawable.tutorial2,
		R.drawable.tutorial3,
		R.drawable.tutorial4,
		R.drawable.tutorial_end
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_intro);

		ViewPager pager = ((ViewPager) findViewById(R.id.pagerIntro));
		pager.setAdapter(new IntroPagerAdapter(getSupportFragmentManager())); 
		
		CirclePageIndicator titleIndicator = (CirclePageIndicator)findViewById(R.id.indicator);
		titleIndicator.setViewPager(pager);
	}

	public class IntroPagerAdapter extends FragmentStatePagerAdapter {

		private PhotoFullFragment[] fragArr = new PhotoFullFragment[getCount()];

		public IntroPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return INTRO_RESOURCE_ARR.length;
		}

		@Override
		public Fragment getItem(int position) {

			IntroPagerFragment f = new IntroPagerFragment();

			Bundle args = new Bundle();
			args.putInt("rid", INTRO_RESOURCE_ARR[position]);
			f.setArguments(args);

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
	public class IntroPagerFragment extends Fragment {

		ImageStr mImageItem;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			super.onCreateView(inflater, container, savedInstanceState);

			View v = inflater.inflate(R.layout.intro_item, container, false);
			int rid = getArguments().getInt("rid");
			((ImageView) v.findViewById(R.id.imgIntro)).setImageResource(rid);

			return v;
		}
	}

}
