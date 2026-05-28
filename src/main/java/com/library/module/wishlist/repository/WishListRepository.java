package com.library.module.wishlist.repository;

import java.util.List;
import java.util.Optional;

import com.library.module.wishlist.entity.WishList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishListRepository extends JpaRepository<WishList, Integer> {

    WishList findByBookIdAndUserId(Integer bookId, Integer userId);

    Optional<WishList> findByIdAndUserId(Integer id, Integer userId);

    Integer countByUserId(Integer userId);

    List<WishList> findByUserId(Integer userId);

    void deleteByUserId(Integer userId);
}
