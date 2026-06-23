package com.wms.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.entity.User;
import com.wms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // 初始化默认用户（如果不存在）
        initUser("admin", "123456", "系统管理员", "13800000001", "admin");
        initUser("manager", "123456", "仓库管理员", "13800000002", "manager");
        initUser("user1", "123456", "张三", "13800000003", "user");
    }

    private void initUser(String username, String password, String realName, String phone, String role) {
        long count = userService.count(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (count == 0) {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setRealName(realName);
            user.setPhone(phone);
            user.setRole(role);
            user.setStatus(1);
            userService.save(user);
            System.out.println("初始化用户: " + username);
        } else {
            // 确保密码正确
            User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
            user.setPassword(passwordEncoder.encode(password));
            userService.updateById(user);
        }
    }
}
