package com.twittersfs.server.controllers;

import com.twittersfs.server.dtos.user.UserData;
import com.twittersfs.server.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public UserData get(Authentication authentication) {
        return userService.getUser(authentication.getPrincipal().toString());
    }
}
