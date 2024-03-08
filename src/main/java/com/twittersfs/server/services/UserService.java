package com.twittersfs.server.services;

import com.twittersfs.server.dtos.user.UserData;
import com.twittersfs.server.dtos.user.UserRegister;
import com.twittersfs.server.security.AuthDto;

public interface UserService {
    UserData getUser(String email);

    void register(UserRegister dto);

    AuthDto getUserAuthDto(String email);

    void resetPassword(String email, String password);
}
