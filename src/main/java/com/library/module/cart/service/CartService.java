package com.library.module.cart.service;

import java.util.List;
import com.library.module.cart.entity.CartItem;

public interface CartService {

    Integer getCountCart(Integer userId);

    void updateQuantity(String sy, Integer itemId, Integer userId);

    void clearCartByUser(Integer userId);

    void addToCart(Integer userId, Integer bookId, Integer quantity);

    List<CartItem> getCartItems(Integer userId);

    void removeItem(Integer itemId, Integer userId);

    CartItem getItemById(Integer id, Integer userId);
}