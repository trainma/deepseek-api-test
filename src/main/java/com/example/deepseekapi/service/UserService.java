package com.example.deepseekapi.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.deepseekapi.model.dto.LoginDTO;
import com.example.deepseekapi.model.dto.RegisterDTO;
import com.example.deepseekapi.model.entity.User;
import com.example.deepseekapi.model.vo.LoginVO;
import com.example.deepseekapi.repository.UserRepository;
import com.example.deepseekapi.util.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

/**
 * 用户服务
 */
@Slf4j
@Service
public class UserService extends ServiceImpl<UserRepository, User> implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 用户注册
     */
    @Transactional
    public User register(RegisterDTO registerDTO) {
        // 检查用户名是否已存在
        if (userRepository.countByUsername(registerDTO.getUsername()) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (registerDTO.getEmail() != null && userRepository.countByEmail(registerDTO.getEmail()) > 0) {
            throw new RuntimeException("邮箱已存在");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setEmail(registerDTO.getEmail());
        user.setStatus(1);
        
        // 保存用户
        this.save(user);
        return user;
    }

    /**
     * 用户登录
     */
    public LoginVO login(LoginDTO loginDTO) {
        try {
            // 验证用户名和密码
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())
            );
            
            // 设置认证信息
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 获取用户信息
            User user = userRepository.findByUsername(loginDTO.getUsername());
            if (user == null) {
                throw new UsernameNotFoundException("用户不存在");
            }
            
            // 生成JWT令牌
            String token = jwtUtils.generateToken(user.getUsername(), user.getId());
            
            // 返回登录结果
            return LoginVO.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .token(token)
                    .expireTime(jwtUtils.getExpirationTime())
                    .build();
            
        } catch (BadCredentialsException e) {
            throw new RuntimeException("用户名或密码错误");
        }
    }

    /**
     * 根据用户名获取用户
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * 实现UserDetailsService接口，用于Spring Security认证
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getStatus() == 1,
                true,
                true,
                true,
                new ArrayList<>()
        );
    }
}
