package com.backend.intelligent_scheduling_user_service.service;

import com.backend.intelligent_scheduling_user_service.model.AttendanceCount;
import com.backend.intelligent_scheduling_user_service.model.Scheduling;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
* @author 86136
* @description 针对表【attendance_count】的数据库操作Service
* @createDate 2023-03-26 15:22:12
*/
public interface AttendanceCountService extends IService<AttendanceCount> {
    void updateAttendanceCount(Scheduling scheduling);

    /**
     * 每天23.30定时启动--统计出勤
     */
    void updateAttendanceScheduling();
}
