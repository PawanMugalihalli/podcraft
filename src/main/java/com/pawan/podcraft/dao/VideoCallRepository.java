package com.pawan.podcraft.dao;

import com.pawan.podcraft.entity.VideoCall;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoCallRepository extends JpaRepository<VideoCall, Integer> {
    List<VideoCall> findByHostIdOrGuestId(String hostId, String guestId);
    VideoCall findByCallId(String callId);
}
