package com.familring.familyservice.service.chat.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {
    private final SimpMessagingTemplate template;

    @EventListener
    public void handleNotificationEvent(NotificationEvent event) {
        String destination = "/room/" + event.getRoomId() + "/readStatus";
        template.convertAndSend(destination, event.getMessage());
        System.out.println("[NotificationEventListener] roomId=" + event.getRoomId() + "에 알림 전송: " + event.getMessage());
    }
}