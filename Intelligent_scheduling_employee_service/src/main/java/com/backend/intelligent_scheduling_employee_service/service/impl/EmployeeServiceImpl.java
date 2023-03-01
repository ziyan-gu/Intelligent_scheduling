package com.backend.intelligent_scheduling_employee_service.service.impl;

import com.alibaba.nacos.common.utils.StringUtils;
import com.backend.intelligent_scheduling_employee_service.common.ErrorCode;
import com.backend.intelligent_scheduling_employee_service.common.UserInfoCheckUtil;
import com.backend.intelligent_scheduling_employee_service.exception.BusinessException;
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
import javax.servlet.http.HttpServletRequest;

/**
* @author 86136
* @description 针对表【employee】的数据库操作Service实现
* @createDate 2023-02-23 23:05:29
*/
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee>
    implements EmployeeService{

    private final static String SALT = "wx";

    public String USER_LOGIN_STATE = "userLoginState";
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

    @Override
    public Employee employeeLogin(String email, String password, HttpServletRequest request) {
        if(StringUtils.isAnyBlank(email,password)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"存在空格");
        }
        if (password.length() < 6){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户密码过短");
        }

        //2.加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
        //查询用户是否存在
        QueryWrapper<Employee> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        queryWrapper.eq("password", encryptPassword);
        Employee employee = employeeMapper.selectOne(queryWrapper);
        //用户不存在
        if (employee == null) {
            //log.info("user login failed, account cannot match password");
            throw new BusinessException(ErrorCode.NULL_ERROR,"该用户不存在或密码错误");
        }

        Employee safetyEmployee = getSafeEmployee(employee);

        //4.记录用户登录状态
        request.getSession().setAttribute(USER_LOGIN_STATE,employee);

        return safetyEmployee;
    }

    @Override
    public Employee getSafeEmployee(Employee originEmployee) {
        if (originEmployee == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR,"该用户不存在");
        }
        Employee safetyEmployee = new Employee();
        safetyEmployee.setId(originEmployee.getId());
        safetyEmployee.setName(originEmployee.getName());
        safetyEmployee.setEmail(originEmployee.getEmail());
        safetyEmployee.setPosition(originEmployee.getPosition());
        safetyEmployee.setStore(originEmployee.getStore());
        safetyEmployee.setPreferenceValue(originEmployee.getPreferenceValue());


        return safetyEmployee;
    }
}



