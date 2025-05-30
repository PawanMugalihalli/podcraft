package com.pawan.riverside.service;

import com.pawan.riverside.dao.RoomRepository;
import com.pawan.riverside.entity.Room;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RoomServiceImpl implements RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Override
    @Transactional
    public void createRoom(String username,String roomName) {
        Room room = new Room();
        room.setRoomCode(UUID.randomUUID().toString().substring(0, 8));
        room.setRoomName(roomName);
        room.setCreatedAt(LocalDateTime.now());
        room.setHost(username);
        room.setActive(true);
        room.setRecordingStatus("NOT_STARTED");  // Assuming a default recording status
        room.setMicOnByDefault(true);  // Default settings for mic and camera
        room.setCameraOnByDefault(true);
        roomRepository.save(room);
    }

    @Override
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }


    @Override
    @Transactional
    public void deleteRoom(String roomCode) {
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        String username=authentication.getName();
        Room room=roomRepository.findByRoomCode(roomCode);
        if(username.equals(room.getHost())){
            roomRepository.delete(room);
        }
    }

    public Room getRoom(String roomCode){
        return roomRepository.findByRoomCode(roomCode);
    }
}
