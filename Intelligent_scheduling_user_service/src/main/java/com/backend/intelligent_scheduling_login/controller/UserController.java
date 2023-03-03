package com.backend.intelligent_scheduling_login.controller;

import com.alibaba.nacos.common.utils.StringUtils;
import com.backend.intelligent_scheduling_login.common.BaseResponse;
import com.backend.intelligent_scheduling_login.common.ErrorCode;
import com.backend.intelligent_scheduling_login.common.ResultUtils;
import com.backend.intelligent_scheduling_login.exception.BusinessException;
import com.backend.intelligent_scheduling_login.model.User;
import com.backend.intelligent_scheduling_login.model.request.UserLoginRequest;
import com.backend.intelligent_scheduling_login.model.request.UserRegisterRequest;
import com.backend.intelligent_scheduling_login.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    public UserService userService;

    @ApiOperation("用户注册")
    @PostMapping("/register")
    public BaseResponse<String> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
//            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        String account = userRegisterRequest.getAccount();
        String password = userRegisterRequest.getPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String name = userRegisterRequest.getName();
        if (StringUtils.isAnyBlank(account, password, checkPassword, name)) {
            return null;
        }
        String result = userService.userRegister(account, password, checkPassword,name);

        return ResultUtils.success(result);
    }

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR,"请求数据为空");
        }
        String account = userLoginRequest.getAccount();
        String password = userLoginRequest.getPassword();

        if (StringUtils.isAnyBlank(account,password)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"输入存在空格");
        }
        User user = userService.userLogin(account, password, request);
        return ResultUtils.success(user);
    }

}
