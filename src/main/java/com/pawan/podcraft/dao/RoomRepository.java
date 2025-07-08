package com.pawan.podcraft.dao;

import com.pawan.podcraft.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;


public interface RoomRepository extends JpaRepository<Room,Integer> {
    public Room findByRoomCode(String roomCode);
}
