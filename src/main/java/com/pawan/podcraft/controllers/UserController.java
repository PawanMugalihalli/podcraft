package com.pawan.podcraft.controllers;

import com.pawan.podcraft.entity.RegisterDTO;
import com.pawan.podcraft.entity.User;
import com.pawan.podcraft.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;


@Controller
public class UserController {

    @Autowired
    UserService userService;
    @GetMapping("/register")
    public String register(Model model) {
        RegisterDTO user=new RegisterDTO();
        model.addAttribute(user);
        model.addAttribute("success",false);
        return "register";
    }
    @PostMapping("/register")
    public String register(Model model,
                           @Valid @ModelAttribute RegisterDTO registerDto,
                           BindingResult result){

        if(!registerDto.getPassword().equals(registerDto.getConfirmPassword())){
            result.addError(
                    new FieldError("registerDto","confirmPassword","Password and Confirm Password do not match")

            );
        }
        User user= userService.loadUserByUsername(registerDto.getUsername());
        if(user!=null){
            result.addError(
                    new FieldError("registerDto","username","username is already used")
            );
        }
        if(result.hasErrors()){
            return "register";
        }
        try {
//            var bCryptEncoder =new BCryptPasswordEncoder();

            User newUser=new User();
            newUser.setUsername(registerDto.getUsername());
            newUser.setPassword("{noop}"+registerDto.getPassword());
            newUser.setRoles("USER");
            userService.save(newUser);

            model.addAttribute("registerDto", new RegisterDTO());
            model.addAttribute("success",true);
        }
        catch(Exception ex){
            result.addError(
                    new FieldError("registerDto","username",ex.getMessage())
            );
        }
        return "register";
    }
}
