package com.pawan.podcraft.controllers;

import com.pawan.podcraft.entity.SignalMessage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class VideoCallController {

    private final SimpMessagingTemplate messagingTemplate;

    public VideoCallController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/signal/{roomCode}")
    public void handleSignal(@DestinationVariable String roomCode, Principal principal, @Payload SignalMessage message) {
        if (message.getToUser() == null || message.getType() == null) return;

        // Include caller's username
        String payload= message.getType().equals("call") ?
                principal.getName() : message.getData();
        messagingTemplate.convertAndSendToUser(
                message.getToUser(),
                "/queue/"+roomCode+"/"+ message.getType(),
                payload
        );
    }

    @MessageMapping("/{roomCode}/guestUrl")
    @SendTo("/queue/{roomCode}/guestUrl")
    public String sendGuestMessage(@DestinationVariable String roomCode, String guestUrl){
        System.out.println(guestUrl);
        return guestUrl;
    }
    @MessageMapping("/{roomCode}/hostUrl")
    @SendTo("/queue/{roomCode}/hostUrl")
    public String sendHostMessage(@DestinationVariable String roomCode, String hostUrl){
        System.out.println(hostUrl);
        return hostUrl;
    }
    @MessageMapping("/ping")
    @SendTo("/queue/pong")
    public String pong(String message) {
        return "pong";
    }


    @MessageMapping("/{roomCode}/endTime")
    @SendTo("/queue/{roomCode}/endTime")
    public String sendEndMessage(@DestinationVariable String roomCode, String endTime){
        return endTime;
    }
    @MessageMapping("/{roomCode}/startTime")
    @SendTo("/queue/{roomCode}/startTime")
    public String sendStartMessage(@DestinationVariable String roomCode, String startTime){
        return startTime;
    }

    @MessageMapping("/{roomCode}/flag")
    @SendTo("/queue/{roomCode}/flag")
    public String sendFlagMessage(@DestinationVariable String roomCode, String flag){
        return flag;
    }

}
