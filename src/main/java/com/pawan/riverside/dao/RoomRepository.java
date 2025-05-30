package com.pawan.riverside.dao;

import com.pawan.riverside.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;


public interface RoomRepository extends JpaRepository<Room,Integer> {
    public Room findByRoomCode(String roomCode);
}
