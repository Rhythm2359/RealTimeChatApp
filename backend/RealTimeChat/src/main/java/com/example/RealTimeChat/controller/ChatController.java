package com.example.RealTimeChat.controller;

import com.example.RealTimeChat.entity.ChatMessage;
import com.example.RealTimeChat.repository.ChatMessageRepository;
import com.example.RealTimeChat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class ChatController {

    @Autowired
    private UserService userService;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    //ADD USER (JOIN CHAT)
    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage,
                               SimpMessageHeaderAccessor headerAccessor) {

        if (userService.userExists(chatMessage.getSender())) {

            // store username in WebSocket session
            headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());

            // mark user as online
            userService.setUserOnlineStatus(chatMessage.getSender(), true);

            System.out.println("User added Successfully " +
                    chatMessage.getSender() + " with session ID " +
                    headerAccessor.getSessionId());

            // timestamp
            chatMessage.setTimeStamp(LocalDateTime.now());

            if (chatMessage.getContent() == null)
                chatMessage.setContent("");

            // save join message
            return chatMessageRepository.save(chatMessage);
        }

        return null;
    }


    // SEND PUBLIC MESSAGE
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {

        if (userService.userExists(chatMessage.getSender())) {

            if (chatMessage.getTimeStamp() == null)
                chatMessage.setTimeStamp(LocalDateTime.now());

            if (chatMessage.getContent() == null)
                chatMessage.setContent("");

            return chatMessageRepository.save(chatMessage);
        }

        return null;
    }


    // SEND PRIVATE MESSAGE
    @MessageMapping("/chat.sendPrivateMessage")
    public void sendPrivateMessage(@Payload ChatMessage chatMessage,
                                   SimpMessageHeaderAccessor headerAccessor) {

        if (userService.userExists(chatMessage.getSender()) &&
                userService.userExists(chatMessage.getRecepient())) {

            if (chatMessage.getTimeStamp() == null)
                chatMessage.setTimeStamp(LocalDateTime.now());

            if (chatMessage.getContent() == null)
                chatMessage.setContent("");

            chatMessage.setType(ChatMessage.MessageType.PRIVATE_MESSAGE);

            // save message in DB
            ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
            System.out.println("Message saved successfully with Id " + savedMessage.getId());

            try {
                // destination for RECEIVER
                String recepientDestination =
                        "/user/" + chatMessage.getRecepient() + "/queue/private";

                System.out.println("Sending message to recipient destination "
                        + recepientDestination);

                messagingTemplate.convertAndSend(recepientDestination, savedMessage);


                // destination for SENDER (to update sender’s chat instantly)
                String senderDestination =
                        "/user/" + chatMessage.getSender() + "/queue/private";

                System.out.println("Sending message to sender destination "
                        + senderDestination);

                messagingTemplate.convertAndSend(senderDestination, savedMessage);

            } catch (Exception e) {
                System.out.println("ERROR occurred while sending the message " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
