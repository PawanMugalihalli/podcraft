package com.pawan.riverside.service;

import com.pawan.riverside.dao.UserRepository;
import com.pawan.riverside.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private UserRepository userRepository;
    public User loadUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public void save(User user){
        userRepository.save(user);
        System.out.println("User saved!");
    }
}
