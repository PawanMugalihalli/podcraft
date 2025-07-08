package com.pawan.podcraft.service;

import com.pawan.podcraft.entity.Room;
import com.pawan.podcraft.entity.RoomDTO;

import java.util.List;

public interface RoomService {

    public void createRoom(String username,String RoomName);
//    public RoomDTO joinRoom(String code);
    public List<Room> getAllRooms();
    public void deleteRoom(String roomCode);
    public Room getRoom(String roomCode);
}
