package com.pawan.riverside.service;

import com.pawan.riverside.dao.VideoCallRepository;
import com.pawan.riverside.entity.VideoCall;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VideoCallService {

    public VideoCall saveCall(VideoCall call);

    public List<VideoCall> getCallsForUser(String userId);

    public Optional<VideoCall> getCallByCallId(String callId);
}
