package com.backend.intelligent_scheduling_user_service.service;

import com.backend.intelligent_scheduling_user_service.model.Scheduling;
import com.backend.intelligent_scheduling_user_service.model.response.GetSchedulingByIdResponse;
import com.backend.intelligent_scheduling_user_service.model.response.GetAllProcessedLayoutResponse;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.sql.Date;
import java.text.ParseException;
import java.util.List;

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

    String changeScheduleByIdAndDate(String id,Date date, Object data) throws JsonProcessingException;

    List<GetSchedulingByIdResponse>  getScheduleById(String id) throws ParseException;

    /**
     * 获取处理过的门店排班数据
     * @param id 店铺
     * @return 员工数据
     */
    List<GetAllProcessedLayoutResponse>  getProcessedScheduleById(String id) throws ParseException, IOException;


}
