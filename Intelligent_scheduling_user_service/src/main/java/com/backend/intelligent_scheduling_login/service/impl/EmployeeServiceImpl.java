package com.backend.intelligent_scheduling_login.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.backend.intelligent_scheduling_login.model.Employee;
import com.backend.intelligent_scheduling_login.service.EmployeeService;
import com.backend.intelligent_scheduling_login.mapper.EmployeeMapper;
import org.springframework.stereotype.Service;

/**
* @author 86136
* @description 针对表【employee】的数据库操作Service实现
* @createDate 2023-03-20 21:17:57
*/
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee>
    implements EmployeeService{

}




