package com.app.auth.auth_app_backend.controllers;

import com.app.auth.auth_app_backend.dtos.UserDto;
import com.app.auth.auth_app_backend.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
public class UserController {
    private   final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto>  createUser(@RequestBody UserDto  userDto){
        return  ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userDto));
    }

    @GetMapping
    public ResponseEntity<Iterable<UserDto>>  getAllUsers(){
        return  ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto>  createUser(@PathVariable("email")  String  mail){
        return  ResponseEntity.status(HttpStatus.FOUND).body(userService.getUserByEmail(mail));
    }


}
