package com.pawan.riverside.controllers;

import com.pawan.riverside.entity.Room;
import com.pawan.riverside.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
public class RoomController {

    @Autowired
    private RoomService roomService;

    @PostMapping("/chat-lobby/create")
    public void createRoom(@RequestBody Room room) {
        String roomName= room.getRoomName();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        roomService.createRoom(username,roomName);
    }

    @GetMapping("/chat-lobby/rooms")
    public List<Room> getRooms(){
        return roomService.getAllRooms();
    }

    @DeleteMapping("/chat-lobby/delete/{roomCode}")
    public void deleteRoom(@PathVariable String roomCode){
        roomService.deleteRoom(roomCode);
    }

    @GetMapping("/current-user")
    @ResponseBody
    public String getCurrentUser(Principal principal) {
        return principal.getName();  // returns the username of the logged-in user
    }

}
