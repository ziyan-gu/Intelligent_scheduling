package com.backend.intelligent_scheduling_login.mapper;

import com.backend.intelligent_scheduling_login.model.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 86136
* @description 针对表【user】的数据库操作Mapper
* @createDate 2023-03-02 23:27:54
* @Entity com.backend.intelligent_scheduling_login.model.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




