package com.backend.intelligent_scheduling_employee_service.mapper;

import com.backend.intelligent_scheduling_employee_service.model.Employee;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
* @author 86136
* @description 针对表【employee】的数据库操作Mapper
* @createDate 2023-02-23 23:05:29
* @Entity generator.model.Employee
*/
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {

    @Select("SELECT MAX(CAST(SUBSTR(id, LENGTH(#{prefix}) + 2) AS UNSIGNED)) FROM employee WHERE id LIKE CONCAT" +
            "(#{prefix}, " +
            "'_%')")
    Integer findMaxIdByPrefix(@Param("prefix") String prefix);




//    @Select("SELECT MAX(CAST(SUBSTR(id, LENGTH('1_1') + 2) AS UNSIGNED)) FROM employee WHERE id LIKE '1_1\\_%'\n")
//    Integer findMaxId();

}




