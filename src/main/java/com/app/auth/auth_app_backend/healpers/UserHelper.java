package com.app.auth.auth_app_backend.healpers;

import java.util.UUID;

public class UserHelper {

    public  static UUID parseUUID(String  uuid){
        return  UUID.fromString(uuid);
    }
}
