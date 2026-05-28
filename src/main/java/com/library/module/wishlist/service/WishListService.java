package com.library.module.wishlist.service;

import java.util.List;

import com.library.module.wishlist.entity.WishList;

public interface WishListService {

    WishList saveWishList(Integer bookId, Integer userId);

    List<WishList> getWishListsByUser(Integer userId);

    Integer getCountWishList(Integer userId);

    Boolean deleteWishList(Integer wishlistId, Integer userId);
}
