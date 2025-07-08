package com.pawan.podcraft.dao;

import com.pawan.podcraft.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Integer> {

    public User findByUsername(String username);
}
