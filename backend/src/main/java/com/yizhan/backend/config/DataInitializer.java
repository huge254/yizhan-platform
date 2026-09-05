package com.yizhan.backend.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yizhan.backend.entity.User;
import com.yizhan.backend.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 启动时确保管理员账号存在：admin / root123456
 * （替代 SQL 里写死无效 BCrypt 哈希的做法）
 */
@Slf4j
@Order(1) // 先于 SeedDataInitializer(@Order(2)) 执行
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;

    public DataInitializer(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public void run(String... args) {
        Long count = userMapper.selectCount(new QueryWrapper<User>().eq("username", "admin"));
        if (count == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(new BCryptPasswordEncoder().encode("root123456"));
            admin.setNickname("系统管理员");
            admin.setRole("ADMIN");
            userMapper.insert(admin);
            log.info("已自动创建管理员账号 admin / root123456");
        }
    }
}
