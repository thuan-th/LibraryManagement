package com.library.module.cart.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.library.module.cart.entity.Cart;

public interface CartRepository extends JpaRepository<Cart, Integer> {

    public Integer countByUserId(Integer userId);

    Optional<Cart> findByUserId(Integer userId);

    void deleteByUserId(Integer userId);
}
