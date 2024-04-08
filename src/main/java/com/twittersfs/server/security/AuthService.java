package com.twittersfs.server.security;

import com.twittersfs.server.dtos.user.UserRegister;
import lombok.NonNull;

public interface AuthService {
    void register(UserRegister dto);
    JwtResponse login(@NonNull JwtRequest authRequest);
    JwtResponse getAccessToken(@NonNull String refreshToken);
    JwtResponse refresh(@NonNull String refreshToken);
    void logout(String email);
    void sendResetPasswordEmail(ResetPassword email);
    void resetPassword(String email, ResetPassword resetData);
    String sendVerificationCode(String email);
}
