package com.library.module.cart.service.impl;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.library.module.cart.entity.CartItem;
import com.library.module.cart.repository.CartItemRepository;
import com.library.module.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import com.library.module.cart.entity.Cart;
import com.library.module.book.entity.Book;
import com.library.module.user.entity.User;
import com.library.module.cart.repository.CartRepository;
import com.library.module.book.repository.BookRepository;
import com.library.module.user.repository.UserRepository;
import jakarta.transaction.Transactional;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    private String formatNumber(Integer number) {
        if (number == null) {
            return null;
        }
        DecimalFormat formatter = new DecimalFormat("#,###,###");
        return formatter.format(number);
    }

    @Override
    public List<CartItem> getCartItems(Integer userId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null) return new ArrayList<>();

        return cartItemRepository.findByCartId(cart.getId());
    }

    @Override
    public Integer getCountCart(Integer userId) {
        return cartRepository.findByUserId(userId)
                .map(cart -> cartItemRepository.findByCartId(cart.getId()).size())
                .orElse(0);
    }

    @Override
    public void clearCartByUser(Integer userId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart != null) {
            List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
            cartItemRepository.deleteAll(items);
        }
    }

    @Override
    public void addToCart(Integer userId, Integer bookId, Integer quantity) {

        if (quantity == null || quantity <= 0) {
            throw new RuntimeException("Số lượng không hợp lệ");
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User không tồn tại"));
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        CartItem item = cartItemRepository
                .findByCartIdAndBookId(cart.getId(), bookId)
                .orElse(null);

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        if (item != null) {
            if (!item.getCart().getUser().getId().equals(userId)) {
                throw new RuntimeException("Không có quyền");
            }

            int newQty = item.getQuantity() + quantity;

            if (newQty > book.getStock()) {
                throw new RuntimeException("Vượt quá số lượng tồn kho");
            }

            item.setQuantity(newQty);

        } else {
            if (quantity > book.getStock()) {
                throw new RuntimeException("Vượt quá số lượng tồn kho");
            }

            item = new CartItem();
            item.setCart(cart);
            item.setBook(book);
            item.setQuantity(quantity);
            item.setPrice(book.getDiscountPrice());
        }

        item.setTotalPrice(item.getQuantity() * item.getPrice());

        cartItemRepository.save(item);
    }

    @Override
    @Transactional
    public void updateQuantity(String sy, Integer itemId, Integer userId) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại trong giỏ hàng"));

        if (!item.getCart().getUser().getId().equals(userId)) {
            throw new RuntimeException("Không có quyền thao tác giỏ hàng này");
        }

        int quantity = item.getQuantity();

        if ("inc".equals(sy)) {
            if (quantity + 1 > item.getBook().getStock()) {
                throw new RuntimeException("Vượt quá số lượng tồn kho");
            }

            quantity++;
        } else if ("dec".equals(sy)) {
            if (quantity - 1 <= 0) {
                cartItemRepository.delete(item);
                return;
            }

            quantity--;
        } else {
            throw new RuntimeException("Thao tác không hợp lệ");
        }

        item.setQuantity(quantity);
        item.setTotalPrice(quantity * item.getPrice());

        cartItemRepository.save(item);
    }

    @Override
    @Transactional
    public void removeItem(Integer itemId, Integer userId) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại trong giỏ hàng"));

        if (!item.getCart().getUser().getId().equals(userId)) {
            throw new RuntimeException("Không có quyền xoá sản phẩm này");
        }

        cartItemRepository.delete(item);
    }

    @Override
    public CartItem getItemById(Integer id, Integer userId) {
        CartItem item = cartItemRepository.findById(id).orElse(null);

        if (item == null) {
            return null;
        }

        if (!item.getCart().getUser().getId().equals(userId)) {
            throw new RuntimeException("Không có quyền xem sản phẩm này");
        }

        return item;
    }
}
