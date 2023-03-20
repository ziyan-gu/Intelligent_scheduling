package com.backend.intelligent_scheduling_login.service;

import com.backend.intelligent_scheduling_login.model.Scheduling;
import com.baomidou.mybatisplus.extension.service.IService;

import java.sql.Date;

/**
* @author 86136
* @description 针对表【scheduling】的数据库操作Service
* @createDate 2023-03-20 23:27:05
*/
public interface SchedulingService extends IService<Scheduling> {

    /**
     * 根据日期和id获取排班
     * @param id id
     * @param date 日期
     * @return 排班
     */
    Object getScheduleByIdAndDate(String id, Date date);

}
