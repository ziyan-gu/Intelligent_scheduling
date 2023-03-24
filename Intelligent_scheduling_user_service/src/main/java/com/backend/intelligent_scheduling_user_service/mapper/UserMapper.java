package com.backend.intelligent_scheduling_user_service.mapper;

import com.backend.intelligent_scheduling_user_service.model.User;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
* @author 86136
* @description 针对表【user】的数据库操作Mapper
* @createDate 2023-03-02 23:27:54
* @Entity com.backend.intelligent_scheduling_login.model.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Select("SELECT COUNT(*) FROM user WHERE type = 'admin'")
    int countAdminUsers(QueryWrapper<User> queryWrapperCount);

    // 查询以指定前缀开头的最大编号
//    @Select("SELECT MAX(id) FROM user WHERE id LIKE CONCAT(#{prefix}, '_%')")
    @Select("SELECT MAX(CAST(SUBSTR(id, LENGTH(#{prefix}) + 2) AS UNSIGNED)) FROM user WHERE id LIKE CONCAT(#{prefix}, '_%')")
    String findMaxIdByPrefix(@Param("prefix") String prefix);
}




