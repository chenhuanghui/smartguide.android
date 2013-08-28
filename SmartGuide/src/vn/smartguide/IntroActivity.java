package vn.smartguide;

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
import android.widget.ProgressBar;
import android.widget.TextView;

public class IntroActivity extends FragmentActivity {
	
	private static final int[] INTRO_RESOURCE_ARR = new int[] {
		R.drawable.intro_1,
		R.drawable.intro_2,
		R.drawable.intro_3,
		R.drawable.intro_4,
		R.drawable.intro_5,
		R.drawable.intro_6,
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_intro);
		
		((ViewPager) findViewById(R.id.pagerIntro)).setAdapter(new IntroPagerAdapter(getSupportFragmentManager())); 
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
