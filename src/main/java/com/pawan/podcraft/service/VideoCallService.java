package com.pawan.podcraft.service;

import com.pawan.podcraft.dao.VideoCallRepository;
import com.pawan.podcraft.entity.VideoCall;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VideoCallService {

    public VideoCall saveCall(VideoCall call);

    public List<VideoCall> getCallsForUser(String userId);

    public Optional<VideoCall> getCallByCallId(String callId);
}
