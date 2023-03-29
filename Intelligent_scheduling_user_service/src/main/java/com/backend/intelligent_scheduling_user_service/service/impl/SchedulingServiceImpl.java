package com.backend.intelligent_scheduling_user_service.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.backend.intelligent_scheduling_user_service.common.ErrorCode;
import com.backend.intelligent_scheduling_user_service.exception.BusinessException;
import com.backend.intelligent_scheduling_user_service.model.response.GetSchedulingByIdResponse;
import com.backend.intelligent_scheduling_user_service.service.AttendanceCountService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.backend.intelligent_scheduling_user_service.model.Scheduling;
import com.backend.intelligent_scheduling_user_service.service.SchedulingService;
import com.backend.intelligent_scheduling_user_service.mapper.SchedulingMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
* @author 86136
* @description 针对表【scheduling】的数据库操作Service实现
* @createDate 2023-03-20 23:27:05
*/
@Service
public class SchedulingServiceImpl extends ServiceImpl<SchedulingMapper, Scheduling>
    implements SchedulingService{

    @Autowired
    private SchedulingMapper schedulingMapper;

    @Autowired
    private AttendanceCountService attendanceCountService;
    @Override
    public Object getScheduleByIdAndDate(String id, Date date) {
        QueryWrapper<Scheduling> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id).eq("date", date);
        Scheduling scheduling = schedulingMapper.selectOne(wrapper);
        return scheduling.getData();
    }

    @Override
    public String changeScheduleByIdAndDate(String id, Date date, Object data) throws JsonProcessingException {

        //数据格式处理
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonData = objectMapper.writeValueAsString(data);

        //是否存在
//        QueryWrapper<Scheduling> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("id", id).eq("date", date);
//        Long count = schedulingMapper.selectCount(queryWrapper);
//        if(count == 0){
//            Scheduling s = new Scheduling();
//            s.setId(id);
//            s.setDate(date);
//            s.setData(jsonData);
//            int insert = schedulingMapper.insert(s);
//            if(insert == 0){
//                throw new BusinessException(ErrorCode.PARAMS_ERROR,"原数据不存在，并且在尝试保存为新数据时失败");
//            }
//            attendanceCountService.updateAttendanceCount(s);
//            return "插入新数据成功";
//        }

        //修改
        UpdateWrapper<Scheduling> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", id).eq("date", date);
        Scheduling scheduling = new Scheduling();
        scheduling.setData(jsonData);


        int result = schedulingMapper.update(scheduling, wrapper);
        if (result == 0){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"保存失败，请重试");
        }
        return "保存成功";
    }

    @Override
    public List<GetSchedulingByIdResponse> getScheduleById(String id){
        QueryWrapper<Scheduling> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id);
        List<Scheduling> schedulings = schedulingMapper.selectList(wrapper);
        if (schedulings.size() == 0){
            throw new BusinessException(ErrorCode.NULL_ERROR,"未找到相关排班");
        }

        schedulings.forEach(scheduling -> scheduling.setData(JSONObject.parse((String) scheduling.getData())));
        List<GetSchedulingByIdResponse> responses = new ArrayList<>();

        for (Scheduling scheduling : schedulings) {
            GetSchedulingByIdResponse response = new GetSchedulingByIdResponse();

            java.util.Date date = scheduling.getDate();
            java.sql.Date sqlDate = new java.sql.Date(date.getTime());

            response.setDate(sqlDate);
            response.setData(scheduling.getData());
            responses.add(response);
        }
        return responses;
    }


}




