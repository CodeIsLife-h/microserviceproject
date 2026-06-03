package com.ecommerce.notification.service;

import com.ecommerce.notification.entity.NotificationLog;
import com.ecommerce.notification.repository.NotificationLogRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final NotificationLogRepository logRepository;

    @Value("${notification.from-email}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender, NotificationLogRepository logRepository) {
        this.mailSender = mailSender;
        this.logRepository = logRepository;
    }

    public void sendOrderConfirmation(Long orderId, String customerEmail, List<?> items, Object total) {
        String subject = "Order Confirmed — Order #" + orderId;
        String body = buildOrderConfirmationHtml(orderId, customerEmail, items, total);

        NotificationLog log = new NotificationLog();
        log.setType("ORDER_CONFIRMED");
        log.setRecipientEmail(customerEmail);
        log.setReferenceId(String.valueOf(orderId));

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(customerEmail);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
            log.setStatus("SENT");
        } catch (Exception e) {
            log.setStatus("FAILED");
        } finally {
            logRepository.save(log);
        }
    }

    private String buildOrderConfirmationHtml(Long orderId, String customerEmail, List<?> items, Object total) {
        StringBuilder rows = new StringBuilder();
        for (Object item : items) {
            if (item instanceof Map<?, ?> map) {
                Object productId = map.get("productId");
                Object quantity = map.get("quantity");
                rows.append("<tr>")
                    .append("<td>").append(productId != null ? productId : "").append("</td>")
                    .append("<td>").append(quantity != null ? quantity : "").append("</td>")
                    .append("</tr>");
            }
        }

        return """
                <html><body>
                <h2>Your order has been confirmed!</h2>
                <p>Thank you for your purchase, %s.</p>
                <p><strong>Order ID:</strong> %d</p>
                <table border="1" cellpadding="8">
                  <thead><tr><th>Product ID</th><th>Quantity</th></tr></thead>
                  <tbody>%s</tbody>
                </table>
                <p><strong>Total:</strong> $%s</p>
                <p>Thank you for shopping with us!</p>
                </body></html>
                """.formatted(customerEmail, orderId, rows, total);
    }
}
