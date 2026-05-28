package com.library.module.order.repository;

import com.library.module.order.entity.BookOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookOrderItemRepository extends JpaRepository<BookOrderItem, Long> {

    List<BookOrderItem> findByBookOrderId(Integer orderId);

}
