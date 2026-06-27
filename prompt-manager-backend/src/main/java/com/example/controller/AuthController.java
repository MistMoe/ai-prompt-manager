package com.example.controller;

import com.example.common.Result;
import com.example.common.UserContext;
import com.example.dto.UserLoginDTO;
import com.example.dto.UserRegisterDTO;
import com.example.entity.SysUser;
import com.example.service.SysUserService;
import com.example.vo.LoginVO;
import com.example.vo.UserInfoVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Resource
    private SysUserService sysUserService;

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        sysUserService.register(registerDTO);
        return Result.success();
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        LoginVO loginVO = sysUserService.login(loginDTO);
        return Result.success(loginVO);
    }

    @GetMapping("/info")
    public Result<UserInfoVO> info() {
        Long userId = UserContext.getUserId();
        SysUser user = sysUserService.getById(userId);

        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setUserId(user.getId());
        userInfoVO.setUserName(user.getUserName());
        userInfoVO.setNickName(user.getNickName());

        return Result.success(userInfoVO);
    }
}