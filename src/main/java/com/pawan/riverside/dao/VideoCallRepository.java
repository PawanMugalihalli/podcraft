package com.pawan.riverside.dao;

import com.pawan.riverside.entity.VideoCall;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoCallRepository extends JpaRepository<VideoCall, Integer> {
    List<VideoCall> findByHostIdOrGuestId(String hostId, String guestId);
    VideoCall findByCallId(String callId);
}
