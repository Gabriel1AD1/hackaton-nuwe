package com.hackathon.blockchain.service;

import com.hackathon.blockchain.dto.RequestLoginUser;
import com.hackathon.blockchain.dto.RequestRegisterUserDTO;
import com.hackathon.blockchain.model.User;

import java.util.Optional;

public interface UserService {
    User register(RequestRegisterUserDTO dto);
    Optional<User> findByUsername(String username);
    User login(RequestLoginUser dto);
}
