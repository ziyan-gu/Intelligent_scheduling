package com.backend.intelligent_scheduling_user_service.service;

import com.backend.intelligent_scheduling_user_service.model.PassengerFlow;
import com.backend.intelligent_scheduling_user_service.model.response.GetPassengerFlowSum;
import com.backend.intelligent_scheduling_user_service.model.response.GetWeekPassengerFlow;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.sql.Date;
import java.text.ParseException;
import java.util.List;

/**
* @author 86136
* @description 针对表【passenger_flow】的数据库操作Service
* @createDate 2023-03-25 21:45:01
*/
public interface PassengerFlowService extends IService<PassengerFlow> {

    Object getPassengerFlow(String id, Date date);

    boolean setPassengerFlow(String id, Date date, Object data) throws JsonProcessingException;

    List<GetPassengerFlowSum> getPassengerFlowSum(String id, Date date);

    boolean generatePassengerFlow(String id) throws JsonProcessingException, ParseException;

    List<GetWeekPassengerFlow> getPassengerFlowOfWeek(String id, Date date);
}
