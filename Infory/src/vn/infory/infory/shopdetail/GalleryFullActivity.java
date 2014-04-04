package vn.infory.infory.shopdetail;

import java.util.List;

import vn.infory.infory.LazyLoadAdapter;
import vn.infory.infory.R;
import vn.infory.infory.data.PhotoGallery;
import vn.infory.infory.network.CyAsyncTask;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.ViewById;

public class GalleryFullActivity extends FragmentActivity {
	
	private static List<PhotoGallery> sItemList;
	private static CyAsyncTask sLoader;
	private static int sIndex;
	
	// GUI
	@ViewById(id = R.id.pager)	private ViewPager mPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gallery_full);
		
		try {
			AndroidAnnotationParser.parse(this, findViewById(android.R.id.content));
		} catch (Exception e) {
			e.printStackTrace();
			finish();
			return;
		}
		
		GalleryFullAdapter shopGalleryadapter = new GalleryFullAdapter(this, sItemList, sLoader, false);
		shopGalleryadapter.setPage(sItemList.size() / LazyLoadAdapter.ITEM_PER_PAGE);
		if (sItemList.size() % LazyLoadAdapter.ITEM_PER_PAGE != 0
				|| sItemList.size() == 0)
			shopGalleryadapter.mIsMore = false;
		mPager.setAdapter(shopGalleryadapter);
		mPager.setOnPageChangeListener(shopGalleryadapter);
		if (sIndex > 0)
			mPager.setCurrentItem(sIndex);
		
		sItemList = null;
		sLoader = null;
	}
	
	public static void newInstance(Activity act, List<PhotoGallery> itemList, 
			CyAsyncTask loader, int index) {
		sItemList = itemList;
		sLoader = loader;
		sIndex = index;
		
		Intent intent = new Intent(act, GalleryFullActivity.class);
		act.startActivity(intent);
	}
}
