package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dto.UserLoginDTO;
import com.example.dto.UserRegisterDTO;
import com.example.entity.SysUser;
import com.example.vo.LoginVO;

public interface SysUserService extends IService<SysUser> {

    void register(UserRegisterDTO registerDTO);

    LoginVO login(UserLoginDTO loginDTO);
}