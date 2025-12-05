package com.app.auth.services.impl;

import com.app.auth.dtos.UserDto;
import com.app.auth.entities.User;
import com.app.auth.services.AuthService;
import com.app.auth.services.UserService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthServiceImpl  implements AuthService {
    private  final UserService  userService ;
    private  final PasswordEncoder  passwordEncoder;


    @Override
    public UserDto registerUser(UserDto userDto) {
        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
         return  userService.createUser(userDto) ;
    }
}
