package com.backend.intelligent_scheduling_employee_service.service.impl;

import com.backend.intelligent_scheduling_employee_service.common.UserInfoCheckUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.backend.intelligent_scheduling_employee_service.model.Employee;
import com.backend.intelligent_scheduling_employee_service.service.EmployeeService;
import com.backend.intelligent_scheduling_employee_service.mapper.EmployeeMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;

/**
* @author 86136
* @description 针对表【employee】的数据库操作Service实现
* @createDate 2023-02-23 23:05:29
*/
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee>
    implements EmployeeService{

    private final static String SALT = "wx";

    @Resource
    public EmployeeMapper employeeMapper;
    @Override
    public String addEmployee(String id, String name, String email, Integer position, String store) {

        //邮箱匹配
        if(!UserInfoCheckUtil.isValidEmail(email)){
            return null;
        }

        //账户不能重复
        QueryWrapper<Employee> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);
        long count = employeeMapper.selectCount(queryWrapper);
        if(count>0){
            return null;
        }

        //员工起始密码均为123456 进行加密
        Employee employee = new Employee();
        String password = "123456";
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());

        employee.setId(id);
        employee.setName(name);
        employee.setEmail(email);
        employee.setPassword(encryptPassword);
        employee.setPosition(position);
        employee.setStore(store);

        boolean saveResult = this.save(employee);
        if(!saveResult){
            return null;
        }
        return employee.getId();
    }

    @Override
    public Boolean modifyEmployeePreferenceService(String id, Employee employee) throws JsonProcessingException {

        Employee oldEmployee = this.getOne(new QueryWrapper<Employee>().eq("id", id));

        //hashmap -> json
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(employee.getPreferenceValue());
        oldEmployee.setPreferenceValue(json);

        UpdateWrapper<Employee> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id",id);
        boolean result = this.update(oldEmployee,updateWrapper);

        return result;
    }
}




