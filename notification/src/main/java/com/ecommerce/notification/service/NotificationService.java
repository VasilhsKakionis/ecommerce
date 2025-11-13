package com.ecommerce.notification.service;

import com.ecommerce.notification.model.Notification;
import com.ecommerce.notification.model.NotificationStatus;
import com.ecommerce.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;
    private final int MAX_RETRIES = 3;

    @KafkaListener(topics = "order-processed-events", groupId = "notification-group")
    public void consumeOrderEvent(String message) {
        System.out.println("Received event: " + message);

        String[] parts = message.split(":");
        Long orderId = Long.parseLong(parts[1]);

        sendNotification(orderId, "EMAIL", "Your order #" + orderId + " has been processed.");
        sendNotification(orderId, "SMS", "Order #" + orderId + " processed successfully.");
    }

    @Transactional
    public void sendNotification(Long orderId, String channel, String content) {
        Notification notification = Notification.builder()
                .orderId(orderId)
                .channel(channel)
                .message(content)
                .status(NotificationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .retryCount(0)
                .build();

        repository.save(notification);

        boolean sent = trySend(notification);

        if (!sent && notification.getRetryCount() < MAX_RETRIES) {
            retryNotification(notification);
        }
    }

    private boolean trySend(Notification notification) {
        try {

            if (Math.random() < 0.8) {
                notification.setStatus(NotificationStatus.SENT);
                notification.setSentAt(LocalDateTime.now());
            } else {
                throw new RuntimeException("Simulated failure");
            }
            repository.save(notification);
            return notification.getStatus() == NotificationStatus.SENT;
        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
            repository.save(notification);
            return false;
        }
    }

    private void retryNotification(Notification notification) {
        notification.setRetryCount(notification.getRetryCount() + 1);
        repository.save(notification);

        try {
            Thread.sleep((long) Math.pow(2, notification.getRetryCount()) * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        trySend(notification);
    }
}