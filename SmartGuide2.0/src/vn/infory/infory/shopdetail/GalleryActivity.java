package vn.infory.infory.shopdetail;

import java.util.ArrayList;
import java.util.List;

import vn.infory.infory.LazyLoadAdapter;
import vn.infory.infory.R;
import vn.infory.infory.data.PhotoGallery;
import vn.infory.infory.network.CyAsyncTask;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;

public class GalleryActivity extends Activity {
	
	private static CyAsyncTask sLoader;
	private static List<PhotoGallery> sItemList;
	private static int sIndex;
	
	// GUI
	@ViewById(id = R.id.grid)		private GridView mGrid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gallery);
		
		try {
			AndroidAnnotationParser.parse(this, findViewById(android.R.id.content));
		} catch (Exception e) {
			e.printStackTrace();
			finish();
			return;
		}
		
		GalleryAdapter adapter = new GalleryAdapter(this, sLoader, 
				(ArrayList) sItemList);
		adapter.setPage(sItemList.size() / LazyLoadAdapter.ITEM_PER_PAGE);
		if (sItemList.size() % LazyLoadAdapter.ITEM_PER_PAGE != 0 ||
				sItemList.size() == 0)
			adapter.mIsMore = false;
		
		mGrid.setAdapter(adapter);
		mGrid.setOnScrollListener(adapter);
		
		if (sIndex >= 0)
			GalleryFullActivity.newInstance(this, sItemList, sLoader.clone(), sIndex);
		
		sLoader = null;
		sItemList = null;
	}
	
	public static void newInstance(Activity act, CyAsyncTask loader, List<PhotoGallery> itemList, int index) {
		sLoader = loader;
		sItemList = itemList;
		sIndex = index;
		
		Intent intent = new Intent(act, GalleryActivity.class);
		act.startActivity(intent);
	}
	
	@Click(id = R.id.imgBack)
	private void onBackClick(View v) {
		finish();
	}
}
