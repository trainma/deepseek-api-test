package com.example.deepseekapi.util;

import com.example.deepseekapi.model.entity.User;
import com.example.deepseekapi.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 安全工具类
 */
@Component
public class SecurityUtil {

    private static UserRepository userRepository;

    public SecurityUtil(UserRepository userRepository) {
        SecurityUtil.userRepository = userRepository;
    }

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("用户未登录");
        }
        
        Object principal = authentication.getPrincipal();
        String username = null;
        
        if (principal instanceof org.springframework.security.core.userdetails.User) {
            username = ((org.springframework.security.core.userdetails.User) principal).getUsername();
        } else if (principal instanceof String) {
            username = (String) principal;
        }
        
        if (username == null) {
            throw new RuntimeException("获取用户名失败");
        }
        
        // 通过用户名查询用户ID
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        return user.getId();
    }
}
