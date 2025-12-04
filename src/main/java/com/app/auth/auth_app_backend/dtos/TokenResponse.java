package com.app.auth.auth_app_backend.dtos;

import com.app.auth.auth_app_backend.entities.User;

public record TokenResponse(
        String accessToken ,
        String refreshToken ,
        long expire,
        String tokenType,
        UserDto  user
) {
    public  static  TokenResponse of(String  accessToken , String  refreshToken , long expire , UserDto  user){
        return  new TokenResponse(accessToken , refreshToken , expire,"Bearer" ,user);
    }
}
