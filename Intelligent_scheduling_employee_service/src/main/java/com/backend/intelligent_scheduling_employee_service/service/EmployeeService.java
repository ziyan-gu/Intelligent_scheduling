package com.backend.intelligent_scheduling_employee_service.service;

import com.backend.intelligent_scheduling_employee_service.model.Employee;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 86136
* @description 针对表【employee】的数据库操作Service
* @createDate 2023-02-23 23:05:29
*/
public interface EmployeeService extends IService<Employee> {

    /**
     * 添加员工
     * @param id id
     * @param name 姓名
     * @param email 邮箱
     * @param position 位置
     * @param store 门店
     * @return 返回id
     */
    String addEmployee(String id, String name, String email, Integer position, String store);

}
