package com.backend.intelligent_scheduling_employee_service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.backend.intelligent_scheduling_employee_service.model.Scheduling;
import com.backend.intelligent_scheduling_employee_service.service.SchedulingService;
import com.backend.intelligent_scheduling_employee_service.mapper.SchedulingMapper;
import org.springframework.stereotype.Service;

/**
* @author 86136
* @description 针对表【scheduling】的数据库操作Service实现
* @createDate 2023-03-26 15:04:42
*/
@Service
public class SchedulingServiceImpl extends ServiceImpl<SchedulingMapper, Scheduling>
    implements SchedulingService{

}




