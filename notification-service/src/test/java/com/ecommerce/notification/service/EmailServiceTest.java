package com.ecommerce.notification.service;

import com.ecommerce.notification.entity.NotificationLog;
import com.ecommerce.notification.repository.NotificationLogRepository;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmailServiceTest {

    @Mock JavaMailSender mailSender;
    @Mock NotificationLogRepository logRepository;
    @Mock MimeMessage mimeMessage;
    @InjectMocks EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@ecommerce.local");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void sendOrderConfirmation_successfullySent_logsAsSent() {
        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailService.sendOrderConfirmation(1L, "customer@example.com",
                List.of(Map.of("productId", 10, "quantity", 2)), "59.98");

        ArgumentCaptor<NotificationLog> captor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(logRepository).save(captor.capture());

        NotificationLog saved = captor.getValue();
        assertThat(saved.getType()).isEqualTo("ORDER_CONFIRMED");
        assertThat(saved.getRecipientEmail()).isEqualTo("customer@example.com");
        assertThat(saved.getStatus()).isEqualTo("SENT");
        assertThat(saved.getReferenceId()).isEqualTo("1");
    }

    @Test
    void sendOrderConfirmation_mailFails_logsAsFailed() {
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

        emailService.sendOrderConfirmation(2L, "customer@example.com", List.of(), "0");

        ArgumentCaptor<NotificationLog> captor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(logRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("FAILED");
    }
}
