package com.backend.intelligent_scheduling_login.service;

import com.backend.intelligent_scheduling_login.model.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
* @author 86136
* @description 针对表【user】的数据库操作Service
* @createDate 2023-02-20 23:21:58
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param account 用户账户
     * @param password 用户密码
     * @param checkPassword 校验密码
     * @return 新用户id
     */
    String userRegister(String account,String password, String checkPassword, String name);

    /**
     * 用户登录
     *
     * @param account  用户账户
     * @param password 用户密码
     * @param request 请求
     * @return 返回用户信息
     */
    User userLogin(String account,String password, HttpServletRequest request);

    User getSafeUser(User OringinUser);

    Integer changePassword(String account, String password);

    Boolean isUser(String account, String password);

    Boolean isEmployee(String account, String password);

    String addStore(String account, String name);

}
