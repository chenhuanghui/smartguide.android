package vn.smartguide;

/**
 * Created by ChauSang on 6/26/13.
 */
public interface MainAcitivyListener {
	public void goToPage(int index);
	public void goNextPage();
	public void goPreviousPage();
	public void startAds();
	public void stopAds();
	public ShopListFragment getShopListFragment();
	public ShopDetailFragment getDetailFragment();
	public void exit();
	public void setNaviText(String naviText);
	public void getAwardTypeOne(int award_id);
	public void getAwardTypeTwo(int award_id);
	public void userToDetail();
	public void setLocation(String cityName);
	//public void getShopList();
}
