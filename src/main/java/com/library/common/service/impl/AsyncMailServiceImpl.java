package com.library.common.service.impl;

import com.library.common.service.AsyncMailService;
import com.library.module.order.entity.BookOrder;
import com.library.module.order.entity.BookOrderItem;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AsyncMailServiceImpl implements AsyncMailService {

    private final JavaMailSender mailSender;

    @Async("mailTaskExecutor")
    @Override
    public void sendOtpMail(String email, String otp) {
        try {
            String safeEmail = escapeHtml(email);
            String safeOtp = escapeHtml(otp);

            String html = """
            <div style="font-family: Arial, sans-serif; background:#f6f6f6; padding:20px;">
                <div style="max-width:600px;margin:auto;background:white;border-radius:8px;padding:24px;">
                    
                    <h2 style="color:#e74c3c;text-align:center;margin-top:0;">Bookly</h2>
                    
                    <p style="font-size:16px;color:#333;margin-bottom:10px;">
                        Xin chào,
                    </p>
                    
                    <p style="font-size:15px;color:#444;line-height:1.6;">
                        Bạn đang thực hiện xác thực tài khoản hoặc yêu cầu đổi mật khẩu tại Bookly.
                        Vui lòng sử dụng mã OTP bên dưới để tiếp tục.
                    </p>
                    
                    <div style="text-align:center;margin:28px 0;">
                        <div style="
                            display:inline-block;
                            padding:14px 28px;
                            background:#f76b73;
                            color:#ffffff;
                            border-radius:8px;
                            font-size:28px;
                            letter-spacing:6px;
                            font-weight:bold;">
                            %s
                        </div>
                    </div>
                    
                    <p style="font-size:15px;color:#444;line-height:1.6;">
                        Mã OTP này có hiệu lực trong <b>5 phút</b>. Không chia sẻ mã này cho bất kỳ ai.
                    </p>
                    
                    <hr style="border:none;border-top:1px solid #eeeeee;margin:24px 0;">
                    
                    <p style="font-size:13px;color:#888;text-align:center;margin-bottom:0;">
                        Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email.
                    </p>
                </div>
            </div>
        """.formatted(safeOtp, safeEmail);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setFrom("thuandat4556@gmail.com", "Bookly");
            helper.setSubject("Mã OTP xác thực - Bookly");
            helper.setText(html, true);

            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Async("mailTaskExecutor")
    @Override
    public void sendOrderMail(BookOrder order, String status) {
        try {
            StringBuilder itemsHtml = new StringBuilder();

            for (BookOrderItem item : order.getItems()) {
                String safeBookName = "";
                String safeAuthor = "";

                if (item.getBook() != null) {
                    safeBookName = escapeHtml(item.getBook().getBookName());
                    safeAuthor = escapeHtml(item.getBook().getAuthor());
                }

                itemsHtml.append("""
                <tr>
                    <td style="padding:8px;border:1px solid #ddd;">%s</td>
                    <td style="padding:8px;border:1px solid #ddd;">%s</td>
                    <td style="padding:8px;border:1px solid #ddd;text-align:center;">%d</td>
                    <td style="padding:8px;border:1px solid #ddd;text-align:right;">%,d đ</td>
                </tr>
            """.formatted(
                        safeBookName,
                        safeAuthor,
                        item.getQuantity(),
                        item.getPrice()
                ));
            }

            String safeCustomerName = "";
            String customerEmail = "";

            if (order.getOrderAddress() != null) {
                safeCustomerName = escapeHtml(order.getOrderAddress().getFirstName());
                customerEmail = order.getOrderAddress().getEmail();
            }

            String safeStatus = escapeHtml(status);
            String safePaymentType = escapeHtml(order.getPaymentType());

            String html = """
            <div style="font-family: Arial, sans-serif; background:#f6f6f6; padding:20px;">
                <div style="max-width:600px;margin:auto;background:white;border-radius:8px;padding:20px;">
                    <h2 style="color:#e74c3c;text-align:center;">Bookly</h2>

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
                </div>
            </div>
        """.formatted(
                    safeCustomerName,
                    safeStatus,
                    itemsHtml.toString(),
                    order.getTotalAmount(),
                    safePaymentType
            );

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("thuandat4556@gmail.com", "Bookly");
            helper.setTo(customerEmail);
            helper.setSubject("Thông báo đơn hàng - Bookly");
            helper.setText(html, true);

            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}