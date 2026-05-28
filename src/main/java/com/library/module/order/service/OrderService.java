package com.library.module.order.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.library.module.order.enums.OrderStatus;
import com.library.module.user.entity.User;
import org.springframework.data.domain.Page;
import com.library.module.order.dto.OrderRequest;
import com.library.module.order.entity.BookOrder;

public interface OrderService {

    public BookOrder saveOrder(User user, OrderRequest orderRequest) throws Exception;

    public List<BookOrder> getOrdersByUser(Integer userId);

    public BookOrder updateOrderStatus(Integer id, OrderStatus newStatus);

    public List<BookOrder> getAllOrders();

    public Map<Integer, Integer> getMonthlyRevenue(int year);

    public Map<Integer, Integer> getDailyRevenue(int year, int month);

    public Map<String, Long> getOrderStatistics(LocalDateTime startDate, LocalDateTime endDate);

    public BookOrder getOrdersByOrderId(String orderId);

    public Page<BookOrder> getAllOrdersPagination(Integer pageNo,Integer pageSize);

    public void handlePaymentSuccess(BookOrder order);

    public void handleCodOrder(BookOrder order);

    public void handlePaymentFail(BookOrder order);

    public BookOrder getOrderById(Integer id);

    public void cancelOrder(BookOrder order);

}
