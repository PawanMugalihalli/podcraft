package com.pawan.podcraft.controllers;

import com.pawan.podcraft.entity.VideoCall;
import com.pawan.podcraft.entity.VideoCallDTO;
import com.pawan.podcraft.service.RoomService;
import com.pawan.podcraft.service.VideoCallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Controller
public class DashBoardController {
    @Autowired
    VideoCallService callService;
    @Autowired
    RoomService roomService;
    @GetMapping("/user/{userId}")
    public List<VideoCall> getUserCalls(@PathVariable String userId) {
        return callService.getCallsForUser(userId);
    }

    // Get a call by UUID
    @GetMapping("/{uuid}")
    public Optional<VideoCall> getCallByUuid(@PathVariable String callId) {
        return callService.getCallByCallId(callId);
    }

    @GetMapping("/dashboard")
    public String getDashboard(Model model, Principal principal) {
        String username = principal.getName();
        List<VideoCall> calls = callService.getCallsForUser(username);
        for (VideoCall call : calls) {
            System.out.println(call);
        }
        model.addAttribute("calls", calls);
        model.addAttribute("username", username);
        return "dashboard";
    }

    @ResponseBody
    @PostMapping("/save")
    public void saveInfo(Principal principal,@RequestBody VideoCallDTO videoCallDTO) {
        String username= principal.getName();

        VideoCall videoCall=new VideoCall();
        videoCall.setCallId(UUID.randomUUID().toString().substring(0, 6));
        videoCall.setDuration(videoCallDTO.getDuration());
        videoCall.setStartTime(videoCallDTO.getStartTime());
        videoCall.setGuestUrl(videoCallDTO.getGuestUrl());
        videoCall.setHostUrl(videoCallDTO.getHostUrl());
        String roomCode=videoCallDTO.getRoomCode();
        String host=roomService.getRoom(roomCode).getHost();
        videoCall.setHostId(host);
        String targetUser=videoCallDTO.getTargetUser();

        if(Objects.equals(targetUser, host)){
            videoCall.setGuestId(username);
        }else{
            videoCall.setGuestId(targetUser);
        }
        callService.saveCall(videoCall);
    }
}
