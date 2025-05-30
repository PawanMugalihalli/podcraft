package com.pawan.riverside.controllers;

import com.pawan.riverside.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class JspController {

    @Autowired
    RoomService roomService;

    @GetMapping("/")
    public String index(){
        return "index";
    }

    @GetMapping("/chat-lobby")
    public String chatLobby(){
        return "chat-lobby";
    }

}
