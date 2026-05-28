package com.library.util;

import java.io.UnsupportedEncodingException;
import java.security.Principal;

import com.library.module.order.entity.BookOrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import com.library.module.order.entity.BookOrder;
import com.library.module.user.entity.User;
import com.library.module.user.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class CommonUtil {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserService userService;
    String msg = null;;

    public Boolean sendMailForBookOrder(BookOrder order, String status) throws Exception {

        StringBuilder itemsHtml = new StringBuilder();

        for (BookOrderItem item : order.getItems()) {
            itemsHtml.append("""
            <tr>
                <td style="padding:8px;border:1px solid #ddd;">%s</td>
                <td style="padding:8px;border:1px solid #ddd;">%s</td>
                <td style="padding:8px;border:1px solid #ddd;text-align:center;">%d</td>
                <td style="padding:8px;border:1px solid #ddd;text-align:right;">%,d đ</td>
            </tr>
        """.formatted(
                    item.getBook().getBookName(),
                    item.getBook().getAuthor(),
                    item.getQuantity(),
                    item.getPrice()
            ));
        }

        String html = """
        <div style="font-family: Arial, sans-serif; background:#f6f6f6; padding:20px;">
            <div style="max-width:600px;margin:auto;background:white;border-radius:8px;padding:20px;">
                
                <h2 style="color:#e74c3c;text-align:center;">📚 Bookly</h2>
                
                <p>Xin chào <b>%s</b>,</p>
                
                <p>Đơn hàng của bạn đang ở trạng thái: 
                    <b style="color:#27ae60;">%s</b>
                </p>

                <h3>Chi tiết đơn hàng</h3>

                <table style="width:100%%;border-collapse:collapse;">
                    <thead>
                        <tr style="background:#f2f2f2;">
                            <th style="padding:8px;border:1px solid #ddd;">Tên sách</th>
                            <th style="padding:8px;border:1px solid #ddd;">Tác giả</th>
                            <th style="padding:8px;border:1px solid #ddd;">SL</th>
                            <th style="padding:8px;border:1px solid #ddd;">Giá</th>
                        </tr>
                    </thead>
                    <tbody>
                        %s
                    </tbody>
                </table>

                <p style="margin-top:15px;">
                    <b>Tổng tiền:</b> <span style="color:#e74c3c;">%,d đ</span>
                </p>

                <p><b>Thanh toán:</b> %s</p>

                <hr>

                <p style="font-size:13px;color:#888;text-align:center;">
                    Cảm ơn bạn đã mua hàng tại Bookly ❤️
                </p>
            </div>
        </div>
    """.formatted(
                order.getOrderAddress().getFirstName(),
                status,
                itemsHtml.toString(),
                order.getTotalAmount(),
                order.getPaymentType()
        );

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("thuandat4556@gmail.com", "Bookly");
        helper.setTo(order.getOrderAddress().getEmail());
        helper.setSubject("Xác nhận đơn hàng - Bookly");
        helper.setText(html, true);

        mailSender.send(message);
        return true;
    }

    public User getLoggedInUserDetails(Principal p) {
        String email = p.getName();
        User user = userService.getUserByEmail(email);
        return user;
    }

    public String normalizeFileName(String fileName) {

        if (fileName == null) return "file";

        String normalized = java.text.Normalizer.normalize(fileName, java.text.Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        normalized = normalized.replaceAll("[^a-zA-Z0-9\\.]", "_");
        normalized = normalized.replaceAll("_+", "_");

        return normalized.toLowerCase();
    }
}