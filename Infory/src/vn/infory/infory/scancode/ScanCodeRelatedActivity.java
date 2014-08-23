package vn.infory.infory.scancode;

import java.util.ArrayList;

import vn.infory.infory.R;
import android.app.ActionBar;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class ScanCodeRelatedActivity extends FragmentActivity {

	ScanCodeRelatedPagerAdapter mScanCodeRelatedPagerAdapter;
	
	ViewPager mViewPager;
	
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_code_related);

        // Create an adapter that when requested, will return a fragment representing an object in
        // the collection.
        // 
        // ViewPager and its adapters use support library fragments, so we must use
        // getSupportFragmentManager.
        mScanCodeRelatedPagerAdapter = new ScanCodeRelatedPagerAdapter(getSupportFragmentManager());

        
    }

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
		/*// Set up action bar.
        final ActionBar actionBar = getActionBar();
		// Specify that the Home button should show an "Up" caret, indicating that touching the
        // button will take the user one step up in the application's hierarchy.
        actionBar.setDisplayHomeAsUpEnabled(true);*/

        // Set up the ViewPager, attaching the adapter.
        mViewPager = (ViewPager) findViewById(R.id.testpager);
        mViewPager.setAdapter(mScanCodeRelatedPagerAdapter);
	}





	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*switch (item.getItemId()) {
            case android.R.id.home:
                // This is called when the Home (Up) button is pressed in the action bar.
                // Create a simple intent that starts the hierarchical parent activity and
                // use NavUtils in the Support Package to ensure proper handling of Up.
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // This activity is not part of the application's task, so create a new task
                    // with a synthesized back stack.
                    TaskStackBuilder.from(this)
                            // If there are ancestor activities, they should be added here.
                            .addNextIntent(upIntent)
                            .startActivities();
                    finish();
                } else {
                    // This activity is part of the application's task, so simply
                    // navigate up to the hierarchical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
        }*/
        return super.onOptionsItemSelected(item);
    }
	
	/**
     * A {@link android.support.v4.app.FragmentStatePagerAdapter} that returns a fragment
     * representing an object in the collection.
     */
    public static class ScanCodeRelatedPagerAdapter extends FragmentStatePagerAdapter {

        public ScanCodeRelatedPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new ScanCodeRelatedFragment();
            Bundle args = new Bundle();
            if(i == 1)
            	args.putInt(ScanCodeRelatedFragment.ARG_OBJECT, 1000); // Our object is just an integer :-P
        	else
            	args.putInt(ScanCodeRelatedFragment.ARG_OBJECT, i + 1); // Our object is just an integer :-P
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            // For this contrived example, we have a 100-object collection.
            return 100;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "OBJECT " + (position + 1);
        }
    }
    
    /**
     * A dummy fragment representing a section of the app, but that simply displays dummy text.
     */
    public static class ScanCodeRelatedFragment extends Fragment {

        public static final String ARG_OBJECT = "object";
        
        ListView list;
    	ScanCodeRelatedListViewAdapter adapter;
    	public ScanCodeRelatedActivity CustomListView = null;
    	public ArrayList<ListModelRelatedShops> CustomListViewValuesArr = new ArrayList<ListModelRelatedShops>();
    	
    	public void setListData()
        {
             
            for (int i = 0; i < 11; i++) {
                 
                final ListModelRelatedShops sched = new ListModelRelatedShops();
                     
                  /******* Firstly take data in model object ******/
                   sched.setName("Company "+i);
                   sched.setDescription("image"+i);
                    
                /******** Take Model Object in ArrayList **********/
                CustomListViewValuesArr.add( sched );
            }             
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.scan_code_related_fragment, container, false);
            Bundle args = getArguments();
            
            if(args.getInt(ARG_OBJECT) == 1){
            	CustomListView = (ScanCodeRelatedActivity) getActivity();
                setListData();
                
                Resources res =getResources();
//                list = ( ListView )rootView.findViewById( R.id.lstRelated );  // List defined in XML ( See Below )
                list = new ListView(getActivity());
                 
                /**************** Create Custom Adapter *********/
                adapter=new ScanCodeRelatedListViewAdapter( CustomListView, CustomListViewValuesArr,res,0 );
                list.setAdapter( adapter );
            }
            else{
            	((TextView) rootView.findViewById(android.R.id.text1)).setText(
                        Integer.toString(args.getInt(ARG_OBJECT)));
            }
            
            return rootView;
        }
    }
}
