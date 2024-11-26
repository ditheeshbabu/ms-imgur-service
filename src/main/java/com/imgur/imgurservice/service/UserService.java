package com.imgur.imgurservice.service;

import com.imgur.imgurservice.model.Authentication.JwtResponse;
import com.imgur.imgurservice.model.UserRequest;
import com.imgur.imgurservice.model.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse createUser(UserRequest user);

    UserResponse getUserByName(String username);

    String deleteByUsername(String userId);

    JwtResponse authorizeUser(String username, String password);

    void updateUserImages(String username, List<String> imageIds);
}
