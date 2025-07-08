package com.pawan.podcraft.service;

import com.pawan.podcraft.entity.User;

public interface UserService {

    public User loadUserByUsername(String username);
    public void save(User user);

}
