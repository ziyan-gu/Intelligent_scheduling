package com.backend.intelligent_scheduling_login.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.backend.intelligent_scheduling_login.domain.User;
import com.backend.intelligent_scheduling_login.service.UserService;
import com.backend.intelligent_scheduling_login.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author 86136
* @description 针对表【user】的数据库操作Service实现
* @createDate 2023-02-20 23:21:58
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

}




