package com.ecommerce.notification.unit;

import com.ecommerce.notification.model.Notification;
import com.ecommerce.notification.model.NotificationStatus;
import com.ecommerce.notification.repository.NotificationRepository;
import com.ecommerce.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationServiceUnitTest {

    @Mock private NotificationRepository repository;

    @InjectMocks private NotificationService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendNotificationSuccess() {
        Notification notification = Notification.builder()
                .orderId(1001L)
                .channel("EMAIL")
                .message("Test message")
                .status(NotificationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .retryCount(0)
                .build();

        when(repository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.sendNotification(1001L, "EMAIL", "Test message");

        verify(repository, atLeastOnce()).save(any(Notification.class));
    }

    @Test
    void testRetryLogic() {
        // Simulate failed send by overriding trySend method (requires refactor or package-private for test)
        // Here, just testing repository interaction
        service.sendNotification(1002L, "SMS", "Retry test message");

        verify(repository, atLeastOnce()).save(any(Notification.class));
    }
}