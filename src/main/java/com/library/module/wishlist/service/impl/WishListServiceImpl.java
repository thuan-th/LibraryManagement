package com.library.module.wishlist.service.impl;

import com.library.module.book.entity.Book;
import com.library.module.book.repository.BookRepository;
import com.library.module.user.entity.User;
import com.library.module.user.repository.UserRepository;
import com.library.module.wishlist.entity.WishList;
import com.library.module.wishlist.repository.WishListRepository;
import com.library.module.wishlist.service.WishListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WishListServiceImpl implements WishListService {

    @Autowired
    private WishListRepository wishListRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Override
    public WishList saveWishList(Integer bookId, Integer userId) {
        WishList existingWishList = wishListRepository.findByBookIdAndUserId(bookId, userId);

        if (existingWishList != null) {
            return existingWishList;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sách"));

        WishList wishList = new WishList();
        wishList.setUser(user);
        wishList.setBook(book);

        return wishListRepository.save(wishList);
    }

    @Override
    public List<WishList> getWishListsByUser(Integer userId) {
        return wishListRepository.findByUserId(userId);
    }

    @Override
    public Integer getCountWishList(Integer userId) {
        return wishListRepository.countByUserId(userId);
    }

    @Override
    public Boolean deleteWishList(Integer wishlistId, Integer userId) {
        WishList wishList = wishListRepository.findByIdAndUserId(wishlistId, userId)
                .orElse(null);

        if (wishList == null) {
            return false;
        }

        wishListRepository.delete(wishList);
        return true;
    }

}