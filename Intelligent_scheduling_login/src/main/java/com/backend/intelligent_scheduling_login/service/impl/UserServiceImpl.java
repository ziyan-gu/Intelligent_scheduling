package com.backend.intelligent_scheduling_login.service.impl;

import com.alibaba.nacos.common.utils.StringUtils;
import com.backend.intelligent_scheduling_login.common.ErrorCode;
import com.backend.intelligent_scheduling_login.common.UserInfoCheckUtil;
import com.backend.intelligent_scheduling_login.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.backend.intelligent_scheduling_login.model.User;
import com.backend.intelligent_scheduling_login.service.UserService;
import com.backend.intelligent_scheduling_login.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
* @author 86136
* @description 针对表【user】的数据库操作Service实现
* @createDate 2023-02-20 23:21:58
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    public String USER_LOGIN_STATE = "userLoginState";

    @Resource
    private UserMapper userMapper;

    private final static String SALT = "wx";


    @Override
    public String userRegister(String account, String password, String checkPassword, String name) {
        if(StringUtils.isAnyBlank(account,password,checkPassword,name)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }

        //账户不能包含特殊字符
//        if(UserInfoCheckUtil.isValidUserAccount(account)){
//            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账户包含特殊字符");
//        }

        //邮箱匹配
        if(!UserInfoCheckUtil.isValidEmail(account)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"邮箱格式不正确");
        }


        //密码和校验密码相同
        if (!password.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次密码输入不相同");
        }

        //账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account", account);
        long count = userMapper.selectCount(queryWrapper);
        if(count>0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"该用户已存在");
        }

        //2.加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());

        //插入数据
        User user = new User();
        user.setAccount(account);
        user.setPassword(encryptPassword);
        user.setType("admin");
        user.setId(UUID.randomUUID().toString());
        user.setName(name);
        boolean saveResult = this.save(user);
        if(!saveResult){
            throw new BusinessException(ErrorCode.NO_AUTH,"保存失败");
        }
        return user.getAccount();
    }

    @Override
    public User userLogin(String account, String password, HttpServletRequest request) {
        //1.校验
        if(StringUtils.isAnyBlank(account,password)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"存在空格");
        }
        if (account.length() < 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账户过短");
        }
        if (password.length() < 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户密码过短");
        }


        //账户不能包含特殊字符
//        if(UserInfoCheckUtil.isValidUserAccount(account)){
//            throw new BusinessException(ErrorCode.PARAMS_ERROR,"存在特殊字符");
//        }


        //2.加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
        //查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account", account);
        queryWrapper.eq("password", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        //用户不存在
        if (user == null) {
            //log.info("user login failed, account cannot match password");
            throw new BusinessException(ErrorCode.NULL_ERROR,"该用户不存在");
        }

        User safetyUser = getSafeUser(user);

        //4.记录用户登录状态
        request.getSession().setAttribute(USER_LOGIN_STATE,user);

        return safetyUser;
    }

    @Override
    public User getSafeUser(User oringinUser) {
        if (oringinUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR,"该用户不存在");
        }
        User safetyUser = new User();
        safetyUser.setId(oringinUser.getId());
        safetyUser.setType(oringinUser.getType());
        safetyUser.setName(oringinUser.getName());
        safetyUser.setAccount(oringinUser.getAccount());
        return safetyUser;
    }


}




