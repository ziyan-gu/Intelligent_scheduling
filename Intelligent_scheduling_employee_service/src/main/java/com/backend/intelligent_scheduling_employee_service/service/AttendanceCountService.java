package com.backend.intelligent_scheduling_employee_service.service;

import com.backend.intelligent_scheduling_employee_service.model.AttendanceCount;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 86136
* @description 针对表【attendance_count】的数据库操作Service
* @createDate 2023-03-26 00:17:26
*/
public interface AttendanceCountService extends IService<AttendanceCount> {
    int getCount(String id);

    boolean addCount(String id);
}
