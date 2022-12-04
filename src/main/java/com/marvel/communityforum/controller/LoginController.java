package com.marvel.communityforum.controller;

import com.marvel.communityforum.entity.User;
import com.marvel.communityforum.service.UserService;
import com.marvel.communityforum.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class LoginController implements CommunityConstant {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        return userService.reigster(user);
    }

    @GetMapping("/activation/{userId}/{activationCode}")
    public String activate(@PathVariable("userId") int id, @PathVariable("activationCode") String code) {
        int res = userService.activation(id, code);
        if (res == ACTIVATION_SUCCESS) {
            return "activation succeeded";
        } else if (res == ACTIVATION_REPEAT) {
            return "do not activate repeatedly";
        } else {
            return "wrong activation code";
        }
    }
}
