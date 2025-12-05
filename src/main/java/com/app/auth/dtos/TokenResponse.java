package com.app.auth.dtos;

import com.app.auth.entities.User;

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
