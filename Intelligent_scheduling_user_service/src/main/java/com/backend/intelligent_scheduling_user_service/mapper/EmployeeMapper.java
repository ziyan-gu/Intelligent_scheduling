package com.backend.intelligent_scheduling_user_service.mapper;

import com.backend.intelligent_scheduling_user_service.model.Employee;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 86136
* @description 针对表【employee】的数据库操作Mapper
* @createDate 2023-03-20 21:17:57
* @Entity generator.model.Employee
*/

@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {

}




