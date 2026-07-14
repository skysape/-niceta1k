package com.example.discordclone.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.util.Map;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/send")
    public void sendMessage(Map<String, Object> message) {
        String channelId = String.valueOf(message.get("channelId"));
        messagingTemplate.convertAndSend("/topic/channel/" + channelId, message);
    }

    @MessageMapping("/chat/reaction")
    public void sendReaction(Map<String, Object> reaction) {
        String channelId = String.valueOf(reaction.get("channelId"));
        messagingTemplate.convertAndSend("/topic/channel/" + channelId + "/reactions", reaction);
    }
}