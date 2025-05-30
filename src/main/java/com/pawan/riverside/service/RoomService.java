package com.pawan.riverside.service;

import com.pawan.riverside.entity.Room;
import com.pawan.riverside.entity.RoomDTO;

import java.util.List;

public interface RoomService {

    public void createRoom(String username,String RoomName);
//    public RoomDTO joinRoom(String code);
    public List<Room> getAllRooms();
    public void deleteRoom(String roomCode);
    public Room getRoom(String roomCode);
}
