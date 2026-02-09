package com.example.carte.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;

@Service
public class FirebaseNotificationService {

        public void sendNotificationToTokens(
                        List<String> tokens,
                        String title,
                        String body) throws Exception {

                if (tokens == null || tokens.isEmpty())
                        return;

                MulticastMessage message = MulticastMessage.builder()
                                .setNotification(
                                                Notification.builder()
                                                                .setTitle(title)
                                                                .setBody(body)
                                                                .build())
                                .addAllTokens(tokens)
                                .build();

                BatchResponse response = FirebaseMessaging.getInstance()
                                .sendEachForMulticast(message);

                System.out.println("Notifications envoyées : "
                                + response.getSuccessCount());
                response.getResponses().forEach(r -> {
                        if (!r.isSuccessful()) {
                                System.out.println("Token invalide : "
                                                + r.getException().getMessage());
                        }
                });

        }
}
