package com.example.discordclone.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.util.Map;

@Controller
public class CallController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/call/invite")
    public void inviteToCall(Map<String, Object> payload) {
        String target = (String) payload.get("target");
        messagingTemplate.convertAndSendToUser(target, "/queue/call-incoming", payload);
    }

    @MessageMapping("/call/accept")
    public void acceptCall(Map<String, Object> payload) {
        String caller = (String) payload.get("caller");
        messagingTemplate.convertAndSendToUser(caller, "/queue/call-accepted", payload);
    }

    @MessageMapping("/call/reject")
    public void rejectCall(Map<String, Object> payload) {
        String caller = (String) payload.get("caller");
        messagingTemplate.convertAndSendToUser(caller, "/queue/call-rejected", payload);
    }
}