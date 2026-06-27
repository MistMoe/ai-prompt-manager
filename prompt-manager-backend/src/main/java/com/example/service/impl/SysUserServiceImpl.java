package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dto.UserLoginDTO;
import com.example.dto.UserRegisterDTO;
import com.example.entity.SysUser;
import com.example.mapper.SysUserMapper;
import com.example.service.SysUserService;
import com.example.utils.JwtUtil;
import com.example.vo.LoginVO;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Override
    public void register(UserRegisterDTO registerDTO) {
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", registerDTO.getUserName());

        SysUser existUser = this.getOne(queryWrapper);
        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUserName(registerDTO.getUserName());
        user.setNickName(registerDTO.getNickName());
        user.setPassword(encryptPassword(registerDTO.getPassword()));
        user.setCreateTime(LocalDateTime.now());

        this.save(user);
    }

    @Override
    public LoginVO login(UserLoginDTO loginDTO) {
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", loginDTO.getUserName());

        SysUser user = this.getOne(queryWrapper);
        if (user == null) {
            throw new RuntimeException("用户名不存在");
        }

        String encryptedPassword = encryptPassword(loginDTO.getPassword());
        if (!encryptedPassword.equals(user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        String token = JwtUtil.createToken(user.getId(), user.getUserName());

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserId(user.getId());
        loginVO.setUserName(user.getUserName());
        loginVO.setNickName(user.getNickName());

        return loginVO;
    }

    private String encryptPassword(String password) {
        return DigestUtils.md5DigestAsHex(
                ("prompt-manager-" + password).getBytes(StandardCharsets.UTF_8)
        );
    }
}