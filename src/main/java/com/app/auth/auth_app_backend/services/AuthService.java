package com.app.auth.auth_app_backend.services;

import com.app.auth.auth_app_backend.dtos.UserDto;

public interface AuthService {
    UserDto  registerUser(UserDto  userDto);
}
