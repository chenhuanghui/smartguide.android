package vn.infory.infory.home;

import java.util.List;

import vn.infory.infory.data.PlaceList;
import vn.infory.infory.data.home.HomeItem_ShopItem;
import vn.infory.infory.data.home.PromoItem;

public interface HomeListener {
	public void onBranchPromoInfoClick(String shopId);
	public void onImageClick(List<String> urlList, int index);
	public void onPlaceListClick(int placeListId, PlaceList placeList);
	public void onShopItemClick(int shopId, HomeItem_ShopItem shopItem);
	public void onShopItemClick(int shopId, PromoItem shopItem);
	public void onStoreItemClick(String storeId);
}
