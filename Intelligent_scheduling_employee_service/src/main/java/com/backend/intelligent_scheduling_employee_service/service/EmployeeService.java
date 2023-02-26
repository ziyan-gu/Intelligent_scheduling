package com.backend.intelligent_scheduling_employee_service.service;

import com.backend.intelligent_scheduling_employee_service.model.Employee;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;

import javax.servlet.http.HttpServletRequest;

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

    /**
     *
     * @param id 用户id
     * @param employee 用户信息
     * @return 是否成功
     */
    Boolean modifyEmployeePreferenceService(String id, Employee employee) throws JsonProcessingException;

    /**
     * 用户登录
     *
     * @param email  用户账户
     * @param password 用户密码
     * @param request 请求
     * @return 返回用户信息
     */
    Employee employeeLogin(String email,String password, HttpServletRequest request);

    Employee getSafeEmployee(Employee originEmployee);
}
