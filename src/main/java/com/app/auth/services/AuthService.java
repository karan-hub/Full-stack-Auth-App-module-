package com.app.auth.services;

import com.app.auth.dtos.UserDto;

public interface AuthService {
    UserDto  registerUser(UserDto  userDto);
}
