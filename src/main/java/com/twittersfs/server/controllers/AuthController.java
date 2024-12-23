package com.twittersfs.server.controllers;

import com.twittersfs.server.dtos.user.UserRegister;
import com.twittersfs.server.security.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/v1/auth")
public class AuthController {
    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public void register(@Valid @NotNull(message = "Request body must not be empty") @RequestBody UserRegister dto) {
        authService.register(dto);
    }

    @PostMapping("/login")
    @ResponseBody
    public JwtResponse login(@Valid @NotNull(message = "Request body must not be empty") @RequestBody JwtRequest authRequest) {
        return authService.login(authRequest);
    }

    @PostMapping("/access")
    @ResponseBody
    public JwtResponse getNewAccessToken(@NotNull(message = "Request body must not be empty") @RequestBody RefreshJwtRequest request) {
        return authService.getAccessToken(request.getRefreshToken());
    }

    @PostMapping("/refresh")
    @ResponseBody
    public JwtResponse getNewRefreshToken(@NotNull(message = "Request body must not be empty") @RequestBody RefreshJwtRequest request) {
        return authService.refresh(request.getRefreshToken());
    }

    @PostMapping("/logout")
    public void logout(Authentication authentication) {
        authService.logout(authentication.getPrincipal().toString());
    }

    @GetMapping("/authorities")
    public Map<String, Object> getPrincipalInfo(Authentication authentication) {
        Collection<String> authorities = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Map<String, Object> info = new HashMap<>();
        info.put("authorities", authorities);
        return info;
    }

//    @PostMapping("/reset-password")
//    public void resetPassword(@NotNull(message = "Request body must not be empty") @RequestBody ResetPassword data){
//        authService.sendResetPasswordEmail(data);
//    }
//
//    @PostMapping("/new-password")
//    public void setNewPassword(Authentication authentication, @NotNull(message = "Request body must not be empty") @RequestBody ResetPassword data){
//        authService.resetPassword(authentication.getPrincipal().toString(), data);
//    }
}
