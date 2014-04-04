package vn.infory.infory.home;

import vn.infory.infory.data.home.HomeItem;
import android.view.View;

public abstract class HomeItemUpdater {
	abstract public void update(View view, HomeItem item, HomeFragment caller);
}
