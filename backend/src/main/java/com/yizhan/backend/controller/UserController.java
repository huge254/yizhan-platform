package com.yizhan.backend.controller;

import com.yizhan.backend.common.Result;
import com.yizhan.backend.common.UserContext;
import com.yizhan.backend.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** 注册（无需登录） */
    @PostMapping("/register")
    public Result<Void> register(@RequestBody Map<String, String> body) {
        userService.register(body.get("username"), body.get("password"), body.get("nickname"));
        return Result.success();
    }

    /** 登录（无需登录） */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        return Result.success(userService.login(body.get("username"), body.get("password")));
    }

    /** 当前用户信息 */
    @GetMapping("/me")
    public Result<Map<String, Object>> me() {
        return Result.success(userService.me(UserContext.getUserId()));
    }
}
