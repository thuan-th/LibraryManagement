package com.library.module.order.service.impl;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

import com.library.module.book.repository.BookRepository;
import com.library.module.cart.entity.CartItem;
import com.library.module.cart.repository.CartItemRepository;
import com.library.module.order.enums.PaymentStatus;
import com.library.module.user.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.library.module.order.entity.OrderAddress;
import com.library.module.order.dto.OrderRequest;
import com.library.module.book.entity.Book;
import com.library.module.order.entity.BookOrder;
import com.library.module.order.entity.BookOrderItem;
import com.library.module.cart.repository.CartRepository;
import com.library.module.order.repository.BookOrderItemRepository;
import com.library.module.order.repository.BookOrderRepository;
import com.library.module.order.service.OrderService;
import com.library.common.service.AsyncMailService;
import com.library.util.CommonUtil;
import com.library.module.order.enums.OrderStatus;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private BookOrderRepository orderRepository;

    @Autowired
    private BookOrderItemRepository bookOrderItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private AsyncMailService asyncMailService;

    @Transactional
    @Override
    public BookOrder saveOrder(User user, OrderRequest orderRequest) throws Exception {

        List<CartItem> items = cartItemRepository.findByCartUserId(user.getId());

        if (items.isEmpty()) {
            throw new RuntimeException("Cart trống");
        }

        for (CartItem item : items) {
            if (item.getQuantity() > item.getBook().getStock()) {
                throw new RuntimeException("Sản phẩm " + item.getBook().getBookName() + " không đủ hàng");
            }
        }

        BookOrder order = new BookOrder();
        String orderId = "BLY-" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + "-"
                + (int) (Math.random() * 1000);
        order.setOrderId(orderId);
        order.setOrderDate(LocalDateTime.now());
        order.setUser(items.get(0).getCart().getUser());
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setStatus(OrderStatus.PENDING_PAYMENT.name());
        order.setPaymentType(orderRequest.getPaymentType());

        int totalAmount = items.stream()
                .mapToInt(item -> item.getBook().getDiscountPrice() * item.getQuantity())
                .sum() + 20000;
        order.setTotalAmount(totalAmount);

        OrderAddress address = new OrderAddress();
        address.setFirstName(orderRequest.getFirstName());
        address.setLastName(orderRequest.getLastName());
        address.setEmail(orderRequest.getEmail());
        address.setMobileNo(orderRequest.getMobileNo());
        address.setAddress(orderRequest.getAddress());
        address.setCity(orderRequest.getCity());
        address.setDistrict(orderRequest.getDistrict());
        address.setNote(orderRequest.getNote());
        order.setOrderAddress(address);

        List<BookOrderItem> orderItems = new ArrayList<>();

        for (CartItem item : items) {

            Integer currentPrice = item.getBook().getDiscountPrice();

            BookOrderItem orderItem = new BookOrderItem(
                    order,
                    item.getBook(),
                    item.getQuantity(),
                    currentPrice
            );

            orderItems.add(orderItem);
        }

        order.setItems(orderItems);

        BookOrder saved = orderRepository.save(order);

        return saved;
    }

    @Override
    public List<BookOrder> getOrdersByUser(Integer userId) {
        List<BookOrder> orders = orderRepository.findByUserIdOrderByOrderDateDesc(userId);
        return orders;
    }

    @Override
    @Transactional
    public BookOrder updateOrderStatus(Integer id, OrderStatus newStatus) {
        BookOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        OrderStatus currentStatus = OrderStatus.valueOf(order.getStatus());

        if (currentStatus == OrderStatus.CANCELLED || currentStatus == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Không thể cập nhật đơn đã hoàn thành hoặc đã huỷ");
        }

        boolean isVnPayOrder = "vnpay".equalsIgnoreCase(order.getPaymentType());
        boolean isCodOrder = "COD".equalsIgnoreCase(order.getPaymentType());

        if (newStatus == OrderStatus.CANCELLED) {
            if (currentStatus == OrderStatus.OUT_FOR_DELIVERY) {
                throw new IllegalStateException("Không thể huỷ khi đơn đang giao");
            }

            restoreStockIfStockWasDeducted(order);
            order.setStatus(OrderStatus.CANCELLED.name());
            return orderRepository.save(order);
        }

        if (newStatus.getId() < currentStatus.getId()) {
            throw new IllegalStateException("Không thể quay ngược trạng thái đơn hàng");
        }

        if (isVnPayOrder && order.getPaymentStatus() != PaymentStatus.PAID) {
            throw new IllegalStateException("Đơn VNPAY chưa thanh toán, không thể cập nhật trạng thái");
        }

        if (currentStatus == OrderStatus.PENDING_PAYMENT && newStatus != OrderStatus.PROCESSING) {
            throw new IllegalStateException("Đơn mới chỉ có thể chuyển sang xử lý hoặc huỷ");
        }

        if (isCodOrder
                && currentStatus == OrderStatus.PENDING_PAYMENT
                && newStatus == OrderStatus.PROCESSING) {
            deductStockAndClearCart(order);
        }

        order.setStatus(newStatus.name());

        if (isCodOrder && newStatus == OrderStatus.DELIVERED) {
            order.setPaymentStatus(PaymentStatus.PAID);
        }

        return orderRepository.save(order);
    }


    @Override
    public List<BookOrder> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Map<Integer, Integer> getMonthlyRevenue(int year) {
        List<Object[]> revenueData = orderRepository.getMonthlyRevenue(year);

        Map<Integer, Integer> monthlyRevenue = new HashMap<>();
        for (Object[] data : revenueData) {
            int month = (int) data[0];
            int revenue = ((Number) data[1]).intValue();
            monthlyRevenue.put(month, revenue);
        }

        for (int i = 1; i <= 12; i++) {
            monthlyRevenue.putIfAbsent(i, 0);
        }

        return monthlyRevenue;
    }

    @Override
    public Map<Integer, Integer> getDailyRevenue(int year, int month) {
        Map<Integer, Integer> dailyRevenue = new HashMap<>();

        List<BookOrder> orders = orderRepository.findOrdersByMonthAndYear(year, month);

        for (BookOrder order : orders) {
            LocalDateTime orderDate = order.getOrderDate();

            if (orderDate == null || order.getTotalAmount() == null) {
                continue;
            }

            int day = orderDate.getDayOfMonth();
            int totalAmount = order.getTotalAmount();

            dailyRevenue.put(day, dailyRevenue.getOrDefault(day, 0) + totalAmount);
        }

        return dailyRevenue;
    }

    @Override
    public Map<String, Long> getOrderStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> results = orderRepository.countOrdersByStatusAndDateRange(startDate, endDate);
        Map<String, Long> statistics = new HashMap<>();
        for (Object[] result : results) {
            String status = (String) result[0];
            Long count = (Long) result[1];
            statistics.put(status, count);
        }
        return statistics;
    }

    @Override
    public BookOrder getOrdersByOrderId(String orderId) {
        return orderRepository.findByOrderId(orderId);
    }

    @Override
    public Page<BookOrder> getAllOrdersPagination(Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Order.desc("orderDate")));
        return orderRepository.findAll(pageable);

    }

    @Transactional
    @Override
    public void handlePaymentSuccess(BookOrder order) {
        BookOrder dbOrder = orderRepository.findById(order.getId())
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        if (dbOrder.getPaymentStatus() == PaymentStatus.PAID) {
            return;
        }

        if (dbOrder.getPaymentStatus() == PaymentStatus.FAILED
                || OrderStatus.CANCELLED.name().equals(dbOrder.getStatus())) {
            throw new RuntimeException("Đơn hàng đã bị huỷ hoặc thanh toán thất bại");
        }

        deductStockAndClearCart(dbOrder);

        dbOrder.setPaymentStatus(PaymentStatus.PAID);
        dbOrder.setStatus(OrderStatus.PROCESSING.name());

        orderRepository.save(dbOrder);

        try {
            asyncMailService.sendOrderMail(dbOrder, OrderStatus.PROCESSING.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Transactional
    @Override
    public void handlePaymentFail(BookOrder order) {
        BookOrder dbOrder = orderRepository.findById(order.getId())
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        if (dbOrder.getPaymentStatus() == PaymentStatus.PAID) {
            return;
        }

        if (dbOrder.getPaymentStatus() == PaymentStatus.FAILED) {
            return;
        }

        dbOrder.setPaymentStatus(PaymentStatus.FAILED);
        dbOrder.setStatus(OrderStatus.CANCELLED.name());

        orderRepository.save(dbOrder);
    }

    @Override
    public BookOrder getOrderById(Integer id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void cancelOrder(BookOrder order) {
        BookOrder dbOrder = orderRepository.findById(order.getId())
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        OrderStatus currentStatus = OrderStatus.valueOf(dbOrder.getStatus());

        if (currentStatus == OrderStatus.CANCELLED) {
            return;
        }

        if (currentStatus == OrderStatus.OUT_FOR_DELIVERY || currentStatus == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Không thể huỷ đơn ở trạng thái hiện tại");
        }

        restoreStockIfStockWasDeducted(dbOrder);

        dbOrder.setStatus(OrderStatus.CANCELLED.name());
        orderRepository.save(dbOrder);
    }

    private void deductStockAndClearCart(BookOrder order) {
        List<BookOrderItem> items = bookOrderItemRepository.findByBookOrderId(order.getId());

        for (BookOrderItem item : items) {
            Book book = item.getBook();
            int stock = book.getStock() == null ? 0 : book.getStock();

            if (stock < item.getQuantity()) {
                throw new RuntimeException("Sản phẩm " + book.getBookName() + " không đủ hàng");
            }
        }

        for (BookOrderItem item : items) {
            Book book = item.getBook();

            book.setStock(book.getStock() - item.getQuantity());
            book.setSold(book.getSold() + item.getQuantity());
        }

        cartItemRepository.deleteByCartUserId(order.getUser().getId());
    }

    private void restoreStockIfStockWasDeducted(BookOrder order) {
        if (!isStockDeducted(order)) {
            return;
        }

        List<BookOrderItem> items = bookOrderItemRepository.findByBookOrderId(order.getId());

        for (BookOrderItem item : items) {
            Book book = item.getBook();

            book.setStock(book.getStock() + item.getQuantity());

            int newSold = book.getSold() - item.getQuantity();
            book.setSold(Math.max(newSold, 0));
        }
    }

    private boolean isStockDeducted(BookOrder order) {
        OrderStatus status = OrderStatus.valueOf(order.getStatus());

        if ("vnpay".equalsIgnoreCase(order.getPaymentType())) {
            return order.getPaymentStatus() == PaymentStatus.PAID;
        }

        if ("COD".equalsIgnoreCase(order.getPaymentType())) {
            return status != OrderStatus.PENDING_PAYMENT
                    && status != OrderStatus.CANCELLED;
        }

        return false;
    }

    @Transactional
    @Override
    public void handleCodOrder(BookOrder order) {
        BookOrder dbOrder = orderRepository.findById(order.getId())
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        if (!"COD".equalsIgnoreCase(dbOrder.getPaymentType())) {
            throw new RuntimeException("Đơn hàng không phải COD");
        }

        if (OrderStatus.CANCELLED.name().equals(dbOrder.getStatus())) {
            throw new RuntimeException("Đơn hàng đã bị huỷ");
        }

        if (!OrderStatus.PENDING_PAYMENT.name().equals(dbOrder.getStatus())) {
            return;
        }

        deductStockAndClearCart(dbOrder);

        dbOrder.setPaymentStatus(PaymentStatus.PENDING);
        dbOrder.setStatus(OrderStatus.PROCESSING.name());

        orderRepository.save(dbOrder);

        try {
            asyncMailService.sendOrderMail(dbOrder, OrderStatus.PROCESSING.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
