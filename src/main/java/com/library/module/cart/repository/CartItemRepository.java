package com.library.module.cart.repository;

import com.library.module.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    Optional<CartItem> findByCartIdAndBookId(Integer cartId, Integer bookId);

    List<CartItem> findByCartId(Integer cartId);

    @Query("SELECT ci FROM CartItem ci JOIN FETCH ci.book WHERE ci.cart.user.id = :userId")
    List<CartItem> findByCartUserId(@Param("userId") Integer userId);

    void deleteByCartUserId(Integer cartId);

}
