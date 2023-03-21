package com.backend.intelligent_scheduling_employee_service.service;

import com.backend.intelligent_scheduling_employee_service.model.Employee;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 86136
* @description 针对表【employee】的数据库操作Service
* @createDate 2023-02-23 23:05:29
*/
public interface EmployeeService extends IService<Employee> {

    /**
     * 添加员工
     * @param name 姓名
     * @param email 邮箱
     * @param position 位置
     * @return 返回id
     */
//    String addEmployee(String id, String name, String email, Integer position, String store);
    String addNewEmployee(String name, String email, Integer position, String store) throws JsonProcessingException;


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

    /**
     * 获取安全用户
     * @param originEmployee  原始
     * @return 安全
     */
    Employee getSafeEmployee(Employee originEmployee);

    /**
     * 查询店铺名
     * @param id  用户id
     * @return 返回店铺名
     */
    String getStoreByEmployeeId(String id);

    /**
     * 根据店铺名获取员工
     * @param storeName 店铺名
     * @return 员工
     */
    List<Employee> getEmployeesByStore(String storeName);
}
