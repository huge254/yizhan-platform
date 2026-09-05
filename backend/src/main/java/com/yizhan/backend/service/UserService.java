package com.yizhan.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yizhan.backend.common.BusinessException;
import com.yizhan.backend.entity.User;
import com.yizhan.backend.mapper.UserMapper;
import com.yizhan.backend.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserService(UserMapper userMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    /** 注册：用户名唯一校验 + BCrypt 加密 */
    public void register(String username, String password, String nickname) {
        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }
        if (password == null || password.length() < 6) {
            throw new BusinessException("密码至少 6 位");
        }
        Long count = userMapper.selectCount(new QueryWrapper<User>().eq("username", username.trim()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(encoder.encode(password));
        user.setNickname(nickname == null || nickname.trim().isEmpty() ? username.trim() : nickname.trim());
        user.setRole("USER");
        userMapper.insert(user);
    }

    /** 登录：校验密码并签发 token */
    public Map<String, Object> login(String username, String password) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (user == null || !encoder.matches(password, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("token", jwtUtil.generate(user.getId(), user.getRole()));
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("role", user.getRole());
        return data;
    }

    /** 当前用户信息（不含密码） */
    public Map<String, Object> me(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("role", user.getRole());
        return data;
    }
}
