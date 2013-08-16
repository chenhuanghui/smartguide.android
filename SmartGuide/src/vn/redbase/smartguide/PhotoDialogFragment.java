package vn.redbase.smartguide;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PhotoDialogFragment extends DialogFragment {

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
        View v = inflater.inflate(R.layout.photo_dialog, container, false);
        
//        ViewPager pager = (ViewPager) v.findViewById(R.id.pagerPhotoFull);
//        pager.setAdapter(new PhotoPagerAdapter(getFragmentManager()));
        
        return v;
    }
    
    
}
