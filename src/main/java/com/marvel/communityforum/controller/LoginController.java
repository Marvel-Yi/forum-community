package com.marvel.communityforum.controller;

import com.marvel.communityforum.entity.User;
import com.marvel.communityforum.service.UserService;
import com.marvel.communityforum.util.CommunityConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
public class LoginController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

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

    @PostMapping("/login")
    public Map<String, Object> login(String userName, String password,
                                     @RequestParam(name = "rememberMe", defaultValue = "false") boolean rememberMe,
                                     HttpServletResponse response) {
        int expiredSeconds = rememberMe ? REMEMBER_LOGIN_EXPIRED_SECONDS : DEFAULT_LOGIN_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(userName, password, expiredSeconds);
        Object ticket = map.get("loginTicketMsg");
        if (ticket != null) {
            // login success
            Cookie cookie = new Cookie("ticket", ticket.toString());
            cookie.setPath("/");
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
        }
        return map;
    }
}
