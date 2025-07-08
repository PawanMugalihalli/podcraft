package com.pawan.podcraft.service;

import com.pawan.podcraft.dao.VideoCallRepository;
import com.pawan.podcraft.entity.VideoCall;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VideoCallServiceImpl implements VideoCallService{

    @Autowired
    private VideoCallRepository videoCallRepository;

    @Transactional
    public VideoCall saveCall(VideoCall call) {
        return videoCallRepository.save(call);
    }

    public List<VideoCall> getCallsForUser(String userId) {
        return videoCallRepository.findByHostIdOrGuestId(userId, userId);
    }

    public Optional<VideoCall> getCallByCallId(String callId) {
        return Optional.ofNullable(videoCallRepository.findByCallId(callId));
    }
}
