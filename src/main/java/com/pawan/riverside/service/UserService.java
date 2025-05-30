package com.pawan.riverside.service;

import com.pawan.riverside.entity.User;

public interface UserService {

    public User loadUserByUsername(String username);
    public void save(User user);

}
