package vn.smartguide;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

/**
 * Created by cycrixlaptop on 7/29/13.
 */
public class DetailMenuFragment extends Fragment {
	
	// Constants
	public static final int BUTTON_PROMOTION 	= -1;
	public static final int BUTTON_INFO 		= 0;
	public static final int BUTTON_SHOP_MENU 	= 1;
	public static final int BUTTON_SHOP_PHOTO 	= 2;
	public static final int BUTTON_SHOP_COMMENT = 3;
	public static final int BUTTON_SHOW_MAP 	= 4;
	
	private static final int[] mXindexArr = new int[] {
		R.id.btnShopInfo,
		R.id.btnShopMenu,
		R.id.btnShopPhoto,
		R.id.btnShopComment,
		R.id.btnShopMap,
	};
    
    private static final int[] ICON_BUTTON = new int[] {
    	R.drawable.icon_shopinfo,
    	R.drawable.icon_shopmenu,
    	R.drawable.icon_shopphoto,
    	R.drawable.icon_shopcomment,
    	R.drawable.icon_shopmap,
    };
    
    private static final int[] ICON_BUTTON_HL = new int[] {
    	R.drawable.icon_shopinfo_hi,
    	R.drawable.icon_shopmenu_hi,
    	R.drawable.icon_shopphoto_hi,
    	R.drawable.icon_shopcomment_hi,
    	R.drawable.icon_shopmap_hi,
    };
	
	// GUI element
	private Button btnPromotion;	// Left
	private Button btnShop;
	private Button btnPromotionOverlay;
	private ImageView imgDivider;
	private ImageView imgPicker;
	
	private ImageView imgPickerBg; 	// Right
	private ImageButton btnShopInfo;
	private ImageButton btnShopMenu;
	private ImageButton btnShopPhoto;
	private ImageButton btnShopComment;
	private ImageButton btnShopMap;

	// GUI size
	private int 	mRootWidth;
	
	private int 	mBtnShopWidth;
	private int 	mBtnPromotionWidth;
	private int 	mDividerWidth;
	private int		mPickerWidth;
	
	private int		mPickerBgWidth;
	private int		mBtnShopInfoWidth;
	private int		mBtnShopMenuWidth;
	private int		mBtnShopPhotoWidth;
	private int		mBtnShopCommentWidth;
	private int		mBtnShopMapWidth;
	
	// Data
	private boolean 	mFirstTimeClick = true;
	private Listener 	mListener 		= new Listener();
    public  boolean 	mShowInfoBar 	= false;
    private int			mButtonIndex	= BUTTON_INFO;
    
    // Others
    private AnimatorSet animatorSet;

    ///////////////////////////////////////////////////////////////////////////
    // Overrided methods
    ///////////////////////////////////////////////////////////////////////////
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.detail_menu_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        
        // Get GUI elements
        btnPromotion 		= (Button) getView().findViewById(R.id.btnPromotion);
        btnPromotionOverlay	= (Button) getView().findViewById(R.id.btnPromotionOverlay);
        btnShop		 		= (Button) getView().findViewById(R.id.btnShop);
        imgDivider 			= (ImageView) getView().findViewById(R.id.imgDivider);
        imgPicker 			= (ImageView) getView().findViewById(R.id.imgPick);
        
        imgPickerBg			= (ImageView) getView().findViewById(R.id.imgPickerBg);
        btnShopInfo			= (ImageButton) getView().findViewById(R.id.btnShopInfo);
        btnShopMenu			= (ImageButton) getView().findViewById(R.id.btnShopMenu);
        btnShopPhoto		= (ImageButton) getView().findViewById(R.id.btnShopPhoto);
        btnShopComment		= (ImageButton) getView().findViewById(R.id.btnShopComment);
        btnShopMap			= (ImageButton) getView().findViewById(R.id.btnShopMap);
        
        // Set GUI event
        btnPromotionOverlay.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {

				toggleShopInfo(false, true);
			}
		});
        
        btnShop.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {

				toggleShopInfo(false, true);
			}
		});
        
        for (int i = 0; i < 5; i++) {
        	ImageButton btn = (ImageButton) getView().findViewById(mXindexArr[i]);
        	final int index = i;
        	btn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					
					switchToButton(index, false, true);
				}
			});
        }

        // Set layout change event to put the picker at position
        getView().addOnLayoutChangeListener(new OnLayoutChangeListener() {

			@Override
			public void onLayoutChange(View v, int left, int top, int right, int bottom,
					int oldLeft, int oldTop, int oldRight, int oldBottom) {

				mRootWidth = right - left; 
				getGuiElementSize();
				
				btnPromotion.setTranslationX((mRootWidth - mBtnShopWidth - mDividerWidth - mBtnPromotionWidth) / 2);
				imgDivider.setTranslationX(mRootWidth - mBtnShopWidth - mDividerWidth);
				btnShop.setTranslationX(mRootWidth - mBtnShopWidth);
				imgPicker.setTranslationX((mRootWidth - mBtnShopWidth - mDividerWidth - mPickerWidth) / 2);
				
				btnPromotionOverlay.setWidth(mRootWidth - mBtnShopWidth - mDividerWidth);
				
				getView().removeOnLayoutChangeListener(this);
			}
		});
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Public methods
    ///////////////////////////////////////////////////////////////////////////
    
    public void setListener(Listener listener) {
    	
    	if (listener == null)
    		listener = new Listener();
    	mListener = listener;
    }
    
    public void switchToButton(int buttonIndex, boolean animate, boolean invokeListener) {
    	
    	if (!mShowInfoBar)
    		return;
    	
    	mButtonIndex = buttonIndex;
    	int width5Button 	= mRootWidth - mBtnPromotionWidth - mDividerWidth;
    	float posPicker		= mBtnPromotionWidth + mDividerWidth + (width5Button / 5 - mPickerWidth) / 2 + width5Button * mButtonIndex / 5;
		float posPickerBg	= mPickerBgWidth - width5Button + (width5Button / 5 - mPickerBgWidth) / 2 + width5Button * mButtonIndex / 5;
    	
    	updateButtonHighLight(buttonIndex);
    	if (animate) {
    		ObjectAnimator pickerAnimator = ObjectAnimator.ofFloat(imgPicker, "translationX", posPicker);
    		ObjectAnimator pickerBgAnimator = ObjectAnimator.ofFloat(imgPickerBg, "translationX", posPickerBg);
			AnimatorSet animatorSet2 = new AnimatorSet();
			animatorSet2.playTogether(pickerAnimator, pickerBgAnimator);
			animatorSet2.setInterpolator(new AccelerateDecelerateInterpolator());
			animatorSet2.start();
    	} else {
    		imgPicker.setTranslationX(posPicker);
    		imgPickerBg.setTranslationX(posPickerBg);
    	}
    	
    	if (invokeListener)
    		mListener.onButtonClick(buttonIndex);
    }
    
    public void toggleShopInfo(boolean animate, boolean invokeListener) {
    	
    	mShowInfoBar = !mShowInfoBar;
    	getGuiElementSize();
    	
    	if (animatorSet != null)
    		animatorSet.cancel();
    	
    	float posPromotion 	= 0;
		float posShop		= 0;
		float posDivider	= 0;
		
		float posPicker		= 0;
		float posPickerBg	= 0;
		float posShopInfo	= 0;
		float posShopMenu	= 0;
		float posShopPhoto	= 0;
		float posShopComment= 0;
		float posShopMap	= 0;
		
		if (mShowInfoBar) {
			
			// Calculate position
			int width5Button = mRootWidth - mBtnPromotionWidth - mDividerWidth;

			posPromotion 	= 0;
			posDivider		= mBtnPromotionWidth;
			posShop			= mBtnPromotionWidth + mPickerWidth + (width5Button - mBtnShopWidth) / 2;
			
			posShopInfo		= mBtnShopInfoWidth - width5Button + (width5Button / 5 - mBtnShopInfoWidth) / 2;
			posShopMenu		= mBtnShopMenuWidth - width5Button + (width5Button / 5 - mBtnShopMenuWidth) / 2 + width5Button / 5;
			posShopPhoto	= mBtnShopPhotoWidth - width5Button + (width5Button / 5 - mBtnShopPhotoWidth) / 2 + width5Button * 2 / 5;
			posShopComment	= mBtnShopCommentWidth - width5Button + (width5Button / 5 - mBtnShopCommentWidth) / 2 + width5Button * 3 / 5;
			posShopMap		= mBtnShopMapWidth - width5Button + (width5Button / 5 - mBtnShopMapWidth) / 2 + width5Button * 4 / 5;
			
			posPicker		= mBtnPromotionWidth + mDividerWidth + (width5Button / 5 - mPickerWidth) / 2 + width5Button * mButtonIndex / 5;
			posPickerBg		= mPickerBgWidth - width5Button + (width5Button / 5 - mPickerBgWidth) / 2 + width5Button * mButtonIndex / 5;
			
			// Set visibility
    		btnShopInfo.setVisibility(View.VISIBLE);
    		btnShopMenu.setVisibility(View.VISIBLE);
    		btnShopPhoto.setVisibility(View.VISIBLE);
    		btnShopComment.setVisibility(View.VISIBLE);
    		btnShopMap.setVisibility(View.VISIBLE);
    		imgPickerBg.setVisibility(View.VISIBLE);
    		btnPromotionOverlay.setWidth(mBtnPromotionWidth);
		} else {
			
			// Calculate position
			posPromotion 	= (mRootWidth - mBtnShopWidth - mDividerWidth - mBtnPromotionWidth) / 2;
			posDivider		= mRootWidth - mBtnShopWidth - mDividerWidth;
			posShop			= mRootWidth - mBtnShopWidth;
			
			posShopInfo		= 0;
			posShopMenu		= 0;
			posShopPhoto	= 0;
			posShopComment	= 0;
			posShopMap		= 0;
			
			posPicker		= (mRootWidth - mBtnShopWidth - mDividerWidth - mPickerWidth) / 2;
			posPickerBg		= 0;
			
			// Set visibility
			btnShop.setVisibility(View.VISIBLE);
		}
    	
    	if (animate) {
    	
    		// Do animation
    		animatorSet = new AnimatorSet();
    		
    		List<Animator> animatorList = new ArrayList<Animator>();
    		if (mShowInfoBar) {
    			
    			// Show Info bar
    			animatorSet.addListener(new AnimatorListener() {
					
					public void onAnimationStart(Animator animation) { }
					public void onAnimationRepeat(Animator animation) { }
					
					public void onAnimationEnd(Animator animation) {
						btnShop.setVisibility(View.INVISIBLE);
					}
					
					public void onAnimationCancel(Animator animation) {
						onAnimationEnd(animation);
					}
				});    			
    		} else {
    			
    			// Show shop bar
    			animatorSet.addListener(new AnimatorListener() {
					
					public void onAnimationStart(Animator animation) { }
					public void onAnimationRepeat(Animator animation) { }
					
					public void onAnimationEnd(Animator animation) {
						btnShopInfo.setVisibility(View.INVISIBLE);
		        		btnShopMenu.setVisibility(View.INVISIBLE);
		        		btnShopPhoto.setVisibility(View.INVISIBLE);
		        		btnShopComment.setVisibility(View.INVISIBLE);
		        		btnShopMap.setVisibility(View.INVISIBLE);
		        		imgPickerBg.setVisibility(View.INVISIBLE);
		        		btnPromotionOverlay.setWidth(mRootWidth - mBtnShopWidth - mDividerWidth);
					}
					
					public void onAnimationCancel(Animator animation) {
						onAnimationEnd(animation);
					}
				});
    		}
    		
    		// Set divider image
    		imgDivider.setImageResource(mShowInfoBar ? R.drawable.detail_menu_divider_right : R.drawable.detail_menu_divider_left);
    		
    		// Set up animator set
    		animatorList.add(ObjectAnimator.ofFloat(btnPromotion, "translationX", posPromotion));
    		animatorList.add(ObjectAnimator.ofFloat(imgDivider, "translationX", posDivider));
    		animatorList.add(ObjectAnimator.ofFloat(btnShop, "translationX", posShop));
    		animatorList.add(ObjectAnimator.ofFloat(btnShopInfo, "translationX", posShopInfo));
    		animatorList.add(ObjectAnimator.ofFloat(btnShopMenu, "translationX", posShopMenu));
    		animatorList.add(ObjectAnimator.ofFloat(btnShopPhoto, "translationX", posShopPhoto));
    		animatorList.add(ObjectAnimator.ofFloat(btnShopComment, "translationX", posShopComment));
    		animatorList.add(ObjectAnimator.ofFloat(btnShopMap, "translationX", posShopMap));
    		animatorList.add(ObjectAnimator.ofFloat(imgPicker, "translationX", posPicker));
    		animatorList.add(ObjectAnimator.ofFloat(imgPickerBg, "translationX", posPickerBg));
    		
    		float alphaStart = mShowInfoBar ? 0.0f : 1.0f;
    		float alphaEnd = mShowInfoBar ? 1.0f : 0.0f;
    		animatorList.add(ObjectAnimator.ofFloat(btnShop, "alpha", alphaEnd, alphaStart));
			animatorList.add(ObjectAnimator.ofFloat(btnShopInfo, "alpha", alphaStart, alphaEnd));
			animatorList.add(ObjectAnimator.ofFloat(btnShopMenu, "alpha", alphaStart, alphaEnd));
			animatorList.add(ObjectAnimator.ofFloat(btnShopPhoto, "alpha", alphaStart, alphaEnd));
			animatorList.add(ObjectAnimator.ofFloat(btnShopComment, "alpha", alphaStart, alphaEnd));
			animatorList.add(ObjectAnimator.ofFloat(btnShopMap, "alpha", alphaStart, alphaEnd));
			animatorList.add(ObjectAnimator.ofFloat(imgPickerBg, "alpha", alphaStart, alphaEnd));
    		
    		animatorSet.playTogether(animatorList);
    		animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
    		animatorSet.start();    		
    	} else {

    		// Change GUI instantly
    		btnPromotion.setTranslationX(posPromotion);
    		imgDivider.setTranslationX(posDivider);
    		btnShop.setTranslationX(posShop);
    		btnShopInfo.setTranslationX(posShopInfo);
    		btnShopMenu.setTranslationX(posShopMenu);
    		btnShopPhoto.setTranslationX(posShopPhoto);
    		btnShopComment.setTranslationX(posShopComment);
    		btnShopMap.setTranslationX(posShopMap);
    		imgPicker.setTranslationX(posPicker);
    		imgPickerBg.setTranslationX(posPickerBg);
    		
    		// Set divider image
    		imgDivider.setImageResource(mShowInfoBar ? R.drawable.detail_menu_divider_right : R.drawable.detail_menu_divider_left);
    		
    		if (mShowInfoBar) {
        		btnShop.setVisibility(View.INVISIBLE);
    		} else {
    			btnShopInfo.setVisibility(View.INVISIBLE);
        		btnShopMenu.setVisibility(View.INVISIBLE);
        		btnShopPhoto.setVisibility(View.INVISIBLE);
        		btnShopComment.setVisibility(View.INVISIBLE);
        		btnShopMap.setVisibility(View.INVISIBLE);
        		imgPickerBg.setVisibility(View.INVISIBLE);
        		btnPromotionOverlay.setWidth(mRootWidth - mBtnShopWidth - mDividerWidth);
    		}
    	}
    	
    	if (invokeListener)
    		if (mShowInfoBar)
    			mListener.onButtonClick(mButtonIndex);
    		else
    			mListener.onButtonClick(BUTTON_PROMOTION);
    }
    
	///////////////////////////////////////////////////////////////////////////
	// Private methods
	///////////////////////////////////////////////////////////////////////////
    
    private void getGuiElementSize() {
    	
    	if (!mFirstTimeClick)
    		return;
    	
    	mBtnPromotionWidth 	= btnPromotion.getWidth();
		mBtnShopWidth 		= btnShop.getWidth();
		mDividerWidth 		= imgDivider.getWidth();
		mPickerWidth 		= imgPicker.getWidth();
		
		mPickerBgWidth		= imgPickerBg.getWidth();
		mBtnShopInfoWidth 	= btnShopInfo.getWidth();
		mBtnShopMenuWidth	= btnShopMenu.getWidth();
		mBtnShopPhotoWidth 	= btnShopPhoto.getWidth();
		mBtnShopCommentWidth= btnShopComment.getWidth();
		mBtnShopMapWidth 	= btnShopMap.getWidth();
    }

    private void updateButtonHighLight(int index) {
    	
    	for (int i = 0; i < mXindexArr.length; i++) {
    		
    		ImageButton btn = (ImageButton) getView().findViewById(mXindexArr[i]);
    		if (i == index) {
    			 btn.setImageResource(ICON_BUTTON_HL[i]);
    		} else {
    			btn.setImageResource(ICON_BUTTON[i]);
    		}
    	}
    }

    ///////////////////////////////////////////////////////////////////////////
    // Listener
    ///////////////////////////////////////////////////////////////////////////
    
    public static class Listener{
    	
    	public void onButtonClick(int buttonIndex) {}
    }
}