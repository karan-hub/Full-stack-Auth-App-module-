package com.app.auth.auth_app_backend.services;

import com.app.auth.auth_app_backend.dtos.UserDto;

public interface UserService {
    UserDto createUser(UserDto  userDto);
    UserDto getUserByEmail(String  mail);
    UserDto updateUser(UserDto  userDto , String  userId);
    void deleteUser(String userId);
    UserDto  getUserById(String userId);

    Iterable<UserDto> getAllUsers();
}
