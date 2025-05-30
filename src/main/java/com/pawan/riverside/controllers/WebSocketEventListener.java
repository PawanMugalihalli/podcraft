package com.pawan.riverside.controllers;

import com.pawan.riverside.entity.JoinRoomMessage;
import com.pawan.riverside.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class WebSocketEventListener {

    @Autowired
    RoomService roomService;

    private final SimpMessagingTemplate messagingTemplate;
    private final ConcurrentHashMap<String, Set<String>> roomUsers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> userRoomMap = new ConcurrentHashMap<>();

    public WebSocketEventListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/requestOnlineUsers/{roomCode}")
    public void handleRequestOnlineUsers(@DestinationVariable String roomCode, Principal principal) {
        broadcastOnlineUsers(roomCode);
    }

    @MessageMapping("/joinRoom")
    public void joinRoom(JoinRoomMessage message, Principal principal) {
        String roomCode = message.getRoomCode();
        String username = principal.getName();
        roomUsers.putIfAbsent(roomCode, ConcurrentHashMap.newKeySet());
        roomUsers.get(roomCode).add(username);
        userRoomMap.put(username, roomCode);
        broadcastOnlineUsers(roomCode);
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        String username = event.getUser().getName();
        String roomCode = userRoomMap.get(username);
        if (roomCode != null) {
            Set<String> users = roomUsers.get(roomCode);
            if (users != null) {
                users.remove(username);
                broadcastOnlineUsers(roomCode);
                if (users.isEmpty()) {
                    roomUsers.remove(roomCode);
                }
            }
            userRoomMap.remove(username);
        }
        broadcastOnlineUsers(roomCode);
    }

    private void broadcastOnlineUsers(String roomCode) {
        Set<String> users = roomUsers.get(roomCode);
        messagingTemplate.convertAndSend("/queue/onlineUsers/" + roomCode, users);
    }

    @GetMapping("/chat-lobby/room/{roomCode}")
    public String joinRoom(Principal principal,Model model, @PathVariable String roomCode, RedirectAttributes redirectAttributes) {

        int roomSize=0;
        if(!roomUsers.isEmpty()){
            if(roomUsers.containsKey(roomCode)){
                String user= principal.getName();
                roomSize=roomUsers.get(roomCode).size();

                if(!roomUsers.get(roomCode).contains(user)  && roomSize >= 2)
                {
                    redirectAttributes.addFlashAttribute("error", "Room is full, Maximum limit is 2!");
                    return "redirect:/chat-lobby";
                }
            }
        }

        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        String username= authentication.getName();
        model.addAttribute("roomCode",roomCode);
        model.addAttribute("username",username);
        String host=roomService.getRoom(roomCode).getHost();
        model.addAttribute("host",host);
        return "videoChat";
    }
}
