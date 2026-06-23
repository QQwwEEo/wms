package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.entity.User;
import com.wms.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/page")
    public Result<PageResult<User>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String role) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(username), User::getUsername, username);
        wrapper.eq(StringUtils.hasText(role), User::getRole, role);
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> page = userService.page(new Page<>(pageNum, pageSize), wrapper);
        page.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(PageResult.of(page.getTotal(), page.getRecords()));
    }

    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user != null) user.setPassword(null);
        return Result.success(user);
    }

    private boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 6) return false;
        return password.matches(".*[a-zA-Z].*") && password.matches(".*\\d.*");
    }

    @PostMapping
    public Result<Void> add(@RequestBody User user) {
        // 检查用户名是否存在
        long count = userService.count(new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername()));
        if (count > 0) {
            return Result.error("用户名已存在");
        }
        if (!isPasswordStrong(user.getPassword())) {
            return Result.error("密码必须包含字母和数字，且长度不少于6位");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.save(user);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody User user) {
        if (StringUtils.hasText(user.getPassword())) {
            if (!isPasswordStrong(user.getPassword())) {
                return Result.error("密码必须包含字母和数字，且长度不少于6位");
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null);
        }
        userService.updateById(user);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success();
    }

    @PutMapping("/updateInfo")
    public Result<Void> updateInfo(@RequestBody User user, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        user.setId(userId);
        if (StringUtils.hasText(user.getPassword())) {
            if (!isPasswordStrong(user.getPassword())) {
                return Result.error("密码必须包含字母和数字，且长度不少于6位");
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null);
        }
        userService.updateById(user);
        return Result.success();
    }

    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        User user = new User();
        user.setId(id);
        user.setStatus(body.get("status"));
        userService.updateById(user);
        return Result.success();
    }
}
